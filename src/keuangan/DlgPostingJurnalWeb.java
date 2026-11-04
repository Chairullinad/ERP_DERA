package keuangan;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.SwingUtilities;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

public class DlgPostingJurnalWeb extends javax.swing.JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final DlgCariRekeningTahun rekening = new DlgCariRekeningTahun(null, false);
    private DlgKwitansi kwitansi = null;
    private JavaBridge javaBridge = null;
    private WebView webView;
    private WebEngine engine;
    private JFXPanel fxPanel;
    private boolean isWebViewInitialized = false;

    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Jurnal-DB-Worker");
        t.setDaemon(true);
        return t;
    });
    private final Object dbLock = new Object();
    private final java.util.concurrent.atomic.AtomicBoolean isProcessing = new java.util.concurrent.atomic.AtomicBoolean(false);

    private boolean isMaximized = false;
    private java.awt.Rectangle normalBounds = null;

    public void toggleMaximize() {
        SwingUtilities.invokeLater(() -> {
            try {
                if (!isMaximized) {
                    normalBounds = getBounds();
                    java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
                    java.awt.Rectangle maxBounds = ge.getMaximumWindowBounds();
                    setBounds(maxBounds);
                    isMaximized = true;
                } else {
                    if (normalBounds != null) {
                        setBounds(normalBounds);
                    } else {
                        setSize(1200, 720);
                        setLocationRelativeTo(null);
                    }
                    isMaximized = false;
                }
            } catch (Exception e) {
                System.err.println("toggleMaximize error: " + e.getMessage());
            }
        });
    }

    public void minimize() {
        SwingUtilities.invokeLater(() -> {
            try {
                java.awt.Window owner = getOwner();
                if (owner instanceof java.awt.Frame) {
                    ((java.awt.Frame) owner).setState(java.awt.Frame.ICONIFIED);
                } else {
                    setVisible(false);
                }
            } catch (Exception e) {
                System.err.println("minimize error: " + e.getMessage());
            }
        });
    }

    public DlgPostingJurnalWeb(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        rekening.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (rekening.getTabel().getSelectedRow() != -1) {
                    String kd = rekening.getTabel().getValueAt(rekening.getTabel().getSelectedRow(), 1).toString();
                    String nm = rekening.getTabel().getValueAt(rekening.getTabel().getSelectedRow(), 2).toString();
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("setAkunDariDialog('" + escapeJS(kd) + "', '" + escapeJS(nm) + "');");
                        }
                    });
                }
            }

            @Override
            public void windowIconified(WindowEvent e) {
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
            }

            @Override
            public void windowActivated(WindowEvent e) {
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

        initializeWebView();
        dbExecutor.submit(this::createTableJurnalUmum);
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
                engine.setConfirmHandler(param -> {
                    int res = javax.swing.JOptionPane.showConfirmDialog(DlgPostingJurnalWeb.this, param, "Konfirmasi", javax.swing.JOptionPane.YES_NO_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE);
                    return res == javax.swing.JOptionPane.YES_OPTION;
                });
                engine.setOnAlert(event -> {
                    String msg = event.getData();
                    SwingUtilities.invokeLater(() -> {
                        javax.swing.JOptionPane.showMessageDialog(DlgPostingJurnalWeb.this, msg, "Notifikasi", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    });
                });

                String html = getHTMLContent();
                try {
                    java.io.File tempHtml = java.io.File.createTempFile("posting_jurnal_", ".html");
                    tempHtml.deleteOnExit();
                    try ( java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(new java.io.FileOutputStream(tempHtml), "UTF-8")) {
                        writer.write(html);
                    }
                    engine.load(tempHtml.toURI().toString());
                } catch (Exception ex) {
                    System.err.println("Error creating temp HTML file: " + ex.getMessage());
                    engine.loadContent(html);
                }

                engine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        try {
                            JSObject window = (JSObject) engine.executeScript("window");
                            // FIX: reuse instance agar lastDraftData/lastValidData tidak hilang
                            if (javaBridge == null) {
                                javaBridge = new JavaBridge();
                            }
                            window.setMember("javaApp", javaBridge);
                            try {
                                engine.executeScript("if (typeof setTanggalHariIni === 'function') setTanggalHariIni();");
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                            try {
                                engine.executeScript("if (typeof generateNoJurnal === 'function') generateNoJurnal();");
                            } catch (Throwable t) {
                                t.printStackTrace();
                            }
                        } catch (Throwable e) {
                            System.err.println("FATAL Error initializing WebView JavaBridge: " + e.getMessage());
                            e.printStackTrace();
                        }
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
            InputStream is = getClass().getResourceAsStream("/resources/posting_jurnal.html");
            if (is == null) {
                is = getClass().getClassLoader().getResourceAsStream("resources/posting_jurnal.html");
            }
            if (is == null) {
                return "<html><body><h1>File posting_jurnal.html tidak ditemukan</h1></body></html>";
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
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
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
        } catch (Exception e) {
        }
        return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
    }

    private void prosesPilihKwitansi(String noRekap) {
        dbExecutor.submit(() -> {
            try {
                String sql = "SELECT k.no_rekap, k.tgl_surat, k.tgl_transaksi, k.nama_tujuan, k.keterangan, "
                        + "k.biaya_satuan, k.jumlah, k.total_biaya, k.nip_petugas, COALESCE(p.nama, k.nip_petugas) AS nama_petugas "
                        + "FROM kwitansi_kegiatan k "
                        + "LEFT JOIN petugas p ON k.nip_petugas = p.nip "
                        + "WHERE k.no_rekap = ?";
                JSONObject json = new JSONObject();
                try ( PreparedStatement ps = koneksi.prepareStatement(sql)) {
                    ps.setString(1, noRekap);
                    try ( ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            json.put("no_rekap", rs.getString("no_rekap") != null ? rs.getString("no_rekap") : "");
                            json.put("tgl_surat", rs.getString("tgl_surat") != null ? rs.getString("tgl_surat") : "");
                            json.put("tgl_transaksi", rs.getString("tgl_transaksi") != null ? rs.getString("tgl_transaksi") : "");
                            json.put("nama_tujuan", rs.getString("nama_tujuan") != null ? rs.getString("nama_tujuan") : "");
                            json.put("keterangan", rs.getString("keterangan") != null ? rs.getString("keterangan") : "");
                            json.put("biaya_satuan", rs.getDouble("biaya_satuan"));
                            json.put("jumlah", rs.getDouble("jumlah"));
                            json.put("total_biaya", rs.getDouble("total_biaya"));
                            json.put("nip_petugas", rs.getString("nip_petugas") != null ? rs.getString("nip_petugas") : "");
                            json.put("nama_petugas", rs.getString("nama_petugas") != null ? rs.getString("nama_petugas") : "");
                        }
                    }
                }

                if (!json.has("no_rekap")) {
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("showToast('Data kwitansi " + escapeJS(noRekap) + " tidak ditemukan', 'error');");
                        }
                    });
                    return;
                }

                boolean sudahDijurnal = false;
                String noJurnalAda = "";
                try ( PreparedStatement psJ = koneksi.prepareStatement("SELECT no_jurnal FROM jurnal WHERE no_bukti = ? LIMIT 1")) {
                    psJ.setString(1, noRekap);
                    try ( ResultSet rsJ = psJ.executeQuery()) {
                        if (rsJ.next()) {
                            sudahDijurnal = true;
                            noJurnalAda = rsJ.getString("no_jurnal");
                        }
                    }
                }
                if (!sudahDijurnal) {
                    try ( PreparedStatement psJU = koneksi.prepareStatement("SELECT no_jurnal FROM jurnal_umum WHERE no_bukti = ? LIMIT 1")) {
                        psJU.setString(1, noRekap);
                        try ( ResultSet rsJU = psJU.executeQuery()) {
                            if (rsJU.next()) {
                                sudahDijurnal = true;
                                noJurnalAda = rsJU.getString("no_jurnal");
                            }
                        }
                    }
                }

                json.put("sudah_dijurnal", sudahDijurnal);
                json.put("no_jurnal_ada", noJurnalAda != null ? noJurnalAda : "");

                String rawJson = json.toString().replace("\u2028", "\\u2028").replace("\u2029", "\\u2029");
                Platform.runLater(() -> {
                    if (engine != null) {
                        engine.executeScript("terimaPilihKwitansi(" + rawJson + ");");
                    }
                });
            } catch (Exception ex) {
                System.err.println("prosesPilihKwitansi error: " + ex.getMessage());
                final String errMsg = ex.getMessage();
                Platform.runLater(() -> {
                    if (engine != null) {
                        engine.executeScript("showAppError('Gagal Membaca Data Kwitansi', '" + escapeJS(errMsg) + "');");
                    }
                });
            }
        });
    }

    @Override
    public void dispose() {
        if (kwitansi != null) {
            kwitansi.dispose();
        }
        super.dispose();
    }

    private void createTableJurnalUmum() {
        // DDL runtime dinonaktifkan sesuai batasan implementasi.
        // Tabel jurnal, detailjurnal, dan jurnal_posting_status dikelola langsung di database.
    }

    private StringBuilder buildBatchPlaceholders(int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append("?");
            if (i < count - 1) {
                sb.append(",");
            }
        }
        return sb;
    }

    private String formatListSample(List<String> list, int max) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int limit = Math.min(list.size(), max);
        for (int i = 0; i < limit; i++) {
            sb.append("- ").append(list.get(i));
            if (i < limit - 1) {
                sb.append("\n");
            }
        }
        if (list.size() > max) {
            sb.append("\n... dan ").append(list.size() - max).append(" lainnya");
        }
        return sb.toString();
    }

    // ============================================================
    // JAVA BRIDGE - SEMUA ASYNC
    // ============================================================
    public class JavaBridge {

        public String getTanggalHariIni() {
            return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        }

        public void autoNomor(String tgl) {
            dbExecutor.submit(() -> {
                String no = "";
                try {
                    String tglSql = formatTglSql(tgl);
                    String tglClean = tglSql.replaceAll("-", "");
                    String prefix = "JM" + tglClean;
                    no = Valid.autoNomer3(
                            "SELECT IFNULL(MAX(CONVERT(RIGHT(no_jurnal,6),SIGNED)),0) FROM ("
                            + "SELECT no_jurnal FROM jurnal WHERE tgl_jurnal='" + tglSql + "' AND no_jurnal LIKE '" + prefix + "%' "
                            + "UNION "
                            + "SELECT no_jurnal FROM jurnal_umum WHERE tgl='" + tglSql + "' AND no_jurnal LIKE '" + prefix + "%' "
                            + "UNION "
                            + "SELECT no_jurnal FROM detail_posting_jurnal WHERE no_jurnal LIKE '" + prefix + "%'"
                            + ") AS t",
                            prefix, 6
                    );
                } catch (Exception e) {
                }
                final String result = no;
                Platform.runLater(() -> {
                    if (engine != null) {
                        engine.executeScript("setNoJurnal('" + escapeJS(result) + "')");
                    }
                });
            });
        }

        public void cariRekening(String kdRek, String rowKey) {
            dbExecutor.submit(() -> {
                String nm = "";
                try {
                    String sql = "SELECT nm_rek FROM rekening WHERE kd_rek = ?";
                    PreparedStatement ps = koneksi.prepareStatement(sql);
                    ps.setString(1, kdRek);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        nm = rs.getString("nm_rek");
                    }
                    rs.close();
                    ps.close();
                } catch (Exception e) {
                }
                final String nama = nm;
                final String key = rowKey;
                Platform.runLater(() -> {
                    if (engine != null) {
                        engine.executeScript("setNamaRekening('" + escapeJS(key) + "', '" + escapeJS(kdRek) + "', '" + escapeJS(nama) + "')");
                    }
                });
            });
        }

        public void bukaCariRekening() {
            SwingUtilities.invokeLater(() -> {
                rekening.tampil();
                rekening.isCek();
                rekening.emptTeks();
                rekening.setSize(1050, 680);
                rekening.setLocationRelativeTo(DlgPostingJurnalWeb.this);
                rekening.setVisible(true);
            });
        }

        public void getJurnalForCopy(String noJurnal) {
            dbExecutor.submit(() -> {
                JSONObject result = new JSONObject();
                try {
                    boolean found = false;
                    String sqlJ = "SELECT no_bukti, tgl_jurnal, jam_jurnal, jenis, keterangan FROM jurnal WHERE no_jurnal = ?";
                    try ( PreparedStatement ps = koneksi.prepareStatement(sqlJ)) {
                        ps.setString(1, noJurnal);
                        try ( ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                found = true;
                                result.put("noJurnal", noJurnal);
                                result.put("noBukti", rs.getString("no_bukti") != null ? rs.getString("no_bukti") : "");
                                result.put("tanggal", rs.getString("tgl_jurnal") != null ? rs.getString("tgl_jurnal") : "");
                                result.put("jenis", rs.getString("jenis") != null ? rs.getString("jenis") : "U");
                                result.put("keterangan", rs.getString("keterangan") != null ? rs.getString("keterangan") : "");
                            }
                        }
                    }

                    if (found) {
                        String sqlDJ = "SELECT dj.kd_rek, r.nm_rek, dj.debet, dj.kredit "
                                + "FROM detailjurnal dj LEFT JOIN rekening r ON dj.kd_rek = r.kd_rek "
                                + "WHERE dj.no_jurnal = ? ORDER BY dj.debet DESC, dj.kd_rek ASC";
                        try ( PreparedStatement ps = koneksi.prepareStatement(sqlDJ)) {
                            ps.setString(1, noJurnal);
                            try ( ResultSet rs = ps.executeQuery()) {
                                JSONArray details = new JSONArray();
                                while (rs.next()) {
                                    JSONObject d = new JSONObject();
                                    d.put("kdRek", rs.getString("kd_rek") != null ? rs.getString("kd_rek") : "");
                                    d.put("nmRek", rs.getString("nm_rek") != null ? rs.getString("nm_rek") : "");
                                    d.put("debet", rs.getDouble("debet"));
                                    d.put("kredit", rs.getDouble("kredit"));
                                    d.put("keterangan", result.optString("keterangan", ""));
                                    details.put(d);
                                }
                                result.put("details", details);
                            }
                        }
                    } else {
                        // Fallback ke jurnal_umum & detail_posting_jurnal jika jurnal legacy
                        String sqlJU = "SELECT no_bukti, tgl as tgl_jurnal, '00:00:00' as jam_jurnal, 'U' as jenis, keterangan FROM jurnal_umum WHERE no_jurnal=?";
                        try ( PreparedStatement ps = koneksi.prepareStatement(sqlJU)) {
                            ps.setString(1, noJurnal);
                            try ( ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    found = true;
                                    result.put("noJurnal", noJurnal);
                                    result.put("noBukti", rs.getString("no_bukti") != null ? rs.getString("no_bukti") : "");
                                    result.put("tanggal", rs.getString("tgl_jurnal") != null ? rs.getString("tgl_jurnal") : "");
                                    result.put("jenis", rs.getString("jenis") != null ? rs.getString("jenis") : "U");
                                    result.put("keterangan", rs.getString("keterangan") != null ? rs.getString("keterangan") : "");
                                }
                            }
                        }
                        if (found) {
                            String sqlDP = "SELECT dp.kd_rek, r.nm_rek, dp.debet, dp.kredit, dp.keterangan "
                                    + "FROM detail_posting_jurnal dp LEFT JOIN rekening r ON dp.kd_rek = r.kd_rek "
                                    + "WHERE dp.no_jurnal=? ORDER BY dp.debet DESC, dp.kd_rek ASC";
                            try ( PreparedStatement ps = koneksi.prepareStatement(sqlDP)) {
                                ps.setString(1, noJurnal);
                                try ( ResultSet rs = ps.executeQuery()) {
                                    JSONArray details = new JSONArray();
                                    while (rs.next()) {
                                        JSONObject d = new JSONObject();
                                        d.put("kdRek", rs.getString("kd_rek") != null ? rs.getString("kd_rek") : "");
                                        d.put("nmRek", rs.getString("nm_rek") != null ? rs.getString("nm_rek") : "");
                                        d.put("debet", rs.getDouble("debet"));
                                        d.put("kredit", rs.getDouble("kredit"));
                                        d.put("keterangan", rs.getString("keterangan") != null ? rs.getString("keterangan") : "");
                                        details.put(d);
                                    }
                                    result.put("details", details);
                                }
                            }
                        }
                    }

                    if (!found) {
                        Platform.runLater(() -> {
                            if (engine != null) {
                                engine.executeScript("showToast('Jurnal " + escapeJS(noJurnal) + " tidak ditemukan', 'error');");
                            }
                        });
                        return;
                    }

                    final String json = result.toString();
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("terimaJurnalCopy('" + escapeJS(json) + "');");
                        }
                    });
                } catch (Exception e) {
                    System.err.println("getJurnalForCopy error: " + e.getMessage());
                }
            });
        }

        public void validasiJurnal(String noJurnal) {
            dbExecutor.submit(() -> {
                if (!isProcessing.compareAndSet(false, true)) {
                    return;
                }
                boolean sukses = false;
                String pesan = "";
                try {
                    if (!akses.getposting_jurnal()) {
                        throw new Exception("Anda tidak memiliki hak akses untuk memvalidasi jurnal.");
                    }

                    // 1. Cek apakah sudah berstatus POSTED di jurnal_posting_status
                    String sqlCekStatus = "SELECT status_posting FROM jurnal_posting_status WHERE no_jurnal = ? LIMIT 1";
                    try ( PreparedStatement psCek = koneksi.prepareStatement(sqlCekStatus)) {
                        psCek.setString(1, noJurnal);
                        try ( ResultSet rsCek = psCek.executeQuery()) {
                            if (rsCek.next() && "POSTED".equalsIgnoreCase(rsCek.getString("status_posting"))) {
                                throw new Exception("Jurnal '" + noJurnal + "' sudah berstatus POSTED dan tidak dapat divalidasi ulang.");
                            }
                        }
                    }

                    // 2. Cek apakah ada di jurnal utama atau jurnal_umum
                    boolean adaDiJurnal = false;
                    String noBukti = noJurnal;
                    String tglJurnal = "";
                    String keterangan = "";
                    String sqlJ = "SELECT no_bukti, tgl_jurnal, keterangan FROM jurnal WHERE no_jurnal = ?";
                    try ( PreparedStatement ps = koneksi.prepareStatement(sqlJ)) {
                        ps.setString(1, noJurnal);
                        try ( ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                adaDiJurnal = true;
                                noBukti = rs.getString("no_bukti");
                                tglJurnal = rs.getString("tgl_jurnal");
                                keterangan = rs.getString("keterangan");
                            }
                        }
                    }

                    if (!adaDiJurnal) {
                        String sqlJU = "SELECT no_bukti, tgl, keterangan FROM jurnal_umum WHERE no_jurnal = ?";
                        try ( PreparedStatement ps = koneksi.prepareStatement(sqlJU)) {
                            ps.setString(1, noJurnal);
                            try ( ResultSet rs = ps.executeQuery()) {
                                if (rs.next()) {
                                    noBukti = rs.getString("no_bukti");
                                    tglJurnal = rs.getString("tgl");
                                    keterangan = rs.getString("keterangan");
                                } else {
                                    throw new Exception("Jurnal '" + noJurnal + "' tidak ditemukan.");
                                }
                            }
                        }
                    }

                    // 3. Cek rincian dan hitung balance
                    List<String> listKdRek = new ArrayList<>();
                    double td = 0;
                    double tk = 0;
                    String sqlCekDet = adaDiJurnal
                            ? "SELECT kd_rek, debet, kredit FROM detailjurnal WHERE no_jurnal = ?"
                            : "SELECT kd_rek, debet, kredit FROM detail_posting_jurnal WHERE no_jurnal = ?";
                    try ( PreparedStatement ps = koneksi.prepareStatement(sqlCekDet)) {
                        ps.setString(1, noJurnal);
                        try ( ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String kd = rs.getString("kd_rek");
                                double deb = rs.getDouble("debet");
                                double kre = rs.getDouble("kredit");
                                if (deb < 0 || kre < 0) {
                                    throw new Exception("Ditemukan nominal negatif pada rekening " + kd);
                                }
                                listKdRek.add(kd);
                                td += deb;
                                tk += kre;
                            }
                        }
                    }

                    if (listKdRek.isEmpty()) {
                        throw new Exception("Jurnal '" + noJurnal + "' tidak memiliki baris rincian rekening.");
                    }

                    long dSen = Math.round(td * 100.0);
                    long kSen = Math.round(tk * 100.0);
                    if (dSen == 0 && kSen == 0) {
                        throw new Exception("Total nilai transaksi tidak boleh nol.");
                    }
                    if (dSen != kSen) {
                        DecimalFormat df = new DecimalFormat("#,##0.00");
                        double selisih = Math.abs((dSen - kSen) / 100.0);
                        throw new Exception("Jurnal tidak balance! Total Debet=" + df.format(td) + ", Total Kredit=" + df.format(tk) + ", Selisih=" + df.format(selisih));
                    }

                    // 4. Validasi keberadaan akun di rekening
                    StringBuilder sb = buildBatchPlaceholders(listKdRek.size());
                    LinkedHashSet<String> akunAda = new LinkedHashSet<>();
                    try ( PreparedStatement psRek = koneksi.prepareStatement("SELECT kd_rek FROM rekening WHERE kd_rek IN (" + sb + ")")) {
                        for (int i = 0; i < listKdRek.size(); i++) {
                            psRek.setString(i + 1, listKdRek.get(i));
                        }
                        try ( ResultSet rsRek = psRek.executeQuery()) {
                            while (rsRek.next()) {
                                akunAda.add(rsRek.getString("kd_rek"));
                            }
                        }
                    }
                    List<String> akunHilang = new ArrayList<>();
                    for (String k : listKdRek) {
                        if (!akunAda.contains(k)) {
                            akunHilang.add(k);
                        }
                    }
                    if (!akunHilang.isEmpty()) {
                        throw new Exception("Kode rekening tidak terdaftar di master rekening:\n" + formatListSample(akunHilang, 5));
                    }

                    // 5. Simpan transaksi posting
                    koneksi.setAutoCommit(false);

                    // Migrasi ke jurnal & detailjurnal jika data baru ada di jurnal_umum
                    if (!adaDiJurnal) {
                        String sqlInsJ = "INSERT INTO jurnal (no_jurnal, no_bukti, tgl_jurnal, jam_jurnal, jenis, keterangan) VALUES (?, ?, ?, '00:00:00', 'U', ?)";
                        try ( PreparedStatement psInsJ = koneksi.prepareStatement(sqlInsJ)) {
                            psInsJ.setString(1, noJurnal);
                            psInsJ.setString(2, noBukti);
                            psInsJ.setString(3, tglJurnal);
                            psInsJ.setString(4, keterangan);
                            psInsJ.executeUpdate();
                        }
                        String sqlCopyDJ = "INSERT INTO detailjurnal (no_jurnal, kd_rek, debet, kredit) "
                                + "SELECT no_jurnal, kd_rek, debet, kredit FROM detail_posting_jurnal WHERE no_jurnal = ?";
                        try ( PreparedStatement psCopyDJ = koneksi.prepareStatement(sqlCopyDJ)) {
                            psCopyDJ.setString(1, noJurnal);
                            psCopyDJ.executeUpdate();
                        }
                    }

                    // Insert atau Update ke jurnal_posting_status
                    boolean adaJps = false;
                    try ( PreparedStatement psCekJps = koneksi.prepareStatement("SELECT no_jurnal FROM jurnal_posting_status WHERE no_jurnal = ?")) {
                        psCekJps.setString(1, noJurnal);
                        try ( ResultSet rsJps = psCekJps.executeQuery()) {
                            if (rsJps.next()) {
                                adaJps = true;
                            }
                        }
                    }
                    if (adaJps) {
                        String sqlUpdJps = "UPDATE jurnal_posting_status SET status_posting = 'POSTED', tanggal_validasi = NOW(), user_validasi = ?, tanggal_posting = NOW(), user_posting = ?, catatan = 'Posted via DlgPostingJurnalWeb' WHERE no_jurnal = ? AND status_posting != 'POSTED'";
                        try ( PreparedStatement psUpd = koneksi.prepareStatement(sqlUpdJps)) {
                            psUpd.setString(1, akses.getkode());
                            psUpd.setString(2, akses.getkode());
                            psUpd.setString(3, noJurnal);
                            psUpd.executeUpdate();
                        }
                    } else {
                        String sqlInsJps = "INSERT INTO jurnal_posting_status (no_jurnal, status_posting, tanggal_validasi, user_validasi, tanggal_posting, user_posting, catatan) VALUES (?, 'POSTED', NOW(), ?, NOW(), ?, 'Posted via DlgPostingJurnalWeb')";
                        try ( PreparedStatement psIns = koneksi.prepareStatement(sqlInsJps)) {
                            psIns.setString(1, noJurnal);
                            psIns.setString(2, akses.getkode());
                            psIns.setString(3, akses.getkode());
                            psIns.executeUpdate();
                        }
                    }

                    // Update jurnal_umum
                    try ( PreparedStatement psJU = koneksi.prepareStatement("UPDATE jurnal_umum SET status_validasi = 'Valid', created_by = ? WHERE no_jurnal = ?")) {
                        psJU.setString(1, akses.getkode());
                        psJU.setString(2, noJurnal);
                        psJU.executeUpdate();
                    }

                    koneksi.commit();
                    koneksi.setAutoCommit(true);
                    sukses = true;
                    pesan = "Jurnal " + noJurnal + " berhasil divalidasi dan berstatus POSTED!";
                } catch (Exception e) {
                    try {
                        koneksi.rollback();
                    } catch (Exception ex) {
                    }
                    pesan = e.getMessage();
                    System.err.println("validasiJurnal error: " + e.getMessage());
                } finally {
                    try {
                        koneksi.setAutoCommit(true);
                    } catch (Exception ex) {
                    }
                    isProcessing.set(false);
                }

                final boolean ok = sukses;
                final String fMsg = pesan;
                Platform.runLater(() -> {
                    if (engine != null) {
                        if (ok) {
                            engine.executeScript("showToast('" + escapeJS(fMsg) + "', 'success'); if (typeof ambilDataDraft === 'function') ambilDataDraft();");
                        } else {
                            engine.executeScript("showAppError('Gagal Validasi Jurnal', '" + escapeJS(fMsg) + "');");
                        }
                    }
                });
            });
        }

        public void simpanJurnal(String jsonData) {
            dbExecutor.submit(() -> {
                if (!isProcessing.compareAndSet(false, true)) {
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("showToast('Proses penyimpanan sedang berjalan, mohon tunggu...', 'warning');");
                        }
                    });
                    return;
                }
                try {
                    String userLogin = akses.getkode();
                    if (userLogin == null || userLogin.trim().isEmpty()) {
                        userLogin = "admin";
                    }
                    if (!akses.getadmin() && !akses.getposting_jurnal() && !akses.getjurnal_harian() && !userLogin.equalsIgnoreCase("admin")) {
                        throw new Exception("Anda tidak memiliki hak akses untuk menyimpan jurnal.");
                    }

                    createTableJurnalUmum();
                    JSONObject data = new JSONObject(jsonData);
                    String tglRaw = data.has("tanggal") ? data.getString("tanggal") : data.optString("tgl", "");
                    String tgl = formatTglSql(tglRaw);
                    String noJurnal = data.optString("noJurnal", "").trim();
                    String tglClean = tgl.replaceAll("-", "");
                    String prefix = "JM" + tglClean;
                    if (noJurnal.isEmpty() || !noJurnal.startsWith(prefix)) {
                        noJurnal = Valid.autoNomer3(
                                "SELECT IFNULL(MAX(CONVERT(RIGHT(no_jurnal,6),SIGNED)),0) FROM ("
                                + "SELECT no_jurnal FROM jurnal WHERE tgl_jurnal='" + tgl + "' AND no_jurnal LIKE '" + prefix + "%' "
                                + "UNION "
                                + "SELECT no_jurnal FROM jurnal_umum WHERE tgl='" + tgl + "' AND no_jurnal LIKE '" + prefix + "%' "
                                + "UNION "
                                + "SELECT no_jurnal FROM detail_posting_jurnal WHERE no_jurnal LIKE '" + prefix + "%'"
                                + ") AS t",
                                prefix, 6
                        );
                    }

                    String rawJenis = data.optString("jenis", "UMUM");
                    String jenis = (rawJenis.equalsIgnoreCase("P") || rawJenis.toLowerCase().startsWith("penyesuaian")) ? "P" : "U";
                    String tindakanJurnal = rawJenis.toUpperCase();
                    String noBukti = data.optString("noBukti", "").trim();
                    if (noBukti.isEmpty()) {
                        noBukti = noJurnal;
                    }
                    String keterangan = data.optString("keterangan", "").trim();
                    if (keterangan.isEmpty()) {
                        keterangan = "Jurnal Manual " + noJurnal;
                    }
                    JSONArray details = data.getJSONArray("details");

                    if (details.length() < 2) {
                        throw new Exception("Jurnal harus memiliki minimal 2 baris rincian rekening (debit dan kredit).");
                    }

                    double totalDebet = 0;
                    double totalKredit = 0;
                    String kdRekDebitPertama = "";
                    String kdRekKreditPertama = "";
                    List<String> listKdRek = new ArrayList<>();

                    for (int i = 0; i < details.length(); i++) {
                        JSONObject d = details.getJSONObject(i);
                        String kd = d.optString("kdRek", "").trim();
                        if (kd.isEmpty()) {
                            throw new Exception("Baris ke-" + (i + 1) + ": Kode rekening tidak boleh kosong.");
                        }
                        listKdRek.add(kd);
                        double deb = d.optDouble("debet", 0);
                        double kre = d.optDouble("kredit", 0);
                        if (deb < 0 || kre < 0) {
                            throw new Exception("Baris ke-" + (i + 1) + " (" + kd + "): Nominal debet atau kredit tidak boleh negatif.");
                        }
                        if (deb == 0 && kre == 0) {
                            throw new Exception("Baris ke-" + (i + 1) + " (" + kd + "): Nominal debet dan kredit tidak boleh nol keduanya.");
                        }
                        if (deb > 0 && kre > 0) {
                            throw new Exception("Baris ke-" + (i + 1) + " (" + kd + "): Satu baris tidak boleh memiliki nilai debet dan kredit sekaligus.");
                        }
                        totalDebet += deb;
                        totalKredit += kre;
                        if (deb > 0 && kdRekDebitPertama.isEmpty()) {
                            kdRekDebitPertama = kd;
                        }
                        if (kre > 0 && kdRekKreditPertama.isEmpty()) {
                            kdRekKreditPertama = kd;
                        }
                    }

                    // Validasi balance Debet = Kredit dalam satuan sen
                    long dSen = Math.round(totalDebet * 100.0);
                    long kSen = Math.round(totalKredit * 100.0);
                    if (dSen == 0 && kSen == 0) {
                        throw new Exception("Total nilai transaksi tidak boleh nol.");
                    }
                    if (dSen != kSen) {
                        DecimalFormat df = new DecimalFormat("#,##0.00");
                        double selisih = Math.abs((dSen - kSen) / 100.0);
                        throw new Exception("Jurnal tidak balance! Total Debet=" + df.format(totalDebet) + ", Total Kredit=" + df.format(totalKredit) + ", Selisih=" + df.format(selisih));
                    }

                    // Validasi keberadaan akun di master rekening
                    StringBuilder sbPlaceholders = buildBatchPlaceholders(listKdRek.size());
                    LinkedHashSet<String> rekeningAda = new LinkedHashSet<>();
                    try ( PreparedStatement psRek = koneksi.prepareStatement("SELECT kd_rek FROM rekening WHERE kd_rek IN (" + sbPlaceholders + ")")) {
                        for (int i = 0; i < listKdRek.size(); i++) {
                            psRek.setString(i + 1, listKdRek.get(i));
                        }
                        try ( ResultSet rsRek = psRek.executeQuery()) {
                            while (rsRek.next()) {
                                rekeningAda.add(rsRek.getString("kd_rek"));
                            }
                        }
                    }
                    List<String> rekHilang = new ArrayList<>();
                    for (String k : listKdRek) {
                        if (!rekeningAda.contains(k)) {
                            rekHilang.add(k);
                        }
                    }
                    if (!rekHilang.isEmpty()) {
                        throw new Exception("Kode rekening berikut tidak terdaftar di master rekening:\n" + formatListSample(rekHilang, 5));
                    }

                    // Cek apakah jurnal ini sudah pernah berstatus POSTED / Valid
                    try ( PreparedStatement psCek = koneksi.prepareStatement("SELECT status_posting FROM jurnal_posting_status WHERE no_jurnal = ? LIMIT 1")) {
                        psCek.setString(1, noJurnal);
                        try ( ResultSet rsCek = psCek.executeQuery()) {
                            if (rsCek.next() && "POSTED".equalsIgnoreCase(rsCek.getString("status_posting"))) {
                                throw new Exception("Jurnal dengan nomor '" + noJurnal + "' sudah berstatus POSTED dan tidak dapat diubah.");
                            }
                        }
                    }
                    try ( PreparedStatement psCekJU = koneksi.prepareStatement("SELECT status_validasi FROM jurnal_umum WHERE no_jurnal = ? LIMIT 1")) {
                        psCekJU.setString(1, noJurnal);
                        try ( ResultSet rsCekJU = psCekJU.executeQuery()) {
                            if (rsCekJU.next()) {
                                String st = rsCekJU.getString("status_validasi");
                                if ("Posted".equalsIgnoreCase(st) || "Valid".equalsIgnoreCase(st)) {
                                    throw new Exception("Jurnal dengan nomor '" + noJurnal + "' sudah divalidasi/diposting dan tidak dapat diubah lagi.");
                                }
                            }
                        }
                    }

                    koneksi.setAutoCommit(false);

                    // 1. Bersihkan record draft lama jika ada di jurnal_umum dan detail_posting_jurnal
                    try ( PreparedStatement psDelJU = koneksi.prepareStatement("DELETE FROM jurnal_umum WHERE no_jurnal = ?")) {
                        psDelJU.setString(1, noJurnal);
                        psDelJU.executeUpdate();
                        trackSql("DELETE FROM jurnal_umum WHERE no_jurnal = '" + noJurnal + "'");
                    }
                    try ( PreparedStatement psDelDP = koneksi.prepareStatement("DELETE FROM detail_posting_jurnal WHERE no_jurnal = ?")) {
                        psDelDP.setString(1, noJurnal);
                        psDelDP.executeUpdate();
                        trackSql("DELETE FROM detail_posting_jurnal WHERE no_jurnal = '" + noJurnal + "'");
                    }

                    // 2. Simpan Header ke tabel jurnal_umum sebagai Draft
                    String sqlJU = "INSERT INTO jurnal_umum "
                            + "(no_jurnal, no_bukti, tgl, jenis, qty, tarif, total_debet, total_kredit, "
                            + "keterangan, created_by, status_validasi) "
                            + "VALUES (?, ?, ?, ?, 1, ?, ?, ?, ?, ?, 'Draft')";
                    try ( PreparedStatement psJU = koneksi.prepareStatement(sqlJU)) {
                        psJU.setString(1, noJurnal);
                        psJU.setString(2, noBukti);
                        psJU.setString(3, tgl);
                        psJU.setString(4, rawJenis);
                        psJU.setDouble(5, totalDebet);
                        psJU.setDouble(6, totalDebet);
                        psJU.setDouble(7, totalKredit);
                        psJU.setString(8, keterangan);
                        psJU.setString(9, userLogin);
                        psJU.executeUpdate();
                    }

                    // 3. Simpan Detail ke tabel detail_posting_jurnal
                    String sqlDP = "INSERT INTO detail_posting_jurnal (no_jurnal, kd_rek, debet, kredit, keterangan) VALUES (?, ?, ?, ?, ?)";
                    try ( PreparedStatement psDP = koneksi.prepareStatement(sqlDP)) {
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject d = details.getJSONObject(i);
                            String kd = d.getString("kdRek");
                            double deb = d.optDouble("debet", 0);
                            double kre = d.optDouble("kredit", 0);
                            String ketLine = d.optString("keterangan", keterangan);
                            psDP.setString(1, noJurnal);
                            psDP.setString(2, kd);
                            psDP.setDouble(3, deb);
                            psDP.setDouble(4, kre);
                            psDP.setString(5, ketLine);
                            psDP.executeUpdate();
                        }
                    }

                    // 4. Catat status di jurnal_posting_status sebagai DRAFT
                    boolean adaJps = false;
                    try ( PreparedStatement psCek = koneksi.prepareStatement("SELECT no_jurnal FROM jurnal_posting_status WHERE no_jurnal = ?")) {
                        psCek.setString(1, noJurnal);
                        try ( ResultSet rs = psCek.executeQuery()) {
                            if (rs.next()) {
                                adaJps = true;
                            }
                        }
                    }
                    if (adaJps) {
                        try ( PreparedStatement psUpd = koneksi.prepareStatement("UPDATE jurnal_posting_status SET status_posting = 'DRAFT', tanggal_validasi = NOW(), user_validasi = ?, catatan = 'Draft Jurnal Manual Tab A' WHERE no_jurnal = ?")) {
                            psUpd.setString(1, userLogin);
                            psUpd.setString(2, noJurnal);
                            psUpd.executeUpdate();
                        }
                    } else {
                        try ( PreparedStatement psIns = koneksi.prepareStatement("INSERT INTO jurnal_posting_status (no_jurnal, status_posting, tanggal_validasi, user_validasi, catatan) VALUES (?, 'DRAFT', NOW(), ?, 'Draft Jurnal Manual Tab A')")) {
                            psIns.setString(1, noJurnal);
                            psIns.setString(2, userLogin);
                            psIns.executeUpdate();
                        }
                    }

                    koneksi.commit();
                    koneksi.setAutoCommit(true);

                    final String finalNoJur = noJurnal;
                    final String finalNoBukti = noBukti;
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("if (typeof terimaHasilSimpan === 'function') { terimaHasilSimpan(true, 'Jurnal " + escapeJS(finalNoJur) + " (No. Bukti: " + escapeJS(finalNoBukti) + ") berhasil disimpan sebagai draft!', '" + escapeJS(finalNoJur) + "'); }");
                        }
                    });

                } catch (Exception e) {
                    try {
                        koneksi.rollback();
                    } catch (Exception ex) {
                    }
                    System.err.println("simpanJurnal error: " + e.getMessage());
                    final String errMsg = e.getMessage() != null ? e.getMessage() : "Unknown Error";
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("if (typeof terimaHasilSimpan === 'function') { terimaHasilSimpan(false, '" + escapeJS(errMsg) + "', ''); }");
                            engine.executeScript("if (typeof showAppError === 'function') { showAppError('Gagal Menyimpan Jurnal', '" + escapeJS(errMsg) + "'); }");
                        }
                    });
                } finally {
                    try {
                        koneksi.setAutoCommit(true);
                    } catch (Exception e) {
                    }
                    isProcessing.set(false);
                }
            });
        }

        public void cetakKwitansiJurnal(String noJurnal) {
            dbExecutor.execute(() -> {
                try {
                    // Ambil data untuk parameter report
                    String noBukti = "";
                    String keteranganJU = "";
                    String keteranganDP = "";
                    String tanggalJU = "";
                    double totalDebet = 0;

                    try ( PreparedStatement ps = koneksi.prepareStatement(
                            "SELECT no_bukti, keterangan, total_debet, DATE_FORMAT(tgl, '%d-%m-%Y') as tgl_fmt FROM jurnal_umum WHERE no_jurnal=? LIMIT 1")) {
                        ps.setString(1, noJurnal);
                        try ( ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                noBukti = rs.getString("no_bukti");
                                keteranganJU = rs.getString("keterangan");
                                totalDebet = rs.getDouble("total_debet");
                                tanggalJU = rs.getString("tgl_fmt");
                            }
                        }
                    }

                    try ( PreparedStatement ps2 = koneksi.prepareStatement(
                            "SELECT keterangan FROM detail_posting_jurnal WHERE no_jurnal=? LIMIT 1")) {
                        ps2.setString(1, noJurnal);
                        try ( ResultSet rs2 = ps2.executeQuery()) {
                            if (rs2.next()) {
                                keteranganDP = rs2.getString("keterangan");
                            }
                        }
                    }

                    java.util.Map<String, Object> param = new java.util.HashMap<>();
                    param.put("no_jurnal", noJurnal);
                    param.put("namars", akses.getnamars());
                    param.put("alamatrs", akses.getalamatrs());
                    param.put("kotars", akses.getkabupatenrs());
                    param.put("propinsirs", akses.getpropinsirs());
                    param.put("kontakrs", akses.getkontakrs());
                    param.put("emailrs", akses.getemailrs());
                    param.put("logo", Sequel.cariGambar("select setting.logo from setting"));

                    // Parameter custom request
                    param.put("noinvoice", noBukti);
                    param.put("perusahaanasuransi", keteranganJU);
                    param.put("catatan", keteranganDP);
                    param.put("tagihan", Valid.SetAngka4(totalDebet));
                    param.put("terbilang", Valid.terbilang(totalDebet) + " Rupiah");
                    param.put("tanggal", tanggalJU);

                    Valid.MyReportqry("rptKwitansiJurnal.jasper", "report", "::[ Cetak Kwitansi Jurnal ]::",
                            "select '1' as no, '' as temp1, '' as temp2, '' as temp3, '' as temp4, '' as temp5, '' as temp6, '' as temp7, '' as temp8, '' as temp9, '' as temp10, '' as temp11, '' as temp12, '' as temp13, '' as temp14 from jurnal_umum where no_jurnal='" + noJurnal + "'", param);
                } catch (Exception e) {
                    System.err.println("Gagal mencetak kwitansi jurnal: " + e.getMessage());
                }
            });
        }

        public void hapusJurnalBatch(String jsonString) {
            dbExecutor.submit(() -> {
                synchronized (dbLock) {
                    try {
                        if (!akses.getposting_jurnal()) {
                            throw new Exception("Anda tidak memiliki hak akses untuk menghapus jurnal.");
                        }

                        org.json.JSONObject payload = new org.json.JSONObject(jsonString);
                        org.json.JSONArray items = payload.getJSONArray("items");

                        if (items.length() == 0) {
                            return;
                        }

                        List<String> listNoJur = new ArrayList<>();
                        for (int i = 0; i < items.length(); i++) {
                            String nj = items.getJSONObject(i).optString("no_jurnal", "").trim();
                            if (!nj.isEmpty()) {
                                listNoJur.add(nj);
                            }
                        }

                        if (!listNoJur.isEmpty()) {
                            StringBuilder sb = buildBatchPlaceholders(listNoJur.size());
                            String sqlCek = "SELECT no_jurnal FROM jurnal_posting_status WHERE status_posting = 'POSTED' AND no_jurnal IN (" + sb + ")";
                            try ( PreparedStatement ps = koneksi.prepareStatement(sqlCek)) {
                                for (int i = 0; i < listNoJur.size(); i++) {
                                    ps.setString(i + 1, listNoJur.get(i));
                                }
                                try ( ResultSet rs = ps.executeQuery()) {
                                    if (rs.next()) {
                                        throw new Exception("Jurnal '" + rs.getString("no_jurnal") + "' sudah berstatus POSTED dan tidak dapat dihapus!");
                                    }
                                }
                            }
                            String sqlCekJU = "SELECT no_jurnal FROM jurnal_umum WHERE status_validasi = 'Posted' AND no_jurnal IN (" + sb + ")";
                            try ( PreparedStatement ps = koneksi.prepareStatement(sqlCekJU)) {
                                for (int i = 0; i < listNoJur.size(); i++) {
                                    ps.setString(i + 1, listNoJur.get(i));
                                }
                                try ( ResultSet rs = ps.executeQuery()) {
                                    if (rs.next()) {
                                        throw new Exception("Jurnal '" + rs.getString("no_jurnal") + "' sudah berstatus Posted di jurnal umum dan tidak dapat dihapus!");
                                    }
                                }
                            }
                        }

                        koneksi.setAutoCommit(false);
                        int successCount = 0;

                        try ( PreparedStatement psDelDJ = koneksi.prepareStatement("DELETE FROM detailjurnal WHERE no_jurnal = ?");  PreparedStatement psDelJ = koneksi.prepareStatement("DELETE FROM jurnal WHERE no_jurnal = ?");  PreparedStatement psDelJPS = koneksi.prepareStatement("DELETE FROM jurnal_posting_status WHERE no_jurnal = ?");  PreparedStatement psDelJU = koneksi.prepareStatement("DELETE FROM jurnal_umum WHERE no_jurnal = ?");  PreparedStatement psDelDP = koneksi.prepareStatement("DELETE FROM detail_posting_jurnal WHERE no_jurnal = ?")) {

                            for (String noJurnal : listNoJur) {
                                psDelDJ.setString(1, noJurnal);
                                psDelDJ.addBatch();

                                psDelJ.setString(1, noJurnal);
                                psDelJ.addBatch();

                                psDelJPS.setString(1, noJurnal);
                                psDelJPS.addBatch();

                                psDelJU.setString(1, noJurnal);
                                psDelJU.addBatch();

                                psDelDP.setString(1, noJurnal);
                                psDelDP.addBatch();

                                trackSql("DELETE JURNAL: " + noJurnal);
                                successCount++;
                            }

                            if (successCount > 0) {
                                psDelDJ.executeBatch();
                                psDelJ.executeBatch();
                                psDelJPS.executeBatch();
                                psDelJU.executeBatch();
                                psDelDP.executeBatch();
                            }
                        }

                        koneksi.commit();
                        koneksi.setAutoCommit(true);

                        final int finalCount = successCount;
                        Platform.runLater(() -> {
                            if (engine != null) {
                                engine.executeScript("terimaHasilHapusJurnal(true, '', " + finalCount + ");");
                            }
                        });
                    } catch (Exception e) {
                        try {
                            koneksi.rollback();
                        } catch (Exception ex) {
                        }
                        System.err.println("hapusJurnalBatch error: " + e.getMessage());
                        Platform.runLater(() -> {
                            if (engine != null) {
                                engine.executeScript("terimaHasilHapusJurnal(false, '" + escapeJS(e.getMessage()) + "', 0);");
                            }
                        });
                    } finally {
                        try {
                            koneksi.setAutoCommit(true);
                        } catch (Exception e) {
                        }
                    }
                }
            });
        }

        public void cetakJasperJurnal(String noJurnal) {
            dbExecutor.execute(() -> {
                try {
                    java.util.Map<String, Object> param = new java.util.HashMap<>();
                    param.put("namars", akses.getnamars());
                    param.put("alamatrs", akses.getalamatrs());
                    param.put("kotars", akses.getkabupatenrs());
                    param.put("propinsirs", akses.getpropinsirs());
                    param.put("kontakrs", akses.getkontakrs());
                    param.put("emailrs", akses.getemailrs());
                    param.put("logo", Sequel.cariGambar("select setting.logo from setting"));

                    Valid.MyReportqry("rptCetakJurnalUmum.jasper", "report", "::[ Cetak Jurnal Umum Jasper ]::",
                            "SELECT ju.no_jurnal, ju.no_bukti, ju.tgl, ju.keterangan, "
                            + "dp.kd_rek, r.nm_rek, dp.debet, dp.kredit, dp.keterangan as ket_detail "
                            + "FROM jurnal_umum ju "
                            + "INNER JOIN detail_posting_jurnal dp ON dp.no_jurnal = ju.no_jurnal "
                            + "INNER JOIN rekening r ON r.kd_rek = dp.kd_rek "
                            + "WHERE ju.no_jurnal='" + noJurnal + "' ORDER BY dp.debet DESC", param);
                } catch (Exception e) {
                    System.err.println("Gagal mencetak jasper jurnal: " + e.getMessage());
                }
            });
        }

        public void updateJurnal(String jsonData) {
            dbExecutor.submit(() -> {
                if (!isProcessing.compareAndSet(false, true)) {
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("showToast('Proses sedang berjalan, mohon tunggu...', 'warning');");
                        }
                    });
                    return;
                }
                try {
                    if (!akses.getposting_jurnal()) {
                        throw new Exception("Anda tidak memiliki hak akses untuk mengupdate jurnal.");
                    }

                    createTableJurnalUmum();
                    JSONObject data = new JSONObject(jsonData);
                    String noJurnal = data.getString("noJurnal").trim();
                    String tgl = formatTglSql(data.getString("tanggal"));
                    String rawJenis = data.optString("jenis", "UMUM");
                    String jenis = (rawJenis.equalsIgnoreCase("P") || rawJenis.toLowerCase().startsWith("penyesuaian")) ? "P" : "U";
                    String tindakanJurnal = rawJenis.toUpperCase();
                    String noBukti = data.optString("noBukti", "").trim();
                    if (noBukti.isEmpty()) {
                        noBukti = noJurnal;
                    }
                    String keterangan = data.optString("keterangan", "").trim();
                    if (keterangan.isEmpty()) {
                        keterangan = "Jurnal Manual " + noJurnal;
                    }
                    JSONArray details = data.getJSONArray("details");

                    if (details.length() < 2) {
                        throw new Exception("Jurnal harus memiliki minimal 2 baris rincian rekening (debit dan kredit).");
                    }

                    // Cek apakah jurnal sudah berstatus POSTED — cegah update
                    String sqlCekPosted = "SELECT status_posting FROM jurnal_posting_status WHERE no_jurnal = ? LIMIT 1";
                    try ( PreparedStatement psCekP = koneksi.prepareStatement(sqlCekPosted)) {
                        psCekP.setString(1, noJurnal);
                        try ( ResultSet rsCekP = psCekP.executeQuery()) {
                            if (rsCekP.next() && "POSTED".equalsIgnoreCase(rsCekP.getString("status_posting"))) {
                                throw new Exception("Jurnal '" + noJurnal + "' sudah berstatus POSTED dan tidak dapat diubah. Buat jurnal koreksi jika diperlukan.");
                            }
                        }
                    }

                    String sqlCekJU = "SELECT status_validasi FROM jurnal_umum WHERE no_jurnal = ? LIMIT 1";
                    try ( PreparedStatement psCekJU = koneksi.prepareStatement(sqlCekJU)) {
                        psCekJU.setString(1, noJurnal);
                        try ( ResultSet rsCekJU = psCekJU.executeQuery()) {
                            if (rsCekJU.next() && "Posted".equalsIgnoreCase(rsCekJU.getString("status_validasi"))) {
                                throw new Exception("Jurnal '" + noJurnal + "' sudah berstatus Posted di jurnal umum dan tidak dapat diubah.");
                            }
                        }
                    }

                    double totalDebet = 0;
                    double totalKredit = 0;
                    String kdRekDebitPertama = "";
                    String kdRekKreditPertama = "";
                    List<String> listKdRek = new ArrayList<>();

                    for (int i = 0; i < details.length(); i++) {
                        JSONObject d = details.getJSONObject(i);
                        String kd = d.optString("kdRek", "").trim();
                        if (kd.isEmpty()) {
                            throw new Exception("Baris ke-" + (i + 1) + ": Kode rekening tidak boleh kosong.");
                        }
                        listKdRek.add(kd);
                        double deb = d.optDouble("debet", 0);
                        double kre = d.optDouble("kredit", 0);
                        if (deb < 0 || kre < 0) {
                            throw new Exception("Baris ke-" + (i + 1) + " (" + kd + "): Nominal debet atau kredit tidak boleh negatif.");
                        }
                        if (deb == 0 && kre == 0) {
                            throw new Exception("Baris ke-" + (i + 1) + " (" + kd + "): Nominal debet dan kredit tidak boleh nol keduanya.");
                        }
                        if (deb > 0 && kre > 0) {
                            throw new Exception("Baris ke-" + (i + 1) + " (" + kd + "): Satu baris tidak boleh memiliki nilai debet dan kredit sekaligus.");
                        }
                        totalDebet += deb;
                        totalKredit += kre;
                        if (deb > 0 && kdRekDebitPertama.isEmpty()) {
                            kdRekDebitPertama = kd;
                        }
                        if (kre > 0 && kdRekKreditPertama.isEmpty()) {
                            kdRekKreditPertama = kd;
                        }
                    }

                    long dSen = Math.round(totalDebet * 100.0);
                    long kSen = Math.round(totalKredit * 100.0);
                    if (dSen == 0 && kSen == 0) {
                        throw new Exception("Total nilai transaksi tidak boleh nol.");
                    }
                    if (dSen != kSen) {
                        DecimalFormat df = new DecimalFormat("#,##0.00");
                        double selisih = Math.abs((dSen - kSen) / 100.0);
                        throw new Exception("Jurnal tidak balance! Total Debet=" + df.format(totalDebet) + ", Total Kredit=" + df.format(totalKredit) + ", Selisih=" + df.format(selisih));
                    }

                    StringBuilder sbPlaceholders = buildBatchPlaceholders(listKdRek.size());
                    LinkedHashSet<String> rekeningAda = new LinkedHashSet<>();
                    try ( PreparedStatement psRek = koneksi.prepareStatement("SELECT kd_rek FROM rekening WHERE kd_rek IN (" + sbPlaceholders + ")")) {
                        for (int i = 0; i < listKdRek.size(); i++) {
                            psRek.setString(i + 1, listKdRek.get(i));
                        }
                        try ( ResultSet rsRek = psRek.executeQuery()) {
                            while (rsRek.next()) {
                                rekeningAda.add(rsRek.getString("kd_rek"));
                            }
                        }
                    }
                    List<String> rekHilang = new ArrayList<>();
                    for (String k : listKdRek) {
                        if (!rekeningAda.contains(k)) {
                            rekHilang.add(k);
                        }
                    }
                    if (!rekHilang.isEmpty()) {
                        throw new Exception("Kode rekening berikut tidak terdaftar di master rekening:\n" + formatListSample(rekHilang, 5));
                    }

                    koneksi.setAutoCommit(false);

                    // 1. Update atau insert header di jurnal
                    boolean adaDiJurnal = false;
                    try ( PreparedStatement psCekJ = koneksi.prepareStatement("SELECT no_jurnal FROM jurnal WHERE no_jurnal = ?")) {
                        psCekJ.setString(1, noJurnal);
                        try ( ResultSet rsJ = psCekJ.executeQuery()) {
                            if (rsJ.next()) {
                                adaDiJurnal = true;
                            }
                        }
                    }
                    if (adaDiJurnal) {
                        String sqlUpdJ = "UPDATE jurnal SET no_bukti = ?, tgl_jurnal = ?, jenis = ?, keterangan = ? WHERE no_jurnal = ?";
                        try ( PreparedStatement psUJ = koneksi.prepareStatement(sqlUpdJ)) {
                            psUJ.setString(1, noBukti);
                            psUJ.setString(2, tgl);
                            psUJ.setString(3, jenis);
                            psUJ.setString(4, keterangan);
                            psUJ.setString(5, noJurnal);
                            psUJ.executeUpdate();
                        }
                    } else {
                        String jamJurnal = new SimpleDateFormat("HH:mm:ss").format(new Date());
                        String sqlInsJ = "INSERT INTO jurnal (no_jurnal, no_bukti, tgl_jurnal, jam_jurnal, jenis, keterangan) VALUES (?, ?, ?, ?, ?, ?)";
                        try ( PreparedStatement psIJ = koneksi.prepareStatement(sqlInsJ)) {
                            psIJ.setString(1, noJurnal);
                            psIJ.setString(2, noBukti);
                            psIJ.setString(3, tgl);
                            psIJ.setString(4, jamJurnal);
                            psIJ.setString(5, jenis);
                            psIJ.setString(6, keterangan);
                            psIJ.executeUpdate();
                        }
                    }

                    // 2. Ganti rincian di detailjurnal
                    try ( PreparedStatement psDelDJ = koneksi.prepareStatement("DELETE FROM detailjurnal WHERE no_jurnal = ?")) {
                        psDelDJ.setString(1, noJurnal);
                        psDelDJ.executeUpdate();
                    }
                    String sqlInsDJ = "INSERT INTO detailjurnal (no_jurnal, kd_rek, debet, kredit) VALUES (?, ?, ?, ?)";
                    try ( PreparedStatement psIDJ = koneksi.prepareStatement(sqlInsDJ)) {
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject d = details.getJSONObject(i);
                            String kd = d.getString("kdRek");
                            double deb = d.optDouble("debet", 0);
                            double kre = d.optDouble("kredit", 0);
                            psIDJ.setString(1, noJurnal);
                            psIDJ.setString(2, kd);
                            psIDJ.setDouble(3, deb);
                            psIDJ.setDouble(4, kre);
                            psIDJ.executeUpdate();
                        }
                    }

                    // 3. Update status di jurnal_posting_status sebagai DRAFT
                    boolean adaJps = false;
                    try ( PreparedStatement psCekJps = koneksi.prepareStatement("SELECT no_jurnal FROM jurnal_posting_status WHERE no_jurnal = ?")) {
                        psCekJps.setString(1, noJurnal);
                        try ( ResultSet rsJps = psCekJps.executeQuery()) {
                            if (rsJps.next()) {
                                adaJps = true;
                            }
                        }
                    }
                    if (adaJps) {
                        try ( PreparedStatement psUJps = koneksi.prepareStatement("UPDATE jurnal_posting_status SET status_posting = 'DRAFT', tanggal_validasi = NOW(), user_validasi = ?, catatan = 'Updated via Tab A' WHERE no_jurnal = ? AND status_posting != 'POSTED'")) {
                            psUJps.setString(1, akses.getkode());
                            psUJps.setString(2, noJurnal);
                            psUJps.executeUpdate();
                        }
                    } else {
                        try ( PreparedStatement psIJps = koneksi.prepareStatement("INSERT INTO jurnal_posting_status (no_jurnal, status_posting, tanggal_validasi, user_validasi, catatan) VALUES (?, 'DRAFT', NOW(), ?, 'Updated via Tab A')")) {
                            psIJps.setString(1, noJurnal);
                            psIJps.setString(2, akses.getkode());
                            psIJps.executeUpdate();
                        }
                    }

                    // 4. Update jurnal_umum & detail_posting_jurnal untuk kompatibilitas cetak kwitansi / laporan
                    try ( PreparedStatement psDelJU = koneksi.prepareStatement("DELETE FROM jurnal_umum WHERE no_jurnal = ?")) {
                        psDelJU.setString(1, noJurnal);
                        psDelJU.executeUpdate();
                    }
                    try ( PreparedStatement psDelDP = koneksi.prepareStatement("DELETE FROM detail_posting_jurnal WHERE no_jurnal = ?")) {
                        psDelDP.setString(1, noJurnal);
                        psDelDP.executeUpdate();
                    }

                    String sqlJU = "INSERT INTO jurnal_umum "
                            + "(no_jurnal, no_bukti, tgl, jenis, qty, tarif, total_debet, total_kredit, "
                            + "keterangan, created_by, status_validasi) "
                            + "VALUES (?, ?, ?, ?, 1, ?, ?, ?, ?, ?, 'Draft')";
                    try ( PreparedStatement psJU = koneksi.prepareStatement(sqlJU)) {
                        psJU.setString(1, noJurnal);
                        psJU.setString(2, noBukti);
                        psJU.setString(3, tgl);
                        psJU.setString(4, rawJenis);
                        psJU.setDouble(5, totalDebet);
                        psJU.setDouble(6, totalDebet);
                        psJU.setDouble(7, totalKredit);
                        psJU.setString(8, keterangan);
                        psJU.setString(9, akses.getkode());
                        psJU.executeUpdate();
                    }

                    String sqlDP = "INSERT INTO detail_posting_jurnal (no_jurnal, kd_rek, debet, kredit, keterangan) VALUES (?, ?, ?, ?, ?)";
                    try ( PreparedStatement psDP = koneksi.prepareStatement(sqlDP)) {
                        for (int i = 0; i < details.length(); i++) {
                            JSONObject d = details.getJSONObject(i);
                            String kd = d.getString("kdRek");
                            double deb = d.optDouble("debet", 0);
                            double kre = d.optDouble("kredit", 0);
                            String ketLine = d.optString("keterangan", keterangan);
                            psDP.setString(1, noJurnal);
                            psDP.setString(2, kd);
                            psDP.setDouble(3, deb);
                            psDP.setDouble(4, kre);
                            psDP.setString(5, ketLine);
                            psDP.executeUpdate();
                        }
                    }

                    koneksi.commit();
                    koneksi.setAutoCommit(true);

                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("resetFormAndRefresh(); showToast('Jurnal " + escapeJS(noJurnal) + " berhasil diperbarui sebagai Draft','success');");
                            engine.executeScript("if (typeof switchTab === 'function') switchTab('draft');");
                            engine.executeScript("if (typeof loadTransaksiDraft === 'function') loadTransaksiDraft();");
                        }
                    });

                } catch (Exception e) {
                    try {
                        koneksi.rollback();
                    } catch (Exception ex) {
                    }
                    System.err.println("updateJurnal error: " + e.getMessage());
                    final String errMsg = e.getMessage() != null ? e.getMessage() : "Unknown Error";
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("showAppError('Gagal mengupdate jurnal', '" + escapeJS(errMsg) + "')");
                        }
                    });
                } finally {
                    try {
                        koneksi.setAutoCommit(true);
                    } catch (Exception e) {
                    }
                    isProcessing.set(false);
                }
            });
        }

        public void bukaBukuBesar() {
            SwingUtilities.invokeLater(() -> {
                try {
                    DlgBukuBesarWeb form = new DlgBukuBesarWeb(null, false);
                    form.setSize(1300, 750);
                    form.setLocationRelativeTo(DlgPostingJurnalWeb.this);
                    form.setVisible(true);
                } catch (Exception ex) {
                    KeuanganBubes form = new KeuanganBubes(null, false);
                    form.setSize(800, 600);
                    form.setLocationRelativeTo(DlgPostingJurnalWeb.this);
                    form.setVisible(true);
                }
            });
        }

        public void bukaMappingCOA() {
            SwingUtilities.invokeLater(() -> {
                DlgMappingCOAWeb dialog = new DlgMappingCOAWeb(null, false);
                dialog.setSize(1200, 720);
                dialog.setLocationRelativeTo(DlgPostingJurnalWeb.this);
                dialog.setVisible(true);
            });
        }

        public void bukaLabaRugi() {
            SwingUtilities.invokeLater(() -> {
                DlgLabaRugi dialog = new DlgLabaRugi(null, false);
                dialog.setSize(1000, 650);
                dialog.setLocationRelativeTo(DlgPostingJurnalWeb.this);
                dialog.setVisible(true);
            });
        }

        public void bukaNeracaSaldo() {
            SwingUtilities.invokeLater(() -> {
                KeuanganSaldoAkunPerBulan dialog = new KeuanganSaldoAkunPerBulan(null, false);
                dialog.setSize(1000, 650);
                dialog.setLocationRelativeTo(DlgPostingJurnalWeb.this);
                dialog.setVisible(true);
            });
        }

        public void loadTransaksiManualPage(String tglAwal, String tglAkhir, String keyword, String filterAkun, String status, int pageSize, int offset) {
            dbExecutor.submit(() -> {
                synchronized (dbLock) {
                    JSONObject wrapper = new JSONObject();
                    JSONArray arr = new JSONArray();
                    boolean hasMore = false;
                    try {
                        String tgl1 = formatTglSql(tglAwal);
                        String tgl2 = formatTglSql(tglAkhir);

                        // Filter status tanpa prefix alias (dipakai di inner subquery)
                        String filterStatus = " 1=1 ";
                        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("semua")) {
                            if (status.equalsIgnoreCase("Draft")) {
                                filterStatus += " AND (status_validasi = 'Draft' OR status_validasi = 'draft' OR status_validasi IS NULL OR TRIM(status_validasi) = '') "
                                        + " AND NOT EXISTS (SELECT 1 FROM jurnal_posting_status jps WHERE (jps.no_jurnal = jurnal_umum.no_jurnal) AND jps.status_posting = 'POSTED') ";
                            } else if (status.equalsIgnoreCase("Valid")) {
                                filterStatus += " AND (LOWER(TRIM(status_validasi)) IN ('valid', 'sudah valid', 'disetujui') OR status_validasi LIKE '%valid%' "
                                        + " OR EXISTS (SELECT 1 FROM jurnal_posting_status jps WHERE (jps.no_jurnal = jurnal_umum.no_jurnal) AND jps.status_posting = 'POSTED')) ";
                            } else if (status.equalsIgnoreCase("Posted")) {
                                filterStatus += " AND (LOWER(TRIM(status_validasi)) = 'posted' OR status_validasi LIKE '%posted%' "
                                        + " OR EXISTS (SELECT 1 FROM jurnal_posting_status jps WHERE (jps.no_jurnal = jurnal_umum.no_jurnal) AND jps.status_posting = 'POSTED')) ";
                            } else {
                                filterStatus += " AND status_validasi = '" + status + "' ";
                            }
                        }

                        // Ambil pageSize+1 untuk deteksi hasMore
                        int fetchLimit = pageSize + 1;

                        // Inner query: paginasi dari jurnal_umum dengan LIMIT/OFFSET
                        StringBuilder innerSql = new StringBuilder();
                        innerSql.append("(SELECT * FROM jurnal_umum WHERE ")
                                .append(filterStatus)
                                .append(" AND tgl BETWEEN ? AND ?");
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            innerSql.append(" AND (no_bukti LIKE ? OR no_jurnal LIKE ? OR keterangan LIKE ? OR jenis LIKE ?)");
                        }
                        innerSql.append(" ORDER BY tgl DESC, no_jurnal DESC LIMIT ")
                                .append(fetchLimit)
                                .append(" OFFSET ")
                                .append(offset)
                                .append(") AS j");

                        // Outer query: JOIN dengan tabel detail, rekening, dan jurnal_posting_status
                        String outerSql = "SELECT j.no_jurnal, j.no_bukti, j.tgl, j.jenis, "
                                + "j.qty, j.tarif, j.total_debet, j.total_kredit, j.keterangan, j.created_by as akuntan, "
                                + "IF(jps.status_posting = 'POSTED' OR LOWER(TRIM(j.status_validasi)) IN ('posted', 'valid'), 'Posted', IFNULL(j.status_validasi, 'Draft')) as status_validasi, "
                                + "dp.kd_rek as dp_kd_rek, r.nm_rek as dp_nm_rek, dp.debet as dp_debet, dp.kredit as dp_kredit, dp.keterangan as dp_keterangan "
                                + "FROM " + innerSql
                                + " LEFT JOIN jurnal_posting_status jps ON jps.no_jurnal = j.no_jurnal "
                                + " LEFT JOIN detail_posting_jurnal dp ON dp.no_jurnal = j.no_jurnal "
                                + " LEFT JOIN rekening r ON dp.kd_rek = r.kd_rek "
                                + " ORDER BY j.tgl DESC, j.no_jurnal DESC, dp.debet DESC, dp.kd_rek ASC";

                        java.util.LinkedHashMap<String, JSONObject> mapHeaders = new java.util.LinkedHashMap<>();
                        try ( PreparedStatement ps = koneksi.prepareStatement(outerSql)) {
                            int idx = 1;
                            ps.setString(idx++, tgl1);
                            ps.setString(idx++, tgl2);
                            if (keyword != null && !keyword.trim().isEmpty()) {
                                String kw = "%" + keyword.trim() + "%";
                                ps.setString(idx++, kw);
                                ps.setString(idx++, kw);
                                ps.setString(idx++, kw);
                                ps.setString(idx++, kw);
                            }
                            try ( ResultSet rs = ps.executeQuery()) {
                                while (rs.next()) {
                                    String noJur = rs.getString("no_jurnal");
                                    if (noJur == null) {
                                        noJur = "";
                                    }
                                    String noBukti = rs.getString("no_bukti");
                                    if (noBukti == null) {
                                        noBukti = "";
                                    }
                                    String key = noJur;

                                    JSONObject obj = mapHeaders.get(key);
                                    if (obj == null) {
                                        obj = new JSONObject();
                                        obj.put("no_jurnal", noJur);
                                        obj.put("no_bukti", noBukti);
                                        obj.put("tgl", rs.getString("tgl") != null ? rs.getString("tgl") : "");
                                        obj.put("jenis", rs.getString("jenis") != null ? rs.getString("jenis") : "UMUM");
                                        obj.put("qty", rs.getDouble("qty"));
                                        obj.put("tarif", rs.getDouble("tarif"));
                                        obj.put("total_debet", rs.getDouble("total_debet"));
                                        obj.put("total_kredit", rs.getDouble("total_kredit"));
                                        obj.put("keterangan", rs.getString("keterangan") != null ? rs.getString("keterangan") : "");
                                        obj.put("akuntan", rs.getString("akuntan") != null ? rs.getString("akuntan") : "-");
                                        obj.put("kasir", rs.getString("akuntan") != null ? rs.getString("akuntan") : "-");
                                        obj.put("status_validasi", rs.getString("status_validasi") != null ? rs.getString("status_validasi") : "Draft");
                                        obj.put("rincian_jurnal", new JSONArray());
                                        mapHeaders.put(key, obj);
                                    }

                                    String dpKdRek = rs.getString("dp_kd_rek");
                                    if (dpKdRek != null && !dpKdRek.isEmpty()) {
                                        JSONObject d = new JSONObject();
                                        double deb = rs.getDouble("dp_debet");
                                        d.put("kd_rek", dpKdRek);
                                        d.put("nm_rek", rs.getString("dp_nm_rek") != null ? rs.getString("dp_nm_rek") : "");
                                        d.put("debet", deb);
                                        d.put("kredit", rs.getDouble("dp_kredit"));
                                        d.put("keterangan", rs.getString("dp_keterangan") != null ? rs.getString("dp_keterangan") : "");
                                        d.put("posisi", deb > 0 ? "DEBET" : "KREDIT");
                                        obj.getJSONArray("rincian_jurnal").put(d);
                                    }
                                }
                            }
                        }

                        // Deteksi hasMore: jika entry unik melebihi pageSize berarti masih ada data
                        java.util.List<JSONObject> allEntries = new java.util.ArrayList<>(mapHeaders.values());
                        if (allEntries.size() > pageSize) {
                            hasMore = true;
                            allEntries = allEntries.subList(0, pageSize);
                        }

                        // Build output array
                        for (JSONObject obj : allEntries) {
                            arr.put(obj);
                        }

                        wrapper.put("data", arr);
                        wrapper.put("hasMore", hasMore);
                        wrapper.put("offset", offset);
                        wrapper.put("pageSize", pageSize);

                    } catch (Throwable e) {
                        System.err.println("loadTransaksiManualPage error: " + e.getMessage());
                        e.printStackTrace();
                        final String errMsg = e.getMessage() != null ? e.getMessage().replace("'", "\\'").replace("\n", "\\n") : "Unknown Error";
                        Platform.runLater(() -> {
                            if (engine != null) {
                                engine.executeScript("if (typeof showAppError === 'function') { showAppError('Gagal Query Database (Manual)', '" + errMsg + "'); }");
                                engine.executeScript("if (typeof setManualLoadingState === 'function') setManualLoadingState(false);");
                            }
                        });
                        return;
                    }

                    final String json = wrapper.toString();
                    lastManualData = json;
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("if (typeof ambilDataManual === 'function') { ambilDataManual(); } else { console.error('ambilDataManual not found!'); }");
                        }
                    });
                }
            });
        }

        private String lastManualData = "";

        public int getManualDataCount() {
            if (lastManualData == null || lastManualData.isEmpty()) {
                return 0;
            }
            return (int) Math.ceil((double) lastManualData.length() / (double) 32000);
        }

        public String getManualDataChunk(int chunkIndex) {
            if (lastManualData == null || lastManualData.isEmpty()) {
                return "";
            }
            int start = chunkIndex * 32000;
            if (start >= lastManualData.length()) {
                return "";
            }
            int end = Math.min(start + 32000, lastManualData.length());
            return lastManualData.substring(start, end);
        }

        public void validasiJurnalUmumManual(String jsonData) {
            dbExecutor.submit(() -> {
                synchronized (dbLock) {
                    if (!isProcessing.compareAndSet(false, true)) {
                        Platform.runLater(() -> {
                            if (engine != null) {
                                engine.executeScript("terimaHasilValidasiManual(false, 'Sedang memproses validasi transaksi lain, mohon tunggu...', 0);");
                            }
                        });
                        return;
                    }

                    boolean sukses = false;
                    String pesan = "";
                    int countValidated = 0;
                    try {
                        String userValidasi = akses.getkode();
                        if (userValidasi == null || userValidasi.trim().isEmpty()) {
                            throw new Exception("User tidak teridentifikasi.");
                        }
                        if (!akses.getposting_jurnal() && !akses.getjurnal_harian()) {
                            throw new Exception("Anda tidak memiliki hak akses untuk memposting jurnal.");
                        }

                        JSONObject root = new JSONObject(jsonData);
                        JSONArray items = root.getJSONArray("items");
                        if (items.length() == 0) {
                            throw new Exception("Tidak ada transaksi yang dipilih untuk divalidasi.");
                        }

                        // 1. Ekstrak daftar unik no_jurnal / no_bukti
                        LinkedHashMap<String, String> itemMap = new LinkedHashMap<>(); // key: noJurnal, val: noBukti
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            String noBukti = item.optString("no_bukti", "").trim();
                            String noJurnal = item.optString("no_jurnal", "").trim();
                            if (noJurnal.isEmpty() && !noBukti.isEmpty()) {
                                noJurnal = noBukti;
                            }
                            if (noBukti.isEmpty() && !noJurnal.isEmpty()) {
                                noBukti = noJurnal;
                            }
                            if (!noJurnal.isEmpty()) {
                                itemMap.put(noJurnal, noBukti);
                            }
                        }

                        if (itemMap.isEmpty()) {
                            throw new Exception("Nomor transaksi tidak valid.");
                        }

                        List<String> allKeys = new ArrayList<>(itemMap.keySet());

                        // 2. Cek apakah sudah berstatus POSTED di jurnal_posting_status
                        LinkedHashSet<String> sudahPosted = new LinkedHashSet<>();
                        int batchSize = 500;
                        for (int i = 0; i < allKeys.size(); i += batchSize) {
                            int end = Math.min(i + batchSize, allKeys.size());
                            List<String> sub = allKeys.subList(i, end);
                            StringBuilder sbIn = buildBatchPlaceholders(sub.size());
                            String sqlCek = "SELECT no_jurnal FROM jurnal_posting_status WHERE status_posting = 'POSTED' AND no_jurnal IN (" + sbIn + ")";
                            try ( PreparedStatement psCek = koneksi.prepareStatement(sqlCek)) {
                                for (int j = 0; j < sub.size(); j++) {
                                    psCek.setString(j + 1, sub.get(j));
                                }
                                try ( ResultSet rsCek = psCek.executeQuery()) {
                                    while (rsCek.next()) {
                                        sudahPosted.add(rsCek.getString("no_jurnal"));
                                    }
                                }
                            }
                        }

                        List<String> jurnalsToProcess = new ArrayList<>();
                        for (String k : allKeys) {
                            if (!sudahPosted.contains(k)) {
                                jurnalsToProcess.add(k);
                            }
                        }

                        if (jurnalsToProcess.isEmpty()) {
                            final int skipCount = sudahPosted.size();
                            Platform.runLater(() -> {
                                if (engine != null) {
                                    engine.executeScript("terimaHasilValidasiManual(true, 'Semua jurnal terpilih (" + skipCount + ") sudah berstatus Posted sebelumnya.', 0);");
                                }
                            });
                            return;
                        }

                        // 3. Validasi setiap jurnal: cek apakah sudah ada di jurnal & detailjurnal atau perlu dimigrasi dari jurnal_umum
                        class ManualItemData {

                            String noJurnal;
                            String noBukti;
                            String tgl;
                            String keterangan;
                            boolean perluMigrasi = false;
                            List<String> kdReks = new ArrayList<>();
                            List<Double> debets = new ArrayList<>();
                            List<Double> kredits = new ArrayList<>();
                        }

                        List<ManualItemData> validItems = new ArrayList<>();
                        List<String> unbalanceList = new ArrayList<>();
                        List<String> akunHilangTotal = new ArrayList<>();
                        List<String> nominalNegatifList = new ArrayList<>();
                        DecimalFormat df = new DecimalFormat("#,##0.00");

                        for (String noJur : jurnalsToProcess) {
                            String noBuk = itemMap.get(noJur);
                            ManualItemData mid = new ManualItemData();
                            mid.noJurnal = noJur;
                            mid.noBukti = noBuk;

                            // Cek eksistensi di jurnal & detailjurnal
                            boolean adaDiJurnal = false;
                            try ( PreparedStatement psJ = koneksi.prepareStatement("SELECT no_jurnal, no_bukti, tgl_jurnal, keterangan FROM jurnal WHERE no_jurnal = ? LIMIT 1")) {
                                psJ.setString(1, noJur);
                                try ( ResultSet rsJ = psJ.executeQuery()) {
                                    if (rsJ.next()) {
                                        adaDiJurnal = true;
                                        mid.tgl = rsJ.getString("tgl_jurnal");
                                        mid.keterangan = rsJ.getString("keterangan");
                                        if (mid.noBukti == null || mid.noBukti.isEmpty()) {
                                            mid.noBukti = rsJ.getString("no_bukti");
                                        }
                                    }
                                }
                            }

                            if (adaDiJurnal) {
                                // Baca rincian dari detailjurnal
                                try ( PreparedStatement psDJ = koneksi.prepareStatement("SELECT kd_rek, debet, kredit FROM detailjurnal WHERE no_jurnal = ?")) {
                                    psDJ.setString(1, noJur);
                                    try ( ResultSet rsDJ = psDJ.executeQuery()) {
                                        while (rsDJ.next()) {
                                            mid.kdReks.add(rsDJ.getString("kd_rek"));
                                            mid.debets.add(rsDJ.getDouble("debet"));
                                            mid.kredits.add(rsDJ.getDouble("kredit"));
                                        }
                                    }
                                }
                            } else {
                                // Ambil dari jurnal_umum & detail_posting_jurnal (legacy)
                                mid.perluMigrasi = true;
                                try ( PreparedStatement psJU = koneksi.prepareStatement("SELECT no_bukti, no_jurnal, tgl, keterangan, total_debet, total_kredit FROM jurnal_umum WHERE no_jurnal = ? OR no_bukti = ? LIMIT 1")) {
                                    psJU.setString(1, noJur);
                                    psJU.setString(2, noBuk);
                                    try ( ResultSet rsJU = psJU.executeQuery()) {
                                        if (rsJU.next()) {
                                            mid.tgl = rsJU.getString("tgl");
                                            mid.keterangan = rsJU.getString("keterangan");
                                            if (mid.noBukti == null || mid.noBukti.isEmpty()) {
                                                mid.noBukti = rsJU.getString("no_bukti");
                                            }
                                            // Baca dari detail_posting_jurnal
                                            try ( PreparedStatement psDP = koneksi.prepareStatement("SELECT kd_rek, debet, kredit FROM detail_posting_jurnal WHERE no_jurnal = ? OR no_jurnal = ?")) {
                                                psDP.setString(1, noJur);
                                                psDP.setString(2, noBuk);
                                                try ( ResultSet rsDP = psDP.executeQuery()) {
                                                    while (rsDP.next()) {
                                                        mid.kdReks.add(rsDP.getString("kd_rek"));
                                                        mid.debets.add(rsDP.getDouble("debet"));
                                                        mid.kredits.add(rsDP.getDouble("kredit"));
                                                    }
                                                }
                                            }
                                        } else {
                                            throw new Exception("Data jurnal '" + noJur + "' tidak ditemukan di sistem.");
                                        }
                                    }
                                }
                            }

                            if (mid.kdReks.size() < 2) {
                                throw new Exception("Jurnal '" + noJur + "' tidak memiliki minimal 2 baris rincian rekening.");
                            }

                            // Cek baris per baris & hitung balance
                            double totalDebet = 0;
                            double totalKredit = 0;
                            for (int idx = 0; idx < mid.kdReks.size(); idx++) {
                                String kd = mid.kdReks.get(idx);
                                double d = mid.debets.get(idx);
                                double k = mid.kredits.get(idx);
                                if (d < 0 || k < 0) {
                                    nominalNegatifList.add(noJur + " (" + kd + ")");
                                }
                                totalDebet += d;
                                totalKredit += k;
                            }

                            long dSen = Math.round(totalDebet * 100.0);
                            long kSen = Math.round(totalKredit * 100.0);
                            if (dSen == 0 && kSen == 0) {
                                throw new Exception("Jurnal '" + noJur + "' memiliki total nilai 0.");
                            }
                            if (dSen != kSen) {
                                double selisih = Math.abs((dSen - kSen) / 100.0);
                                unbalanceList.add(noJur + " (Debet: " + df.format(totalDebet) + ", Kredit: " + df.format(totalKredit) + ", Selisih: " + df.format(selisih) + ")");
                            }

                            // Cek master rekening
                            StringBuilder sbPl = buildBatchPlaceholders(mid.kdReks.size());
                            LinkedHashSet<String> adaRek = new LinkedHashSet<>();
                            try ( PreparedStatement psRek = koneksi.prepareStatement("SELECT kd_rek FROM rekening WHERE kd_rek IN (" + sbPl + ")")) {
                                for (int idx = 0; idx < mid.kdReks.size(); idx++) {
                                    psRek.setString(idx + 1, mid.kdReks.get(idx));
                                }
                                try ( ResultSet rsRek = psRek.executeQuery()) {
                                    while (rsRek.next()) {
                                        adaRek.add(rsRek.getString("kd_rek"));
                                    }
                                }
                            }
                            for (String kd : mid.kdReks) {
                                if (!adaRek.contains(kd)) {
                                    akunHilangTotal.add(kd);
                                }
                            }

                            validItems.add(mid);
                        }

                        if (!nominalNegatifList.isEmpty()) {
                            throw new Exception("Ditemukan nominal negatif pada rincian jurnal:\n" + formatListSample(nominalNegatifList, 5));
                        }
                        if (!unbalanceList.isEmpty()) {
                            throw new Exception("Ditemukan jurnal yang tidak balance:\n" + formatListSample(unbalanceList, 5));
                        }
                        if (!akunHilangTotal.isEmpty()) {
                            throw new Exception("Kode rekening berikut tidak terdaftar di master rekening:\n" + formatListSample(akunHilangTotal, 5));
                        }

                        // 4. Eksekusi database transaction
                        koneksi.setAutoCommit(false);

                        // Migrasi item jika belum ada di jurnal & detailjurnal
                        String sqlInsJ = "INSERT INTO jurnal (no_jurnal, no_bukti, tgl_jurnal, jam_jurnal, jenis, keterangan) VALUES (?, ?, ?, '00:00:00', 'U', ?)";
                        String sqlInsDJ = "INSERT INTO detailjurnal (no_jurnal, kd_rek, debet, kredit) VALUES (?, ?, ?, ?)";
                        try ( PreparedStatement psInsJ = koneksi.prepareStatement(sqlInsJ);  PreparedStatement psInsDJ = koneksi.prepareStatement(sqlInsDJ)) {
                            for (ManualItemData mid : validItems) {
                                if (mid.perluMigrasi) {
                                    psInsJ.setString(1, mid.noJurnal);
                                    psInsJ.setString(2, mid.noBukti != null ? mid.noBukti : mid.noJurnal);
                                    psInsJ.setString(3, mid.tgl);
                                    psInsJ.setString(4, mid.keterangan != null ? mid.keterangan : "");
                                    psInsJ.executeUpdate();

                                    for (int idx = 0; idx < mid.kdReks.size(); idx++) {
                                        psInsDJ.setString(1, mid.noJurnal);
                                        psInsDJ.setString(2, mid.kdReks.get(idx));
                                        psInsDJ.setDouble(3, mid.debets.get(idx));
                                        psInsDJ.setDouble(4, mid.kredits.get(idx));
                                        psInsDJ.addBatch();
                                    }
                                    psInsDJ.executeBatch();
                                }
                            }
                        }

                        // Update / Insert jurnal_posting_status
                        for (ManualItemData mid : validItems) {
                            boolean adaJps = false;
                            try ( PreparedStatement psCek = koneksi.prepareStatement("SELECT no_jurnal FROM jurnal_posting_status WHERE no_jurnal = ?")) {
                                psCek.setString(1, mid.noJurnal);
                                try ( ResultSet rsCek = psCek.executeQuery()) {
                                    if (rsCek.next()) {
                                        adaJps = true;
                                    }
                                }
                            }
                            if (adaJps) {
                                String sqlUpd = "UPDATE jurnal_posting_status SET status_posting = 'POSTED', tanggal_validasi = NOW(), user_validasi = ?, tanggal_posting = NOW(), user_posting = ?, catatan = 'Validasi Manual via DlgPostingJurnalWeb' WHERE no_jurnal = ? AND status_posting != 'POSTED'";
                                try ( PreparedStatement psUpd = koneksi.prepareStatement(sqlUpd)) {
                                    psUpd.setString(1, userValidasi);
                                    psUpd.setString(2, userValidasi);
                                    psUpd.setString(3, mid.noJurnal);
                                    psUpd.executeUpdate();
                                }
                            } else {
                                String sqlIns = "INSERT INTO jurnal_posting_status (no_jurnal, status_posting, tanggal_validasi, user_validasi, tanggal_posting, user_posting, catatan) VALUES (?, 'POSTED', NOW(), ?, NOW(), ?, 'Validasi Manual via DlgPostingJurnalWeb')";
                                try ( PreparedStatement psIns = koneksi.prepareStatement(sqlIns)) {
                                    psIns.setString(1, mid.noJurnal);
                                    psIns.setString(2, userValidasi);
                                    psIns.setString(3, userValidasi);
                                    psIns.executeUpdate();
                                }
                            }

                            // Update jurnal_umum status_validasi = 'Posted'
                            String sqlUpdJU = "UPDATE jurnal_umum SET status_validasi = 'Posted', created_by = ? WHERE (no_jurnal = ? AND no_jurnal != '') OR no_bukti = ?";
                            try ( PreparedStatement psJU = koneksi.prepareStatement(sqlUpdJU)) {
                                psJU.setString(1, userValidasi);
                                psJU.setString(2, mid.noJurnal);
                                psJU.setString(3, mid.noBukti != null ? mid.noBukti : mid.noJurnal);
                                psJU.executeUpdate();
                            }
                            countValidated++;
                        }

                        koneksi.commit();
                        koneksi.setAutoCommit(true);
                        sukses = true;
                        pesan = "Berhasil memvalidasi " + countValidated + " transaksi! Status berubah menjadi 'Posted' di jurnal utama.";
                        if (sudahPosted.size() > 0) {
                            pesan += " (" + sudahPosted.size() + " transaksi dilewati karena sudah Posted).";
                        }
                    } catch (Exception e) {
                        try {
                            koneksi.rollback();
                        } catch (Exception ex) {
                        }
                        pesan = e.getMessage() != null ? e.getMessage() : "Terjadi kesalahan.";
                        System.err.println("validasiJurnalUmumManual error: " + pesan);
                    } finally {
                        try {
                            koneksi.setAutoCommit(true);
                        } catch (Exception e) {
                        }
                        isProcessing.set(false);
                    }

                    final boolean ok = sukses;
                    final String msg = pesan;
                    final int jml = countValidated;
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("if (typeof terimaHasilValidasiManual === 'function') { terimaHasilValidasiManual(" + ok + ", '" + escapeJS(msg) + "', " + jml + "); }");
                        }
                    });
                }
            });
        }

        public void bukaKwitansi() {
            SwingUtilities.invokeLater(() -> {
                try {
                    if (kwitansi == null) {
                        kwitansi = new DlgKwitansi(null, false);
                        kwitansi.setSize(1050, 680);
                        kwitansi.setLocationRelativeTo(DlgPostingJurnalWeb.this);
                        kwitansi.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseClicked(java.awt.event.MouseEvent evt) {
                                if (evt.getClickCount() == 2 && kwitansi.getTable().getSelectedRow() != -1) {
                                    kwitansi.dispose();
                                }
                            }
                        });
                        kwitansi.addWindowListener(new java.awt.event.WindowAdapter() {
                            @Override
                            public void windowClosed(java.awt.event.WindowEvent e) {
                                int selRow = kwitansi.getTable().getSelectedRow();
                                if (selRow != -1) {
                                    String noRekap = kwitansi.getTable().getValueAt(selRow, 0).toString();
                                    prosesPilihKwitansi(noRekap);
                                }
                            }
                        });
                    }
                    kwitansi.tampil();
                    kwitansi.setVisible(true);
                } catch (Exception ex) {
                    System.err.println("bukaKwitansi error: " + ex.getMessage());
                }
            });
        }

        public void loadJurnalSumber(String filterJson) {
            dbExecutor.submit(() -> {
                synchronized (dbLock) {
                    try {
                        JSONObject req = new JSONObject(filterJson);
                        String sumber = req.optString("sumber", "MASUK").toUpperCase();
                        boolean isMasuk = sumber.equals("MASUK");
                        String tgl1 = formatTglSql(req.optString("tgl1"));
                        String tgl2 = formatTglSql(req.optString("tgl2"));
                        String statusPosting = req.optString("statusPosting", "Semua");
                        String kategori = req.optString("kategori", "Semua");
                        String akunBayar = req.optString("akunBayar", "Semua");
                        String keyword = req.optString("keyword", "").trim();
                        int limit = Math.max(50, Math.min(500, req.optInt("limit", 250)));
                        int page = Math.max(1, req.optInt("page", 1));
                        int offset = (page - 1) * limit;

                        StringBuilder whereBase = new StringBuilder();
                        List<Object> paramsBase = new ArrayList<>();

                        whereBase.append(" WHERE j.tgl_jurnal BETWEEN ? AND ? ");
                        paramsBase.add(tgl1);
                        paramsBase.add(tgl2);

                        if (isMasuk) {
                            whereBase.append(" AND (j.keterangan LIKE 'PEMASUKAN LAIN-LAIN%' OR j.keterangan LIKE 'PEMBATALAN PEMASUKAN%') ");
                        } else {
                            whereBase.append(" AND (j.no_bukti LIKE 'PH%' OR j.keterangan LIKE 'PENGELUARAN HARIAN%' OR j.keterangan LIKE 'PEMBATALAN PENGELUARAN%') ");
                        }

                        if ("Draft".equalsIgnoreCase(statusPosting)) {
                            whereBase.append(" AND (jps.status_posting IS NULL OR jps.status_posting != 'POSTED') AND v.no_jurnal IS NULL ");
                        } else if ("Posted".equalsIgnoreCase(statusPosting)) {
                            whereBase.append(" AND (jps.status_posting = 'POSTED' OR v.no_jurnal IS NOT NULL) ");
                        }

                        if (!"Semua".equalsIgnoreCase(kategori) && !kategori.isEmpty()) {
                            if (isMasuk) {
                                whereBase.append(" AND (kpl.kode_kategori = ? OR kpl.nama_kategori = ?) ");
                                paramsBase.add(kategori);
                                paramsBase.add(kategori);
                            } else {
                                whereBase.append(" AND (kph.kode_kategori = ? OR kph.nama_kategori = ?) ");
                                paramsBase.add(kategori);
                                paramsBase.add(kategori);
                            }
                        }

                        if (isMasuk && !"Semua".equalsIgnoreCase(akunBayar) && !akunBayar.isEmpty()) {
                            whereBase.append(" AND pab.nama_bayar = ? ");
                            paramsBase.add(akunBayar);
                        }

                        if (!keyword.isEmpty()) {
                            String kw = "%" + keyword + "%";
                            if (isMasuk) {
                                whereBase.append(" AND (j.no_bukti LIKE ? OR j.no_jurnal LIKE ? OR j.keterangan LIKE ? OR pl.keterangan LIKE ? OR pl.keperluan LIKE ? OR pt.nama LIKE ?) ");
                                for (int k = 0; k < 6; k++) {
                                    paramsBase.add(kw);
                                }
                            } else {
                                whereBase.append(" AND (j.no_bukti LIKE ? OR j.no_jurnal LIKE ? OR j.keterangan LIKE ? OR ph.keterangan LIKE ? OR ph.no_bukti LIKE ? OR pt.nama LIKE ?) ");
                                for (int k = 0; k < 6; k++) {
                                    paramsBase.add(kw);
                                }
                            }
                        }

                        String joinFrom = isMasuk
                                ? "jurnal j "
                                + "LEFT JOIN jurnal_posting_status jps ON j.no_jurnal = jps.no_jurnal "
                                + "LEFT JOIN validasi_jurnal_harian v ON j.no_jurnal = v.no_jurnal "
                                + "LEFT JOIN pemasukan_lain pl ON j.no_bukti = pl.no_masuk "
                                + "LEFT JOIN kategori_pemasukan_lain kpl ON pl.kode_kategori = kpl.kode_kategori "
                                + "LEFT JOIN pemasukan_lain_akunbayar pab ON pl.no_masuk = pab.no_masuk "
                                + "LEFT JOIN petugas pt ON pl.nip = pt.nip "
                                : "jurnal j "
                                + "LEFT JOIN jurnal_posting_status jps ON j.no_jurnal = jps.no_jurnal "
                                + "LEFT JOIN validasi_jurnal_harian v ON j.no_jurnal = v.no_jurnal "
                                + "LEFT JOIN pengeluaran_harian ph ON j.no_bukti = ph.no_keluar "
                                + "LEFT JOIN kategori_pengeluaran_harian kph ON ph.kode_kategori = kph.kode_kategori "
                                + "LEFT JOIN petugas pt ON ph.nip = pt.nip ";

                        int totalTransaksi = 0;
                        String sqlCount = "SELECT COUNT(DISTINCT j.tgl_jurnal, j.no_bukti) FROM " + joinFrom + whereBase.toString();
                        try ( PreparedStatement psCount = koneksi.prepareStatement(sqlCount)) {
                            for (int i = 0; i < paramsBase.size(); i++) {
                                psCount.setObject(i + 1, paramsBase.get(i));
                            }
                            try ( ResultSet rs = psCount.executeQuery()) {
                                if (rs.next()) {
                                    totalTransaksi = rs.getInt(1);
                                }
                            }
                        }

                        int totalHalaman = Math.max(1, (int) Math.ceil((double) totalTransaksi / (double) limit));

                        if (totalTransaksi == 0) {
                            final int fTotTrx = 0;
                            final int fTotHal = 1;
                            Platform.runLater(() -> {
                                if (engine != null) {
                                    engine.executeScript("terimaJurnalSumber('" + sumber + "', '[]');");
                                    engine.executeScript("setInfoHalamanSumber('" + sumber + "', 1, " + fTotHal + ", " + fTotTrx + ");");
                                }
                            });
                            return;
                        }

                        List<String> pageNoBukti = new ArrayList<>();
                        String sqlPageGroups = "SELECT DISTINCT j.tgl_jurnal, j.no_bukti FROM " + joinFrom + whereBase.toString()
                                + " ORDER BY j.tgl_jurnal ASC, j.no_bukti ASC LIMIT ? OFFSET ?";
                        try ( PreparedStatement psGrp = koneksi.prepareStatement(sqlPageGroups)) {
                            int idx = 1;
                            for (Object p : paramsBase) {
                                psGrp.setObject(idx++, p);
                            }
                            psGrp.setInt(idx++, limit);
                            psGrp.setInt(idx++, offset);
                            try ( ResultSet rs = psGrp.executeQuery()) {
                                while (rs.next()) {
                                    String nb = rs.getString("no_bukti");
                                    if (nb != null && !nb.isEmpty()) {
                                        pageNoBukti.add(nb);
                                    }
                                }
                            }
                        }

                        if (pageNoBukti.isEmpty()) {
                            final int fTotTrx = totalTransaksi;
                            final int fTotHal = totalHalaman;
                            Platform.runLater(() -> {
                                if (engine != null) {
                                    engine.executeScript("terimaJurnalSumber('" + sumber + "', '[]');");
                                    engine.executeScript("setInfoHalamanSumber('" + sumber + "', " + page + ", " + fTotHal + ", " + fTotTrx + ");");
                                }
                            });
                            return;
                        }

                        StringBuilder inBukti = new StringBuilder();
                        for (int i = 0; i < pageNoBukti.size(); i++) {
                            inBukti.append("?");
                            if (i < pageNoBukti.size() - 1) {
                                inBukti.append(",");
                            }
                        }

                        String sqlDetails;
                        if (isMasuk) {
                            sqlDetails = "SELECT j.no_jurnal, j.no_bukti, j.tgl_jurnal, j.keterangan as j_keterangan, "
                                    + "dj.kd_rek, r.nm_rek, dj.debet, dj.kredit, "
                                    + "IF(jps.status_posting = 'POSTED' OR v.no_jurnal IS NOT NULL, 'Posted', 'Draft') AS status_validasi, "
                                    + "pl.no_masuk, kpl.nama_kategori, pl.keterangan as pl_keterangan, pl.keperluan, "
                                    + "COALESCE(pt.nama, pl.nip, '') AS nama_petugas, pab.nama_bayar "
                                    + "FROM jurnal j "
                                    + "INNER JOIN detailjurnal dj ON j.no_jurnal = dj.no_jurnal "
                                    + "LEFT JOIN rekening r ON dj.kd_rek = r.kd_rek "
                                    + "LEFT JOIN jurnal_posting_status jps ON j.no_jurnal = jps.no_jurnal "
                                    + "LEFT JOIN validasi_jurnal_harian v ON j.no_jurnal = v.no_jurnal "
                                    + "LEFT JOIN pemasukan_lain pl ON j.no_bukti = pl.no_masuk "
                                    + "LEFT JOIN kategori_pemasukan_lain kpl ON pl.kode_kategori = kpl.kode_kategori "
                                    + "LEFT JOIN pemasukan_lain_akunbayar pab ON pl.no_masuk = pab.no_masuk "
                                    + "LEFT JOIN petugas pt ON pl.nip = pt.nip "
                                    + "WHERE j.tgl_jurnal BETWEEN ? AND ? AND j.no_bukti IN (" + inBukti.toString() + ") "
                                    + "ORDER BY j.tgl_jurnal ASC, j.no_bukti ASC, j.no_jurnal ASC, dj.debet DESC, dj.kd_rek ASC";
                        } else {
                            sqlDetails = "SELECT j.no_jurnal, j.no_bukti, j.tgl_jurnal, j.keterangan as j_keterangan, "
                                    + "dj.kd_rek, r.nm_rek, dj.debet, dj.kredit, "
                                    + "IF(jps.status_posting = 'POSTED' OR v.no_jurnal IS NOT NULL, 'Posted', 'Draft') AS status_validasi, "
                                    + "ph.no_keluar, kph.nama_kategori, ph.keterangan as ph_keterangan, ph.no_bukti as ph_kwitansi, "
                                    + "COALESCE(pt.nama, ph.nip, '') AS nama_petugas "
                                    + "FROM jurnal j "
                                    + "INNER JOIN detailjurnal dj ON j.no_jurnal = dj.no_jurnal "
                                    + "LEFT JOIN rekening r ON dj.kd_rek = r.kd_rek "
                                    + "LEFT JOIN jurnal_posting_status jps ON j.no_jurnal = jps.no_jurnal "
                                    + "LEFT JOIN validasi_jurnal_harian v ON j.no_jurnal = v.no_jurnal "
                                    + "LEFT JOIN pengeluaran_harian ph ON j.no_bukti = ph.no_keluar "
                                    + "LEFT JOIN kategori_pengeluaran_harian kph ON ph.kode_kategori = kph.kode_kategori "
                                    + "LEFT JOIN petugas pt ON ph.nip = pt.nip "
                                    + "WHERE j.tgl_jurnal BETWEEN ? AND ? AND j.no_bukti IN (" + inBukti.toString() + ") "
                                    + "ORDER BY j.tgl_jurnal ASC, j.no_bukti ASC, j.no_jurnal ASC, dj.debet DESC, dj.kd_rek ASC";
                        }

                        JSONArray arrAyat = new JSONArray();
                        SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd-MM-yyyy");
                        try ( PreparedStatement psDet = koneksi.prepareStatement(sqlDetails)) {
                            int idx = 1;
                            psDet.setString(idx++, tgl1);
                            psDet.setString(idx++, tgl2);
                            for (String nb : pageNoBukti) {
                                psDet.setString(idx++, nb);
                            }

                            try ( ResultSet rs = psDet.executeQuery()) {
                                while (rs.next()) {
                                    JSONObject ayat = new JSONObject();
                                    ayat.put("no_jurnal", rs.getString("no_jurnal") != null ? rs.getString("no_jurnal") : "");
                                    ayat.put("no_bukti", rs.getString("no_bukti") != null ? rs.getString("no_bukti") : "");
                                    java.sql.Date dSql = rs.getDate("tgl_jurnal");
                                    ayat.put("tgl_jurnal", dSql != null ? sdfDisplay.format(dSql) : "");
                                    ayat.put("kd_rek", rs.getString("kd_rek") != null ? rs.getString("kd_rek") : "");
                                    ayat.put("nm_rek", rs.getString("nm_rek") != null ? rs.getString("nm_rek") : "");
                                    ayat.put("debet", rs.getDouble("debet"));
                                    ayat.put("kredit", rs.getDouble("kredit"));
                                    ayat.put("status", rs.getString("status_validasi") != null ? rs.getString("status_validasi") : "Draft");
                                    ayat.put("no_trx", rs.getString("no_bukti") != null ? rs.getString("no_bukti") : "");

                                    String jKet = rs.getString("j_keterangan") != null ? rs.getString("j_keterangan") : "";
                                    boolean isBatal = jKet.toUpperCase().contains("PEMBATALAN");
                                    ayat.put("is_batal", isBatal);

                                    if (isMasuk) {
                                        String kat = rs.getString("nama_kategori");
                                        ayat.put("kategori", kat != null && !kat.isEmpty() ? kat : "Lain-lain");
                                        String plKet = rs.getString("pl_keterangan");
                                        String plKep = rs.getString("keperluan");
                                        String kep = (plKet != null && !plKet.isEmpty()) ? plKet : ((plKep != null && !plKep.isEmpty()) ? plKep : jKet);
                                        ayat.put("keperluan", kep);
                                        ayat.put("petugas", rs.getString("nama_petugas") != null ? rs.getString("nama_petugas") : "");
                                        ayat.put("akun_bayar", rs.getString("nama_bayar") != null ? rs.getString("nama_bayar") : "");
                                    } else {
                                        String kat = rs.getString("nama_kategori");
                                        ayat.put("kategori", kat != null && !kat.isEmpty() ? kat : "Pengeluaran Harian");
                                        String phKet = rs.getString("ph_keterangan");
                                        ayat.put("keperluan", phKet != null && !phKet.isEmpty() ? phKet : jKet);
                                        ayat.put("petugas", rs.getString("nama_petugas") != null ? rs.getString("nama_petugas") : "");
                                        ayat.put("akun_bayar", rs.getString("ph_kwitansi") != null ? rs.getString("ph_kwitansi") : "");
                                    }
                                    arrAyat.put(ayat);
                                }
                            }
                        }

                        final String jsonStr = arrAyat.toString().replace("\u2028", "\\u2028").replace("\u2029", "\\u2029");
                        final int fPage = page;
                        final int fTotHal = totalHalaman;
                        final int fTotTrx = totalTransaksi;

                        Platform.runLater(() -> {
                            if (engine != null) {
                                engine.executeScript("terimaJurnalSumber('" + sumber + "', '" + escapeJS(jsonStr) + "');");
                                engine.executeScript("setInfoHalamanSumber('" + sumber + "', " + fPage + ", " + fTotHal + ", " + fTotTrx + ");");
                            }
                        });

                    } catch (Throwable e) {
                        System.err.println("loadJurnalSumber error: " + e.getMessage());
                        e.printStackTrace();
                        final String errMsg = e.getMessage() != null ? e.getMessage() : "Unknown Error";
                        Platform.runLater(() -> {
                            if (engine != null) {
                                engine.executeScript("showAppError('Gagal Query Jurnal Sumber', '" + escapeJS(errMsg) + "');");
                            }
                        });
                    }
                }
            });
        }

        public void validasiJurnalSumber(String noJurnalsJson) {
            dbExecutor.submit(() -> {
                synchronized (dbLock) {
                    if (!isProcessing.compareAndSet(false, true)) {
                        Platform.runLater(() -> {
                            if (engine != null) {
                                engine.executeScript("terimaHasilValidasiSumber(false, 'Sedang memproses validasi transaksi lain, mohon tunggu...', 0);");
                            }
                        });
                        return;
                    }

                    try {
                        String userValidasi = akses.getkode();
                        if (userValidasi == null || userValidasi.trim().isEmpty()) {
                            Platform.runLater(() -> {
                                if (engine != null) {
                                    engine.executeScript("terimaHasilValidasiSumber(false, 'User tidak teridentifikasi', 0);");
                                }
                            });
                            return;
                        }

                        if (!akses.getposting_jurnal() && !akses.getjurnal_harian()) {
                            Platform.runLater(() -> {
                                if (engine != null) {
                                    engine.executeScript("terimaHasilValidasiSumber(false, 'Anda tidak memiliki hak akses untuk memposting jurnal', 0);");
                                }
                            });
                            return;
                        }

                        JSONArray arr = new JSONArray(noJurnalsJson);
                        if (arr.length() == 0) {
                            Platform.runLater(() -> {
                                if (engine != null) {
                                    engine.executeScript("terimaHasilValidasiSumber(false, 'Tidak ada jurnal dipilih', 0);");
                                }
                            });
                            return;
                        }

                        LinkedHashSet<String> validJurnals = new LinkedHashSet<>();
                        for (int i = 0; i < arr.length(); i++) {
                            String jrn = arr.optString(i, "").trim();
                            if (!jrn.isEmpty()) {
                                validJurnals.add(jrn);
                            }
                        }

                        if (validJurnals.isEmpty()) {
                            Platform.runLater(() -> {
                                if (engine != null) {
                                    engine.executeScript("terimaHasilValidasiSumber(false, 'Tidak ada jurnal valid untuk diposting', 0);");
                                }
                            });
                            return;
                        }

                        List<String> validJurnalsList = new ArrayList<>(validJurnals);
                        int batchSize = 500;

                        // 1. Cek apakah sudah posted di jurnal_posting_status atau validasi_jurnal_harian
                        LinkedHashSet<String> sudahPosted = new LinkedHashSet<>();
                        for (int i = 0; i < validJurnalsList.size(); i += batchSize) {
                            int end = Math.min(i + batchSize, validJurnalsList.size());
                            List<String> batchList = validJurnalsList.subList(i, end);
                            StringBuilder batchIn = buildBatchPlaceholders(batchList.size());
                            String sqlCek = "SELECT no_jurnal FROM jurnal_posting_status WHERE status_posting = 'POSTED' AND no_jurnal IN (" + batchIn + ") "
                                    + "UNION SELECT no_jurnal FROM validasi_jurnal_harian WHERE no_jurnal IN (" + batchIn + ")";
                            try ( PreparedStatement ps = koneksi.prepareStatement(sqlCek)) {
                                for (int j = 0; j < batchList.size(); j++) {
                                    ps.setString(j + 1, batchList.get(j));
                                    ps.setString(batchList.size() + j + 1, batchList.get(j));
                                }
                                try ( ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                        sudahPosted.add(rs.getString("no_jurnal"));
                                    }
                                }
                            }
                        }

                        List<String> jurnalsToPost = new ArrayList<>();
                        for (String j : validJurnalsList) {
                            if (!sudahPosted.contains(j)) {
                                jurnalsToPost.add(j);
                            }
                        }

                        if (jurnalsToPost.isEmpty()) {
                            final int skipped = sudahPosted.size();
                            Platform.runLater(() -> {
                                if (engine != null) {
                                    engine.executeScript("terimaHasilValidasiSumber(true, 'Semua jurnal terpilih (" + skipped + ") sudah berstatus Posted sebelumnya.', 0);");
                                }
                            });
                            return;
                        }

                        // 2. Cek eksistensi header di jurnal
                        LinkedHashSet<String> jurnalAda = new LinkedHashSet<>();
                        for (int i = 0; i < jurnalsToPost.size(); i += batchSize) {
                            int end = Math.min(i + batchSize, jurnalsToPost.size());
                            List<String> batchList = jurnalsToPost.subList(i, end);
                            StringBuilder batchIn = buildBatchPlaceholders(batchList.size());
                            try ( PreparedStatement ps = koneksi.prepareStatement("SELECT no_jurnal FROM jurnal WHERE no_jurnal IN (" + batchIn + ")")) {
                                for (int j = 0; j < batchList.size(); j++) {
                                    ps.setString(j + 1, batchList.get(j));
                                }
                                try ( ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                        jurnalAda.add(rs.getString("no_jurnal"));
                                    }
                                }
                            }
                        }

                        List<String> jurnalHilang = new ArrayList<>();
                        for (String j : jurnalsToPost) {
                            if (!jurnalAda.contains(j)) {
                                jurnalHilang.add(j);
                            }
                        }
                        if (!jurnalHilang.isEmpty()) {
                            final String errMsg = "Validasi Gagal: Jurnal tidak ditemukan di tabel jurnal utama:\n" + formatListSample(jurnalHilang, 5);
                            Platform.runLater(() -> {
                                if (engine != null) {
                                    engine.executeScript("terimaHasilValidasiSumber(false, '" + escapeJS(errMsg) + "', 0);");
                                }
                            });
                            return;
                        }

                        // 3. Cek rincian detailjurnal minimal 2 baris
                        LinkedHashMap<String, Integer> mapJumlahDetail = new LinkedHashMap<>();
                        for (int i = 0; i < jurnalsToPost.size(); i += batchSize) {
                            int end = Math.min(i + batchSize, jurnalsToPost.size());
                            List<String> batchList = jurnalsToPost.subList(i, end);
                            StringBuilder batchIn = buildBatchPlaceholders(batchList.size());
                            String sqlCountDet = "SELECT no_jurnal, COUNT(kd_rek) AS jml FROM detailjurnal WHERE no_jurnal IN (" + batchIn + ") GROUP BY no_jurnal";
                            try ( PreparedStatement ps = koneksi.prepareStatement(sqlCountDet)) {
                                for (int j = 0; j < batchList.size(); j++) {
                                    ps.setString(j + 1, batchList.get(j));
                                }
                                try ( ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                        mapJumlahDetail.put(rs.getString("no_jurnal"), rs.getInt("jml"));
                                    }
                                }
                            }
                        }

                        List<String> kurangDetail = new ArrayList<>();
                        for (String j : jurnalsToPost) {
                            int jml = mapJumlahDetail.getOrDefault(j, 0);
                            if (jml < 2) {
                                kurangDetail.add(j + " (" + jml + " baris)");
                            }
                        }
                        if (!kurangDetail.isEmpty()) {
                            final String errMsg = "Validasi Gagal: Jurnal tidak memiliki rincian atau kurang dari 2 baris rekening:\n" + formatListSample(kurangDetail, 5);
                            Platform.runLater(() -> {
                                if (engine != null) {
                                    engine.executeScript("terimaHasilValidasiSumber(false, '" + escapeJS(errMsg) + "', 0);");
                                }
                            });
                            return;
                        }

                        // 4. Cek rekening valid di master rekening
                        List<String> akunHilang = new ArrayList<>();
                        for (int i = 0; i < jurnalsToPost.size(); i += batchSize) {
                            int end = Math.min(i + batchSize, jurnalsToPost.size());
                            List<String> batchList = jurnalsToPost.subList(i, end);
                            StringBuilder batchIn = buildBatchPlaceholders(batchList.size());
                            String sqlRek = "SELECT DISTINCT dj.kd_rek FROM detailjurnal dj LEFT JOIN rekening r ON dj.kd_rek = r.kd_rek "
                                    + "WHERE dj.no_jurnal IN (" + batchIn + ") AND r.kd_rek IS NULL";
                            try ( PreparedStatement ps = koneksi.prepareStatement(sqlRek)) {
                                for (int j = 0; j < batchList.size(); j++) {
                                    ps.setString(j + 1, batchList.get(j));
                                }
                                try ( ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                        akunHilang.add(rs.getString("kd_rek"));
                                    }
                                }
                            }
                        }
                        if (!akunHilang.isEmpty()) {
                            final String errMsg = "Validasi Gagal: Kode rekening berikut tidak terdaftar di master rekening:\n" + formatListSample(akunHilang, 5);
                            Platform.runLater(() -> {
                                if (engine != null) {
                                    engine.executeScript("terimaHasilValidasiSumber(false, '" + escapeJS(errMsg) + "', 0);");
                                }
                            });
                            return;
                        }

                        // 5. Cek nominal negatif
                        List<String> nominalNegatif = new ArrayList<>();
                        for (int i = 0; i < jurnalsToPost.size(); i += batchSize) {
                            int end = Math.min(i + batchSize, jurnalsToPost.size());
                            List<String> batchList = jurnalsToPost.subList(i, end);
                            StringBuilder batchIn = buildBatchPlaceholders(batchList.size());
                            String sqlNeg = "SELECT DISTINCT no_jurnal FROM detailjurnal WHERE no_jurnal IN (" + batchIn + ") AND (debet < 0 OR kredit < 0)";
                            try ( PreparedStatement ps = koneksi.prepareStatement(sqlNeg)) {
                                for (int j = 0; j < batchList.size(); j++) {
                                    ps.setString(j + 1, batchList.get(j));
                                }
                                try ( ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                        nominalNegatif.add(rs.getString("no_jurnal"));
                                    }
                                }
                            }
                        }
                        if (!nominalNegatif.isEmpty()) {
                            final String errMsg = "Validasi Gagal: Ditemukan nominal debit atau kredit negatif pada rincian jurnal:\n" + formatListSample(nominalNegatif, 5);
                            Platform.runLater(() -> {
                                if (engine != null) {
                                    engine.executeScript("terimaHasilValidasiSumber(false, '" + escapeJS(errMsg) + "', 0);");
                                }
                            });
                            return;
                        }

                        // 6. Cek balance per jurnal dalam sen
                        List<String> unbalance = new ArrayList<>();
                        List<String> nominalNol = new ArrayList<>();
                        DecimalFormat df = new DecimalFormat("#,##0.00");
                        for (int i = 0; i < jurnalsToPost.size(); i += batchSize) {
                            int end = Math.min(i + batchSize, jurnalsToPost.size());
                            List<String> batchList = jurnalsToPost.subList(i, end);
                            StringBuilder batchIn = buildBatchPlaceholders(batchList.size());
                            try ( PreparedStatement ps = koneksi.prepareStatement(
                                    "SELECT no_jurnal, SUM(debet) AS td, SUM(kredit) AS tk FROM detailjurnal WHERE no_jurnal IN (" + batchIn + ") GROUP BY no_jurnal")) {
                                for (int j = 0; j < batchList.size(); j++) {
                                    ps.setString(j + 1, batchList.get(j));
                                }
                                try ( ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                        String nj = rs.getString("no_jurnal");
                                        double td = rs.getDouble("td");
                                        double tk = rs.getDouble("tk");
                                        long d = Math.round(td * 100);
                                        long k = Math.round(tk * 100);
                                        if (d == 0 && k == 0) {
                                            nominalNol.add(nj);
                                        } else if (d != k) {
                                            double selisih = Math.abs((d - k) / 100.0);
                                            unbalance.add(nj + " (Debit: " + df.format(td) + ", Kredit: " + df.format(tk) + ", Selisih: " + df.format(selisih) + ")");
                                        }
                                    }
                                }
                            }
                        }

                        if (!nominalNol.isEmpty()) {
                            final String errMsg = "Validasi Gagal: Jurnal memiliki total debit dan kredit 0:\n" + formatListSample(nominalNol, 5);
                            Platform.runLater(() -> {
                                if (engine != null) {
                                    engine.executeScript("terimaHasilValidasiSumber(false, '" + escapeJS(errMsg) + "', 0);");
                                }
                            });
                            return;
                        }

                        if (!unbalance.isEmpty()) {
                            final String errMsg = "Validasi Gagal: Jurnal tidak balance (Total Debit != Total Kredit):\n" + formatListSample(unbalance, 5);
                            Platform.runLater(() -> {
                                if (engine != null) {
                                    engine.executeScript("terimaHasilValidasiSumber(false, '" + escapeJS(errMsg) + "', 0);");
                                }
                            });
                            return;
                        }

                        // 7. Simpan status POSTED ke jurnal_posting_status & validasi_jurnal_harian dalam transaksi DB
                        LinkedHashSet<String> existingDraft = new LinkedHashSet<>();
                        for (int i = 0; i < jurnalsToPost.size(); i += batchSize) {
                            int end = Math.min(i + batchSize, jurnalsToPost.size());
                            List<String> batchList = jurnalsToPost.subList(i, end);
                            StringBuilder batchIn = buildBatchPlaceholders(batchList.size());
                            String sqlCekDraft = "SELECT no_jurnal FROM jurnal_posting_status WHERE no_jurnal IN (" + batchIn + ")";
                            try ( PreparedStatement ps = koneksi.prepareStatement(sqlCekDraft)) {
                                for (int j = 0; j < batchList.size(); j++) {
                                    ps.setString(j + 1, batchList.get(j));
                                }
                                try ( ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                        existingDraft.add(rs.getString("no_jurnal"));
                                    }
                                }
                            }
                        }

                        LinkedHashSet<String> existingVJH = new LinkedHashSet<>();
                        for (int i = 0; i < jurnalsToPost.size(); i += batchSize) {
                            int end = Math.min(i + batchSize, jurnalsToPost.size());
                            List<String> batchList = jurnalsToPost.subList(i, end);
                            StringBuilder batchIn = buildBatchPlaceholders(batchList.size());
                            String sqlCekVJH = "SELECT no_jurnal FROM validasi_jurnal_harian WHERE no_jurnal IN (" + batchIn + ")";
                            try ( PreparedStatement ps = koneksi.prepareStatement(sqlCekVJH)) {
                                for (int j = 0; j < batchList.size(); j++) {
                                    ps.setString(j + 1, batchList.get(j));
                                }
                                try ( ResultSet rs = ps.executeQuery()) {
                                    while (rs.next()) {
                                        existingVJH.add(rs.getString("no_jurnal"));
                                    }
                                }
                            }
                        }

                        int count = 0;
                        try {
                            koneksi.setAutoCommit(false);

                            // 7a. Insert baru ke jurnal_posting_status
                            String sqlInsJps = "INSERT INTO jurnal_posting_status (no_jurnal, status_posting, tanggal_validasi, user_validasi, tanggal_posting, user_posting, catatan) "
                                    + "VALUES (?, 'POSTED', NOW(), ?, NOW(), ?, 'Posted via DlgPostingJurnalWeb Tab C/D')";
                            try ( PreparedStatement psIns = koneksi.prepareStatement(sqlInsJps)) {
                                int localBatch = 0;
                                for (String nj : jurnalsToPost) {
                                    if (existingDraft.contains(nj)) {
                                        continue;
                                    }
                                    psIns.setString(1, nj);
                                    psIns.setString(2, userValidasi);
                                    psIns.setString(3, userValidasi);
                                    psIns.addBatch();
                                    localBatch++;
                                    if (localBatch % batchSize == 0) {
                                        psIns.executeBatch();
                                        localBatch = 0;
                                    }
                                }
                                if (localBatch > 0) {
                                    psIns.executeBatch();
                                }
                            }

                            // 7b. Update draft di jurnal_posting_status
                            String sqlUpdJps = "UPDATE jurnal_posting_status SET status_posting = 'POSTED', tanggal_validasi = NOW(), user_validasi = ?, "
                                    + "tanggal_posting = NOW(), user_posting = ?, catatan = 'Posted via DlgPostingJurnalWeb Tab C/D' "
                                    + "WHERE no_jurnal = ? AND status_posting != 'POSTED'";
                            try ( PreparedStatement psUpd = koneksi.prepareStatement(sqlUpdJps)) {
                                int localBatch = 0;
                                for (String nj : jurnalsToPost) {
                                    if (!existingDraft.contains(nj)) {
                                        continue;
                                    }
                                    psUpd.setString(1, userValidasi);
                                    psUpd.setString(2, userValidasi);
                                    psUpd.setString(3, nj);
                                    psUpd.addBatch();
                                    localBatch++;
                                    if (localBatch % batchSize == 0) {
                                        psUpd.executeBatch();
                                        localBatch = 0;
                                    }
                                }
                                if (localBatch > 0) {
                                    psUpd.executeBatch();
                                }
                            }

                            // 7c. Mirror insert ke validasi_jurnal_harian untuk kompatibilitas Khanza lama
                            String sqlVjh = "INSERT INTO validasi_jurnal_harian (no_jurnal, tanggal_validasi, user_validasi) VALUES (?, NOW(), ?)";
                            try ( PreparedStatement psVjh = koneksi.prepareStatement(sqlVjh)) {
                                int localBatch = 0;
                                for (String nj : jurnalsToPost) {
                                    if (existingVJH.contains(nj)) {
                                        continue;
                                    }
                                    psVjh.setString(1, nj);
                                    psVjh.setString(2, userValidasi);
                                    psVjh.addBatch();
                                    localBatch++;
                                    if (localBatch % batchSize == 0) {
                                        psVjh.executeBatch();
                                        localBatch = 0;
                                    }
                                }
                                if (localBatch > 0) {
                                    psVjh.executeBatch();
                                }
                            }

                            koneksi.commit();
                            count = jurnalsToPost.size();
                        } catch (Exception e) {
                            try {
                                koneksi.rollback();
                            } catch (Exception ex) {
                            }
                            if (e instanceof java.sql.SQLIntegrityConstraintViolationException || (e.getMessage() != null && e.getMessage().toLowerCase().contains("duplicate"))) {
                                throw new Exception("Sebagian jurnal sudah divalidasi oleh pengguna lain, silakan refresh data.");
                            }
                            throw e;
                        } finally {
                            try {
                                koneksi.setAutoCommit(true);
                            } catch (Exception ex) {
                            }
                        }

                        final int total = count;
                        final int skipped = sudahPosted.size();
                        Platform.runLater(() -> {
                            if (engine != null) {
                                String msg = "Berhasil memposting " + total + " jurnal.";
                                if (skipped > 0) {
                                    msg += " (" + skipped + " jurnal dilewati karena sudah Posted)";
                                }
                                engine.executeScript("if (typeof terimaHasilValidasiSumber === 'function') { terimaHasilValidasiSumber(true, '" + escapeJS(msg) + "', " + total + "); }");
                            }
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                        final String errMsg = e.getMessage() != null ? e.getMessage() : "Unknown Error";
                        Platform.runLater(() -> {
                            if (engine != null) {
                                engine.executeScript("if (typeof terimaHasilValidasiSumber === 'function') { terimaHasilValidasiSumber(false, '" + escapeJS(errMsg) + "', 0); }");
                            }
                        });
                    } finally {
                        isProcessing.set(false);
                    }
                }
            });
        }

        public String getUserInfo() {
            return akses.getkode();
        }

        public void toggleMaximize() {
            DlgPostingJurnalWeb.this.toggleMaximize();
        }

        public void minimize() {
            DlgPostingJurnalWeb.this.minimize();
        }

        public void keluarForm() {
            SwingUtilities.invokeLater(() -> dispose());
        }

        private void trackSql(String pesan) {
            try {
                String sql = "INSERT INTO trackersql (tanggal, sqle, usere) VALUES (NOW(), ?, ?)";
                try ( PreparedStatement ps = koneksi.prepareStatement(sql)) {
                    String host = "";
                    try {
                        java.net.InetAddress addr = java.net.InetAddress.getLocalHost();
                        host = "[" + addr.getHostName() + " / " + addr.getHostAddress() + "] ";
                    } catch (Exception e) {
                    }
                    ps.setString(1, host + pesan);
                    ps.setString(2, akses.getkode());
                    ps.executeUpdate();
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
        setResizable(true);
        setSize(1200, 720);
        setLocationRelativeTo(null);
        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                "::[ Posting Jurnal & Validasi ]::",
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
