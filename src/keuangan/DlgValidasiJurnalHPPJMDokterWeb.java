package keuangan;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class DlgValidasiJurnalHPPJMDokterWeb extends javax.swing.JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();

    private WebView webView;
    private WebEngine engine;
    private JFXPanel fxPanel;
    private boolean isWebViewInitialized = false;

    private final ExecutorService dbExecutor = Executors.newFixedThreadPool(10, r -> {
        Thread t = new Thread(r, "Validasi-HPP-JM-Worker");
        t.setDaemon(true);
        return t;
    });

    // Default COA cache
    private String akunDebetHpp = "5.1.02.01";
    private String akunKreditHpp = "1.1.05.01";
    private String akunDebetJm = "5.1.01.01";
    private String akunKreditJm = "2.1.03.01";

    public DlgValidasiJurnalHPPJMDokterWeb(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        detectDefaultCoa();
        initializeWebView();
    }

    private void detectDefaultCoa() {
        try {
            // Cari akun HPP Farmasi / Obat
            try (Statement st = koneksi.createStatement();
                 ResultSet rs = st.executeQuery("SELECT kd_rek FROM rekening WHERE nm_rek LIKE '%HPP%' OR nm_rek LIKE '%HARGA POKOK%' LIMIT 1")) {
                if (rs.next()) akunDebetHpp = rs.getString("kd_rek");
            }
            // Cari akun Persediaan Farmasi / Obat
            try (Statement st = koneksi.createStatement();
                 ResultSet rs = st.executeQuery("SELECT kd_rek FROM rekening WHERE nm_rek LIKE '%PERSEDIAAN%' AND (nm_rek LIKE '%OBAT%' OR nm_rek LIKE '%FARMASI%') LIMIT 1")) {
                if (rs.next()) akunKreditHpp = rs.getString("kd_rek");
            }
            // Cari akun Beban Jasa Medis Dokter
            try (Statement st = koneksi.createStatement();
                 ResultSet rs = st.executeQuery("SELECT kd_rek FROM rekening WHERE (nm_rek LIKE '%BEBAN%' OR nm_rek LIKE '%BIAYA%') AND nm_rek LIKE '%JASA MEDIS%' LIMIT 1")) {
                if (rs.next()) akunDebetJm = rs.getString("kd_rek");
            }
            // Cari akun Hutang Jasa Medis / Kas
            try (Statement st = koneksi.createStatement();
                 ResultSet rs = st.executeQuery("SELECT kd_rek FROM rekening WHERE nm_rek LIKE '%HUTANG%' AND nm_rek LIKE '%JASA MEDIS%' LIMIT 1")) {
                if (rs.next()) akunKreditJm = rs.getString("kd_rek");
            }
        } catch (Exception e) {
            System.err.println("detectDefaultCoa warning: " + e.getMessage());
        }
    }

    private void initializeWebView() {
        if (isWebViewInitialized) return;
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
                        engine.executeScript("if (typeof cariData === 'function') cariData();");
                    }
                });

                fxPanel = new JFXPanel();
                Scene scene = new Scene(webView);
                fxPanel.setScene(scene);

                SwingUtilities.invokeLater(() -> {
                    jPanelWeb.removeAll();
                    jPanelWeb.setLayout(new BorderLayout());
                    jPanelWeb.add(fxPanel, BorderLayout.CENTER);
                    jPanelWeb.revalidate();
                    jPanelWeb.repaint();
                    isWebViewInitialized = true;
                });

            } catch (Exception e) {
                System.err.println("Error WebView Validasi HPP JM: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private String getHTMLContent() {
        try {
            InputStream is = getClass().getResourceAsStream("/resources/validasi_jurnal_hpp_jm.html");
            if (is == null) {
                is = getClass().getClassLoader().getResourceAsStream("resources/validasi_jurnal_hpp_jm.html");
            }
            if (is == null) {
                return "<html><body><h1>File validasi_jurnal_hpp_jm.html tidak ditemukan</h1></body></html>";
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            reader.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "<html><body><h1>Error loading HTML</h1></body></html>";
        }
    }

    private String escapeJS(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private String placeholderIn(int jumlah) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < jumlah; i++) {
            if (i > 0) sb.append(",");
            sb.append("?");
        }
        return sb.toString();
    }

    private String formatTglSql(String tgl) {
        if (tgl == null || tgl.trim().isEmpty()) {
            return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        }
        tgl = tgl.trim().replaceAll("'", "");
        if (tgl.matches("^\\d{4}-\\d{2}-\\d{2}.*")) {
            return tgl.substring(0, 10);
        }
        if (tgl.matches("^\\d{4}/\\d{2}/\\d{2}.*")) {
            return tgl.substring(0, 10).replace('/', '-');
        }
        if (tgl.matches("^\\d{2}-\\d{2}-\\d{4}.*")) {
            return Valid.SetTgl(tgl.substring(0, 10));
        }
        if (tgl.matches("^\\d{2}/\\d{2}/\\d{4}.*")) {
            return Valid.SetTgl(tgl.substring(0, 10).replace('/', '-'));
        }
        if (tgl.length() >= 10) {
            String sub = tgl.substring(0, 10);
            if (sub.charAt(4) == '-' && sub.charAt(7) == '-') {
                return sub;
            }
        }
        try {
            String res = Valid.SetTgl(tgl);
            if (res != null && res.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                return res;
            }
        } catch (Exception e) {}
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
    }

    // ============================================================
    // JAVA BRIDGE
    // ============================================================
    public class JavaBridge {

        public String getUserInfo() {
            return akses.getkode();
        }

        public void getDaftarDokter() {
            dbExecutor.submit(() -> {
                JSONArray list = new JSONArray();
                try {
                    String sql = "SELECT kd_dokter, nm_dokter FROM dokter WHERE status='1' ORDER BY nm_dokter ASC";
                    try (Statement st = koneksi.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                        while (rs.next()) {
                            JSONObject d = new JSONObject();
                            d.put("kd_dokter", rs.getString("kd_dokter"));
                            d.put("nm_dokter", rs.getString("nm_dokter"));
                            list.put(d);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("getDaftarDokter error: " + e.getMessage());
                }
                final String json = list.toString();
                Platform.runLater(() -> {
                    if (engine != null) engine.executeScript("terimaDaftarDokter('" + escapeJS(json) + "')");
                });
            });
        }

        public void loadRekonsiliasi(String payloadJson) {
            dbExecutor.submit(() -> {
                JSONArray result = new JSONArray();
                try {
                    JSONObject req = new JSONObject(payloadJson);
                    String tglAwal = req.optString("tglAwal", "");
                    String tglAkhir = req.optString("tglAkhir", "");
                    String mode = req.optString("mode", "Semua"); // Semua, HPP, JM
                    String unitFilter = req.optString("unit", "Semua");
                    String kdDokterFilter = req.optString("kdDokter", "Semua");
                    String statusJurnalFilter = req.optString("statusJurnal", "Semua"); // Semua, Belum, Sudah
                    String kw = req.optString("keyword", "").trim().toLowerCase();

                    List<Map<String, Object>> items = new ArrayList<>();
                    List<String> noRawatList = new ArrayList<>();

                    // 1. DATA HPP OBAT & BHP
                    if (mode.equals("Semua") || mode.equals("HPP")) {
                        StringBuilder sqlHpp = new StringBuilder();
                        sqlHpp.append("SELECT ")
                              .append("  dpo.no_rawat, dpo.tgl_perawatan, dpo.jam, rp.no_rkm_medis, pas.nm_pasien, ")
                              .append("  IFNULL(pj.png_jawab, '-') AS penjamin, rp.status_lanjut, ")
                              .append("  db.kode_brng, db.nama_brng, dpo.jml, db.h_beli, (dpo.jml * db.h_beli) AS total_hpp, ")
                              .append("  dpo.biaya_obat, dpo.total AS total_jual, dpo.status AS asal_pemberian, ")
                              .append("  COALESCE(dr.nm_dokter, '-') AS nm_dokter ")
                              .append("FROM detail_pemberian_obat dpo ")
                              .append("INNER JOIN databarang db ON dpo.kode_brng = db.kode_brng ")
                              .append("INNER JOIN reg_periksa rp ON dpo.no_rawat = rp.no_rawat ")
                              .append("INNER JOIN pasien pas ON rp.no_rkm_medis = pas.no_rkm_medis ")
                              .append("LEFT JOIN penjab pj ON rp.kd_pj = pj.kd_pj ")
                              .append("LEFT JOIN dokter dr ON rp.kd_dokter = dr.kd_dokter ")
                              .append("WHERE CONCAT(dpo.tgl_perawatan, ' ', dpo.jam) BETWEEN ? AND ? ");

                        if (!kdDokterFilter.equals("Semua")) {
                            sqlHpp.append(" AND rp.kd_dokter = ? ");
                        }
                        sqlHpp.append("ORDER BY dpo.tgl_perawatan DESC, dpo.jam DESC LIMIT 1000");

                        try (PreparedStatement psHpp = koneksi.prepareStatement(sqlHpp.toString())) {
                            psHpp.setString(1, tglAwal);
                            psHpp.setString(2, tglAkhir);
                            if (!kdDokterFilter.equals("Semua")) {
                                psHpp.setString(3, kdDokterFilter);
                            }
                            try (ResultSet rs = psHpp.executeQuery()) {
                                while (rs.next()) {
                                    String unit = "Ralan".equalsIgnoreCase(rs.getString("status_lanjut")) ? "Rawat Jalan" : "Rawat Inap";
                                    if (!unitFilter.equals("Semua") && !unit.equalsIgnoreCase(unitFilter)) continue;

                                    Map<String, Object> m = new HashMap<>();
                                    m.put("modul", "HPP");
                                    m.put("waktu", rs.getString("tgl_perawatan") + " " + rs.getString("jam"));
                                    m.put("no_rawat", rs.getString("no_rawat"));
                                    m.put("no_rkm_medis", rs.getString("no_rkm_medis"));
                                    m.put("nm_pasien", rs.getString("nm_pasien"));
                                    m.put("penjamin", rs.getString("penjamin"));
                                    m.put("unit", unit);
                                    m.put("nm_dokter", rs.getString("nm_dokter"));
                                    m.put("item", rs.getString("nama_brng"));
                                    m.put("qty", rs.getDouble("jml"));
                                    m.put("tarif_jual", rs.getDouble("total_jual"));
                                    m.put("nilai_hpp_jm", rs.getDouble("total_hpp"));
                                    m.put("kd_debet", akunDebetHpp);
                                    m.put("kd_kredit", akunKreditHpp);
                                    items.add(m);
                                    noRawatList.add(rs.getString("no_rawat"));
                                }
                            }
                        }
                    }

                    // 2. DATA JASA MEDIS DOKTER (RALAN & RANAP & OPERASI)
                    if (mode.equals("Semua") || mode.equals("JM")) {
                        // Rawat Jalan Dokter
                        if (unitFilter.equals("Semua") || unitFilter.equals("Ralan") || unitFilter.equals("Rawat Jalan")) {
                            StringBuilder sqlRj = new StringBuilder();
                            sqlRj.append("SELECT r.no_rawat, r.tgl_perawatan, r.jam_rawat, rp.no_rkm_medis, pas.nm_pasien, ")
                                 .append("  IFNULL(pj.png_jawab, '-') AS penjamin, 'Rawat Jalan' AS unit, ")
                                 .append("  d.nm_dokter, j.nm_perawatan, count(r.kd_jenis_prw) as jml, sum(r.biaya_rawat) as total_jual, sum(r.tarif_tindakandr) as total_jm ")
                                 .append("FROM rawat_jl_dr r ")
                                 .append("INNER JOIN jns_perawatan j ON r.kd_jenis_prw = j.kd_jenis_prw ")
                                 .append("INNER JOIN dokter d ON r.kd_dokter = d.kd_dokter ")
                                 .append("INNER JOIN reg_periksa rp ON r.no_rawat = rp.no_rawat ")
                                 .append("INNER JOIN pasien pas ON rp.no_rkm_medis = pas.no_rkm_medis ")
                                 .append("LEFT JOIN penjab pj ON rp.kd_pj = pj.kd_pj ")
                                 .append("WHERE CONCAT(r.tgl_perawatan, ' ', r.jam_rawat) BETWEEN ? AND ? ");
                            if (!kdDokterFilter.equals("Semua")) sqlRj.append(" AND r.kd_dokter = ? ");
                            sqlRj.append("GROUP BY r.no_rawat, r.kd_jenis_prw, r.kd_dokter LIMIT 500");

                            try (PreparedStatement psRj = koneksi.prepareStatement(sqlRj.toString())) {
                                psRj.setString(1, tglAwal);
                                psRj.setString(2, tglAkhir);
                                if (!kdDokterFilter.equals("Semua")) psRj.setString(3, kdDokterFilter);
                                try (ResultSet rs = psRj.executeQuery()) {
                                    while (rs.next()) {
                                        Map<String, Object> m = new HashMap<>();
                                        m.put("modul", "JM");
                                        m.put("waktu", rs.getString("tgl_perawatan") + " " + rs.getString("jam_rawat"));
                                        m.put("no_rawat", rs.getString("no_rawat"));
                                        m.put("no_rkm_medis", rs.getString("no_rkm_medis"));
                                        m.put("nm_pasien", rs.getString("nm_pasien"));
                                        m.put("penjamin", rs.getString("penjamin"));
                                        m.put("unit", "Rawat Jalan");
                                        m.put("nm_dokter", rs.getString("nm_dokter"));
                                        m.put("item", rs.getString("nm_perawatan"));
                                        m.put("qty", rs.getDouble("jml"));
                                        m.put("tarif_jual", rs.getDouble("total_jual"));
                                        m.put("nilai_hpp_jm", rs.getDouble("total_jm"));
                                        m.put("kd_debet", akunDebetJm);
                                        m.put("kd_kredit", akunKreditJm);
                                        items.add(m);
                                        noRawatList.add(rs.getString("no_rawat"));
                                    }
                                }
                            }
                        }

                        // Rawat Inap Dokter
                        if (unitFilter.equals("Semua") || unitFilter.equals("Ranap") || unitFilter.equals("Rawat Inap")) {
                            StringBuilder sqlRi = new StringBuilder();
                            sqlRi.append("SELECT r.no_rawat, r.tgl_perawatan, r.jam_rawat, rp.no_rkm_medis, pas.nm_pasien, ")
                                 .append("  IFNULL(pj.png_jawab, '-') AS penjamin, 'Rawat Inap' AS unit, ")
                                 .append("  d.nm_dokter, j.nm_perawatan, count(r.kd_jenis_prw) as jml, sum(r.biaya_rawat) as total_jual, sum(r.tarif_tindakandr) as total_jm ")
                                 .append("FROM rawat_inap_dr r ")
                                 .append("INNER JOIN jns_perawatan_inap j ON r.kd_jenis_prw = j.kd_jenis_prw ")
                                 .append("INNER JOIN dokter d ON r.kd_dokter = d.kd_dokter ")
                                 .append("INNER JOIN reg_periksa rp ON r.no_rawat = rp.no_rawat ")
                                 .append("INNER JOIN pasien pas ON rp.no_rkm_medis = pas.no_rkm_medis ")
                                 .append("LEFT JOIN penjab pj ON rp.kd_pj = pj.kd_pj ")
                                 .append("WHERE CONCAT(r.tgl_perawatan, ' ', r.jam_rawat) BETWEEN ? AND ? ");
                            if (!kdDokterFilter.equals("Semua")) sqlRi.append(" AND r.kd_dokter = ? ");
                            sqlRi.append("GROUP BY r.no_rawat, r.kd_jenis_prw, r.kd_dokter LIMIT 500");

                            try (PreparedStatement psRi = koneksi.prepareStatement(sqlRi.toString())) {
                                psRi.setString(1, tglAwal);
                                psRi.setString(2, tglAkhir);
                                if (!kdDokterFilter.equals("Semua")) psRi.setString(3, kdDokterFilter);
                                try (ResultSet rs = psRi.executeQuery()) {
                                    while (rs.next()) {
                                        Map<String, Object> m = new HashMap<>();
                                        m.put("modul", "JM");
                                        m.put("waktu", rs.getString("tgl_perawatan") + " " + rs.getString("jam_rawat"));
                                        m.put("no_rawat", rs.getString("no_rawat"));
                                        m.put("no_rkm_medis", rs.getString("no_rkm_medis"));
                                        m.put("nm_pasien", rs.getString("nm_pasien"));
                                        m.put("penjamin", rs.getString("penjamin"));
                                        m.put("unit", "Rawat Inap");
                                        m.put("nm_dokter", rs.getString("nm_dokter"));
                                        m.put("item", rs.getString("nm_perawatan"));
                                        m.put("qty", rs.getDouble("jml"));
                                        m.put("tarif_jual", rs.getDouble("total_jual"));
                                        m.put("nilai_hpp_jm", rs.getDouble("total_jm"));
                                        m.put("kd_debet", akunDebetJm);
                                        m.put("kd_kredit", akunKreditJm);
                                        items.add(m);
                                        noRawatList.add(rs.getString("no_rawat"));
                                    }
                                }
                            }
                        }

                        // Operasi
                        if (unitFilter.equals("Semua") || unitFilter.equals("Operasi")) {
                            StringBuilder sqlOp = new StringBuilder();
                            sqlOp.append("SELECT o.no_rawat, o.tgl_operasi, o.jam_selesai, rp.no_rkm_medis, pas.nm_pasien, ")
                                 .append("  IFNULL(pj.png_jawab, '-') AS penjamin, 'Operasi' AS unit, ")
                                 .append("  d.nm_dokter, p.nm_perawatan, 1 as jml, ")
                                 .append("  (o.biayaoperator1+o.biayaoperator2+o.biayadokter_anestesi+o.biayadokter_anak) as total_jual, ")
                                 .append("  (o.biayaoperator1+o.biayaoperator2+o.biayadokter_anestesi+o.biayadokter_anak) as total_jm ")
                                 .append("FROM operasi o ")
                                 .append("INNER JOIN paket_operasi p ON o.kode_paket = p.kode_paket ")
                                 .append("LEFT JOIN dokter d ON o.operator1 = d.kd_dokter ")
                                 .append("INNER JOIN reg_periksa rp ON o.no_rawat = rp.no_rawat ")
                                 .append("INNER JOIN pasien pas ON rp.no_rkm_medis = pas.no_rkm_medis ")
                                 .append("LEFT JOIN penjab pj ON rp.kd_pj = pj.kd_pj ")
                                 .append("WHERE CONCAT(o.tgl_operasi, ' ', o.jam_selesai) BETWEEN ? AND ? ");
                            if (!kdDokterFilter.equals("Semua")) sqlOp.append(" AND o.operator1 = ? ");
                            sqlOp.append("LIMIT 300");

                            try (PreparedStatement psOp = koneksi.prepareStatement(sqlOp.toString())) {
                                psOp.setString(1, tglAwal);
                                psOp.setString(2, tglAkhir);
                                if (!kdDokterFilter.equals("Semua")) psOp.setString(3, kdDokterFilter);
                                try (ResultSet rs = psOp.executeQuery()) {
                                    while (rs.next()) {
                                        Map<String, Object> m = new HashMap<>();
                                        m.put("modul", "JM");
                                        m.put("waktu", rs.getString("tgl_operasi") + " " + rs.getString("jam_selesai"));
                                        m.put("no_rawat", rs.getString("no_rawat"));
                                        m.put("no_rkm_medis", rs.getString("no_rkm_medis"));
                                        m.put("nm_pasien", rs.getString("nm_pasien"));
                                        m.put("penjamin", rs.getString("penjamin"));
                                        m.put("unit", "Operasi");
                                        m.put("nm_dokter", rs.getString("nm_dokter"));
                                        m.put("item", rs.getString("nm_perawatan"));
                                        m.put("qty", 1.0);
                                        m.put("tarif_jual", rs.getDouble("total_jual"));
                                        m.put("nilai_hpp_jm", rs.getDouble("total_jm"));
                                        m.put("kd_debet", akunDebetJm);
                                        m.put("kd_kredit", akunKreditJm);
                                        items.add(m);
                                        noRawatList.add(rs.getString("no_rawat"));
                                    }
                                }
                            }
                        }
                    }

                    // 3. CROSS-CHECK TERHADAP JURNAL RESMI (BATCH)
                    Map<String, String> mapJurnal = new HashMap<>();
                    if (!noRawatList.isEmpty()) {
                        String inClause = placeholderIn(Math.min(noRawatList.size(), 800));
                        String sqlJur = "SELECT no_bukti, no_jurnal, keterangan FROM jurnal WHERE no_bukti IN (" + inClause + ")";
                        try (PreparedStatement psJur = koneksi.prepareStatement(sqlJur)) {
                            int pIdx = 1;
                            for (int i = 0; i < Math.min(noRawatList.size(), 800); i++) {
                                psJur.setString(pIdx++, noRawatList.get(i));
                            }
                            try (ResultSet rsJur = psJur.executeQuery()) {
                                while (rsJur.next()) {
                                    String nb = rsJur.getString("no_bukti");
                                    String nj = rsJur.getString("no_jurnal");
                                    String ket = rsJur.getString("keterangan").toUpperCase();
                                    mapJurnal.put(nb + "|" + ket, nj);
                                    mapJurnal.put(nb, nj); // generic fallback
                                }
                            }
                        } catch (Exception exJur) {
                            System.err.println("Gagal cross-check jurnal: " + exJur.getMessage());
                        }
                    }

                    // 4. FILTERING & CONVERSION TO JSON
                    for (Map<String, Object> m : items) {
                        String noRawat = (String) m.get("no_rawat");
                        String item = (String) m.get("item");
                        String modul = (String) m.get("modul");

                        // Keyword filter
                        if (!kw.isEmpty()) {
                            String haystack = (noRawat + " " + m.get("no_rkm_medis") + " " + m.get("nm_pasien") + " " + item + " " + m.get("nm_dokter")).toLowerCase();
                            if (!haystack.contains(kw)) continue;
                        }

                        // Jurnal status check
                        String noJurnal = mapJurnal.get(noRawat + "|" + item.toUpperCase());
                        if (noJurnal == null) {
                            // Cek berdasarkan jenis modul
                            if (modul.equals("HPP")) {
                                noJurnal = mapJurnal.get(noRawat + "|PEMBERIAN OBAT");
                            } else {
                                noJurnal = mapJurnal.get(noRawat + "|TINDAKAN");
                            }
                        }

                        boolean isSudah = (noJurnal != null && !noJurnal.isEmpty());
                        if (statusJurnalFilter.equals("Belum") && isSudah) continue;
                        if (statusJurnalFilter.equals("Sudah") && !isSudah) continue;

                        JSONObject obj = new JSONObject();
                        obj.put("modul", modul);
                        obj.put("waktu", m.get("waktu"));
                        obj.put("no_rawat", noRawat);
                        obj.put("no_rkm_medis", m.get("no_rkm_medis"));
                        obj.put("nm_pasien", m.get("nm_pasien"));
                        obj.put("penjamin", m.get("penjamin"));
                        obj.put("unit", m.get("unit"));
                        obj.put("nm_dokter", m.get("nm_dokter"));
                        obj.put("item", item);
                        obj.put("qty", m.get("qty"));
                        obj.put("tarif_jual", m.get("tarif_jual"));
                        obj.put("nilai_hpp_jm", m.get("nilai_hpp_jm"));
                        obj.put("kd_debet", m.get("kd_debet"));
                        obj.put("kd_kredit", m.get("kd_kredit"));
                        obj.put("status_jurnal", isSudah ? "Sudah" : "Belum");
                        obj.put("no_jurnal", isSudah ? noJurnal : "");
                        result.put(obj);
                    }

                } catch (Exception e) {
                    System.err.println("loadRekonsiliasi error: " + e.getMessage());
                    e.printStackTrace();
                }

                final String finalJson = result.toString();
                Platform.runLater(() -> {
                    if (engine != null) engine.executeScript("terimaDataRekonsiliasi('" + escapeJS(finalJson) + "')");
                });
            });
        }

        public void postingJurnalBatch(String jsonItemsStr) {
            dbExecutor.submit(() -> {
                boolean sukses = false;
                String pesan = "";
                int countPosted = 0;
                try {
                    JSONArray items = new JSONArray(jsonItemsStr);
                    if (items.length() == 0) {
                        pesan = "Tidak ada transaksi yang dipilih.";
                    } else {
                        koneksi.setAutoCommit(false);

                        String sqlHeader = "INSERT INTO jurnal (no_jurnal, no_bukti, tgl_jurnal, jam_jurnal, jenis, keterangan) VALUES (?,?,?,?,?,?)";
                        String sqlDetail = "INSERT INTO detail_posting_jurnal (no_jurnal, kd_rek, debet, kredit, keterangan) VALUES (?,?,?,?,?)";
                        String sqlDetail2 = "INSERT INTO detailjurnal (no_jurnal, kd_rek, debet, kredit) VALUES (?,?,?,?)";

                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            String noRawat = item.getString("no_rawat");
                            String rawWaktu = item.optString("waktu", "");
                            String tglSql = formatTglSql(rawWaktu);
                            String jamJurnal = rawWaktu.length() >= 19 ? rawWaktu.substring(11, 19) : "00:00:00";

                            String modul = item.optString("modul", "HPP");
                            String prefix = modul.equals("HPP") ? "JHPP" : "JJM";
                            String noJurnal = Valid.autoNomer3(
                                "select ifnull(MAX(CONVERT(RIGHT(no_jurnal,6),signed)),0) from jurnal where tgl_jurnal='" + tglSql + "'",
                                prefix + tglSql.replaceAll("-", ""), 6
                            );

                            String namaItem = item.optString("item", "");
                            String namaPasien = item.optString("nm_pasien", "");
                            String keterangan = (modul.equals("HPP") ? "HPP OBAT: " : "BEBAN JM: ") + namaItem + " (" + namaPasien + ")";
                            if (keterangan.length() > 200) keterangan = keterangan.substring(0, 200);

                            try (PreparedStatement psH = koneksi.prepareStatement(sqlHeader)) {
                                psH.setString(1, noJurnal);
                                psH.setString(2, noRawat);
                                psH.setString(3, tglSql);
                                psH.setString(4, jamJurnal);
                                psH.setString(5, "U");
                                psH.setString(6, keterangan + ", OLEH " + akses.getkode());
                                psH.executeUpdate();
                            }

                            String kdDebet = item.getString("kd_debet");
                            String kdKredit = item.getString("kd_kredit");
                            double nilai = item.getDouble("nilai_hpp_jm");

                            // Debet
                            try (PreparedStatement psD = koneksi.prepareStatement(sqlDetail)) {
                                psD.setString(1, noJurnal);
                                psD.setString(2, kdDebet);
                                psD.setDouble(3, nilai);
                                psD.setDouble(4, 0);
                                psD.setString(5, keterangan);
                                psD.executeUpdate();
                            }
                            try (PreparedStatement psD2 = koneksi.prepareStatement(sqlDetail2)) {
                                psD2.setString(1, noJurnal);
                                psD2.setString(2, kdDebet);
                                psD2.setDouble(3, nilai);
                                psD2.setDouble(4, 0);
                                psD2.executeUpdate();
                            }

                            // Kredit
                            try (PreparedStatement psK = koneksi.prepareStatement(sqlDetail)) {
                                psK.setString(1, noJurnal);
                                psK.setString(2, kdKredit);
                                psK.setDouble(3, 0);
                                psK.setDouble(4, nilai);
                                psK.setString(5, keterangan);
                                psK.executeUpdate();
                            }
                            try (PreparedStatement psK2 = koneksi.prepareStatement(sqlDetail2)) {
                                psK2.setString(1, noJurnal);
                                psK2.setString(2, kdKredit);
                                psK2.setDouble(3, 0);
                                psK2.setDouble(4, nilai);
                                psK2.executeUpdate();
                            }

                            countPosted++;
                        }

                        koneksi.commit();
                        koneksi.setAutoCommit(true);
                        sukses = true;
                        pesan = "Berhasil memposting " + countPosted + " transaksi ke Jurnal Buku Besar resmi!";
                        trackSql("POSTING JURNAL HPP & JM BATCH: " + countPosted + " transaksi");
                    }
                } catch (Exception e) {
                    try { koneksi.rollback(); } catch (Exception ex) {}
                    pesan = "Gagal posting: " + e.getMessage();
                    System.err.println("postingJurnalBatch error: " + e.getMessage());
                } finally {
                    try { koneksi.setAutoCommit(true); } catch (Exception ex) {}
                }

                final boolean ok = sukses;
                final String msg = pesan;
                final int jml = countPosted;
                Platform.runLater(() -> {
                    if (engine != null) engine.executeScript("postingJurnalSelesai(" + ok + ", '" + escapeJS(msg) + "', " + jml + ");");
                });
            });
        }

        public void keluarForm() {
            SwingUtilities.invokeLater(() -> dispose());
        }

        private void trackSql(String pesan) {
            try {
                if ("yes".equalsIgnoreCase(koneksiDB.AKTIFKANTRACKSQL())) {
                    String host = "";
                    try {
                        java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
                        host = addr.getHostName() + " " + addr.getHostAddress() + " ";
                    } catch (Exception e) {}
                    try (PreparedStatement ps = koneksi.prepareStatement("INSERT INTO trackersql (tanggal, sqle, usere) VALUES (NOW(), ?, ?)")) {
                        ps.setString(1, host + pesan);
                        ps.setString(2, akses.getkode());
                        ps.executeUpdate();
                    }
                }
            } catch (Exception e) {
                System.err.println("Gagal catat trackersql: " + e.getMessage());
            }
        }
    }

    // ============================================================
    // GENERATED UI
    // ============================================================
    private void initComponents() {
        internalFrame1 = new widget.InternalFrame();
        jPanelWeb = new javax.swing.JPanel();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setSize(1400, 800);
        setLocationRelativeTo(null);
        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                "::[ Validasi & Rekonsiliasi Jurnal HPP & Jasa Medis Dokter ]::",
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
