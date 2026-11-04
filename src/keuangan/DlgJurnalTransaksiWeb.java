package keuangan;

import fungsi.akses;
import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.SwingUtilities;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicBoolean;

public class DlgJurnalTransaksiWeb extends javax.swing.JDialog {

    private Connection getKoneksi() {
        return koneksiDB.condb();
    }

    private WebView webView;
    private WebEngine engine;
    private JFXPanel fxPanel;
    private boolean isWebViewInitialized = false;
    private final JavaBridge bridge = new JavaBridge();

    private final java.util.concurrent.ExecutorService dbExecutor
            = java.util.concurrent.Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "Jurnal-Transaksi-Worker");
                t.setDaemon(true);
                return t;
            });

    private static final int MAX_BARIS_TAMPIL = 3000;
    private static final boolean DEBUG = false;
    private final AtomicBoolean isPostingRunning = new AtomicBoolean(false);

    public DlgJurnalTransaksiWeb(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        initializeWebView();
    }

    private void initializeWebView() {
        if (isWebViewInitialized) return;
        new JFXPanel();

        Platform.runLater(() -> {
            try {
                webView = new WebView();
                engine = webView.getEngine();
                engine.setJavaScriptEnabled(true);
                engine.setOnAlert(event -> {
                    String data = event.getData();
                    SwingUtilities.invokeLater(() -> {
                        javax.swing.JOptionPane.showMessageDialog(DlgJurnalTransaksiWeb.this, data, "Informasi", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    });
                });
                engine.setConfirmHandler(param -> {
                    final boolean[] res = new boolean[1];
                    try {
                        if (SwingUtilities.isEventDispatchThread()) {
                            int r = javax.swing.JOptionPane.showConfirmDialog(DlgJurnalTransaksiWeb.this, param, "Konfirmasi", javax.swing.JOptionPane.YES_NO_OPTION);
                            res[0] = (r == javax.swing.JOptionPane.YES_OPTION);
                        } else {
                            SwingUtilities.invokeAndWait(() -> {
                                int r = javax.swing.JOptionPane.showConfirmDialog(DlgJurnalTransaksiWeb.this, param, "Konfirmasi", javax.swing.JOptionPane.YES_NO_OPTION);
                                res[0] = (r == javax.swing.JOptionPane.YES_OPTION);
                            });
                        }
                    } catch (Exception e) {
                        res[0] = true;
                    }
                    return res[0];
                });

                String html = getHTMLContent();
                engine.loadContent(html);

                engine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        JSObject window = (JSObject) engine.executeScript("window");
                        window.setMember("javaApp", bridge);
                        // javaApp baru tersedia di titik ini; panggil ulang init()
                        // (dijaga idempoten oleh flag appInitialized di JS) untuk
                        // menghindari race dengan DOMContentLoaded yang mungkin
                        // sudah lebih dulu jalan sebelum javaApp di-set.
                        engine.executeScript("if (typeof init === 'function') init();");
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
                System.err.println("Error WebView: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private String getHTMLContent() {
        try {
            InputStream is = getClass().getResourceAsStream("/resources/jurnal_transaksi.html");
            if (is == null) {
                is = getClass().getClassLoader().getResourceAsStream("resources/jurnal_transaksi.html");
            }
            if (is == null) {
                return "<html><body><h1>File jurnal_transaksi.html tidak ditemukan</h1></body></html>";
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
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


    private void pastikanTabelValidasi() {
        // DDL ditiadakan: skema tabel jurnal_posting_status dikelola langsung di database
    }

    private String normalisasiTanggal(String tgl) {
        if (tgl == null || tgl.trim().isEmpty()) return null;
        try {
            java.text.SimpleDateFormat sdfIn = new java.text.SimpleDateFormat("yyyy-MM-dd");
            sdfIn.setLenient(false);
            java.util.Date parsed = sdfIn.parse(tgl);
            return sdfIn.format(parsed);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void dispose() {
        if (bridge.rekening != null) { bridge.rekening.dispose(); bridge.rekening = null; }
        dbExecutor.shutdown();
        Platform.runLater(() -> {
            if (engine != null) {
                engine.load("about:blank");
            }
        });
        super.dispose();
    }

    // ============================================================
    // JAVA BRIDGE
    // ============================================================
    public class JavaBridge {
    private DlgRekening rekening;

    public void bukaCariRekening() {
        SwingUtilities.invokeLater(() -> {
            if (rekening == null) {
                rekening = new DlgRekening(null, false);
                rekening.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosed(java.awt.event.WindowEvent e) {
                        if (fungsi.akses.getform().equals("DlgJurnalTransaksiWeb")) {
                            if (rekening.getTabel().getSelectedRow() != -1) {
                                String kd = rekening.getTabel().getValueAt(rekening.getTabel().getSelectedRow(), 0).toString();
                                String nm = rekening.getTabel().getValueAt(rekening.getTabel().getSelectedRow(), 1).toString();
                                Platform.runLater(() -> {
                                    if (engine != null) {
                                        engine.executeScript("terimaPilihRekening('" + escapeJS(kd) + "','" + escapeJS(nm) + "');");
                                    }
                                });
                            }
                        }
                    }
                });
                
                rekening.getTabel().addKeyListener(new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyPressed(java.awt.event.KeyEvent e) {
                        if (fungsi.akses.getform().equals("DlgJurnalTransaksiWeb")) {
                            if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
                                rekening.dispose();
                            }
                        }
                    }
                });
            }
            fungsi.akses.setform("DlgJurnalTransaksiWeb");
            rekening.emptTeks();
            rekening.isCek();
            rekening.setSize(628, 674);
            rekening.setLocationRelativeTo(DlgJurnalTransaksiWeb.this);
            rekening.setAlwaysOnTop(false);
            rekening.setVisible(true);
        });
    }

    private String escapeJS(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

        public void tutup() {
            SwingUtilities.invokeLater(() -> dispose());
        }

        public String getTanggalHariIni() {
            return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        }

        public void loadData(String filterJson) {
            dbExecutor.submit(() -> {
                try {
                    pastikanTabelValidasi();

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode filter = mapper.readTree(filterJson);
                    
                    int page = filter.path("page").asInt(1);
                    int limit = filter.path("limit").asInt(250);
                    if (limit < 50) limit = 50;
                    if (limit > 500) limit = 500;
                    
                    StringBuilder baseFilter = new StringBuilder();
                    java.util.List<Object> filterParams = new java.util.ArrayList<>();
                    bangunFilter(filter, baseFilter, filterParams);
                    
                    String joins = "FROM jurnal j " +
                                   "LEFT JOIN reg_periksa rp ON j.no_bukti = rp.no_rawat " +
                                   "LEFT JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis " +
                                   "LEFT JOIN jurnal_posting_status jps ON j.no_jurnal = jps.no_jurnal ";
                    
                    // 1. Hitung total kelompok
                    int totalKelompok = 0;
                    String sqlCountKelompok = "SELECT COUNT(*) AS total FROM (SELECT 1 " + joins + baseFilter.toString() + " GROUP BY j.tgl_jurnal, IFNULL(j.no_bukti,'')) t";
                    try (java.sql.PreparedStatement psC = getKoneksi().prepareStatement(sqlCountKelompok)) {
                        for (int i = 0; i < filterParams.size(); i++) {
                            psC.setObject(i + 1, filterParams.get(i));
                        }
                        try (java.sql.ResultSet rsC = psC.executeQuery()) {
                            if (rsC.next()) totalKelompok = rsC.getInt("total");
                        }
                    }

                    // Hitung total jurnal
                    int totalJurnal = 0;
                    String sqlCountJurnal = "SELECT COUNT(DISTINCT j.no_jurnal) AS total " + joins + baseFilter.toString();
                    try (java.sql.PreparedStatement psC = getKoneksi().prepareStatement(sqlCountJurnal)) {
                        for (int i = 0; i < filterParams.size(); i++) {
                            psC.setObject(i + 1, filterParams.get(i));
                        }
                        try (java.sql.ResultSet rsC = psC.executeQuery()) {
                            if (rsC.next()) totalJurnal = rsC.getInt("total");
                        }
                    }
                    
                    int totalPages = Math.max(1, (int) Math.ceil((double) totalKelompok / limit));
                    if (page > totalPages) page = totalPages;
                    if (page < 1) page = 1;
                    
                    int offset = (page - 1) * limit;

                    // 2. Daftar kelompok halaman ini
                    String sqlKelompok = "SELECT j.tgl_jurnal AS tgl, IFNULL(j.no_bukti,'') AS nb " + joins + baseFilter.toString() + 
                                         " GROUP BY j.tgl_jurnal, IFNULL(j.no_bukti,'') ORDER BY j.tgl_jurnal ASC, nb ASC LIMIT ? OFFSET ?";
                    
                    String tglA = null, nbA = null, tglZ = null, nbZ = null;
                    try (java.sql.PreparedStatement ps = getKoneksi().prepareStatement(sqlKelompok)) {
                        for (int i = 0; i < filterParams.size(); i++) {
                            ps.setObject(i + 1, filterParams.get(i));
                        }
                        ps.setInt(filterParams.size() + 1, limit);
                        ps.setInt(filterParams.size() + 2, offset);
                        
                        try (java.sql.ResultSet rs = ps.executeQuery()) {
                            while(rs.next()) {
                                if (tglA == null) {
                                    tglA = rs.getString("tgl");
                                    nbA = rs.getString("nb");
                                }
                                tglZ = rs.getString("tgl");
                                nbZ = rs.getString("nb");
                            }
                        }
                    }
                    
                    ArrayNode arr = mapper.createArrayNode();
                    if (tglA == null) {
                        // kosong
                    } else {
                        // 3. Baris detail halaman ini
                        StringBuilder sqlDetail = new StringBuilder();
                        sqlDetail.append("SELECT j.no_jurnal, j.no_bukti, j.tgl_jurnal, ");
                        sqlDetail.append("dj.kd_rek, COALESCE(r.nm_rek,'(REKENING TIDAK TERDAFTAR)') AS nm_rek, dj.debet, dj.kredit, ");
                        sqlDetail.append("rp.no_rkm_medis, p.nm_pasien, rp.status_lanjut, ");
                        sqlDetail.append("IF(jps.status_posting = 'POSTED', 'Posted', 'Draft') AS status_validasi, ");
                        sqlDetail.append("pl.tgl_pulang_raw ");
                        sqlDetail.append("FROM jurnal j ");
                        sqlDetail.append("STRAIGHT_JOIN detailjurnal dj ON j.no_jurnal = dj.no_jurnal ");
                        sqlDetail.append("LEFT JOIN rekening r ON dj.kd_rek = r.kd_rek ");
                        sqlDetail.append("LEFT JOIN reg_periksa rp ON j.no_bukti = rp.no_rawat ");
                        sqlDetail.append("LEFT JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis ");
                        sqlDetail.append("LEFT JOIN jurnal_posting_status jps ON j.no_jurnal = jps.no_jurnal ");
                        sqlDetail.append("LEFT JOIN ( ");
                        sqlDetail.append("    SELECT ki.no_rawat, MAX(ki.tgl_keluar) AS tgl_pulang_raw ");
                        sqlDetail.append("    FROM kamar_inap ki ");
                        sqlDetail.append("    WHERE ki.stts_pulang NOT IN ('-', 'Pindah Kamar') ");
                        sqlDetail.append("      AND ki.tgl_keluar IS NOT NULL AND ki.tgl_keluar != '0000-00-00' ");
                        sqlDetail.append("    GROUP BY ki.no_rawat ");
                        sqlDetail.append(") pl ON j.no_bukti = pl.no_rawat ");
                        sqlDetail.append(baseFilter.toString());
                        sqlDetail.append(" AND (j.tgl_jurnal > ? OR (j.tgl_jurnal = ? AND IFNULL(j.no_bukti,'') >= ?)) ");
                        sqlDetail.append(" AND (j.tgl_jurnal < ? OR (j.tgl_jurnal = ? AND IFNULL(j.no_bukti,'') <= ?)) ");
                        sqlDetail.append(" ORDER BY j.tgl_jurnal ASC, IFNULL(j.no_bukti,'') ASC, j.no_jurnal ASC, dj.debet DESC, dj.kd_rek ASC");
                        
                        try (java.sql.PreparedStatement ps = getKoneksi().prepareStatement(sqlDetail.toString())) {
                            int idx = 1;
                            for (Object prm : filterParams) ps.setObject(idx++, prm);
                            ps.setString(idx++, tglA);
                            ps.setString(idx++, tglA);
                            ps.setString(idx++, nbA);
                            ps.setString(idx++, tglZ);
                            ps.setString(idx++, tglZ);
                            ps.setString(idx++, nbZ);
                            
                            java.text.SimpleDateFormat outSdf = new java.text.SimpleDateFormat("dd-MM-yyyy");
                            int rowCount = 0;
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                while(rs.next()) {
                                    rowCount++;
                                    ObjectNode obj = mapper.createObjectNode();
                                    obj.put("no_jurnal", rs.getString("no_jurnal"));
                                    obj.put("no_bukti", rs.getString("no_bukti"));
                                    
                                    String tgl = rs.getString("tgl_jurnal");
                                    if(tgl != null && tgl.length() > 10) tgl = tgl.substring(0,10);
                                    try {
                                        if(tgl != null) tgl = outSdf.format(java.sql.Date.valueOf(tgl));
                                    } catch(Exception ex) {}
                                    
                                    obj.put("tgl_jurnal", tgl);
                                    obj.put("kd_rek", rs.getString("kd_rek"));
                                    obj.put("nm_rek", rs.getString("nm_rek"));
                                    obj.put("debet", rs.getDouble("debet"));
                                    obj.put("kredit", rs.getDouble("kredit"));
                                    
                                    String noRm = rs.getString("no_rkm_medis");
                                    String nmPas = rs.getString("nm_pasien");
                                    String stsLanjut = rs.getString("status_lanjut");
                                    
                                    if (noRm == null || noRm.trim().isEmpty()) {
                                        obj.put("grup_header", "-- JURNAL UMUM / PENGELUARAN (NON-PASIEN) --");
                                    } else {
                                        obj.put("grup_header", "-- (" + noRm + ") " + nmPas + " - " + (stsLanjut != null ? stsLanjut.toUpperCase() : "") + " --");
                                    }
                                    
                                    obj.put("status", rs.getString("status_validasi"));
                                    
                                    String tglPulangRaw = rs.getString("tgl_pulang_raw");
                                    String statusPulang = "";
                                    String tglPulang = "";
                                    if ("Ranap".equalsIgnoreCase(stsLanjut)) {
                                        if (tglPulangRaw != null && !tglPulangRaw.trim().isEmpty() && !"0000-00-00".equals(tglPulangRaw)) {
                                            statusPulang = "PULANG";
                                            try {
                                                if (tglPulangRaw.length() > 10) tglPulangRaw = tglPulangRaw.substring(0, 10);
                                                tglPulang = outSdf.format(java.sql.Date.valueOf(tglPulangRaw));
                                            } catch (Exception ex) {
                                                tglPulang = tglPulangRaw;
                                            }
                                        } else {
                                            statusPulang = "DIRAWAT";
                                        }
                                    }
                                    obj.put("status_pulang", statusPulang);
                                    obj.put("tgl_pulang", tglPulang);
                                    
                                    arr.add(obj);
                                }
                            }
                            if (rowCount > 8000) {
                                System.err.println("[WARNING] loadData mengembalikan " + rowCount + " baris detail. Kelompok terlalu besar?");
                            }
                        }
                    }

                    String jsonResult = arr.toString();
                    String safeJsonResult = "\"[]\"";
                    try {
                        safeJsonResult = mapper.writeValueAsString(jsonResult);
                        safeJsonResult = safeJsonResult.replace("\\u2028", "\\\\u2028").replace("\\u2029", "\\\\u2029");
                    } catch (Exception ex) {}
                    
                    final int finalPage = page;
                    final int finalTotalJurnal = totalJurnal;
                    final int finalTotalPages = totalPages;
                    final String fj = safeJsonResult;
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("terimaDataJurnal(" + fj + ");");
                            engine.executeScript("setInfoHalaman(" + finalPage + ", " + finalTotalPages + ", " + finalTotalJurnal + ");");
                        }
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("terimaDataJurnal('[]'); alert('Error load data: " + escapeJS(e.getMessage()) + "');");
                        }
                    });
                }
            });
        }

        public void loadRekapAkunBayar(String filterJson) {
            dbExecutor.submit(() -> {
                try {
                    pastikanTabelValidasi();

                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode filter = mapper.readTree(filterJson);
                    
                    String kategoriCOA = filter.has("kategoriCOA") ? filter.path("kategoriCOA").asText() : "Semua";
                    
                    if (kategoriCOA.equals("Detail_Bayar")) {
                        String statusPasien = filter.has("statusPasien") ? filter.path("statusPasien").asText() : "Semua";
                        String tgl1 = filter.has("tgl1") ? filter.path("tgl1").asText() : "";
                        String tgl2 = filter.has("tgl2") ? filter.path("tgl2").asText() : "";
                        
                        StringBuilder sql = new StringBuilder();
                        java.util.List<Object> params = new java.util.ArrayList<>();
                        
                        sql.append("SELECT ");
                        sql.append("    COALESCE(ab.kd_rek, '-') AS kd_rek, ");
                        sql.append("    u.nama_bayar, ");
                        sql.append("    u.jenis, ");
                        sql.append("    COUNT(DISTINCT u.no_rawat) AS jml_pasien, ");
                        sql.append("    SUM(u.besar_bayar) AS total_debet, ");
                        sql.append("    COALESCE(SUM(jn.jml_jurnal), 0) AS jml_jurnal, ");
                        sql.append("    COALESCE(SUM(jn.jml_draft), 0) AS jml_draft ");
                        sql.append("FROM ( ");
                        
                        boolean includeRalan = statusPasien.equals("Semua") || statusPasien.equals("Ralan") || statusPasien.equals("Rajal");
                        boolean includeRanap = statusPasien.equals("Semua") || statusPasien.equals("Ranap");
                        
                        if (includeRalan && includeRanap) {
                            sql.append("    SELECT nj.no_rawat, nj.tanggal, dnj.nama_bayar, dnj.besar_bayar, 'Kasir Ralan' AS jenis ");
                            sql.append("    FROM nota_jalan nj JOIN detail_nota_jalan dnj ON nj.no_rawat = dnj.no_rawat ");
                            sql.append("    WHERE nj.tanggal BETWEEN ? AND ? ");
                            sql.append("    UNION ALL ");
                            sql.append("    SELECT ni.no_rawat, ni.tanggal, dni.nama_bayar, dni.besar_bayar, 'Kasir Ranap' AS jenis ");
                            sql.append("    FROM nota_inap ni JOIN detail_nota_inap dni ON ni.no_rawat = dni.no_rawat ");
                            sql.append("    WHERE ni.tanggal BETWEEN ? AND ? ");
                            params.add(tgl1); params.add(tgl2);
                            params.add(tgl1); params.add(tgl2);
                        } else if (includeRalan) {
                            sql.append("    SELECT nj.no_rawat, nj.tanggal, dnj.nama_bayar, dnj.besar_bayar, 'Kasir Ralan' AS jenis ");
                            sql.append("    FROM nota_jalan nj JOIN detail_nota_jalan dnj ON nj.no_rawat = dnj.no_rawat ");
                            sql.append("    WHERE nj.tanggal BETWEEN ? AND ? ");
                            params.add(tgl1); params.add(tgl2);
                        } else {
                            sql.append("    SELECT ni.no_rawat, ni.tanggal, dni.nama_bayar, dni.besar_bayar, 'Kasir Ranap' AS jenis ");
                            sql.append("    FROM nota_inap ni JOIN detail_nota_inap dni ON ni.no_rawat = dni.no_rawat ");
                            sql.append("    WHERE ni.tanggal BETWEEN ? AND ? ");
                            params.add(tgl1); params.add(tgl2);
                        }
                        
                        sql.append(") u ");
                        sql.append("LEFT JOIN akun_bayar ab ON ab.nama_bayar = u.nama_bayar ");
                        sql.append("LEFT JOIN ( ");
                        sql.append("    SELECT ");
                        sql.append("        j.no_bukti, ");
                        sql.append("        COUNT(DISTINCT j.no_jurnal) AS jml_jurnal, ");
                        sql.append("        COUNT(DISTINCT CASE WHEN jps.status_posting IS NULL OR jps.status_posting != 'POSTED' THEN j.no_jurnal END) AS jml_draft ");
                        sql.append("    FROM jurnal j ");
                        sql.append("    LEFT JOIN jurnal_posting_status jps ON jps.no_jurnal = j.no_jurnal ");
                        sql.append("    WHERE j.tgl_jurnal BETWEEN ? AND ? ");
                        sql.append("    GROUP BY j.no_bukti ");
                        sql.append(") jn ON jn.no_bukti = u.no_rawat ");
                        params.add(tgl1); params.add(tgl2);
                        
                        sql.append("GROUP BY u.nama_bayar, ab.kd_rek, u.jenis ");
                        sql.append("ORDER BY u.jenis, u.nama_bayar");
                        
                        ArrayNode arr = mapper.createArrayNode();
                        try (java.sql.PreparedStatement ps = getKoneksi().prepareStatement(sql.toString())) {
                            for (int i = 0; i < params.size(); i++) {
                                ps.setObject(i + 1, params.get(i));
                            }
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    ObjectNode obj = mapper.createObjectNode();
                                    obj.put("kd_rek", rs.getString("kd_rek"));
                                    obj.put("nm_rek", rs.getString("nama_bayar"));
                                    obj.put("nama_bayar", "");
                                    obj.put("jenis", rs.getString("jenis"));
                                    obj.put("jml_jurnal", rs.getInt("jml_jurnal"));
                                    obj.put("jml_draft", rs.getInt("jml_draft"));
                                    obj.put("jml_pasien", rs.getInt("jml_pasien"));
                                    obj.put("total_debet", rs.getDouble("total_debet"));
                                    arr.add(obj);
                                }
                            }
                        }
                        
                        String jsonResult = arr.toString();
                        String safeJsonResult = "\"[]\"";
                        try {
                            safeJsonResult = mapper.writeValueAsString(jsonResult);
                            safeJsonResult = safeJsonResult.replace("\\u2028", "\\\\u2028").replace("\\u2029", "\\\\u2029");
                        } catch (Exception ex) {}
                        final String fj = safeJsonResult;
                        Platform.runLater(() -> {
                            if (engine != null) {
                                engine.executeScript("terimaRekapAkunBayar(" + fj + ");");
                            }
                        });
                        return;
                    }
                    
                    StringBuilder baseFilter = new StringBuilder();
                    java.util.List<Object> filterParams = new java.util.ArrayList<>();
                    bangunFilter(filter, baseFilter, filterParams);
                    
                    StringBuilder sql = new StringBuilder();
                    boolean isKredit = kategoriCOA.startsWith("Pendapatan");
                    String sisi = isKredit ? "kredit" : "debet";
                    
                    sql.append("SELECT dj.kd_rek, ");
                    sql.append("COALESCE(r.nm_rek,'(REKENING TIDAK TERDAFTAR)') AS nm_rek, ");
                    sql.append("m.jenis, ");
                    sql.append("'' AS nama_bayar, ");
                    sql.append("COUNT(DISTINCT j.no_jurnal) AS jml_jurnal, ");
                    sql.append("COUNT(DISTINCT CASE WHEN jps.status_posting IS NULL OR jps.status_posting != 'POSTED' THEN j.no_jurnal END) AS jml_draft, ");
                    sql.append("COUNT(DISTINCT NULLIF(rp.no_rkm_medis,'')) AS jml_pasien, ");
                    sql.append("SUM(dj.").append(sisi).append(") AS total_debet ");
                    sql.append("FROM jurnal j ");
                    sql.append("STRAIGHT_JOIN detailjurnal dj ON j.no_jurnal = dj.no_jurnal AND dj.").append(sisi).append(" > 0 ");
                    sql.append("JOIN ( ");
                    if (kategoriCOA.equals("Akun_Piutang")) {
                        sql.append("SELECT kd_rek, 'Piutang Penjamin' AS jenis FROM akun_piutang GROUP BY kd_rek ");
                    } else if (kategoriCOA.equals("Akun_Bayar")) {
                        sql.append("SELECT kd_rek, 'Tunai / Bank' AS jenis FROM akun_bayar GROUP BY kd_rek ");
                    } else if (kategoriCOA.equals("Pendapatan_Rajal")) {
                        sql.append("SELECT coa AS kd_rek, 'Pendapatan Rajal' AS jenis FROM coa_poli GROUP BY coa ");
                    } else if (kategoriCOA.equals("Pendapatan_Ranap")) {
                        sql.append("SELECT coa AS kd_rek, 'Pendapatan Ranap' AS jenis FROM coa_bangsal GROUP BY coa ");
                    } else {
                        sql.append("    SELECT kd_rek, CASE WHEN MAX(prio) = 2 THEN 'Piutang Penjamin' ELSE 'Tunai / Bank' END AS jenis ");
                        sql.append("    FROM ( ");
                        sql.append("        SELECT kd_rek, 1 AS prio FROM akun_bayar ");
                        sql.append("        UNION ALL ");
                        sql.append("        SELECT kd_rek, 2 AS prio FROM akun_piutang ");
                        sql.append("    ) u GROUP BY kd_rek ");
                    }
                    sql.append(") m ON m.kd_rek = dj.kd_rek ");
                    sql.append("LEFT JOIN rekening r ON r.kd_rek = dj.kd_rek ");
                    sql.append("LEFT JOIN reg_periksa rp ON j.no_bukti = rp.no_rawat ");
                    sql.append("LEFT JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis ");
                    sql.append("LEFT JOIN jurnal_posting_status jps ON j.no_jurnal = jps.no_jurnal ");
                    sql.append(baseFilter.toString());
                    sql.append("GROUP BY dj.kd_rek, r.nm_rek, m.jenis ");
                    sql.append("ORDER BY (m.jenis = 'Piutang Penjamin') DESC, nm_rek");

                    ArrayNode arr = mapper.createArrayNode();
                    try (java.sql.PreparedStatement ps = getKoneksi().prepareStatement(sql.toString())) {
                        for (int i = 0; i < filterParams.size(); i++) {
                            ps.setObject(i + 1, filterParams.get(i));
                        }
                        try (java.sql.ResultSet rs = ps.executeQuery()) {
                            while(rs.next()) {
                                ObjectNode obj = mapper.createObjectNode();
                                obj.put("kd_rek", rs.getString("kd_rek"));
                                obj.put("nm_rek", rs.getString("nm_rek"));
                                obj.put("nama_bayar", "");
                                obj.put("jenis", rs.getString("jenis"));
                                obj.put("jml_jurnal", rs.getInt("jml_jurnal"));
                                obj.put("jml_draft", rs.getInt("jml_draft"));
                                obj.put("jml_pasien", rs.getInt("jml_pasien"));
                                obj.put("total_debet", rs.getDouble("total_debet"));
                                arr.add(obj);
                            }
                        }
                    }
                    
                    String jsonResult = arr.toString();
                    String safeJsonResult = "\"[]\"";
                    try {
                        safeJsonResult = mapper.writeValueAsString(jsonResult);
                        safeJsonResult = safeJsonResult.replace("\\u2028", "\\\\u2028").replace("\\u2029", "\\\\u2029");
                    } catch (Exception ex) {}
                    final String fj = safeJsonResult;
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("terimaRekapAkunBayar(" + fj + ");");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("terimaRekapAkunBayar('[]'); alert('Error load rekap: " + escapeJS(e.getMessage()) + "');");
                        }
                    });
                }
            });
        }

        public void validasiJurnal(String noJurnalsJson) {
            if (!isPostingRunning.compareAndSet(false, true)) {
                Platform.runLater(() -> {
                    if (engine != null) {
                        engine.executeScript("alert('Proses validasi/posting sedang berjalan, mohon tunggu sebentar...'); if (typeof isPosting !== 'undefined') isPosting = false;");
                    }
                });
                return;
            }

            dbExecutor.submit(() -> {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode arr = mapper.readTree(noJurnalsJson);
                    if (arr == null || arr.size() == 0) {
                        Platform.runLater(() -> {
                            if (engine != null) engine.executeScript("alert('Tidak ada jurnal dipilih'); if (typeof isPosting !== 'undefined') isPosting = false;");
                        });
                        return;
                    }

                    String userValidasi = akses.getkode();
                    if (userValidasi == null || userValidasi.trim().isEmpty()) {
                        Platform.runLater(() -> {
                            if (engine != null) engine.executeScript("alert('User tidak teridentifikasi'); if (typeof isPosting !== 'undefined') isPosting = false;");
                        });
                        return;
                    }

                    if (!akses.getposting_jurnal() && !akses.getjurnal_harian()) {
                        Platform.runLater(() -> {
                            if (engine != null) engine.executeScript("alert('Anda tidak memiliki akses untuk memposting jurnal'); if (typeof isPosting !== 'undefined') isPosting = false;");
                        });
                        return;
                    }

                    java.util.LinkedHashSet<String> validJurnals = new java.util.LinkedHashSet<>();
                    for (int i = 0; i < arr.size(); i++) {
                        String jrn = arr.get(i).asText();
                        if (jrn != null && !jrn.trim().isEmpty()) {
                            validJurnals.add(jrn.trim());
                        }
                    }

                    if (validJurnals.isEmpty()) {
                        Platform.runLater(() -> {
                            if (engine != null) engine.executeScript("alert('Tidak ada nomor jurnal valid untuk diposting'); if (typeof isPosting !== 'undefined') isPosting = false;");
                        });
                        return;
                    }

                    java.util.List<String> validJurnalsList = new java.util.ArrayList<>(validJurnals);
                    java.sql.Connection conn = getKoneksi();
                    int batchSize = 500;

                    // 1. Cek jurnal yang sudah berstatus POSTED di jurnal_posting_status
                    java.util.LinkedHashSet<String> sudahPosted = new java.util.LinkedHashSet<>();
                    for (int i = 0; i < validJurnalsList.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, validJurnalsList.size());
                        java.util.List<String> batchList = validJurnalsList.subList(i, end);
                        StringBuilder batchIn = buildBatchPlaceholders(batchList.size());

                        String sqlCek = "SELECT no_jurnal FROM jurnal_posting_status WHERE status_posting = 'POSTED' AND no_jurnal IN (" + batchIn + ")";
                        try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlCek)) {
                            for (int j = 0; j < batchList.size(); j++) {
                                ps.setString(j + 1, batchList.get(j));
                            }
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    sudahPosted.add(rs.getString("no_jurnal"));
                                }
                            }
                        }
                    }

                    // Saring jurnal yang belum pernah berstatus POSTED
                    java.util.List<String> jurnalsToPost = new java.util.ArrayList<>();
                    for (String nj : validJurnalsList) {
                        if (!sudahPosted.contains(nj)) {
                            jurnalsToPost.add(nj);
                        }
                    }

                    if (jurnalsToPost.isEmpty()) {
                        Platform.runLater(() -> {
                            if (engine != null) {
                                engine.executeScript("alert('Seluruh jurnal yang dipilih (" + validJurnalsList.size() + " jurnal) sudah berstatus POSTED.\\nTidak ada yang perlu diposting ulang.'); if (typeof isPosting !== 'undefined') isPosting = false; cariData(false);");
                            }
                        });
                        return;
                    }

                    // 2. Cek eksistensi header di tabel jurnal
                    java.util.LinkedHashSet<String> headerAda = new java.util.LinkedHashSet<>();
                    for (int i = 0; i < jurnalsToPost.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, jurnalsToPost.size());
                        java.util.List<String> batchList = jurnalsToPost.subList(i, end);
                        StringBuilder batchIn = buildBatchPlaceholders(batchList.size());

                        String sqlHeader = "SELECT no_jurnal FROM jurnal WHERE no_jurnal IN (" + batchIn + ")";
                        try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlHeader)) {
                            for (int j = 0; j < batchList.size(); j++) {
                                ps.setString(j + 1, batchList.get(j));
                            }
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    headerAda.add(rs.getString("no_jurnal"));
                                }
                            }
                        }
                    }

                    java.util.List<String> headerHilang = new java.util.ArrayList<>();
                    for (String nj : jurnalsToPost) {
                        if (!headerAda.contains(nj)) {
                            headerHilang.add(nj);
                        }
                    }
                    if (!headerHilang.isEmpty()) {
                        final String errMsg = "Validasi Gagal: Jurnal tidak ditemukan di tabel header jurnal:\\n" + formatListSample(headerHilang, 5);
                        Platform.runLater(() -> {
                            if (engine != null) engine.executeScript("alert('" + escapeJS(errMsg) + "'); if (typeof isPosting !== 'undefined') isPosting = false;");
                        });
                        return;
                    }

                    // 3. Cek detail tersedia di detailjurnal (tidak boleh detail kosong)
                    java.util.LinkedHashSet<String> jurnalDenganDetail = new java.util.LinkedHashSet<>();
                    for (int i = 0; i < jurnalsToPost.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, jurnalsToPost.size());
                        java.util.List<String> batchList = jurnalsToPost.subList(i, end);
                        StringBuilder batchIn = buildBatchPlaceholders(batchList.size());

                        String sqlDetail = "SELECT DISTINCT no_jurnal FROM detailjurnal WHERE no_jurnal IN (" + batchIn + ")";
                        try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlDetail)) {
                            for (int j = 0; j < batchList.size(); j++) {
                                ps.setString(j + 1, batchList.get(j));
                            }
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    jurnalDenganDetail.add(rs.getString("no_jurnal"));
                                }
                            }
                        }
                    }

                    java.util.List<String> detailKosong = new java.util.ArrayList<>();
                    for (String nj : jurnalsToPost) {
                        if (!jurnalDenganDetail.contains(nj)) {
                            detailKosong.add(nj);
                        }
                    }
                    if (!detailKosong.isEmpty()) {
                        final String errMsg = "Validasi Gagal: Jurnal tidak memiliki baris rincian di detailjurnal:\\n" + formatListSample(detailKosong, 5);
                        Platform.runLater(() -> {
                            if (engine != null) engine.executeScript("alert('" + escapeJS(errMsg) + "'); if (typeof isPosting !== 'undefined') isPosting = false;");
                        });
                        return;
                    }

                    // 4. Cek kode rekening terdaftar di tabel rekening
                    java.util.List<String> akunTidakValid = new java.util.ArrayList<>();
                    for (int i = 0; i < jurnalsToPost.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, jurnalsToPost.size());
                        java.util.List<String> batchList = jurnalsToPost.subList(i, end);
                        StringBuilder batchIn = buildBatchPlaceholders(batchList.size());

                        String sqlAkun = "SELECT DISTINCT dj.no_jurnal, dj.kd_rek FROM detailjurnal dj " +
                                         "LEFT JOIN rekening r ON dj.kd_rek = r.kd_rek " +
                                         "WHERE r.kd_rek IS NULL AND dj.no_jurnal IN (" + batchIn + ")";
                        try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlAkun)) {
                            for (int j = 0; j < batchList.size(); j++) {
                                ps.setString(j + 1, batchList.get(j));
                            }
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    akunTidakValid.add(rs.getString("no_jurnal") + " (COA: " + rs.getString("kd_rek") + ")");
                                }
                            }
                        }
                    }
                    if (!akunTidakValid.isEmpty()) {
                        final String errMsg = "Validasi Gagal: Ditemukan kode rekening yang tidak terdaftar di master rekening:\\n" + formatListSample(akunTidakValid, 5);
                        Platform.runLater(() -> {
                            if (engine != null) engine.executeScript("alert('" + escapeJS(errMsg) + "'); if (typeof isPosting !== 'undefined') isPosting = false;");
                        });
                        return;
                    }

                    // 5. Cek nominal debit dan kredit tidak negatif
                    java.util.List<String> nominalNegatif = new java.util.ArrayList<>();
                    for (int i = 0; i < jurnalsToPost.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, jurnalsToPost.size());
                        java.util.List<String> batchList = jurnalsToPost.subList(i, end);
                        StringBuilder batchIn = buildBatchPlaceholders(batchList.size());

                        String sqlNegatif = "SELECT DISTINCT no_jurnal FROM detailjurnal " +
                                            "WHERE (debet < 0 OR kredit < 0) AND no_jurnal IN (" + batchIn + ")";
                        try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlNegatif)) {
                            for (int j = 0; j < batchList.size(); j++) {
                                ps.setString(j + 1, batchList.get(j));
                            }
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    nominalNegatif.add(rs.getString("no_jurnal"));
                                }
                            }
                        }
                    }
                    if (!nominalNegatif.isEmpty()) {
                        final String errMsg = "Validasi Gagal: Ditemukan nominal debit atau kredit negatif pada jurnal:\\n" + formatListSample(nominalNegatif, 5);
                        Platform.runLater(() -> {
                            if (engine != null) engine.executeScript("alert('" + escapeJS(errMsg) + "'); if (typeof isPosting !== 'undefined') isPosting = false;");
                        });
                        return;
                    }

                    // 6. Cek total debit == total kredit berbasis sen
                    java.util.List<String> unbalance = new java.util.ArrayList<>();
                    java.util.List<String> nominalNol = new java.util.ArrayList<>();
                    DecimalFormat df = new DecimalFormat("#,##0.00");
                    for (int i = 0; i < jurnalsToPost.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, jurnalsToPost.size());
                        java.util.List<String> batchList = jurnalsToPost.subList(i, end);
                        StringBuilder batchIn = buildBatchPlaceholders(batchList.size());

                        String sqlBalance = "SELECT no_jurnal, SUM(debet) AS td, SUM(kredit) AS tk FROM detailjurnal " +
                                            "WHERE no_jurnal IN (" + batchIn + ") GROUP BY no_jurnal";
                        try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlBalance)) {
                            for (int j = 0; j < batchList.size(); j++) {
                                ps.setString(j + 1, batchList.get(j));
                            }
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    String nj = rs.getString("no_jurnal");
                                    double td = rs.getDouble("td");
                                    double tk = rs.getDouble("tk");
                                    long dSen = Math.round(td * 100.0);
                                    long kSen = Math.round(tk * 100.0);
                                    if (dSen == 0 && kSen == 0) {
                                        nominalNol.add(nj);
                                    } else if (dSen != kSen) {
                                        double selisih = Math.abs((dSen - kSen) / 100.0);
                                        unbalance.add(nj + " (Debit: " + df.format(td) + ", Kredit: " + df.format(tk) + ", Selisih: " + df.format(selisih) + ")");
                                    }
                                }
                            }
                        }
                    }
                    if (!nominalNol.isEmpty()) {
                        final String errMsg = "Validasi Gagal: Jurnal memiliki total debit dan kredit 0 (tidak ada nilai transaksi):\\n" + formatListSample(nominalNol, 5);
                        Platform.runLater(() -> {
                            if (engine != null) engine.executeScript("alert('" + escapeJS(errMsg) + "'); if (typeof isPosting !== 'undefined') isPosting = false;");
                        });
                        return;
                    }
                    if (!unbalance.isEmpty()) {
                        final String errMsg = "Validasi Gagal: Jurnal tidak balance (Total Debit != Total Kredit):\\n" + formatListSample(unbalance, 5);
                        Platform.runLater(() -> {
                            if (engine != null) engine.executeScript("alert('" + escapeJS(errMsg) + "'); if (typeof isPosting !== 'undefined') isPosting = false;");
                        });
                        return;
                    }

                    // 7. Posting otomatis ke jurnal_posting_status di dalam transaksi database
                    java.util.LinkedHashSet<String> existingDraft = new java.util.LinkedHashSet<>();
                    for (int i = 0; i < jurnalsToPost.size(); i += batchSize) {
                        int end = Math.min(i + batchSize, jurnalsToPost.size());
                        java.util.List<String> batchList = jurnalsToPost.subList(i, end);
                        StringBuilder batchIn = buildBatchPlaceholders(batchList.size());

                        String sqlCekDraft = "SELECT no_jurnal FROM jurnal_posting_status WHERE no_jurnal IN (" + batchIn + ")";
                        try (java.sql.PreparedStatement ps = conn.prepareStatement(sqlCekDraft)) {
                            for (int j = 0; j < batchList.size(); j++) {
                                ps.setString(j + 1, batchList.get(j));
                            }
                            try (java.sql.ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    existingDraft.add(rs.getString("no_jurnal"));
                                }
                            }
                        }
                    }

                    int countInserted = 0;
                    int countUpdated = 0;
                    boolean autoCommitOrig = conn.getAutoCommit();
                    try {
                        conn.setAutoCommit(false);

                        // 7a. Insert baru untuk jurnal yang belum ada di jurnal_posting_status
                        String sqlInsert = "INSERT INTO jurnal_posting_status " +
                                           "(no_jurnal, status_posting, tanggal_validasi, user_validasi, tanggal_posting, user_posting, catatan) " +
                                           "VALUES (?, 'POSTED', NOW(), ?, NOW(), ?, 'Auto-posted via Jurnal Transaksi Harian')";
                        try (java.sql.PreparedStatement psIns = conn.prepareStatement(sqlInsert)) {
                            int localBatch = 0;
                            for (String nj : jurnalsToPost) {
                                if (existingDraft.contains(nj)) continue;
                                psIns.setString(1, nj);
                                psIns.setString(2, userValidasi);
                                psIns.setString(3, userValidasi);
                                psIns.addBatch();
                                localBatch++;
                                if (localBatch % batchSize == 0) {
                                    int[] res = psIns.executeBatch();
                                    for (int r : res) {
                                        if (r == java.sql.Statement.SUCCESS_NO_INFO || r > 0) countInserted++;
                                    }
                                    localBatch = 0;
                                }
                            }
                            if (localBatch > 0) {
                                int[] res = psIns.executeBatch();
                                for (int r : res) {
                                    if (r == java.sql.Statement.SUCCESS_NO_INFO || r > 0) countInserted++;
                                }
                            }
                        }

                        // 7b. Update untuk jurnal yang sebelumnya tercatat DRAFT (bukan POSTED)
                        String sqlUpdate = "UPDATE jurnal_posting_status SET " +
                                           "status_posting = 'POSTED', tanggal_validasi = NOW(), user_validasi = ?, " +
                                           "tanggal_posting = NOW(), user_posting = ?, catatan = 'Auto-posted via Jurnal Transaksi Harian' " +
                                           "WHERE no_jurnal = ? AND status_posting != 'POSTED'";
                        try (java.sql.PreparedStatement psUpd = conn.prepareStatement(sqlUpdate)) {
                            int localBatch = 0;
                            for (String nj : jurnalsToPost) {
                                if (!existingDraft.contains(nj)) continue;
                                psUpd.setString(1, userValidasi);
                                psUpd.setString(2, userValidasi);
                                psUpd.setString(3, nj);
                                psUpd.addBatch();
                                localBatch++;
                                if (localBatch % batchSize == 0) {
                                    int[] res = psUpd.executeBatch();
                                    for (int r : res) {
                                        if (r == java.sql.Statement.SUCCESS_NO_INFO || r > 0) countUpdated++;
                                    }
                                    localBatch = 0;
                                }
                            }
                            if (localBatch > 0) {
                                int[] res = psUpd.executeBatch();
                                for (int r : res) {
                                    if (r == java.sql.Statement.SUCCESS_NO_INFO || r > 0) countUpdated++;
                                }
                            }
                        }

                        conn.commit();
                    } catch (Exception ex) {
                        try { conn.rollback(); } catch (Exception rbEx) {}
                        if (ex instanceof java.sql.SQLIntegrityConstraintViolationException ||
                            (ex.getMessage() != null && ex.getMessage().toLowerCase().contains("duplicate"))) {
                            throw new Exception("Sebagian jurnal baru saja diposting oleh pengguna lain. Silakan muat ulang data transaksi.");
                        }
                        throw ex;
                    } finally {
                        try { conn.setAutoCommit(autoCommitOrig); } catch (Exception acEx) {}
                    }

                    final int totalBerhasil = countInserted + countUpdated;
                    final int totalDilewati = sudahPosted.size();
                    Platform.runLater(() -> {
                        if (engine != null) {
                            String msg = "Berhasil memvalidasi & memposting " + totalBerhasil + " jurnal ke status POSTED.";
                            if (totalDilewati > 0) {
                                msg += "\\n(Dilewati " + totalDilewati + " jurnal yang sudah berstatus POSTED sebelumnya).";
                            }
                            engine.executeScript("alert('" + escapeJS(msg) + "'); if (typeof isPosting !== 'undefined') isPosting = false; if (typeof cariData === 'function') cariData(false);");
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("alert('Gagal memposting: " + escapeJS(e.getMessage()) + "'); if (typeof isPosting !== 'undefined') isPosting = false;");
                        }
                    });
                } finally {
                    isPostingRunning.set(false);
                }
            });
        }

        private StringBuilder buildBatchPlaceholders(int count) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < count; i++) {
                sb.append("?");
                if (i < count - 1) sb.append(",");
            }
            return sb;
        }

        private String formatListSample(java.util.List<String> list, int max) {
            if (list == null || list.isEmpty()) return "";
            StringBuilder sb = new StringBuilder();
            int limit = Math.min(list.size(), max);
            for (int i = 0; i < limit; i++) {
                sb.append("- ").append(list.get(i));
                if (i < limit - 1) sb.append("\\n");
            }
            if (list.size() > max) {
                sb.append("\\n...dan ").append(list.size() - max).append(" jurnal lainnya");
            }
            return sb.toString();
        }

        public void exportExcel(String title, String htmlTable) {
            dbExecutor.submit(() -> {
                try {
                    java.io.File file = java.io.File.createTempFile(title + "_", ".xls");
                    try (java.io.PrintWriter out = new java.io.PrintWriter(new java.io.OutputStreamWriter(new java.io.FileOutputStream(file), "UTF-8"))) {
                        out.write("<html><head><meta charset=\"UTF-8\"></head><body>");
                        out.write(htmlTable);
                        out.write("</body></html>");
                    }
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(file);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        if(engine != null) engine.executeScript("alert('Gagal export: " + escapeJS(e.getMessage()) + "');");
                    });
                }
            });
        }
    }

    // ============================================================
    // GENERATED CODE
    // ============================================================
    private void initComponents() {
        internalFrame1 = new widget.InternalFrame();
        jPanelWeb = new javax.swing.JPanel();
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setSize(1300, 750);
        setLocationRelativeTo(null);
        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                "::[ Jurnal Transaksi Harian ]::",
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

    private void bangunFilter(JsonNode json, StringBuilder filterSql, java.util.List<Object> params) {
        String tgl1 = json.has("tgl1") ? json.get("tgl1").asText() : "";
        String tgl2 = json.has("tgl2") ? json.get("tgl2").asText() : "";
        String statusPasien = json.has("statusPasien") ? json.get("statusPasien").asText() : "Semua";
        String keyword = json.has("keyword") ? json.get("keyword").asText() : "";
        String statusPosting = json.has("statusPosting") ? json.get("statusPosting").asText() : "Semua";
        String statusPulang = json.has("statusPulang") ? json.get("statusPulang").asText() : "Semua";
        
        String coa = json.has("coa") ? json.get("coa").asText().trim() : "";
        boolean coaExact = json.has("coaExact") && json.get("coaExact").asBoolean();
        String kdRek = json.has("kdRek") ? json.get("kdRek").asText().trim() : "";

        filterSql.append(" WHERE j.tgl_jurnal BETWEEN ? AND ? ");
        params.add(tgl1);
        params.add(tgl2);
        
        if (!statusPasien.equals("Semua")) {
            if (statusPasien.equals("Rajal") || statusPasien.equals("Ralan")) {
                filterSql.append("AND rp.status_lanjut = 'Ralan' ");
            } else if (statusPasien.equals("Ranap")) {
                filterSql.append("AND rp.status_lanjut = 'Ranap' ");
            }
        }
        
        if (!statusPulang.equals("Semua") && !statusPulang.isEmpty()) {
            if (statusPulang.equals("Pulang")) {
                filterSql.append("AND rp.status_lanjut = 'Ranap' ");
                filterSql.append("AND EXISTS (SELECT 1 FROM kamar_inap ki WHERE ki.no_rawat = j.no_bukti AND ki.stts_pulang NOT IN ('-', 'Pindah Kamar') AND ki.tgl_keluar BETWEEN ? AND ?) ");
                params.add(tgl1);
                params.add(tgl2);
            } else if (statusPulang.equals("Belum")) {
                filterSql.append("AND rp.status_lanjut = 'Ranap' ");
                filterSql.append("AND NOT EXISTS (SELECT 1 FROM kamar_inap ki WHERE ki.no_rawat = j.no_bukti AND ki.stts_pulang NOT IN ('-', 'Pindah Kamar') AND ki.tgl_keluar IS NOT NULL AND ki.tgl_keluar != '0000-00-00') ");
            }
        }
        if (!keyword.isEmpty()) {
            filterSql.append("AND (p.nm_pasien LIKE ? OR rp.no_rkm_medis LIKE ? OR j.no_bukti LIKE ?) ");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }
        if (!statusPosting.equals("Semua")) {
            if (statusPosting.equals("Posted")) {
                filterSql.append("AND jps.status_posting = 'POSTED' ");
            } else if (statusPosting.equals("Draft")) {
                filterSql.append("AND (jps.status_posting IS NULL OR jps.status_posting != 'POSTED') ");
            }
        }
        
        if (!coa.isEmpty()) {
            if (coaExact) {
                filterSql.append("AND EXISTS (SELECT 1 FROM detailjurnal dj_ex WHERE dj_ex.no_jurnal = j.no_jurnal AND dj_ex.kd_rek = ?) ");
                params.add(coa);
            } else {
                filterSql.append("AND EXISTS (SELECT 1 FROM detailjurnal dj_ex LEFT JOIN rekening rx_ex ON rx_ex.kd_rek = dj_ex.kd_rek WHERE dj_ex.no_jurnal = j.no_jurnal AND (dj_ex.kd_rek LIKE ? ESCAPE '\\' OR rx_ex.nm_rek LIKE ? ESCAPE '\\')) ");
                String escCoa = "%" + coa.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
                params.add(escCoa);
                params.add(escCoa);
            }
        } else if (!kdRek.isEmpty()) {
            filterSql.append("AND EXISTS (SELECT 1 FROM detailjurnal dj_ex WHERE dj_ex.no_jurnal = j.no_jurnal AND dj_ex.kd_rek = ?) ");
            params.add(kdRek);
        }
    }
}

