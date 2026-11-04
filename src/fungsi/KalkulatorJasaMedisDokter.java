package fungsi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kalkulator Jasa Medis Dokter BPJS
 * Mengelola kalkulasi remunerasi dokter berbasis klaim paket INA-CBG (Umbal)
 * dengan dukungan Dynamic Rule Engine dan tabel konfigurasi set_aturan_jm_bpjs
 * mencakup 39 aturan baku RS Indriati Boyolali (Rawat Jalan & Rawat Inap).
 * 
 * @author it-rsib & AI Engineering Team
 */
public class KalkulatorJasaMedisDokter {

    // Struktur data in-memory untuk 1 aturan JM BPJS
    public static class RuleAturanJM {
        private final String idAturan;
        private final String kategori;
        private final String namaAturan;
        private final String kondisiPemicu;
        private final String metodeHitung;
        private final double nilaiP1;
        private final double nilaiP2;
        private final double nilaiP3;
        private final boolean aktif;
        private final String keterangan;

        public RuleAturanJM(String idAturan, String kategori, String namaAturan, String kondisiPemicu,
                            String metodeHitung, double nilaiP1, double nilaiP2, double nilaiP3, boolean aktif, String keterangan) {
            this.idAturan = idAturan;
            this.kategori = kategori;
            this.namaAturan = namaAturan;
            this.kondisiPemicu = kondisiPemicu;
            this.metodeHitung = metodeHitung;
            this.nilaiP1 = nilaiP1;
            this.nilaiP2 = nilaiP2;
            this.nilaiP3 = nilaiP3;
            this.aktif = aktif;
            this.keterangan = keterangan;
        }

        public RuleAturanJM(String idAturan, String kategori, String namaAturan, String kondisiPemicu,
                            String metodeHitung, double nilaiP1, double nilaiP2, boolean aktif, String keterangan) {
            this(idAturan, kategori, namaAturan, kondisiPemicu, metodeHitung, nilaiP1, nilaiP2, 0.0, aktif, keterangan);
        }

        public String getIdAturan() { return idAturan; }
        public String getKategori() { return kategori; }
        public String getNamaAturan() { return namaAturan; }
        public String getKondisiPemicu() { return kondisiPemicu; }
        public String getMetodeHitung() { return metodeHitung; }
        public double getNilaiP1() { return nilaiP1; }
        public double getNilaiP2() { return nilaiP2; }
        public double getNilaiP3() { return nilaiP3; }
        public boolean isAktif() { return aktif; }
        public String getKeterangan() { return keterangan; }
    }

    // Cache aturan in-memory untuk efisiensi tinggi (menghindari query database berulang pada ribuan baris)
    private static final Map<String, RuleAturanJM> ruleCache = new ConcurrentHashMap<>();
    private static volatile boolean isInitialized = false;

