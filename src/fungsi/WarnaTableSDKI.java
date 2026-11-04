package fungsi;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class WarnaTableSDKI extends DefaultTableCellRenderer {

    private final Map<String, Color> warnaEntitas = new HashMap<>();
    private final Map<String, Color> warnaEntitasSLKI = new HashMap<>();
    private final Map<String, Color> warnaEntitasSIKI = new HashMap<>();

    private static final Map<String, Color> defaultSDKI = new HashMap<>();
    private static final Map<String, Color> defaultSLKI = new HashMap<>();
    private static final Map<String, Color> defaultSIKI = new HashMap<>();
    private static final Map<String, Color> customWarnaDB = new HashMap<>();
    private static boolean isStaticInitialized = false;

    public WarnaTableSDKI() {
        initSDKI();
        initSLKI();
        initSIKI();
        initStaticMaps();
        if (customWarnaDB.isEmpty()) {
            loadCustomWarna();
        }
    }

    /**
     * Inisialisasi map statis dari dataset warna bawaan SDKI, SLKI, dan SIKI
     */
    public static synchronized void initStaticMaps() {
        if (isStaticInitialized) {
            return;
        }
        WarnaTableSDKI temp = new WarnaTableSDKI(true);
        defaultSDKI.putAll(temp.warnaEntitas);
        defaultSLKI.putAll(temp.warnaEntitasSLKI);
        defaultSIKI.putAll(temp.warnaEntitasSIKI);
        isStaticInitialized = true;
    }

    /**
     * Konstruktor privat internal untuk mencegah recursive loop saat inisialisasi static maps
     */
    private WarnaTableSDKI(boolean internal) {
        initSDKI();
        initSLKI();
        initSIKI();
    }

    /**
     * Memuat konfigurasi warna kustom dari database (master_entitas_warna) ke static in-memory cache
     */
    public static synchronized void loadCustomWarna() {
        customWarnaDB.clear();
        try {
            Connection koneksi = koneksiDB.condb();
            if (koneksi != null) {
                Statement stmt = koneksi.createStatement();
                try {
                    stmt.execute(
                        "create table if not exists master_entitas_warna (" +
                        "  kategori varchar(10) not null," +
                        "  entitas varchar(150) not null," +
                        "  kode_warna varchar(10) not null," +
                        "  primary key (kategori, entitas)" +
                        ") engine=innodb default charset=latin1"
                    );
                } finally {
                    stmt.close();
                }

                PreparedStatement ps = koneksi.prepareStatement("select kategori, entitas, kode_warna from master_entitas_warna");
                try {
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            String kat = rs.getString("kategori").trim().toUpperCase();
                            String entRaw = rs.getString("entitas");
                            String ent = cleanEntityName(entRaw);
                            String hex = rs.getString("kode_warna").trim();
                            try {
                                Color col = Color.decode(hex);
                                customWarnaDB.put(kat + "#" + ent, col);
                                customWarnaDB.put(ent, col);
                                // Simpan juga variasi tanpa tanda kurung jika ada
                                String entNoParen = cleanEntityName(ent.replaceAll("\\s*\\(.*?\\)", ""));
                                if (!entNoParen.isEmpty() && !entNoParen.equals(ent)) {
                                    customWarnaDB.put(kat + "#" + entNoParen, col);
                                    customWarnaDB.put(entNoParen, col);
                                }
                            } catch (Exception ex) {
                            }
                        }
                    } finally {
                        rs.close();
                    }
                } finally {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi loadCustomWarna WarnaTableSDKI: " + e);
        }
    }

    /**
     * Membersihkan nama entitas dari non-breaking spaces (\u00A0), multiple spaces, trim, dan lowercase.
     */
    public static String cleanEntityName(String str) {
        if (str == null) return "";
        return str.replace('\u00A0', ' ')
                  .replaceAll("\\s+", " ")
                  .trim()
                  .toLowerCase();
    }

    /**
     * Pencocokan warna cerdas dengan toleransi teks:
     * 1. Exact match (setelah disanitasi)
     * 2. Menghilangkan keterangan dalam tanda kurung pada target (contoh: "konseling laktasi (lactation counseling)" -> "konseling laktasi")
     * 3. Menghilangkan tanda kurung pada key di map (contoh: map key "konseling laktasi (lactation counseling)" dicocokkan dengan target "konseling laktasi")
     * 4. Fallback prefix/containment match
     */
    public static Color findColor(Map<String, Color> map, String target) {
        if (target == null || target.trim().isEmpty() || map == null || map.isEmpty()) {
            return null;
        }
        String cleaned = cleanEntityName(target);
        if (cleaned.isEmpty()) {
            return null;
        }

        // 1. Exact match
        if (map.containsKey(cleaned)) {
            return map.get(cleaned);
        }

        // 2. Target tanpa tanda kurung
        String cleanNoParen = cleanEntityName(cleaned.replaceAll("\\s*\\(.*?\\)", ""));
        if (!cleanNoParen.isEmpty() && map.containsKey(cleanNoParen)) {
            return map.get(cleanNoParen);
        }

        // 3. Bandingkan dengan key di map yang tanda kurungnya dihilangkan
        for (Map.Entry<String, Color> entry : map.entrySet()) {
            String key = entry.getKey();
            String keyNoParen = cleanEntityName(key.replaceAll("\\s*\\(.*?\\)", ""));
            if (!keyNoParen.isEmpty()) {
                if (keyNoParen.equals(cleaned) || (!cleanNoParen.isEmpty() && keyNoParen.equals(cleanNoParen))) {
                    return entry.getValue();
                }
            }
        }

        // 4. Fallback prefix / containment match (jika panjang cukup memadai)
        if (cleaned.length() >= 5) {
            for (Map.Entry<String, Color> entry : map.entrySet()) {
                String key = entry.getKey();
                String keyClean = cleanEntityName(key.replaceAll("\\s*\\(.*?\\)", ""));
                if (keyClean.length() >= 5) {
                    if (cleaned.startsWith(keyClean) || keyClean.startsWith(cleaned)) {
                        return entry.getValue();
                    }
                }
            }
        }

        return null;
    }

    /**
     * Pencocokan warna cerdas dengan prefix kategori (contoh: "SIKI#konseling laktasi").
     */
    public static Color findColorWithPrefix(Map<String, Color> map, String prefix, String target) {
        if (target == null || target.trim().isEmpty() || map == null || map.isEmpty()) {
            return null;
        }
        String cleaned = cleanEntityName(target);
        if (cleaned.isEmpty()) {
            return null;
        }
        String cleanNoParen = cleanEntityName(cleaned.replaceAll("\\s*\\(.*?\\)", ""));

        String prefixTag = (prefix == null || prefix.trim().isEmpty()) ? "" : (prefix.trim().toUpperCase() + "#");

        // 1. Exact match with prefix
        String fullKey = prefixTag + cleaned;
        if (map.containsKey(fullKey)) {
            return map.get(fullKey);
        }

        // 2. Target tanpa tanda kurung with prefix
        if (!cleanNoParen.isEmpty()) {
            String fullKeyNoParen = prefixTag + cleanNoParen;
            if (map.containsKey(fullKeyNoParen)) {
                return map.get(fullKeyNoParen);
            }
        }

        // 3. Iterasi entries dengan prefix
        for (Map.Entry<String, Color> entry : map.entrySet()) {
            String k = entry.getKey();
            if (prefixTag.isEmpty() || k.startsWith(prefixTag)) {
                String entPart = prefixTag.isEmpty() ? k : k.substring(prefixTag.length());
                String entNoParen = cleanEntityName(entPart.replaceAll("\\s*\\(.*?\\)", ""));
                if (entPart.equals(cleaned) || (!cleanNoParen.isEmpty() && entPart.equals(cleanNoParen))) {
                    return entry.getValue();
                }
                if (!entNoParen.isEmpty() && (entNoParen.equals(cleaned) || (!cleanNoParen.isEmpty() && entNoParen.equals(cleanNoParen)))) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    /**
     * Mengambil warna entitas secara terpadu (Single Source of Truth).
     * Prioritas:
     * 1. Kustomisasi di database master_entitas_warna
     * 2. Bawaan SDKI, SLKI, atau SIKI sesuai kategori
     * 3. null jika belum pernah terdaftar (fallback ke pewarnaan default tabel)
     */
    public static Color getWarna(String kategori, String entitas) {
        if (!isStaticInitialized) {
            initStaticMaps();
        }
        if (customWarnaDB.isEmpty()) {
            loadCustomWarna();
        }
        if (entitas == null || entitas.trim().isEmpty()) {
            return null;
        }
        String ent = cleanEntityName(entitas);
        String kat = (kategori == null) ? "" : kategori.trim().toUpperCase();

        // 1. Cek dari database kustom terlebih dahulu (dengan prefix kategori)
        if (!kat.isEmpty()) {
            Color c = findColorWithPrefix(customWarnaDB, kat, ent);
            if (c != null) return c;
        }
        Color cCustom = findColor(customWarnaDB, ent);
        if (cCustom != null) return cCustom;

        // 2. Jika tidak ada di DB, fallback ke warna bawaan sesuai kategori
        if (kat.equals("SDKI") || kat.equals("DIAGNOSA") || kat.isEmpty()) {
            Color c = findColor(defaultSDKI, ent);
            if (c != null) return c;
        }
        if (kat.equals("SLKI") || kat.equals("INTERVENSI") || kat.isEmpty()) {
            Color c = findColor(defaultSLKI, ent);
            if (c != null) return c;
        }
        if (kat.equals("SIKI") || kat.equals("EVALUASI") || kat.isEmpty()) {
            Color c = findColor(defaultSIKI, ent);
            if (c != null) return c;
        }

        // 3. Fallback pencocokan nama entitas global
        Color cGlobal = findColor(defaultSDKI, ent);
        if (cGlobal != null) return cGlobal;
        cGlobal = findColor(defaultSLKI, ent);
        if (cGlobal != null) return cGlobal;
        cGlobal = findColor(defaultSIKI, ent);
        if (cGlobal != null) return cGlobal;

        return null;
    }

    private void initSDKI() {
        Color pinkLembut = new Color(255, 204, 203);
        Color biruSangatMuda = new Color(204, 229, 255);
        Color krem = new Color(255, 255, 204);
        Color hijauMint = new Color(204, 255, 204);

        warnaEntitas.put("faktor risiko", pinkLembut);
        warnaEntitas.put("penyebab", pinkLembut);
        warnaEntitas.put("kondisi klinis terkait", biruSangatMuda);
        warnaEntitas.put("gejala dan tanda minor", krem);
        warnaEntitas.put("gejala dan tanda mayor", hijauMint);
    }

    private void initSLKI() {
        Color hijauMint = new Color(204, 255, 204);
        String[] meningkat = {
            "bersihan jalan nafas meningkat", "pertukaran gas meningkat", "keseimbangan cairan meningkat",
            "keseimbangan elektrolit meningkat", "status cairan membaik", "status nutrisi membaik",
            "mobilitas fisik meningkat", "toleransi aktivitas meningkat",
            "perfusi miokard meningkat", "perfusi perifer meningkat", "perfusi serebral meningkat",
            "status sirkulasi membaik", "termoregulasi membaik", "termoregulasi neonatus membaik",
            "integritas kulit dan jaringan meningkat", "perawatan diri meningkat", "status perkembangan membaik",
            "pola tidur membaik", "pola nafas membaik", "status neurologis membaik", "komunikasi verbal meningkat",
            "status menelan membaik", "penyapihan ventilator meningkat", "pemulihan pasca bedah meningkat",
            "kapasitas adaptif intrakranial meningkat", "motilitas gastrointestinal membaik",
            "motalitas gastrointestinal membaik", "fungsi gastrointestinal membaik", "citra tubuh meningkat",
            "harga diri meningkat", "status menyusui membaik", "status antepartum membaik",
            "status intrapartum membaik", "status kenyamanan paska partum meningkat", "tingkat pengetahuan meningkat",
            "kontrol nyeri meningkat", "kestabilan kadar glukosa darah meningkat", "eliminasi fekal membaik",
            "eliminasi urine membaik", "kontinensia urine membaik", "kontinensia fekal membaik",
            "inkontinensia fekal membaik"
        };
        for (String s : meningkat) {
            warnaEntitasSLKI.put(s.toLowerCase(), hijauMint);
        }

        Color biruMuda = new Color(204, 229, 255);
        String[] menurun = {
            "tingkat nyeri menurun", "tingkat nausea menurun", "tingkat ansietas menurun",
            "tingkat jatuh menurun", "tingkat perdarahan menurun",
            "tingkat cedera menurun", "tingkat infeksi menurun", "tingkat aspirasi menurun",
            "tingkat syok menurun", "curah jantung meningkat"
        };
        for (String s : menurun) {
            warnaEntitasSLKI.put(s.toLowerCase(), biruMuda);
        }
    }

    private void initSIKI() {
        Color biruLangit = new Color(204, 229, 255);
        String[] pemantauan = {
            "pemantauan cairan", "pemantauan respirasi", "pemantauan elektrolit", "pemantauan hemodinamik invasif"
        };
        for (String s : pemantauan) {
            warnaEntitasSIKI.put(s.toLowerCase(), biruLangit);
        }

        Color hijauMint = new Color(204, 255, 204);
        String[] manajemen = {
            "manajemen jalan nafas (airway management)", "manajemen jalan nafas", "managemen hipervolemi",
            "manajemen nutrisi", "manajemen energi", "manajemen nyeri (pain management)", "manajemen nyeri",
            "managemen mual", "manajemen sedasi",
            "manajemen cairan", "managemen hipertermia", "managemen hipotermia",
            "managemen peningkatan tekanan intrakranial", "manajemen keselamatan lingkungan",
            "perawatan jantung (cardiac care)", "perawatan jantung (cardia care)", "perawatan jantung",
            "perawatan sirkulasi", "perawatan integritas kulit", "perawatan area insisi",
            "perawatan perkembangan", "manajemen eliminasi fekal", "manajeman konstipasi", "manajemen diare",
            "managemen eliminasi urine", "kateterisasi urine", "managemen hipovolemi", "manajemen hipovolemik",
            "manajemen hipervolemi", "manajemen syok", "kondisi klinis terkini", "manajemen hipoglikemi"
        };
        for (String s : manajemen) {
            warnaEntitasSIKI.put(s.toLowerCase(), hijauMint);
        }

        Color pinkLembut = new Color(255, 204, 203);
        String[] pencegahan = {
            "pencegahan infeksi", "pencegahan jatuh", "pencegahan jatuh (fall prevention)", "pencegahan perdarahan",
            "pencegahan cedera", "pencegahan luka tekan", "pencegahan aspirasi", "pencegahan aspirasi (aspiration precaution)",
            "pencegahan syok", "pencegahan konstipasi", "tingkat delirium menurun", "perawatan luka"
        };
        for (String s : pencegahan) {
            warnaEntitasSIKI.put(s.toLowerCase(), pinkLembut);
        }

        Color unguLavender = new Color(230, 204, 255);
        String[] edukasi = {
            "edukasi termoregulasi", "edukasi menyusui", "edukasi persalinan", "health education", "health education (leaflet)",
            "konseling laktasi", "konseling laktasi (lactation counseling)", "promosi asi eksklusif", "promosi citra tubuh",
            "promosi kepercayaan diri", "promosi kesehatan : defisit bicara", "promosi kesehatan: defisit bicara",
            "promosi komunikasi : defisit pendengaran", "promosi komunikasi: defisit pendengaran"
        };
        for (String s : edukasi) {
            warnaEntitasSIKI.put(s.toLowerCase(), unguLavender);
        }

        Color peach = new Color(255, 229, 204);
        String[] dukungan = {
            "dukungan mobilisasi", "dukungan perawatan diri", "dukungan perawatan diri : makan/minum",
            "dukungan perawatan diri: makan/minum", "dukungan tidur", "dukungan perawatan diri: bab/bak",
            "dukungan perawatan diri : bab/bak", "manajemen hiperglikemi"
        };
        for (String s : dukungan) {
            warnaEntitasSIKI.put(s.toLowerCase(), peach);
        }

        Color aqua = new Color(204, 255, 255);
        String[] terapi = {
            "pemberian produk darah", "terapi oksigen", "reduksi ansietas", "fototerapi neonatus",
            "penyapihan ventilasi mekanik"
        };
        for (String s : terapi) {
            warnaEntitasSIKI.put(s.toLowerCase(), aqua);
        }

        Color hijauPupus = new Color(229, 255, 204);
        String[] latihan = {
            "tehnik latihan penguatan sendi", "latihan eliminasi fekal"
        };
        for (String s : latihan) {
            warnaEntitasSIKI.put(s.toLowerCase(), hijauPupus);
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {

        Component component = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);

        if (isSelected) {
            component.setBackground(table.getSelectionBackground());
            component.setForeground(table.getSelectionForeground());
            return component;
        }

        Color bgDefault = (row % 2 == 1)
                ? new Color(195, 226, 230)
                : new Color(255, 255, 255);
        component.setBackground(bgDefault);
        component.setForeground(new Color(50, 50, 50));

        if (column >= 4 && column <= 7) {
            Object entitasObj = table.getValueAt(row, 4);
            if (entitasObj != null) {
                String entitas = cleanEntityName(entitasObj.toString());
                String tableName = table.getName();
                if (tableName == null) {
                    tableName = "";
                }

                Color bg = null;
                String katKey = "";
                switch (tableName) {
                    case "tbDiagKep":
                        katKey = "SDKI";
                        break;
                    case "tbIntervensi":
                        katKey = "SLKI";
                        break;
                    case "tbEvaluasi":
                        katKey = "SIKI";
                        break;
                    default:
                        break;
                }

                if (customWarnaDB.isEmpty()) {
                    loadCustomWarna();
                }

                // Cek override database kustom terlebih dahulu
                if (!katKey.isEmpty()) {
                    bg = findColorWithPrefix(customWarnaDB, katKey, entitas);
                }
                if (bg == null) {
                    bg = findColor(customWarnaDB, entitas);
                }

                // Jika tidak ada di DB, gunakan warna bawaan eksisting (algoritma lama tetap berjalan dengan smart matching)
                if (bg == null) {
                    switch (tableName) {
                        case "tbDiagKep":
                            bg = findColor(warnaEntitas, entitas);
                            break;
                        case "tbIntervensi":
                            bg = findColor(warnaEntitasSLKI, entitas);
                            break;
                        case "tbEvaluasi":
                            bg = findColor(warnaEntitasSIKI, entitas);
                            break;
                        default:
                            break;
                    }
                }

                if (bg != null) {
                    // Hitung indeks urutan entitas (block index) dari baris ke-0 hingga saat ini
                    int entityIndex = 0;
                    String prevEntity = null;
                    for (int i = 0; i <= row; i++) {
                        Object val = table.getValueAt(i, 4);
                        String currentEntity = (val == null) ? "" : cleanEntityName(val.toString());
                        if (prevEntity == null || !currentEntity.equals(prevEntity)) {
                            entityIndex++;
                            prevEntity = currentEntity;
                        }
                    }

                    // Buat selang-seling warna antar entitas agar tidak menyatu (Zebra block striping)
                    if (entityIndex % 2 == 0) {
                        // Gelapkan warna dasar sebanyak 35 poin RGB agar lebih kontras
                        int r = Math.max(0, bg.getRed() - 35);
                        int g = Math.max(0, bg.getGreen() - 35);
                        int b = Math.max(0, bg.getBlue() - 35);
                        bg = new Color(r, g, b);
                    }

                    component.setBackground(bg);
                }
            }
        }

        return component;
    }
}
