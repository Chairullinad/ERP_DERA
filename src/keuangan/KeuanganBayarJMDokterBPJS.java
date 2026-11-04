package keuangan;

import com.fasterxml.jackson.databind.JsonNode;
import fungsi.KalkulatorJasaMedisDokter;
import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Desktop;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import static javafx.concurrent.Worker.State.FAILED;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import java.util.Locale;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import simrskhanza.DlgCariCaraBayar;

/**
 *
 * @author perpustakaan
 */
public final class KeuanganBayarJMDokterBPJS extends javax.swing.JDialog {

    private final DefaultTableModel tabModeRawatJalan, tabModeRawatInap;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private KalkulatorJasaMedisDokter kalkulator = new KalkulatorJasaMedisDokter();
    private PreparedStatement ps, psrawatjalandr, psrawatjalandrpr, psrawatinapdr, psrawatinapdrpr, psbiayaoperator1, psbiayaoperator2, psbiayaoperator3, psbiayadokter_anak,
            psbiayadokter_anestesi, psdetaillab, psperiksa_lab, psperiksa_radiologi, psperiksa_lab2, psdetaillab2, psperiksa_radiologi2, psbiaya_dokter_pjanak, psbiaya_dokter_umum;
    private ResultSet rs, rsrawatjalandr, rsrawatjalandrpr, rsrawatinapdr, rsrawatinapdrpr, rsbiayaoperator1, rsbiayaoperator2, rsbiayaoperator3, rsbiayadokter_anak,
            rsbiayadokter_anestesi, rsdetaillab, rsperiksa_lab, rsperiksa_radiologi, rsbiaya_dokter_pjanak, rsbiaya_dokter_umum;
    private DlgCariCaraBayar carabayar = new DlgCariCaraBayar(null, false);
    private int row = 0, i = 0, jml = 0, index = 0, countVisite = 0;
    private double total = 0, bayar = 0, totalrawatjalan = 0, totalrawatinap = 0, totallabrawatjalan = 0, totallabrawatinap = 0, totalradrawatjalan = 0, totalradrawatinap = 0, totaloperasirawatjalan = 0, totaloperasirawatinap = 0, totaljmtarif = 0;
    private boolean sukses = true, isEEG = false, isAudiometri = false, isDPJP = false, isDokterUmum = false, isCDL = false, isDoubleLumen = false, isAVShunt = false, isESWL = false, isDP = false, isRaber = false, isTindakan = false, isTerapi = false, isHD = false, isOP = false;
    private double[] piutang, tarifinacbg, selisihrs, jmbpjs, tarifjm, umbal;
    private String[] norawat, tglpiutang, dpjp, raber, terapi, kodedokter, jenis, statusbedah, visit, namadokter, tindakan, hd, noseptemp, jamtemp, norm, pasien, statusrawat, carabayarArray, nokartu, asalperusahaan, nip, nonota, waktugrouping, statusklaim, keterangancob;
    private SwingWorker<List<Object[]>, Void> workerRawatJalan = null;
    private SwingWorker<List<Object[]>, Void> workerRawatInap = null;

    private static class RalanCandidate {

        String noRawat = "";
        String tglPiutang = "";
        String jamReg = "";
        String noSep = "";
        String noRkmMedis = "";
        String nmPasien = "";
        String kdDokter = "";
        String nmDokter = "";
        double totalPiutang = 0;
        double umbal = 0;
        String statusKlaim = "";
        String kdDpjp = "";
        String kdPoli = "";
        String nmPoli = "";
        String ketCob = "-";
        String ketTindakan = "-";
        boolean isDPJP = false;
        boolean hasExplicitPeriksa = false;
        boolean isRaber = false;
        boolean isTindakan = false;
        boolean isPeriksa = false;
        boolean isTerapi = false;
        boolean isHD = false;
        boolean isDoubleLumen = false;
        boolean isDokterUmum = false;
        boolean isRadiologi = false;
        boolean isEEG = false;
        boolean isAudiometri = false;
        boolean isAVShunt = false;
        boolean isCDL = false;
        boolean isESWL = false;
        boolean isKFR = false;
        double totalTarifJm = 0;
        double calculatedJm = 0;
    }

    private static class RanapSepGroup {
        String noSep = "";
        String tglPiutang = "";
        String jamReg = "";
        String statusKlaim = "";
        String ketCob = "-";
        String dpjpSep = "";
        String primaryDpjpSimrs = "";
        double umbal = 0.0;
        double uangmuka = 0.0;

        Set<String> noRms = new HashSet<>();
        Set<String> noRawats = new HashSet<>();

        Map<String, RanapDoctorInfo> doctorMap = new LinkedHashMap<>();

        boolean hasOperation = false;
        boolean isPostSC = false;
        boolean hasDoubleLumen = false;
        boolean hasAVShunt = false;
        Set<String> operators = new LinkedHashSet<>();
        String dokterAnestesi = "";
        String dokterAnak = "";
    }

    private static class RanapDoctorInfo {
        String kdDokter = "";
        String nmDokter = "";
        String noRawat = "";
        String noRkmMedis = "";
        String nmPasien = "";
        double totalPiutang = 0.0;
        double tarifVisite = 0.0;
        double opTarif = 0.0;
        int visitCount = 0;
        boolean hasVisit = false;
        boolean hasOperation = false;
        boolean isOperator = false;
        boolean isAnestesi = false;
        boolean isDokterAnak = false;
        boolean isDpjpSimrs = false;
        boolean isDpjpSep = false;
        boolean isDokterUmum = false;
        boolean isRadiologi = false;
        String ketTindakan = "-";
        double calculatedJm = 0.0;
        String jenisPctStr = "0";
    }
    private boolean[] pilih;
    private volatile boolean isRecalculating = false;
    private volatile boolean isLoadingData = false;
    private WebEngine engine;
    private DlgBilingRanap bilranap;
//    private DlgBilingRanap billing ;
    private KeuanganCariBayarJMDokter form;
    private DlgSetAturanJMBPJS dlgAturanJM;
    // private KeuanganCariBayarJMDokter form = new KeuanganCariBayarJMDokter(null, false);
    //private final DlgBilingRanap bilranap = new DlgBilingRanap(null, false);
    private WebView currentWebView; // Store the current WebView reference
    private boolean isWebViewReady = false; // Track if WebView is ready for PDF conversion
    private final JFXPanel jfxPanelicare = new JFXPanel();
    private final JFXPanel jfxinvoices = new JFXPanel();
    private final Properties prop = new Properties();
    final ComboBoxModel<String> opModel = new DefaultComboBoxModel<>(new String[]{"20", "19", "18", "17", "16"});
    final ComboBoxModel<String> nonOpModel = new DefaultComboBoxModel<>(new String[]{"9", "8", "7", "6", "5", "4", "3", "2", "0"});

    /**
     * Creates new form DlgLhtBiaya
     *
     * @param parent
     * @param modal
     */
    public KeuanganBayarJMDokterBPJS(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8, 1);
        setSize(885, 674);

        tabModeRawatJalan = new DefaultTableModel(null, new Object[]{
            "P", "Tanggal Piutang", "Jam", "No SEP", "No Rawat", "No RM", "Nama Pasien",
            "Kode Dokter", "Nama Dokter", "Total Piutang", "Total Tarif JM", "Umbal",
            "Status", "Keterangan", "DPJP", "Raber", "Terapi", "Tindakan", "HD", "JM BPJS"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                boolean a = false;
                // P column (checkbox), Umbal, JM BPJS, and combo box columns are editable
                // Kolom P (0), Umbal (11), dan JM BPJS (19) yang dapat diedit. Kolom DPJP (14) & Raber (15) dikunci non-editable.
                if (colIndex == 0 || colIndex == 11 || colIndex == 19) {
                    a = true;
                }
                return a;
            }
            Class[] types = new Class[]{
                java.lang.Boolean.class, // P
                java.lang.Object.class, // Tanggal Piutang
                java.lang.Object.class, // Jam
                java.lang.Object.class, // No SEP
                java.lang.Object.class, // No Rawat
                java.lang.Object.class, // No RM
                java.lang.Object.class, // Nama Pasien
                java.lang.Object.class, // Kode Dokter
                java.lang.Object.class, // Nama Dokter
                java.lang.Double.class, // Total Piutang
                java.lang.Double.class, // Total Tarif JM
                java.lang.Double.class, // Umbal
                java.lang.Object.class, // Status
                java.lang.Object.class, // Keterangan (COB)
                java.lang.Object.class, // DPJP (ComboBox)
                java.lang.Object.class, // Raber (ComboBox)
                java.lang.Object.class, // Terapi (ComboBox)
                java.lang.Object.class, // Tindakan (ComboBox)
                java.lang.Object.class, // HD (ComboBox)
                java.lang.Double.class // JM BPJS
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbJMRawatJalan.setModel(tabModeRawatJalan);
        tbJMRawatJalan.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbJMRawatJalan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Set column widths for 18 columns
        int[] columnWidthsRalan = {25, 85, 55, 120, 105, 70, 180, 80, 150, 90, 90, 90, 85, 130, 60, 60, 0, 0, 0, 80};
        for (int i = 0; i < columnWidthsRalan.length; i++) {
            tbJMRawatJalan.getColumnModel().getColumn(i).setPreferredWidth(columnWidthsRalan[i]);
        }

        if (false) {
            for (int i = 0; i < 19; i++) {
                TableColumn column = tbJMRawatJalan.getColumnModel().getColumn(i);
                if (i == 0) {                    // P
                    column.setPreferredWidth(25);
                } else if (i == 1) {             // Tanggal Piutang
                    column.setPreferredWidth(85);
                } else if (i == 2) {             // Jam
                    column.setPreferredWidth(55);
                } else if (i == 3) {             // No SEP
                    column.setPreferredWidth(120);
                } else if (i == 4) {             // No Rawat
                    column.setPreferredWidth(105);
                } else if (i == 5) {             // No RM
                    column.setPreferredWidth(70);
                } else if (i == 6) {             // Nama Pasien
                    column.setPreferredWidth(180);
                } else if (i == 7) {             // Kode Dokter
                    column.setPreferredWidth(80);
                } else if (i == 8) {             // Nama Dokter
                    column.setPreferredWidth(150);
                } else if (i == 9) {             // Total Piutang
                    column.setPreferredWidth(90);
                } else if (i == 10) {            // Total Tarif JM
                    column.setPreferredWidth(90);
                } else if (i == 11) {            // Umbal
                    column.setPreferredWidth(75);
                } else if (i == 12) {            // Status
                    column.setPreferredWidth(85);
                } else if (i == 13) {            // DPJP
                    column.setPreferredWidth(60);
                } else if (i == 14) {            // Raber
                    column.setPreferredWidth(60);
                } else if (i == 15) {            // Terapi
                    column.setPreferredWidth(60);
                } else if (i == 16) {            // Tindakan
                    column.setPreferredWidth(75);
                } else if (i == 17) {            // HD
                    column.setPreferredWidth(45);
                } else if (i == 18) {            // JM BPJS
                    column.setPreferredWidth(80);
                }
            }
        }
        // Set up combobox cell editors for DPJP, Raber, Terapi, Tindakan, HD columns
        JComboBox<String> comboYaTidak = new JComboBox<>(new String[]{"Ya", "Tidak"});
        DefaultCellEditor comboEditor = new DefaultCellEditor(comboYaTidak);

        tbJMRawatJalan.getColumnModel().getColumn(14).setCellEditor(comboEditor); // DPJP
        tbJMRawatJalan.getColumnModel().getColumn(15).setCellEditor(comboEditor); // Raber
        tbJMRawatJalan.getColumnModel().getColumn(16).setCellEditor(comboEditor); // Terapi
        tbJMRawatJalan.getColumnModel().getColumn(17).setCellEditor(comboEditor); // Tindakan
        tbJMRawatJalan.getColumnModel().getColumn(18).setCellEditor(comboEditor); // HD

        tbJMRawatJalan.setDefaultRenderer(Object.class, new WarnaTable());
        tbJMRawatJalan.setRowSorter(null);
        // Sembunyikan kolom Terapi (16), Tindakan (17), dan HD (18) pada Rawat Jalan
        for (int colHide : new int[]{16, 17, 18}) {
            tbJMRawatJalan.getColumnModel().getColumn(colHide).setMinWidth(0);
            tbJMRawatJalan.getColumnModel().getColumn(colHide).setMaxWidth(0);
            tbJMRawatJalan.getColumnModel().getColumn(colHide).setPreferredWidth(0);
        }

        // 2. REPLACE YOUR ENTIRE TABLE INITIALIZATION WITH THIS:
        tabModeRawatInap = new DefaultTableModel(null, new Object[]{
            "P", "Tanggal Piutang", "Jam", "No SEP", "No Rawat", "No RM", "Nama Pasien",
            "Kode Dokter", "Nama Dokter", "Total Piutang", "Total Tarif JM", "Umbal + TopUp",
            "Status", "Keterangan", "DPJP", "TINDAKAN", "STATUS", "%", "Visit | LOS", "JM BPJS"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                // P column (checkbox), Umbal, JM BPJS, and combo box columns are editable
                // Kolom P (0), Umbal (11), dan JM BPJS (19) yang dapat diedit. Kolom DPJP (14), TINDAKAN (15), STATUS (16), % (17) dikunci non-editable.
                return colIndex == 0 || colIndex == 11 || colIndex == 19;

            }

            Class[] types = new Class[]{
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Double.class, java.lang.Double.class, java.lang.Double.class,
                java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Double.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        // Set up combobox cell editors
        JComboBox<String> comboDPJP = new JComboBox<>(new String[]{"DPJP", "DP", "Anestesi"});
        DefaultCellEditor dpjpEditor = new DefaultCellEditor(comboDPJP);

        JComboBox<String> comboTindakan = new JComboBox<>(new String[]{"OP", "NON-OP"});
        DefaultCellEditor tindakanEditor = new DefaultCellEditor(comboTindakan);

        JComboBox<String> comboStatus = new JComboBox<>(new String[]{"Sendiri", "Raber"});
        DefaultCellEditor statusEditor = new DefaultCellEditor(comboStatus);

        tbJMRawatInap.setModel(tabModeRawatInap);
        tbJMRawatInap.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbJMRawatInap.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Set column widths
        int[] columnWidths = {25, 85, 55, 120, 105, 70, 180, 80, 150, 90, 90, 125, 85, 130, 80, 80, 70, 50, 150, 80};
        for (int i = 0; i < columnWidths.length; i++) {
            tbJMRawatInap.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
        }

        // Set cell editors for combo boxes
        tbJMRawatInap.getColumnModel().getColumn(14).setCellEditor(dpjpEditor);
        tbJMRawatInap.getColumnModel().getColumn(15).setCellEditor(tindakanEditor);
        tbJMRawatInap.getColumnModel().getColumn(16).setCellEditor(statusEditor);

        // Dynamic JENIS column editor (column 15)
        tbJMRawatInap.getColumnModel().getColumn(17).setCellEditor(
                new DefaultCellEditor(new JComboBox<String>()) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                    boolean isSelected, int row, int column) {
                JComboBox<String> editorComboBox = (JComboBox<String>) this.getComponent();
                String tindakanStatus = String.valueOf(table.getValueAt(row, 15));

                if ("OP".equals(tindakanStatus)) {
                    editorComboBox.setModel(opModel);
                } else {
                    editorComboBox.setModel(nonOpModel);
                }

                editorComboBox.setSelectedItem(value);
                return editorComboBox;
            }
        });

        tbJMRawatInap.setDefaultRenderer(Object.class, new WarnaTable());
        tbJMRawatInap.setRowSorter(null);

        // ============================================================
        // CRITICAL: ADD TABLE MODEL LISTENER *AFTER* ALL SETUP IS DONE
        // ============================================================
        tabModeRawatInap.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                // Ignore events during data loading or recalculation
                if (isLoadingData || isRecalculating) {
                    return;
                }

                // Only process cell updates (not insert/delete)
                if (e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int column = e.getColumn();

                    // Validate row index
                    if (row < 0 || row >= tabModeRawatInap.getRowCount()) {
                        return;
                    }

                    // Trigger recalculation for these columns:
                    // 11: Umbal, 12: DPJP, 13: TINDAKAN, 14: STATUS, 15: JENIS, 16: Visit
                    if (column == 11 || column == 14 || column == 15 || column == 16 || column == 17 || column == 18) { //

                        // Use invokeLater to avoid EDT conflicts
                        final int finalRow = row;
                        SwingUtilities.invokeLater(() -> recalculateJmBpjs(finalRow));
                    }
                }
            }
        });

        NoTagihan.setDocument(new batasInput((byte) 17).getKata(NoTagihan));
        Keterangan.setDocument(new batasInput((int) 150).getKata(Keterangan));

        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        prosesCari();
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        prosesCari();
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        prosesCari();
                    }
                }
            });
        }

        TCari.setDocument(new batasInput((int) 100).getKata(TCari));
        NoTagihan.setDocument(new batasInput((int) 17).getKata(NoTagihan));
        Keterangan.setDocument(new batasInput((int) 70).getKata(Keterangan));

        carabayar.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (carabayar.getTable().getSelectedRow() != -1) {
                    KdCaraBayar.setText(carabayar.getTable().getValueAt(carabayar.getTable().getSelectedRow(), 1).toString());
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
                carabayar.onCari();
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

        carabayar.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    carabayar.dispose();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });

        // Fallback guard agar filter Status Klaim & COB tetap terpasang jika NetBeans me-regenerate initComponents()
        if (CmbStatusKlaim == null) {
            jLabelStatusKlaim = new widget.Label();
            jLabelStatusKlaim.setText("Status Klaim :");
            jLabelStatusKlaim.setName("jLabelStatusKlaim");
            panelisi4.add(jLabelStatusKlaim);
            jLabelStatusKlaim.setBounds(755, 40, 75, 23);

            CmbStatusKlaim = new widget.ComboBox();
            CmbStatusKlaim.setName("CmbStatusKlaim");
            CmbStatusKlaim.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    CmbStatusKlaimItemStateChanged(evt);
                }
            });
            panelisi4.add(CmbStatusKlaim);
            CmbStatusKlaim.setBounds(833, 40, 135, 23);
        }

        if (cmbCOB == null) {
            jLabelCOB = new widget.Label();
            jLabelCOB.setText("COB :");
            jLabelCOB.setName("jLabelCOB");
            panelisi4.add(jLabelCOB);
            jLabelCOB.setBounds(975, 40, 40, 23);

            cmbCOB = new widget.ComboBox();
            cmbCOB.setName("cmbCOB");
            cmbCOB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Semua", "Non COB", "Hanya COB"}));
            cmbCOB.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    cmbCOBItemStateChanged(evt);
                }
            });
            panelisi4.add(cmbCOB);
            cmbCOB.setBounds(1018, 40, 95, 23);
        }

        tampilStatusKlaim();


        /*
                public void keyTyped(KeyEvent e) {
                }

                public void keyPressed(KeyEvent e) {
                }

                public void keyReleased(KeyEvent e) {
                }
            }
        }


    */

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TKd = new widget.TextBox();
        Popup = new javax.swing.JPopupMenu();
        ppBersihkan = new javax.swing.JMenuItem();
        ppSemua = new javax.swing.JMenuItem();
        MnDetailPiutang = new javax.swing.JMenuItem();
        MnDetailHarianPasien = new javax.swing.JMenuItem();
        MnRincianJM = new javax.swing.JMenuItem();
        KdCaraBayar = new widget.TextBox();
        lblNoRawat = new widget.Label();
        internalFrame1 = new widget.InternalFrame();
        jPanel1 = new javax.swing.JPanel();
        panelisi3 = new widget.panelisi();
        jLabel16 = new widget.Label();
        Periode1 = new widget.Tanggal();
        Period2 = new widget.Tanggal();
        jLabel12 = new widget.Label();
        cmbStatus = new widget.ComboBox();
        label17 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        BtnGaji = new widget.Button();
        panelisi1 = new widget.panelisi();
        jLabel10 = new javax.swing.JLabel();
        LCount = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        LCount1 = new javax.swing.JLabel();
        BtnSimpan = new widget.Button();
        BtnCari1 = new widget.Button();
        BtnAturanJM = new widget.Button();
        BtnExport = new widget.Button();
        BtnKeluar = new widget.Button();
        panelisi4 = new widget.panelisi();
        label32 = new widget.Label();
        label36 = new widget.Label();
        NoTagihan = new widget.TextBox();
        label16 = new widget.Label();
        kddokter = new widget.TextBox();
        nmdokter = new widget.TextBox();
        Tanggal = new widget.Tanggal();
        BtnPetugas = new widget.Button();
        Keterangan = new widget.TextBox();
        label39 = new widget.Label();
        jLabel13 = new widget.Label();
        AkunBayar = new widget.ComboBox();
        BtnAll1 = new widget.Button();
        jLabelStatusKlaim = new widget.Label();
        CmbStatusKlaim = new widget.ComboBox();
        jLabelCOB = new widget.Label();
        cmbCOB = new widget.ComboBox();
        TabRawat = new javax.swing.JTabbedPane();
        Scroll1 = new widget.ScrollPane();
        tbJMRawatJalan = new widget.Table();
        Scroll2 = new widget.ScrollPane();
        tbJMRawatInap = new widget.Table();
        panelInvoices = new widget.panelisi();

        TKd.setForeground(new java.awt.Color(255, 255, 255));
        TKd.setName("TKd"); // NOI18N

        Popup.setName("Popup"); // NOI18N

        ppBersihkan.setBackground(new java.awt.Color(255, 255, 254));
        ppBersihkan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppBersihkan.setForeground(new java.awt.Color(50, 50, 50));
        ppBersihkan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppBersihkan.setText("Bersihkan Pilihan");
        ppBersihkan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppBersihkan.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppBersihkan.setName("ppBersihkan"); // NOI18N
        ppBersihkan.setPreferredSize(new java.awt.Dimension(200, 25));
        ppBersihkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppBersihkanActionPerformed(evt);
            }
        });
        Popup.add(ppBersihkan);

        ppSemua.setBackground(new java.awt.Color(255, 255, 254));
        ppSemua.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppSemua.setForeground(new java.awt.Color(50, 50, 50));
        ppSemua.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppSemua.setText("Pilih Semua");
        ppSemua.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppSemua.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppSemua.setName("ppSemua"); // NOI18N
        ppSemua.setPreferredSize(new java.awt.Dimension(200, 25));
        ppSemua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppSemuaActionPerformed(evt);
            }
        });
        Popup.add(ppSemua);

        MnDetailPiutang.setBackground(new java.awt.Color(255, 255, 254));
        MnDetailPiutang.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnDetailPiutang.setForeground(new java.awt.Color(50, 50, 50));
        MnDetailPiutang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnDetailPiutang.setText("Detail Piutang");
        MnDetailPiutang.setName("MnDetailPiutang"); // NOI18N
        MnDetailPiutang.setPreferredSize(new java.awt.Dimension(200, 28));
        MnDetailPiutang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnDetailPiutangActionPerformed(evt);
            }
        });
        Popup.add(MnDetailPiutang);

        MnDetailHarianPasien.setBackground(new java.awt.Color(255, 255, 254));
        MnDetailHarianPasien.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnDetailHarianPasien.setForeground(new java.awt.Color(50, 50, 50));
        MnDetailHarianPasien.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        MnDetailHarianPasien.setText("Detail Harian Pasien (Buku Bantu)");
        MnDetailHarianPasien.setName("MnDetailHarianPasien"); // NOI18N
        MnDetailHarianPasien.setPreferredSize(new java.awt.Dimension(220, 28));
        MnDetailHarianPasien.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnDetailHarianPasienActionPerformed(evt);
            }
        });
        Popup.add(MnDetailHarianPasien);

        MnRincianJM.setBackground(new java.awt.Color(255, 255, 254));
        MnRincianJM.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnRincianJM.setForeground(new java.awt.Color(50, 50, 50));
        MnRincianJM.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/preview.png"))); // NOI18N
        MnRincianJM.setText("Rincian Perhitungan JM Pasien");
        MnRincianJM.setName("MnRincianJM"); // NOI18N
        MnRincianJM.setPreferredSize(new java.awt.Dimension(220, 28));
        MnRincianJM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnRincianJMActionPerformed(evt);
            }
        });
        Popup.add(MnRincianJM);

        KdCaraBayar.setEditable(false);
        KdCaraBayar.setName("KdCaraBayar"); // NOI18N
        KdCaraBayar.setPreferredSize(new java.awt.Dimension(50, 23));

        lblNoRawat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblNoRawat.setText("NoRawat");
        lblNoRawat.setName("lblNoRawat"); // NOI18N
        lblNoRawat.setPreferredSize(new java.awt.Dimension(300, 14));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Bayar Jasa Medis Dokter ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(816, 115));
        jPanel1.setLayout(new java.awt.BorderLayout(1, 1));

        panelisi3.setName("panelisi3"); // NOI18N
        panelisi3.setPreferredSize(new java.awt.Dimension(50, 30));
        panelisi3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        jLabel16.setText("Periode :");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setPreferredSize(new java.awt.Dimension(70, 30));
        panelisi3.add(jLabel16);

        Periode1.setDisplayFormat("dd-MM-yyyy");
        Periode1.setName("Periode1"); // NOI18N
        Periode1.setPreferredSize(new java.awt.Dimension(90, 23));
        Periode1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Periode1ItemStateChanged(evt);
            }
        });
        Periode1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Periode1KeyPressed(evt);
            }
        });
        panelisi3.add(Periode1);

        Period2.setDisplayFormat("dd-MM-yyyy");
        Period2.setName("Period2"); // NOI18N
        Period2.setPreferredSize(new java.awt.Dimension(90, 23));
        Period2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                Period2ItemStateChanged(evt);
            }
        });
        Period2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Period2KeyPressed(evt);
            }
        });
        panelisi3.add(Period2);

        jLabel12.setText("Status :");
        jLabel12.setName("jLabel12"); // NOI18N
        jLabel12.setPreferredSize(new java.awt.Dimension(55, 23));
        panelisi3.add(jLabel12);

        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Piutang Belum Lunas", "Piutang Sudah Lunas" }));
        cmbStatus.setName("cmbStatus"); // NOI18N
        cmbStatus.setPreferredSize(new java.awt.Dimension(125, 23));
        panelisi3.add(cmbStatus);

        label17.setText("Key Word :");
        label17.setName("label17"); // NOI18N
        label17.setPreferredSize(new java.awt.Dimension(85, 23));
        panelisi3.add(label17);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(270, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelisi3.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('2');
        BtnCari.setToolTipText("Alt+2");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        BtnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariKeyPressed(evt);
            }
        });
        panelisi3.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        BtnAll.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllKeyPressed(evt);
            }
        });
        panelisi3.add(BtnAll);

        BtnGaji.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/gaji.png"))); // NOI18N
        BtnGaji.setMnemonic('2');
        BtnGaji.setToolTipText("Alt+2");
        BtnGaji.setName("BtnGaji"); // NOI18N
        BtnGaji.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnGaji.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnGajiActionPerformed(evt);
            }
        });
        BtnGaji.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnGajiKeyPressed(evt);
            }
        });
        panelisi3.add(BtnGaji);

        jPanel1.add(panelisi3, java.awt.BorderLayout.CENTER);

        panelisi1.setName("panelisi1"); // NOI18N
        panelisi1.setPreferredSize(new java.awt.Dimension(55, 55));
        panelisi1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(50, 50, 50));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Total J.M. :");
        jLabel10.setName("jLabel10"); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(66, 23));
        panelisi1.add(jLabel10);

        LCount.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        LCount.setForeground(new java.awt.Color(50, 50, 50));
        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(150, 23));
        panelisi1.add(LCount);

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(50, 50, 50));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel11.setText("Dibayar :");
        jLabel11.setName("jLabel11"); // NOI18N
        jLabel11.setPreferredSize(new java.awt.Dimension(76, 23));
        panelisi1.add(jLabel11);

        LCount1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        LCount1.setForeground(new java.awt.Color(50, 50, 50));
        LCount1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount1.setText("0");
        LCount1.setName("LCount1"); // NOI18N
        LCount1.setPreferredSize(new java.awt.Dimension(125, 23));
        panelisi1.add(LCount1);

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('B');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanActionPerformed(evt);
            }
        });
        BtnSimpan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanKeyPressed(evt);
            }
        });
        panelisi1.add(BtnSimpan);

        BtnCari1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnCari1.setMnemonic('C');
        BtnCari1.setText("Cari");
        BtnCari1.setToolTipText("Alt+C");
        BtnCari1.setName("BtnCari1"); // NOI18N
        BtnCari1.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnCari1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCari1ActionPerformed(evt);
            }
        });
        BtnCari1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCari1KeyPressed(evt);
            }
        });
        panelisi1.add(BtnCari1);

        BtnAturanJM.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnAturanJM.setMnemonic('A');
        BtnAturanJM.setText("Aturan JM");
        BtnAturanJM.setToolTipText("Alt+A (Buka Pengaturan Ketentuan JM BPJS)");
        BtnAturanJM.setName("BtnAturanJM"); // NOI18N
        BtnAturanJM.setPreferredSize(new java.awt.Dimension(115, 30));
        BtnAturanJM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAturanJMActionPerformed(evt);
            }
        });
        BtnAturanJM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAturanJMKeyPressed(evt);
            }
        });
        panelisi1.add(BtnAturanJM);

                BtnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnExport.setMnemonic('E');
        BtnExport.setText("Export Excel");
        BtnExport.setToolTipText("Alt+E (Export Data Tabel ke File Excel .xlsx)");
        BtnExport.setName("BtnExport"); // NOI18N
        BtnExport.setPreferredSize(new java.awt.Dimension(120, 30));
        BtnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnExportActionPerformed(evt);
            }
        });
        BtnExport.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnExportKeyPressed(evt);
            }
        });
        panelisi1.add(BtnExport);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnKeluarKeyPressed(evt);
            }
        });
        panelisi1.add(BtnKeluar);

        jPanel1.add(panelisi1, java.awt.BorderLayout.PAGE_END);

        internalFrame1.add(jPanel1, java.awt.BorderLayout.PAGE_END);

        panelisi4.setName("panelisi4"); // NOI18N
        panelisi4.setPreferredSize(new java.awt.Dimension(100, 74));
        panelisi4.setLayout(null);

        label32.setText("Tanggal :");
        label32.setName("label32"); // NOI18N
        label32.setPreferredSize(new java.awt.Dimension(35, 23));
        panelisi4.add(label32);
        label32.setBounds(197, 10, 59, 23);

        label36.setText("Keterangan :");
        label36.setName("label36"); // NOI18N
        label36.setPreferredSize(new java.awt.Dimension(35, 23));
        panelisi4.add(label36);
        label36.setBounds(0, 40, 74, 23);

        NoTagihan.setHighlighter(null);
        NoTagihan.setName("NoTagihan"); // NOI18N
        NoTagihan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoTagihanKeyPressed(evt);
            }
        });
        panelisi4.add(NoTagihan);
        NoTagihan.setBounds(78, 10, 120, 23);

        label16.setText("Dokter :");
        label16.setName("label16"); // NOI18N
        label16.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi4.add(label16);
        label16.setBounds(363, 10, 70, 23);

        kddokter.setEditable(false);
        kddokter.setName("kddokter"); // NOI18N
        kddokter.setPreferredSize(new java.awt.Dimension(80, 23));
        kddokter.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kddokterKeyPressed(evt);
            }
        });
        panelisi4.add(kddokter);
        kddokter.setBounds(437, 10, 100, 23);

        nmdokter.setEditable(false);
        nmdokter.setName("nmdokter"); // NOI18N
        nmdokter.setPreferredSize(new java.awt.Dimension(207, 23));
        panelisi4.add(nmdokter);
        nmdokter.setBounds(539, 10, 181, 23);

        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        panelisi4.add(Tanggal);
        Tanggal.setBounds(260, 10, 90, 23);

        BtnPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPetugas.setMnemonic('1');
        BtnPetugas.setToolTipText("ALt+1");
        BtnPetugas.setName("BtnPetugas"); // NOI18N
        BtnPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPetugasActionPerformed(evt);
            }
        });
        BtnPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPetugasKeyPressed(evt);
            }
        });
        panelisi4.add(BtnPetugas);
        BtnPetugas.setBounds(722, 10, 28, 23);

        Keterangan.setHighlighter(null);
        Keterangan.setName("Keterangan"); // NOI18N
        Keterangan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganKeyPressed(evt);
            }
        });
        panelisi4.add(Keterangan);
        Keterangan.setBounds(78, 40, 272, 23);

        label39.setText("Nomor J.M. :");
        label39.setName("label39"); // NOI18N
        label39.setPreferredSize(new java.awt.Dimension(35, 23));
        panelisi4.add(label39);
        label39.setBounds(0, 10, 74, 23);

        jLabel13.setText("Akun Bayar :");
        jLabel13.setName("jLabel13"); // NOI18N
        panelisi4.add(jLabel13);
        jLabel13.setBounds(363, 40, 70, 23);

        AkunBayar.setName("AkunBayar"); // NOI18N
        AkunBayar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AkunBayarKeyPressed(evt);
            }
        });
        panelisi4.add(AkunBayar);
        AkunBayar.setBounds(437, 40, 283, 23);

        BtnAll1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/refresh.png"))); // NOI18N
        BtnAll1.setMnemonic('M');
        BtnAll1.setToolTipText("Alt+M");
        BtnAll1.setName("BtnAll1"); // NOI18N
        BtnAll1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAll1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAll1ActionPerformed(evt);
            }
        });
        panelisi4.add(BtnAll1);
        BtnAll1.setBounds(722, 40, 28, 23);

        jLabelStatusKlaim.setText("Status Klaim :");
        jLabelStatusKlaim.setName("jLabelStatusKlaim");
        jLabelStatusKlaim.setPreferredSize(new java.awt.Dimension(75, 23));
        panelisi4.add(jLabelStatusKlaim);
        jLabelStatusKlaim.setBounds(755, 40, 75, 23);

        CmbStatusKlaim.setName("CmbStatusKlaim");
        CmbStatusKlaim.setPreferredSize(new java.awt.Dimension(135, 23));
        CmbStatusKlaim.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CmbStatusKlaimItemStateChanged(evt);
            }
        });
        panelisi4.add(CmbStatusKlaim);
        CmbStatusKlaim.setBounds(833, 40, 135, 23);

        jLabelCOB.setText("COB :");
        jLabelCOB.setName("jLabelCOB");
        jLabelCOB.setPreferredSize(new java.awt.Dimension(40, 23));
        panelisi4.add(jLabelCOB);
        jLabelCOB.setBounds(975, 40, 40, 23);

        cmbCOB.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Semua", "Non COB", "Hanya COB"}));
        cmbCOB.setName("cmbCOB");
        cmbCOB.setPreferredSize(new java.awt.Dimension(95, 23));
        cmbCOB.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbCOBItemStateChanged(evt);
            }
        });
        panelisi4.add(cmbCOB);
        cmbCOB.setBounds(1018, 40, 95, 23);

        internalFrame1.add(panelisi4, java.awt.BorderLayout.PAGE_START);

        TabRawat.setBackground(new java.awt.Color(255, 255, 253));
        TabRawat.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(241, 246, 236)));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });
        TabRawat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TabRawatKeyPressed(evt);
            }
        });

        Scroll1.setName("Scroll1"); // NOI18N
        Scroll1.setOpaque(true);

        tbJMRawatJalan.setToolTipText("Silahkan centang di depan untuk memilih data hutang yang mau dibayar");
        tbJMRawatJalan.setComponentPopupMenu(Popup);
        tbJMRawatJalan.setName("tbJMRawatJalan"); // NOI18N
        tbJMRawatJalan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbJMRawatJalanMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (javax.swing.SwingUtilities.isRightMouseButton(evt) || evt.isPopupTrigger()) {
                    int r = tbJMRawatJalan.rowAtPoint(evt.getPoint());
                    if (r >= 0) {
                        tbJMRawatJalan.setRowSelectionInterval(r, r);
                    }
                }
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (javax.swing.SwingUtilities.isRightMouseButton(evt) || evt.isPopupTrigger()) {
                    int r = tbJMRawatJalan.rowAtPoint(evt.getPoint());
                    if (r >= 0) {
                        tbJMRawatJalan.setRowSelectionInterval(r, r);
                    }
                }
            }
        });
        tbJMRawatJalan.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                tbJMRawatJalanPropertyChange(evt);
            }
        });
        tbJMRawatJalan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbJMRawatJalanKeyPressed(evt);
            }
        });
        Scroll1.setViewportView(tbJMRawatJalan);

        TabRawat.addTab("Rawat Jalan", Scroll1);

        Scroll2.setName("Scroll2"); // NOI18N
        Scroll2.setOpaque(true);

        tbJMRawatInap.setToolTipText("Silahkan centang di depan untuk memilih data hutang yang mau dibayar");
        tbJMRawatInap.setComponentPopupMenu(Popup);
        tbJMRawatInap.setName("tbJMRawatInap"); // NOI18N
        tbJMRawatInap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbJMRawatInapMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                if (javax.swing.SwingUtilities.isRightMouseButton(evt) || evt.isPopupTrigger()) {
                    int r = tbJMRawatInap.rowAtPoint(evt.getPoint());
                    if (r >= 0) {
                        tbJMRawatInap.setRowSelectionInterval(r, r);
                    }
                }
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                if (javax.swing.SwingUtilities.isRightMouseButton(evt) || evt.isPopupTrigger()) {
                    int r = tbJMRawatInap.rowAtPoint(evt.getPoint());
                    if (r >= 0) {
                        tbJMRawatInap.setRowSelectionInterval(r, r);
                    }
                }
            }
        });
        tbJMRawatInap.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                tbJMRawatInapPropertyChange(evt);
            }
        });
        tbJMRawatInap.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbJMRawatInapKeyPressed(evt);
            }
        });
        Scroll2.setViewportView(tbJMRawatInap);

        TabRawat.addTab("Rawat Inap", Scroll2);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        panelInvoices.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder(null, "INVOICE / BILLING PASIEN", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION), "INVOICE/BILLING PASIEN", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION));
        panelInvoices.setName("panelInvoices"); // NOI18N
        panelInvoices.setPreferredSize(new java.awt.Dimension(500, 150));
        panelInvoices.setLayout(new java.awt.BorderLayout());
        internalFrame1.add(panelInvoices, java.awt.BorderLayout.LINE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnAturanJMActionPerformed(java.awt.event.ActionEvent evt) {
        if (dlgAturanJM == null) {
            dlgAturanJM = new DlgSetAturanJMBPJS(null, false);
            dlgAturanJM.addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {
                }

                @Override
                public void windowClosing(WindowEvent e) {
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    // Ketika form aturan ditutup, muat ulang cache dan refresh kalkulasi jika data sudah tampil
                    kalkulator.reloadCache(koneksi);
                    if (tabModeRawatJalan.getRowCount() > 0 || tabModeRawatInap.getRowCount() > 0) {
                        prosesCari();
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
        }
        dlgAturanJM.emptTeks();
        dlgAturanJM.tampil();
        dlgAturanJM.setSize(internalFrame1.getWidth() - 40, internalFrame1.getHeight() - 40);
        dlgAturanJM.setLocationRelativeTo(internalFrame1);
        dlgAturanJM.setVisible(true);
    }

    private void BtnAturanJMKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnAturanJMActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnCari1, BtnExport);
        }
    }

    private void BtnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnExportActionPerformed
        exportToExcelActionPerformed(evt);
    }//GEN-LAST:event_BtnExportActionPerformed

    private void BtnExportKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnExportKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            exportToExcelActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnAturanJM, BtnKeluar);
        }
    }//GEN-LAST:event_BtnExportKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            dispose();
        } else {
            Valid.pindah(evt, BtnAll, TKd);
        }
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        kddokter.setText("");
        nmdokter.setText("");
        KdCaraBayar.setText("");
        if (CmbStatusKlaim != null && CmbStatusKlaim.getItemCount() > 0) {
            CmbStatusKlaim.setSelectedIndex(0);
        }
        if (cmbCOB != null && cmbCOB.getItemCount() > 0) {
            cmbCOB.setSelectedIndex(0);
        }
