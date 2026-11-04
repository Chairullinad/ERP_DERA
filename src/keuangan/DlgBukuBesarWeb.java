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

/**
 * Modul Buku Besar Baru (buku_besar_new) dengan Running Balance SAK
 */
public class DlgBukuBesarWeb extends javax.swing.JDialog {

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final Connection koneksi = koneksiDB.condb();
    private final DlgCariRekeningTahun rekening = new DlgCariRekeningTahun(null, false);
    private final DlgRekeningTahun rekeningTahun = new DlgRekeningTahun(null, false);

    private WebView webView;
    private WebEngine engine;
    private JFXPanel fxPanel;
    private boolean isWebViewInitialized = false;
    private String lastBukuBesarData = "";
    private String lastRingkasanCOAData = "";

    private final ExecutorService dbExecutor = Executors.newFixedThreadPool(8, r -> {
        Thread t = new Thread(r, "Buku-Besar-Worker");
        t.setDaemon(true);
        return t;
    });

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
                        setSize(1300, 750);
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

    public DlgBukuBesarWeb(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        rekening.addWindowListener(new WindowListener() {
            @Override public void windowOpened(WindowEvent e) {}
            @Override public void windowClosing(WindowEvent e) {}
            @Override public void windowClosed(WindowEvent e) {
                if (rekening.getTabel().getSelectedRow() != -1) {
                    String kd = rekening.getTabel().getValueAt(rekening.getTabel().getSelectedRow(), 1).toString();
                    String nm = rekening.getTabel().getValueAt(rekening.getTabel().getSelectedRow(), 2).toString();
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("if (typeof setAkunDariDialog === 'function') { setAkunDariDialog('" + escapeJS(kd) + "', '" + escapeJS(nm) + "'); }");
                        }
                    });
                }
            }
            @Override public void windowIconified(WindowEvent e) {}
            @Override public void windowDeiconified(WindowEvent e) {}
            @Override public void windowActivated(WindowEvent e) {}
            @Override public void windowDeactivated(WindowEvent e) {}
        });

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
                        javax.swing.JOptionPane.showMessageDialog(DlgBukuBesarWeb.this, data, "Informasi", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                    });
                });

                String html = getHTMLContent();
                engine.loadContent(html);

                engine.getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        try {
                            JSObject window = (JSObject) engine.executeScript("window");
                            window.setMember("javaApp", new JavaBridge());
                            // Memicu init() begitu bridge siap terpasang
                            engine.executeScript("if (typeof init === 'function') init();");
                        } catch (Throwable e) {
                            System.err.println("FATAL Error initializing WebView JavaBridge in BukuBesar: " + e.getMessage());
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
                System.err.println("Error WebView BukuBesar: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private String getHTMLContent() {
        try {
            InputStream is = getClass().getResourceAsStream("/resources/buku_besar.html");
            if (is == null) is = getClass().getClassLoader().getResourceAsStream("resources/buku_besar.html");
            if (is == null) {
                return "<html><body><h1>File buku_besar.html tidak ditemukan</h1></body></html>";
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

        public String getTanggalHariIni() {
            return new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        }

        public void bukaCariRekening() {
            SwingUtilities.invokeLater(() -> {
                rekening.tampil();
                rekening.isCek();
                rekening.emptTeks();
                rekening.setSize(1050, 680);
                rekening.setLocationRelativeTo(DlgBukuBesarWeb.this);
                rekening.setVisible(true);
            });
        }

        /**
         * TAB A: Ringkasan seluruh COA tanpa N+1 query.
         * Mengambil seluruh rekening aktif, saldo awal dari rekeningtahun + mutasi POSTED sebelum tgl1,
         * serta mutasi periode tgl1 s/d tgl2.
         * Diurutkan dari saldo akhir numerik terbesar ke terkecil.
         */
        public void loadRingkasanCOA(String tglAwal, String tglAkhir, String keyword) {
            dbExecutor.submit(() -> {
                JSONObject response = new JSONObject();
                JSONArray coaArray = new JSONArray();
                double grandSaldoAwal = 0;
                double grandTotalDebet = 0;
                double grandTotalKredit = 0;
                double grandSaldoAkhir = 0;

                try {
                    String tgl1 = formatTglSql(tglAwal);
                    String tgl2 = formatTglSql(tglAkhir);
                    String tahun = tgl1.length() >= 4 ? tgl1.substring(0, 4) : "2026";
                    String awalTahun = tahun + "-01-01";

                    // Query efisien 1-pass: rekening + rekeningtahun + agregasi mutasi POSTED sebelum periode + periode aktif
                    StringBuilder sql = new StringBuilder();
                    sql.append("SELECT r.kd_rek, r.nm_rek, r.balance, r.tipe, ")
                       .append("COALESCE(rt.saldo_awal, 0) AS saldo_awal_tahun, ")
                       .append("COALESCE(prev.sum_d_prev, 0) AS d_prev, ")
                       .append("COALESCE(prev.sum_k_prev, 0) AS k_prev, ")
                       .append("COALESCE(cur.sum_d_cur, 0) AS d_cur, ")
                       .append("COALESCE(cur.sum_k_cur, 0) AS k_cur ")
                       .append("FROM rekening r ")
                       .append("LEFT JOIN rekeningtahun rt ON r.kd_rek = rt.kd_rek AND rt.thn = ? ")
                       .append("LEFT JOIN ( ")
                       .append("    SELECT dj.kd_rek, SUM(dj.debet) AS sum_d_prev, SUM(dj.kredit) AS sum_k_prev ")
                       .append("    FROM jurnal j ")
                       .append("    INNER JOIN detailjurnal dj ON j.no_jurnal = dj.no_jurnal ")
                       .append("    INNER JOIN jurnal_posting_status jps ON j.no_jurnal = jps.no_jurnal ")
                       .append("    WHERE jps.status_posting = 'POSTED' ")
                       .append("      AND j.tgl_jurnal >= ? AND j.tgl_jurnal < ? ")
                       .append("    GROUP BY dj.kd_rek ")
                       .append(") prev ON r.kd_rek = prev.kd_rek ")
                       .append("LEFT JOIN ( ")
                       .append("    SELECT dj.kd_rek, SUM(dj.debet) AS sum_d_cur, SUM(dj.kredit) AS sum_k_cur ")
                       .append("    FROM jurnal j ")
                       .append("    INNER JOIN detailjurnal dj ON j.no_jurnal = dj.no_jurnal ")
                       .append("    INNER JOIN jurnal_posting_status jps ON j.no_jurnal = jps.no_jurnal ")
                       .append("    WHERE jps.status_posting = 'POSTED' ")
                       .append("      AND j.tgl_jurnal BETWEEN ? AND ? ")
                       .append("    GROUP BY dj.kd_rek ")
                       .append(") cur ON r.kd_rek = cur.kd_rek ");

                    boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());
                    if (hasKeyword) {
                        sql.append("WHERE (r.kd_rek LIKE ? OR r.nm_rek LIKE ?) ");
                    }

                    java.util.List<JSONObject> listCoa = new java.util.ArrayList<>();

                    try (PreparedStatement ps = koneksi.prepareStatement(sql.toString())) {
                        int pIdx = 1;
                        ps.setString(pIdx++, tahun);
                        ps.setString(pIdx++, awalTahun);
                        ps.setString(pIdx++, tgl1);
                        ps.setString(pIdx++, tgl1);
                        ps.setString(pIdx++, tgl2);
                        if (hasKeyword) {
                            String kw = "%" + keyword.trim() + "%";
                            ps.setString(pIdx++, kw);
                            ps.setString(pIdx++, kw);
                        }

                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                String kd = rs.getString("kd_rek");
                                String nm = rs.getString("nm_rek");
                                String bal = rs.getString("balance");
                                if (bal == null || bal.trim().isEmpty()) bal = "D";
                                else bal = bal.trim().toUpperCase();

                                double saldoAwalTahun = rs.getDouble("saldo_awal_tahun");
                                double dPrev = rs.getDouble("d_prev");
                                double kPrev = rs.getDouble("k_prev");
                                double dCur = rs.getDouble("d_cur");
                                double kCur = rs.getDouble("k_cur");

                                double saldoAwal;
                                if ("K".equals(bal)) {
                                    saldoAwal = saldoAwalTahun + (kPrev - dPrev);
                                } else {
                                    saldoAwal = saldoAwalTahun + (dPrev - kPrev);
                                }

                                double saldoAkhir;
                                if ("K".equals(bal)) {
                                    saldoAkhir = saldoAwal + (kCur - dCur);
                                } else {
                                    saldoAkhir = saldoAwal + (dCur - kCur);
                                }

                                JSONObject row = new JSONObject();
                                row.put("kd_rek", kd != null ? kd : "");
                                row.put("nm_rek", nm != null ? nm : "");
                                row.put("balance", bal);
                                row.put("tipe", rs.getString("tipe") != null ? rs.getString("tipe") : "");
                                row.put("saldo_awal", saldoAwal);
                                row.put("total_debet", dCur);
                                row.put("total_kredit", kCur);
                                row.put("saldo_akhir", saldoAkhir);

                                listCoa.add(row);

                                grandSaldoAwal += saldoAwal;
                                grandTotalDebet += dCur;
                                grandTotalKredit += kCur;
                                grandSaldoAkhir += saldoAkhir;
                            }
                        }
                    }

                    // Urutkan berdasarkan saldo akhir numerik terbesar ke terkecil (descending)
                    listCoa.sort((a, b) -> {
                        double saA = a.optDouble("saldo_akhir", 0.0);
                        double saB = b.optDouble("saldo_akhir", 0.0);
                        if (Double.compare(saB, saA) != 0) {
                            return Double.compare(saB, saA);
                        }
                        return a.optString("kd_rek", "").compareTo(b.optString("kd_rek", ""));
                    });

                    // Beri nomor urut
                    int noUrut = 1;
                    for (JSONObject r : listCoa) {
                        r.put("no", noUrut++);
                        coaArray.put(r);
                    }

                    response.put("sukses", true);
                    response.put("tgl_awal", tgl1);
                    response.put("tgl_akhir", tgl2);
                    response.put("total_coa", listCoa.size());
                    response.put("grand_saldo_awal", grandSaldoAwal);
                    response.put("grand_total_debet", grandTotalDebet);
                    response.put("grand_total_kredit", grandTotalKredit);
                    response.put("grand_saldo_akhir", grandSaldoAkhir);
                    response.put("ringkasan", coaArray);

                } catch (Throwable e) {
                    System.err.println("loadRingkasanCOA error: " + e.getMessage());
                    e.printStackTrace();
                    final String errMsg = e.getMessage() != null ? e.getMessage().replace("'", "\\'").replace("\n", "\\n") : "Unknown Error";
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("if (typeof showAppError === 'function') { showAppError('Gagal Memuat Ringkasan COA (Buku Besar)', '" + errMsg + "'); } else { alert('Error: " + errMsg + "'); }");
                        }
                    });
                    return;
                }

                lastRingkasanCOAData = response.toString();
                Platform.runLater(() -> {
                    if (engine != null) {
                        engine.executeScript("if (typeof terimaRingkasanCOA === 'function') { terimaRingkasanCOA(); } else { console.error('terimaRingkasanCOA not found!'); }");
                    }
                });
            });
        }

        public String getRingkasanCOAData() {
            return lastRingkasanCOAData;
        }

        public int getRingkasanCOACount() {
            if (lastRingkasanCOAData == null) return 0;
            return (int) Math.ceil((double) lastRingkasanCOAData.length() / 32000.0);
        }

        public String getRingkasanCOAChunk(int index) {
            if (lastRingkasanCOAData == null) return "";
            int start = index * 32000;
            if (start >= lastRingkasanCOAData.length()) return "";
            int end = Math.min(start + 32000, lastRingkasanCOAData.length());
            return lastRingkasanCOAData.substring(start, end);
        }

        /**
         * TAB B: Detail transaksi rekening terpilih berstatus POSTED.
         * Menggunakan tabel resmi: jurnal, detailjurnal, jurnal_posting_status (POSTED).
         * Saldo awal dan saldo akhir konsisten dengan Tab A.
         */
        public void loadDetailBukuBesar(String kdRek, String tglAwal, String tglAkhir, String keyword) {
            dbExecutor.submit(() -> {
                JSONObject response = new JSONObject();
                JSONArray mutasiArray = new JSONArray();
                double saldoAwal = 0;
                double totalDebet = 0;
                double totalKredit = 0;
                String balanceType = "D";
                String nmRek = "";

                try {
                    String tgl1 = formatTglSql(tglAwal);
                    String tgl2 = formatTglSql(tglAkhir);
                    String tahun = tgl1.length() >= 4 ? tgl1.substring(0, 4) : "2026";
                    String awalTahun = tahun + "-01-01";

                    // 1. Ambil info saldo normal dan nama akun
                    if (kdRek != null && !kdRek.trim().isEmpty()) {
                        String sqlRek = "SELECT nm_rek, balance, tipe FROM rekening WHERE kd_rek = ?";
                        try (PreparedStatement psRek = koneksi.prepareStatement(sqlRek)) {
                            psRek.setString(1, kdRek);
                            try (ResultSet rsRek = psRek.executeQuery()) {
                                if (rsRek.next()) {
                                    nmRek = rsRek.getString("nm_rek");
                                    String b = rsRek.getString("balance");
                                    if (b != null && !b.isEmpty()) balanceType = b.toUpperCase();
                                }
                            }
                        }

                        // Saldo awal awal tahun dari rekeningtahun
                        double saldoAwalTahun = 0;
                        String sqlThn = "SELECT saldo_awal FROM rekeningtahun WHERE kd_rek = ? AND thn = ?";
                        try (PreparedStatement psThn = koneksi.prepareStatement(sqlThn)) {
                            psThn.setString(1, kdRek);
                            psThn.setString(2, tahun);
                            try (ResultSet rsThn = psThn.executeQuery()) {
                                if (rsThn.next()) {
                                    saldoAwalTahun = rsThn.getDouble("saldo_awal");
                                }
                            }
                        }

                        // Mutasi sebelum tgl1 (di tahun berjalan) dari jurnal berstatus POSTED
                        double debetSebelum = 0;
                        double kreditSebelum = 0;
                        String sqlPrev = "SELECT COALESCE(SUM(d.debet),0) as sum_d, COALESCE(SUM(d.kredit),0) as sum_k "
                                + "FROM jurnal j "
                                + "INNER JOIN detailjurnal d ON j.no_jurnal = d.no_jurnal "
                                + "INNER JOIN jurnal_posting_status jps ON j.no_jurnal = jps.no_jurnal "
                                + "WHERE d.kd_rek = ? AND j.tgl_jurnal >= ? AND j.tgl_jurnal < ? "
                                + "  AND jps.status_posting = 'POSTED'";

                        try (PreparedStatement psPrev = koneksi.prepareStatement(sqlPrev)) {
                            psPrev.setString(1, kdRek);
                            psPrev.setString(2, awalTahun);
                            psPrev.setString(3, tgl1);
                            try (ResultSet rsPrev = psPrev.executeQuery()) {
                                if (rsPrev.next()) {
                                    debetSebelum = rsPrev.getDouble("sum_d");
                                    kreditSebelum = rsPrev.getDouble("sum_k");
                                }
                            }
                        }

                        // Hitung Saldo Awal per tanggal tgl1
                        if ("K".equals(balanceType)) {
                            saldoAwal = saldoAwalTahun + (kreditSebelum - debetSebelum);
                        } else {
                            saldoAwal = saldoAwalTahun + (debetSebelum - kreditSebelum);
                        }
                    }

                    // 2. Query transaksi mutasi dari jurnal, detailjurnal, jurnal_posting_status (POSTED)
                    StringBuilder sqlQuery = new StringBuilder();
                    sqlQuery.append("SELECT j.tgl_jurnal as tgl_transaksi, j.jam_jurnal, j.no_jurnal, j.no_bukti, ")
                            .append("j.keterangan, d.kd_rek, ")
                            .append("COALESCE(r.nm_rek, '') as nm_rek, r.balance, ")
                            .append("d.debet, d.kredit ")
                            .append("FROM jurnal j ")
                            .append("INNER JOIN detailjurnal d ON j.no_jurnal = d.no_jurnal ")
                            .append("INNER JOIN jurnal_posting_status jps ON j.no_jurnal = jps.no_jurnal ")
                            .append("LEFT JOIN rekening r ON d.kd_rek = r.kd_rek ")
                            .append("WHERE jps.status_posting = 'POSTED' ")
                            .append("  AND j.tgl_jurnal BETWEEN ? AND ? ");

                    if (kdRek != null && !kdRek.trim().isEmpty()) {
                        sqlQuery.append(" AND d.kd_rek = ? ");
                    }
                    if (keyword != null && !keyword.trim().isEmpty()) {
                        sqlQuery.append(" AND (j.no_jurnal LIKE ? OR j.no_bukti LIKE ? OR j.keterangan LIKE ? OR r.nm_rek LIKE ?) ");
                    }
                    sqlQuery.append("ORDER BY j.tgl_jurnal ASC, j.jam_jurnal ASC, j.no_jurnal ASC");

                    double runningBalance = saldoAwal;

                    try (PreparedStatement ps = koneksi.prepareStatement(sqlQuery.toString())) {
                        int pIdx = 1;
                        ps.setString(pIdx++, tgl1);
                        ps.setString(pIdx++, tgl2);
                        if (kdRek != null && !kdRek.trim().isEmpty()) {
                            ps.setString(pIdx++, kdRek);
                        }
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            String kw = "%" + keyword.trim() + "%";
                            ps.setString(pIdx++, kw);
                            ps.setString(pIdx++, kw);
                            ps.setString(pIdx++, kw);
                            ps.setString(pIdx++, kw);
                        }

                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) {
                                JSONObject row = new JSONObject();
                                String tglTr = rs.getString("tgl_transaksi");
                                String jamTr = rs.getString("jam_jurnal");
                                String noJur = rs.getString("no_jurnal");
                                String noBuk = rs.getString("no_bukti");
                                String kdR = rs.getString("kd_rek");
                                String nmR = rs.getString("nm_rek");
                                String ket = rs.getString("keterangan");

                                row.put("tgl_transaksi", tglTr != null ? (jamTr != null && !jamTr.isEmpty() ? tglTr + " " + jamTr : tglTr) : "-");
                                row.put("no_jurnal", noJur != null ? noJur : "-");
                                row.put("no_bukti", noBuk != null ? noBuk : "-");
                                row.put("kd_rek", kdR != null ? kdR : "");
                                row.put("nm_rek", nmR != null ? nmR : "");
                                row.put("keterangan", ket != null ? ket : "");

                                double d = rs.getDouble("debet");
                                double k = rs.getDouble("kredit");
                                row.put("debet", d);
                                row.put("kredit", k);

                                totalDebet += d;
                                totalKredit += k;

                                String rowBal = rs.getString("balance");
                                if (rowBal == null || rowBal.isEmpty()) rowBal = balanceType;

                                if ("K".equalsIgnoreCase(rowBal)) {
                                    runningBalance += (k - d);
                                } else {
                                    runningBalance += (d - k);
                                }
                                row.put("saldo_berjalan", runningBalance);
                                mutasiArray.put(row);
                            }
                        }
                    }

                    response.put("sukses", true);
                    response.put("kd_rek", kdRek != null ? kdRek : "");
                    response.put("nm_rek", nmRek != null ? nmRek : "");
                    response.put("balance_type", balanceType);
                    response.put("saldo_awal", saldoAwal);
                    response.put("total_debet", totalDebet);
                    response.put("total_kredit", totalKredit);
                    response.put("saldo_akhir", runningBalance);
                    response.put("mutasi", mutasiArray);

                } catch (Throwable e) {
                    System.err.println("loadDetailBukuBesar error: " + e.getMessage());
                    e.printStackTrace();
                    final String errMsg = e.getMessage() != null ? e.getMessage().replace("'", "\\'").replace("\n", "\\n") : "Unknown Error";
                    Platform.runLater(() -> {
                        if (engine != null) {
                            engine.executeScript("if (typeof showAppError === 'function') { showAppError('Gagal Query Database Detail (Buku Besar)', '" + errMsg + "'); } else { alert('Error: " + errMsg + "'); }");
                        }
                    });
                    return;
                }

                lastBukuBesarData = response.toString();
                Platform.runLater(() -> {
                    if (engine != null) {
                        engine.executeScript("if (typeof terimaDetailBukuBesar === 'function') { terimaDetailBukuBesar(); } else if (typeof terimaBukuBesar === 'function') { terimaBukuBesar(); }");
                    }
                });
            });
        }

        // Alias untuk backward compatibility
        public void loadBukuBesar(String kdRek, String tglAwal, String tglAkhir, String keyword) {
            loadDetailBukuBesar(kdRek, tglAwal, tglAkhir, keyword);
        }

        public String getBukuBesarData() {
            return lastBukuBesarData;
        }

        public int getBukuBesarDataCount() {
            if (lastBukuBesarData == null) return 0;
            return (int) Math.ceil((double) lastBukuBesarData.length() / 32000.0);
        }

        public String getBukuBesarDataChunk(int index) {
            if (lastBukuBesarData == null) return "";
            int start = index * 32000;
            if (start >= lastBukuBesarData.length()) return "";
            int end = Math.min(start + 32000, lastBukuBesarData.length());
            return lastBukuBesarData.substring(start, end);
        }

        public void bukaJurnalTransaksi() {
            SwingUtilities.invokeLater(() -> {
                DlgJurnalTransaksiWeb dlg = new DlgJurnalTransaksiWeb(null, false);
                dlg.setSize(1350, 780);
                dlg.setLocationRelativeTo(DlgBukuBesarWeb.this);
                dlg.setVisible(true);
            });
        }

        public void bukaPostingJurnal() {
            SwingUtilities.invokeLater(() -> {
                DlgPostingJurnalWeb dlg = new DlgPostingJurnalWeb(null, false);
                dlg.setSize(1400, 800);
                dlg.setLocationRelativeTo(DlgBukuBesarWeb.this);
                dlg.setVisible(true);
            });
        }

        public void bukaMappingCOA() {
            SwingUtilities.invokeLater(() -> {
                DlgMappingCOAWeb dlg = new DlgMappingCOAWeb(null, false);
                dlg.setSize(1250, 750);
                dlg.setLocationRelativeTo(DlgBukuBesarWeb.this);
                dlg.setVisible(true);
            });
        }

        public void bukaRekeningTahunan() {
            SwingUtilities.invokeLater(() -> {
                rekeningTahun.tampil();
                rekeningTahun.setSize(1050, 680);
                rekeningTahun.setLocationRelativeTo(DlgBukuBesarWeb.this);
                rekeningTahun.setVisible(true);
            });
        }

        public void simpanFileExcel(String csvContent, String defaultFileName) {
            SwingUtilities.invokeLater(() -> {
                try {
                    javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
                    fileChooser.setDialogTitle("Simpan Export Excel Buku Besar");
                    fileChooser.setSelectedFile(new java.io.File(defaultFileName));
                    fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Excel File (*.csv)", "csv"));

                    int userSelection = fileChooser.showSaveDialog(DlgBukuBesarWeb.this);
                    if (userSelection == javax.swing.JFileChooser.APPROVE_OPTION) {
                        java.io.File fileToSave = fileChooser.getSelectedFile();
                        String filePath = fileToSave.getAbsolutePath();
                        if (!filePath.toLowerCase().endsWith(".csv")) {
                            filePath += ".csv";
                        }

                        try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                                new java.io.FileOutputStream(filePath), java.nio.charset.StandardCharsets.UTF_8)) {
                            writer.write(csvContent);
                            writer.flush();
                        }

                        final String savedPath = filePath;
                        javax.swing.JOptionPane.showMessageDialog(DlgBukuBesarWeb.this,
                                "File Excel berhasil disimpan di:\n" + savedPath,
                                "Export Excel Berhasil", javax.swing.JOptionPane.INFORMATION_MESSAGE);

                        Platform.runLater(() -> {
                            if (engine != null) {
                                engine.executeScript("if (typeof showToast === 'function') { showToast('File Excel disimpan di: " + escapeJS(savedPath) + "', 'success'); }");
                            }
                        });
                    }
                } catch (Exception e) {
                    javax.swing.JOptionPane.showMessageDialog(DlgBukuBesarWeb.this,
                            "Gagal menyimpan file Excel: " + e.getMessage(),
                            "Export Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            });
        }

        public void toggleMaximize() {
            DlgBukuBesarWeb.this.toggleMaximize();
        }

        public void minimize() {
            DlgBukuBesarWeb.this.minimize();
        }

        public String getUserInfo() {
            return akses.getkode();
        }

        public void tutup() {
            SwingUtilities.invokeLater(() -> dispose());
        }

        public void keluarForm() {
            SwingUtilities.invokeLater(() -> dispose());
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
        setSize(1300, 750);
        setLocationRelativeTo(null);
        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                "::[ Buku Besar Umum (General Ledger) ]::",
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
