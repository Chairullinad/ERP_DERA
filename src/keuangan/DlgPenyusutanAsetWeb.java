package keuangan;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.Period;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.SwingUtilities;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Modul Webview Depresiasi & Penyusutan Aset Terpadu
 * Dilengkapi dengan perhitungan depresiasi garis lurus,
 * sinkronisasi/koreksi data master aset langsung dari web,
 * dan posting jurnal otomatis ke Buku Besar Khanza.
 */
public class DlgPenyusutanAsetWeb extends javax.swing.JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final Jurnal jur = new Jurnal();

    private WebView webView;
    private WebEngine engine;
    private JFXPanel fxPanel;
    private boolean isWebViewInitialized = false;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "PenyusutanAset-DB-Worker");
        t.setDaemon(true);
        return t;
    });

    public DlgPenyusutanAsetWeb(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeWebView();
    }

    public void isCek() {
        // Compatibility method
    }

    private void initializeWebView() {
        if (isWebViewInitialized) {
            return;
        }
        new JFXPanel();

        Platform.runLater(() -> {
            try {
                webView = new WebView();
                engine = webView.getEngine();
                engine.setJavaScriptEnabled(true);

                String html = getHTMLContent();
                engine.loadContent(html);

                engine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        JSObject window = (JSObject) engine.executeScript("window");
                        window.setMember("javaApp", new JavaBridge());
                    }
                });

                fxPanel = new JFXPanel();
                Scene scene = new Scene(webView);
                fxPanel.setScene(scene);

                SwingUtilities.invokeLater(() -> {
                    jPanelWeb.removeAll();
                    jPanelWeb.setLayout(new java.awt.BorderLayout());
                    jPanelWeb.add(fxPanel, java.awt.BorderLayout.CENTER);
                    jPanelWeb.revalidate();
                    jPanelWeb.repaint();
                    isWebViewInitialized = true;
                });
            } catch (Exception e) {
                System.err.println("Gagal inisialisasi WebView Penyusutan Aset: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private String getHTMLContent() {
        StringBuilder content = new StringBuilder();
        try {
            InputStream is = getClass().getResourceAsStream("/resources/penyusutan_aset.html");
            if (is == null) {
                is = new java.io.FileInputStream("src/resources/penyusutan_aset.html");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            System.err.println("Gagal memuat file penyusutan_aset.html: " + e.getMessage());
        }
        return content.toString();
    }

    private String escapeJS(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    public class JavaBridge {

        public void loadDataAset(String idKategori, String bulanStr, String tahunStr, String keyword) {
            dbExecutor.submit(() -> {
                JSONArray rows = new JSONArray();
                try {
                    int bulan = Integer.parseInt(bulanStr);
                    int tahun = Integer.parseInt(tahunStr);
                    String kw = "%" + (keyword != null ? keyword.trim() : "") + "%";

                    StringBuilder sql = new StringBuilder();
                    sql.append("SELECT inv.no_inventaris, ib.kode_barang, ib.nama_barang, \n")
                       .append("       prod.nama_produsen, merk.nama_merk, ib.thn_produksi, \n")
                       .append("       kat.nama_kategori, ib.id_kategori, jns.nama_jenis, \n")
                       .append("       inv.asal_barang, inv.tgl_pengadaan, inv.harga, \n")
                       .append("       inv.status_barang, rng.nama_ruang, inv.no_rak, inv.no_box \n")
                       .append("FROM inventaris inv \n")
                       .append("INNER JOIN inventaris_barang ib ON ib.kode_barang = inv.kode_barang \n")
                       .append("LEFT JOIN inventaris_produsen prod ON ib.kode_produsen = prod.kode_produsen \n")
                       .append("LEFT JOIN inventaris_merk merk ON ib.id_merk = merk.id_merk \n")
                       .append("LEFT JOIN inventaris_kategori kat ON ib.id_kategori = kat.id_kategori \n")
                       .append("LEFT JOIN inventaris_jenis jns ON ib.id_jenis = jns.id_jenis \n")
                       .append("LEFT JOIN inventaris_ruang rng ON inv.id_ruang = rng.id_ruang \n")
                       .append("WHERE 1=1 \n");

                    if (idKategori != null && !idKategori.equals("Semua") && !idKategori.isEmpty()) {
                        sql.append("  AND ib.id_kategori = ? \n");
                    }
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        sql.append("  AND (ib.kode_barang LIKE ? OR ib.nama_barang LIKE ? OR inv.no_inventaris LIKE ? OR rng.nama_ruang LIKE ?) \n");
                    }

                    // Hanya ambil aset yang belum disusutkan untuk bulan & tahun ini
                    sql.append("  AND NOT EXISTS ( \n")
                       .append("      SELECT 1 FROM penyusutan_aset pa \n")
                       .append("      WHERE pa.no_inventaris = inv.no_inventaris \n")
                       .append("        AND MONTH(pa.tgl_penyusutan) = ? \n")
                       .append("        AND YEAR(pa.tgl_penyusutan) = ? \n")
                       .append("        AND pa.status = 'Sudah diproses' \n")
                       .append("  ) \n")
                       .append("ORDER BY ib.kode_barang, inv.no_inventaris");

                    try (PreparedStatement ps = koneksi.prepareStatement(sql.toString())) {
                        int idx = 1;
                        if (idKategori != null && !idKategori.equals("Semua") && !idKategori.isEmpty()) {
                            ps.setString(idx++, idKategori);
                        }
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            ps.setString(idx++, kw);
                            ps.setString(idx++, kw);
                            ps.setString(idx++, kw);
                            ps.setString(idx++, kw);
                        }
                        ps.setInt(idx++, bulan);
                        ps.setInt(idx++, tahun);

                        LocalDate targetPenyusutan = LocalDate.of(tahun, bulan, 28);

                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String katId = rs.getString("id_kategori");
                                if (katId == null) katId = "KL1";

                                int umurTahun = 8;
                                switch (katId) {
                                    case "BGN": umurTahun = 40; break;
                                    case "KL1": umurTahun = 8; break;
                                    case "KL2": umurTahun = 16; break;
                                    case "TNH": umurTahun = 0; break;
                                }

                                double harga = rs.getDouble("harga");
                                String tglPengadaanStr = rs.getString("tgl_pengadaan");
                                LocalDate tglPengadaan;
                                try {
                                    tglPengadaan = (tglPengadaanStr != null && !tglPengadaanStr.isEmpty()) 
                                            ? LocalDate.parse(tglPengadaanStr) 
                                            : targetPenyusutan;
                                } catch (Exception exDate) {
                                    tglPengadaan = targetPenyusutan;
                                }

                                int umurBulan = umurTahun * 12;
                                Period selisih = Period.between(tglPengadaan.withDayOfMonth(1), targetPenyusutan.withDayOfMonth(1));
                                int totalBulan = selisih.getYears() * 12 + selisih.getMonths() + 1;
                                if (totalBulan < 0) totalBulan = 0;

                                double susutPerBulan = 0;
                                double akumulasiSusut = 0;
                                double nilaiBuku = harga;

                                if (umurBulan > 0 && harga > 0) {
                                    BigDecimal bdHarga = BigDecimal.valueOf(harga);
                                    BigDecimal bdUmurBulan = BigDecimal.valueOf(umurBulan);
                                    BigDecimal bdSusutBln = bdHarga.divide(bdUmurBulan, 2, RoundingMode.HALF_UP);
                                    susutPerBulan = bdSusutBln.doubleValue();

                                    if (totalBulan >= umurBulan) {
                                        akumulasiSusut = harga;
                                        nilaiBuku = 0;
                                        susutPerBulan = 0; // sudah habis masa susutnya
                                    } else {
                                        akumulasiSusut = susutPerBulan * totalBulan;
                                        if (akumulasiSusut > harga) akumulasiSusut = harga;
                                        nilaiBuku = harga - akumulasiSusut;
                                        if (nilaiBuku < 0) nilaiBuku = 0;
                                    }
                                }

                                JSONObject obj = new JSONObject();
                                obj.put("no_inventaris", rs.getString("no_inventaris"));
                                obj.put("kode_barang", rs.getString("kode_barang"));
                                obj.put("nama_barang", rs.getString("nama_barang"));
                                obj.put("nama_produsen", rs.getString("nama_produsen"));
                                obj.put("nama_merk", rs.getString("nama_merk"));
                                obj.put("thn_produksi", rs.getString("thn_produksi") != null ? rs.getString("thn_produksi") : "");
                                obj.put("nama_kategori", rs.getString("nama_kategori"));
                                obj.put("id_kategori", katId);
                                obj.put("nama_ruang", rs.getString("nama_ruang"));
                                obj.put("tgl_pengadaan", tglPengadaanStr);
                                obj.put("harga", harga);
                                obj.put("umur_tahun", umurTahun);
                                obj.put("umur_bulan", umurBulan);
                                obj.put("selisih_bulan", totalBulan);
                                obj.put("beban_susut_bulan", susutPerBulan);
                                obj.put("akumulasi_susut", akumulasiSusut);
                                obj.put("nilai_buku", nilaiBuku);

                                rows.put(obj);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("loadDataAset error: " + e.getMessage());
                    e.printStackTrace();
                }

                JSONObject res = new JSONObject();
                try {
                    res.put("rows", rows);
                } catch (Exception e) {
                    System.err.println("Gagal membungkus rows ke JSON: " + e.getMessage());
                }
                final String jsonStr = res.toString();

                Platform.runLater(() -> {
                    if (engine != null) {
                        engine.executeScript("terimaDataAset('" + escapeJS(jsonStr) + "');");
                    }
                });
            });
        }

        public void updateMasterAset(String jsonStr) {
            dbExecutor.submit(() -> {
                boolean ok = false;
                String pesan = "";
                try {
                    JSONObject obj = new JSONObject(jsonStr);
                    String noInventaris = obj.getString("no_inventaris");
                    String kodeBarang = obj.getString("kode_barang");
                    String tglPengadaan = obj.getString("tgl_pengadaan");
                    String thnProduksi = obj.getString("thn_produksi");
                    double harga = obj.getDouble("harga");
                    String idKategori = obj.getString("id_kategori");

                    koneksi.setAutoCommit(false);

                    // 1. Update inventaris
                    String sqlInv = "UPDATE inventaris SET tgl_pengadaan = ?, harga = ? WHERE no_inventaris = ?";
                    try (PreparedStatement psInv = koneksi.prepareStatement(sqlInv)) {
                        psInv.setString(1, tglPengadaan);
                        psInv.setDouble(2, harga);
                        psInv.setString(3, noInventaris);
                        psInv.executeUpdate();
                    }

                    // 2. Update inventaris_barang
                    String sqlIb = "UPDATE inventaris_barang SET thn_produksi = ?, id_kategori = ? WHERE kode_barang = ?";
                    try (PreparedStatement psIb = koneksi.prepareStatement(sqlIb)) {
                        psIb.setString(1, thnProduksi);
                        psIb.setString(2, idKategori);
                        psIb.setString(3, kodeBarang);
                        psIb.executeUpdate();
                    }

                    koneksi.commit();
                    koneksi.setAutoCommit(true);
                    ok = true;
                    pesan = "Master aset " + noInventaris + " berhasil diperbarui.";
                } catch (Exception e) {
                    try { koneksi.rollback(); koneksi.setAutoCommit(true); } catch (Exception ignore) {}
                    pesan = e.getMessage();
                }

                final boolean sukses = ok;
                final String msg = pesan;
                Platform.runLater(() -> {
                    if (engine != null) {
                        engine.executeScript("hasilUpdateMaster(" + sukses + ", '" + escapeJS(msg) + "');");
                    }
                });
            });
        }

        public void postingPenyusutan(String jsonRows, String tglSusut, String keterangan) {
            dbExecutor.submit(() -> {
                boolean ok = false;
                String pesan = "";
                int count = 0;
                try {
                    JSONArray arr = new JSONArray(jsonRows);
                    if (arr.length() == 0) {
                        pesan = "Tidak ada baris yang dipilih.";
                    } else {
                        koneksi.setAutoCommit(false);

                        String kdRekDebet = Sequel.cariIsi("SELECT Biaya_Penyusutan_Aktiva_Tetap FROM set_akun");
                        if (kdRekDebet == null || kdRekDebet.isEmpty()) kdRekDebet = "52000";

                        String kdRekKredit = Sequel.cariIsi("SELECT Akumulasi_Penyusutan_Aset FROM set_akun");
                        if (kdRekKredit == null || kdRekKredit.isEmpty()) kdRekKredit = "13900";

                        String prefix = "AST" + tglSusut.replaceAll("-", "");

                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject item = arr.getJSONObject(i);
                            String noInv = item.getString("no_inventaris");
                            String nmBarang = item.getString("nama_barang");
                            double hargaAwal = item.getDouble("harga");
                            int umurThn = item.getInt("umur_tahun");
                            double susutBulan = item.getDouble("beban_susut_bulan");
                            double akumSusut = item.getDouble("akumulasi_susut");
                            double nilaiBuku = item.getDouble("nilai_buku");

                            if (susutBulan <= 0) {
                                continue; // aset sudah habis disusutkan
                            }

                            // Generate no_penyusutan
                            String noPenyusutan = Valid.autoNomer3(
                                    "SELECT IFNULL(MAX(CONVERT(RIGHT(no_penyusutan,6),SIGNED)),0) FROM penyusutan_aset WHERE tgl_penyusutan='" + tglSusut + "'",
                                    prefix,
                                    6
                            );

                            // Insert into penyusutan_aset
                            String sqlIns = "INSERT INTO penyusutan_aset (no_penyusutan, tgl_penyusutan, no_inventaris, nama_barang, "
                                    + "nilai_aset_awal, umur_kategori, nilai_susut_bulan, nilai_susut_total, nilai_buku, keterangan, kd_petugas, status) "
                                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Sudah diproses')";
                            try (PreparedStatement psIns = koneksi.prepareStatement(sqlIns)) {
                                psIns.setString(1, noPenyusutan);
                                psIns.setString(2, tglSusut);
                                psIns.setString(3, noInv);
                                psIns.setString(4, nmBarang);
                                psIns.setDouble(5, hargaAwal);
                                psIns.setInt(6, umurThn);
                                psIns.setDouble(7, susutBulan);
                                psIns.setDouble(8, akumSusut);
                                psIns.setDouble(9, nilaiBuku);
                                psIns.setString(10, keterangan != null ? keterangan : "Penyusutan Aset");
                                psIns.setString(11, akses.getkode());
                                psIns.executeUpdate();
                            }

                            // JURNAL AKUNTANSI KHANZA
                            Sequel.queryu("DELETE FROM tampjurnal");
                            Sequel.menyimpan("tampjurnal", "?,?,?,?", 4, new String[]{kdRekDebet, "BIAYA PENYUSUTAN " + nmBarang, String.valueOf(susutBulan), "0"});
                            Sequel.menyimpan("tampjurnal", "?,?,?,?", 4, new String[]{kdRekKredit, "AKUMULASI PENYUSUTAN " + nmBarang, "0", String.valueOf(susutBulan)});
                            
                            boolean jurSukses = jur.simpanJurnal(noPenyusutan, "U", "Penyusutan Aset " + nmBarang + " (" + noInv + ")");
                            if (!jurSukses) {
                                throw new RuntimeException("Gagal mencatat jurnal buku besar untuk aset " + noInv);
                            }

                            // Catat ke detail_posting_jurnal (buku besar tracking)
                            try {
                                String sqlPost = "INSERT INTO detail_posting_jurnal (no_jurnal, kd_rek, tgl_posting, keterangan, debet, kredit, posted_by) "
                                        + "VALUES (?, ?, ?, ?, ?, ?, ?)";
                                try (PreparedStatement psPost = koneksi.prepareStatement(sqlPost)) {
                                    // Baris Debet
                                    psPost.setString(1, noPenyusutan);
                                    psPost.setString(2, kdRekDebet);
                                    psPost.setString(3, tglSusut);
                                    psPost.setString(4, "Biaya Penyusutan " + nmBarang);
                                    psPost.setDouble(5, susutBulan);
                                    psPost.setDouble(6, 0);
                                    psPost.setString(7, akses.getkode());
                                    psPost.executeUpdate();

                                    // Baris Kredit
                                    psPost.setString(1, noPenyusutan);
                                    psPost.setString(2, kdRekKredit);
                                    psPost.setString(3, tglSusut);
                                    psPost.setString(4, "Akumulasi Penyusutan " + nmBarang);
                                    psPost.setDouble(5, 0);
                                    psPost.setDouble(6, susutBulan);
                                    psPost.setString(7, akses.getkode());
                                    psPost.executeUpdate();
                                }
                            } catch (Exception ignorePost) {
                                // optional jika tabel detail_posting_jurnal belum di-migrate
                            }

                            count++;
                        }

                        koneksi.commit();
                        koneksi.setAutoCommit(true);
                        ok = true;
                        pesan = "Berhasil memposting " + count + " aset ke jurnal buku besar.";
                        trackSql("Posting Jurnal Penyusutan Aset: Berhasil memposting " + count + " unit aset ke jurnal buku besar");
                    }
                } catch (Exception e) {
                    try { koneksi.rollback(); koneksi.setAutoCommit(true); } catch (Exception ignore) {}
                    pesan = e.getMessage();
                }

                final boolean sukses = ok;
                final String msg = pesan;
                Platform.runLater(() -> {
                    if (engine != null) {
                        engine.executeScript("hasilPostingPenyusutan(" + sukses + ", '" + escapeJS(msg) + "');");
                    }
                });
            });
        }

        public void loadRiwayatPenyusutan(String tglAwal, String tglAkhir, String keyword) {
            dbExecutor.submit(() -> {
                JSONArray rows = new JSONArray();
                try {
                    String sql = "SELECT pa.no_penyusutan, pa.tgl_penyusutan, pa.no_inventaris, "
                            + "       pa.nilai_aset_awal, pa.umur_kategori, pa.nilai_susut_bulan, "
                            + "       pa.nilai_susut_total, pa.nilai_buku, pa.keterangan, pa.kd_petugas, "
                            + "       pa.nama_barang, peg.nama AS petugas_nama, kat.nama_kategori "
                            + "FROM penyusutan_aset pa "
                            + "LEFT JOIN pegawai peg ON pa.kd_petugas = peg.nik "
                            + "LEFT JOIN inventaris inv ON pa.no_inventaris = inv.no_inventaris "
                            + "LEFT JOIN inventaris_barang ib ON inv.kode_barang = ib.kode_barang "
                            + "LEFT JOIN inventaris_kategori kat ON ib.id_kategori = kat.id_kategori "
                            + "WHERE pa.status = 'Sudah diproses' "
                            + "  AND pa.tgl_penyusutan BETWEEN ? AND ? "
                            + "  AND (pa.no_inventaris LIKE ? OR pa.nama_barang LIKE ? OR pa.no_penyusutan LIKE ?) "
                            + "ORDER BY pa.no_penyusutan DESC LIMIT 500";

                    try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                        String kw = "%" + (keyword != null ? keyword.trim() : "") + "%";
                        ps.setString(1, tglAwal);
                        ps.setString(2, tglAkhir);
                        ps.setString(3, kw);
                        ps.setString(4, kw);
                        ps.setString(5, kw);

                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                JSONObject r = new JSONObject();
                                r.put("no_penyusutan", rs.getString("no_penyusutan"));
                                r.put("tgl_penyusutan", rs.getString("tgl_penyusutan"));
                                r.put("no_inventaris", rs.getString("no_inventaris"));
                                r.put("nama_barang", rs.getString("nama_barang"));
                                r.put("nama_kategori", rs.getString("nama_kategori"));
                                r.put("nilai_aset_awal", rs.getDouble("nilai_aset_awal"));
                                r.put("nilai_susut_bulan", rs.getDouble("nilai_susut_bulan"));
                                r.put("nilai_susut_total", rs.getDouble("nilai_susut_total"));
                                r.put("nilai_buku", rs.getDouble("nilai_buku"));
                                r.put("kd_petugas", rs.getString("kd_petugas"));
                                r.put("petugas_nama", rs.getString("petugas_nama"));
                                rows.put(r);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("loadRiwayatPenyusutan error: " + e.getMessage());
                }

                JSONObject res = new JSONObject();
                try {
                    res.put("rows", rows);
                } catch (Exception e) {
                    System.err.println("Gagal membungkus rows riwayat ke JSON: " + e.getMessage());
                }
                final String jsonStr = res.toString();

                Platform.runLater(() -> {
                    if (engine != null) {
                        engine.executeScript("terimaRiwayatPenyusutan('" + escapeJS(jsonStr) + "');");
                    }
                });
            });
        }

        public void bukaLabaRugi() {
            SwingUtilities.invokeLater(() -> {
                DlgLabaRugi dlg = new DlgLabaRugi(null, false);
                dlg.setSize(1000, 650);
                dlg.setLocationRelativeTo(DlgPenyusutanAsetWeb.this);
                dlg.setVisible(true);
            });
        }

        public void bukaNeracaSaldo() {
            SwingUtilities.invokeLater(() -> {
                KeuanganSaldoAkunPerBulan dlg = new KeuanganSaldoAkunPerBulan(null, false);
                dlg.setSize(1000, 650);
                dlg.setLocationRelativeTo(DlgPenyusutanAsetWeb.this);
                dlg.setVisible(true);
            });
        }

        public void keluarForm() {
            SwingUtilities.invokeLater(() -> dispose());
        }

        private void trackSql(String pesan) {
            try {
                String sql = "INSERT INTO trackersql (tanggal, sqle, usere) VALUES (NOW(), ?, ?)";
                try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                    String host = "";
                    try {
                        java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
                        host = "[" + addr.getHostName() + " / " + addr.getHostAddress() + "] ";
                    } catch (Exception e) {}
                    ps.setString(1, host + pesan);
                    ps.setString(2, akses.getkode());
                    ps.executeUpdate();
                }
            } catch (Exception e) {
                System.err.println("Gagal catat trackersql: " + e.getMessage());
            }
        }
    }

    private void initComponents() {
        internalFrame1 = new widget.InternalFrame();
        jPanelWeb = new javax.swing.JPanel();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setSize(1350, 750);
        setLocationRelativeTo(null);
        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                "::[ Depresiasi & Penyusutan Aset RS ]::",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Tahoma", 0, 11),
                new java.awt.Color(50, 50, 50)
        ));
        internalFrame1.setLayout(new java.awt.BorderLayout());
        jPanelWeb.setLayout(new java.awt.BorderLayout());
        internalFrame1.add(jPanelWeb, java.awt.BorderLayout.CENTER);
        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);
        pack();
    }

    private widget.InternalFrame internalFrame1;
    private javax.swing.JPanel jPanelWeb;
}