//        NmCaraBayar.setText("");
        prosesCari();
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnAllActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnCari1, BtnExport);
        }
}//GEN-LAST:event_BtnAllKeyPressed

private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            BtnCariActionPerformed(null);
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            BtnCari.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            BtnKeluar.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        prosesCari();
}//GEN-LAST:event_BtnCariActionPerformed

private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            prosesCari();
        } else {
            Valid.pindah(evt, TKd, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        try {
            if (Valid.daysOld("./cache/akunbayarhutang.iyem") < 8) {
                tampilAkunBayar2();
            } else {
                tampilAkunBayar();
                tampilStatusKlaim();
            }
        } catch (Exception e) {
        }
    }//GEN-LAST:event_formWindowOpened

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (NoTagihan.getText().trim().equals("")) {
            Valid.textKosong(NoTagihan, "No.Bayar");
        } else if (kddokter.getText().trim().equals("") || nmdokter.getText().trim().equals("")) {
            Valid.textKosong(kddokter, "Petugas");
        } else if (tabModeRawatJalan.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda simpan...!!!!");
        } else if (Keterangan.getText().trim().equals("")) {
            Valid.textKosong(Keterangan, "Keterangan");
        } else if (bayar <= 0) {
            JOptionPane.showMessageDialog(null, "Maaf, Silahkan masukkan tagihan...!!!!");
            tbJMRawatJalan.requestFocus();
        } else {
            i = JOptionPane.showConfirmDialog(rootPane, "Eeiiiiiits, udah bener belum data yang mau disimpan..??", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (i == JOptionPane.YES_OPTION) {
                Sequel.AutoComitFalse();
                sukses = true;

                getCariBayarJMDokter().koderekening = "";
                try {
                    getCariBayarJMDokter().myObj = new FileReader("./cache/akunbayarhutang.iyem");
                    getCariBayarJMDokter().root = getCariBayarJMDokter().mapper.readTree(getCariBayarJMDokter().myObj);
                    getCariBayarJMDokter().response = getCariBayarJMDokter().root.path("akunbayarhutang");
                    if (getCariBayarJMDokter().response.isArray()) {
                        for (JsonNode list : getCariBayarJMDokter().response) {
                            if (list.path("NamaAkun").asText().equals(AkunBayar.getSelectedItem().toString())) {
                                getCariBayarJMDokter().koderekening = list.path("KodeRek").asText();
                            }
                        }
                    }
                    getCariBayarJMDokter().myObj.close();
                } catch (Exception e) {
                    sukses = false;
                }

                if (Sequel.menyimpantf2("bayar_jm_dokter", "?,?,?,?,?,?,?,?,?,?,?,?,?,?", "No.Tagihan", 14, new String[]{
                    NoTagihan.getText(), Valid.SetTgl(Tanggal.getSelectedItem() + ""), kddokter.getText(), bayar + "", AkunBayar.getSelectedItem().toString(), Keterangan.getText(),
                    totalrawatjalan + "", totalrawatinap + "", totallabrawatjalan + "", totallabrawatinap + "", totalradrawatjalan + "", totalradrawatinap + "", totaloperasirawatjalan + "", totaloperasirawatinap + ""
                }) == true) {
                    row = tbJMRawatJalan.getRowCount();
                    for (i = 0; i < row; i++) {
                        //"P"0,"Tanggal"1,"Jam"2,"No.Rawat"3,"No.RM"4,"Nama Pasien"5,"Kode "6,"Tindakan Medis"7,"Status"8,"Jasa Medis"9
                        if (tabModeRawatJalan.getValueAt(i, 0).toString().equals("true")) {

                        }
                    }
                } else {
                    sukses = false;
                    JOptionPane.showMessageDialog(rootPane, "Gagal Menyimpan, kemungkinan Nomor J.M. sudah ada sebelumnya...!!");
                }

                if (sukses == true) {
                    Sequel.queryu("delete from tampjurnal");
                    if (totalrawatjalan > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Tindakan_Ralan + "','Utang Jasa Medik Dokter Tindakan Ralan','" + totalrawatjalan + "','0'", "debet=debet+'" + (totalrawatjalan) + "'", "kd_rek='" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Tindakan_Ralan + "'");
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Tindakan_Ralan + "','Beban Jasa Medik Dokter Tindakan Ralan','0','" + totalrawatjalan + "'", "kredit=kredit+'" + (totalrawatjalan) + "'", "kd_rek='" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Tindakan_Ralan + "'");
                    }
                    if (totalrawatinap > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Tindakan_Ranap + "','Utang Jasa Medik Dokter Tindakan Ranap','" + totalrawatinap + "','0'", "debet=debet+'" + (totalrawatinap) + "'", "kd_rek='" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Tindakan_Ranap + "'");
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Tindakan_Ranap + "','Beban Jasa Medik Dokter Tindakan Ranap','0','" + totalrawatinap + "'", "kredit=kredit+'" + (totalrawatinap) + "'", "kd_rek='" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Tindakan_Ranap + "'");
                    }
                    if (totallabrawatjalan > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Laborat_Ralan + "','Utang Jasa Medik Dokter Laborat Ralan','" + totallabrawatjalan + "','0'", "debet=debet+'" + (totallabrawatjalan) + "'", "kd_rek='" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Laborat_Ralan + "'");
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Laborat_Ralan + "','Beban Jasa Medik Dokter Laborat Ralan','0','" + totallabrawatjalan + "'", "kredit=kredit+'" + (totallabrawatjalan) + "'", "kd_rek='" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Laborat_Ralan + "'");
                    }
                    if (totallabrawatinap > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Laborat_Ranap + "','Beban Jasa Medik Dokter Laborat Ranap','0','" + totallabrawatinap + "'", "kredit=kredit+'" + (totallabrawatinap) + "'", "kd_rek='" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Laborat_Ranap + "'");
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Laborat_Ranap + "','Utang Jasa Medik Dokter Laborat Ranap','" + totallabrawatinap + "','0'", "debet=debet+'" + (totallabrawatinap) + "'", "kd_rek='" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Laborat_Ranap + "'");
                    }
                    if (totalradrawatjalan > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Radiologi_Ralan + "','Beban Jasa Medik Dokter Radiologi Ralan','0','" + totalradrawatjalan + "'", "kredit=kredit+'" + (totalradrawatjalan) + "'", "kd_rek='" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Radiologi_Ralan + "'");
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Radiologi_Ralan + "','Utang Jasa Medik Dokter Radiologi Ralan','" + totalradrawatjalan + "','0'", "debet=debet+'" + (totalradrawatjalan) + "'", "kd_rek='" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Radiologi_Ralan + "'");
                    }
                    if (totalradrawatinap > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Radiologi_Ranap + "','Beban Jasa Medik Dokter Radiologi Ranap','0','" + totalradrawatinap + "'", "kredit=kredit+'" + (totalradrawatinap) + "'", "kd_rek='" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Radiologi_Ranap + "'");
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Radiologi_Ranap + "','Utang Jasa Medik Dokter Radiologi Ranap','" + totalradrawatinap + "','0'", "debet=debet+'" + (totalradrawatinap) + "'", "kd_rek='" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Radiologi_Ranap + "'");
                    }
                    if (totaloperasirawatjalan > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Operasi_Ralan + "','Beban Jasa Medik Dokter Operasi Ralan','0','" + totaloperasirawatjalan + "'", "kredit=kredit+'" + totaloperasirawatjalan + "'", "kd_rek='" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Operasi_Ralan + "'");
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Operasi_Ralan + "','Utang Jasa Medik Dokter Operasi Ralan','" + totaloperasirawatjalan + "','0'", "debet=debet+'" + totaloperasirawatjalan + "'", "kd_rek='" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Operasi_Ralan + "'");
                    }
                    if (totaloperasirawatinap > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Operasi_Ranap + "','Beban Jasa Medik Dokter Operasi Ranap','0','" + totaloperasirawatinap + "'", "kredit=kredit+'" + totaloperasirawatinap + "'", "kd_rek='" + getCariBayarJMDokter().Beban_Jasa_Medik_Dokter_Operasi_Ranap + "'");
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Operasi_Ranap + "','Utang Jasa Medik Dokter Operasi Ranap','" + totaloperasirawatinap + "','0'", "debet=debet+'" + totaloperasirawatinap + "'", "kd_rek='" + getCariBayarJMDokter().Utang_Jasa_Medik_Dokter_Operasi_Ranap + "'");
                    }
                    if (bayar > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().Bayar_JM_Dokter + "','Bayar JM Dokter','" + bayar + "','0'", "debet=debet+'" + (bayar) + "'", "kd_rek='" + getCariBayarJMDokter().Bayar_JM_Dokter + "'");
                        Sequel.menyimpan("tampjurnal", "'" + getCariBayarJMDokter().koderekening + "','" + AkunBayar.getSelectedItem().toString() + "','0','" + bayar + "'", "kredit=kredit+'" + (bayar) + "'", "kd_rek='" + getCariBayarJMDokter().koderekening + "'");
                    }
                    sukses = getCariBayarJMDokter().jur.simpanJurnal(NoTagihan.getText(), "U", "PEMBAYARAN JASA MEDIS DOKTER " + kddokter.getText() + " " + nmdokter.getText() + ", OLEH " + akses.getkode());
                }

                if (sukses == true) {
                    autoNomor();
                    prosesCari();
                    bayar = 0;
                    totalrawatjalan = 0;
                    totalrawatinap = 0;
                    totallabrawatjalan = 0;
                    totallabrawatinap = 0;
                    totalradrawatjalan = 0;
                    totalradrawatinap = 0;
                    totaloperasirawatjalan = 0;
                    totaloperasirawatinap = 0;
                    LCount1.setText("0");
                    Sequel.Commit();
                } else {
                    JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat pemrosesan data, transaksi dibatalkan.\nPeriksa kembali data sebelum melanjutkan menyimpan..!!");
                    Sequel.RollBack();
                }

                Sequel.AutoComitTrue();
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        Valid.pindah(evt, TCari, BtnCari1);
    }//GEN-LAST:event_BtnSimpanKeyPressed

    private void NoTagihanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoTagihanKeyPressed
        Valid.pindah(evt, TCari, Tanggal);
    }//GEN-LAST:event_NoTagihanKeyPressed

    private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed
        Valid.pindah2(evt, NoTagihan, AkunBayar);
    }//GEN-LAST:event_TanggalKeyPressed

    private void KeteranganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganKeyPressed
        Valid.pindah(evt, BtnPetugas, BtnSimpan);
    }//GEN-LAST:event_KeteranganKeyPressed

    private void ppBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBersihkanActionPerformed
        for (row = 0; row < tbJMRawatJalan.getRowCount(); row++) {
            tbJMRawatJalan.setValueAt(false, row, 0);
        }
        getData();
    }//GEN-LAST:event_ppBersihkanActionPerformed

    private void ppSemuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppSemuaActionPerformed
        for (row = 0; row < tbJMRawatJalan.getRowCount(); row++) {
            tbJMRawatJalan.setValueAt(true, row, 0);
        }
        getData();
    }//GEN-LAST:event_ppSemuaActionPerformed

    private void BtnCari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCari1ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        getCariBayarJMDokter().emptTeks();
        getCariBayarJMDokter().isCek();
        getCariBayarJMDokter().setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        getCariBayarJMDokter().setLocationRelativeTo(internalFrame1);
        getCariBayarJMDokter().setAlwaysOnTop(false);
        getCariBayarJMDokter().setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnCari1ActionPerformed

    private void BtnCari1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCari1KeyPressed
        Valid.pindah(evt, BtnSimpan, BtnKeluar);
    }//GEN-LAST:event_BtnCari1KeyPressed

    private void BtnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPetugasActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        akses.setform("KeuanganBayarJMDokter");
        getCariBayarJMDokter().dokter.emptTeks();
        getCariBayarJMDokter().dokter.isCek();
        getCariBayarJMDokter().dokter.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        getCariBayarJMDokter().dokter.setLocationRelativeTo(internalFrame1);
        getCariBayarJMDokter().dokter.setAlwaysOnTop(false);
        getCariBayarJMDokter().dokter.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnPetugasActionPerformed

    private void kddokterKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kddokterKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            Keterangan.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            Keterangan.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            BtnPetugasActionPerformed(null);
        }
    }//GEN-LAST:event_kddokterKeyPressed

    private void AkunBayarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AkunBayarKeyPressed
        Valid.pindah(evt, Tanggal, BtnPetugas);
    }//GEN-LAST:event_AkunBayarKeyPressed

    private void tbJMRawatJalanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbJMRawatJalanKeyPressed
        if (tabModeRawatJalan.getRowCount() != 0) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                getData();
                if (tbJMRawatJalan.getSelectedRow() != -1) {
                    String noRawat = tbJMRawatJalan.getValueAt(tbJMRawatJalan.getSelectedRow(), 4).toString().trim();
                    muatNotaBilling(noRawat);
                }
            }
        }
    }//GEN-LAST:event_tbJMRawatJalanKeyPressed

    private void tbJMRawatJalanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbJMRawatJalanMouseClicked
        if (tabModeRawatJalan.getRowCount() != 0) {
            try {
                getData();
                if (tbJMRawatJalan.getSelectedRow() != -1) {
                    String noRawat = tbJMRawatJalan.getValueAt(tbJMRawatJalan.getSelectedRow(), 4).toString().trim();
                    muatNotaBilling(noRawat);
                }
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbJMRawatJalanMouseClicked

    private void tbJMRawatJalanPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tbJMRawatJalanPropertyChange
        // Dinonaktifkan: Mencegah perubahan data / formula lama saat baris atau sel diklik
    }//GEN-LAST:event_tbJMRawatJalanPropertyChange

    private void BtnPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPetugasKeyPressed
        Valid.pindah(evt, AkunBayar, Keterangan);
    }//GEN-LAST:event_BtnPetugasKeyPressed

    private void BtnGajiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGajiActionPerformed

    }//GEN-LAST:event_BtnGajiActionPerformed

    private void BtnGajiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnGajiKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnGajiActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnCari, BtnAll);
        }
    }//GEN-LAST:event_BtnGajiKeyPressed

    private void BtnAll1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAll1ActionPerformed
        tampilAkunBayar();
        tampilStatusKlaim();
    }//GEN-LAST:event_BtnAll1ActionPerformed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        prosesCari();
    }//GEN-LAST:event_TabRawatMouseClicked

    private void TabRawatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TabRawatKeyPressed

    }//GEN-LAST:event_TabRawatKeyPressed

    private void tbJMRawatInapMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbJMRawatInapMouseClicked
        if (tabModeRawatInap.getRowCount() != 0) {
            try {
                getDataRawatInap();
                if (tbJMRawatInap.getSelectedRow() != -1) {
                    String noRawat = tbJMRawatInap.getValueAt(tbJMRawatInap.getSelectedRow(), 4).toString().trim();
                    muatNotaBilling(noRawat);
                }
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbJMRawatInapMouseClicked

    private void tbJMRawatInapPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tbJMRawatInapPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_tbJMRawatInapPropertyChange

    private void tbJMRawatInapKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbJMRawatInapKeyPressed
        if (tabModeRawatInap.getRowCount() != 0) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                getDataRawatInap();
                if (tbJMRawatInap.getSelectedRow() != -1) {
                    String noRawat = tbJMRawatInap.getValueAt(tbJMRawatInap.getSelectedRow(), 4).toString().trim();
                    muatNotaBilling(noRawat);
                }
            }
        }
    }//GEN-LAST:event_tbJMRawatInapKeyPressed

    private void Periode1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Periode1ItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_Periode1ItemStateChanged

    private void Periode1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Periode1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Periode1KeyPressed

    private void Period2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_Period2ItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_Period2ItemStateChanged

    private void Period2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Period2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Period2KeyPressed

    private void MnRincianJMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnRincianJMActionPerformed
        int tabIdx = TabRawat.getSelectedIndex();
        JTable table = (tabIdx == 0 ? tbJMRawatJalan : tbJMRawatInap);
        int selectedRowRincian = table.getSelectedRow();
        if (selectedRowRincian == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih salah satu data pasien pada tabel terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String targetSep = table.getValueAt(selectedRowRincian, 3) != null ? table.getValueAt(selectedRowRincian, 3).toString().trim() : "";
        String targetNoRawat = table.getValueAt(selectedRowRincian, 4) != null ? table.getValueAt(selectedRowRincian, 4).toString().trim() : "";
        String noRm = table.getValueAt(selectedRowRincian, 5) != null ? table.getValueAt(selectedRowRincian, 5).toString().trim() : "";
        String nmPasien = table.getValueAt(selectedRowRincian, 6) != null ? table.getValueAt(selectedRowRincian, 6).toString().trim() : "";
        String statusRawat = (tabIdx == 0 ? "Ralan" : "Ranap");

        List<DlgRincianJMBPJS.DokterSepItem> listDokter = new ArrayList<>();
        double sepUmbal = 0.0;
        double sepTotalPiutang = 0.0;

        for (int r = 0; r < table.getRowCount(); r++) {
            String curSep = table.getValueAt(r, 3) != null ? table.getValueAt(r, 3).toString().trim() : "";
            String curRawat = table.getValueAt(r, 4) != null ? table.getValueAt(r, 4).toString().trim() : "";

            boolean match = false;
            if (!targetSep.isEmpty() && !targetSep.equalsIgnoreCase("Belum di Grouping")) {
                match = curSep.equalsIgnoreCase(targetSep);
            } else {
                match = curRawat.equalsIgnoreCase(targetNoRawat);
            }

            if (match) {
                String kdDokter = table.getValueAt(r, 7) != null ? table.getValueAt(r, 7).toString().trim() : "";
                String nmDokter = table.getValueAt(r, 8) != null ? table.getValueAt(r, 8).toString().trim() : "";
                double totPiutang = table.getValueAt(r, 9) instanceof Number ? ((Number) table.getValueAt(r, 9)).doubleValue() : 0.0;
                double totTarifJm = table.getValueAt(r, 10) instanceof Number ? ((Number) table.getValueAt(r, 10)).doubleValue() : 0.0;
                double umbal = table.getValueAt(r, 11) instanceof Number ? ((Number) table.getValueAt(r, 11)).doubleValue() : 0.0;
                String keterangan = table.getValueAt(r, 13) != null ? table.getValueAt(r, 13).toString().trim() : "";

                String statusDokter = "";
                String statusOperasi = "";
                String jenisPct = "";
                String visitLos = "";

                if (tabIdx == 1) { // Ranap
                    statusDokter = table.getValueAt(r, 14) != null ? table.getValueAt(r, 14).toString().trim() : "";
                    statusOperasi = table.getValueAt(r, 15) != null ? table.getValueAt(r, 15).toString().trim() : "";
                    jenisPct = table.getValueAt(r, 17) != null ? table.getValueAt(r, 17).toString().trim() : "";
                    visitLos = table.getValueAt(r, 18) != null ? table.getValueAt(r, 18).toString().trim() : "";
                } else { // Ralan
                    statusDokter = table.getValueAt(r, 14) != null ? table.getValueAt(r, 14).toString().trim() : "";
                }

                double jmBpjs = table.getValueAt(r, 19) instanceof Number ? ((Number) table.getValueAt(r, 19)).doubleValue() : 0.0;

                DlgRincianJMBPJS.DokterSepItem docItem = new DlgRincianJMBPJS.DokterSepItem(
                        kdDokter, nmDokter, totPiutang, totTarifJm, umbal, jmBpjs,
                        keterangan, statusDokter, statusOperasi, jenisPct, visitLos
                );
                listDokter.add(docItem);

                if (umbal > sepUmbal) {
                    sepUmbal = umbal;
                }
                if (totPiutang > sepTotalPiutang) {
                    sepTotalPiutang = totPiutang;
                }
            }
        }

        DlgRincianJMBPJS dlgRincian = new DlgRincianJMBPJS(null, false);
        dlgRincian.setSize(internalFrame1.getWidth() - 30, internalFrame1.getHeight() - 40);
        dlgRincian.setLocationRelativeTo(internalFrame1);
        dlgRincian.tampilkanRincianSep(
                targetNoRawat, targetSep, noRm, nmPasien, statusRawat,
                sepTotalPiutang, sepUmbal, listDokter
        );
        dlgRincian.setVisible(true);
        return;
    }

    /* private void _unusedBlock1() {
        int tabIdx = TabRawat.getSelectedIndex();
        JTable table = (tabIdx == 0 ? tbJMRawatJalan : tbJMRawatInap);
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih salah satu data pasien pada tabel terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String targetSep = table.getValueAt(row, 3) != null ? table.getValueAt(row, 3).toString().trim() : "";
        String targetNoRawat = table.getValueAt(row, 4) != null ? table.getValueAt(row, 4).toString().trim() : "";
        String noRm = table.getValueAt(row, 5) != null ? table.getValueAt(row, 5).toString().trim() : "";
        String nmPasien = table.getValueAt(row, 6) != null ? table.getValueAt(row, 6).toString().trim() : "";
        String statusRawat = (tabIdx == 0 ? "Ralan" : "Ranap");

        // Kumpulkan seluruh dokter dalam 1 SEP yang sama (atau 1 No. Rawat jika SEP belum digrouping)
        List<DlgRincianJMBPJS.DokterSepItem> listDokter = new ArrayList<>();
        double sepUmbal = 0.0;
        double sepTotalPiutang = 0.0;

        for (int r = 0; r < table.getRowCount(); r++) {
            String curSep = table.getValueAt(r, 3) != null ? table.getValueAt(r, 3).toString().trim() : "";
            String curRawat = table.getValueAt(r, 4) != null ? table.getValueAt(r, 4).toString().trim() : "";

            boolean match = false;
            if (!targetSep.isEmpty() && !targetSep.equalsIgnoreCase("Belum di Grouping")) {
                match = curSep.equalsIgnoreCase(targetSep);
            } else {
                match = curRawat.equalsIgnoreCase(targetNoRawat);
            }

            if (match) {
                String kdDokter = table.getValueAt(r, 7) != null ? table.getValueAt(r, 7).toString().trim() : "";
                String nmDokter = table.getValueAt(r, 8) != null ? table.getValueAt(r, 8).toString().trim() : "";
                double totPiutang = table.getValueAt(r, 9) instanceof Number ? ((Number) table.getValueAt(r, 9)).doubleValue() : 0.0;
                double totTarifJm = table.getValueAt(r, 10) instanceof Number ? ((Number) table.getValueAt(r, 10)).doubleValue() : 0.0;
                double umbal = table.getValueAt(r, 11) instanceof Number ? ((Number) table.getValueAt(r, 11)).doubleValue() : 0.0;
                String keterangan = table.getValueAt(r, 13) != null ? table.getValueAt(r, 13).toString().trim() : "";

                String statusDokter = "";
                String statusOperasi = "";
                String jenisPct = "";
                String visitLos = "";

                if (tabIdx == 1) { // Ranap
                    statusDokter = table.getValueAt(r, 14) != null ? table.getValueAt(r, 14).toString().trim() : "";
                    statusOperasi = table.getValueAt(r, 15) != null ? table.getValueAt(r, 15).toString().trim() : "";
                    jenisPct = table.getValueAt(r, 17) != null ? table.getValueAt(r, 17).toString().trim() : "";
                    visitLos = table.getValueAt(r, 18) != null ? table.getValueAt(r, 18).toString().trim() : "";
                } else { // Ralan
                    statusDokter = table.getValueAt(r, 14) != null ? table.getValueAt(r, 14).toString().trim() : "";
                }

                double jmBpjs = table.getValueAt(r, 19) instanceof Number ? ((Number) table.getValueAt(r, 19)).doubleValue() : 0.0;

                DlgRincianJMBPJS.DokterSepItem docItem = new DlgRincianJMBPJS.DokterSepItem(
                        kdDokter, nmDokter, totPiutang, totTarifJm, umbal, jmBpjs,
                        keterangan, statusDokter, statusOperasi, jenisPct, visitLos
                );
                listDokter.add(docItem);

                if (umbal > sepUmbal) {
                    sepUmbal = umbal;
                }
                if (totPiutang > sepTotalPiutang) {
                    sepTotalPiutang = totPiutang;
                }
            }
        }

        DlgRincianJMBPJS dlgRincian = new DlgRincianJMBPJS(null, false);
        dlgRincian.setSize(internalFrame1.getWidth() - 30, internalFrame1.getHeight() - 40);
        dlgRincian.setLocationRelativeTo(internalFrame1);
        dlgRincian.tampilkanRincianSep(
                targetNoRawat, targetSep, noRm, nmPasien, statusRawat,
                sepTotalPiutang, sepUmbal, listDokter
        );
        dlgRincian.setVisible(true);

        String noSep = table.getValueAt(row, 3) != null ? table.getValueAt(row, 3).toString().trim() : "";
        String noRawat = table.getValueAt(row, 4) != null ? table.getValueAt(row, 4).toString().trim() : "";
        String noRm = table.getValueAt(row, 5) != null ? table.getValueAt(row, 5).toString().trim() : "";
        String nmPasien = table.getValueAt(row, 6) != null ? table.getValueAt(row, 6).toString().trim() : "";
        String kdDokter = table.getValueAt(row, 7) != null ? table.getValueAt(row, 7).toString().trim() : "";
        String nmDokter = table.getValueAt(row, 8) != null ? table.getValueAt(row, 8).toString().trim() : "";
        double totalPiutang = table.getValueAt(row, 9) instanceof Number ? ((Number) table.getValueAt(row, 9)).doubleValue() : 0.0;
        double totalTarifJm = table.getValueAt(row, 10) instanceof Number ? ((Number) table.getValueAt(row, 10)).doubleValue() : 0.0;
        double umbal = table.getValueAt(row, 11) instanceof Number ? ((Number) table.getValueAt(row, 11)).doubleValue() : 0.0;
        String statusRawat = (tabIdx == 0 ? "Ralan" : "Ranap");
        String keterangan = table.getValueAt(row, 13) != null ? table.getValueAt(row, 13).toString().trim() : "";

        String statusDokter = "";
        String statusOperasi = "";
        String jenisPct = "";
        String visitLos = "";

        if (tabIdx == 1) { // Ranap
            statusDokter = table.getValueAt(row, 14) != null ? table.getValueAt(row, 14).toString().trim() : "";
            statusOperasi = table.getValueAt(row, 15) != null ? table.getValueAt(row, 15).toString().trim() : "";
            jenisPct = table.getValueAt(row, 17) != null ? table.getValueAt(row, 17).toString().trim() : "";
            visitLos = table.getValueAt(row, 18) != null ? table.getValueAt(row, 18).toString().trim() : "";
        }

        double jmBpjs = table.getValueAt(row, 19) instanceof Number ? ((Number) table.getValueAt(row, 19)).doubleValue() : 0.0;

        DlgRincianJMBPJS dlgRincian = new DlgRincianJMBPJS(null, false);
        dlgRincian.setSize(internalFrame1.getWidth() - 30, internalFrame1.getHeight() - 40);
        dlgRincian.setLocationRelativeTo(internalFrame1);
        dlgRincian.tampilkanRincian(
                noRawat, noSep, noRm, nmPasien, kdDokter, nmDokter, statusRawat,
                totalPiutang, totalTarifJm, umbal, jmBpjs,
                keterangan, statusDokter, statusOperasi, jenisPct, visitLos
        );
        dlgRincian.setVisible(true);
    }//GEN-LAST:event_MnRincianJMActionPerformed

    } // close _unusedBlock1 */

    private void MnDetailHarianPasienActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnDetailHarianPasienActionPerformed
        int tabIdx = TabRawat.getSelectedIndex();
        JTable table = (tabIdx == 0 ? tbJMRawatJalan : tbJMRawatInap);
        int selectedRowDetail = table.getSelectedRow();
        if (selectedRowDetail == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih salah satu data pasien pada tabel terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tglPiutang = table.getValueAt(selectedRowDetail, 1) != null ? table.getValueAt(selectedRowDetail, 1).toString().trim() : "";
        String noSep = table.getValueAt(selectedRowDetail, 3) != null ? table.getValueAt(selectedRowDetail, 3).toString().trim() : "";
        String noRawat = table.getValueAt(selectedRowDetail, 4) != null ? table.getValueAt(selectedRowDetail, 4).toString().trim() : "";
        String noRm = table.getValueAt(selectedRowDetail, 5) != null ? table.getValueAt(selectedRowDetail, 5).toString().trim() : "";
        String statusRawat = (tabIdx == 0 ? "Ralan" : "Ranap");

        String tglBilling = Sequel.cariIsi("select tgl_byr from billing where no_rawat=? and no='No.Nota' limit 1", noRawat);
        String tglAwal = Valid.SetTgl(Periode1.getSelectedItem() + "");
        String tglAkhir = Valid.SetTgl(Period2.getSelectedItem() + "");

        if (tglBilling != null && !tglBilling.trim().isEmpty()) {
            tglAwal = tglBilling.trim();
            tglAkhir = tglBilling.trim();
        } else if (!tglPiutang.isEmpty() && !"-".equals(tglPiutang)) {
            tglAwal = tglPiutang;
            tglAkhir = tglPiutang;
        }

        DlgDetailHarianPasien dialogDetail = new DlgDetailHarianPasien(null, false);
        dialogDetail.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        dialogDetail.setLocationRelativeTo(internalFrame1);
        dialogDetail.setPasien(tglAwal, tglAkhir, noSep, noRawat, noRm, statusRawat);
        dialogDetail.setVisible(true);
        return;
    }

    /* private void _unusedBlock2() {
        int tabIdx = TabRawat.getSelectedIndex();
        JTable table = (tabIdx == 0 ? tbJMRawatJalan : tbJMRawatInap);
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Silakan pilih salah satu data pasien pada tabel terlebih dahulu!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String targetSep = table.getValueAt(row, 3) != null ? table.getValueAt(row, 3).toString().trim() : "";
        String targetNoRawat = table.getValueAt(row, 4) != null ? table.getValueAt(row, 4).toString().trim() : "";
        String noRm = table.getValueAt(row, 5) != null ? table.getValueAt(row, 5).toString().trim() : "";
        String nmPasien = table.getValueAt(row, 6) != null ? table.getValueAt(row, 6).toString().trim() : "";
        String statusRawat = (tabIdx == 0 ? "Ralan" : "Ranap");

        // Kumpulkan seluruh dokter dalam 1 SEP yang sama (atau 1 No. Rawat jika SEP belum digrouping)
        List<DlgRincianJMBPJS.DokterSepItem> listDokter = new ArrayList<>();
        double sepUmbal = 0.0;
        double sepTotalPiutang = 0.0;

        for (int r = 0; r < table.getRowCount(); r++) {
            String curSep = table.getValueAt(r, 3) != null ? table.getValueAt(r, 3).toString().trim() : "";
            String curRawat = table.getValueAt(r, 4) != null ? table.getValueAt(r, 4).toString().trim() : "";

            boolean match = false;
            if (!targetSep.isEmpty() && !targetSep.equalsIgnoreCase("Belum di Grouping")) {
                match = curSep.equalsIgnoreCase(targetSep);
            } else {
                match = curRawat.equalsIgnoreCase(targetNoRawat);
            }

            if (match) {
                String kdDokter = table.getValueAt(r, 7) != null ? table.getValueAt(r, 7).toString().trim() : "";
                String nmDokter = table.getValueAt(r, 8) != null ? table.getValueAt(r, 8).toString().trim() : "";
                double totPiutang = table.getValueAt(r, 9) instanceof Number ? ((Number) table.getValueAt(r, 9)).doubleValue() : 0.0;
                double totTarifJm = table.getValueAt(r, 10) instanceof Number ? ((Number) table.getValueAt(r, 10)).doubleValue() : 0.0;
                double umbal = table.getValueAt(r, 11) instanceof Number ? ((Number) table.getValueAt(r, 11)).doubleValue() : 0.0;
                String keterangan = table.getValueAt(r, 13) != null ? table.getValueAt(r, 13).toString().trim() : "";

                String statusDokter = "";
                String statusOperasi = "";
                String jenisPct = "";
                String visitLos = "";

                if (tabIdx == 1) { // Ranap
                    statusDokter = table.getValueAt(r, 14) != null ? table.getValueAt(r, 14).toString().trim() : "";
                    statusOperasi = table.getValueAt(r, 15) != null ? table.getValueAt(r, 15).toString().trim() : "";
                    jenisPct = table.getValueAt(r, 17) != null ? table.getValueAt(r, 17).toString().trim() : "";
                    visitLos = table.getValueAt(r, 18) != null ? table.getValueAt(r, 18).toString().trim() : "";
                } else { // Ralan
                    statusDokter = table.getValueAt(r, 14) != null ? table.getValueAt(r, 14).toString().trim() : "";
                }

                double jmBpjs = table.getValueAt(r, 19) instanceof Number ? ((Number) table.getValueAt(r, 19)).doubleValue() : 0.0;

                DlgRincianJMBPJS.DokterSepItem docItem = new DlgRincianJMBPJS.DokterSepItem(
                        kdDokter, nmDokter, totPiutang, totTarifJm, umbal, jmBpjs,
                        keterangan, statusDokter, statusOperasi, jenisPct, visitLos
                );
                listDokter.add(docItem);

                if (umbal > sepUmbal) {
                    sepUmbal = umbal;
                }
                if (totPiutang > sepTotalPiutang) {
                    sepTotalPiutang = totPiutang;
                }
            }
        }

        DlgRincianJMBPJS dlgRincian = new DlgRincianJMBPJS(null, false);
        dlgRincian.setSize(internalFrame1.getWidth() - 30, internalFrame1.getHeight() - 40);
        dlgRincian.setLocationRelativeTo(internalFrame1);
        dlgRincian.tampilkanRincianSep(
                targetNoRawat, targetSep, noRm, nmPasien, statusRawat,
                sepTotalPiutang, sepUmbal, listDokter
        );
        dlgRincian.setVisible(true);

        String noSep = table.getValueAt(row, 3) != null ? table.getValueAt(row, 3).toString().trim() : "";
        String noRawat = table.getValueAt(row, 4) != null ? table.getValueAt(row, 4).toString().trim() : "";
        String noRm = table.getValueAt(row, 5) != null ? table.getValueAt(row, 5).toString().trim() : "";
        String statusRawat = (tabIdx == 0 ? "Ralan" : "Ranap");

        // Cari tanggal billing riil untuk no_rawat ini agar rentang tanggal di DlgDetailHarianPasien presisi
        String tglBilling = Sequel.cariIsi("select tgl_byr from billing where no_rawat=? and no='No.Nota' limit 1", noRawat);
        String tglAwal = Valid.SetTgl(Periode1.getSelectedItem() + "");
        String tglAkhir = Valid.SetTgl(Period2.getSelectedItem() + "");

        if (tglBilling != null && !tglBilling.trim().isEmpty()) {
            tglAwal = tglBilling.trim();
            tglAkhir = tglBilling.trim();
        } else if (!tglPiutang.isEmpty() && !"-".equals(tglPiutang)) {
            tglAwal = tglPiutang;
            tglAkhir = tglPiutang;
        }

        DlgDetailHarianPasien dialogDetail = new DlgDetailHarianPasien(null, false);
        dialogDetail.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        dialogDetail.setLocationRelativeTo(internalFrame1);
        dialogDetail.setPasien(tglAwal, tglAkhir, noSep, noRawat, noRm, statusRawat);
        dialogDetail.setVisible(true);
    }//GEN-LAST:event_MnDetailHarianPasienActionPerformed

    } // close _unusedBlock2 */

    private void MnDetailPiutangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnDetailPiutangActionPerformed

        if (TabRawat.getSelectedIndex() == 0) {
            DlgBilingRalan billing = new DlgBilingRalan(null, false);
            billing.TNoRw.setText(tbJMRawatJalan.getValueAt(tbJMRawatJalan.getSelectedRow(), 4).toString());
            billing.isCek();
            billing.isRawat();
            if (Sequel.cariInteger("select count(no_rawat) from piutang_pasien where no_rawat=?", billing.TNoRw.getText()) > 0) {
                billing.setPiutang();
            }
            billing.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            billing.setLocationRelativeTo(internalFrame1);
            billing.setVisible(true);
        } else {
            getBilranap().TNoRw.setText(tbJMRawatInap.getValueAt(tbJMRawatInap.getSelectedRow(), 4).toString());
            getBilranap().isCek();
            getBilranap().isRawat();
            getBilranap().setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            getBilranap().setLocationRelativeTo(internalFrame1);
            getBilranap().setVisible(true);
        }

    }//GEN-LAST:event_MnDetailPiutangActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            KeuanganBayarJMDokterBPJS dialog = new KeuanganBayarJMDokterBPJS(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.ComboBox AkunBayar;
    private widget.Button BtnAll;
    private widget.Button BtnAll1;
    private widget.Button BtnCari;
    private widget.Button BtnCari1;
    private widget.ComboBox CmbStatusKlaim;
    private widget.ComboBox cmbCOB;
    private widget.Button BtnAturanJM;
    private widget.Button BtnExport;
    private widget.Button BtnGaji;
    private widget.Button BtnKeluar;
    private widget.Button BtnPetugas;
    private widget.Button BtnSimpan;
    private widget.TextBox KdCaraBayar;
    private widget.TextBox Keterangan;
    private javax.swing.JLabel LCount;
    private javax.swing.JLabel LCount1;
    private javax.swing.JMenuItem MnDetailPiutang;
    private javax.swing.JMenuItem MnDetailHarianPasien;
    private javax.swing.JMenuItem MnRincianJM;
    private widget.TextBox NoTagihan;
    private widget.Tanggal Period2;
    private widget.Tanggal Periode1;
    private javax.swing.JPopupMenu Popup;
    private widget.ScrollPane Scroll1;
    private widget.ScrollPane Scroll2;
    private widget.TextBox TCari;
    private widget.TextBox TKd;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Tanggal Tanggal;
    private widget.ComboBox cmbStatus;
    private widget.InternalFrame internalFrame1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel16;
    private widget.Label jLabelStatusKlaim;
    private widget.Label jLabelCOB;
    private javax.swing.JPanel jPanel1;
    private widget.TextBox kddokter;
    private widget.Label label16;
    private widget.Label label17;
    private widget.Label label32;
    private widget.Label label36;
    private widget.Label label39;
    private widget.Label lblNoRawat;
    private widget.TextBox nmdokter;
    private widget.panelisi panelInvoices;
    private widget.panelisi panelisi1;
    private widget.panelisi panelisi3;
    private widget.panelisi panelisi4;
    private javax.swing.JMenuItem ppBersihkan;
    private javax.swing.JMenuItem ppSemua;
    private widget.Table tbJMRawatInap;
    private widget.Table tbJMRawatJalan;
    // End of variables declaration//GEN-END:variables

    private void CmbStatusKlaimItemStateChanged(java.awt.event.ItemEvent evt) {
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            BtnCariActionPerformed(null);
        }
    }

    private void cmbCOBItemStateChanged(java.awt.event.ItemEvent evt) {
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            BtnCariActionPerformed(null);
        }
    }

    private void tampilStatusKlaim() {
        if (CmbStatusKlaim == null) {
            return;
        }
        try {
            PreparedStatement psstatusklaim = koneksi.prepareStatement("select inacbg_klaim_umpanbalik.status from inacbg_klaim_umpanbalik group by 1");
            try {
                ResultSet rsstatusklaim = psstatusklaim.executeQuery();
                CmbStatusKlaim.removeAllItems();
                CmbStatusKlaim.addItem("Semua");
                while (rsstatusklaim.next()) {
                    String st = rsstatusklaim.getString("status");
                    if (st != null && !st.trim().isEmpty()) {
                        CmbStatusKlaim.addItem(st);
                    }
                }
            } catch (Exception e) {
                System.out.println("Notifikasi status klaim: " + e);
            } finally {
                if (psstatusklaim != null) {
                    psstatusklaim.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi status klaim: " + e);
        }
    }

    private static boolean isAvShuntText(String text) {
        if (text == null) return false;
        String clean = text.toUpperCase(Locale.ROOT).replace("-", " ").replace("_", " ").replaceAll("\\s+", " ");
        return clean.contains("AV SHUNT") || clean.contains("AVSHUNT") || clean.contains("CIMINO")
                || clean.contains("ARTERIOVENOUS SHUNT") || clean.contains("AV FISTULA") || clean.contains("AVF ");
    }

    private static boolean isDoubleLumenText(String text) {
        if (text == null) return false;
        String clean = text.toUpperCase(Locale.ROOT).replace("-", " ").replace("_", " ").replaceAll("\\s+", " ");
        return clean.contains("DOUBLE LUMEN") || clean.contains("DOUBLELUMEN") || clean.contains("CDL")
                || clean.contains("KATETER DOUBLE LUMEN") || clean.contains("CATHETER DOUBLE LUMEN");
    }

    private static boolean isEswlText(String text) {
        if (text == null) return false;
        String clean = text.toUpperCase(Locale.ROOT).replace("-", " ").replace("_", " ").replaceAll("\\s+", " ");
        return clean.contains("ESWL") || clean.contains("LITHOTRIPSY") || clean.contains("LITHOTRIPTOR");
    }

    private static boolean isHdSingleUseText(String text) {
        if (text == null) return false;
        String clean = text.toUpperCase(Locale.ROOT).replace("-", " ").replace("_", " ").replaceAll("\\s+", " ");
        return clean.contains("SINGLE USE") || clean.contains("SINGLEUSE")
                || (clean.contains("HEMODIALISA") && clean.contains("SINGLE"));
    }

    public static boolean isDokterBebasAturanBPJS(String kdDokter, String nmDokter, String nmPoli, Map<String, String> mapSpesialis) {
        String sps = (mapSpesialis != null && kdDokter != null) ? mapSpesialis.getOrDefault(kdDokter, "").toLowerCase(Locale.ROOT) : "";
        if (sps.contains("radiologi") || sps.contains("bedah mulut") || sps.contains("bmm")) {
            return true;
        }
        return isDokterBebasAturanBPJS(nmDokter, nmPoli);
    }

    public static boolean isDokterBebasAturanBPJS(String nmDokter, String nmPoli) {
        String dr = (nmDokter != null ? nmDokter.toLowerCase(Locale.ROOT) : "");
        String poli = (nmPoli != null ? nmPoli.toLowerCase(Locale.ROOT) : "");

        boolean isRadiologi = dr.contains("sp.rad") || dr.contains("sp. rad") || dr.contains("radiologi")
                || poli.contains("radiologi");
        boolean isBedahMulut = dr.contains("sp.bm") || dr.contains("sp. bm") || dr.contains("sp.b.m") || dr.contains("sp.bmm") || dr.contains("sp. bmm") || dr.contains("sp.b.m.m")
                || dr.contains("bedah mulut") || poli.contains("bedah mulut");

        return isRadiologi || isBedahMulut;
    }

private static boolean isPemeriksaanText(String nmPrw, String katPrw) {
        String nmUpper = nmPrw != null ? nmPrw.toUpperCase(Locale.ROOT) : "";
        String katUpper = katPrw != null ? katPrw.toUpperCase(Locale.ROOT) : "";
        return katUpper.contains("VISIT & KONSULTASI")
                || katUpper.contains("KONSULTASI")
                || katUpper.contains("PEMERIKSAAN")
                || nmUpper.contains("PEMERIKSAAN")
                || nmUpper.contains("KONSULTASI")
                || nmUpper.contains("KONSUL")
                || nmUpper.contains("PERIKSA");
    }

    private boolean isFilterStatusKlaimPass(String stKlaim) {
        if (CmbStatusKlaim == null || CmbStatusKlaim.getSelectedItem() == null) {
            return true;
        }
        String selected = CmbStatusKlaim.getSelectedItem().toString().trim();
        if (selected.equalsIgnoreCase("Semua") || selected.isEmpty()) {
            return true;
        }
        if (stKlaim == null) {
            stKlaim = "";
        }
        return selected.equalsIgnoreCase(stKlaim.trim());
    }

    private boolean isFilterCOBPass(boolean isCob) {
        if (cmbCOB == null || cmbCOB.getSelectedItem() == null) {
            return true;
        }
        String selected = cmbCOB.getSelectedItem().toString();
        if (selected.equalsIgnoreCase("Hanya COB") || selected.equalsIgnoreCase("COB")) {
            return isCob;
        } else if (selected.equalsIgnoreCase("Non COB")) {
            return !isCob;
        }
        return true; // "Semua"
    }

    public String isCekCOB(String norawat) {
        if (norawat == null || norawat.trim().isEmpty()) {
            return "";
        }
        StringBuilder hasilCOB = new StringBuilder();
        PreparedStatement pscekCOB = null;
        ResultSet rsCekCOB = null;

        try {
            pscekCOB = koneksi.prepareStatement(
                    "SELECT nama_bayar, kd_pj FROM detail_piutang_pasien WHERE no_rawat = ?"
            );
            pscekCOB.setString(1, norawat);
            rsCekCOB = pscekCOB.executeQuery();

            int rowCount = 0;
            while (rsCekCOB.next()) {
                if (rowCount > 0) {
                    hasilCOB.append(" | ");
                }
                hasilCOB.append(rsCekCOB.getString("nama_bayar"));
                rowCount++;
            }

            return rowCount >= 2 ? "COB (" + hasilCOB.toString() + ")" : "";
        } catch (SQLException e) {
            System.err.println("Error checking COB for norawat: " + norawat + " - " + e.getMessage());
            return "";
        } finally {
            try {
                if (rsCekCOB != null) {
                    rsCekCOB.close();
                }
                if (pscekCOB != null) {
                    pscekCOB.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }

    private void prosesCari() {
        if (TabRawat.getSelectedIndex() == 0) {
            tampilRawatJalan();
        } else {
            tampilRawatInap();
        }
    }

    private void getData() {
        row = tbJMRawatJalan.getRowCount();
        bayar = 0;
        totalrawatjalan = 0;
        totalrawatinap = 0;
        totallabrawatjalan = 0;
        totallabrawatinap = 0;
        totalradrawatjalan = 0;
        totalradrawatinap = 0;
        totaloperasirawatjalan = 0;
        totaloperasirawatinap = 0;

        if (tbJMRawatJalan.getSelectedRow() != -1) {
            lblNoRawat.setText(tbJMRawatJalan.getValueAt(tbJMRawatJalan.getSelectedRow(), 4).toString());
        }

        for (i = 0; i < row; i++) {
            if (tbJMRawatJalan.getValueAt(i, 0).toString().equals("true")) {
                bayar = bayar + Double.parseDouble(tbJMRawatJalan.getValueAt(i, 19).toString());
            }
        }

        LCount1.setText(Valid.SetAngka(bayar));
    }

    private void getDataRawatInap() {
        row = tbJMRawatInap.getRowCount();
        bayar = 0;
        totalrawatjalan = 0;
        totalrawatinap = 0;
        totallabrawatjalan = 0;
        totallabrawatinap = 0;
        totalradrawatjalan = 0;
        totalradrawatinap = 0;
        totaloperasirawatjalan = 0;
        totaloperasirawatinap = 0;

        if (tbJMRawatInap.getSelectedRow() != -1) {
            lblNoRawat.setText(tbJMRawatInap.getValueAt(tbJMRawatInap.getSelectedRow(), 4).toString());
        }

        for (i = 0; i < row; i++) {
            if (tbJMRawatInap.getValueAt(i, 0).toString().equals("true")) {
                bayar = bayar + Double.parseDouble(tbJMRawatInap.getValueAt(i, 19).toString());
            }
        }
        LCount1.setText(Valid.SetAngka(bayar));
    }

//    private void autoNomor() {
//        Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(bayar_jm_dokter.no_bayar,3),signed)),0) from bayar_jm_dokter where bayar_jm_dokter.tanggal='" + Valid.SetTgl(Tanggal.getSelectedItem() + "") + "' ",
//                "JMD" + Tanggal.getSelectedItem().toString().substring(6, 10) + Tanggal.getSelectedItem().toString().substring(3, 5) + Tanggal.getSelectedItem().toString().substring(0, 2), 3, NoTagihan);
//    }
    private void autoNomor() {
        if (Tanggal.getSelectedItem() != null) {
            Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(bayar_jm_dokter.no_bayar,3),signed)),0) from bayar_jm_dokter where bayar_jm_dokter.tanggal='" + Valid.SetTgl(Tanggal.getSelectedItem() + "") + "' ",
                    "JMD" + Tanggal.getSelectedItem().toString().substring(6, 10) + Tanggal.getSelectedItem().toString().substring(3, 5) + Tanggal.getSelectedItem().toString().substring(0, 2), 3, NoTagihan);
        } else {
            // Use current date when Tanggal is null
            java.util.Date currentDate = new java.util.Date();
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = dateFormat.format(currentDate);
            Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(bayar_jm_dokter.no_bayar,3),signed)),0) from bayar_jm_dokter where bayar_jm_dokter.tanggal='" + Valid.SetTgl(formattedDate) + "' ",
                    "JMD" + formattedDate.substring(0, 4) + formattedDate.substring(5, 7) + formattedDate.substring(8, 10), 3, NoTagihan);
        }
    }

    public void isCek() {
        autoNomor();
        TCari.requestFocus();
        BtnSimpan.setEnabled(akses.getbayar_jm_dokter());
    }

    private void tampilAkunBayar() {
        try {
            getCariBayarJMDokter().file = new File("./cache/akunbayarhutang.iyem");
            getCariBayarJMDokter().file.createNewFile();
            getCariBayarJMDokter().fileWriter = new FileWriter(getCariBayarJMDokter().file);
            getCariBayarJMDokter().iyem = "";
            ps = koneksi.prepareStatement("select * from akun_bayar_hutang order by akun_bayar_hutang.nama_bayar");
            try {
                rs = ps.executeQuery();
                AkunBayar.removeAllItems();
                while (rs.next()) {
                    AkunBayar.addItem(rs.getString(1).replaceAll("\"", ""));
                    getCariBayarJMDokter().iyem = getCariBayarJMDokter().iyem + "{\"NamaAkun\":\"" + rs.getString(1).replaceAll("\"", "") + "\",\"KodeRek\":\"" + rs.getString(2) + "\"},";
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void tampilAkunBayar2() {
        try {
            getCariBayarJMDokter().myObj = new FileReader("./cache/akunbayarhutang.iyem");
            getCariBayarJMDokter().root = getCariBayarJMDokter().mapper.readTree(getCariBayarJMDokter().myObj);
            getCariBayarJMDokter().response = getCariBayarJMDokter().root.path("akunbayarhutang");
            if (getCariBayarJMDokter().response.isArray()) {
                for (JsonNode list : getCariBayarJMDokter().response) {
                    AkunBayar.addItem(list.path("NamaAkun").asText().replaceAll("\"", ""));
                }
            }
            getCariBayarJMDokter().myObj.close();
        } catch (Exception ex) {
            System.out.println("Notifikasi : " + ex);
        }
    }

    private void jalankanWorkerRawatJalan() {
        if (workerRawatJalan != null && !workerRawatJalan.isDone()) {
            workerRawatJalan.cancel(true);
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        BtnCari.setEnabled(false);

        final String tglAwal = Valid.SetTgl(Periode1.getSelectedItem() + "");
        final String tglAkhir = Valid.SetTgl(Period2.getSelectedItem() + "");
        final String keyword = "%" + TCari.getText().trim() + "%";
        final String targetKdDokter = kddokter.getText().trim();
        final String targetTCari = TCari.getText().trim().toLowerCase(Locale.ROOT);

        workerRawatJalan = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> resultRows = new ArrayList<>();

                // 1. Pre-cache nama pegawai, dokter, spesialis (Dokter Umum vs Spesialis), dan mapping DPJP VClaim
                Map<String, String> mapPegawai = new HashMap<>();
                Map<String, Boolean> mapDokterUmum = new HashMap<>();
                Map<String, String> mapDpjpVClaimToRs = new HashMap<>();
                Map<String, String> mapSpesialis = new HashMap<>();

                try (PreparedStatement psPeg = koneksi.prepareStatement("SELECT nik, nama FROM pegawai");
                     ResultSet rsPeg = psPeg.executeQuery()) {
                    while (rsPeg.next()) {
                        mapPegawai.put(rsPeg.getString("nik"), rsPeg.getString("nama"));
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Cache Pegawai : " + e);
                }

                try (PreparedStatement psDok = koneksi.prepareStatement(
                        "SELECT dokter.kd_dokter, dokter.nm_dokter, spesialis.nm_sps "
                        + "FROM dokter LEFT JOIN spesialis ON dokter.kd_sps = spesialis.kd_sps");
                     ResultSet rsDok = psDok.executeQuery()) {
                    while (rsDok.next()) {
                        String kd = rsDok.getString("kd_dokter");
                        String nm = rsDok.getString("nm_dokter");
                        String sps = rsDok.getString("nm_sps");
                        mapPegawai.put(kd, nm);
                        boolean isUmum = sps != null && sps.toLowerCase(Locale.ROOT).contains("umum");
                        mapDokterUmum.put(kd, isUmum);
                        if (sps != null) {
                            mapSpesialis.put(kd, sps);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Cache Dokter : " + e);
                }

                try (PreparedStatement psMap = koneksi.prepareStatement(
                        "SELECT kd_dokter, kd_dokter_bpjs FROM maping_dokter_dpjpvclaim");
                     ResultSet rsMap = psMap.executeQuery()) {
                    while (rsMap.next()) {
                        String kdBpjs = rsMap.getString("kd_dokter_bpjs");
                        String kdRs = rsMap.getString("kd_dokter");
                        if (kdBpjs != null && kdRs != null) {
                            mapDpjpVClaimToRs.put(kdBpjs, kdRs);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Cache Mapping DPJP : " + e);
                }

                if (isCancelled()) {
                    return resultRows;
                }

                // 2. Query data piutang rawat jalan
                String sqlMain = "SELECT "
                        + "piutang_pasien.no_rawat, "
                        + "piutang_pasien.tgl_piutang, "
                        + "reg_periksa.jam_reg, "
                        + "bridging_sep.no_sep, "
                        + "piutang_pasien.no_rkm_medis, "
                        + "pasien.nm_pasien, "
                        + "reg_periksa.kd_dokter, "
                        + "dokter.nm_dokter, "
                        + "detail_piutang_pasien.totalpiutang, "
                        + "bridging_sep.kddpjp, "
                        + "IFNULL(inacbg_klaim_umpanbalik.bydisetujui, '0') AS umbal, "
                        + "IFNULL(inacbg_klaim_umpanbalik.status, '') AS statusklaim, "
                        + "reg_periksa.kd_poli, "
                        + "poliklinik.nm_poli "
                        + "FROM piutang_pasien "
                        + "INNER JOIN reg_periksa ON piutang_pasien.no_rawat = reg_periksa.no_rawat "
                        + "INNER JOIN pasien ON piutang_pasien.no_rkm_medis = pasien.no_rkm_medis "
                        + "INNER JOIN poliklinik ON reg_periksa.kd_poli = poliklinik.kd_poli "
                        + "INNER JOIN dokter ON reg_periksa.kd_dokter = dokter.kd_dokter "
                        + "INNER JOIN detail_piutang_pasien ON piutang_pasien.no_rawat = detail_piutang_pasien.no_rawat "
                        + "INNER JOIN penjab ON detail_piutang_pasien.kd_pj = penjab.kd_pj "
                        + "INNER JOIN bridging_sep ON detail_piutang_pasien.no_rawat = bridging_sep.no_rawat "
                        + "    AND ((reg_periksa.status_lanjut = 'Ralan' AND bridging_sep.jnspelayanan = '2') "
                        + "         OR (reg_periksa.status_lanjut = 'Ranap' AND bridging_sep.jnspelayanan = '1')) "
                        + "LEFT JOIN inacbg_klaim_umpanbalik ON bridging_sep.no_sep = inacbg_klaim_umpanbalik.no_sep "
                        + "WHERE piutang_pasien.tgl_piutang BETWEEN ? AND ? "
                        + "AND detail_piutang_pasien.sisapiutang <= 0 "
                        + "AND reg_periksa.status_lanjut = 'Ralan' "
                        + "AND (piutang_pasien.no_rawat LIKE ? OR piutang_pasien.no_rkm_medis LIKE ? OR pasien.nm_pasien LIKE ? OR bridging_sep.no_sep LIKE ?) "
                        + "ORDER BY bridging_sep.no_sep ASC, piutang_pasien.tgl_piutang ASC";

                List<RalanCandidate> candidates = new ArrayList<>();

                try (PreparedStatement psMain = koneksi.prepareStatement(sqlMain)) {
                    psMain.setString(1, tglAwal);
                    psMain.setString(2, tglAkhir);
                    psMain.setString(3, keyword);
                    psMain.setString(4, keyword);
                    psMain.setString(5, keyword);
                    psMain.setString(6, keyword);

                    try (ResultSet rsMain = psMain.executeQuery()) {
                        while (rsMain.next()) {
                            if (isCancelled()) {
                                return resultRows;
                            }
                            RalanCandidate c = new RalanCandidate();
                            c.noRawat = rsMain.getString("no_rawat");
                            c.tglPiutang = rsMain.getString("tgl_piutang");
                            c.jamReg = rsMain.getString("jam_reg");
                            c.noSep = rsMain.getString("no_sep") != null ? rsMain.getString("no_sep") : "";
                            c.noRkmMedis = rsMain.getString("no_rkm_medis");
                            c.nmPasien = rsMain.getString("nm_pasien");
                            c.kdDokter = rsMain.getString("kd_dokter");
                            c.nmDokter = rsMain.getString("nm_dokter");
                            c.totalPiutang = rsMain.getDouble("totalpiutang");
                            c.umbal = rsMain.getDouble("umbal");
                            c.statusKlaim = rsMain.getString("statusklaim");
                            c.kdDpjp = rsMain.getString("kddpjp") != null ? rsMain.getString("kddpjp") : "";
                            c.kdPoli = rsMain.getString("kd_poli") != null ? rsMain.getString("kd_poli") : "";
                            c.nmPoli = rsMain.getString("nm_poli") != null ? rsMain.getString("nm_poli") : "";

                            candidates.add(c);
                        }
                    }
                }

                if (isCancelled() || candidates.isEmpty()) {
                    return resultRows;
                }

                // 3. Ekstraksi tindakan per baris secara efisien dengan PreparedStatements reusable
                String sqlRalanDr = "SELECT rawat_jl_dr.kd_jenis_prw, jns_perawatan.nm_perawatan, "
                        + "kategori_perawatan.nm_kategori, rawat_jl_dr.tarif_tindakandr "
                        + "FROM rawat_jl_dr "
                        + "INNER JOIN jns_perawatan ON rawat_jl_dr.kd_jenis_prw = jns_perawatan.kd_jenis_prw "
                        + "INNER JOIN kategori_perawatan ON jns_perawatan.kd_kategori = kategori_perawatan.kd_kategori "
                        + "WHERE rawat_jl_dr.kd_dokter = ? AND rawat_jl_dr.no_rawat = ?";

                String sqlRalanDrPr = "SELECT rawat_jl_drpr.kd_jenis_prw, jns_perawatan.nm_perawatan, "
                        + "kategori_perawatan.nm_kategori, rawat_jl_drpr.tarif_tindakanpr, rawat_jl_drpr.tarif_tindakandr "
                        + "FROM rawat_jl_drpr "
                        + "INNER JOIN jns_perawatan ON rawat_jl_drpr.kd_jenis_prw = jns_perawatan.kd_jenis_prw "
                        + "INNER JOIN kategori_perawatan ON jns_perawatan.kd_kategori = kategori_perawatan.kd_kategori "
                        + "WHERE rawat_jl_drpr.kd_dokter = ? AND rawat_jl_drpr.no_rawat = ?";

                String sqlOperasi = "SELECT paket_operasi.nm_perawatan, operasi.operator1, operasi.operator2, operasi.operator3, "
                        + "operasi.biayaoperator1, d1.nm_dokter AS nm_op1 "
                        + "FROM operasi "
                        + "INNER JOIN paket_operasi ON operasi.kode_paket = paket_operasi.kode_paket "
                        + "LEFT JOIN dokter d1 ON operasi.operator1 = d1.kd_dokter "
                        + "WHERE operasi.no_rawat = ?";

                String sqlRadRalan = "SELECT periksa_radiologi.kd_dokter, dokter.nm_dokter, "
                        + "SUM(periksa_radiologi.tarif_tindakan_dokter) AS tarif_rad, "
                        + "GROUP_CONCAT(jns_perawatan_radiologi.nm_perawatan SEPARATOR ', ') AS nm_rad "
                        + "FROM periksa_radiologi "
                        + "INNER JOIN dokter ON periksa_radiologi.kd_dokter = dokter.kd_dokter "
                        + "INNER JOIN jns_perawatan_radiologi ON periksa_radiologi.kd_jenis_prw = jns_perawatan_radiologi.kd_jenis_prw "
                        + "WHERE periksa_radiologi.no_rawat = ? AND periksa_radiologi.status = 'Ralan' "
                        + "AND periksa_radiologi.tarif_tindakan_dokter > 0 "
                        + "GROUP BY periksa_radiologi.kd_dokter";

                String sqlCob = "SELECT nama_bayar, kd_pj FROM detail_piutang_pasien WHERE no_rawat = ?";

                try (PreparedStatement psDr = koneksi.prepareStatement(sqlRalanDr);
                     PreparedStatement psDrPr = koneksi.prepareStatement(sqlRalanDrPr);
                     PreparedStatement psOp = koneksi.prepareStatement(sqlOperasi);
                     PreparedStatement psRad = koneksi.prepareStatement(sqlRadRalan);
                     PreparedStatement psCob = koneksi.prepareStatement(sqlCob)) {

                    Map<String, List<RalanCandidate>> groupedBySep = new LinkedHashMap<>();
                    Set<String> processedRadRalan = new HashSet<>();
                    Set<String> processedOpRalan = new HashSet<>();

                    // Tahap 1: Ekstraksi tindakan dan data kandidat per kunjungan
                    for (RalanCandidate c : candidates) {
                        if (isCancelled()) {
                            return resultRows;
                        }

                        // 1. Filter Status Klaim
                        if (!isFilterStatusKlaimPass(c.statusKlaim)) {
                            continue;
                        }

                        // 2. Deteksi COB (Coordination of Benefit)
                        psCob.setString(1, c.noRawat);
                        StringBuilder hasilCOB = new StringBuilder();
                        int cobCount = 0;
                        try (ResultSet rsCob = psCob.executeQuery()) {
                            while (rsCob.next()) {
                                if (cobCount > 0) {
                                    hasilCOB.append(" | ");
                                }
                                hasilCOB.append(rsCob.getString("nama_bayar"));
                                cobCount++;
                            }
                        }
                        boolean isCob = (cobCount >= 2);
                        if (!isFilterCOBPass(isCob)) {
                            continue;
                        }
                        c.ketCob = isCob ? "COB (" + hasilCOB.toString() + ")" : "-";

                        // DPJP mapping
                        String mappedKdRs = mapDpjpVClaimToRs.getOrDefault(c.kdDpjp, "");
                        if (!mappedKdRs.isEmpty() && mappedKdRs.equals(c.kdDokter)) {
                            c.isDPJP = true;
                        }

                        // Dokter Umum
                        c.isDokterUmum = mapDokterUmum.getOrDefault(c.kdDokter, false);

                        // Poliklinik KFR (Poli U0036 dr. Anwar & dr. Dian)
                        if (c.kdPoli.equalsIgnoreCase("U0036") || c.nmPoli.toUpperCase(Locale.ROOT).contains("REHABILITASI") || c.nmPoli.toUpperCase(Locale.ROOT).contains("KEDOKTERAN FISIK")) {
                            c.isKFR = true;
                        }

                        // Deteksi Awal HD dari Poliklinik
                        if (c.nmPoli.toUpperCase(Locale.ROOT).contains("HEMODIALISA")
                                || c.kdPoli.equalsIgnoreCase("HD")
                                || c.kdPoli.equalsIgnoreCase("HDL")) {
                            c.isHD = true;
                        }

                        // Tindakan Rawat Jalan Dokter
                        psDr.setString(1, c.kdDokter);
                        psDr.setString(2, c.noRawat);
                        try (ResultSet rsDr = psDr.executeQuery()) {
                            while (rsDr.next()) {
                                double tarif = rsDr.getDouble("tarif_tindakandr");
                                c.totalTarifJm += tarif;
                                String nmPrw = rsDr.getString("nm_perawatan");
                                String katPrw = rsDr.getString("nm_kategori");
                                String nmUpper = nmPrw != null ? nmPrw.toUpperCase(Locale.ROOT) : "";
                                String katUpper = katPrw != null ? katPrw.toUpperCase(Locale.ROOT) : "";

                                if (isPemeriksaanText(nmPrw, katPrw)) {
                                    c.isPeriksa = true;
                                    c.hasExplicitPeriksa = true;
                                } else if (isHdSingleUseText(nmPrw)) {
                                    c.isHD = true;
                                    c.isTindakan = true;
                                } else {
                                    c.isTindakan = true;
                                }
                                if (isDoubleLumenText(nmPrw)) {
                                    c.isDoubleLumen = true;
                                    c.isTindakan = true;
                                }
                                if (isAvShuntText(nmPrw)) {
                                    c.isAVShunt = true;
                                    c.isTindakan = true;
                                }
                                if (isEswlText(nmPrw)) {
                                    c.isESWL = true;
                                    c.isTindakan = true;
                                }
                                if (nmUpper.contains("EEG")) {
                                    c.isEEG = true;
                                }
                                if (nmUpper.contains("AUDIOMETRI")) {
                                    c.isAudiometri = true;
                                }
                            }
                        }

                        // Tindakan Rawat Jalan Dokter & Paramedis
                        psDrPr.setString(1, c.kdDokter);
                        psDrPr.setString(2, c.noRawat);
                        try (ResultSet rsDrPr = psDrPr.executeQuery()) {
                            while (rsDrPr.next()) {
                                double tarifDr = rsDrPr.getDouble("tarif_tindakandr");
                                double tarifPr = rsDrPr.getDouble("tarif_tindakanpr");
                                c.totalTarifJm += (tarifDr + tarifPr);
                                String nmPrw = rsDrPr.getString("nm_perawatan");
                                String katPrw = rsDrPr.getString("nm_kategori");
                                String nmUpper = nmPrw != null ? nmPrw.toUpperCase(Locale.ROOT) : "";

                                if (isPemeriksaanText(nmPrw, katPrw)) {
                                    c.isPeriksa = true;
                                    c.hasExplicitPeriksa = true;
                                } else if (isHdSingleUseText(nmPrw)) {
                                    c.isHD = true;
                                    c.isTindakan = true;
                                } else {
                                    c.isTindakan = true;
                                }
                                if (nmUpper.contains("(KRM)") || nmUpper.contains("(OT)") || nmUpper.contains("(OKUPASI)") || nmUpper.contains("REHAB MEDIK")) {
                                    c.isTerapi = true;
                                }
                                if (isDoubleLumenText(nmPrw)) {
                                    c.isDoubleLumen = true;
                                    c.isTindakan = true;
                                }
                                if (isAvShuntText(nmPrw)) {
                                    c.isAVShunt = true;
                                    c.isTindakan = true;
                                }
                                if (isEswlText(nmPrw)) {
                                    c.isESWL = true;
                                    c.isTindakan = true;
                                }
                                if (nmUpper.contains("EEG")) {
                                    c.isEEG = true;
                                }
                                if (nmUpper.contains("AUDIOMETRI")) {
                                    c.isAudiometri = true;
                                }
                            }
                        }

                        // Tindakan Operasi Khusus
                        psOp.setString(1, c.noRawat);
                        try (ResultSet rsOp = psOp.executeQuery()) {
                            while (rsOp.next()) {
                                String nmOp = rsOp.getString("nm_perawatan");
                                String op1 = rsOp.getString("operator1");
                                String op2 = rsOp.getString("operator2");
                                String op3 = rsOp.getString("operator3");
                                double biayaOp1 = rsOp.getDouble("biayaoperator1");
                                String nmOp1 = rsOp.getString("nm_op1");
                                boolean isDokterInvolved = (op1 == null || op1.isEmpty() || c.kdDokter.equals(op1) || c.kdDokter.equals(op2) || c.kdDokter.equals(op3) || c.isDPJP);

                                if (isAvShuntText(nmOp)) {
                                    if (isDokterInvolved) {
                                        c.isAVShunt = true;
                                        c.isTindakan = true;
                                    }
                                }
                                if (isEswlText(nmOp)) {
                                    if (isDokterInvolved) {
                                        c.isESWL = true;
                                        c.isTindakan = true;
                                    }
                                }
                                // Ekstraksi Dokter Bedah Mulut / Bebas Aturan dari Operasi Ralan
                                if (op1 != null && !op1.trim().isEmpty() && processedOpRalan.add(c.noRawat + "_" + op1)) {
                                    if (isDokterBebasAturanBPJS(op1, nmOp1, "Bedah Mulut", mapSpesialis)) {
                                        RalanCandidate cOp = new RalanCandidate();
                                        cOp.noRawat = c.noRawat;
                                        cOp.tglPiutang = c.tglPiutang;
                                        cOp.jamReg = c.jamReg;
                                        cOp.noSep = c.noSep;
                                        cOp.noRkmMedis = c.noRkmMedis;
                                        cOp.nmPasien = c.nmPasien;
                                        cOp.kdDokter = op1;
                                        cOp.nmDokter = nmOp1 != null ? nmOp1 : mapPegawai.getOrDefault(op1, op1);
                                        cOp.totalPiutang = c.totalPiutang;
                                        cOp.umbal = c.umbal;
                                        cOp.statusKlaim = c.statusKlaim;
                                        cOp.kdPoli = "IBS";
                                        cOp.nmPoli = "Bedah Mulut";
                                        cOp.ketCob = c.ketCob;
                                        cOp.totalTarifJm = biayaOp1;
                                        cOp.calculatedJm = biayaOp1;
                                        cOp.ketTindakan = "Tarif Normal RS (Bebas Aturan BPJS) | " + (nmOp != null ? nmOp : "Operasi Bedah Mulut");
                                        cOp.isTindakan = true;

                                        String sepKeyOp = (!cOp.noSep.trim().isEmpty()) ? cOp.noSep.trim() : cOp.noRawat;
                                        groupedBySep.computeIfAbsent(sepKeyOp, k -> new ArrayList<>()).add(cOp);
                                    }
                                }

                                if (isDoubleLumenText(nmOp)) {
                                    if (isDokterInvolved) {
                                        c.isDoubleLumen = true;
                                        c.isTindakan = true;
                                    }
                                }
                            }
                        }

                        if (!c.isTindakan && !c.isHD && !c.isAVShunt && !c.isESWL && !c.isDoubleLumen && !c.isTerapi) {
                            c.isPeriksa = true;
                        }

                        String sepKey = (!c.noSep.trim().isEmpty()) ? c.noSep.trim() : c.noRawat;
                        groupedBySep.computeIfAbsent(sepKey, k -> new ArrayList<>()).add(c);

                        // Ekstraksi Dokter Radiologi Ralan
                        if (processedRadRalan.add(c.noRawat)) {
                            psRad.setString(1, c.noRawat);
                            try (ResultSet rsRad = psRad.executeQuery()) {
                                while (rsRad.next()) {
                                    String kdRad = rsRad.getString("kd_dokter");
                                    String nmRad = rsRad.getString("nm_dokter");
                                    double tarifRad = rsRad.getDouble("tarif_rad");
                                    String tindakanRad = rsRad.getString("nm_rad");

                                    RalanCandidate cRad = new RalanCandidate();
                                    cRad.noRawat = c.noRawat;
                                    cRad.tglPiutang = c.tglPiutang;
                                    cRad.jamReg = c.jamReg;
                                    cRad.noSep = c.noSep;
                                    cRad.noRkmMedis = c.noRkmMedis;
                                    cRad.nmPasien = c.nmPasien;
                                    cRad.kdDokter = kdRad;
                                    cRad.nmDokter = nmRad != null ? nmRad : mapPegawai.getOrDefault(kdRad, kdRad);
                                    cRad.totalPiutang = c.totalPiutang;
                                    cRad.umbal = c.umbal;
                                    cRad.statusKlaim = c.statusKlaim;
                                    cRad.kdPoli = "RAD";
                                    cRad.nmPoli = "Radiologi";
                                    cRad.ketCob = c.ketCob;
                                    cRad.totalTarifJm = tarifRad;
                                    cRad.calculatedJm = tarifRad;
                                    cRad.ketTindakan = "Tarif Normal RS (Bebas Aturan BPJS) | " + (tindakanRad != null ? tindakanRad : "Pemeriksaan Radiologi");
                                    cRad.isTindakan = true;
                                    cRad.isRadiologi = true;

                                    String sepKeyRad = (!cRad.noSep.trim().isEmpty()) ? cRad.noSep.trim() : cRad.noRawat;
                                    groupedBySep.computeIfAbsent(sepKeyRad, k -> new ArrayList<>()).add(cRad);
                                }
                            }
                        }
                    }

                    // Tahap 2: Evaluasi Aturan Baku Ralan (13 Aturan) per No. SEP
                    for (List<RalanCandidate> sepList : groupedBySep.values()) {
                        if (isCancelled()) {
                            return resultRows;
                        }
                        if (sepList.isEmpty()) {
                            continue;
                        }

                        Set<String> uniqueDoctors = new HashSet<>();
                        for (RalanCandidate cand : sepList) {
                            uniqueDoctors.add(cand.kdDokter);
                        }
                        int totalDoctorsInSep = uniqueDoctors.size();
                        boolean isRujukanInternal = (totalDoctorsInSep == 2);

                        boolean sepHasHD = sepList.stream().anyMatch(cand -> cand.isHD);
                        boolean sepHasAVShunt = sepList.stream().anyMatch(cand -> cand.isAVShunt);
                        boolean sepHasESWL = sepList.stream().anyMatch(cand -> cand.isESWL);
                        boolean sepHasDoubleLumen = sepList.stream().anyMatch(cand -> cand.isDoubleLumen || cand.isCDL);

                        boolean sepAlreadyGaveHdSingleUse = false;
                        boolean sepAlreadyGaveEswl = false;

                        // Cek apakah ada kunjungan dokter spesialis lain di SEP yang sama
                        boolean otherSpecialistInAvShunt = sepList.stream().anyMatch(cand -> !cand.isAVShunt && !cand.isDokterUmum);
                        boolean otherSpecialistInDoubleLumen = sepList.stream().anyMatch(cand -> !cand.isDoubleLumen && !cand.isCDL && !cand.isDokterUmum);

                        for (RalanCandidate c : sepList) {
                            if (totalDoctorsInSep > 1 && !c.isDPJP) {
                                c.isRaber = true;
                            }

                            // 0. Pengecualian Khusus Dokter Radiologi (Sp.Rad) & Dokter Gigi Bedah Mulut (Sp.BM)
                            if (c.isRadiologi || isDokterBebasAturanBPJS(c.kdDokter, c.nmDokter, c.nmPoli, mapSpesialis)) {
                                c.calculatedJm = c.totalTarifJm;
                                if (c.ketTindakan == null || !c.ketTindakan.startsWith("Tarif Normal RS")) {
                                    c.ketTindakan = "Tarif Normal RS (Bebas Aturan BPJS)";
                                }
                            }
                            // 1. Kasus Hemodialisa (Single Use)
                            else if (sepHasHD) {
                                if (c.isHD && !sepAlreadyGaveHdSingleUse) {
                                    c.calculatedJm = KalkulatorJasaMedisDokter.getParameter1("RALAN_HD_SINGLE_USE", 75000.0);
                                    c.ketTindakan = "Hemodialisa Single Use";
                                    sepAlreadyGaveHdSingleUse = true;
                                } else if (c.isDokterUmum) {
                                    c.calculatedJm = 0.0;
                                    c.ketTindakan = "Dokter Umum IGD (Non-JM)";
                                } else {
                                    // Pemeriksaan dokter spesialis diabaikan (Rp 0)
                                    c.calculatedJm = 0.0;
                                    c.ketTindakan = "Periksa Sp (Diabaikan - SEP Single Use HD)";
                                }
                            }
                            // 2. Kasus AV Shunt
                            else if (sepHasAVShunt) {
                                double tarifAv = KalkulatorJasaMedisDokter.getParameter1("RALAN_AV_SHUNT", 575000.0);
                                double tarifPeriksa = KalkulatorJasaMedisDokter.getParameter2("RALAN_AV_SHUNT", 72000.0);

                                if (c.isAVShunt) {
                                    if (otherSpecialistInAvShunt) {
                                        // Ada kunjungan dr. Spesialis lain di SEP yang sama -> tindakan AV Shunt dapat P1
                                        c.calculatedJm = tarifAv;
                                        c.ketTindakan = "AV Shunt";
                                    } else if (c.isDPJP || c.hasExplicitPeriksa || c.isPeriksa) {
                                        // 1 kunjungan tunggal, DPJP / terdapat pemeriksaan -> dapat AV Shunt + Pemeriksaan
                                        c.calculatedJm = tarifAv + tarifPeriksa;
                                        c.ketTindakan = "AV Shunt + Periksa Sp";
                                    } else {
                                        c.calculatedJm = tarifAv;
                                        c.ketTindakan = "AV Shunt";
                                    }
                                } else if (c.isDokterUmum) {
                                    c.calculatedJm = 0.0;
                                    c.ketTindakan = "Dokter Umum IGD (Non-JM)";
                                } else {
                                    // Kunjungan pemeriksaan dokter spesialis di SEP yang sama diberi Rp 72.000 (P2)
                                    c.calculatedJm = tarifPeriksa;
                                    c.ketTindakan = "Pemeriksaan dr. Spesialis (SEP AV Shunt)";
                                }
                            }
                            // 3. Kasus ESWL
                            else if (sepHasESWL) {
                                double tarifEswl = KalkulatorJasaMedisDokter.getParameter1("RALAN_ESWL", 822760.0);
                                if (c.isESWL && !sepAlreadyGaveEswl) {
                                    c.calculatedJm = tarifEswl;
                                    c.ketTindakan = "ESWL";
                                    sepAlreadyGaveEswl = true;
                                } else if (c.isDokterUmum) {
                                    c.calculatedJm = 0.0;
                                    c.ketTindakan = "Dokter Umum IGD (Non-JM)";
                                } else {
                                    // Kunjungan pemeriksaan di SEP yang sama dinilai 0 rupiah
                                    c.calculatedJm = 0.0;
                                    c.ketTindakan = "Periksa Sp (Diabaikan - SEP ESWL)";
                                }
                            }
                            // 4. Kasus Double Lumen / CDL
                            else if (sepHasDoubleLumen) {
                                double tarifDl = KalkulatorJasaMedisDokter.getParameter1("RALAN_DOUBLE_LUMEN", 500000.0);
                                double tarifPeriksa = KalkulatorJasaMedisDokter.getParameter2("RALAN_DOUBLE_LUMEN", 72000.0);

                                if (c.isDoubleLumen || c.isCDL) {
                                    if (otherSpecialistInDoubleLumen) {
                                        c.calculatedJm = tarifDl;
                                        c.ketTindakan = "Double Lumen";
                                    } else if (c.isDPJP || c.hasExplicitPeriksa || c.isPeriksa) {
                                        c.calculatedJm = tarifDl + tarifPeriksa;
                                        c.ketTindakan = "Double Lumen + Periksa Sp";
                                    } else {
                                        c.calculatedJm = tarifDl;
                                        c.ketTindakan = "Double Lumen";
                                    }
                                } else if (c.isDokterUmum) {
                                    c.calculatedJm = 0.0;
                                    c.ketTindakan = "Dokter Umum IGD (Non-JM)";
                                } else {
                                    // Kunjungan pemeriksaan dokter spesialis di SEP yang sama diberi Rp 72.000 (P2)
                                    c.calculatedJm = tarifPeriksa;
                                    c.ketTindakan = "Pemeriksaan dr. Spesialis (SEP Double Lumen)";
                                }
                            }
                            // 5. Dokter Umum di IGD
                            else if (c.isDokterUmum) {
                                c.calculatedJm = KalkulatorJasaMedisDokter.getParameter1("RALAN_DOKTER_UMUM_IGD", 0.0);
                                c.ketTindakan = "Dokter Umum IGD (Non-JM)";
                            }
                            // 6. Dokter Spesialis KFR (Poli U0036 dr. Anwar & dr. Dian)
                            else if (c.isKFR) {
                                if (c.isRaber) {
                                    c.calculatedJm = KalkulatorJasaMedisDokter.getParameter1("RALAN_KFR_RABER", 36000.0);
                                    c.ketTindakan = "KFR (Raber)";
                                } else if (c.isTindakan && !c.hasExplicitPeriksa) {
                                    c.calculatedJm = KalkulatorJasaMedisDokter.getParameter1("RALAN_KFR_TINDAKAN", 35000.0);
                                    c.ketTindakan = "KFR (Tindakan Saja)";
                                } else {
                                    c.calculatedJm = KalkulatorJasaMedisDokter.getParameter1("RALAN_KFR_PERIKSA_SP", 36000.0);
                                    c.ketTindakan = "KFR (Pemeriksaan Sp)";
                                }
                            }
                            // 7. Rujukan Internal (2 Dokter Spesialis dalam 1 SEP)
                            else if (isRujukanInternal) {
                                boolean partnerHasTindakan = false;
                                for (RalanCandidate other : sepList) {
                                    if (!other.kdDokter.equals(c.kdDokter) && other.isTindakan) {
                                        partnerHasTindakan = true;
                                        break;
                                    }
                                }
                                c.calculatedJm = KalkulatorJasaMedisDokter.hitungJasaMedisDokterRawatJalanLengkap(
                                        c.umbal, c.isTindakan, c.isPeriksa, c.isDPJP, c.isRaber, false, false, false, false, false, c.isDokterUmum, true, partnerHasTindakan
                                );
                                if (!c.isTindakan && !partnerHasTindakan) {
                                    c.ketTindakan = "Rujukan Internal (Pemeriksaan Bersama)";
                                } else if (c.isTindakan && !partnerHasTindakan) {
                                    c.ketTindakan = "Rujukan Internal (Dokter Bertindakan)";
                                } else if (!c.isTindakan && partnerHasTindakan) {
                                    c.ketTindakan = "Rujukan Internal (Dokter Non-Tindakan)";
                                } else {
                                    c.ketTindakan = "Rujukan Internal (Keduanya Bertindakan)";
                                }
                            }
                            // 8. Dokter Spesialis Standar (1 DPJP Tunggal dengan/tanpa tindakan)
                            else {
                                c.calculatedJm = KalkulatorJasaMedisDokter.hitungJasaMedisDokterRawatJalanLengkap(
                                        c.umbal, c.isTindakan, c.isPeriksa, c.isDPJP, c.isRaber, false, false, false, false, false, c.isDokterUmum, false, false
                                );
                                c.ketTindakan = c.isTindakan ? "Spesialis + Tindakan" : "Spesialis Non-Tindakan";
                            }

                            // Format kolom 13 (Keterangan) dengan tindakan medis dan COB (jika ada)
                            String colKeterangan = c.ketTindakan;
                            if (c.ketCob != null && !c.ketCob.trim().isEmpty() && !c.ketCob.equals("-")) {
                                colKeterangan = (colKeterangan.isEmpty() ? "" : colKeterangan + " | ") + c.ketCob;
                            }
                            if (colKeterangan.isEmpty()) {
                                colKeterangan = "-";
                            }

                            // 20 Kolom lengkap tabModeRawatJalan
                            Object[] rowData = new Object[]{
                                false, // 0: P (checkbox)
                                c.tglPiutang, // 1: Tanggal Piutang
                                c.jamReg, // 2: Jam
                                c.noSep, // 3: No SEP
                                c.noRawat, // 4: No Rawat
                                c.noRkmMedis, // 5: No RM
                                c.nmPasien, // 6: Nama Pasien
                                c.kdDokter, // 7: Kode Dokter
                                c.nmDokter, // 8: Nama Dokter
                                c.totalPiutang, // 9: Total Piutang
                                c.totalTarifJm, // 10: Total Tarif JM
                                c.umbal, // 11: Umbal
                                c.statusKlaim, // 12: Status Klaim
                                colKeterangan, // 13: Keterangan (Tindakan Medis & COB)
                                c.isDPJP ? "Ya" : "Tidak", // 14: DPJP
                                c.isRaber ? "Ya" : "Tidak", // 15: Raber
                                c.isTerapi ? "Ya" : "Tidak", // 16: Terapi
                                c.isTindakan ? "Ya" : "Tidak", // 17: Tindakan
                                c.isHD ? "Ya" : "Tidak", // 18: HD
                                c.calculatedJm // 19: JM BPJS
                            };

                            // Filter target dokter jika kddokter diisi
                            if (!targetKdDokter.isEmpty() && !c.kdDokter.equalsIgnoreCase(targetKdDokter)) {
                                continue;
                            }
                            // Filter TCari terhadap data baris
                            if (!targetTCari.isEmpty()) {
                                boolean matchKeyword = c.kdDokter.toLowerCase(Locale.ROOT).contains(targetTCari)
                                        || c.nmDokter.toLowerCase(Locale.ROOT).contains(targetTCari)
                                        || c.noRawat.toLowerCase(Locale.ROOT).contains(targetTCari)
                                        || c.noRkmMedis.toLowerCase(Locale.ROOT).contains(targetTCari)
                                        || c.nmPasien.toLowerCase(Locale.ROOT).contains(targetTCari)
                                        || c.noSep.toLowerCase(Locale.ROOT).contains(targetTCari);
                                if (!matchKeyword) {
                                    continue;
                                }
                            }

                            resultRows.add(rowData);
                        }
                    }
                }

                return resultRows;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();
                    if (rows != null) {
                        for (Object[] rowData : rows) {
                            tabModeRawatJalan.addRow(rowData);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi SwingWorker Ralan : " + e);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                    BtnCari.setEnabled(true);
                }
            }
        };

        workerRawatJalan.execute();
    }

    private void tampilRawatJalan() {
        try {
            jml = 0;
            for (i = 0; i < tbJMRawatJalan.getRowCount(); i++) {
                if (tbJMRawatJalan.getValueAt(i, 0).toString().equals("true")) {
                    jml++;
                }
            }

            pilih = new boolean[jml];
            norawat = new String[jml];
            tglpiutang = new String[jml];
            norm = new String[jml];
            pasien = new String[jml];
            kodedokter = new String[jml];
            namadokter = new String[jml];
            piutang = new double[jml];
            tarifjm = new double[jml];
            umbal = new double[jml];
            statusklaim = new String[jml];
            keterangancob = new String[jml];
            dpjp = new String[jml];
            raber = new String[jml];
            terapi = new String[jml];
            tindakan = new String[jml];
            hd = new String[jml];
            jmbpjs = new double[jml];
            noseptemp = new String[jml];
            jamtemp = new String[jml];

            index = 0;
            for (i = 0; i < tbJMRawatJalan.getRowCount(); i++) {
                if (tbJMRawatJalan.getValueAt(i, 0).toString().equals("true")) {
                    pilih[index] = true;
                    tglpiutang[index] = tbJMRawatJalan.getValueAt(i, 1).toString();
                    jamtemp[index] = tbJMRawatJalan.getValueAt(i, 2).toString();
                    noseptemp[index] = tbJMRawatJalan.getValueAt(i, 3).toString();
                    norawat[index] = tbJMRawatJalan.getValueAt(i, 4).toString();
                    norm[index] = tbJMRawatJalan.getValueAt(i, 5).toString();
                    pasien[index] = tbJMRawatJalan.getValueAt(i, 6).toString();
                    kodedokter[index] = tbJMRawatJalan.getValueAt(i, 7).toString();
                    namadokter[index] = tbJMRawatJalan.getValueAt(i, 8).toString();
                    piutang[index] = Double.parseDouble(tbJMRawatJalan.getValueAt(i, 9).toString());
                    tarifjm[index] = Double.parseDouble(tbJMRawatJalan.getValueAt(i, 10).toString());
                    umbal[index] = Double.parseDouble(tbJMRawatJalan.getValueAt(i, 11).toString());
                    statusklaim[index] = tbJMRawatJalan.getValueAt(i, 12).toString();
                    keterangancob[index] = tbJMRawatJalan.getValueAt(i, 13).toString();
                    dpjp[index] = tbJMRawatJalan.getValueAt(i, 14).toString();
                    raber[index] = tbJMRawatJalan.getValueAt(i, 15).toString();
                    terapi[index] = tbJMRawatJalan.getValueAt(i, 16).toString();
                    tindakan[index] = tbJMRawatJalan.getValueAt(i, 17).toString();
                    hd[index] = tbJMRawatJalan.getValueAt(i, 18).toString();
                    jmbpjs[index] = Double.parseDouble(tbJMRawatJalan.getValueAt(i, 19).toString());
                    index++;
                }
            }

            Valid.tabelKosong(tabModeRawatJalan);

            for (i = 0; i < jml; i++) {
                tabModeRawatJalan.addRow(new Object[]{
                    pilih[i],
                    tglpiutang[i],
                    jamtemp[i],
                    noseptemp[i],
                    norawat[i],
                    norm[i],
                    pasien[i],
                    kodedokter[i],
                    namadokter[i],
                    piutang[i],
                    tarifjm[i],
                    umbal[i],
                    statusklaim[i],
                    keterangancob[i],
                    statusklaim[i],
                    keterangancob[i],
                    dpjp[i],
                    raber[i],
                    terapi[i],
                    tindakan[i],
                    hd[i],
                    jmbpjs[i]
                });
            }

            jalankanWorkerRawatJalan();
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void old_tampilRawatJalan() {
        try {
            jml = 0;
            for (i = 0; i < tbJMRawatJalan.getRowCount(); i++) {
                if (tbJMRawatJalan.getValueAt(i, 0).toString().equals("true")) {
                    jml++;
                }
            }

            pilih = null;
            pilih = new boolean[jml];
            norawat = null;
            norawat = new String[jml];
            tglpiutang = null;
            tglpiutang = new String[jml];
            norm = null;
            norm = new String[jml];
            pasien = null;
            pasien = new String[jml];
            kodedokter = null;
            kodedokter = new String[jml];
            namadokter = null;
            namadokter = new String[jml];
            piutang = null;
            piutang = new double[jml];
            tarifjm = null;
            tarifjm = new double[jml];
            umbal = null;
            umbal = new double[jml];
            statusklaim = null;
            statusklaim = new String[jml];
            keterangancob = new String[jml];
            dpjp = null;
            dpjp = new String[jml];
            raber = null;
            raber = new String[jml];
            terapi = null;
            terapi = new String[jml];
            tindakan = null;
            tindakan = new String[jml];
            hd = null;
            hd = new String[jml];
            jmbpjs = null;
            jmbpjs = new double[jml];
            noseptemp = null;
            noseptemp = new String[jml];
            jamtemp = null;
            jamtemp = new String[jml];

            index = 0;
            for (i = 0; i < tbJMRawatJalan.getRowCount(); i++) {
                if (tbJMRawatJalan.getValueAt(i, 0).toString().equals("true")) {
                    pilih[index] = true;
                    tglpiutang[index] = tbJMRawatJalan.getValueAt(i, 1).toString();
                    jamtemp[index] = tbJMRawatJalan.getValueAt(i, 2).toString();
                    noseptemp[index] = tbJMRawatJalan.getValueAt(i, 3).toString();
                    norawat[index] = tbJMRawatJalan.getValueAt(i, 4).toString();
                    norm[index] = tbJMRawatJalan.getValueAt(i, 5).toString();
                    pasien[index] = tbJMRawatJalan.getValueAt(i, 6).toString();
                    kodedokter[index] = tbJMRawatJalan.getValueAt(i, 7).toString();
                    namadokter[index] = tbJMRawatJalan.getValueAt(i, 8).toString();
                    piutang[index] = Double.parseDouble(tbJMRawatJalan.getValueAt(i, 9).toString());
                    tarifjm[index] = Double.parseDouble(tbJMRawatJalan.getValueAt(i, 10).toString());
                    umbal[index] = Double.parseDouble(tbJMRawatJalan.getValueAt(i, 11).toString());
                    statusklaim[index] = tbJMRawatJalan.getValueAt(i, 12).toString();
                    keterangancob[index] = tbJMRawatJalan.getValueAt(i, 13).toString();
                    dpjp[index] = tbJMRawatJalan.getValueAt(i, 14).toString();
                    raber[index] = tbJMRawatJalan.getValueAt(i, 15).toString();
                    terapi[index] = tbJMRawatJalan.getValueAt(i, 16).toString();
                    tindakan[index] = tbJMRawatJalan.getValueAt(i, 17).toString();
                    hd[index] = tbJMRawatJalan.getValueAt(i, 18).toString();
                    jmbpjs[index] = Double.parseDouble(tbJMRawatJalan.getValueAt(i, 19).toString());
                    index++;
                }
            }

            Valid.tabelKosong(tabModeRawatJalan);

            for (i = 0; i < jml; i++) {
                tabModeRawatJalan.addRow(new Object[]{
                    pilih[i],
                    tglpiutang[i],
                    jamtemp[i],
                    noseptemp[i],
                    norawat[i],
                    norm[i],
                    pasien[i],
                    kodedokter[i],
                    namadokter[i],
                    piutang[i],
                    tarifjm[i],
                    umbal[i],
                    statusklaim[i],
                    keterangancob[i],
                    dpjp[i],
                    raber[i],
                    terapi[i],
                    tindakan[i],
                    hd[i],
                    jmbpjs[i]
                });
            }
            if (TabRawat.getSelectedIndex() == 1) {
                jalankanWorkerRawatInap();
                return;
            }
            String belumgruping = "";
            ps = koneksi.prepareStatement(
                    "SELECT "
                    + "piutang_pasien.no_rawat, "
                    + "piutang_pasien.tgl_piutang, "
                    + "reg_periksa.jam_reg, "
                    + "bridging_sep.no_sep, "
                    + "piutang_pasien.no_rkm_medis, "
                    + "pasien.nm_pasien, "
                    + "reg_periksa.kd_dokter, "
                    + "dokter.nm_dokter, "
                    + "detail_piutang_pasien.totalpiutang,"
                    + "bridging_sep.kddpjp, "
                    + "0 as tarif_jm, "
                    + // Placeholder for Total Tarif JM
                    "IFNULL( inacbg_klaim_umpanbalik.bydisetujui, '0' ) AS umbal, "
                    + // Placeholder for Umbal
                    "'Tidak' as dpjp, "
                    + // Default value for DPJP
                    "'Tidak' as raber, "
                    + // Default value for Raber
                    "'Tidak' as terapi, "
                    + // Default value for Terapi
                    "'Tidak' as tindakan, "
                    + // Default value for Tindakan
                    "'Tidak' as hd, "
                    + // Default value for HD
                    "0 as jm_bpjs,"
                    + "CASE\n"
                    + "		\n"
                    + "		WHEN reg_periksa.status_lanjut = 'Ranap' THEN\n"
                    + "		nota_inap.no_nota \n"
                    + "		WHEN reg_periksa.status_lanjut = 'Ralan' THEN\n"
                    + "		nota_jalan.no_nota ELSE NULL \n"
                    + "	END AS no_nota,\n"
                    + "	pasien.nip,\n"
                    + "	IFNULL( inacbg_data_terkirim_final_dc.no_sep, 'Belum di Grouping' ) AS no_sep,\n"
                    + "	IFNULL( pegawai.nama, '' ) AS nama,\n"
                    + "	IFNULL( inacbg_data_terkirim2.waktudikirim, '' ) AS waktudikirim,\n"
                    + "	IFNULL( inacbg_grouping_stage12.tarif, '0' ) AS tarifperkiraan "
                    + // Placeholder for JM BPJS
                    "FROM piutang_pasien "
                    + " INNER JOIN reg_periksa ON piutang_pasien.no_rawat = reg_periksa.no_rawat "
                    + " INNER JOIN pasien ON piutang_pasien.no_rkm_medis = pasien.no_rkm_medis "
                    + " INNER JOIN dokter ON reg_periksa.kd_dokter = dokter.kd_dokter "
                    + " INNER JOIN detail_piutang_pasien ON piutang_pasien.no_rawat = detail_piutang_pasien.no_rawat"
                    + " INNER JOIN penjab ON detail_piutang_pasien.kd_pj = penjab.kd_pj"
                    + "	LEFT JOIN nota_jalan ON nota_jalan.no_rawat = reg_periksa.no_rawat\n"
                    + "	LEFT JOIN nota_inap ON nota_inap.no_rawat = reg_periksa.no_rawat "
                    + "	INNER JOIN bridging_sep ON detail_piutang_pasien.no_rawat = bridging_sep.no_rawat \n"
                    + "	AND (\n"
                    + "		( reg_periksa.status_lanjut = 'Ralan' AND bridging_sep.jnspelayanan = '2' ) \n"
                    + "		OR ( reg_periksa.status_lanjut = 'Ranap' AND bridging_sep.jnspelayanan = '1' ) \n"
                    + "	)\n"
                    + "	LEFT JOIN inacbg_data_terkirim2 ON bridging_sep.no_sep = inacbg_data_terkirim2.no_sep\n"
                    + "	LEFT JOIN inacbg_grouping_stage12 ON bridging_sep.no_sep = inacbg_grouping_stage12.no_sep\n"
                    + "	LEFT JOIN pegawai ON inacbg_data_terkirim2.nik = pegawai.no_ktp\n"
                    + "	LEFT JOIN inacbg_data_terkirim_final_dc ON bridging_sep.no_sep = inacbg_data_terkirim_final_dc.no_sep"
                    + "	LEFT JOIN inacbg_klaim_umpanbalik ON bridging_sep.no_sep = inacbg_klaim_umpanbalik.no_sep "
                    + "WHERE " + belumgruping + " piutang_pasien.tgl_piutang between ? and ? "
                    + "AND detail_piutang_pasien.sisapiutang <= 0 "
                    + "AND reg_periksa.status_lanjut = 'Ralan' "
                    + "AND (piutang_pasien.no_rawat LIKE ? OR piutang_pasien.no_rkm_medis LIKE ? OR pasien.nm_pasien LIKE ? OR bridging_sep.no_sep LIKE ?) "
                    + "ORDER BY bridging_sep.no_sep ASC, piutang_pasien.tgl_piutang ASC"
            );

            try {
                ps.setString(1, Valid.SetTgl(Periode1.getSelectedItem() + ""));
                ps.setString(2, Valid.SetTgl(Period2.getSelectedItem() + ""));
//                ps.setString(3, NmCaraBayar.getText());
                ps.setString(3, "%" + TCari.getText() + "%");
                ps.setString(4, "%" + TCari.getText() + "%");
                ps.setString(5, "%" + TCari.getText() + "%");
                ps.setString(6, "%" + TCari.getText() + "%");
                rs = ps.executeQuery();
                totaljmtarif = 0;
                // Add this variable before your loop to track the previous nosep
                String previousNoSep = null;
                while (rs.next()) {
                    isDPJP = false;  // Set your logic to determine if this is DPJP
                    isRaber = false; // Set your logic to determine if this is Raber
                    isTindakan = false; // Set your logic to determine if this is Tindakan
                    isTerapi = false; // Set your logic to determine if this is Terapi
                    isHD = false;    // Set your logic to determine if this is HD
                    isDoubleLumen = false;
                    isDokterUmum = false;
                    isEEG = false;
                    if (Sequel.cariIsi("select kd_dokter from maping_dokter_dpjpvclaim where kd_dokter_bpjs='" + rs.getString("kddpjp") + "'").equals(rs.getString("kd_dokter"))) {
                        isDPJP = true;
                    }

                    if (Sequel.cariInteger("select count(dokter.kd_dokter) from dokter inner join spesialis on dokter.kd_sps=spesialis.kd_sps where dokter.kd_dokter='" + rs.getString("kd_dokter") + "' and spesialis.nm_sps like '%umum%'") > 0) {
                        isDokterUmum = true;
                    }

                    String currentNoSep = rs.getString("no_sep");

                    // Check if current nosep equals previous nosep
                    if (previousNoSep != null && currentNoSep.equals(previousNoSep)) {
                        isRaber = true;
                    }

                    totaljmtarif = 0;

                    prosesCariSemua(rs.getString("no_rawat"), rs.getString("kd_dokter"), "rajal");

                    // Dihitung dinamis menggunakan Kalkulator JM BPJS & aturan tabel set_aturan_jm_bpjs
                    double jmdihitung = kalkulator.hitungJasaMedisDokterRawatJalan(
                            rs.getDouble("umbal"), isTindakan, isTerapi, isDPJP, isRaber, isHD, isAudiometri,
                            isCDL, isDoubleLumen, isAVShunt, isESWL, isEEG, isDokterUmum
                    );

                    /* Blok ketentuan hardcode lama dinonaktifkan, digantikan kalkulator dinamis:
                        jmdihitung = 500000;
                    }

                    if (isESWL) {
                        jmdihitung = (20 / 100) * 822760;
                    }

                    if (isDoubleLumen) {
                        jmdihitung = 570000;
                    }
                    if (isEEG) {
                        jmdihitung = 90000;
                    }

                    if (isDokterUmum) {
                        jmdihitung = 0;
                    }

                    if (isAVShunt) {
                        jmdihitung = 575000;
                    } else if (isAVShunt && isDPJP) {
                        jmdihitung = 575000 + 72000;
                    }

                    if (jmdihitung == 0 && !isDokterUmum) {
                        jmdihitung = kalkulator.hitungJasaMedisDokterRawatJalan(rs.getDouble("umbal"), isTindakan, isTerapi, isDPJP, isRaber, isHD, isAudiometri); // JM BPJS


                    }
                     */
                    tabModeRawatJalan.addRow(new Object[]{
                        false, // P (checkbox)
                        rs.getString("tgl_piutang"), // Tanggal Piutang
                        rs.getString("jam_reg"), // Jam
                        rs.getString("no_sep"), // No SEP
                        rs.getString("no_rawat"), // No Rawat
                        rs.getString("no_rkm_medis"), // No RM
                        rs.getString("nm_pasien"), // Nama Pasien
                        rs.getString("kd_dokter"), // Kode Dokter
                        rs.getString("nm_dokter"), // Nama Dokter
                        rs.getDouble("totalpiutang"), // Total Piutang
                        totaljmtarif, // Total Tarif JM
                        rs.getDouble("umbal"), // Umbal
                        isDPJP ? "Ya" : "Tidak", // DPJP (converted from boolean)
                        isRaber ? "Ya" : "Tidak", // Raber (converted from boolean)
                        isTerapi ? "Ya" : "Tidak", // Terapi (converted from boolean)
                        isTindakan ? "Ya" : "Tidak", // Tindakan (converted from boolean)
                        isHD ? "Ya" : "Tidak", // HD (converted from boolean)
                        jmdihitung
                    });
                    // Update previousNoSep for next iteration
                    previousNoSep = currentNoSep;
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }

        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void tampilRawatInap() {
        try {
            jml = 0;
            for (i = 0; i < tbJMRawatInap.getRowCount(); i++) {
                if (tbJMRawatInap.getValueAt(i, 0).toString().equals("true")) {
                    jml++;
                }
            }

            pilih = null;
            pilih = new boolean[jml];
            norawat = null;
            norawat = new String[jml];
            tglpiutang = null;
            tglpiutang = new String[jml];
            norm = null;
            norm = new String[jml];
            pasien = null;
            pasien = new String[jml];
            kodedokter = null;
            kodedokter = new String[jml];
            namadokter = null;
            namadokter = new String[jml];
            piutang = null;
            piutang = new double[jml];
            tarifjm = null;
            tarifjm = new double[jml];
            umbal = null;
            umbal = new double[jml];
            statusklaim = null;
            statusklaim = new String[jml];
            keterangancob = new String[jml];
            dpjp = null;
            dpjp = new String[jml];
            tindakan = null;
            tindakan = new String[jml];
            statusbedah = null;
            statusbedah = new String[jml];
            jenis = null;
            jenis = new String[jml];
            visit = null;
            visit = new String[jml];
            jmbpjs = null;
            jmbpjs = new double[jml];
            noseptemp = null;
            noseptemp = new String[jml];
            jamtemp = null;
            jamtemp = new String[jml];

            index = 0;
            for (i = 0; i < tbJMRawatInap.getRowCount(); i++) {
                if (tbJMRawatInap.getValueAt(i, 0).toString().equals("true")) {
                    pilih[index] = true;
                    tglpiutang[index] = tbJMRawatInap.getValueAt(i, 1).toString();
                    jamtemp[index] = tbJMRawatInap.getValueAt(i, 2).toString();
                    noseptemp[index] = tbJMRawatInap.getValueAt(i, 3).toString();
                    norawat[index] = tbJMRawatInap.getValueAt(i, 4).toString();
                    norm[index] = tbJMRawatInap.getValueAt(i, 5).toString();
                    pasien[index] = tbJMRawatInap.getValueAt(i, 6).toString();
                    kodedokter[index] = tbJMRawatInap.getValueAt(i, 7).toString();
                    namadokter[index] = tbJMRawatInap.getValueAt(i, 8).toString();
                    piutang[index] = Double.parseDouble(tbJMRawatInap.getValueAt(i, 9).toString());
                    tarifjm[index] = Double.parseDouble(tbJMRawatInap.getValueAt(i, 10).toString());
                    umbal[index] = Double.parseDouble(tbJMRawatInap.getValueAt(i, 11).toString());
                    statusklaim[index] = tbJMRawatInap.getValueAt(i, 12).toString();
                    keterangancob[index] = tbJMRawatInap.getValueAt(i, 13).toString();
                    dpjp[index] = tbJMRawatInap.getValueAt(i, 14).toString();
                    tindakan[index] = tbJMRawatInap.getValueAt(i, 15).toString();
                    statusbedah[index] = tbJMRawatInap.getValueAt(i, 16).toString();
                    jenis[index] = tbJMRawatInap.getValueAt(i, 17).toString();
                    visit[index] = tbJMRawatInap.getValueAt(i, 18).toString();
                    jmbpjs[index] = Double.parseDouble(tbJMRawatInap.getValueAt(i, 19).toString());
                    index++;
                }
            }

            Valid.tabelKosong(tabModeRawatInap);
            isLoadingData = true;

            Map<String, List<Map<String, Object>>> sortedData = new TreeMap<>();

            for (i = 0; i < jml; i++) {
                tabModeRawatInap.addRow(new Object[]{
                    pilih[i],
                    tglpiutang[i],
                    jamtemp[i],
                    noseptemp[i],
                    norawat[i],
                    norm[i],
                    pasien[i],
                    kodedokter[i],
                    namadokter[i],
                    piutang[i],
                    tarifjm[i],
                    umbal[i],
                    statusklaim[i],
                    keterangancob[i],
                    dpjp[i],
                    tindakan[i],
                    statusbedah[i],
                    jenis[i],
                    visit[i],
                    jmbpjs[i]
                });
            }
            if (TabRawat.getSelectedIndex() == 1) {
                jalankanWorkerRawatInap();
                return;
            }
            String belumgruping = "";
            ps = koneksi.prepareStatement(
                    "SELECT "
                    + "piutang_pasien.no_rawat, "
                    + "piutang_pasien.tgl_piutang, "
                    + "reg_periksa.jam_reg, "
                    + "bridging_sep.no_sep, "
                    + "piutang_pasien.no_rkm_medis, "
                    + "piutang_pasien.uangmuka, "
                    + "pasien.nm_pasien, "
                    + "reg_periksa.kd_dokter, "
                    + "dokter.nm_dokter, "
                    + "detail_piutang_pasien.totalpiutang, "
                    + "0 as tarif_jm, "
                    + "bridging_sep.kddpjp, "
                    + // Placeholder for Total Tarif JM
                    "IFNULL( inacbg_klaim_umpanbalik.bydisetujui, '0' ) AS umbal, "
                    + // Placeholder for Umbal
                    "'DPJP' as dpjp, "
                    + // Default value for DPJP
                    "'OP' as tindakan, "
                    + // Default value for TINDAKAN
                    "'Sendiri' as status, "
                    + // Default value for STATUS
                    "'Bedah Dengan Tindakan' as jenis, "
                    + // Default value for JENIS
                    "'full' as visit, "
                    + // Default value for Visit
                    "0 as jm_bpjs, "
                    + "CASE\n"
                    + "		\n"
                    + "		WHEN reg_periksa.status_lanjut = 'Ranap' THEN\n"
                    + "		nota_inap.no_nota \n"
                    + "		WHEN reg_periksa.status_lanjut = 'Ralan' THEN\n"
                    + "		nota_jalan.no_nota ELSE NULL \n"
                    + "	END AS no_nota,\n"
                    + "	pasien.nip,\n"
                    + "	IFNULL( inacbg_data_terkirim_final_dc.no_sep, 'Belum di Grouping' ) AS no_sep,\n"
                    + "	IFNULL( pegawai.nama, '' ) AS nama,\n"
                    + "	IFNULL( inacbg_data_terkirim2.waktudikirim, '' ) AS waktudikirim,\n"
                    + "	IFNULL( inacbg_grouping_stage12.tarif, '0' ) AS tarifperkiraan "
                    + // Placeholder for JM BPJS
                    "FROM piutang_pasien "
                    + " INNER JOIN reg_periksa ON piutang_pasien.no_rawat = reg_periksa.no_rawat "
                    + " INNER JOIN pasien ON piutang_pasien.no_rkm_medis = pasien.no_rkm_medis "
                    + " INNER JOIN dokter ON reg_periksa.kd_dokter = dokter.kd_dokter "
                    + " INNER JOIN detail_piutang_pasien ON piutang_pasien.no_rawat = detail_piutang_pasien.no_rawat"
                    + " INNER JOIN penjab ON detail_piutang_pasien.kd_pj = penjab.kd_pj"
                    + "	LEFT JOIN nota_jalan ON nota_jalan.no_rawat = reg_periksa.no_rawat\n"
                    + "	LEFT JOIN nota_inap ON nota_inap.no_rawat = reg_periksa.no_rawat "
                    + "	INNER JOIN bridging_sep ON detail_piutang_pasien.no_rawat = bridging_sep.no_rawat \n"
                    + "	AND (\n"
                    + "		( reg_periksa.status_lanjut = 'Ralan' AND bridging_sep.jnspelayanan = '2' ) \n"
                    + "		OR ( reg_periksa.status_lanjut = 'Ranap' AND bridging_sep.jnspelayanan = '1' ) \n"
                    + "	)\n"
                    + "	LEFT JOIN inacbg_data_terkirim2 ON bridging_sep.no_sep = inacbg_data_terkirim2.no_sep\n"
                    + "	LEFT JOIN inacbg_grouping_stage12 ON bridging_sep.no_sep = inacbg_grouping_stage12.no_sep\n"
                    + "	LEFT JOIN pegawai ON inacbg_data_terkirim2.nik = pegawai.no_ktp\n"
                    + "	LEFT JOIN inacbg_data_terkirim_final_dc ON bridging_sep.no_sep = inacbg_data_terkirim_final_dc.no_sep"
                    + "	LEFT JOIN inacbg_klaim_umpanbalik ON bridging_sep.no_sep = inacbg_klaim_umpanbalik.no_sep "
                    + "WHERE " + belumgruping + " piutang_pasien.tgl_piutang between ? and ? "
                    + "AND detail_piutang_pasien.sisapiutang <= 0 "
                    + "AND detail_piutang_pasien.nama_bayar = '01. Piutang BPJS Kesehatan' "
                    + "AND reg_periksa.status_lanjut = 'Ranap' "
                    + // Only inpatient records
                    "AND (piutang_pasien.no_rawat LIKE ? OR piutang_pasien.no_rkm_medis LIKE ? OR pasien.nm_pasien LIKE ? OR bridging_sep.no_sep LIKE ?) "
                    + "ORDER BY bridging_sep.no_sep ASC, piutang_pasien.tgl_piutang ASC,piutang_pasien.no_rawat ASC"
            );

            try {
                ps.setString(1, Valid.SetTgl(Periode1.getSelectedItem() + ""));
                ps.setString(2, Valid.SetTgl(Period2.getSelectedItem() + ""));
                ps.setString(3, "%" + TCari.getText() + "%");
                ps.setString(4, "%" + TCari.getText() + "%");
                ps.setString(5, "%" + TCari.getText() + "%");
                ps.setString(6, "%" + TCari.getText() + "%");
                rs = ps.executeQuery();
                while (rs.next()) {
                    String currentNoRawat = rs.getString("no_rawat");

                    // Map to collect all unique doctors and their data for this no_rawat
                    Map<String, Map<String, Object>> allUniqueDoctors = new HashMap<>();

                    //rawat inap visit data
                    psrawatinapdr = koneksi.prepareStatement(
                            "SELECT\n"
                            + "	pasien.nm_pasien, \n"
                            + "	jns_perawatan_inap.nm_perawatan, \n"
                            + "	rawat_inap_dr.tarif_tindakandr, \n"
                            + "	rawat_inap_dr.tgl_perawatan, \n"
                            + "	rawat_inap_dr.jam_rawat, \n"
                            + "	reg_periksa.kd_pj, \n"
                            + "	rawat_inap_dr.kd_jenis_prw, \n"
                            + "	reg_periksa.no_rawat, \n"
                            + "	reg_periksa.no_rkm_medis, \n"
                            + "	SUM(rawat_inap_dr.tarif_tindakandr) AS tarifjmdokter, \n"
                            + "	count(rawat_inap_dr.kd_jenis_prw) AS counttindakan, \n"
                            + "	rawat_inap_dr.kd_dokter, \n"
                            + "	dokter.nm_dokter\n"
                            + " FROM\n"
                            + "	pasien\n"
                            + "	INNER JOIN\n"
                            + "	reg_periksa\n"
                            + "	ON \n"
                            + "		reg_periksa.no_rkm_medis = pasien.no_rkm_medis\n"
                            + "	INNER JOIN\n"
                            + "	rawat_inap_dr\n"
                            + "	ON \n"
                            + "		rawat_inap_dr.no_rawat = reg_periksa.no_rawat\n"
                            + "	INNER JOIN\n"
                            + "	jns_perawatan_inap\n"
                            + "	ON \n"
                            + "		rawat_inap_dr.kd_jenis_prw = jns_perawatan_inap.kd_jenis_prw\n"
                            + "	INNER JOIN\n"
                            + "	penjab\n"
                            + "	ON \n"
                            + "		reg_periksa.kd_pj = penjab.kd_pj\n"
                            + "	INNER JOIN\n"
                            + "	dokter\n"
                            + "	ON \n"
                            + "		rawat_inap_dr.kd_dokter = dokter.kd_dokter\n"
                            + " WHERE ( jns_perawatan_inap.nm_perawatan LIKE '%Visite Dokter Spesialis - Intensive%' OR jns_perawatan_inap.nm_perawatan LIKE '%Visite Dokter Spesialis%' OR jns_perawatan_inap.nm_perawatan LIKE '%Jasa Telekonsultasi dr Spesialis%' OR jns_perawatan_inap.nm_perawatan LIKE '%Jasa Konsultasi dr Spesialis%' OR jns_perawatan_inap.nm_perawatan LIKE '%Visite ICU%' OR jns_perawatan_inap.nm_perawatan LIKE '%Visite Dokter R.Isolasi%' )"
                            + "  and rawat_inap_dr.tarif_tindakandr>0 "
                            + "  and rawat_inap_dr.no_rawat=? "
                            + "  GROUP BY rawat_inap_dr.no_rawat, rawat_inap_dr.kd_dokter ");

                    try {
                        psrawatinapdr.setString(1, currentNoRawat);
                        rsrawatinapdr = psrawatinapdr.executeQuery();

                        // Collect visit doctors
                        while (rsrawatinapdr.next()) {
                            String kdDokter = rsrawatinapdr.getString("kd_dokter");
                            String nmDokter = rsrawatinapdr.getString("nm_dokter");
                            double tarifJmDokter = rsrawatinapdr.getDouble("tarifjmdokter");
                            int countTindakan = rsrawatinapdr.getInt("counttindakan");

                            Map<String, Object> doctorData = new HashMap<>();
                            doctorData.put("kd_dokter", kdDokter);
                            doctorData.put("nm_dokter", nmDokter);
                            doctorData.put("tarif_total", tarifJmDokter);
                            doctorData.put("count_tindakan", countTindakan);
                            doctorData.put("has_visit", true);
                            doctorData.put("has_operation", false);

                            allUniqueDoctors.put(kdDokter, doctorData);
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi Ranap : " + e);
                    } finally {
                        if (rsrawatinapdr != null) {
                            rsrawatinapdr.close();
                        }
                        if (psrawatinapdr != null) {
                            psrawatinapdr.close();
                        }
                    }

                    try {
                        // Process operation data - operator1
                        psbiayaoperator1 = koneksi.prepareStatement(
                                "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator1,operasi.status,"
                                + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                                + "sum(operasi.biayaoperator1) as operator1, operasi.operator1 as kd_dokter, d1.nm_dokter "
                                + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                                + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                                + "inner join dokter d1 on operasi.operator1 = d1.kd_dokter "
                                + "where operasi.biayaoperator1>0 and operasi.no_rawat=? GROUP BY operasi.operator1");

                        psbiayaoperator1.setString(1, currentNoRawat);
                        rsbiayaoperator1 = psbiayaoperator1.executeQuery();
                        while (rsbiayaoperator1.next()) {
                            String kdDokter = rsbiayaoperator1.getString("kd_dokter");
                            String nmDokter = rsbiayaoperator1.getString("nm_dokter");
                            double biayaOperator = rsbiayaoperator1.getDouble("operator1");

                            if (kdDokter != null && !kdDokter.trim().isEmpty()) {
                                if (allUniqueDoctors.containsKey(kdDokter)) {
                                    // Doctor already exists, keep visit tarif, add operation tarif separately
                                    Map<String, Object> doctorData = allUniqueDoctors.get(kdDokter);
                                    doctorData.put("tarif_operation", biayaOperator);
                                    doctorData.put("has_operation", true);
                                } else {
                                    // New doctor from operation
                                    Map<String, Object> doctorData = new HashMap<>();
                                    doctorData.put("kd_dokter", kdDokter);
                                    doctorData.put("nm_dokter", nmDokter);
                                    doctorData.put("tarif_total", 0.0); // No visit tarif
                                    doctorData.put("tarif_operation", biayaOperator);
                                    doctorData.put("count_tindakan", 1);
                                    doctorData.put("has_visit", false);
                                    doctorData.put("has_operation", true);
                                    allUniqueDoctors.put(kdDokter, doctorData);
                                }
                            }
                        }

                        // Process operation data - operator2
                        psbiayaoperator2 = koneksi.prepareStatement(
                                "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator2,operasi.status,"
                                + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                                + "sum(operasi.biayaoperator2) as operator2, operasi.operator2 as kd_dokter, d2.nm_dokter "
                                + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                                + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                                + "inner join dokter d2 on operasi.operator2 = d2.kd_dokter "
                                + "where operasi.biayaoperator2>0 and operasi.no_rawat=? GROUP BY operasi.operator2");

                        psbiayaoperator2.setString(1, currentNoRawat);
                        rsbiayaoperator2 = psbiayaoperator2.executeQuery();
                        while (rsbiayaoperator2.next()) {
                            String kdDokter = rsbiayaoperator2.getString("kd_dokter");
                            String nmDokter = rsbiayaoperator2.getString("nm_dokter");
                            double biayaOperator = rsbiayaoperator2.getDouble("operator2");

                            if (kdDokter != null && !kdDokter.trim().isEmpty()) {
                                if (allUniqueDoctors.containsKey(kdDokter)) {
                                    Map<String, Object> doctorData = allUniqueDoctors.get(kdDokter);
                                    // Add to existing operation tarif or set if doesn't exist
                                    double currentOpTarif = doctorData.containsKey("tarif_operation")
                                            ? (Double) doctorData.get("tarif_operation") : 0.0;
                                    doctorData.put("tarif_operation", currentOpTarif + biayaOperator);
                                    doctorData.put("has_operation", true);
                                } else {
                                    Map<String, Object> doctorData = new HashMap<>();
                                    doctorData.put("kd_dokter", kdDokter);
                                    doctorData.put("nm_dokter", nmDokter);
                                    doctorData.put("tarif_total", 0.0);
                                    doctorData.put("tarif_operation", biayaOperator);
                                    doctorData.put("count_tindakan", 1);
                                    doctorData.put("has_visit", false);
                                    doctorData.put("has_operation", true);
                                    allUniqueDoctors.put(kdDokter, doctorData);
                                }
                            }
                        }

                        // Process operation data - operator3
                        psbiayaoperator3 = koneksi.prepareStatement(
                                "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator3,operasi.status,"
                                + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                                + "sum(operasi.biayaoperator3) as operator3, operasi.operator3 as kd_dokter, d3.nm_dokter "
                                + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                                + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                                + "inner join dokter d3 on operasi.operator3 = d3.kd_dokter "
                                + "where operasi.biayaoperator3>0 and operasi.no_rawat=? GROUP BY operasi.operator3");

                        psbiayaoperator3.setString(1, currentNoRawat);
                        rsbiayaoperator3 = psbiayaoperator3.executeQuery();
                        // Apply the same pattern for all other operation sections
                        while (rsbiayaoperator3.next()) {
                            String kdDokter = rsbiayaoperator3.getString("kd_dokter");
                            String nmDokter = rsbiayaoperator3.getString("nm_dokter");
                            double biayaOperator = rsbiayaoperator3.getDouble("operator3");

                            if (kdDokter != null && !kdDokter.trim().isEmpty()) {
                                if (allUniqueDoctors.containsKey(kdDokter)) {
                                    Map<String, Object> doctorData = allUniqueDoctors.get(kdDokter);
                                    double currentOpTarif = doctorData.containsKey("tarif_operation")
                                            ? (Double) doctorData.get("tarif_operation") : 0.0;
                                    doctorData.put("tarif_operation", currentOpTarif + biayaOperator);
                                    doctorData.put("has_operation", true);
                                } else {
                                    Map<String, Object> doctorData = new HashMap<>();
                                    doctorData.put("kd_dokter", kdDokter);
                                    doctorData.put("nm_dokter", nmDokter);
                                    doctorData.put("tarif_total", 0.0);
                                    doctorData.put("tarif_operation", biayaOperator);
                                    doctorData.put("count_tindakan", 1);
                                    doctorData.put("has_visit", false);
                                    doctorData.put("has_operation", true);
                                    allUniqueDoctors.put(kdDokter, doctorData);
                                }
                            }
                        }

                        // Process dokter_anak
                        psbiayadokter_anak = koneksi.prepareStatement(
                                "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayadokter_anak,operasi.status,"
                                + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                                + "sum(operasi.biayadokter_anak) as dokter_anak, operasi.dokter_anak as kd_dokter, da.nm_dokter "
                                + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                                + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                                + "inner join dokter da on operasi.dokter_anak = da.kd_dokter "
                                + "where operasi.biayadokter_anak>0 and operasi.no_rawat=? GROUP BY operasi.dokter_anak");

                        // Process dokter_umum
                        psbiaya_dokter_umum = koneksi.prepareStatement(
                                "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biaya_dokter_umum,operasi.status,"
                                + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                                + "sum(operasi.biaya_dokter_umum) as dokter_umum, operasi.dokter_umum as kd_dokter, du.nm_dokter "
                                + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                                + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                                + "inner join dokter du on operasi.dokter_umum = du.kd_dokter "
                                + "where operasi.biaya_dokter_umum>0 and operasi.no_rawat=? GROUP BY operasi.dokter_umum");
// Process dokter_pjanak
                        psbiaya_dokter_pjanak = koneksi.prepareStatement(
                                "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biaya_dokter_pjanak,operasi.status,"
                                + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                                + "sum(operasi.biaya_dokter_pjanak) as dokter_pjanak, operasi.dokter_pjanak as kd_dokter, dp.nm_dokter "
                                + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                                + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                                + "inner join dokter dp on operasi.dokter_pjanak = dp.kd_dokter "
                                + "where operasi.biaya_dokter_pjanak>0 and operasi.no_rawat=? GROUP BY operasi.dokter_pjanak");

                        psbiayadokter_anak.setString(1, currentNoRawat);
                        rsbiayadokter_anak = psbiayadokter_anak.executeQuery();
                        while (rsbiayadokter_anak.next()) {
                            String kdDokter = rsbiayadokter_anak.getString("kd_dokter");
                            String nmDokter = rsbiayadokter_anak.getString("nm_dokter");
                            double biayaDokter = rsbiayadokter_anak.getDouble("dokter_anak");

                            if (kdDokter != null && !kdDokter.trim().isEmpty()) {
                                if (allUniqueDoctors.containsKey(kdDokter)) {
                                    Map<String, Object> doctorData = allUniqueDoctors.get(kdDokter);
                                    double currentOpTarif = doctorData.containsKey("tarif_operation")
                                            ? (Double) doctorData.get("tarif_operation") : 0.0;
                                    doctorData.put("tarif_operation", currentOpTarif + biayaDokter);
                                    doctorData.put("has_operation", true);
                                } else {
                                    Map<String, Object> doctorData = new HashMap<>();
                                    doctorData.put("kd_dokter", kdDokter);
                                    doctorData.put("nm_dokter", nmDokter);
                                    doctorData.put("tarif_total", 0.0);
                                    doctorData.put("tarif_operation", biayaDokter);
                                    doctorData.put("count_tindakan", 1);
                                    doctorData.put("has_visit", false);
                                    doctorData.put("has_operation", true);
                                    allUniqueDoctors.put(kdDokter, doctorData);
                                }
                            }
                        }

                        psbiaya_dokter_umum.setString(1, currentNoRawat);
                        rsbiaya_dokter_umum = psbiaya_dokter_umum.executeQuery();
                        while (rsbiaya_dokter_umum.next()) {
                            String kdDokter = rsbiaya_dokter_umum.getString("kd_dokter");
                            String nmDokter = rsbiaya_dokter_umum.getString("nm_dokter");
                            double biayaDokter = rsbiaya_dokter_umum.getDouble("dokter_umum");

                            if (kdDokter != null && !kdDokter.trim().isEmpty()) {
                                if (allUniqueDoctors.containsKey(kdDokter)) {
                                    Map<String, Object> doctorData = allUniqueDoctors.get(kdDokter);
                                    double currentOpTarif = doctorData.containsKey("tarif_operation")
                                            ? (Double) doctorData.get("tarif_operation") : 0.0;
                                    doctorData.put("tarif_operation", currentOpTarif + biayaDokter);
                                    doctorData.put("has_operation", true);
                                } else {
                                    Map<String, Object> doctorData = new HashMap<>();
                                    doctorData.put("kd_dokter", kdDokter);
                                    doctorData.put("nm_dokter", nmDokter);
                                    doctorData.put("tarif_total", 0.0);
                                    doctorData.put("tarif_operation", biayaDokter);
                                    doctorData.put("count_tindakan", 1);
                                    doctorData.put("has_visit", false);
                                    doctorData.put("has_operation", true);
                                    allUniqueDoctors.put(kdDokter, doctorData);
                                }
                            }
                        }

                        psbiaya_dokter_pjanak.setString(1, currentNoRawat);
                        rsbiaya_dokter_pjanak = psbiaya_dokter_pjanak.executeQuery();

                        while (rsbiaya_dokter_pjanak.next()) {
                            String kdDokter = rsbiaya_dokter_pjanak.getString("kd_dokter");
                            String nmDokter = rsbiaya_dokter_pjanak.getString("nm_dokter");
                            double biayaDokter = rsbiaya_dokter_pjanak.getDouble("dokter_pjanak");

                            if (kdDokter != null && !kdDokter.trim().isEmpty()) {
                                if (allUniqueDoctors.containsKey(kdDokter)) {
                                    Map<String, Object> doctorData = allUniqueDoctors.get(kdDokter);
                                    double currentOpTarif = doctorData.containsKey("tarif_operation")
                                            ? (Double) doctorData.get("tarif_operation") : 0.0;
                                    doctorData.put("tarif_operation", currentOpTarif + biayaDokter);
                                    doctorData.put("has_operation", true);
                                } else {
                                    Map<String, Object> doctorData = new HashMap<>();
                                    doctorData.put("kd_dokter", kdDokter);
                                    doctorData.put("nm_dokter", nmDokter);
                                    doctorData.put("tarif_total", 0.0);
                                    doctorData.put("tarif_operation", biayaDokter);
                                    doctorData.put("count_tindakan", 1);
                                    doctorData.put("has_visit", false);
                                    doctorData.put("has_operation", true);
                                    allUniqueDoctors.put(kdDokter, doctorData);
                                }
                            }
                        }

                        // Process dokter_anestesi
                        psbiayadokter_anestesi = koneksi.prepareStatement(
                                "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayadokter_anestesi,operasi.status,"
                                + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                                + "sum(operasi.biayadokter_anestesi) as dokter_anestesi, operasi.dokter_anestesi as kd_dokter, danes.nm_dokter "
                                + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                                + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                                + "inner join dokter danes on operasi.dokter_anestesi = danes.kd_dokter "
                                + "where operasi.biayadokter_anestesi>0 and operasi.no_rawat=? GROUP BY operasi.dokter_anestesi");

                        psbiayadokter_anestesi.setString(1, currentNoRawat);
                        rsbiayadokter_anestesi = psbiayadokter_anestesi.executeQuery();

                        while (rsbiayadokter_anestesi.next()) {
                            String kdDokter = rsbiayadokter_anestesi.getString("kd_dokter");
                            String nmDokter = rsbiayadokter_anestesi.getString("nm_dokter");
                            double biayaDokter = rsbiayadokter_anestesi.getDouble("dokter_anestesi");

                            if (kdDokter != null && !kdDokter.trim().isEmpty()) {
                                if (allUniqueDoctors.containsKey(kdDokter)) {
                                    Map<String, Object> doctorData = allUniqueDoctors.get(kdDokter);
                                    double currentOpTarif = doctorData.containsKey("tarif_operation")
                                            ? (Double) doctorData.get("tarif_operation") : 0.0;
                                    doctorData.put("tarif_operation", currentOpTarif + biayaDokter);
                                    doctorData.put("has_operation", true);
                                } else {
                                    Map<String, Object> doctorData = new HashMap<>();
                                    doctorData.put("kd_dokter", kdDokter);
                                    doctorData.put("nm_dokter", nmDokter);
                                    doctorData.put("tarif_total", 0.0);
                                    doctorData.put("tarif_operation", biayaDokter);
                                    doctorData.put("count_tindakan", 1);
                                    doctorData.put("has_visit", false);
                                    doctorData.put("has_operation", true);
                                    allUniqueDoctors.put(kdDokter, doctorData);
                                }
                            }
                        }

                    } catch (Exception e) {
                        System.out.println("Notifikasi Operasi : " + e);
                    } finally {
                        // Close all resources
                        if (rsbiayaoperator1 != null) {
                            rsbiayaoperator1.close();
                        }
                        if (psbiayaoperator1 != null) {
                            psbiayaoperator1.close();
                        }
                        if (rsbiayaoperator2 != null) {
                            rsbiayaoperator2.close();
                        }
                        if (psbiayaoperator2 != null) {
                            psbiayaoperator2.close();
                        }
                        if (rsbiayaoperator3 != null) {
                            rsbiayaoperator3.close();
                        }
                        if (psbiayaoperator3 != null) {
                            psbiayaoperator3.close();
                        }
                        if (rsbiayadokter_anak != null) {
                            rsbiayadokter_anak.close();
                        }
                        if (psbiayadokter_anak != null) {
                            psbiayadokter_anak.close();
                        }
                        if (rsbiaya_dokter_umum != null) {
                            rsbiaya_dokter_umum.close();
                        }
                        if (psbiaya_dokter_umum != null) {
                            psbiaya_dokter_umum.close();
                        }
                        if (rsbiaya_dokter_pjanak != null) {
                            rsbiaya_dokter_pjanak.close();
                        }
                        if (psbiaya_dokter_pjanak != null) {
                            psbiaya_dokter_pjanak.close();
                        }
                        if (rsbiayadokter_anestesi != null) {
                            rsbiayadokter_anestesi.close();
                        }
                        if (psbiayadokter_anestesi != null) {
                            psbiayadokter_anestesi.close();
                        }
                    }

                    // Now process all unique doctors and add rows
                    int totalUniqueDoctors = allUniqueDoctors.size();
                    boolean hasAnyOperation = allUniqueDoctors.values().stream().anyMatch(doctorData -> (Boolean) doctorData.get("has_operation"));

                    for (Map<String, Object> doctorData : allUniqueDoctors.values()) {
                        String kdDokter = (String) doctorData.get("kd_dokter");
                        String nmDokter = (String) doctorData.get("nm_dokter");
                        boolean hasVisit = (Boolean) doctorData.get("has_visit");
                        boolean hasOperation = (Boolean) doctorData.get("has_operation");
                        int countTindakan = (Integer) doctorData.get("count_tindakan");

                        // Calculate final tariff - visit tariff OR operation tariff (not combined)
                        double finalTariff;
                        if (hasVisit) {
                            // Use visit tariff (tarifjmdokter from visits)
                            finalTariff = (Double) doctorData.get("tarif_total");
                        } else {
                            // Use operation tariff only
                            finalTariff = doctorData.containsKey("tarif_operation")
                                    ? (Double) doctorData.get("tarif_operation") : 0.0;
                        }
                        String namadokter = (String) doctorData.get("nm_dokter");
                        double tarifTotal = (Double) doctorData.get("tarif_total");
                        int countTindakanbaru = (Integer) doctorData.get("count_tindakan");
                        boolean hasOperationbaru = (Boolean) doctorData.get("has_operation");

                        // Determine STATUS (Raber/Sendiri)
                        String statusText = (totalUniqueDoctors == 1) ? "Sendiri" : "Raber";

                        // Check if this doctor is DPJP
                        isDpjpUtama(kdDokter, currentNoRawat);

                        String statusdokter = isDPJP ? "DPJP" : isAnestesi(kdDokter, currentNoRawat);
                        String statusOperasi = hasAnyOperation ? "OP" : "NON-OP";

                        // Determine JENIS based on conditions
                        String jenisText;
                        if (hasAnyOperation) {
                            JComboBox<String> comboJenis = new JComboBox<>(new String[]{"20", "19", "18", "17", "16"});
                            DefaultCellEditor jenisEditor = new DefaultCellEditor(comboJenis);
                            jenisText = String.valueOf((int) kalkulator.getPersenDefaultRanap(true, isDPJP));
                        } else {
                            JComboBox<String> comboJenis = new JComboBox<>(new String[]{"9", "8", "7", "6", "5", "4", "3", "2", "0"});
                            DefaultCellEditor jenisEditor = new DefaultCellEditor(comboJenis);
                            if (statusdokter.equals("DP")) {
                                jenisText = String.valueOf((int) kalkulator.getPersenDefaultRanap(false, false));
                            } else {
                                jenisText = String.valueOf((int) kalkulator.getPersenDefaultRanap(false, true));
                            }

                        }

                        double jm_bpjs = calculateDoctorFee(
                                statusdokter,
                                statusOperasi,
                                statusText,
                                jenisText,
                                countTindakan,
                                rs.getDouble("umbal")
                        );

                        // Create a map for this record
                        Map<String, Object> record = new HashMap<>();
                        record.put("checked", false);
                        record.put("tgl_piutang", rs.getString("tgl_piutang"));
                        record.put("jam_reg", rs.getString("jam_reg"));
                        record.put("no_sep", rs.getString("no_sep"));
                        record.put("no_rawat", rs.getString("no_rawat"));
                        record.put("no_rkm_medis", rs.getString("no_rkm_medis"));
                        record.put("nm_pasien", rs.getString("nm_pasien"));
                        record.put("kd_dokter", kdDokter);
                        record.put("nm_dokter", nmDokter);
                        record.put("totalpiutang", rs.getDouble("totalpiutang"));
                        record.put("tarifTotal", tarifTotal);
                        record.put("umbal", rs.getDouble("umbal") + rs.getDouble("uangmuka"));
                        record.put("statusdokter", statusdokter);
                        record.put("statusOperasi", statusOperasi);
                        record.put("statusText", statusText);
                        record.put("jenisText", jenisText);
                        record.put("countTindakan", "Visit : " + countTindakan + " | Los:  " + String.valueOf(Valid.getLos(rs.getString("no_rawat"))) + " hari");
                        record.put("jm_bpjs", jm_bpjs);

                        //dalam 2 hari setidaknya tindakan ada 1, maka normal, kalau tidak memenuhi itu, maka tidak normal
                        //kalau dalam 5 hari visite min 2x maka normal
                        //kalau los 3 hari dan visite nya 1 dan di hari weekdays, maka tidak normal
                        //kalau los 3 hari dan visite nya 1 dan di hari weekend, maka normal
                        // Create a composite key for sorting
                        String compositeKey = rs.getString("tgl_piutang") + "|"
                                + rs.getString("no_sep") + "|" + rs.getString("no_rawat");

                        // Add to our sorted map
                        if (!sortedData.containsKey(compositeKey)) {
                            sortedData.put(compositeKey, new ArrayList<>());
                        }
                        sortedData.get(compositeKey).add(record);
                    }
                }

                // Now process the sorted data and adjust anesthesia fees
                for (List<Map<String, Object>> records : sortedData.values()) {
                    // First pass: find DPJP records and store their jm_bpjs
                    Map<String, Double> dpjpFees = new HashMap<>();
                    for (Map<String, Object> record : records) {
                        if ("DPJP".equals(record.get("statusdokter"))) {
                            String no_rawat = (String) record.get("no_rawat");
                            double jm_bpjs = (Double) record.get("jm_bpjs");
                            dpjpFees.put(no_rawat, jm_bpjs);
                        }
                    }

                    // Second pass: adjust anesthesia fees and sort records
                    List<Map<String, Object>> sortedRecords = new ArrayList<>(records);

                    // Sort so DPJP comes first, then others
                    sortedRecords.sort((a, b) -> {
                        boolean aIsDPJP = "DPJP".equals(a.get("statusdokter"));
                        boolean bIsDPJP = "DPJP".equals(b.get("statusdokter"));

                        if (aIsDPJP && !bIsDPJP) {
                            return -1;
                        }
                        if (!aIsDPJP && bIsDPJP) {
                            return 1;
                        }
                        return 0;
                    });

                    // Adjust anesthesia fees and add to table
                    for (Map<String, Object> record : sortedRecords) {
                        if ("Anestesi".equals(record.get("statusdokter"))) {
                            String no_rawat = (String) record.get("no_rawat");
                            if (dpjpFees.containsKey(no_rawat)) {
                                // Set anesthesia fee to 40% of DPJP fee
                                double dpjpFee = dpjpFees.get(no_rawat);
                                record.put("jm_bpjs", dpjpFee * 0.40);
                            }
                        }

//                        System.out.println(record.get("nm_pasien"));
                        // Add to table
                        tabModeRawatInap.addRow(new Object[]{
                            record.get("checked"),
                            record.get("tgl_piutang"),
                            record.get("jam_reg"),
                            record.get("no_sep"),
                            record.get("no_rawat"),
                            record.get("no_rkm_medis"),
                            record.get("nm_pasien"),
                            record.get("kd_dokter"),
                            record.get("nm_dokter"),
                            record.get("totalpiutang"),
                            record.get("tarifTotal"),
                            record.get("umbal"),
                            record.get("statusdokter"),
                            record.get("statusOperasi"),
                            record.get("statusText"),
                            record.get("jenisText"),
                            String.valueOf(record.get("countTindakan")),
                            record.get("jm_bpjs")
                        });
                    }
                }

            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }

                isLoadingData = false;
            }

        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void jalankanWorkerRawatInap() {
        if (workerRawatInap != null && !workerRawatInap.isDone()) {
            workerRawatInap.cancel(true);
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        BtnCari.setEnabled(false);
        isLoadingData = true;

        final String tglAwal = Valid.SetTgl(Periode1.getSelectedItem() + "");
        final String tglAkhir = Valid.SetTgl(Period2.getSelectedItem() + "");
        final String keyword = "%" + TCari.getText().trim() + "%";
        final String targetKdDokter = kddokter.getText().trim();
        final String targetTCari = TCari.getText().trim().toLowerCase(Locale.ROOT);

        workerRawatInap = new SwingWorker<List<Object[]>, Void>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> resultRows = new ArrayList<>();

                // 1. Pre-cache nama dokter, mapping DPJP VClaim, dan DPJP Ranap Utama
                Map<String, String> mapDokter = new HashMap<>();
                Map<String, Boolean> mapDokterUmum = new HashMap<>();
                Map<String, String> mapDpjpVClaimToRs = new HashMap<>();
                Map<String, String> mapSpesialis = new HashMap<>();
                Map<String, String> mapDpjpRanapUtama = new HashMap<>();

                try (PreparedStatement psDok = koneksi.prepareStatement("SELECT dokter.kd_dokter, dokter.nm_dokter, spesialis.nm_sps FROM dokter LEFT JOIN spesialis ON dokter.kd_sps = spesialis.kd_sps");
                     ResultSet rsDok = psDok.executeQuery()) {
                    while (rsDok.next()) {
                        String sps = rsDok.getString("nm_sps");
                        mapDokter.put(rsDok.getString("kd_dokter"), rsDok.getString("nm_dokter"));
                        mapDokterUmum.put(rsDok.getString("kd_dokter"), sps != null && sps.toLowerCase(Locale.ROOT).contains("umum"));
                        if (sps != null) {
                            mapSpesialis.put(rsDok.getString("kd_dokter"), sps);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Cache Dokter Ranap : " + e);
                }

                try (PreparedStatement psMap = koneksi.prepareStatement("SELECT kd_dokter, kd_dokter_bpjs FROM maping_dokter_dpjpvclaim");
                     ResultSet rsMap = psMap.executeQuery()) {
                    while (rsMap.next()) {
                        String kdBpjs = rsMap.getString("kd_dokter_bpjs");
                        String kdRs = rsMap.getString("kd_dokter");
                        if (kdBpjs != null && kdRs != null) {
                            mapDpjpVClaimToRs.put(kdBpjs, kdRs);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Cache Mapping DPJP Ranap : " + e);
                }

                try (PreparedStatement psDpjpRanap = koneksi.prepareStatement("SELECT no_rawat, kd_dokter FROM dpjp_ranap WHERE statusdpjp = 'Utama'");
                     ResultSet rsDpjpRanap = psDpjpRanap.executeQuery()) {
                    while (rsDpjpRanap.next()) {
                        mapDpjpRanapUtama.put(rsDpjpRanap.getString("no_rawat"), rsDpjpRanap.getString("kd_dokter"));
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Cache DPJP Ranap Utama : " + e);
                }

                if (isCancelled()) {
                    return resultRows;
                }

                // 2. Query data piutang rawat inap
                String sqlMain = "SELECT "
                        + "piutang_pasien.no_rawat, "
                        + "piutang_pasien.tgl_piutang, "
                        + "reg_periksa.jam_reg, "
                        + "bridging_sep.no_sep, "
                        + "piutang_pasien.no_rkm_medis, "
                        + "piutang_pasien.uangmuka, "
                        + "pasien.nm_pasien, "
                        + "reg_periksa.kd_dokter, "
                        + "dokter.nm_dokter, "
                        + "detail_piutang_pasien.totalpiutang, "
                        + "bridging_sep.kddpjp, "
                        + "IFNULL(inacbg_klaim_umpanbalik.bydisetujui, '0') AS umbal, "
                        + "IFNULL(inacbg_klaim_umpanbalik.status, '') AS statusklaim "
                        + "FROM piutang_pasien "
                        + "INNER JOIN reg_periksa ON piutang_pasien.no_rawat = reg_periksa.no_rawat "
                        + "INNER JOIN pasien ON piutang_pasien.no_rkm_medis = pasien.no_rkm_medis "
                        + "INNER JOIN dokter ON reg_periksa.kd_dokter = dokter.kd_dokter "
                        + "INNER JOIN detail_piutang_pasien ON piutang_pasien.no_rawat = detail_piutang_pasien.no_rawat "
                        + "INNER JOIN penjab ON detail_piutang_pasien.kd_pj = penjab.kd_pj "
                        + "INNER JOIN bridging_sep ON detail_piutang_pasien.no_rawat = bridging_sep.no_rawat "
                        + "    AND ((reg_periksa.status_lanjut = 'Ralan' AND bridging_sep.jnspelayanan = '2') "
                        + "         OR (reg_periksa.status_lanjut = 'Ranap' AND bridging_sep.jnspelayanan = '1')) "
                        + "LEFT JOIN inacbg_klaim_umpanbalik ON bridging_sep.no_sep = inacbg_klaim_umpanbalik.no_sep "
                        + "WHERE piutang_pasien.tgl_piutang BETWEEN ? AND ? "
                        + "AND detail_piutang_pasien.sisapiutang <= 0 "
                        + "AND detail_piutang_pasien.nama_bayar = '01. Piutang BPJS Kesehatan' "
                        + "AND reg_periksa.status_lanjut = 'Ranap' "
                        + "AND (piutang_pasien.no_rawat LIKE ? OR piutang_pasien.no_rkm_medis LIKE ? OR pasien.nm_pasien LIKE ? OR bridging_sep.no_sep LIKE ?) "
                        + "ORDER BY bridging_sep.no_sep ASC, piutang_pasien.tgl_piutang ASC, piutang_pasien.no_rawat ASC";

                String sqlVisit = "SELECT rawat_inap_dr.kd_dokter, dokter.nm_dokter, "
                        + "SUM(rawat_inap_dr.tarif_tindakandr) AS tarifjmdokter, "
                        + "COUNT(rawat_inap_dr.kd_jenis_prw) AS counttindakan "
                        + "FROM rawat_inap_dr "
                        + "INNER JOIN jns_perawatan_inap ON rawat_inap_dr.kd_jenis_prw = jns_perawatan_inap.kd_jenis_prw "
                        + "INNER JOIN dokter ON rawat_inap_dr.kd_dokter = dokter.kd_dokter "
                        + "WHERE (jns_perawatan_inap.nm_perawatan LIKE '%Visite%' OR jns_perawatan_inap.nm_perawatan LIKE '%Konsultasi%') "
                        + "AND rawat_inap_dr.tarif_tindakandr > 0 AND rawat_inap_dr.no_rawat = ? "
                        + "GROUP BY rawat_inap_dr.kd_dokter";

                String sqlOp1 = "SELECT operasi.operator1 AS kd_dokter, d1.nm_dokter, SUM(operasi.biayaoperator1) AS biaya, paket_operasi.nm_perawatan "
                        + "FROM operasi "
                        + "INNER JOIN dokter d1 ON operasi.operator1 = d1.kd_dokter "
                        + "INNER JOIN paket_operasi ON operasi.kode_paket = paket_operasi.kode_paket "
                        + "WHERE operasi.biayaoperator1 > 0 AND operasi.no_rawat = ? "
                        + "GROUP BY operasi.operator1, paket_operasi.nm_perawatan";

                String sqlOp2 = "SELECT operasi.operator2 AS kd_dokter, d2.nm_dokter, SUM(operasi.biayaoperator2) AS biaya, paket_operasi.nm_perawatan "
                        + "FROM operasi "
                        + "INNER JOIN dokter d2 ON operasi.operator2 = d2.kd_dokter "
                        + "INNER JOIN paket_operasi ON operasi.kode_paket = paket_operasi.kode_paket "
                        + "WHERE operasi.biayaoperator2 > 0 AND operasi.no_rawat = ? "
                        + "GROUP BY operasi.operator2, paket_operasi.nm_perawatan";

                String sqlOp3 = "SELECT operasi.operator3 AS kd_dokter, d3.nm_dokter, SUM(operasi.biayaoperator3) AS biaya, paket_operasi.nm_perawatan "
                        + "FROM operasi "
                        + "INNER JOIN dokter d3 ON operasi.operator3 = d3.kd_dokter "
                        + "INNER JOIN paket_operasi ON operasi.kode_paket = paket_operasi.kode_paket "
                        + "WHERE operasi.biayaoperator3 > 0 AND operasi.no_rawat = ? "
                        + "GROUP BY operasi.operator3, paket_operasi.nm_perawatan";

                String sqlAnest = "SELECT operasi.dokter_anestesi AS kd_dokter, danes.nm_dokter, SUM(operasi.biayadokter_anestesi) AS biaya "
                        + "FROM operasi "
                        + "INNER JOIN dokter danes ON operasi.dokter_anestesi = danes.kd_dokter "
                        + "WHERE operasi.biayadokter_anestesi > 0 AND operasi.no_rawat = ? "
                        + "GROUP BY operasi.dokter_anestesi";

                String sqlAnak = "SELECT operasi.dokter_anak AS kd_dokter, da.nm_dokter, SUM(operasi.biayadokter_anak) AS biaya "
                        + "FROM operasi "
                        + "INNER JOIN dokter da ON operasi.dokter_anak = da.kd_dokter "
                        + "WHERE operasi.biayadokter_anak > 0 AND operasi.no_rawat = ? "
                        + "GROUP BY operasi.dokter_anak";

                String sqlRadRanap = "SELECT periksa_radiologi.kd_dokter, dokter.nm_dokter, "
                        + "SUM(periksa_radiologi.tarif_tindakan_dokter) AS tarif_rad, "
                        + "GROUP_CONCAT(jns_perawatan_radiologi.nm_perawatan SEPARATOR ', ') AS nm_rad "
                        + "FROM periksa_radiologi "
                        + "INNER JOIN dokter ON periksa_radiologi.kd_dokter = dokter.kd_dokter "
                        + "INNER JOIN jns_perawatan_radiologi ON periksa_radiologi.kd_jenis_prw = jns_perawatan_radiologi.kd_jenis_prw "
                        + "WHERE periksa_radiologi.no_rawat = ? AND periksa_radiologi.status = 'Ranap' "
                        + "AND periksa_radiologi.tarif_tindakan_dokter > 0 "
                        + "GROUP BY periksa_radiologi.kd_dokter";

                String sqlCob = "SELECT nama_bayar FROM detail_piutang_pasien WHERE no_rawat = ?";

                Map<String, RanapSepGroup> sepGroups = new LinkedHashMap<>();

                try (PreparedStatement psMain = koneksi.prepareStatement(sqlMain);
                     PreparedStatement psVisit = koneksi.prepareStatement(sqlVisit);
                     PreparedStatement psOp1 = koneksi.prepareStatement(sqlOp1);
                     PreparedStatement psOp2 = koneksi.prepareStatement(sqlOp2);
                     PreparedStatement psOp3 = koneksi.prepareStatement(sqlOp3);
                     PreparedStatement psAnest = koneksi.prepareStatement(sqlAnest);
                     PreparedStatement psAnak = koneksi.prepareStatement(sqlAnak);
                     PreparedStatement psRad = koneksi.prepareStatement(sqlRadRanap);
                     PreparedStatement psCob = koneksi.prepareStatement(sqlCob)) {

                    psMain.setString(1, tglAwal);
                    psMain.setString(2, tglAkhir);
                    psMain.setString(3, keyword);
                    psMain.setString(4, keyword);
                    psMain.setString(5, keyword);
                    psMain.setString(6, keyword);

                    try (ResultSet rsMain = psMain.executeQuery()) {
                        while (rsMain.next()) {
                            if (isCancelled()) {
                                return resultRows;
                            }

                            String currentNoRawat = rsMain.getString("no_rawat");
                            String statusKlaim = rsMain.getString("statusklaim") != null ? rsMain.getString("statusklaim") : "";

                            if (!isFilterStatusKlaimPass(statusKlaim)) {
                                continue;
                            }

                            // Deteksi COB
                            psCob.setString(1, currentNoRawat);
                            StringBuilder hasilCOB = new StringBuilder();
                            int cobCount = 0;
                            try (ResultSet rsCob = psCob.executeQuery()) {
                                while (rsCob.next()) {
                                    if (cobCount > 0) {
                                        hasilCOB.append(" | ");
                                    }
                                    hasilCOB.append(rsCob.getString("nama_bayar"));
                                    cobCount++;
                                }
                            }
                            boolean isCob = (cobCount >= 2);
                            if (!isFilterCOBPass(isCob)) {
                                continue;
                            }
                            String ketCob = isCob ? "COB (" + hasilCOB.toString() + ")" : "-";

                            String noSep = rsMain.getString("no_sep") != null ? rsMain.getString("no_sep") : "";
                            String sepKey = (!noSep.trim().isEmpty() && !noSep.equalsIgnoreCase("Belum di Grouping")) ? noSep.trim() : currentNoRawat;

                            RanapSepGroup group = sepGroups.computeIfAbsent(sepKey, k -> new RanapSepGroup());
                            group.noSep = noSep;
                            group.tglPiutang = rsMain.getString("tgl_piutang");
                            group.jamReg = rsMain.getString("jam_reg");
                            group.statusKlaim = statusKlaim;
                            group.ketCob = ketCob;
                            group.umbal = Math.max(group.umbal, rsMain.getDouble("umbal"));
                            group.uangmuka = Math.max(group.uangmuka, rsMain.getDouble("uangmuka"));

                            String noRm = rsMain.getString("no_rkm_medis");
                            String nmPasien = rsMain.getString("nm_pasien");
                            double totPiutang = rsMain.getDouble("totalpiutang");

                            group.noRawats.add(currentNoRawat);
                            group.noRms.add(noRm);

                            if (group.dpjpSep.isEmpty()) {
                                String kdDpjpVclaim = rsMain.getString("kddpjp");
                                group.dpjpSep = mapDpjpVClaimToRs.getOrDefault(kdDpjpVclaim, "");
                            }

                            String dpjpSimrs = mapDpjpRanapUtama.getOrDefault(currentNoRawat, rsMain.getString("kd_dokter"));
                            if (group.primaryDpjpSimrs.isEmpty()) {
                                group.primaryDpjpSimrs = dpjpSimrs;
                            }

                            // 1. Visite Rawat Inap
                            psVisit.setString(1, currentNoRawat);
                            try (ResultSet rsVisit = psVisit.executeQuery()) {
                                while (rsVisit.next()) {
                                    String kdDok = rsVisit.getString("kd_dokter");
                                    String nmDok = rsVisit.getString("nm_dokter");
                                    double tarifJm = rsVisit.getDouble("tarifjmdokter");
                                    int countTind = rsVisit.getInt("counttindakan");

                                    RanapDoctorInfo d = group.doctorMap.computeIfAbsent(kdDok, k -> new RanapDoctorInfo());
                                    d.kdDokter = kdDok;
                                    d.nmDokter = nmDok;
                                    d.noRawat = currentNoRawat;
                                    d.noRkmMedis = noRm;
                                    d.nmPasien = nmPasien;
                                    d.totalPiutang = totPiutang;
                                    d.isDokterUmum = mapDokterUmum.getOrDefault(kdDok, false);
                                    d.visitCount += countTind;
                                    d.tarifVisite += tarifJm;
                                    d.hasVisit = true;
                                }
                            }

                            // Helper untuk memproses operator bedah
                            java.util.function.Consumer<PreparedStatement> processOp = (psStmt) -> {
                                try {
                                    psStmt.setString(1, currentNoRawat);
                                    try (ResultSet rsOp = psStmt.executeQuery()) {
                                        while (rsOp.next()) {
                                            String kdDok = rsOp.getString("kd_dokter");
                                            String nmDok = rsOp.getString("nm_dokter");
                                            double biayaOp = rsOp.getDouble("biaya");
                                            String nmPrw = rsOp.getString("nm_perawatan");
                                            String nmUpper = nmPrw != null ? nmPrw.toUpperCase(Locale.ROOT) : "";

                                            group.hasOperation = true;
                                            if (nmUpper.contains("SC") || nmUpper.contains("SECTIO") || nmUpper.contains("CAESAR")) {
                                                group.isPostSC = true;
                                            }
                                            if (isDoubleLumenText(nmPrw)) {
                                                group.hasDoubleLumen = true;
                                            }
                                            if (isAvShuntText(nmPrw)) {
                                                group.hasAVShunt = true;
                                            }

                                            if (kdDok != null && !kdDok.trim().isEmpty()) {
                                                group.operators.add(kdDok);
                                                RanapDoctorInfo d = group.doctorMap.computeIfAbsent(kdDok, k -> new RanapDoctorInfo());
                                                d.kdDokter = kdDok;
                                                d.nmDokter = nmDok;
                                                d.noRawat = currentNoRawat;
                                                d.noRkmMedis = noRm;
                                                d.nmPasien = nmPasien;
                                                d.totalPiutang = totPiutang;
                                                d.opTarif += biayaOp;
                                                d.hasOperation = true;
                                                d.isDokterUmum = mapDokterUmum.getOrDefault(kdDok, false);
                                                d.isOperator = true;
                                            }
                                        }
                                    }
                                } catch (Exception ex) {
                                    // Log silently
                                }
                            };

                            processOp.accept(psOp1);
                            processOp.accept(psOp2);
                            processOp.accept(psOp3);

                            // Dokter Anestesi
                            try {
                                psAnest.setString(1, currentNoRawat);
                                try (ResultSet rsAnest = psAnest.executeQuery()) {
                                    while (rsAnest.next()) {
                                        String kdDok = rsAnest.getString("kd_dokter");
                                        String nmDok = rsAnest.getString("nm_dokter");
                                        double biayaAnest = rsAnest.getDouble("biaya");

                                        group.hasOperation = true;
                                        if (kdDok != null && !kdDok.trim().isEmpty()) {
                                            group.dokterAnestesi = kdDok;
                                            RanapDoctorInfo d = group.doctorMap.computeIfAbsent(kdDok, k -> new RanapDoctorInfo());
                                            d.kdDokter = kdDok;
                                            d.nmDokter = nmDok;
                                            d.noRawat = currentNoRawat;
                                            d.noRkmMedis = noRm;
                                            d.nmPasien = nmPasien;
                                            d.totalPiutang = totPiutang;
                                            d.opTarif += biayaAnest;
                                            d.hasOperation = true;
                                            d.isDokterUmum = mapDokterUmum.getOrDefault(kdDok, false);
                                            d.isAnestesi = true;
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                // Log silently
                            }

                            // Dokter Anak di OK
                            try {
                                psAnak.setString(1, currentNoRawat);
                                // 7. Pemeriksaan Radiologi Ranap
                                psRad.setString(1, currentNoRawat);
                                try (ResultSet rsRad = psRad.executeQuery()) {
                                    while (rsRad.next()) {
                                        String kdDok = rsRad.getString("kd_dokter");
                                        String nmDok = rsRad.getString("nm_dokter");
                                        double tarifRad = rsRad.getDouble("tarif_rad");
                                        String nmRad = rsRad.getString("nm_rad");

                                        RanapDoctorInfo d = group.doctorMap.computeIfAbsent(kdDok, k -> new RanapDoctorInfo());
                                        d.kdDokter = kdDok;
                                        d.nmDokter = nmDok != null ? nmDok : mapDokter.getOrDefault(kdDok, kdDok);
                                        d.noRawat = currentNoRawat;
                                        d.noRkmMedis = noRm;
                                        d.nmPasien = nmPasien;
                                        d.totalPiutang = totPiutang;
                                        d.opTarif += tarifRad;
                                        d.isRadiologi = true;
                                        d.ketTindakan = "Tarif Normal RS (Bebas Aturan BPJS) | " + (nmRad != null ? nmRad : "Pemeriksaan Radiologi");
                                    }
                                }

                                try (ResultSet rsAnak = psAnak.executeQuery()) {
                                    while (rsAnak.next()) {
                                        String kdDok = rsAnak.getString("kd_dokter");
                                        String nmDok = rsAnak.getString("nm_dokter");
                                        double biayaAnak = rsAnak.getDouble("biaya");

                                        if (kdDok != null && !kdDok.trim().isEmpty()) {
                                            group.dokterAnak = kdDok;
                                            RanapDoctorInfo d = group.doctorMap.computeIfAbsent(kdDok, k -> new RanapDoctorInfo());
                                            d.kdDokter = kdDok;
                                            d.nmDokter = nmDok;
                                            d.noRawat = currentNoRawat;
                                            d.noRkmMedis = noRm;
                                            d.nmPasien = nmPasien;
                                            d.totalPiutang = totPiutang;
                                            d.opTarif += biayaAnak;
                                            d.isDokterUmum = mapDokterUmum.getOrDefault(kdDok, false);
                                            d.isDokterAnak = true;
                                        }
                                    }
                                }
                            } catch (Exception ex) {
                                // Log silently
                            }
                        }
                    }

                    // Tahap 2: Evaluasi Aturan Baku Ranap (26 Aturan) per No. SEP
                    for (RanapSepGroup group : sepGroups.values()) {
                        if (isCancelled()) {
                            return resultRows;
                        }

                        double totalKlaim = group.umbal + group.uangmuka;
                        boolean hasMultipleRm = (group.noRms.size() >= 2);
                        String babyRm = hasMultipleRm ? Collections.max(group.noRms) : "";

                        // Tandai DPJP SIMRS dan DPJP SEP
                        for (RanapDoctorInfo d : group.doctorMap.values()) {
                            if (d.kdDokter.equals(group.primaryDpjpSimrs)) {
                                d.isDpjpSimrs = true;
                            }
                            if (!group.dpjpSep.isEmpty() && d.kdDokter.equals(group.dpjpSep)) {
                                d.isDpjpSep = true;
                            }
                        }

                        // Deteksi Alih Rawat (Transfer Pasien)
                        boolean isAlihRawat = (!group.dpjpSep.isEmpty() && !group.primaryDpjpSimrs.isEmpty() && !group.dpjpSep.equalsIgnoreCase(group.primaryDpjpSimrs));

                        // Hitung Dokter Tandem (Dokter selain DPJP, Operator, dan Anestesi)
                        int tandemCount = 0;
                        int totalTandemVisits = 0;
                        for (RanapDoctorInfo d : group.doctorMap.values()) {
                            if (!d.isDpjpSimrs && !d.isDpjpSep && !d.isAnestesi && !d.isOperator && !d.isDokterUmum && !d.isRadiologi) {
                                tandemCount++;
                                totalTandemVisits += d.visitCount;
                            }
                        }

                        int dpjpVisitCount = 0;
                        if (group.doctorMap.containsKey(group.primaryDpjpSimrs)) {
                            dpjpVisitCount = group.doctorMap.get(group.primaryDpjpSimrs).visitCount;
                        }

                        boolean isMultiOp = (group.operators.size() >= 2);

                        // Estimasi hak operator utama untuk patokan Anestesi (40%)
                        double feeOperatorUtama = 0.0;
                        for (String opKd : group.operators) {
                            RanapDoctorInfo opDoc = group.doctorMap.get(opKd);
                            if (opDoc != null) {
                                double opFee = KalkulatorJasaMedisDokter.hitungJasaMedisDokterRawatInap(
                                        totalKlaim, true, opDoc.isDpjpSimrs, opDoc.isDpjpSep, true, false,
                                        opDoc.visitCount, dpjpVisitCount, tandemCount, opDoc.visitCount, totalTandemVisits,
                                        isAlihRawat, false, false, isMultiOp, (tandemCount > 0), 0.0
                                );
                                feeOperatorUtama += opFee;
                            }
                        }

                        // Hitung Jasa Medis tiap dokter dalam grup SEP
                        for (RanapDoctorInfo d : group.doctorMap.values()) {
                            boolean isBabyDoctor = (hasMultipleRm && d.noRkmMedis.equals(babyRm) && !d.hasOperation);

                            // 0. Pengecualian Khusus Dokter Radiologi (Sp.Rad) & Dokter Gigi Bedah Mulut (Sp.BM)
                            if (d.isRadiologi || isDokterBebasAturanBPJS(d.kdDokter, d.nmDokter, "", mapSpesialis)) {
                                d.calculatedJm = (d.hasVisit ? d.tarifVisite : d.opTarif);
                                d.jenisPctStr = "-";
                                if (d.ketTindakan == null || !d.ketTindakan.startsWith("Tarif Normal RS")) {
                                    d.ketTindakan = "Tarif Normal RS (Bebas Aturan BPJS)";
                                }
                            }
                            // 0.1 Kasus Dokter Umum di Ranap (Non-JM, payroll terpisah)
                            else if (d.isDokterUmum) {
                                d.calculatedJm = 0.0;
                                d.jenisPctStr = "0";
                                d.ketTindakan = "Dokter Umum Ranap (Non-JM)";
                            }
                            // 1. Kasus Double Lumen di Ranap
                            else if (group.hasDoubleLumen && d.isOperator) {
                                d.calculatedJm = KalkulatorJasaMedisDokter.getParameter1("RANAP_TK_DOUBLE_LUMEN", KalkulatorJasaMedisDokter.getParameter1("RANAP_DOUBLE_LUMEN", 500000.0));
                                d.jenisPctStr = "-";
                                d.ketTindakan = "Double Lumen Ranap";
                            }
                            // 2. Kasus AV Shunt di Ranap
                            else if (group.hasAVShunt && d.isOperator) {
                                d.calculatedJm = KalkulatorJasaMedisDokter.getParameter1("RANAP_TK_AV_SHUNT", KalkulatorJasaMedisDokter.getParameter1("RANAP_AV_SHUNT", 575000.0));
                                d.jenisPctStr = "-";
                                d.ketTindakan = "AV Shunt Ranap";
                            }
                            // 3. Kasus Dokter Anestesi (Universal 40% dari Operator)
                            else if (d.isAnestesi) {
                                d.calculatedJm = feeOperatorUtama * kalkulator.getPersenAnestesiRanap();
                                d.jenisPctStr = String.valueOf((int) Math.round(kalkulator.getPersenAnestesiRanap() * 100));
                                d.ketTindakan = "Operasi (Anestesi)";
                            }
                            // 4. Kasus Dokter Anak Pasien Bayi Baru Lahir
                            else if (isBabyDoctor) {
                                if (group.isPostSC) {
                                    double pct = KalkulatorJasaMedisDokter.getParameter1("RANAP_ANAK_POST_SC", 4.0);
                                    d.calculatedJm = totalKlaim * (pct / 100.0);
                                    d.jenisPctStr = String.valueOf((int) pct);
                                    d.ketTindakan = "Bayi Lahir Post SC (dr. Anak)";
                                } else {
                                    d.calculatedJm = KalkulatorJasaMedisDokter.getParameter1("RANAP_ANAK_POST_PARTUM", 60000.0);
                                    d.jenisPctStr = "-";
                                    d.ketTindakan = "Bayi Lahir Post Partum (dr. Anak)";
                                }
                            }
                            // 5. Standar Operasi / Non-Operasi via Kalkulator JM BPJS
                            else {
                                d.calculatedJm = KalkulatorJasaMedisDokter.hitungJasaMedisDokterRawatInap(
                                        totalKlaim, group.hasOperation, d.isDpjpSimrs, d.isDpjpSep, d.isOperator, d.isAnestesi,
                                        d.visitCount, dpjpVisitCount, tandemCount, d.visitCount, totalTandemVisits,
                                        isAlihRawat, false, false, isMultiOp, (tandemCount > 0), feeOperatorUtama
                                );
                                if (totalKlaim > 0 && d.calculatedJm > 0) {
                                    int pct = (int) Math.round((d.calculatedJm / totalKlaim) * 100.0);
                                    d.jenisPctStr = String.valueOf(pct);
                                } else {
                                    d.jenisPctStr = "0";
                                }

                                if (d.isOperator) {
                                    d.ketTindakan = "Operasi (Operator)";
                                } else if (d.isDokterAnak) {
                                    d.ketTindakan = "Operasi (dr. Anak)";
                                } else if (d.isDokterUmum) {
                                    d.ketTindakan = "Operasi (dr. Umum)";
                                } else if (group.hasOperation) {
                                    d.ketTindakan = "Operasi (" + (d.hasVisit ? "Visite" : "Konsultasi") + ")";
                                } else if (isAlihRawat) {
                                    d.ketTindakan = "Alih Rawat (" + (d.isDpjpSimrs || d.isDpjpSep ? "DPJP" : "Tandem") + ")";
                                } else {
                                    d.ketTindakan = (d.isDpjpSimrs || d.isDpjpSep ? "DPJP (" : "Tandem (") + (d.hasVisit ? "Visite " + d.visitCount + "x)" : "Konsultasi)");
                                }
                            }

                            String statusDokter = d.isRadiologi ? "Radiologi" : (d.isDokterUmum ? "Dokter Umum" : (d.isOperator ? "DPJP" : (d.isAnestesi ? "Anestesi" : (d.isDpjpSimrs || d.isDpjpSep ? "DPJP" : "DP"))));
                            String statusOperasi = d.isRadiologi ? "RAD" : (group.hasOperation ? "OP" : "NON-OP");
                            String statusText = (group.doctorMap.size() == 1) ? "Sendiri" : "Raber";
                            String visitLos = d.hasOperation && !d.hasVisit
                                    ? "Operasi"
                                    : "Visit : " + d.visitCount + " | Los: " + Valid.getLos(d.noRawat) + " hari";

                            String colKetRanap = d.ketTindakan;
                            if (group.ketCob != null && !group.ketCob.trim().isEmpty() && !group.ketCob.equals("-")) {
                                colKetRanap = (colKetRanap.isEmpty() ? "" : colKetRanap + " | ") + group.ketCob;
                            }
                            if (colKetRanap.isEmpty()) {
                                colKetRanap = "-";
                            }

                            // Filter target dokter jika kddokter diisi
                            if (!targetKdDokter.isEmpty() && !d.kdDokter.equalsIgnoreCase(targetKdDokter)) {
                                continue;
                            }
                            // Filter TCari terhadap data baris
                            if (!targetTCari.isEmpty()) {
                                boolean matchKeyword = d.kdDokter.toLowerCase(Locale.ROOT).contains(targetTCari)
                                        || d.nmDokter.toLowerCase(Locale.ROOT).contains(targetTCari)
                                        || d.noRawat.toLowerCase(Locale.ROOT).contains(targetTCari)
                                        || d.noRkmMedis.toLowerCase(Locale.ROOT).contains(targetTCari)
                                        || d.nmPasien.toLowerCase(Locale.ROOT).contains(targetTCari)
                                        || group.noSep.toLowerCase(Locale.ROOT).contains(targetTCari);
                                if (!matchKeyword) {
                                    continue;
                                }
                            }

                            resultRows.add(new Object[]{
                                false, // 0: P
                                group.tglPiutang, // 1: Tanggal Piutang
                                group.jamReg, // 2: Jam
                                group.noSep, // 3: No SEP
                                d.noRawat, // 4: No Rawat
                                d.noRkmMedis, // 5: No RM
                                d.nmPasien, // 6: Nama Pasien
                                d.kdDokter, // 7: Kode Dokter
                                d.nmDokter, // 8: Nama Dokter
                                d.totalPiutang, // 9: Total Piutang
                                d.hasVisit ? d.tarifVisite : d.opTarif, // 10: Total Tarif JM
                                totalKlaim, // 11: Umbal + TopUp
                                group.statusKlaim, // 12: Status
                                colKetRanap, // 13: Keterangan (Tindakan & COB)
                                statusDokter, // 14: DPJP
                                statusOperasi, // 15: TINDAKAN
                                statusText, // 16: STATUS
                                d.jenisPctStr, // 17: %
                                visitLos, // 18: Visit | LOS
                                d.calculatedJm // 19: JM BPJS
                            });
                        }
                    }
                }

                return resultRows;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();
                    if (rows != null) {
                        for (Object[] rowData : rows) {
                            tabModeRawatInap.addRow(rowData);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi SwingWorker Ranap : " + e);
                } finally {
                    setCursor(Cursor.getDefaultCursor());
                    BtnCari.setEnabled(true);
                    isLoadingData = false;
                }
            }
        };

        workerRawatInap.execute();
    }

    private void prosesCariSemua(String norawat, String kodedokter, String jenis) {

        try {

            if (jenis.equals("rajal")) {
                //rawat jalan
                psrawatjalandr = koneksi.prepareStatement("select pasien.nm_pasien,rawat_jl_dr.tarif_tindakandr,"
                        + "jns_perawatan.nm_perawatan,rawat_jl_dr.tgl_perawatan,rawat_jl_dr.jam_rawat,reg_periksa.kd_pj,rawat_jl_dr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,kategori_perawatan.nm_kategori as kategoriperawatan from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_jl_dr on rawat_jl_dr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan on rawat_jl_dr.kd_jenis_prw=jns_perawatan.kd_jenis_prw inner join kategori_perawatan ON jns_perawatan.kd_kategori=kategori_perawatan.kd_kategori "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where rawat_jl_dr.kd_dokter=? and rawat_jl_dr.tarif_tindakandr>0 and rawat_jl_dr.no_rawat=?");
                psrawatjalandrpr = koneksi.prepareStatement("select pasien.nm_pasien,rawat_jl_drpr.tarif_tindakandr,rawat_jl_drpr.tarif_tindakanpr,"
                        + "jns_perawatan.nm_perawatan,rawat_jl_drpr.tgl_perawatan,rawat_jl_drpr.jam_rawat,reg_periksa.kd_pj,rawat_jl_drpr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,kategori_perawatan.nm_kategori as kategoriperawatan from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_jl_drpr on rawat_jl_drpr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan on rawat_jl_drpr.kd_jenis_prw=jns_perawatan.kd_jenis_prw inner join kategori_perawatan ON jns_perawatan.kd_kategori=kategori_perawatan.kd_kategori "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where rawat_jl_drpr.kd_dokter=? and rawat_jl_drpr.no_rawat=? ");
                try {
                    psrawatjalandr.setString(1, kodedokter);
                    psrawatjalandr.setString(2, norawat);
                    rsrawatjalandr = psrawatjalandr.executeQuery();
                    psrawatjalandrpr.setString(1, kodedokter);
                    psrawatjalandrpr.setString(2, norawat);
                    rsrawatjalandrpr = psrawatjalandrpr.executeQuery();

                    //disini cari tindakan rawat jalan
                    while (rsrawatjalandr.next()) {
                        totaljmtarif = totaljmtarif + rsrawatjalandr.getDouble("tarif_tindakandr");
                        if (!rsrawatjalandr.getString("kategoriperawatan").contains("VISIT & KONSULTASI") && !rsrawatjalandr.getString("nm_perawatan").contains("SISTEM SINGLE USE")) {
                            isTindakan = true;
                        }

                        if (!rsrawatjalandr.getString("kategoriperawatan").contains("VISIT & KONSULTASI") && rsrawatjalandr.getString("nm_perawatan").toLowerCase().contains("double lumen")) {
                            isDoubleLumen = true;
                        }
                        if (rsrawatjalandr.getString("kategoriperawatan").contains("PROSEDUR") && rsrawatjalandr.getString("nm_perawatan").contains("SISTEM SINGLE USE")) {
                            isHD = true;
                        }

                        if (rsrawatjalandr.getString("nm_perawatan").toLowerCase().contains("eeg")) {
                            isEEG = true;
                        }
                    }

                    while (rsrawatjalandrpr.next()) {
                        totaljmtarif = totaljmtarif + rsrawatjalandrpr.getDouble("tarif_tindakanpr") + rsrawatjalandrpr.getDouble("tarif_tindakandr");
                        if (!rsrawatjalandrpr.getString("kategoriperawatan").contains("VISIT & KONSULTASI") && !rsrawatjalandrpr.getString("nm_perawatan").contains("SISTEM SINGLE USE")) {
                            isTindakan = true;
                        }
                        if (rsrawatjalandrpr.getString("kategoriperawatan").contains("PROSEDUR") && rsrawatjalandrpr.getString("nm_perawatan").contains("SISTEM SINGLE USE")) {
                            isHD = true;
                        }

                        if (rsrawatjalandrpr.getString("nm_perawatan").contains("(KRM)") || rsrawatjalandrpr.getString("nm_perawatan").contains("(OT)") || rsrawatjalandrpr.getString("nm_perawatan").contains("(Okupasi)")) {
                            isTerapi = true;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi RJ : " + e);
                } finally {
                    if (rsrawatjalandr != null) {
                        rsrawatjalandr.close();
                    }
                    if (psrawatjalandr != null) {
                        psrawatjalandr.close();
                    }
                    if (rsrawatjalandrpr != null) {
                        rsrawatjalandrpr.close();
                    }
                    if (psrawatjalandrpr != null) {
                        psrawatjalandrpr.close();
                    }
                }

                psbiayaoperator1 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator1,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where operasi.operator1=? and operasi.biayaoperator1>0 and operasi.no_rawat=? ");
                try {
                    psbiayaoperator1.setString(1, kodedokter);
                    psbiayaoperator1.setString(2, norawat);
                    rsbiayaoperator1 = psbiayaoperator1.executeQuery();

                    isAVShunt = false;
                    isCDL = false;
                    isESWL = false;
//
                    while (rsbiayaoperator1.next()) {

                        if (rsbiayaoperator1.getString("nm_perawatan").toLowerCase().contains("av shunt")) {
                            isAVShunt = true;
                        }

                        if (rsbiayaoperator1.getString("nm_perawatan").toLowerCase().contains("eswl")) {
                            isESWL = true;
                        }

                        if (rsbiayaoperator1.getString("nm_perawatan").toLowerCase().contains("double lumen")) {
                            isCDL = true;
                        }

                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Operasi : " + e);
                } finally {
                    if (rsbiayaoperator1 != null) {
                        rsbiayaoperator1.close();
                    }

                }
            }

            if (jenis.equals("ranap")) {
                //rawat inap
                psrawatinapdr = koneksi.prepareStatement(
                        "select pasien.nm_pasien,jns_perawatan_inap.nm_perawatan,rawat_inap_dr.tarif_tindakandr,"
                        + "rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,reg_periksa.kd_pj,rawat_inap_dr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_inap_dr on rawat_inap_dr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan_inap on rawat_inap_dr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where rawat_inap_dr.kd_dokter=? and rawat_inap_dr.tarif_tindakandr>0 and rawat_inap_dr.no_rawat=? ");
                psrawatinapdrpr = koneksi.prepareStatement(
                        "select pasien.nm_pasien,jns_perawatan_inap.nm_perawatan,rawat_inap_drpr.tarif_tindakandr,"
                        + "rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,reg_periksa.kd_pj,rawat_inap_drpr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_inap_drpr on rawat_inap_drpr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan_inap on rawat_inap_drpr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where rawat_inap_drpr.kd_dokter=? and rawat_inap_drpr.tarif_tindakandr>0 and rawat_inap_drpr.no_rawat=?");
                try {
                    psrawatinapdr.setString(1, kodedokter);
                    psrawatinapdr.setString(2, norawat);
                    rsrawatinapdr = psrawatinapdr.executeQuery();

                    psrawatinapdrpr.setString(1, kodedokter);
                    psrawatinapdrpr.setString(2, norawat);
                    rsrawatinapdrpr = psrawatinapdrpr.executeQuery();

                    while (rsrawatinapdr.next()) {

                    }

                    while (rsrawatinapdrpr.next()) {

                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Ranap : " + e);
                } finally {
                    if (rsrawatinapdr != null) {
                        rsrawatinapdr.close();
                    }
                    if (psrawatinapdr != null) {
                        psrawatinapdr.close();
                    }
                    if (rsrawatinapdrpr != null) {
                        rsrawatinapdrpr.close();
                    }
                    if (psrawatinapdrpr != null) {
                        psrawatinapdrpr.close();
                    }
                }

                psbiayaoperator1 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator1,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where operasi.operator1=? and operasi.biayaoperator1>0 and operasi.no_rawat=? ");
                psbiayaoperator2 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator2,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where operasi.operator2=? and operasi.biayaoperator2>0 and operasi.no_rawat=? ");
                psbiayaoperator3 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator3,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where operasi.operator3=? and operasi.biayaoperator3>0 and operasi.no_rawat=? ");
                psbiayadokter_anak = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayadokter_anak,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where operasi.dokter_anak=? and operasi.biayadokter_anak>0 and operasi.no_rawat=?");
                psbiaya_dokter_umum = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biaya_dokter_umum,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where operasi.dokter_umum=? and operasi.biaya_dokter_umum>0 and operasi.no_rawat=?");
                psbiaya_dokter_pjanak = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biaya_dokter_pjanak,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where operasi.dokter_pjanak=? and operasi.biaya_dokter_pjanak>0 and operasi.no_rawat=? ");
                try {
                    psbiayaoperator1.setString(1, kodedokter);
                    psbiayaoperator1.setString(2, norawat);
                    rsbiayaoperator1 = psbiayaoperator1.executeQuery();

// For psbiayaoperator2
                    psbiayaoperator2.setString(1, kodedokter);
                    psbiayaoperator2.setString(2, norawat);
                    rsbiayaoperator2 = psbiayaoperator2.executeQuery();

// For psbiayaoperator3
                    psbiayaoperator3.setString(1, kodedokter);
                    psbiayaoperator3.setString(2, norawat);
                    rsbiayaoperator3 = psbiayaoperator3.executeQuery();

// For psbiayadokter_anak
                    psbiayadokter_anak.setString(1, kodedokter);
                    psbiayadokter_anak.setString(2, norawat);
                    rsbiayadokter_anak = psbiayadokter_anak.executeQuery();

// For psbiaya_dokter_umum
                    psbiaya_dokter_umum.setString(1, kodedokter);
                    psbiaya_dokter_umum.setString(2, norawat);
                    rsbiaya_dokter_umum = psbiaya_dokter_umum.executeQuery();

// For psbiaya_dokter_pjanak
                    psbiaya_dokter_pjanak.setString(1, kodedokter);
                    psbiaya_dokter_pjanak.setString(2, norawat);
                    rsbiaya_dokter_pjanak = psbiaya_dokter_pjanak.executeQuery();

// For psbiayadokter_anestesi
                    psbiayadokter_anestesi.setString(1, kodedokter);
                    psbiayadokter_anestesi.setString(2, norawat);
                    rsbiayadokter_anestesi = psbiayadokter_anestesi.executeQuery();

                    while (rsbiayaoperator1.next()) {

                    }

                    while (rsbiayaoperator2.next()) {

                    }

                    while (rsbiayaoperator3.next()) {

                    }

                    while (rsbiayadokter_anak.next()) {

                    }

                    while (rsbiaya_dokter_pjanak.next()) {

                    }

                    while (rsbiaya_dokter_umum.next()) {

                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Operasi : " + e);
                } finally {
                    if (rsbiayaoperator1 != null) {
                        rsbiayaoperator1.close();
                    }
                    if (psbiayaoperator1 != null) {
                        psbiayaoperator1.close();
                    }
                    if (rsbiayaoperator2 != null) {
                        rsbiayaoperator2.close();
                    }
                    if (psbiayaoperator2 != null) {
                        psbiayaoperator2.close();
                    }
                    if (rsbiayaoperator3 != null) {
                        rsbiayaoperator3.close();
                    }
                    if (psbiayaoperator3 != null) {
                        psbiayaoperator3.close();
                    }
                    if (rsbiayadokter_anak != null) {
                        rsbiayadokter_anak.close();
                    }
                    if (psbiayadokter_anak != null) {
                        psbiayadokter_anak.close();
                    }
                    if (rsbiaya_dokter_umum != null) {
                        rsbiaya_dokter_umum.close();
                    }
                    if (psbiaya_dokter_umum != null) {
                        psbiaya_dokter_umum.close();
                    }
                    if (rsbiaya_dokter_pjanak != null) {
                        rsbiaya_dokter_pjanak.close();
                    }
                    if (psbiaya_dokter_pjanak != null) {
                        psbiaya_dokter_pjanak.close();
                    }

                }

            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private boolean isDpjpUtama(String kodedokter, String norawat) {
        isDPJP = false;
        if (Sequel.cariInteger("select count(kd_dokter) from dpjp_ranap where kd_dokter='" + kodedokter + "' and statusdpjp='Utama' and no_rawat='" + norawat + "'") > 0) {
            isDPJP = true;
        }

        return isDPJP;

    }

    private String isAnestesi(String kodedokter, String norawat) {
        String statusdokter = "DP";
        try {
            try {
                psbiayadokter_anestesi = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayadokter_anestesi,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where operasi.dokter_anestesi=? and operasi.biayadokter_anestesi>0 and operasi.no_rawat=?");

                psbiayadokter_anestesi.setString(1, kodedokter);
                psbiayadokter_anestesi.setString(2, norawat);
                rsbiayadokter_anestesi = psbiayadokter_anestesi.executeQuery();
                while (rsbiayadokter_anestesi.next()) {
                    statusdokter = "Anestesi";
                }
            } catch (Exception e) {

            } finally {
                if (rsbiayadokter_anestesi != null) {
                    rsbiayadokter_anestesi.close();
                }
                if (psbiayadokter_anestesi != null) {
                    psbiayadokter_anestesi.close();
                }
            }

        } catch (Exception e) {
        }

        return statusdokter;

    }

        
    private void exportToExcelActionPerformed(java.awt.event.ActionEvent evt) {
        int tabIdx = TabRawat.getSelectedIndex();
        DefaultTableModel tabMode = (tabIdx == 0 ? tabModeRawatJalan : tabModeRawatInap);
        JTable table = (tabIdx == 0 ? tbJMRawatJalan : tbJMRawatInap);
        String labelTab = (tabIdx == 0 ? "Rawat Jalan" : "Rawat Inap");

        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Maaf, data tabel " + labelTab + " masih kosong. Silakan cari/tampilkan data terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Export Excel JM Dokter BPJS (" + labelTab + ")");
        File defaultDir = new File(System.getProperty("user.home"), "Documents");
        if (!defaultDir.exists()) {
            defaultDir = new File(System.getProperty("user.home"), "Desktop");
        }
        if (defaultDir.exists()) {
            fileChooser.setCurrentDirectory(defaultDir);
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String defaultName = "JM_BPJS_" + (tabIdx == 0 ? "Rawat_Jalan" : "Rawat_Inap") + "_"
                + Valid.SetTgl(Periode1.getSelectedItem() + "") + "_sd_" + Valid.SetTgl(Period2.getSelectedItem() + "")
                + "_" + timestamp + ".xlsx";
        fileChooser.setSelectedFile(new File(defaultDir.exists() ? defaultDir : new File("."), defaultName));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();
        if (file == null) {
            return;
        }
        if (!file.getAbsolutePath().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }

        if (file.exists()) {
            try (FileOutputStream testFos = new FileOutputStream(file, true)) {
                // Berkas tidak terkunci oleh aplikasi lain
            } catch (Exception exLock) {
                JOptionPane.showMessageDialog(this,
                        "Berkas '" + file.getName() + "' sedang dibuka di Microsoft Excel!\nSilakan tutup terlebih dahulu atau simpan dengan nama berbeda.",
                        "Berkas Terkunci", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            Charset utf8 = StandardCharsets.UTF_8;

            zos.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n"
                    + "  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n"
                    + "  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n"
                    + "  <Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>\n"
                    + "  <Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>\n"
                    + "  <Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>\n"
                    + "</Types>").getBytes(utf8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("_rels/.rels"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n"
                    + "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>\n"
                    + "</Relationships>").getBytes(utf8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("xl/_rels/workbook.xml.rels"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n"
                    + "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>\n"
                    + "  <Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>\n"
                    + "</Relationships>").getBytes(utf8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("xl/workbook.xml"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">\n"
                    + "  <sheets>\n"
                    + "    <sheet name=\"" + (tabIdx == 0 ? "JM Rawat Jalan" : "JM Rawat Inap") + "\" sheetId=\"1\" r:id=\"rId1\"/>\n"
                    + "  </sheets>\n"
                    + "</workbook>").getBytes(utf8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("xl/styles.xml"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n"
                    + "  <numFmts count=\"1\">\n"
                    + "    <numFmt numFmtId=\"164\" formatCode=\"#,##0\"/>\n"
                    + "  </numFmts>\n"
                    + "  <fonts count=\"3\">\n"
                    + "    <font><name val=\"Tahoma\"/><sz val=\"10\"/></font>\n"
                    + "    <font><b/><color rgb=\"FFFFFFFF\"/><name val=\"Tahoma\"/><sz val=\"10\"/></font>\n"
                    + "    <font><b/><name val=\"Tahoma\"/><sz val=\"10\"/></font>\n"
                    + "  </fonts>\n"
                    + "  <fills count=\"3\">\n"
                    + "    <fill><patternFill patternType=\"none\"/></fill>\n"
                    + "    <fill><patternFill patternType=\"gray125\"/></fill>\n"
                    + "    <fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF2E8B57\"/></patternFill></fill>\n"
                    + "  </fills>\n"
                    + "  <borders count=\"2\">\n"
                    + "    <border><left/><right/><top/><bottom/><diagonal/></border>\n"
                    + "    <border><left style=\"thin\"/><right style=\"thin\"/><top style=\"thin\"/><bottom style=\"thin\"/><diagonal/></border>\n"
                    + "  </borders>\n"
                    + "  <cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>\n"
                    + "  <cellXfs count=\"6\">\n"
                    + "    <xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\"/>\n"
                    + "    <xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\"/>\n"
                    + "    <xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\"/>\n"
                    + "    <xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>\n"
                    + "    <xf numFmtId=\"164\" fontId=\"2\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyNumberFormat=\"1\"/>\n"
                    + "    <xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"/>\n"
                    + "  </cellXfs>\n"
                    + "</styleSheet>").getBytes(utf8));
            zos.closeEntry();

            // Tentukan kolom yang diekspor (skip kolom 0 'P', dan skip kolom tersembunyi dengan width == 0)
            List<Integer> exportCols = new ArrayList<>();
            for (int c = 1; c < tabMode.getColumnCount(); c++) {
                if (table.getColumnModel().getColumn(c).getWidth() > 0) {
                    exportCols.add(c);
                }
            }

            zos.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
            StringBuilder sb = new StringBuilder(65536);
            sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
            sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n");
            sb.append("  <sheetData>\n");

            // Baris Header
            sb.append("    <row r=\"1\">\n");
            for (int idx = 0; idx < exportCols.size(); idx++) {
                int c = exportCols.get(idx);
                String colRef = getExcelColumnName(idx) + "1";
                String headerText = escapeXml(tabMode.getColumnName(c));
                sb.append("      <c r=\"").append(colRef).append("\" s=\"1\" t=\"inlineStr\"><is><t>")
                  .append(headerText).append("</t></is></c>\n");
            }
            sb.append("    </row>\n");
            zos.write(sb.toString().getBytes(utf8));
            sb.setLength(0);

            // Baris Data & akumulasi total
            int rowCount = tabMode.getRowCount();
            double totalPiutang = 0;
            double totalTarifJM = 0;
            double totalUmbal = 0;
            double totalJMBPJS = 0;

            for (int r = 0; r < rowCount; r++) {
                int excelRow = r + 2;
                sb.append("    <row r=\"").append(excelRow).append("\">\n");
                for (int idx = 0; idx < exportCols.size(); idx++) {
                    int c = exportCols.get(idx);
                    Object val = tabMode.getValueAt(r, c);
                    String colRef = getExcelColumnName(idx) + excelRow;
                    boolean isMonetaryCol = (c == 9 || c == 10 || c == 11 || c == 19);

                    if (isMonetaryCol) {
                        double numVal = 0;
                        if (val instanceof Number) {
                            numVal = ((Number) val).doubleValue();
                        } else if (val != null) {
                            try {
                                numVal = Double.parseDouble(val.toString().trim());
                            } catch (Exception exNum) {
                                numVal = 0;
                            }
                        }

                        if (c == 9) totalPiutang += numVal;
                        if (c == 10) totalTarifJM += numVal;
                        if (c == 11) totalUmbal += numVal;
                        if (c == 19) totalJMBPJS += numVal;

                        sb.append("      <c r=\"").append(colRef).append("\" s=\"2\"><v>")
                          .append(numVal).append("</v></c>\n");
                    } else {
                        String text = val == null ? "" : escapeXml(val.toString());
                        sb.append("      <c r=\"").append(colRef).append("\" s=\"0\" t=\"inlineStr\"><is><t>")
                          .append(text).append("</t></is></c>\n");
                    }
                }
                sb.append("    </row>\n");
                if (sb.length() > 32768) {
                    zos.write(sb.toString().getBytes(utf8));
                    sb.setLength(0);
                }
            }

            // Total Summary Row di akhir tabel
            int totalRow = rowCount + 2;
            sb.append("    <row r=\"").append(totalRow).append("\">\n");
            for (int idx = 0; idx < exportCols.size(); idx++) {
                int c = exportCols.get(idx);
                String colRef = getExcelColumnName(idx) + totalRow;
                if (c == 8) { // Kolom Nama Dokter
                    sb.append("      <c r=\"").append(colRef).append("\" s=\"5\" t=\"inlineStr\"><is><t>TOTAL</t></is></c>\n");
                } else if (c == 9) { // Total Piutang
                    sb.append("      <c r=\"").append(colRef).append("\" s=\"4\"><v>").append(totalPiutang).append("</v></c>\n");
                } else if (c == 10) { // Total Tarif JM
                    sb.append("      <c r=\"").append(colRef).append("\" s=\"4\"><v>").append(totalTarifJM).append("</v></c>\n");
                } else if (c == 11) { // Umbal
                    sb.append("      <c r=\"").append(colRef).append("\" s=\"4\"><v>").append(totalUmbal).append("</v></c>\n");
                } else if (c == 19) { // JM BPJS
                    sb.append("      <c r=\"").append(colRef).append("\" s=\"4\"><v>").append(totalJMBPJS).append("</v></c>\n");
                } else {
                    sb.append("      <c r=\"").append(colRef).append("\" s=\"0\" t=\"inlineStr\"><is><t></t></is></c>\n");
                }
            }
            sb.append("    </row>\n");

            sb.append("  </sheetData>\n");
            sb.append("</worksheet>");
            zos.write(sb.toString().getBytes(utf8));
            zos.closeEntry();

            setCursor(Cursor.getDefaultCursor());
            int openChoice = JOptionPane.showConfirmDialog(this,
                    "Export Excel (" + labelTab + ") berhasil disimpan di:\n" + file.getAbsolutePath() + "\n\nApakah Anda ingin membuka file sekarang?",
                    "Export Berhasil", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
            if (openChoice == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception ex) {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(this, "Gagal mengekspor Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String getExcelColumnName(int index) {
        StringBuilder sb = new StringBuilder();
        int n = index;
        while (n >= 0) {
            sb.insert(0, (char) ('A' + (n % 26)));
            n = (n / 26) - 1;
        }
        return sb.toString();
    }

    private static String escapeXml(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        StringBuilder out = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (ch) {
                case '<': out.append("&lt;"); break;
                case '>': out.append("&gt;"); break;
                case '&': out.append("&amp;"); break;
                case '"': out.append("&quot;"); break;
                case '\'': out.append("&apos;"); break;
                default:
                    if (ch < 0x20 && ch != '\t' && ch != '\n' && ch != '\r') {
                        // ignore control characters
                    } else {
                        out.append(ch);
                    }
                    break;
            }
        }
        return out.toString();
    }

    public void muatNotaBilling(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) {
            return;
        }
        String host = koneksiDB.HOSTHYBRIDWEB();
        String port = koneksiDB.PORTWEB();
        String hybrid = koneksiDB.HYBRIDWEB();
        if (host == null || host.trim().isEmpty()) {
            return;
        }
        if (port == null || port.trim().isEmpty()) {
            port = "80";
        }
        if (hybrid == null || hybrid.trim().isEmpty()) {
            hybrid = "webapps";
        }
        String url = "http://" + host + ":" + port + "/" + hybrid + "/berkasrawat/pages/billingtampil.php?norawat=" + noRawat.trim();
        tampilInvoice(url);
    }

public void tampilInvoice(String urlditerima) {
        if (urlditerima == null || urlditerima.trim().isEmpty()) {
            return;
        }
        String url = urlditerima;
        isWebViewReady = false; // Reset ready state

        Platform.runLater(() -> {
            WebView view = new WebView();
            currentWebView = view; // Store reference for PDF conversion
            engine = view.getEngine();
            engine.setJavaScriptEnabled(true);

            engine.setCreatePopupHandler(new Callback<PopupFeatures, WebEngine>() {
                @Override
                public WebEngine call(PopupFeatures p) {
                    Stage stage = new Stage(StageStyle.TRANSPARENT);
                    return view.getEngine();
                }
            });

            engine.getLoadWorker().exceptionProperty().addListener(
                    (ObservableValue<? extends Throwable> o, Throwable old, final Throwable value) -> {
                        if (engine.getLoadWorker().getState() == FAILED) {
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(panelInvoices,
                                        (value != null)
                                                ? engine.getLocation() + "\n" + value.getMessage()
                                                : engine.getLocation() + "\nUnexpected Error.",
                                        "Loading Error...",
                                        JOptionPane.ERROR_MESSAGE);
                            });
                            isWebViewReady = false;
                        }
                    });

            // IMPORTANT: Add state listener to track when WebView is ready
            engine.getLoadWorker().stateProperty().addListener(
                    (ObservableValue<? extends Worker.State> observable,
                            Worker.State oldValue, Worker.State newValue) -> {
                        if (newValue == Worker.State.SUCCEEDED) {
                            // Wait a bit for dynamic content to load
                            Platform.runLater(() -> {
                                Timer timer = new Timer(2000, e -> {
                                    isWebViewReady = true;
                                    // System.out.println("WebView is ready for PDF conversion");
                                    // Optional: Enable your PDF button here
                                    SwingUtilities.invokeLater(() -> {
                                        // If you have a PDF button, enable it here
                                        // pdfButton.setEnabled(true);
                                    });
                                });
                                timer.setRepeats(false);
                                timer.start();
                            });
                        } else if (newValue == Worker.State.RUNNING) {
                            isWebViewReady = false;
                            // System.out.println("WebView is loading...");
                            // Optional: Disable PDF button while loading
                            SwingUtilities.invokeLater(() -> {
                                // pdfButton.setEnabled(false);
                            });
                        }
                    });

            jfxinvoices.setScene(new Scene(view));

            try {
                engine.load(url);
            } catch (Exception exception) {
                System.err.println("Error loading URL: " + exception.getMessage());
                engine.load(url); // Retry once
                isWebViewReady = false;
            }
        });

        panelInvoices.add(jfxinvoices, BorderLayout.CENTER);
        panelInvoices.revalidate();
        panelInvoices.repaint();
    }

    public double calculateDoctorFee(String dpjpAnestesi, String tindakan, String status, String jenis,
            int countTindakan, double umbal) {
        if ("Anestesi".equalsIgnoreCase(dpjpAnestesi)) {
            return umbal * kalkulator.getPersenAnestesiRanap();
        }

        try {
            double percentage = Double.parseDouble(jenis) / 100.0;
            return umbal * percentage;
        } catch (Exception e) {
            return 0.0;
        }
    }

//    public double calculateDoctorFee(String dpjpAnestesi, String tindakan, String status, String jenis,
//            int countTindakan, double umbal) {
//        // Determine which visit column to use based on countTindakan
//        String visitColumn;
//        if (countTindakan > 1) {
//            visitColumn = "visit>1";
//        } else if (countTindakan == 1) {
//            visitColumn = "visit=1";
//        } else {
//            visitColumn = "visit0";
//        }
//
//        // Initialize percentage
//        double percentage = 0.0;
//
//        // Special case for Anestesi
//        if ("Anestesi".equalsIgnoreCase(dpjpAnestesi)) {
//            return umbal * kalkulator.getPersenAnestesiRanap(); // Porsi Anestesi dinamis dari aturan
//        }
//
//        // Decision tree logic
//        if ("DPJP".equals(dpjpAnestesi)) {
//            if ("OP".equals(tindakan)) {
//                if ("Sendiri".equals(status)) {
//                    if ("Bedah Dengan Tindakan".equals(jenis)) {
//                        percentage = getPercentageForVisit(countTindakan, 0.2, 0.18, 0.16);
//                    }
//                } else if ("Raber".equals(status)) {
//                    if ("Bedah Tanpa Tindakan".equals(jenis)) {
//                        percentage = 0.17;
//                    } else if ("Bedah Dengan Tindakan".equals(jenis)) {
//                        percentage = 0.1;
//                    } else if ("Bedah dengan Tindakan dengan Dokter lain".equals(jenis)) {
//                        percentage = 0.09;
//                    } else if ("Non Bedah 1x Visit".equals(jenis)) {
//                        percentage = getPercentageForVisit(countTindakan, 0.19, 0.18, 0.16);
//                    } else if ("Non Bedah Raber".equals(jenis)) {
//                        percentage = getPercentageForVisit(countTindakan, 0.17, 0.16, 0.15);
//                    } else if ("Non Bedah Multi".equals(jenis)) {
//                        percentage = getPercentageForVisit(countTindakan, 0.17, 0.16, 0.15);
//                    }
//                }
//            } else if ("NON-OP".equals(tindakan)) {
//                if ("Sendiri".equals(status)) {
//                    percentage = getPercentageForVisit(countTindakan, 0.09, 0.06, 0.04);
//                } else if ("Raber".equals(status)) {
//                    if ("-".equals(jenis) || jenis.isEmpty()) {
//                        percentage = getPercentageForVisit(countTindakan, 0.08, 0.06, 0.04);
//                    } else if ("1 Dr Raber".equals(jenis)) {
//                        percentage = getPercentageForVisit(countTindakan, 0.06, 0.05, 0.04);
//                    } else if ("Multi".equals(jenis)) {
//                        percentage = getPercentageForVisit(countTindakan, 0.06, 0.05, 0.04);
//                    }
//                }
//            }
//        } else if ("Pendamping".equals(dpjpAnestesi)) {
//            if ("OP".equals(tindakan)) {
//                if ("Raber".equals(status)) {
//                    if ("Bedah Tanpa Tindakan".equals(jenis)) {
//                        percentage = 0.03;
//                    } else if ("Bedah Dengan Tindakan".equals(jenis)) {
//                        percentage = 0.1;
//                    } else if ("Bedah dengan Tindakan dengan Dokter lain".equals(jenis)) {
//                        percentage = 0.09;
//                    } else if ("Dr Lain, Bedah + Tindakan".equals(jenis)) {
//                        percentage = 0.03; // Assuming 3% from the Excel (3% '@)
//                    }
//                }
//            } else if ("NON-OP".equals(tindakan)) {
//                if ("Raber".equals(status)) {
//                    if ("1x Visit".equals(jenis)) {
//                        percentage = getPercentageForVisit(countTindakan, 0.02, 0.02, 0.02);
//                    } else if ("-".equals(jenis) || jenis.isEmpty()) {
//                        percentage = getPercentageForVisit(countTindakan, 0.04, 0.03, 0.02);
//                    }
//                } else if ("Multi".equals(status)) {
//                    percentage = 0.04;
//                }
//            }
//        }
//
//        // Calculate final fee
//        return umbal * percentage;
//    }
// Helper method to get percentage based on visit count
    private double getPercentageForVisit(int countTindakan, double gt1, double eq1, double eq0) {
        if (countTindakan > 1) {
            return gt1;
        } else if (countTindakan == 1) {
            return eq1;
        } else {
            return eq0;
        }
    }

    /**
     * Recalculates jm_bpjs for a specific row when table properties change
     * Critical for real-time healthcare billing calculations
     */
    private void recalculateJmBpjs(int row) {
        // Prevent recursive calls
        if (isRecalculating) {
            return;
        }

        try {
            isRecalculating = true;

            // Validate row bounds
            if (row < 0 || row >= tabModeRawatInap.getRowCount()) {
                return;
            }

            // Safely get values with null checks
            Object statusdokterObj = tabModeRawatInap.getValueAt(row, 14);
            Object statusOperasiObj = tabModeRawatInap.getValueAt(row, 15);
            Object statusTextObj = tabModeRawatInap.getValueAt(row, 16);
            Object jenisTextObj = tabModeRawatInap.getValueAt(row, 17);
            Object countTindakanObj = tabModeRawatInap.getValueAt(row, 18);
            Object umbalObj = tabModeRawatInap.getValueAt(row, 11);

            // Convert to strings with null safety
            String statusdokter = statusdokterObj != null ? statusdokterObj.toString() : "";
            String statusOperasi = statusOperasiObj != null ? statusOperasiObj.toString() : "";
            String statusText = statusTextObj != null ? statusTextObj.toString() : "";
            String jenisText = jenisTextObj != null ? jenisTextObj.toString() : "";

            // Parse numeric values safely
            int countTindakan = 0;
            if (countTindakanObj != null) {
                String countStr = countTindakanObj.toString().replaceAll("[^0-9]", "");
                if (!countStr.isEmpty()) {
                    countTindakan = Integer.parseInt(countStr);
                }
            }

            double umbal = 0.0;
            if (umbalObj != null) {
                try {
                    umbal = Double.parseDouble(umbalObj.toString().replace(",", ""));
                } catch (NumberFormatException e) {
                    umbal = 0.0;
                }
            }

            // Calculate new jm_bpjs
            double newJmBpjs = calculateDoctorFee(
                    statusdokter,
                    statusOperasi,
                    statusText,
                    jenisText,
                    countTindakan,
                    umbal
            );

            // Update column 17 (JM BPJS)
            tabModeRawatInap.setValueAt(newJmBpjs, row, 19);

            // Handle anesthesia fee adjustment
            handleAnesthesiaFeeAdjustment(row, statusdokter, newJmBpjs);

        } catch (Exception e) {
            System.err.println("Error recalculating JM BPJS for row " + row + ": " + e.getMessage());
            e.printStackTrace();
        } finally {
            isRecalculating = false;
        }
    }

    /**
     * Handles anesthesia fee adjustment when DPJP fees change Maintains 40%
     * relationship for anesthesia doctors
     */
    private void handleAnesthesiaFeeAdjustment(int changedRow, String statusdokter, double newJmBpjs) {
        try {
            if ("DPJP".equalsIgnoreCase(statusdokter)) {
                String noRawat = tabModeRawatInap.getValueAt(changedRow, 4).toString();
                String noSep = tabModeRawatInap.getValueAt(changedRow, 3).toString();

                for (int i = 0; i < tabModeRawatInap.getRowCount(); i++) {
                    if (i != changedRow) {
                        String currentNoRawat = tabModeRawatInap.getValueAt(i, 4).toString();
                        String currentNoSep = tabModeRawatInap.getValueAt(i, 3).toString();
                        String currentStatusDokter = tabModeRawatInap.getValueAt(i, 14).toString();

                        boolean match = (!noSep.isEmpty() && noSep.equals(currentNoSep)) || noRawat.equals(currentNoRawat);
                        if (match && "Anestesi".equalsIgnoreCase(currentStatusDokter)) {
                            double anesthesiaFee = newJmBpjs * kalkulator.getPersenAnestesiRanap();
                            tabModeRawatInap.setValueAt(anesthesiaFee, i, 19);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log silently
        }
    }

    private DlgBilingRanap getBilranap() {
        // 2. Check if the object has been created yet.
        if (bilranap == null) {
            // 3. If not, create it now.
            bilranap = new DlgBilingRanap(null, false);
        }
        // 4. Return the instance.
        return bilranap;
    }

    private KeuanganCariBayarJMDokter getCariBayarJMDokter() {
        // 2. Check if the object has been created yet.
        if (form == null) {
            // 3. If not, create it now.
            form = new KeuanganCariBayarJMDokter(null, false);

            form.dokter.addWindowListener(new WindowListener() {
                @Override
                public void windowOpened(WindowEvent e) {
                }

                @Override
                public void windowClosing(WindowEvent e) {
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    if (akses.getform().equals("KeuanganBayarJMDokter")) {
                        if (form.dokter.getTable().getSelectedRow() != -1) {
                            kddokter.setText(form.dokter.getTable().getValueAt(form.dokter.getTable().getSelectedRow(), 0).toString());
                            nmdokter.setText(form.dokter.getTable().getValueAt(form.dokter.getTable().getSelectedRow(), 1).toString());
                        }
                        kddokter.requestFocus();
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
                    form.dokter.emptTeks();
                }

                @Override
                public void windowDeactivated(WindowEvent e) {
                }
            });
        }
        // 4. Return the instance.
        return form;
    }

}