    /**
     * Inisialisasi skema database set_aturan_jm_bpjs secara idempotent
     * dan memasukkan seed data ketentuan default jika belum terdaftar.
     */
    public static synchronized void setupDatabaseTable(Connection conn) {
        if (conn == null) {
            conn = koneksiDB.condb();
        }
        if (conn == null) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            // 1. Buat tabel jika belum ada
            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS set_aturan_jm_bpjs ("
                + "  id_aturan varchar(40) NOT NULL,"
                + "  kategori enum('Rawat Jalan','Rawat Inap','Tindakan Khusus') NOT NULL,"
                + "  nama_aturan varchar(100) NOT NULL,"
                + "  kondisi_pemicu varchar(150) NOT NULL,"
                + "  metode_hitung enum('Flat','Persentase','Formula Selisih','Proporsional DPJP','Prorata Persentase') NOT NULL,"
                + "  nilai_parameter_1 double NOT NULL DEFAULT '0',"
                + "  nilai_parameter_2 double NOT NULL DEFAULT '0',"
                + "  nilai_parameter_3 double NOT NULL DEFAULT '0',"
                + "  status_aktif enum('Aktif','Tidak Aktif') NOT NULL DEFAULT 'Aktif',"
                + "  keterangan varchar(200) NOT NULL DEFAULT '-',"
                + "  PRIMARY KEY (id_aturan)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;"
            );

            // Migrasi dinamis kolom metode_hitung agar mendukung 'Prorata Persentase' bila tabel lama sudah ada
            try {
                stmt.executeUpdate(
                    "ALTER TABLE set_aturan_jm_bpjs MODIFY COLUMN metode_hitung "
                    + "enum('Flat','Persentase','Formula Selisih','Proporsional DPJP','Prorata Persentase') NOT NULL"
                );
            } catch (Exception e) {
                // Diabaikan jika skema sudah sesuai
            }

            // Migrasi dinamis kolom nilai_parameter_3
            try {
                stmt.executeUpdate("ALTER TABLE set_aturan_jm_bpjs ADD COLUMN nilai_parameter_3 double NOT NULL DEFAULT '0' AFTER nilai_parameter_2");
            } catch (Exception e) {
                // Diabaikan jika kolom sudah ada
            }

            // 2. 39 Master Aturan Baku RS Indriati Boyolali (Rawat Jalan & Rawat Inap)
            String[][] masterRules = {
                // RAWAT JALAN (13 Aturan)
                {"RALAN_HD_SINGLE_USE", "Rawat Jalan", "Hemodialisa (HD Single Use) DPJP", "Tindakan HD Single Use dalam 1 SEP; pemeriksaan dokter diabaikan (Rp 0)", "Flat", "75000", "0", "0", "Aktif", "Tarif flat JM dokter spesialis tindakan Hemodialisa Single Use"},
                {"RALAN_AV_SHUNT", "Rawat Jalan", "AV Shunt + Pemeriksaan DPJP", "Tindakan AV Shunt dalam 1 SEP; periksa dr. Sp dinilai Rp 72.000", "Flat", "575000", "72000", "0", "Aktif", "P1=Tarif tindakan AV Shunt, P2=Tambahan jika ada pemeriksaan dr. Spesialis di SEP yang sama"},
                {"RALAN_ESWL", "Rawat Jalan", "ESWL DPJP", "Tindakan ESWL dalam 1 SEP; pemeriksaan dokter spesialis diabaikan (Rp 0)", "Flat", "822760", "0", "0", "Aktif", "Tarif flat JM dokter tindakan ESWL (sudah termasuk periksa)"},
                {"RALAN_DOUBLE_LUMEN", "Rawat Jalan", "Double Lumen + Pemeriksaan DPJP", "Tindakan Double Lumen dalam 1 SEP; periksa dr. Sp dinilai Rp 72.000", "Flat", "500000", "72000", "0", "Aktif", "P1=Tarif Double Lumen, P2=Tambahan jika ada pemeriksaan dr. Spesialis di SEP yang sama"},
                {"RALAN_DOKTER_UMUM_IGD", "Rawat Jalan", "Dokter Umum di Pelayanan IGD", "Pelayanan pemeriksaan oleh Dokter Umum di IGD", "Flat", "0", "0", "0", "Aktif", "Dokter umum IGD tidak mendapatkan pembagian JM klaim INA-CBG"},
                {"RALAN_SPESIALIS_NON_TINDAKAN", "Rawat Jalan", "Pemeriksaan Spesialis Tanpa Tindakan", "1 DPJP Spesialis tunggal, murni periksa tanpa tindakan", "Flat", "72000", "0", "0", "Aktif", "Tarif dasar per pemeriksaan dokter spesialis rawat jalan tanpa tindakan"},
                {"RALAN_SPESIALIS_TINDAKAN", "Rawat Jalan", "Pemeriksaan Spesialis Disertai Tindakan", "1 DPJP Spesialis + ada tindakan (Umbal > Nilai Batas)", "Formula Selisih", "196100", "20", "0", "Aktif", "P1=Batas minimal klaim, P2=% selisih (Rp 72.000 + 20% x (INA-CBG - 196.100))"},
                {"RALAN_KFR_PERIKSA_SP", "Rawat Jalan", "KFR: Billing Hanya Pemeriksaan Spesialis", "Klinik KFR (Poli U0036) murni pemeriksaan dokter spesialis", "Flat", "36000", "0", "0", "Aktif", "Billing hanya pemeriksaan dokter spesialis KFR (dr. Anwar / dr. Dian)"},
                {"RALAN_KFR_TINDAKAN", "Rawat Jalan", "KFR: Billing Hanya Tindakan", "Klinik KFR (Poli U0036) hanya tindakan KFR tanpa periksa Sp", "Flat", "35000", "0", "0", "Aktif", "Billing hanya tindakan dokter KFR tanpa pemeriksaan spesialis"},
                {"RALAN_KFR_RABER", "Rawat Jalan", "KFR: Pasien Rawat Bersama (Raber)", "Klinik KFR (Poli U0036) sebagai dokter konsul/raber", "Flat", "36000", "0", "0", "Aktif", "Seluruh dr. Sp KFR berstatus raber sehingga tarif pemeriksaan adalah Rp 36.000"},
                {"RALAN_RUJUKAN_INT_NON_TINDAKAN", "Rawat Jalan", "Rujukan Internal 2 Dokter: Tanpa Tindakan", "1 SEP ada 2 dokter spesialis, keduanya murni periksa", "Flat", "36000", "0", "0", "Aktif", "Tarif Rp 72.000 dibagi 2 = Rp 36.000 per dokter"},
                {"RALAN_RUJUKAN_INT_1_TINDAKAN", "Rawat Jalan", "Rujukan Internal 2 Dokter: 1 Tindakan", "1 dokter bertindakan, 1 dokter murni periksa", "Formula Selisih", "196100", "20", "0", "Aktif", "Dr non-tindakan Rp 36.000, Dr bertindakan Rp 36.000 + 20% x (INA-CBG - 196.100)"},
                {"RALAN_RUJUKAN_INT_2_TINDAKAN", "Rawat Jalan", "Rujukan Internal 2 Dokter: Keduanya Tindakan", "Kedua dokter sama-sama melakukan tindakan medis", "Formula Selisih", "196100", "20", "0", "Aktif", "Dibagi 2 proporsional: (Rp 72.000 + 20% x (INA-CBG - 196.100)) / 2"},

                // RAWAT INAP NON-OPERASI (10 Aturan)
                {"RANAP_NON_OP_1DPJP_VISIT_GT1", "Rawat Inap", "Non-OP 1 DPJP: Visite > 1 Kali", "1 DPJP sendiri, frekuensi visite > 1 kali", "Persentase", "9", "0", "0", "Aktif", "DPJP Utama mendapat 9% dari total klaim (INA-CBG + TopUp)"},
                {"RANAP_NON_OP_1DPJP_VISIT_EQ1", "Rawat Inap", "Non-OP 1 DPJP: Visite = 1 Kali", "1 DPJP sendiri, frekuensi visite = 1 kali", "Persentase", "6", "0", "0", "Aktif", "DPJP Utama mendapat 6% dari total klaim (INA-CBG + TopUp)"},
                {"RANAP_NON_OP_1DPJP_TELEKONSUL", "Rawat Inap", "Non-OP 1 DPJP: Hanya Telekonsultasi", "1 DPJP sendiri, hanya telekonsultasi tanpa visit fisik", "Persentase", "4", "0", "0", "Aktif", "DPJP Utama mendapat 4% dari total klaim (INA-CBG + TopUp)"},
                {"RANAP_NON_OP_1TANDEM_DPJP_GT1", "Rawat Inap", "Non-OP DPJP+1 Tandem: DPJP Visite > 1x", "1 DPJP (visite > 1x) dan didampingi 1 dokter tandem", "Persentase", "8", "2", "0", "Aktif", "P1=8% untuk DPJP, P2=2% untuk 1 dokter tandem"},
                {"RANAP_NON_OP_1TANDEM_DPJP_EQ1", "Rawat Inap", "Non-OP DPJP+1 Tandem: DPJP Visite = 1x", "1 DPJP (visite 1x) dan didampingi 1 dokter tandem", "Persentase", "6", "2", "0", "Aktif", "P1=6% untuk DPJP, P2=2% untuk 1 dokter tandem"},
                {"RANAP_NON_OP_1TANDEM_DPJP_TELE", "Rawat Inap", "Non-OP DPJP+1 Tandem: DPJP Telekonsultasi", "1 DPJP (telekonsul) dan didampingi 1 dokter tandem", "Persentase", "4", "2", "0", "Aktif", "P1=4% DPJP (maks Rp 40.000), P2=2% Tandem"},
                {"RANAP_NON_OP_PLAFON_1TANDEM_TELE", "Rawat Inap", "Plafon DPJP (Telekonsultasi)", "Batas nominal maksimum bagi DPJP saat hanya telekonsul", "Flat", "40000", "0", "0", "Aktif", "Maksimal Rp 40.000 jika 4% klaim melebihi Rp 40.000"},
                {"RANAP_NON_OP_MULTI_DPJP_GT1", "Rawat Inap", "Non-OP DPJP+Multi Tandem: DPJP Visite > 1x", "1 DPJP (visite > 1x) dan rawat bersama > 1 dokter tandem", "Prorata Persentase", "6", "4", "0", "Aktif", "P1=6% untuk DPJP, P2=4% total alokasi seluruh tandem secara Prorata"},
                {"RANAP_NON_OP_MULTI_DPJP_EQ1", "Rawat Inap", "Non-OP DPJP+Multi Tandem: DPJP Visite = 1x", "1 DPJP (visite 1x) dan rawat bersama > 1 dokter tandem", "Prorata Persentase", "5", "3", "0", "Aktif", "P1=5% untuk DPJP, P2=3% total alokasi seluruh tandem secara Prorata"},
                {"RANAP_NON_OP_MULTI_DPJP_TELE", "Rawat Inap", "Non-OP DPJP+Multi Tandem: DPJP Telekonsul", "1 DPJP (telekonsul) dan rawat bersama > 1 dokter tandem", "Prorata Persentase", "4", "2", "0", "Aktif", "P1=4% untuk DPJP, P2=2% total alokasi seluruh tandem secara Prorata"},

                // KASUS KHUSUS & ALIH RAWAT (5 Aturan)
                {"RANAP_ALIH_RAWAT_DPJP", "Rawat Inap", "Alih Rawat: Transfer DPJP", "DPJP pada SEP VClaim berbeda dengan DPJP saat ini di SIMRS", "Persentase", "4", "4", "3", "Aktif", "P1=4% DPJP SEP, P2=4% DPJP SIMRS, P3=3% total Prorata jika ada tandem lain"},
                {"RANAP_ANAK_POST_SC", "Rawat Inap", "Bayi Baru Lahir: dr. Anak Post SC", "1 SEP terdapat 2 No RM (bayi = RM baru) dengan tindakan SC", "Persentase", "4", "0", "0", "Aktif", "Dokter Spesialis Anak tanpa tindakan mendapat 4% dari klaim (INA-CBG + TopUp)"},
                {"RANAP_ANAK_POST_PARTUM", "Rawat Inap", "Bayi Baru Lahir: dr. Anak Post Partum", "1 SEP terdapat 2 No RM (bayi = RM baru) persalinan normal", "Flat", "60000", "0", "0", "Aktif", "Dokter Spesialis Anak tanpa tindakan mendapat Rp 60.000 flat"},
                {"RANAP_TK_DOUBLE_LUMEN", "Tindakan Khusus", "Double Lumen (Ranap)", "Tindakan Double Lumen pada pasien rawat inap", "Flat", "500000", "0", "0", "Aktif", "Tarif flat JM dokter tindakan Double Lumen di ruang rawat inap"},
                {"RANAP_TK_AV_SHUNT", "Tindakan Khusus", "AV Shunt (Ranap)", "Tindakan AV Shunt pada pasien rawat inap", "Flat", "575000", "0", "0", "Aktif", "Tarif flat JM dokter tindakan AV Shunt di ruang rawat inap"},

                // RAWAT INAP BEDAH / OPERASI (11 Aturan)
                {"RANAP_OP_1DPJP_VISIT_GT1", "Rawat Inap", "Operasi 1 DPJP: Visite Post-OP > 1x", "Operator = DPJP SEP tunggal, visite post-op > 1 kali", "Persentase", "20", "0", "0", "Aktif", "DPJP Operator mendapat 20% dari total klaim (INA-CBG + TopUp)"},
                {"RANAP_OP_1DPJP_VISIT_EQ1", "Rawat Inap", "Operasi 1 DPJP: Visite Post-OP = 1x", "Operator = DPJP SEP tunggal, visite post-op = 1 kali", "Persentase", "18", "0", "0", "Aktif", "DPJP Operator mendapat 18% dari total klaim (INA-CBG + TopUp)"},
                {"RANAP_OP_1DPJP_TELEKONSUL", "Rawat Inap", "Operasi 1 DPJP: Telekonsul Tanpa Visit", "Operator = DPJP SEP tunggal, telekonsul / tidak visite", "Persentase", "16", "0", "0", "Aktif", "DPJP Operator mendapat 16% dari total klaim (INA-CBG + TopUp)"},
                {"RANAP_OP_1TANDEM_VISIT_GT1", "Rawat Inap", "Operasi DPJP+Tandem: Visite Post-OP > 1x", "Operator = DPJP SEP + ada dokter raber, DPJP visit > 1x", "Prorata Persentase", "17", "3", "0", "Aktif", "P1=17% DPJP Operator, P2=3% total tandem secara Prorata"},
                {"RANAP_OP_1TANDEM_VISIT_EQ1", "Rawat Inap", "Operasi DPJP+Tandem: Visite Post-OP = 1x", "Operator = DPJP SEP + ada dokter raber, DPJP visit = 1x", "Prorata Persentase", "16", "2", "0", "Aktif", "P1=16% DPJP Operator, P2=2% total tandem secara Prorata"},
                {"RANAP_OP_1TANDEM_TELEKONSUL", "Rawat Inap", "Operasi DPJP+Tandem: Telekonsul", "Operator = DPJP SEP + ada dokter raber, DPJP telekonsul", "Prorata Persentase", "15", "2", "0", "Aktif", "P1=15% DPJP Operator, P2=2% total tandem secara Prorata"},
                {"RANAP_OP_2DOKTER_TANPA_RABER", "Rawat Inap", "Operasi 2 Dokter / 2 Tindakan: Tanpa Raber", "1 Pasien 1 SEP, 2 tindakan operasi oleh 2 dokter berbeda", "Persentase", "10", "10", "0", "Aktif", "P1=10% untuk Dokter 1, P2=10% untuk Dokter 2"},
                {"RANAP_OP_2DOKTER_ADA_RABER", "Rawat Inap", "Operasi 2 Dokter / 2 Tindakan: Ada Raber", "2 dokter operasi berbeda + ada dokter tandem/raber lain", "Persentase", "9", "9", "2", "Aktif", "P1=9% untuk Dokter 1, P2=9% untuk Dokter 2, P3=2% total tandem secara Prorata"},
                {"RANAP_OP_NON_DPJP_TANPA_RABER", "Rawat Inap", "Operator Bukan DPJP SEP: Tanpa Raber", "DPJP SEP tidak operasi, operasi oleh dokter bedah lain", "Persentase", "6", "14", "0", "Aktif", "P1=6% untuk Dokter DPJP SEP, P2=14% untuk Dokter Operator"},
                {"RANAP_OP_NON_DPJP_ADA_RABER", "Rawat Inap", "Operator Bukan DPJP SEP: Ada Raber", "DPJP SEP tidak operasi + dokter operator + dokter raber", "Persentase", "4", "14", "3", "Aktif", "P1=4% DPJP SEP, P2=14% Operator, P3=3% total tandem secara Prorata"},
                {"RANAP_ANESTESI_PORSI_OP", "Rawat Inap", "Dokter Anestesi Operasi (Universal)", "Seluruh tindakan operasi bedah di IBS", "Proporsional DPJP", "40", "0", "0", "Aktif", "Dokter Anestesi selalu menerima 40% dari hak JM Dokter Operator"}
            };

            // Seed secara idempotent menggunakan INSERT IGNORE agar konfigurasi custom pengguna tidak tertimpa
            String sqlInsert = "INSERT IGNORE INTO set_aturan_jm_bpjs "
                + "(id_aturan, kategori, nama_aturan, kondisi_pemicu, metode_hitung, nilai_parameter_1, nilai_parameter_2, nilai_parameter_3, status_aktif, keterangan) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                for (String[] r : masterRules) {
                    psInsert.setString(1, r[0]);
                    psInsert.setString(2, r[1]);
                    psInsert.setString(3, r[2]);
                    psInsert.setString(4, r[3]);
                    psInsert.setString(5, r[4]);
                    psInsert.setDouble(6, Double.parseDouble(r[5]));
                    psInsert.setDouble(7, Double.parseDouble(r[6]));
                    psInsert.setDouble(8, Double.parseDouble(r[7]));
                    psInsert.setString(9, r[8]);
                    psInsert.setString(10, r[9]);
                    psInsert.executeUpdate();
                }
            }

            // Sinkronisasi otomatis aturan 3-pihak bagi database yang sudah memiliki baris lama
            try {
                stmt.executeUpdate("UPDATE set_aturan_jm_bpjs SET nilai_parameter_3 = 3 WHERE id_aturan = 'RANAP_ALIH_RAWAT_DPJP' AND nilai_parameter_3 = 0");
                stmt.executeUpdate("UPDATE set_aturan_jm_bpjs SET nilai_parameter_2 = 4 WHERE id_aturan = 'RANAP_ALIH_RAWAT_DPJP' AND nilai_parameter_2 = 3");
                stmt.executeUpdate("UPDATE set_aturan_jm_bpjs SET nilai_parameter_3 = 2 WHERE id_aturan = 'RANAP_OP_2DOKTER_ADA_RABER' AND nilai_parameter_3 = 0");
                stmt.executeUpdate("UPDATE set_aturan_jm_bpjs SET nilai_parameter_3 = 3 WHERE id_aturan = 'RANAP_OP_NON_DPJP_ADA_RABER' AND nilai_parameter_3 = 0");
            } catch (Exception e) {
                // Diabaikan jika update tidak diperlukan
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Inisialisasi set_aturan_jm_bpjs: " + e);
        }

        // Muat ulang cache in-memory
        reloadCache(conn);
    }

    /**
     * Memuat ulang cache aturan dari database ke dalam memori.
     */
    public static synchronized void reloadCache(Connection conn) {
        if (conn == null) {
            conn = koneksiDB.condb();
        }
        if (conn == null) {
            return;
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT id_aturan, kategori, nama_aturan, kondisi_pemicu, metode_hitung, "
                 + "nilai_parameter_1, nilai_parameter_2, nilai_parameter_3, status_aktif, keterangan FROM set_aturan_jm_bpjs")) {

            ruleCache.clear();
            while (rs.next()) {
                String id = rs.getString("id_aturan");
                RuleAturanJM rule = new RuleAturanJM(
                    id,
                    rs.getString("kategori"),
                    rs.getString("nama_aturan"),
                    rs.getString("kondisi_pemicu"),
                    rs.getString("metode_hitung"),
                    rs.getDouble("nilai_parameter_1"),
                    rs.getDouble("nilai_parameter_2"),
                    rs.getDouble("nilai_parameter_3"),
                    "Aktif".equalsIgnoreCase(rs.getString("status_aktif")),
                    rs.getString("keterangan")
                );
                ruleCache.put(id, rule);
            }
            isInitialized = true;
        } catch (Exception e) {
            System.out.println("Notifikasi reloadCache set_aturan_jm_bpjs: " + e);
        }
    }

    private static void pastikanCacheTersedia() {
        if (!isInitialized || ruleCache.isEmpty()) {
            setupDatabaseTable(koneksiDB.condb());
        }
    }

    /**
     * Cek apakah suatu aturan terdaftar dan berstatus Aktif.
     */
    public static boolean isRuleAktif(String idAturan, boolean defaultJikaTidakAda) {
        pastikanCacheTersedia();
        RuleAturanJM rule = ruleCache.get(idAturan);
        if (rule != null) {
            return rule.isAktif();
        }
        return defaultJikaTidakAda;
    }

    /**
     * Mengambil Nilai Parameter 1 dari aturan tertentu.
     */
    public static double getParameter1(String idAturan, double defaultFallback) {
        pastikanCacheTersedia();
        RuleAturanJM rule = ruleCache.get(idAturan);
        if (rule != null) {
            return rule.getNilaiP1();
        }
        return defaultFallback;
    }

    /**
     * Mengambil Nilai Parameter 2 dari aturan tertentu.
     */
    public static double getParameter2(String idAturan, double defaultFallback) {
        pastikanCacheTersedia();
        RuleAturanJM rule = ruleCache.get(idAturan);
        if (rule != null) {
            return rule.getNilaiP2();
        }
        return defaultFallback;
    }

    /**
     * Mengambil Nilai Parameter 3 dari aturan tertentu.
     */
    public static double getParameter3(String idAturan, double defaultFallback) {
        pastikanCacheTersedia();
        RuleAturanJM rule = ruleCache.get(idAturan);
        if (rule != null) {
            return rule.getNilaiP3();
        }
        return defaultFallback;
    }

    /**
     * Mengambil persentase alokasi dokter anestesi pada rawat inap (default 40% = 0.40).
     */
    public static double getPersenAnestesiRanap() {
        if (isRuleAktif("RANAP_ANESTESI_PORSI_OP", true)) {
            return getParameter1("RANAP_ANESTESI_PORSI_OP", 40.0) / 100.0;
        } else if (isRuleAktif("RANAP_ANESTESI_OP", true)) {
            return getParameter1("RANAP_ANESTESI_OP", 40.0) / 100.0;
        }
        return 0.40;
    }

    /**
     * Mengambil persentase default rawat inap untuk kasus operasi atau non-operasi (backward compatibility).
     */
    public static double getPersenDefaultRanap(boolean isOp, boolean isDpjp) {
        if (isOp) {
            if (isRuleAktif("RANAP_OP_1DPJP_VISIT_GT1", true)) {
                return getParameter1("RANAP_OP_1DPJP_VISIT_GT1", 20.0);
            } else if (isRuleAktif("RANAP_BEDAH_OP_DPJP", true)) {
                return getParameter1("RANAP_BEDAH_OP_DPJP", 20.0);
            }
            return 20.0;
        } else {
            if (isDpjp) {
                if (isRuleAktif("RANAP_NON_OP_1DPJP_VISIT_GT1", true)) {
                    return getParameter1("RANAP_NON_OP_1DPJP_VISIT_GT1", 9.0);
                } else if (isRuleAktif("RANAP_NON_OP_DPJP", true)) {
                    return getParameter1("RANAP_NON_OP_DPJP", 9.0);
                }
                return 9.0;
            } else {
                if (isRuleAktif("RANAP_NON_OP_MULTI_DPJP_GT1", true)) {
                    return getParameter2("RANAP_NON_OP_MULTI_DPJP_GT1", 4.0);
                } else if (isRuleAktif("RANAP_NON_OP_PENDAMPING", true)) {
                    return getParameter1("RANAP_NON_OP_PENDAMPING", 4.0);
                }
                return 4.0;
            }
        }
    }

    /**
     * Penghitungan Jasa Medis Dokter Rawat Jalan lengkap sesuai 13 Aturan Baku Ralan.
     * Evaluasi berbasis 1 No. SEP.
     */
    public static double hitungJasaMedisDokterRawatJalanLengkap(
            double biayaMedisDisetujui,
            boolean isTindakan,
            boolean isPeriksaSpesialis,
            boolean isDPJP,
            boolean isRaber,
            boolean isHD,
            boolean isDoubleLumen,
            boolean isAVShunt,
            boolean isESWL,
            boolean isKFR,
            boolean isDokterUmum,
            boolean isRujukanInternal2Dokter,
            boolean partnerHasTindakan) {

        pastikanCacheTersedia();

        // 1. Evaluasi Dokter Umum di IGD
        if (isDokterUmum && isRuleAktif("RALAN_DOKTER_UMUM_IGD", true)) {
            return getParameter1("RALAN_DOKTER_UMUM_IGD", 0.0);
        }

        // 2. Evaluasi Hemodialisa (Sistem Single Use)
        // Jika dalam 1 SEP ada Single Use, dokter dapat Rp 75.000; pemeriksaan spesialis diabaikan
        if (isHD && isRuleAktif("RALAN_HD_SINGLE_USE", true)) {
            return getParameter1("RALAN_HD_SINGLE_USE", 75000.0);
        }

        // 3. Evaluasi ESWL
        // Jika dalam 1 SEP ada ESWL, dokter dapat Rp 822.760; pemeriksaan diabaikan (Rp 0)
        if (isESWL && isRuleAktif("RALAN_ESWL", true)) {
            return getParameter1("RALAN_ESWL", 822760.0);
        }

        // 4. Evaluasi AV Shunt
        // AV Shunt = Rp 575.000; jika terdapat kunjungan pemeriksaan di SEP yang sama, periksa diberi Rp 72.000
        if (isAVShunt && isRuleAktif("RALAN_AV_SHUNT", true)) {
            double base = getParameter1("RALAN_AV_SHUNT", 575000.0);
            if (isPeriksaSpesialis) {
                base += getParameter2("RALAN_AV_SHUNT", 72000.0);
            }
            return base;
        }

        // 5. Evaluasi Double Lumen
        // Double Lumen = Rp 500.000; jika terdapat kunjungan periksa di SEP yang sama, periksa diberi Rp 72.000
        if (isDoubleLumen && isRuleAktif("RALAN_DOUBLE_LUMEN", true)) {
            double base = getParameter1("RALAN_DOUBLE_LUMEN", 500000.0);
            if (isPeriksaSpesialis) {
                base += getParameter2("RALAN_DOUBLE_LUMEN", 72000.0);
            }
            return base;
        }

        // 6. Evaluasi Klinik KFR (Poli U0036 - dr. Anwar & dr. Dian)
        if (isKFR) {
            if (isRaber && isRuleAktif("RALAN_KFR_RABER", true)) {
                return getParameter1("RALAN_KFR_RABER", 36000.0);
            }
            if (isTindakan && !isPeriksaSpesialis && isRuleAktif("RALAN_KFR_TINDAKAN", true)) {
                return getParameter1("RALAN_KFR_TINDAKAN", 35000.0);
            }
            if (isRuleAktif("RALAN_KFR_PERIKSA_SP", true)) {
                return getParameter1("RALAN_KFR_PERIKSA_SP", 36000.0);
            }
            return 36000.0;
        }

        // 7. Evaluasi Rujukan Internal (2 Dokter dalam 1 SEP)
        if (isRujukanInternal2Dokter) {
            double ambang = getParameter1("RALAN_RUJUKAN_INT_1_TINDAKAN", 196100.0);
            double persenSelisih = getParameter2("RALAN_RUJUKAN_INT_1_TINDAKAN", 20.0);

            // Kasus 1: Kedua dokter tanpa tindakan (murni periksa)
            if (!isTindakan && !partnerHasTindakan) {
                return getParameter1("RALAN_RUJUKAN_INT_NON_TINDAKAN", 36000.0);
            }
            // Kasus 2: Kedua dokter sama-sama melakukan tindakan
            if (isTindakan && partnerHasTindakan) {
                double totalRumus = 72000.0 + ((persenSelisih / 100.0) * Math.max(0, biayaMedisDisetujui - ambang));
                return totalRumus / 2.0;
            }
            // Kasus 3: Hanya 1 dokter yang bertindakan
            if (isTindakan && !partnerHasTindakan) {
                return 36000.0 + ((persenSelisih / 100.0) * Math.max(0, biayaMedisDisetujui - ambang));
            } else {
                return 36000.0;
            }
        }

        // 8. Evaluasi Dokter Rawat Bersama (Raber / Konsul Bukan DPJP Tunggal)
        if (!isDPJP && isRaber && isRuleAktif("RALAN_RUJUKAN_INT_NON_TINDAKAN", true)) {
            return getParameter1("RALAN_RUJUKAN_INT_NON_TINDAKAN", 36000.0);
        }

        // 9. Evaluasi 1 DPJP Spesialis Disertai Tindakan
        if (isDPJP && isTindakan) {
            double batasThreshold = getParameter1("RALAN_SPESIALIS_TINDAKAN", 196100.0);
            double persenSelisih = getParameter2("RALAN_SPESIALIS_TINDAKAN", 20.0);
            double tarifDasar = getParameter1("RALAN_SPESIALIS_NON_TINDAKAN", 72000.0);

            if (biayaMedisDisetujui > batasThreshold && isRuleAktif("RALAN_SPESIALIS_TINDAKAN", true)) {
                return tarifDasar + ((persenSelisih / 100.0) * (biayaMedisDisetujui - batasThreshold));
            }
        }

        // 10. Evaluasi 1 DPJP Spesialis Tanpa Tindakan (Standar Flat Rp 72.000)
        if (isDPJP && isRuleAktif("RALAN_SPESIALIS_NON_TINDAKAN", true)) {
            return getParameter1("RALAN_SPESIALIS_NON_TINDAKAN", 72000.0);
        }

        // Fallback default
        return isDPJP ? 72000.0 : 0.0;
    }

    /**
     * Backward-compatible overload untuk penghitungan Rawat Jalan lama.
     */
    public static double hitungJasaMedisDokterRawatJalan(
            double biayaMedisDisetujui,
            boolean isTindakan,
            boolean isTerapi,
            boolean isDPJP,
            boolean isRaber,
            boolean isHD,
            boolean isAudiometri,
            boolean isCDL,
            boolean isDoubleLumen,
            boolean isAVShunt,
            boolean isESWL,
            boolean isEEG,
            boolean isDokterUmum) {

        return hitungJasaMedisDokterRawatJalanLengkap(
            biayaMedisDisetujui,
            isTindakan,
            true, // isPeriksaSpesialis default true
            isDPJP,
            isRaber,
            isHD,
            isDoubleLumen || isCDL,
            isAVShunt,
            isESWL,
            isTerapi, // Terapi KFR
            isDokterUmum,
            false,
            false
        );
    }

    /**
     * Backward-compatible overload 7 parameter.
     */
    public static double hitungJasaMedisDokterRawatJalan(
            double biayaMedisDisetujui,
            boolean isTindakan,
            boolean isTerapi,
            boolean isDPJP,
            boolean isRaber,
            boolean isHD,
            boolean isAudiometri) {

        return hitungJasaMedisDokterRawatJalan(
                biayaMedisDisetujui, isTindakan, isTerapi, isDPJP, isRaber, isHD, isAudiometri,
                false, false, false, false, false, false
        );
    }

    /**
     * Perhitungan Jasa Medis Dokter Rawat Inap komprehensif mengikuti aturan baku RS Indriati Boyolali.
     * Basis nilai: (INA-CBG + Top Up).
     */
    public static double hitungJasaMedisDokterRawatInap(
            double totalKlaim,
            boolean isOperasi,
            boolean isDpjp,
            boolean isDpjpSep,
            boolean isOperator,
            boolean isAnestesi,
            int visitCount,
            int dpjpVisitCount,
            int tandemCount,
            int myTandemVisits,
            int totalTandemVisits,
            boolean isAlihRawat,
            boolean isBayiLahir,
            boolean isPostSC,
            boolean isMultiOperator,
            boolean hasOtherRaberInOp,
            double feeOperator) {

        pastikanCacheTersedia();

        if (totalKlaim < 0) {
            return 0.0;
        }

        // 1. Kasus Dokter Anestesi: Universal 40% dari hak JM dokter operator
        if (isAnestesi) {
            double persenAnest = getParameter1("RANAP_ANESTESI_PORSI_OP", 40.0) / 100.0;
            return feeOperator * persenAnest;
        }

        // 2. Kasus Bayi Baru Lahir (Spesialis Anak Tanpa Tindakan)
        if (isBayiLahir && !isOperasi) {
            if (isPostSC && isRuleAktif("RANAP_ANAK_POST_SC", true)) {
                double pct = getParameter1("RANAP_ANAK_POST_SC", 4.0) / 100.0;
                return totalKlaim * pct;
            } else if (isRuleAktif("RANAP_ANAK_POST_PARTUM", true)) {
                return getParameter1("RANAP_ANAK_POST_PARTUM", 60000.0);
            }
            return 60000.0;
        }

        // 3. Kasus Alih Rawat (Transfer DPJP: DPJP SEP != DPJP SIMRS)
        if (isAlihRawat && !isOperasi) {
            if (isDpjpSep) {
                double pct = getParameter1("RANAP_ALIH_RAWAT_DPJP", 4.0) / 100.0;
                return totalKlaim * pct;
            } else if (isDpjp) {
                double pct = getParameter2("RANAP_ALIH_RAWAT_DPJP", 4.0) / 100.0;
                return totalKlaim * pct;
            } else {
                // Dokter tandem lain: 3% Prorata (Parameter 3)
                double totalPct = getParameter3("RANAP_ALIH_RAWAT_DPJP", 3.0) / 100.0;
                double proporsi = totalTandemVisits > 0 ? ((double) myTandemVisits / totalTandemVisits) : (1.0 / Math.max(1, tandemCount));
                return totalKlaim * totalPct * proporsi;
            }
        }

        // 4. Kasus Bedah / Operasi IBS
        if (isOperasi) {
            // Kasus 4A: Operasi 2 Dokter / 2 Tindakan Berbeda dalam 1 SEP
            if (isMultiOperator) {
                if (isOperator) {
                    if (!hasOtherRaberInOp && isRuleAktif("RANAP_OP_2DOKTER_TANPA_RABER", true)) {
                        double pct = getParameter1("RANAP_OP_2DOKTER_TANPA_RABER", 10.0) / 100.0;
                        return totalKlaim * pct;
                    } else if (isRuleAktif("RANAP_OP_2DOKTER_ADA_RABER", true)) {
                        double pct = getParameter1("RANAP_OP_2DOKTER_ADA_RABER", 9.0) / 100.0;
                        return totalKlaim * pct;
                    }
                    return totalKlaim * 0.10;
                } else if (!isDpjp) {
                    // Dokter tandem raber lain pada multi-operator: 2% Prorata (Parameter 3)
                    double totalPct = getParameter3("RANAP_OP_2DOKTER_ADA_RABER", 2.0) / 100.0;
                    double proporsi = totalTandemVisits > 0 ? ((double) myTandemVisits / totalTandemVisits) : (1.0 / Math.max(1, tandemCount));
                    return totalKlaim * totalPct * proporsi;
                }
            }

            // Kasus 4B: Operator BUKAN DPJP SEP
            if (!isDpjpSep && isOperator) {
                // Dokter Operator mendapat 14%
                double pctOp = getParameter2("RANAP_OP_NON_DPJP_TANPA_RABER", 14.0) / 100.0;
                return totalKlaim * pctOp;
            }
            if (!isOperator && isDpjpSep) {
                // DPJP SEP tidak operasi
                if (!hasOtherRaberInOp && isRuleAktif("RANAP_OP_NON_DPJP_TANPA_RABER", true)) {
                    double pctDpjp = getParameter1("RANAP_OP_NON_DPJP_TANPA_RABER", 6.0) / 100.0;
                    return totalKlaim * pctDpjp;
                } else if (isRuleAktif("RANAP_OP_NON_DPJP_ADA_RABER", true)) {
                    double pctDpjp = getParameter1("RANAP_OP_NON_DPJP_ADA_RABER", 4.0) / 100.0;
                    return totalKlaim * pctDpjp;
                }
                return totalKlaim * 0.06;
            }
            if (!isOperator && !isDpjpSep && hasOtherRaberInOp) {
                // Dokter tandem lain pada kasus operator bukan DPJP: 3% Prorata (Parameter 3)
                double totalPct = getParameter3("RANAP_OP_NON_DPJP_ADA_RABER", 3.0) / 100.0;
                double proporsi = totalTandemVisits > 0 ? ((double) myTandemVisits / totalTandemVisits) : (1.0 / Math.max(1, tandemCount));
                return totalKlaim * totalPct * proporsi;
            }

            // Kasus 4C: Operator = DPJP SEP
            if (isDpjpSep && isOperator) {
                if (tandemCount == 0) {
                    // 1 DPJP Sendiri
                    if (visitCount > 1 && isRuleAktif("RANAP_OP_1DPJP_VISIT_GT1", true)) {
                        return totalKlaim * (getParameter1("RANAP_OP_1DPJP_VISIT_GT1", 20.0) / 100.0);
                    } else if (visitCount == 1 && isRuleAktif("RANAP_OP_1DPJP_VISIT_EQ1", true)) {
                        return totalKlaim * (getParameter1("RANAP_OP_1DPJP_VISIT_EQ1", 18.0) / 100.0);
                    } else if (isRuleAktif("RANAP_OP_1DPJP_TELEKONSUL", true)) {
                        return totalKlaim * (getParameter1("RANAP_OP_1DPJP_TELEKONSUL", 16.0) / 100.0);
                    }
                    return totalKlaim * 0.20;
                } else {
                    // DPJP + Dokter Tandem / Raber
                    if (visitCount > 1 && isRuleAktif("RANAP_OP_1TANDEM_VISIT_GT1", true)) {
                        return totalKlaim * (getParameter1("RANAP_OP_1TANDEM_VISIT_GT1", 17.0) / 100.0);
                    } else if (visitCount == 1 && isRuleAktif("RANAP_OP_1TANDEM_VISIT_EQ1", true)) {
                        return totalKlaim * (getParameter1("RANAP_OP_1TANDEM_VISIT_EQ1", 16.0) / 100.0);
                    } else if (isRuleAktif("RANAP_OP_1TANDEM_TELEKONSUL", true)) {
                        return totalKlaim * (getParameter1("RANAP_OP_1TANDEM_TELEKONSUL", 15.0) / 100.0);
                    }
                    return totalKlaim * 0.17;
                }
            } else if (!isOperator && tandemCount > 0) {
                // Dokter tandem pada kasus DPJP mengoperasi
                double totalTandemPct = (dpjpVisitCount > 1) ? getParameter2("RANAP_OP_1TANDEM_VISIT_GT1", 3.0) :
                                        (dpjpVisitCount == 1) ? getParameter2("RANAP_OP_1TANDEM_VISIT_EQ1", 2.0) :
                                        getParameter2("RANAP_OP_1TANDEM_TELEKONSUL", 2.0);
                double proporsi = totalTandemVisits > 0 ? ((double) myTandemVisits / totalTandemVisits) : (1.0 / tandemCount);
                return totalKlaim * (totalTandemPct / 100.0) * proporsi;
            }
        }

        // 5. Kasus Non-Operasi (Pelayanan Medis Standar)
        if (tandemCount == 0) {
            // 1 DPJP Tunggal Sendiri
            if (visitCount > 1 && isRuleAktif("RANAP_NON_OP_1DPJP_VISIT_GT1", true)) {
                return totalKlaim * (getParameter1("RANAP_NON_OP_1DPJP_VISIT_GT1", 9.0) / 100.0);
            } else if (visitCount == 1 && isRuleAktif("RANAP_NON_OP_1DPJP_VISIT_EQ1", true)) {
                return totalKlaim * (getParameter1("RANAP_NON_OP_1DPJP_VISIT_EQ1", 6.0) / 100.0);
            } else if (isRuleAktif("RANAP_NON_OP_1DPJP_TELEKONSUL", true)) {
                return totalKlaim * (getParameter1("RANAP_NON_OP_1DPJP_TELEKONSUL", 4.0) / 100.0);
            }
            return totalKlaim * 0.09;
        } else if (tandemCount == 1) {
            // 1 DPJP + 1 Dokter Tandem
            if (isDpjp) {
                if (visitCount > 1 && isRuleAktif("RANAP_NON_OP_1TANDEM_DPJP_GT1", true)) {
                    return totalKlaim * (getParameter1("RANAP_NON_OP_1TANDEM_DPJP_GT1", 8.0) / 100.0);
                } else if (visitCount == 1 && isRuleAktif("RANAP_NON_OP_1TANDEM_DPJP_EQ1", true)) {
                    return totalKlaim * (getParameter1("RANAP_NON_OP_1TANDEM_DPJP_EQ1", 6.0) / 100.0);
                } else if (isRuleAktif("RANAP_NON_OP_1TANDEM_DPJP_TELE", true)) {
                    double fee = totalKlaim * (getParameter1("RANAP_NON_OP_1TANDEM_DPJP_TELE", 4.0) / 100.0);
                    if (isRuleAktif("RANAP_NON_OP_PLAFON_1TANDEM_TELE", true)) {
                        double plafon = getParameter1("RANAP_NON_OP_PLAFON_1TANDEM_TELE", 40000.0);
                        if (fee > plafon) {
                            fee = plafon;
                        }
                    }
                    return fee;
                }
                return totalKlaim * 0.08;
            } else {
                // Dokter Tandem Tunggal (1x visite fisik): 2% utuh tanpa batas plafon
                return totalKlaim * (getParameter2("RANAP_NON_OP_1TANDEM_DPJP_GT1", 2.0) / 100.0);
            }
        } else {
            // 1 DPJP + Multi Tandem (> 1 Dokter Tandem)
            if (isDpjp) {
                if (visitCount > 1 && isRuleAktif("RANAP_NON_OP_MULTI_DPJP_GT1", true)) {
                    return totalKlaim * (getParameter1("RANAP_NON_OP_MULTI_DPJP_GT1", 6.0) / 100.0);
                } else if (visitCount == 1 && isRuleAktif("RANAP_NON_OP_MULTI_DPJP_EQ1", true)) {
                    return totalKlaim * (getParameter1("RANAP_NON_OP_MULTI_DPJP_EQ1", 5.0) / 100.0);
                } else if (isRuleAktif("RANAP_NON_OP_MULTI_DPJP_TELE", true)) {
                    return totalKlaim * (getParameter1("RANAP_NON_OP_MULTI_DPJP_TELE", 4.0) / 100.0);
                }
                return totalKlaim * 0.06;
            } else {
                // Dokter Tandem Prorata
                double totalTandemPct = (dpjpVisitCount > 1) ? getParameter2("RANAP_NON_OP_MULTI_DPJP_GT1", 4.0) :
                                        (dpjpVisitCount == 1) ? getParameter2("RANAP_NON_OP_MULTI_DPJP_EQ1", 3.0) :
                                        getParameter2("RANAP_NON_OP_MULTI_DPJP_TELE", 2.0);
                double proporsi = totalTandemVisits > 0 ? ((double) myTandemVisits / totalTandemVisits) : (1.0 / tandemCount);
                return totalKlaim * (totalTandemPct / 100.0) * proporsi;
            }
        }
    }

    /**
     * Backward-compatible fallback.
     */
    public static double hitungJasaMedisDokter(
            double biayaMedisDisetujui,
            boolean isDPJP,
            boolean isDP,
            int jumlahKunjungan,
            boolean isAnestesi,
            boolean isOP,
            boolean isStatusRawat,
            String jenis) {

        if (biayaMedisDisetujui < 0) {
            throw new IllegalArgumentException("Biaya medis yang disetujui tidak boleh negatif");
        }

        if (jumlahKunjungan < 0) {
            throw new IllegalArgumentException("Jumlah kunjungan tidak boleh negatif");
        }

        if (isAnestesi) {
            return getPersenAnestesiRanap() * biayaMedisDisetujui;
        }

        double persentaseBiaya = (getPersenDefaultRanap(isOP, isDPJP) / 100.0);
        return persentaseBiaya * biayaMedisDisetujui;
    }
}
