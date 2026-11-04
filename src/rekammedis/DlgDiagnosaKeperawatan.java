/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * DlgPerawatan.java
 *
 * Created on May 23, 2010, 6:36:30 PM
 */
package rekammedis;

import fungsi.WarnaTable;
import fungsi.WarnaTableSDKI;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.validasi;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import java.awt.Color;
import java.util.Set;
import java.util.HashSet;
import fungsi.sekuel;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;

/**
 *
 * @author dosen
 */
public final class DlgDiagnosaKeperawatan extends javax.swing.JDialog {

    private final DefaultTableModel tabMode, tabModeIntervensi, tabModeDiagKep, tabModeEvaluasi, tabModeDetailMasalah, tabModeDK;
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private Connection koneksi = koneksiDB.condb();
    private DlgCariDokter dokter = new DlgCariDokter(null, false);
    private PreparedStatement ps, pstindakan;
    private ResultSet rs, rstindakan;
//    private String[] kode, nama, pemeriksaan2, satuan2, nilai_rujukan2, idtemplate2;
    private int stick = 0, jml = 0, i = 0, jmldiagkep = 0, jmlintervensi = 0, jmlevaluasi = 0;
    private String aktifkanparsial = "no", tampilvip = "", tampileksekutif = "", norawatibu = "", kelas = "", kamar, namakamar, cara_bayar_lab = "Yes", kelas_lab = "Yes", status = "", la = "", ld = "", pa = "", pd = "", finger = "";
    private boolean sukses = true;
    private String[] pilih, kodemasalah, namamasalah, definisi;
    public static String dataTerpilih = "";
    public static String jenisData = "";
    private javax.swing.JTextField targetTextField = null;
    private javax.swing.JTextArea targetTextArea = null;

    // ── KOMPONEN FITUR FILTER STATUS MASALAH (TAHAP 4) ────────────────────────
    private javax.swing.table.TableRowSorter<DefaultTableModel> sorterDetailMasalah;
    private widget.ComboBox cmbFilterStatus;
    private widget.Label lblCountStatus;
    private javax.swing.JPopupMenu PopupFilterStatus;
    private javax.swing.JRadioButtonMenuItem menuFilterSemua;
    private javax.swing.JRadioButtonMenuItem menuFilterTeratasi;
    private javax.swing.JRadioButtonMenuItem menuFilterBelumTeratasi;
    private javax.swing.ButtonGroup btnGroupFilter;
    private boolean isUpdatingFilter = false;
    private boolean isMemuatDiagKep = false;
    private boolean isAutoSavingDiagKep = false;

    // ── KOMPONEN KATEGORI INPUT MASALAH (PENDEKATAN B) ────────────────────────
    private widget.RadioButton rbRanap;
    private widget.RadioButton rbRalan;
    private javax.swing.ButtonGroup bgKategoriInput;

    public interface DataCallback {

        void onDataSelected(String data, String jenisData);
    }
    private DataCallback callback;

    private String dataHasil = "";

    public String getDataHasil() {
        return dataHasil;
    }

    // Method untuk set data hasil (dipanggil dari BtnTambah1)
    public void setDataHasil(String data) {
        this.dataHasil = data;
    }

    /**
     * Creates new form DlgPerawatan
     *
     * @param parent
     * @param modal
     */
    public DlgDiagnosaKeperawatan(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.callback = null;
        this.setLocation(8, 1);
        setSize(885, 674);
        this.targetTextField = null;
        this.targetTextArea = null;

        Object[] row = {
            "P", "Pemeriksaan", "Satuan", "Nilai Rujukan", "id_template"};
        tabMode = new DefaultTableModel(null, row) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                boolean a = false;
                if (colIndex == 0) {
                    a = true;
                }
                return a;
            }
            Class[] types = new Class[]{
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tabModeDetailMasalah = new DefaultTableModel(null, new Object[]{
            "Kode", "Masalah Keperawatan", "Lama Evaluasi", "Status", "Nrs. Penegak", "Tgl. Penegakan", "Nrs. Evaluasi", "Tgl. Teratasi"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return colIndex == 2 || colIndex == 3;
            }

            Class[] types = new Class[]{
                java.lang.Object.class, // Kode
                java.lang.Object.class, // Masalah Keperawatan
                java.lang.Object.class, // Waktu eval
                java.lang.Object.class, // Keterangan
                java.lang.Object.class, // NIP
                java.lang.Object.class, //tgl input
                java.lang.Object.class,
                java.lang.Object.class // tgl eval
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbMasalahDetailMasalah.setModel(tabModeDetailMasalah);
        // Komit otomatis nilai sel (Lama Evaluasi & Status) saat fokus berpindah tanpa wajib tekan Enter
        tbMasalahDetailMasalah.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        tbMasalahDetailMasalah.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbMasalahDetailMasalah.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 8; i++) {
            TableColumn column = tbMasalahDetailMasalah.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(50);
            } else if (i == 1) {
                column.setPreferredWidth(250);
            } else if (i == 2) {
                column.setPreferredWidth(100);
            } else if (i == 3) {
                column.setPreferredWidth(180);
            } else if (i == 4) {
                column.setPreferredWidth(70);
            } else if (i == 5) {
                column.setPreferredWidth(100);
            } else if (i == 6) {
                column.setPreferredWidth(100);
            } else if (i == 7) {
                column.setPreferredWidth(100);
            }
        }
        tbMasalahDetailMasalah.setDefaultRenderer(Object.class, new WarnaTable());

        // Tab DIAGNOSA
        tabModeDiagKep = new DefaultTableModel(null, new Object[]{
            "P", "Kode", "DIAGNOSA KEPERAWATAN", "Kategori", "Entitas", "Sub-Entitas", "DIAGNOSA KEPERAWATAN", "Kd Rencana"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                boolean a = false;
                if (colIndex == 0 || colIndex == 6) {
                    a = true;
                }
                return a;
            }
            Class[] types = new Class[]{
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbDiagKep.setModel(tabModeDiagKep);
        tbDiagKep.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbDiagKep.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbDiagKep.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 8; i++) {
            TableColumn column = tbDiagKep.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(20);
            } else if (i == 1) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 2) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 3) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 4) {
                column.setPreferredWidth(140);
            } else if (i == 5) {
                column.setPreferredWidth(140);
            } else if (i == 6) {
                column.setPreferredWidth(240);
            } else if (i == 7) {
                column.setPreferredWidth(80);
            }
        }
        tbDiagKep.getTableHeader().setForeground(new Color(255, 252, 252));
        tbDiagKep.getTableHeader().setBackground(new Color(170, 00, 0));
        tbDiagKep.setDefaultRenderer(Object.class, new WarnaTableSDKI());

        // Single-click inplace editor untuk kolom diagnosa keperawatan SDKI (kolom 6)
        javax.swing.JTextField txtEditItemSDKI = new javax.swing.JTextField();
        txtEditItemSDKI.setFont(new java.awt.Font("Tahoma", java.awt.Font.PLAIN, 11));
        javax.swing.DefaultCellEditor cellEditorSDKI = new javax.swing.DefaultCellEditor(txtEditItemSDKI);
        cellEditorSDKI.setClickCountToStart(1);
        tbDiagKep.getColumnModel().getColumn(6).setCellEditor(cellEditorSDKI);

        // Listener auto-save saat kolom diagnosa keperawatan (kolom 6) selesai diedit
        tabModeDiagKep.addTableModelListener(new javax.swing.event.TableModelListener() {
            @Override
            public void tableChanged(javax.swing.event.TableModelEvent e) {
                if (e.getType() == javax.swing.event.TableModelEvent.UPDATE && e.getColumn() == 6) {
                    if (!isMemuatDiagKep && !isAutoSavingDiagKep) {
                        int row = e.getFirstRow();
                        autoSaveDiagnosaKepItem(row);
                    }
                }
            }
        });
// Tab INTERVENSI
        tabModeIntervensi = new DefaultTableModel(null, new Object[]{
            "P", "Kode", "TUJUAN KEPERAWATAN (SLKI)", "Kategori", "Entitas", "Sub-Entitas", "TUJUAN KEPERAWATAN (SLKI)", "Kd Rencana"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                boolean a = false;
                if (colIndex == 0) {
                    a = true;
                }
                return a;
            }
            Class[] types = new Class[]{
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbIntervensi.setModel(tabModeIntervensi);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbIntervensi.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbIntervensi.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 8; i++) {
            TableColumn column = tbIntervensi.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(20);
            } else if (i == 1) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 2) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 3) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 4) {
                column.setPreferredWidth(140);
            } else if (i == 5) {
                column.setPreferredWidth(140);
            } else if (i == 6) {
                column.setPreferredWidth(240);
            } else if (i == 7) {
                column.setPreferredWidth(90);
            }
        }
        tbIntervensi.getTableHeader().setForeground(new Color(255, 252, 252));
        tbIntervensi.getTableHeader().setBackground(new Color(170, 00, 0));
        tbIntervensi.setDefaultRenderer(Object.class, new WarnaTableSDKI());

        // Tab EVALUASI
        tabModeEvaluasi = new DefaultTableModel(null, new Object[]{
            "P", "Kode", "RENCANA TINDAKAN", "Kategori", "Entitas", "Sub-Entitas", "RENCANA TINDAKAN (SIKI)", "Kd Rencana"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                boolean a = false;
                if (colIndex == 0) {
                    a = true;
                }
                return a;
            }
            Class[] types = new Class[]{
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbEvaluasi.setModel(tabModeEvaluasi);

        tbEvaluasi.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbEvaluasi.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 8; i++) {
            TableColumn column = tbEvaluasi.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(20);
            } else if (i == 1) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 2) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 3) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 4) {
                column.setPreferredWidth(140);
            } else if (i == 5) {
                column.setPreferredWidth(140);
            } else if (i == 6) {
                column.setPreferredWidth(240);
            } else if (i == 7) {
                column.setPreferredWidth(80);
            }
        }
        tbEvaluasi.getTableHeader().setForeground(new Color(255, 252, 252));
        tbEvaluasi.getTableHeader().setBackground(new Color(170, 00, 0));
        tbEvaluasi.setDefaultRenderer(Object.class, new WarnaTableSDKI());

        tabModeDK = new DefaultTableModel(null, new Object[]{
            "P", "Kode", "Nama Masalah", "Definisi"}) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                boolean a = false;
                if (colIndex == 0) {
                    a = true;
                }
                return a;
            }
            Class[] types = new Class[]{
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        tbMasalah.setModel(tabModeDK);
        //tbPenyakit.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbPenyakit.getBackground()));
        tbMasalah.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbMasalah.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < 4; i++) {
            TableColumn column = tbMasalah.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(30);
            } else if (i == 1) {
                column.setPreferredWidth(50);
            } else if (i == 2) {
                column.setPreferredWidth(250);
            } else if (i == 3) {
                column.setPreferredWidth(800);
            }
        }
        tbMasalah.setDefaultRenderer(Object.class, new WarnaTable());

        TNoRw.setDocument(new batasInput((byte) 17).getKata(TNoRw));

        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCariSkala1.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (TCariSkala1.getText().length() > 2) {
                        tampil();
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (TCariSkala1.getText().length() > 2) {
                        tampil();
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (TCariSkala1.getText().length() > 2) {
                        tampil();
                    }
                }
            });

        }
//        ChkJln.setSelected(true);

        dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (dokter.getTable().getSelectedRow() != -1) {

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

        // ── INISIALISASI FITUR FILTER STATUS MASALAH (TAHAP 4) ──
        initFilterStatus();
        initKategoriInput();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Penjab = new widget.TextBox();
        buttonGroup1 = new javax.swing.ButtonGroup();
        Popup = new javax.swing.JPopupMenu();
        ppBersihkan = new javax.swing.JMenuItem();
        ppSemua = new javax.swing.JMenuItem();
        Jk = new widget.TextBox();
        Umur = new widget.TextBox();
        Alamat = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        internalFrame2 = new widget.InternalFrame();
        FormInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        PanelInput = new widget.PanelBiasa();
        jLabel3 = new widget.Label();
        TNoRw = new widget.TextBox();
        TNoRM = new widget.TextBox();
        TPasien = new widget.TextBox();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        jLabel11 = new widget.Label();
        Jk1 = new widget.TextBox();
        TabRawat = new javax.swing.JTabbedPane();
        PanelCariUtama = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        Scroll17 = new widget.ScrollPane();
        tbMasalahDetailMasalah = new widget.Table();
        jPanel5 = new javax.swing.JPanel();
        panelisi6 = new widget.panelisi();
        label35 = new widget.Label();
        TCariSkala1 = new widget.TextBox();
        BtnCariSkala1 = new widget.Button();
        BtnTambahSkala1 = new widget.Button();
        Tab1dan2dan3 = new javax.swing.JTabbedPane();
        Scroll3 = new widget.ScrollPane();
        tbDiagKep = new widget.Table();
        Scroll18 = new widget.ScrollPane();
        tbIntervensi = new widget.Table();
        Scroll5 = new widget.ScrollPane();
        tbEvaluasi = new widget.Table();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        Scroll = new widget.ScrollPane();
        tbMasalah = new widget.Table();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnEdit = new widget.Button();
        BtnHapus = new widget.Button();
        BtnTambah1 = new widget.Button();
        BtnKeluar = new widget.Button();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel10 = new widget.Label();

        Penjab.setEditable(false);
        Penjab.setFocusTraversalPolicyProvider(true);
        Penjab.setName("Penjab"); // NOI18N
        Penjab.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                PenjabKeyPressed(evt);
            }
        });

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

        Jk.setEditable(false);
        Jk.setFocusTraversalPolicyProvider(true);
        Jk.setName("Jk"); // NOI18N

        Umur.setEditable(false);
        Umur.setHighlighter(null);
        Umur.setName("Umur"); // NOI18N

        Alamat.setEditable(false);
        Alamat.setHighlighter(null);
        Alamat.setName("Alamat"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Diagnosa Keperawatan ]::", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        internalFrame2.setBorder(null);
        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setOpaque(false);
        FormInput.setPreferredSize(new java.awt.Dimension(560, 100));
        FormInput.setLayout(new java.awt.BorderLayout(1, 1));

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('I');
        ChkInput.setSelected(true);
        ChkInput.setText(".: Input Data");
        ChkInput.setToolTipText("Alt+I");
        ChkInput.setBorderPainted(true);
        ChkInput.setBorderPaintedFlat(true);
        ChkInput.setFocusable(false);
        ChkInput.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkInput.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkInput.setName("ChkInput"); // NOI18N
        ChkInput.setPreferredSize(new java.awt.Dimension(192, 20));
        ChkInput.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkInputActionPerformed(evt);
            }
        });
        FormInput.add(ChkInput, java.awt.BorderLayout.PAGE_END);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setPreferredSize(new java.awt.Dimension(360, 200));
        PanelInput.setLayout(null);

        jLabel3.setText("No.Rawat :");
        jLabel3.setName("jLabel3"); // NOI18N
        PanelInput.add(jLabel3);
        jLabel3.setBounds(0, 10, 92, 23);

        TNoRw.setEditable(false);
        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        PanelInput.add(TNoRw);
        TNoRw.setBounds(95, 10, 128, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        PanelInput.add(TNoRM);
        TNoRM.setBounds(225, 10, 105, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        PanelInput.add(TPasien);
        TPasien.setBounds(332, 10, 440, 23);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        PanelInput.add(jLabel8);
        jLabel8.setBounds(40, 40, 60, 23);

        TglLahir.setEditable(false);
        TglLahir.setName("TglLahir"); // NOI18N
        PanelInput.add(TglLahir);
        TglLahir.setBounds(100, 40, 120, 23);

        jLabel11.setText("J.K. :");
        jLabel11.setName("jLabel11"); // NOI18N
        PanelInput.add(jLabel11);
        jLabel11.setBounds(220, 40, 30, 23);

        Jk1.setEditable(false);
        Jk1.setName("Jk1"); // NOI18N
        Jk1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Jk1ActionPerformed(evt);
            }
        });
        PanelInput.add(Jk1);
        Jk1.setBounds(250, 40, 80, 23);

        FormInput.add(PanelInput, java.awt.BorderLayout.CENTER);

        internalFrame2.add(FormInput, java.awt.BorderLayout.PAGE_START);

        TabRawat.setBackground(new java.awt.Color(255, 255, 253));
        TabRawat.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 102), 1, true));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        PanelCariUtama.setName("PanelCariUtama"); // NOI18N
        PanelCariUtama.setOpaque(false);
        PanelCariUtama.setPreferredSize(new java.awt.Dimension(100, 143));
        PanelCariUtama.setLayout(new java.awt.BorderLayout());

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(816, 102));
        jPanel1.setLayout(new java.awt.GridLayout(1, 2));

        Scroll17.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 254)));
        Scroll17.setName("Scroll17"); // NOI18N
        Scroll17.setOpaque(true);
        Scroll17.setPreferredSize(new java.awt.Dimension(318, 42));

        tbMasalahDetailMasalah.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tbMasalahDetailMasalah.setName("tbMasalahDetailMasalah"); // NOI18N
        tbMasalahDetailMasalah.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbMasalahDetailMasalahMouseClicked(evt);
            }
        });
        tbMasalahDetailMasalah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbMasalahDetailMasalahKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbMasalahDetailMasalahKeyReleased(evt);
            }
        });
        Scroll17.setViewportView(tbMasalahDetailMasalah);

        jPanel1.add(Scroll17);

        jPanel5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jPanel5.setName("jPanel5"); // NOI18N
        jPanel5.setOpaque(false);
        jPanel5.setPreferredSize(new java.awt.Dimension(295, 102));
        jPanel5.setLayout(new java.awt.BorderLayout(1, 1));

        panelisi6.setBorder(null);
        panelisi6.setName("panelisi6"); // NOI18N
        panelisi6.setPreferredSize(new java.awt.Dimension(100, 43));
        panelisi6.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        label35.setForeground(new java.awt.Color(170, 0, 0));
        label35.setText("Key Word :");
        label35.setName("label35"); // NOI18N
        label35.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi6.add(label35);

        TCariSkala1.setForeground(new java.awt.Color(170, 0, 0));
        TCariSkala1.setToolTipText("Alt+C");
        TCariSkala1.setName("TCariSkala1"); // NOI18N
        TCariSkala1.setPreferredSize(new java.awt.Dimension(200, 23));
        TCariSkala1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariSkala1KeyPressed(evt);
            }
        });
        panelisi6.add(TCariSkala1);

        BtnCariSkala1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCariSkala1.setMnemonic('1');
        BtnCariSkala1.setToolTipText("Alt+1");
        BtnCariSkala1.setName("BtnCariSkala1"); // NOI18N
        BtnCariSkala1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCariSkala1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariSkala1ActionPerformed(evt);
            }
        });
        BtnCariSkala1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariSkala1KeyPressed(evt);
            }
        });
        panelisi6.add(BtnCariSkala1);

        BtnTambahSkala1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/plus_16.png"))); // NOI18N
        BtnTambahSkala1.setMnemonic('3');
        BtnTambahSkala1.setToolTipText("Alt+3");
        BtnTambahSkala1.setName("BtnTambahSkala1"); // NOI18N
        BtnTambahSkala1.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnTambahSkala1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambahSkala1ActionPerformed(evt);
            }
        });
        panelisi6.add(BtnTambahSkala1);

        jPanel5.add(panelisi6, java.awt.BorderLayout.PAGE_END);

        Tab1dan2dan3.setBackground(new java.awt.Color(255, 255, 254));
        Tab1dan2dan3.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Tab1dan2dan3.setForeground(new java.awt.Color(50, 50, 50));
        Tab1dan2dan3.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        Tab1dan2dan3.setName("Tab1dan2dan3"); // NOI18N
        Tab1dan2dan3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Tab1dan2dan3MouseClicked(evt);
            }
        });

        Scroll3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        Scroll3.setForeground(new java.awt.Color(170, 0, 0));
        Scroll3.setName("Scroll3"); // NOI18N
        Scroll3.setOpaque(true);
        Scroll3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                Scroll3KeyReleased(evt);
            }
        });

        tbDiagKep.setForeground(new java.awt.Color(170, 0, 0));
        tbDiagKep.setName("tbDiagKep"); // NOI18N
        tbDiagKep.setSelectionForeground(new java.awt.Color(0, 255, 0));
        Scroll3.setViewportView(tbDiagKep);

        Tab1dan2dan3.addTab("DIAGNOSIS KEPERAWATAN (SDKI)", Scroll3);

        Scroll18.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        Scroll18.setName("Scroll18"); // NOI18N
        Scroll18.setOpaque(true);

        tbIntervensi.setForeground(new java.awt.Color(255, 0, 0));
        tbIntervensi.setName("tbIntervensi"); // NOI18N
        tbIntervensi.setSelectionForeground(new java.awt.Color(0, 255, 0));
        Scroll18.setViewportView(tbIntervensi);

        Tab1dan2dan3.addTab("TUJUAN KEPERAWATAN (SLKI)", Scroll18);

        Scroll5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(255, 255, 253)));
        Scroll5.setName("Scroll5"); // NOI18N
        Scroll5.setOpaque(true);

        tbEvaluasi.setForeground(new java.awt.Color(255, 0, 0));
        tbEvaluasi.setName("tbEvaluasi"); // NOI18N
        tbEvaluasi.setSelectionForeground(new java.awt.Color(0, 255, 0));
        Scroll5.setViewportView(tbEvaluasi);

        Tab1dan2dan3.addTab("RENCANA TINDAKAN (SIKI)", Scroll5);

        jPanel5.add(Tab1dan2dan3, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel5);

        PanelCariUtama.add(jPanel1, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("Diagnosa Keperawatan", PanelCariUtama);

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new java.awt.Dimension(816, 102));
        jPanel2.setLayout(new java.awt.GridLayout(1, 2));

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 51, 0), 1, true), "Masalah Keperawatan", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 12), new java.awt.Color(50, 50, 50))); // NOI18N
        jPanel4.setName("jPanel4"); // NOI18N
        jPanel4.setOpaque(false);
        jPanel4.setPreferredSize(new java.awt.Dimension(310, 143));
        jPanel4.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbMasalah.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tbMasalah.setName("tbMasalah"); // NOI18N
        tbMasalah.addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                tbMasalahAncestorAdded(evt);
            }
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });
        tbMasalah.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbMasalahMouseClicked(evt);
            }
        });
        tbMasalah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbMasalahKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbMasalah);

        jPanel4.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel4);

        TabRawat.addTab("Input Diagnosa Keperawatan", jPanel2);

        internalFrame2.add(TabRawat, java.awt.BorderLayout.CENTER);
        TabRawat.getAccessibleContext().setAccessibleName("Diagnosa Keperawatan");

        internalFrame1.add(internalFrame2, java.awt.BorderLayout.CENTER);

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(55, 55));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
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
        panelGlass8.add(BtnSimpan);

        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        BtnBatal.setMnemonic('B');
        BtnBatal.setText("Baru");
        BtnBatal.setToolTipText("Alt+B");
        BtnBatal.setName("BtnBatal"); // NOI18N
        BtnBatal.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBatalActionPerformed(evt);
            }
        });
        BtnBatal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnBatalKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnBatal);

        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnEdit.setMnemonic('G');
        BtnEdit.setText("Ganti");
        BtnEdit.setToolTipText("Alt+G");
        BtnEdit.setName("BtnEdit"); // NOI18N
        BtnEdit.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnEditActionPerformed(evt);
            }
        });
        BtnEdit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnEditKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnEdit);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus"); // NOI18N
        BtnHapus.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapusActionPerformed(evt);
            }
        });
        BtnHapus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnHapusKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnHapus);

        BtnTambah1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/download.png"))); // NOI18N
        BtnTambah1.setMnemonic('3');
        BtnTambah1.setText("AMBIL DATA");
        BtnTambah1.setToolTipText("Alt+3");
        BtnTambah1.setName("BtnTambah1"); // NOI18N
        BtnTambah1.setPreferredSize(new java.awt.Dimension(120, 30));
        BtnTambah1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnTambah1ActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnTambah1);

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
        panelGlass8.add(BtnKeluar);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass8.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(265, 23));
        TCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TCariActionPerformed(evt);
            }
        });
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass8.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('4');
        BtnCari.setToolTipText("Alt+4");
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
        panelGlass8.add(BtnCari);

        jLabel10.setName("jLabel10"); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(140, 30));
        panelGlass8.add(jLabel10);

        internalFrame1.add(panelGlass8, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if (tbMasalahDetailMasalah.isEditing()) {
            tbMasalahDetailMasalah.getCellEditor().stopCellEditing();
        }
        if (tbDiagKep.isEditing()) {
            tbDiagKep.getCellEditor().stopCellEditing();
        }
        if (!BtnSimpan.isEnabled()) return;
        BtnSimpan.setEnabled(false);
        try {
        String rw = TNoRw.getText().trim();
        if (rw.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nomor rawat tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nipUser = akses.getkode();
        if (nipUser == null || nipUser.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "NIP user tidak ditemukan! Login ulang.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        nipUser = nipUser.trim();

        try {
            switch (TabRawat.getSelectedIndex()) {
                case 0: // Tab Detail Masalah
                    int selectedRow = tbMasalahDetailMasalah.getSelectedRow();
                    if (selectedRow == -1) {
                        JOptionPane.showMessageDialog(null, "Pilih masalah keperawatan terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String kodeMasalah = tbMasalahDetailMasalah.getValueAt(selectedRow, 0).toString().trim();
                    String namaMasalahTarget = tbMasalahDetailMasalah.getValueAt(selectedRow, 1).toString().trim();
                    String keterangan = tbMasalahDetailMasalah.getValueAt(selectedRow, 3).toString().trim();
                    Object waktuEvalObj = tbMasalahDetailMasalah.getValueAt(selectedRow, 2);
                    String waktuEval = (waktuEvalObj == null) ? "" : waktuEvalObj.toString().trim();
                    String now = Sequel.cariIsi("SELECT NOW()");

                    if (kodeMasalah.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "Kode masalah tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String nipevalFinal = null;
                    String tanggalEvalFinal = null;
                    if (!"BELUM TERATASI".equalsIgnoreCase(keterangan)) {
                        nipevalFinal = nipUser;
                        tanggalEvalFinal = now;
                    }

                    // ── Tentukan tabel target berdasarkan data yang ada ──────────────
                    boolean adaRanapCase0 = Sequel.cariInteger(
                            "SELECT COUNT(*) FROM penilaian_awal_keperawatan_ranap_masalah "
                            + "WHERE no_rawat='" + rw + "' AND kode_masalah='" + kodeMasalah + "'") > 0;

                    boolean adaRalanCase0 = Sequel.cariInteger(
                            "SELECT COUNT(*) FROM penilaian_awal_keperawatan_ralan_masalah "
                            + "WHERE no_rawat='" + rw + "' AND kode_masalah='" + kodeMasalah + "'") > 0;

                    // Kalau tidak ada di keduanya, cek no_rawat untuk default tabel
                    if (!adaRanapCase0 && !adaRalanCase0) {
                        adaRanapCase0 = Sequel.cariInteger(
                                "SELECT COUNT(*) FROM penilaian_awal_keperawatan_ranap_masalah "
                                + "WHERE no_rawat='" + rw + "'") > 0;
                        adaRalanCase0 = !adaRanapCase0;
                    }

                    String sqlTemplate = "INSERT INTO %s "
                            + "(no_rawat, kode_masalah, nip, keterangan, nipeval, tanggal_eval, waktu_eval) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                            + "ON DUPLICATE KEY UPDATE "
                            + "nip = COALESCE(nip, VALUES(nip)), "
                            + "keterangan = VALUES(keterangan), "
                            + "nipeval = VALUES(nipeval), "
                            + "tanggal_eval = VALUES(tanggal_eval), "
                            + "waktu_eval = VALUES(waktu_eval)";

                    if (adaRanapCase0) {
                        try ( PreparedStatement ps = koneksi.prepareStatement(
                                String.format(sqlTemplate, "penilaian_awal_keperawatan_ranap_masalah"))) {
                            ps.setString(1, rw);
                            ps.setString(2, kodeMasalah);
                            ps.setString(3, nipUser);
                            ps.setString(4, keterangan);
                            ps.setString(5, nipevalFinal);
                            ps.setString(6, tanggalEvalFinal);
                            ps.setString(7, waktuEval.isEmpty() ? null : waktuEval);
                            ps.executeUpdate();
                        }
                    }

                    if (adaRalanCase0) {
                        try ( PreparedStatement ps = koneksi.prepareStatement(
                                String.format(sqlTemplate, "penilaian_awal_keperawatan_ralan_masalah"))) {
                            ps.setString(1, rw);
                            ps.setString(2, kodeMasalah);
                            ps.setString(3, nipUser);
                            ps.setString(4, keterangan);
                            ps.setString(5, nipevalFinal);
                            ps.setString(6, tanggalEvalFinal);
                            ps.setString(7, waktuEval.isEmpty() ? null : waktuEval);
                            ps.executeUpdate();
                        }
                    }

                    syncChecklistPerMasalah("data_detail_diagnosa_kep", "kode_diagkep", tbDiagKep, 7, rw, kodeMasalah);
                    syncChecklistPerMasalah("data_detail_slki", "kode_slki", tbIntervensi, 7, rw, kodeMasalah);
                    syncChecklistPerMasalah("data_detail_siki", "kode_siki", tbEvaluasi, 7, rw, kodeMasalah);
                    simpanBatchDetailMasalah(rw, nipUser, now);

                    getMasalah();
                    boolean barisDitemukan = false;
                    for (int i = 0; i < tbMasalahDetailMasalah.getRowCount(); i++) {
                        String kRow = tbMasalahDetailMasalah.getValueAt(i, 0).toString().trim();
                        String nRow = tbMasalahDetailMasalah.getValueAt(i, 1).toString().trim();
                        if (kRow.equals(kodeMasalah) && nRow.equals(namaMasalahTarget)) {
                            tbMasalahDetailMasalah.setRowSelectionInterval(i, i);
                            barisDitemukan = true;
                            break;
                        }
                    }

                    if (barisDitemukan) {
                        tampildiagkep();
                        tampilintervensi();
                        tampilevaluasi();
                        JOptionPane.showMessageDialog(null,
                                "Data diagnosa " + kodeMasalah + " berhasil disimpan.",
                                "Sukses",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        tbMasalahDetailMasalah.clearSelection();
                        kosongkanDetailDiagIntervEval();

                        int activeFilter = (cmbFilterStatus != null) ? cmbFilterStatus.getSelectedIndex() : 0;
                        boolean isBelum = "BELUM TERATASI".equalsIgnoreCase(keterangan.trim());
                        if (activeFilter == 2 && !isBelum) {
                            JOptionPane.showMessageDialog(null,
                                    "Data diagnosa " + kodeMasalah + " berhasil disimpan.\nMasalah dipindahkan ke daftar Teratasi.",
                                    "Sukses",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else if (activeFilter == 1 && isBelum) {
                            JOptionPane.showMessageDialog(null,
                                    "Data diagnosa " + kodeMasalah + " berhasil disimpan.\nMasalah dipindahkan ke daftar Belum Teratasi.",
                                    "Sukses",
                                    JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "Data diagnosa " + kodeMasalah + " berhasil disimpan.",
                                    "Sukses",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                    break;

                case 1: // Tab Masalah Keperawatan (Input Masalah Baru dari Master)
                    int jumlahChecked = 0;
                    for (int i = 0; i < tbMasalah.getRowCount(); i++) {
                        if (tbMasalah.getValueAt(i, 0) != null && tbMasalah.getValueAt(i, 0).toString().equals("true")) {
                            jumlahChecked++;
                        }
                    }

                    if (jumlahChecked == 0) {
                        JOptionPane.showMessageDialog(null,
                                "Tidak ada masalah keperawatan yang dipilih!",
                                "Peringatan",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Ambil target kategori rawat dari Radio Button (Pendekatan B)
                    boolean isRanap = (rbRanap != null && rbRanap.isSelected());
                    String tabelTarget = isRanap ? "penilaian_awal_keperawatan_ranap_masalah" : "penilaian_awal_keperawatan_ralan_masalah";
                    String labelKategori = isRanap ? "Rawat Inap (Ranap)" : "Rawat Jalan (Ralan)";

                    int berhasilSimpan = 0;
                    for (int i = 0; i < tbMasalah.getRowCount(); i++) {
                        if (tbMasalah.getValueAt(i, 0) != null && tbMasalah.getValueAt(i, 0).toString().equals("true")) {
                            Sequel.menyimpan2(tabelTarget, "?,?,?,?,?,?,?", 7, new String[]{
                                rw,
                                tbMasalah.getValueAt(i, 1).toString(),
                                nipUser,
                                "BELUM TERATASI",
                                "",
                                "00:00:00",
                                ""
                            });
                            berhasilSimpan++;
                        }
                    }

                    for (int i = 0; i < tbMasalah.getRowCount(); i++) {
                        tbMasalah.setValueAt(false, i, 0);
                    }
                    getMasalah();
                    TabRawat.setSelectedIndex(0);

                    JOptionPane.showMessageDialog(null,
                            berhasilSimpan + " masalah keperawatan berhasil disimpan ke " + labelKategori + ".",
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;

                default:
                    JOptionPane.showMessageDialog(null, "Tab tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
                    break;
            }
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Duplicate entry")) {
                JOptionPane.showMessageDialog(null,
                        "Data sudah ada! Gunakan tombol Update untuk mengubah data.",
                        "Peringatan",
                        JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null,
                        "Gagal menyimpan: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Gagal menyimpan: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
        } finally {
            BtnSimpan.setEnabled(true);
        }
    }

    /**
     * Menyimpan perubahan Lama Evaluasi (waktu_eval) dan Status (keterangan)
     * untuk seluruh baris pada tbMasalahDetailMasalah sekaligus.
     * Riwayat nipeval dan tanggal_eval dipertahankan jika masalah sudah teratasi sebelumnya.
     */
    private void simpanBatchDetailMasalah(String rw, String nipUser, String now) throws SQLException {
        int rowCount = tabModeDetailMasalah.getRowCount();
        String sqlUpsertTemplate = "INSERT INTO %s "
                + "(no_rawat, kode_masalah, nip, keterangan, nipeval, tanggal_eval, waktu_eval) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "nip = COALESCE(nip, VALUES(nip)), "
                + "keterangan = VALUES(keterangan), "
                + "nipeval = VALUES(nipeval), "
                + "tanggal_eval = VALUES(tanggal_eval), "
                + "waktu_eval = VALUES(waktu_eval)";

        for (int r = 0; r < rowCount; r++) {
            Object kodeObj = tabModeDetailMasalah.getValueAt(r, 0);
            String kodeMasalahRow = (kodeObj == null) ? "" : kodeObj.toString().trim();
            if (kodeMasalahRow.isEmpty()) {
                continue;
            }

            Object namaObj = tabModeDetailMasalah.getValueAt(r, 1);
            String namaMasalahRow = (namaObj == null) ? "" : namaObj.toString().trim();

            Object waktuEvalObj = tabModeDetailMasalah.getValueAt(r, 2);
            String waktuEvalRow = (waktuEvalObj == null) ? "" : waktuEvalObj.toString().trim();

            Object ketObj = tabModeDetailMasalah.getValueAt(r, 3);
            String keteranganRow = (ketObj == null || ketObj.toString().trim().isEmpty())
                    ? "BELUM TERATASI"
                    : ketObj.toString().trim();

            Object nipEvalObj = tabModeDetailMasalah.getValueAt(r, 6);
            String nipEvalLama = (nipEvalObj == null) ? "" : nipEvalObj.toString().trim();

            Object tglEvalObj = tabModeDetailMasalah.getValueAt(r, 7);
            String tglEvalLama = (tglEvalObj == null) ? "" : tglEvalObj.toString().trim();

            String nipevalFinal = null;
            String tanggalEvalFinal = null;
            if (!"BELUM TERATASI".equalsIgnoreCase(keteranganRow)) {
                // Jika sebelumnya sudah memiliki evaluator & tanggal evaluasi, pertahankan agar tidak tertimpa
                if (!nipEvalLama.isEmpty() && !tglEvalLama.isEmpty()) {
                    nipevalFinal = nipEvalLama;
                    tanggalEvalFinal = tglEvalLama;
                } else {
                    nipevalFinal = nipUser;
                    tanggalEvalFinal = now;
                }
            }

            boolean isRalanRow = namaMasalahRow.endsWith("(Ralan)");
            boolean isRanapRow = namaMasalahRow.endsWith("(Ranap)");
            String tabelTarget;
            if (isRalanRow) {
                tabelTarget = "penilaian_awal_keperawatan_ralan_masalah";
            } else if (isRanapRow) {
                tabelTarget = "penilaian_awal_keperawatan_ranap_masalah";
            } else {
                boolean adaRanap = Sequel.cariInteger(
                        "SELECT COUNT(*) FROM penilaian_awal_keperawatan_ranap_masalah "
                        + "WHERE no_rawat='" + rw + "' AND kode_masalah='" + kodeMasalahRow + "'") > 0;
                tabelTarget = adaRanap
                        ? "penilaian_awal_keperawatan_ranap_masalah"
                        : "penilaian_awal_keperawatan_ralan_masalah";
            }

            try (PreparedStatement psUpsert = koneksi.prepareStatement(String.format(sqlUpsertTemplate, tabelTarget))) {
                psUpsert.setString(1, rw);
                psUpsert.setString(2, kodeMasalahRow);
                psUpsert.setString(3, nipUser);
                psUpsert.setString(4, keteranganRow);
                psUpsert.setString(5, nipevalFinal);
                psUpsert.setString(6, tanggalEvalFinal);
                psUpsert.setString(7, waktuEvalRow.isEmpty() ? null : waktuEvalRow);
                psUpsert.executeUpdate();
            }
        }
    }

    private void syncChecklistPerMasalah(String tableName, String codeColName, javax.swing.JTable table, int codeColumnIndex, String noRawat, String kodeMasalah) throws Exception {

        Set<String> existing = getExistingCodesFromDBPerMasalah(tableName, codeColName, noRawat, kodeMasalah);

        Map<String, String> currentChecked = new HashMap<>();

        for (int i = 0; i < table.getRowCount(); i++) {
            Object chk = table.getValueAt(i, 0);
            boolean checked = (chk instanceof Boolean) ? (Boolean) chk
                    : (chk != null && "true".equalsIgnoreCase(chk.toString()));
            if (checked) {
                Object codeObj = table.getValueAt(i, codeColumnIndex);
                String code = (codeObj == null) ? "" : codeObj.toString().trim().replaceAll("\\u00A0", "");

                String item = "";
                if ("data_detail_diagnosa_kep".equals(tableName)) {
                    Object itemObj = table.getValueAt(i, 6); // Kolom 6: item
                    item = (itemObj == null) ? "" : itemObj.toString().trim();

                }

                if (!code.isEmpty()) {
                    currentChecked.put(code, item);
                }
            }
        }

        Set<String> toInsert = new HashSet<>(currentChecked.keySet());
        toInsert.removeAll(existing);

        Set<String> toDelete = new HashSet<>(existing);
        toDelete.removeAll(currentChecked.keySet());

        // ✅ PERBAIKAN: INSERT dengan kolom item untuk data_detail_diagnosa_kep
        for (String code : toInsert) {
            if ("data_detail_diagnosa_kep".equals(tableName)) {
                String itemValue = currentChecked.get(code);
                if (itemValue != null && itemValue.length() > 255) {
                    itemValue = itemValue.substring(0, 255);
                }
                String sql = "INSERT INTO " + tableName + " (no_rawat, " + codeColName + ", kode_masalah, item) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE item = VALUES(item)";
                System.out.println("DEBUG - INSERT/UPDATE | Code: " + code + " | Item: " + itemValue); // DEBUG
                try ( PreparedStatement ps = koneksi.prepareStatement(sql)) {
                    ps.setString(1, noRawat);
                    ps.setString(2, code);
                    ps.setString(3, kodeMasalah);
                    ps.setString(4, itemValue);
                    ps.executeUpdate();
                }
            } else {
                // Untuk tabel lain (slki, siki) tanpa kolom item
                String sql = "INSERT INTO " + tableName + " (no_rawat, " + codeColName + ", kode_masalah) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE " + codeColName + " = VALUES(" + codeColName + ")";
                try ( PreparedStatement ps = koneksi.prepareStatement(sql)) {
                    ps.setString(1, noRawat);
                    ps.setString(2, code);
                    ps.setString(3, kodeMasalah);
                    ps.executeUpdate();
                }
            }
        }

        // ✅ UPDATE data yang sudah ada (untuk edit)
        for (String code : existing) {
            if (currentChecked.containsKey(code)) {
                if ("data_detail_diagnosa_kep".equals(tableName)) {
                    String itemValue = currentChecked.get(code);
                    if (itemValue != null && itemValue.length() > 255) {
                        itemValue = itemValue.substring(0, 255);
                    }
                    String sql = "UPDATE " + tableName + " SET item = ? WHERE no_rawat = ? AND " + codeColName + " = ? AND kode_masalah = ?";
                    System.out.println("DEBUG - UPDATE | Code: " + code + " | Item: " + itemValue); // DEBUG
                    try ( PreparedStatement ps = koneksi.prepareStatement(sql)) {
                        ps.setString(1, itemValue);
                        ps.setString(2, noRawat);
                        ps.setString(3, code);
                        ps.setString(4, kodeMasalah);
                        ps.executeUpdate();
                    }
                }
            }
        }

        // DELETE data yang tidak dicentang lagi
        for (String code : toDelete) {
            String sql = "DELETE FROM " + tableName + " WHERE no_rawat = ? AND " + codeColName + " = ? AND kode_masalah = ?";
            try ( PreparedStatement ps = koneksi.prepareStatement(sql)) {
                ps.setString(1, noRawat);
                ps.setString(2, code);
                ps.setString(3, kodeMasalah);
                ps.executeUpdate();
            }
        }
    }

    private Set<String> getExistingCodesFromDBPerMasalah(String tableName, String codeColName, String noRawat, String kodeMasalah) throws Exception {
        Set<String> result = new HashSet<>();
        String sql = "SELECT " + codeColName + " FROM " + tableName + " WHERE no_rawat = ? AND kode_masalah = ?";
        try ( PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, noRawat);
            ps.setString(2, kodeMasalah);
            try ( ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String kode = rs.getString(1);
                    if (kode != null && !kode.trim().isEmpty()) {
                        result.add(kode.trim().replaceAll("\\u00A0", ""));
                    }
                }
            }
        }
        return result;
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnSimpanActionPerformed(null);
        } else {
//            Valid.pindah(evt, Pemeriksaan, BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
        int selectedRow = tbMasalahDetailMasalah.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Pilih masalah keperawatan terlebih dahulu!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Uncheck semua checkbox di 3 tabel
        for (int i = 0; i < tbDiagKep.getRowCount(); i++) {
            tbDiagKep.setValueAt(false, i, 0);
        }
        for (int i = 0; i < tbIntervensi.getRowCount(); i++) {
            tbIntervensi.setValueAt(false, i, 0);
        }
        for (int i = 0; i < tbEvaluasi.getRowCount(); i++) {
            tbEvaluasi.setValueAt(false, i, 0);
        }
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            emptTeks();
        } else {
            Valid.pindah(evt, BtnSimpan, BtnHapus);
        }
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dokter.dispose();
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            dispose();
        } else {
//            Valid.pindah(evt, TCari, Pemeriksaan);
        }
}//GEN-LAST:event_BtnKeluarKeyPressed

private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        String rw = TNoRw.getText().trim();
        if (rw.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nomor rawat tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedRow = tbMasalahDetailMasalah.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(null, "Pilih masalah keperawatan yang akan dihapus!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String kodeMasalah = tbMasalahDetailMasalah.getValueAt(selectedRow, 0).toString().trim();
        String namaMasalahRaw = tbMasalahDetailMasalah.getValueAt(selectedRow, 1).toString().trim();
        boolean isRalan = namaMasalahRaw.endsWith("(Ralan)");
        String namaMasalah = namaMasalahRaw.replace(" (Ranap)", "").replace(" (Ralan)", "").trim();

        int confirm = JOptionPane.showConfirmDialog(null,
                "Yakin ingin menghapus diagnosa:\n" + kodeMasalah + " - " + namaMasalah + " (" + (isRalan ? "Ralan" : "Ranap") + ")?\n\n"
                + "Catatan: Detail SDKI, SLKI, dan SIKI terkait akan ikut terhapus jika diagnosa ini\ntidak digunakan di episode rawat lainnya.",
                "Konfirmasi Hapus",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        String tabelTarget = isRalan ? "penilaian_awal_keperawatan_ralan_masalah" : "penilaian_awal_keperawatan_ranap_masalah";
        String tabelLawan = isRalan ? "penilaian_awal_keperawatan_ranap_masalah" : "penilaian_awal_keperawatan_ralan_masalah";

        boolean initialAutoCommit = true;
        try {
            initialAutoCommit = koneksi.getAutoCommit();
            koneksi.setAutoCommit(false);

            // 1. Hapus hanya baris masalah pada tabel target episode yang dipilih
            String sqlDeleteMasalah = "DELETE FROM " + tabelTarget + " WHERE no_rawat = ? AND kode_masalah = ?";
            int deleted = 0;
            try (PreparedStatement psMasalah = koneksi.prepareStatement(sqlDeleteMasalah)) {
                psMasalah.setString(1, rw);
                psMasalah.setString(2, kodeMasalah);
                deleted = psMasalah.executeUpdate();
            }

            if (deleted <= 0) {
                koneksi.rollback();
                JOptionPane.showMessageDialog(null,
                        "Data tidak ditemukan atau sudah terhapus.",
                        "Informasi",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // 2. Cek apakah masih ada baris dengan no_rawat dan kode_masalah yang sama di tabel lawan
            boolean sisaDiLawan = false;
            String sqlCek = "SELECT COUNT(*) FROM " + tabelLawan + " WHERE no_rawat = ? AND kode_masalah = ?";
            try (PreparedStatement psCek = koneksi.prepareStatement(sqlCek)) {
                psCek.setString(1, rw);
                psCek.setString(2, kodeMasalah);
                try (ResultSet rsCek = psCek.executeQuery()) {
                    if (rsCek.next() && rsCek.getInt(1) > 0) {
                        sisaDiLawan = true;
                    }
                }
            }

            // 3. Detail SDKI/SLKI/SIKI hanya dihapus jika tidak ada lagi baris masalah di tabel manapun
            if (!sisaDiLawan) {
                String sqlDeleteSDKI = "DELETE FROM data_detail_diagnosa_kep WHERE no_rawat = ? AND kode_masalah = ?";
                String sqlDeleteSLKI = "DELETE FROM data_detail_slki WHERE no_rawat = ? AND kode_masalah = ?";
                String sqlDeleteSIKI = "DELETE FROM data_detail_siki WHERE no_rawat = ? AND kode_masalah = ?";
                try (PreparedStatement psSDKI = koneksi.prepareStatement(sqlDeleteSDKI);
                     PreparedStatement psSLKI = koneksi.prepareStatement(sqlDeleteSLKI);
                     PreparedStatement psSIKI = koneksi.prepareStatement(sqlDeleteSIKI)) {
                    psSDKI.setString(1, rw);
                    psSDKI.setString(2, kodeMasalah);
                    psSDKI.executeUpdate();

                    psSLKI.setString(1, rw);
                    psSLKI.setString(2, kodeMasalah);
                    psSLKI.executeUpdate();

                    psSIKI.setString(1, rw);
                    psSIKI.setString(2, kodeMasalah);
                    psSIKI.executeUpdate();
                }
            }

            koneksi.commit();

            JOptionPane.showMessageDialog(null,
                    "Data diagnosa " + kodeMasalah + " berhasil dihapus.",
                    "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);

            getMasalah();
            tbMasalahDetailMasalah.clearSelection();
            kosongkanDetailDiagIntervEval();
            updateFilterCount();

        } catch (Exception ex) {
            try {
                koneksi.rollback();
            } catch (SQLException rbEx) {
                System.err.println("Rollback gagal: " + rbEx.getMessage());
            }
            System.err.println("Error saat menghapus data diagnosa keperawatan: " + ex.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Gagal menghapus data. Hubungi administrator.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                koneksi.setAutoCommit(initialAutoCommit);
            } catch (SQLException e) {
                System.err.println("Gagal restore auto-commit: " + e.getMessage());
            }
        }
}//GEN-LAST:event_BtnHapusActionPerformed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void PenjabKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_PenjabKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_PenjabKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened

    }//GEN-LAST:event_formWindowOpened

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnHapusActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnBatal, BtnSimpan);
        }
    }//GEN-LAST:event_BtnHapusKeyPressed

    private void ppBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBersihkanActionPerformed

    }//GEN-LAST:event_ppBersihkanActionPerformed

    private void ppSemuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppSemuaActionPerformed

    }//GEN-LAST:event_ppSemuaActionPerformed

    private void TCariSkala1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariSkala1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            BtnCariSkala1ActionPerformed(null);
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            BtnCariSkala1.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            BtnKeluar.requestFocus();
        }
    }//GEN-LAST:event_TCariSkala1KeyPressed

    private void BtnCariSkala1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariSkala1ActionPerformed
        if (tbMasalahDetailMasalah.getSelectedRow() != -1) {
//            getMasalah();
            tampildiagkep();
            tampilintervensi();
            tampilevaluasi();
        } else {
            JOptionPane.showMessageDialog(null, "Silahkan pilih pemeriksaan terlebih dahulu..!!");
        }
    }//GEN-LAST:event_BtnCariSkala1ActionPerformed

    private void BtnCariSkala1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariSkala1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnCariSkala1KeyPressed

    private void BtnTambahSkala1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambahSkala1ActionPerformed
        if (Tab1dan2dan3.getSelectedIndex() == 1) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            MasterIntervensiImplementasi form = new MasterIntervensiImplementasi(null, false);
            form.isCek();
            form.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            form.setLocationRelativeTo(internalFrame1);
            form.setVisible(true);
            this.setCursor(Cursor.getDefaultCursor());
        } else if (Tab1dan2dan3.getSelectedIndex() == 2) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            MasterEvaluasiIntraOperatif form = new MasterEvaluasiIntraOperatif(null, false);
            form.isCek();
            form.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            form.setLocationRelativeTo(internalFrame1);
            form.setVisible(true);
            this.setCursor(Cursor.getDefaultCursor());
        } else if (Tab1dan2dan3.getSelectedIndex() == 0) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            MasterRencanaKeperawatan form = new MasterRencanaKeperawatan(null, false);
            form.isCek();
            form.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
            form.setLocationRelativeTo(internalFrame1);
            form.setVisible(true);
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_BtnTambahSkala1ActionPerformed

    private void Scroll3KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Scroll3KeyReleased
        if (tabModeDetailMasalah.getRowCount() != 0) {
            if ((evt.getKeyCode() == KeyEvent.VK_ENTER) || (evt.getKeyCode() == KeyEvent.VK_UP) || (evt.getKeyCode() == KeyEvent.VK_DOWN)) {
                try {
                    tampildiagkep();
                    tampilintervensi();
                    tampilevaluasi();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_Scroll3KeyReleased

    private void Tab1dan2dan3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_Tab1dan2dan3MouseClicked
        if (tbDiagKep.isEditing()) {
            tbDiagKep.getCellEditor().stopCellEditing();
        }
        if (Tab1dan2dan3.getSelectedIndex() == 0) {
//            label11.setForeground(new Color(170, 00, 0));
            TCariSkala1.setForeground(new Color(170, 00, 0));
            BtnTambahSkala1.setEnabled(akses.getpenilaian_awal_keperawatan_ralan());
        } else if (Tab1dan2dan3.getSelectedIndex() == 1) {
//            label11.setForeground(new Color(255, 0, 0));
            TCariSkala1.setForeground(new Color(255, 00, 0));
            BtnTambahSkala1.setEnabled(akses.getpenilaian_awal_keperawatan_ralan());
        } else if (Tab1dan2dan3.getSelectedIndex() == 2) {
//            label11.setForeground(new Color(255, 0, 0));
            TCariSkala1.setForeground(new Color(255, 00, 0));
            BtnTambahSkala1.setEnabled(akses.getpenilaian_awal_keperawatan_ralan());
        }
    }//GEN-LAST:event_Tab1dan2dan3MouseClicked

    private void tbMasalahDetailMasalahMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbMasalahDetailMasalahMouseClicked
        if (tbDiagKep.isEditing()) {
            tbDiagKep.getCellEditor().stopCellEditing();
        }
        if (tabModeDetailMasalah.getRowCount() != 0) {
            try {
                tampildiagkep();
                tampilintervensi();
                tampilevaluasi();
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbMasalahDetailMasalahMouseClicked

    private void tbMasalahDetailMasalahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbMasalahDetailMasalahKeyPressed
        if (tbDiagKep.isEditing()) {
            tbDiagKep.getCellEditor().stopCellEditing();
        }
    }//GEN-LAST:event_tbMasalahDetailMasalahKeyPressed

    private void tbMasalahDetailMasalahKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbMasalahDetailMasalahKeyReleased
        if (tbDiagKep.isEditing()) {
            tbDiagKep.getCellEditor().stopCellEditing();
        }
        if (tabModeDetailMasalah.getRowCount() != 0) {
            if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_UP || evt.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN) {
                try {
                    tampildiagkep();
                    tampilintervensi();
                    tampilevaluasi();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbMasalahDetailMasalahKeyReleased

    private void Jk1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Jk1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_Jk1ActionPerformed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if (tbDiagKep.isEditing()) {
            tbDiagKep.getCellEditor().stopCellEditing();
        }
        if (TabRawat.getSelectedIndex() == 0) {
            getMasalah();
            tampildiagkep();
            tampilintervensi();
            tampilevaluasi();
        } else if (TabRawat.getSelectedIndex() == 1) {
            updateDefaultKategoriInput();
            tampil();
        } else if (TabRawat.getSelectedIndex() == 2) {

        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if (tbMasalahDetailMasalah.isEditing()) {
            tbMasalahDetailMasalah.getCellEditor().stopCellEditing();
        }
        if (tbDiagKep.isEditing()) {
            tbDiagKep.getCellEditor().stopCellEditing();
        }
        String rw = TNoRw.getText().trim();
        if (rw.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nomor rawat tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String nipUser = akses.getkode();
        if (nipUser == null || nipUser.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "NIP user tidak ditemukan! Login ulang.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        nipUser = nipUser.trim();

        try {
            switch (TabRawat.getSelectedIndex()) {
                case 0: // Tab Detail Masalah
                    int selectedRow = tbMasalahDetailMasalah.getSelectedRow();
                    if (selectedRow == -1) {
                        JOptionPane.showMessageDialog(null, "Pilih masalah keperawatan yang akan diupdate!", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String kodeMasalah = tbMasalahDetailMasalah.getValueAt(selectedRow, 0).toString().trim();
                    Object keteranganObj = tbMasalahDetailMasalah.getValueAt(selectedRow, 3);
                    String keterangan = (keteranganObj == null) ? "BELUM TERATASI" : keteranganObj.toString().trim();
                    Object waktuEvalObj = tbMasalahDetailMasalah.getValueAt(selectedRow, 2);
                    String waktuEval = (waktuEvalObj == null) ? "" : waktuEvalObj.toString().trim();

                    String now = Sequel.cariIsi("SELECT NOW()");
                    String nipevalFinal = null;
                    String tanggalEvalFinal = null;

                    if (!"BELUM TERATASI".equalsIgnoreCase(keterangan)) {
                        nipevalFinal = nipUser;
                        tanggalEvalFinal = now;
                    }

                    // Deteksi apakah baris ini dari ranap atau ralan
                    // berdasarkan label nama masalah di kolom 1 yang diisi getMasalah()
                    String namaMasalahRow = tbMasalahDetailMasalah.getValueAt(selectedRow, 1).toString();
                    boolean isRalan = namaMasalahRow.endsWith("(Ralan)");
                    String tabelTarget = isRalan
                            ? "penilaian_awal_keperawatan_ralan_masalah"
                            : "penilaian_awal_keperawatan_ranap_masalah";

                    String sql = "UPDATE " + tabelTarget + " "
                            + "SET keterangan = ?, nipeval = ?, tanggal_eval = ?, waktu_eval = ? "
                            + "WHERE no_rawat = ? AND kode_masalah = ?";

                    try ( PreparedStatement ps = koneksi.prepareStatement(sql)) {
                        ps.setString(1, keterangan);
                        ps.setString(2, nipevalFinal);
                        ps.setString(3, tanggalEvalFinal);
                        ps.setString(4, waktuEval.isEmpty() ? null : waktuEval);
                        ps.setString(5, rw);
                        ps.setString(6, kodeMasalah);

                        int updated = ps.executeUpdate();
                        if (updated > 0) {
                            syncChecklistPerMasalah("data_detail_diagnosa_kep", "kode_diagkep", tbDiagKep, 7, rw, kodeMasalah);
                            syncChecklistPerMasalah("data_detail_slki", "kode_slki", tbIntervensi, 7, rw, kodeMasalah);
                            syncChecklistPerMasalah("data_detail_siki", "kode_siki", tbEvaluasi, 7, rw, kodeMasalah);
                            simpanBatchDetailMasalah(rw, nipUser, now);

                            JOptionPane.showMessageDialog(null,
                                    "Data diagnosa " + kodeMasalah + " berhasil diupdate.",
                                    "Sukses",
                                    JOptionPane.INFORMATION_MESSAGE);

                            getMasalah();
                            boolean barisDitemukan = false;
                            for (int i = 0; i < tbMasalahDetailMasalah.getRowCount(); i++) {
                                String kRow = tbMasalahDetailMasalah.getValueAt(i, 0).toString().trim();
                                String nRow = tbMasalahDetailMasalah.getValueAt(i, 1).toString().trim();
                                if (kRow.equals(kodeMasalah) && nRow.equals(namaMasalahRow.trim())) {
                                    tbMasalahDetailMasalah.setRowSelectionInterval(i, i);
                                    barisDitemukan = true;
                                    break;
                                }
                            }

                            if (barisDitemukan) {
                                tampildiagkep();
                                tampilintervensi();
                                tampilevaluasi();
                                JOptionPane.showMessageDialog(null,
                                        "Data diagnosa " + kodeMasalah + " berhasil diupdate.",
                                        "Sukses",
                                        JOptionPane.INFORMATION_MESSAGE);
                            } else {
                                tbMasalahDetailMasalah.clearSelection();
                                kosongkanDetailDiagIntervEval();

                                int activeFilter = (cmbFilterStatus != null) ? cmbFilterStatus.getSelectedIndex() : 0;
                                boolean isBelum = "BELUM TERATASI".equalsIgnoreCase(keterangan.trim());
                                if (activeFilter == 2 && !isBelum) {
                                    JOptionPane.showMessageDialog(null,
                                            "Data diagnosa " + kodeMasalah + " berhasil diupdate.\nMasalah dipindahkan ke daftar Teratasi.",
                                            "Sukses",
                                            JOptionPane.INFORMATION_MESSAGE);
                                } else if (activeFilter == 1 && isBelum) {
                                    JOptionPane.showMessageDialog(null,
                                            "Data diagnosa " + kodeMasalah + " berhasil diupdate.\nMasalah dipindahkan ke daftar Belum Teratasi.",
                                            "Sukses",
                                            JOptionPane.INFORMATION_MESSAGE);
                                } else {
                                    JOptionPane.showMessageDialog(null,
                                            "Data diagnosa " + kodeMasalah + " berhasil diupdate.",
                                            "Sukses",
                                            JOptionPane.INFORMATION_MESSAGE);
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(null,
                                    "Data tidak ditemukan!",
                                    "Peringatan",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }
                    break;

                case 1: // Tab Masalah Keperawatan
                    int jumlahChecked = 0;

                    for (int i = 0; i < tbMasalah.getRowCount(); i++) {
                        if (tbMasalah.getValueAt(i, 0).toString().equals("true")) {
                            String kodeMasalahUpd = tbMasalah.getValueAt(i, 1).toString();

                            // Cek apakah data sudah ada
                            if (Sequel.cariInteger("SELECT COUNT(*) FROM penilaian_awal_keperawatan_ranap_masalah "
                                    + "WHERE no_rawat='" + rw + "' AND kode_masalah='" + kodeMasalahUpd + "'") > 0) {
                                // Update jika sudah ada
                                Sequel.mengedit("penilaian_awal_keperawatan_ranap_masalah",
                                        "no_rawat='" + rw + "' AND kode_masalah='" + kodeMasalahUpd + "'",
                                        "keterangan='BELUM TERATASI', nip='" + nipUser + "'");
                            } else {
                                // Insert jika belum ada
                                Sequel.menyimpan2("penilaian_awal_keperawatan_ranap_masalah", "?,?,?,?,?,?,?", 7, new String[]{
                                    rw,
                                    kodeMasalahUpd,
                                    akses.getkode(),
                                    "BELUM TERATASI",
                                    "",
                                    "00:00:00",
                                    ""
                                });
                            }
                            jumlahChecked++;
                        }
                    }

                    if (jumlahChecked == 0) {
                        JOptionPane.showMessageDialog(null,
                                "Tidak ada masalah keperawatan yang dipilih!",
                                "Peringatan",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    getMasalah();

                    JOptionPane.showMessageDialog(null,
                            jumlahChecked + " masalah keperawatan berhasil diupdate.",
                            "Sukses",
                            JOptionPane.INFORMATION_MESSAGE);
                    break;

                default:
                    JOptionPane.showMessageDialog(null, "Tab tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
                    break;
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Gagal update: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnEditActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnHapus, BtnSimpan);
        }
    }//GEN-LAST:event_BtnEditKeyPressed

    private void BtnTambah1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnTambah1ActionPerformed
        String rw = TNoRw.getText().trim();
        if (rw.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nomor rawat tidak boleh kosong!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String[] options = {"Masalah Keperawatan", "SDKI (Diagnosa)", "SLKI (Tujuan)", "SIKI (Rencana Tindakan)"};
        int pilihan = JOptionPane.showOptionDialog(
                null,
                "Pilih data yang akan diambil:",
                "Ambil Data",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (pilihan == -1) {
            return;
        }

        StringBuilder hasilData = new StringBuilder();
        int jumlahData = 0;

        try {
            switch (pilihan) {
                case 0: // Masalah Keperawatan - Ambil dari data yang sedang tampil di tabel (sesuai filter aktif)
                    for (int i = 0; i < tbMasalahDetailMasalah.getRowCount(); i++) {
                        String kode = tbMasalahDetailMasalah.getValueAt(i, 0) == null ? "" : tbMasalahDetailMasalah.getValueAt(i, 0).toString().trim();
                        String nama = tbMasalahDetailMasalah.getValueAt(i, 1) == null ? "" : tbMasalahDetailMasalah.getValueAt(i, 1).toString().trim();
                        if (!kode.isEmpty() || !nama.isEmpty()) {
                            if (jumlahData > 0) {
                                hasilData.append("\n");
                            }
                            if (!kode.isEmpty() && !nama.isEmpty()) {
                                hasilData.append(kode).append(" - ").append(nama);
                            } else {
                                hasilData.append(kode).append(nama);
                            }
                            jumlahData++;
                        }
                    }

                    if (jumlahData == 0) {
                        JOptionPane.showMessageDialog(null, "Tidak ada data masalah keperawatan yang tampil di tabel!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    break;

                case 1: // SDKI (Diagnosa Keperawatan)
                    for (int i = 0; i < tbDiagKep.getRowCount(); i++) {
                        if (tbDiagKep.getValueAt(i, 0).toString().equals("true")) {
                            if (jumlahData > 0) {
                                hasilData.append("\n");
                            }
                            hasilData.append(tbDiagKep.getValueAt(i, 6).toString());
                            jumlahData++;
                        }
                    }
                    if (jumlahData == 0) {
                        JOptionPane.showMessageDialog(null, "Tidak ada diagnosa yang dicentang!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    break;

                case 2: // SLKI (Tujuan Keperawatan)
                    for (int i = 0; i < tbIntervensi.getRowCount(); i++) {
                        if (tbIntervensi.getValueAt(i, 0).toString().equals("true")) {
                            if (jumlahData > 0) {
                                hasilData.append("\n");
                            }
                            hasilData.append(tbIntervensi.getValueAt(i, 6).toString());
                            jumlahData++;
                        }
                    }
                    if (jumlahData == 0) {
                        JOptionPane.showMessageDialog(null, "Tidak ada tujuan yang dicentang!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    break;

                case 3: // SIKI (Rencana Tindakan)
                    for (int i = 0; i < tbEvaluasi.getRowCount(); i++) {
                        if (tbEvaluasi.getValueAt(i, 0).toString().equals("true")) {
                            if (jumlahData > 0) {
                                hasilData.append("\n");
                            }
                            hasilData.append(tbEvaluasi.getValueAt(i, 6).toString());
                            jumlahData++;
                        }
                    }
                    if (jumlahData == 0) {
                        JOptionPane.showMessageDialog(null, "Tidak ada rencana tindakan yang dicentang!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    break;
            }

            // Simpan data
            setDataHasil(hasilData.toString());
            System.out.println("Data disimpan: " + hasilData.toString());

            JOptionPane.showMessageDialog(null,
                    jumlahData + " data " + options[pilihan] + " berhasil diambil.",
                    "Sukses",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null,
                    "Gagal mengambil data: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }//GEN-LAST:event_BtnTambah1ActionPerformed

    private void tbMasalahMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbMasalahMouseClicked
        if (tabMode.getRowCount() != 0) {
            if (evt.getClickCount() == 2) {
                dispose();
            }
            if (tbMasalah.getSelectedRow() != -1) {
//                tampilSDKI(tbMasalah.getValueAt(tbMasalah.getSelectedRow(), 1).toString());
//                tampilSLKI(tbMasalah.getValueAt(tbMasalah.getSelectedRow(), 1).toString());
//                tampilSIKI(tbMasalah.getValueAt(tbMasalah.getSelectedRow(), 1).toString());
            }
        }
    }//GEN-LAST:event_tbMasalahMouseClicked

    private void tbMasalahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbMasalahKeyPressed
        if (tabMode.getRowCount() != 0) {
            if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                dispose();
            } else if (evt.getKeyCode() == KeyEvent.VK_SHIFT) {
//                TCari.setText("");
//                TCari.requestFocus();
            }
        }
    }//GEN-LAST:event_tbMasalahKeyPressed

    private void tbMasalahAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_tbMasalahAncestorAdded
        // TODO add your handling code here:
    }//GEN-LAST:event_tbMasalahAncestorAdded

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        tampil();
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnCariActionPerformed(null);
        } else {
            Valid.pindah(evt, TCari, TCari);
        }
    }//GEN-LAST:event_BtnCariKeyPressed

    private void TCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TCariActionPerformed

    }//GEN-LAST:event_TCariActionPerformed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            BtnCariActionPerformed(null);
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            BtnCari.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            BtnKeluar.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            tbMasalah.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgDiagnosaKeperawatan dialog = new DlgDiagnosaKeperawatan(new javax.swing.JFrame(), true);
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
    private widget.TextBox Alamat;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnCariSkala1;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnSimpan;
    private widget.Button BtnTambah1;
    private widget.Button BtnTambahSkala1;
    private widget.CekBox ChkInput;
    private javax.swing.JPanel FormInput;
    private widget.TextBox Jk;
    private widget.TextBox Jk1;
    private javax.swing.JPanel PanelCariUtama;
    private widget.PanelBiasa PanelInput;
    private widget.TextBox Penjab;
    private javax.swing.JPopupMenu Popup;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll17;
    private widget.ScrollPane Scroll18;
    private widget.ScrollPane Scroll3;
    private widget.ScrollPane Scroll5;
    private widget.TextBox TCari;
    private widget.TextBox TCariSkala1;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private javax.swing.JTabbedPane Tab1dan2dan3;
    private javax.swing.JTabbedPane TabRawat;
    private widget.TextBox TglLahir;
    private widget.TextBox Umur;
    private javax.swing.ButtonGroup buttonGroup1;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel3;
    private widget.Label jLabel6;
    private widget.Label jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private widget.Label label35;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelisi6;
    private javax.swing.JMenuItem ppBersihkan;
    private javax.swing.JMenuItem ppSemua;
    private widget.Table tbDiagKep;
    private widget.Table tbEvaluasi;
    private widget.Table tbIntervensi;
    private widget.Table tbMasalah;
    private widget.Table tbMasalahDetailMasalah;
    // End of variables declaration//GEN-END:variables

    public void emptTeks() {

    }

    public void onCari() {
        TCariSkala1.requestFocus();
    }

    private void isRawat() {
        if (status.equals("Ranap")) {
            norawatibu = Sequel.cariIsi("select no_rawat from ranap_gabung where no_rawat2=?", TNoRw.getText());
            if (!norawatibu.equals("")) {
                kamar = Sequel.cariIsi("select ifnull(kd_kamar,'') from kamar_inap where no_rawat=? order by tgl_masuk desc limit 1", norawatibu);
                kelas = Sequel.cariIsi(
                        "select kamar.kelas from kamar inner join kamar_inap "
                        + "on kamar.kd_kamar=kamar_inap.kd_kamar where no_rawat=? "
                        + "and stts_pulang='-' order by STR_TO_DATE(concat(kamar_inap.tgl_masuk,' ',jam_masuk),'%Y-%m-%d %H:%i:%s') desc limit 1", norawatibu);
            } else {
                kamar = Sequel.cariIsi("select ifnull(kd_kamar,'') from kamar_inap where no_rawat=? order by tgl_masuk desc limit 1", TNoRw.getText());
                kelas = Sequel.cariIsi(
                        "select kamar.kelas from kamar inner join kamar_inap "
                        + "on kamar.kd_kamar=kamar_inap.kd_kamar where no_rawat=? "
                        + "and stts_pulang='-' order by STR_TO_DATE(concat(kamar_inap.tgl_masuk,' ',jam_masuk),'%Y-%m-%d %H:%i:%s') desc limit 1", TNoRw.getText());
            }
            namakamar = kamar + ", " + Sequel.cariIsi("select nm_bangsal from bangsal inner join kamar on bangsal.kd_bangsal=kamar.kd_bangsal "
                    + " where kamar.kd_kamar=? ", kamar);
            kamar = "Kamar";
        } else if (status.equals("Ralan")) {
            kelas = "Rawat Jalan";
            kamar = "Poli";
            namakamar = Sequel.cariIsi("select nm_poli from poliklinik inner join reg_periksa on poliklinik.kd_poli=reg_periksa.kd_poli "
                    + "where reg_periksa.no_rawat=?", TNoRw.getText());
        }
    }

    private void isPsien() {
        try {
            pstindakan = koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,reg_periksa.kd_pj,reg_periksa.kd_dokter,dokter.nm_dokter,pasien.nm_pasien,pasien.jk,pasien.umur,pasien.tgl_lahir,"
                    + "concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat "
                    + "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis inner join kelurahan on pasien.kd_kel=kelurahan.kd_kel "
                    + "inner join kecamatan on pasien.kd_kec=kecamatan.kd_kec inner join kabupaten on pasien.kd_kab=kabupaten.kd_kab "
                    + "inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter where no_rawat=?");
            try {
                pstindakan.setString(1, TNoRw.getText());
                rstindakan = pstindakan.executeQuery();
                while (rstindakan.next()) {
                    TNoRM.setText(rstindakan.getString("no_rkm_medis"));
                    Penjab.setText(rstindakan.getString("kd_pj"));
                    if (status.equals("Ranap")) {
//                        KodePerujuk.setText(Sequel.cariIsi("select dpjp_ranap.kd_dokter from dpjp_ranap where dpjp_ranap.no_rawat='" + TNoRw.getText() + "' LIMIT 1"));
//                        NmPerujuk.setText(Sequel.cariIsi("select dokter.nm_dokter from dokter where dokter.kd_dokter='" + KodePerujuk.getText() + "'"));
                    } else {
//                        KodePerujuk.setText(rstindakan.getString("kd_dokter"));
//                        NmPerujuk.setText(rstindakan.getString("nm_dokter"));
                    }
                    TPasien.setText(rstindakan.getString("nm_pasien"));
                    Jk1.setText(rstindakan.getString("jk"));
                    Umur.setText(rstindakan.getString("umur"));
                    TglLahir.setText(rstindakan.getString("alamat"));
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            } finally {
                if (rstindakan != null) {
                    rstindakan.close();
                }
                if (pstindakan != null) {
                    pstindakan.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }
    }

    private void isReset() {

    }

    public void setNoRm(String norwt, String posisi) {
        TNoRw.setText(norwt);
        this.status = posisi;
        isRawat();
        isPsien();
        isReset();
        getMasalah();
        resetFilterStatus();
        updateDefaultKategoriInput();

    }

    public void setNoRm(String norwt, String posisi, String kddokter, String nmdokter) {
        TNoRw.setText(norwt);
        this.status = posisi;
        isRawat();
        isPsien();
        isReset();
        getMasalah();
        resetFilterStatus();
        updateDefaultKategoriInput();

    }

    public void isCek() {
        BtnSimpan.setEnabled(akses.getpenilaian_awal_keperawatan_ranap());
        BtnHapus.setEnabled(akses.getpenilaian_awal_keperawatan_ranap());

    }

    private void isForm() {
        if (ChkInput.isSelected() == true) {
            ChkInput.setVisible(false);
            FormInput.setPreferredSize(new Dimension(WIDTH, 100));
            PanelInput.setVisible(true);
            ChkInput.setVisible(true);
        } else if (ChkInput.isSelected() == false) {
            ChkInput.setVisible(false);
            FormInput.setPreferredSize(new Dimension(WIDTH, 20));
            PanelInput.setVisible(false);
            ChkInput.setVisible(true);
        }
    }

    private void getMasalah() {
        try {
            Valid.tabelKosong(tabModeDetailMasalah);
// ── 2. Cek apakah no_rawat ada di tabel RALAN ──────────────────────────
            boolean adaRalan = false;
            ps = koneksi.prepareStatement(
                    "SELECT 1 FROM penilaian_awal_keperawatan_ralan_masalah "
                    + "WHERE no_rawat = ? LIMIT 1");
            ps.setString(1, TNoRw.getText());
            rs = ps.executeQuery();
            if (rs.next()) {
                adaRalan = true;
            }
            rs.close();
            ps.close();

            if (adaRalan) {
                ps = koneksi.prepareStatement(
                        "SELECT\n"
                        + "	master_masalah_keperawatan.kode_masalah,\n"
                        + "	master_masalah_keperawatan.nama_masalah,\n"
                        + "	master_masalah_keperawatan.definisi,\n"
                        + "	penilaian_awal_keperawatan_ralan_masalah.keterangan,\n"
                        + "	COALESCE(penilaian_awal_keperawatan_igd.tanggal, penilaian_awal_keperawatan_ralan.tanggal, reg_periksa.tgl_registrasi) AS tanggal_input,\n"
                        + "	penilaian_awal_keperawatan_ralan_masalah.tanggal_eval,\n"
                        + "	penilaian_awal_keperawatan_ralan_masalah.nip,\n"
                        + "	petugas.nama AS nama_petugas,\n"
                        + "	penilaian_awal_keperawatan_ralan_masalah.nipeval,\n"
                        + "	penilaian_awal_keperawatan_ralan_masalah.waktu_eval,\n"
                        + "	petugas_eval.nama AS nama_petugas_eval\n"
                        + "FROM\n"
                        + "	master_masalah_keperawatan\n"
                        + "	INNER JOIN penilaian_awal_keperawatan_ralan_masalah ON penilaian_awal_keperawatan_ralan_masalah.kode_masalah = master_masalah_keperawatan.kode_masalah\n"
                        + "	LEFT JOIN penilaian_awal_keperawatan_ralan ON penilaian_awal_keperawatan_ralan.no_rawat = penilaian_awal_keperawatan_ralan_masalah.no_rawat\n"
                        + "	LEFT JOIN petugas ON penilaian_awal_keperawatan_ralan_masalah.nip = petugas.nip\n"
                        + "	LEFT JOIN petugas AS petugas_eval ON penilaian_awal_keperawatan_ralan_masalah.nipeval = petugas_eval.nip\n"
                        + "	LEFT JOIN penilaian_awal_keperawatan_igd ON penilaian_awal_keperawatan_ralan_masalah.no_rawat = penilaian_awal_keperawatan_igd.no_rawat \n"
                        + "	LEFT JOIN reg_periksa ON reg_periksa.no_rawat = penilaian_awal_keperawatan_ralan_masalah.no_rawat \n"
                        + "WHERE\n"
                        + "    penilaian_awal_keperawatan_ralan_masalah.no_rawat = ?\n"
                        + "ORDER BY kode_masalah");
                try {
                    ps.setString(1, TNoRw.getText());
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        String namaMasalah = (rs.getString("nama_masalah") == null ? "" : rs.getString("nama_masalah")) + " (Ralan)";
                        tabModeDetailMasalah.addRow(new Object[]{
                            (rs.getString("kode_masalah") == null ? "" : rs.getString("kode_masalah")),
                            namaMasalah,
                            (rs.getString("waktu_eval") == null ? "" : rs.getString("waktu_eval")),
                            (rs.getString("keterangan") == null ? "BELUM TERATASI" : rs.getString("keterangan")),
                            (rs.getString("nip") == null ? "" : rs.getString("nip")),
                            (rs.getString("tanggal_input") == null ? "" : rs.getString("tanggal_input")),
                            (rs.getString("nipeval") == null ? "" : rs.getString("nipeval")),
                            (rs.getString("tanggal_eval") == null ? "" : rs.getString("tanggal_eval"))
                        });
                    }
                } finally {
                    if (rs != null) {
                        rs.close();
                    }
                    if (ps != null) {
                        ps.close();
                    }
                }
            }
            // ── 1. Cek apakah no_rawat ada di tabel RANAP ──────────────────────────
            boolean adaRanap = false;
            ps = koneksi.prepareStatement(
                    "SELECT 1 FROM penilaian_awal_keperawatan_ranap_masalah "
                    + "WHERE no_rawat = ? LIMIT 1");
            ps.setString(1, TNoRw.getText());
            rs = ps.executeQuery();
            if (rs.next()) {
                adaRanap = true;
            }
            rs.close();
            ps.close();

            if (adaRanap) {
                ps = koneksi.prepareStatement(
                        "SELECT\n"
                        + "    master_masalah_keperawatan.kode_masalah,\n"
                        + "    master_masalah_keperawatan.nama_masalah,\n"
                        + "    penilaian_awal_keperawatan_ranap_masalah.keterangan,\n"
                        + "    COALESCE(penilaian_awal_keperawatan_ranap_masalah.nip, ranap.nip, ranap_anak.nip) AS nip,\n"
                        + "    COALESCE(ranap.tanggal, ranap_anak.tanggal, reg_periksa.tgl_registrasi) AS tanggal,\n"
                        + "    penilaian_awal_keperawatan_ranap_masalah.tanggal_eval,\n"
                        + "    penilaian_awal_keperawatan_ranap_masalah.nip AS nip_masalah,\n"
                        + "    penilaian_awal_keperawatan_ranap_masalah.nipeval,\n"
                        + "    penilaian_awal_keperawatan_ranap_masalah.waktu_eval\n"
                        + "FROM\n"
                        + "    master_masalah_keperawatan\n"
                        + "    INNER JOIN penilaian_awal_keperawatan_ranap_masalah\n"
                        + "        ON penilaian_awal_keperawatan_ranap_masalah.kode_masalah = master_masalah_keperawatan.kode_masalah\n"
                        + "    LEFT JOIN penilaian_awal_keperawatan_ranap ranap\n"
                        + "        ON ranap.no_rawat = penilaian_awal_keperawatan_ranap_masalah.no_rawat\n"
                        + "    LEFT JOIN penilaian_awal_keperawatan_ranap_anak ranap_anak\n"
                        + "        ON ranap_anak.no_rawat = penilaian_awal_keperawatan_ranap_masalah.no_rawat\n"
                        + "    LEFT JOIN reg_periksa\n"
                        + "        ON reg_periksa.no_rawat = penilaian_awal_keperawatan_ranap_masalah.no_rawat\n"
                        + "WHERE\n"
                        + "    penilaian_awal_keperawatan_ranap_masalah.no_rawat = ?\n"
                        + "ORDER BY kode_masalah");
                try {
                    ps.setString(1, TNoRw.getText());
                    rs = ps.executeQuery();
                    while (rs.next()) {
                        String namaMasalah = (rs.getString("nama_masalah") == null ? "" : rs.getString("nama_masalah")) + " (Ranap)";
                        tabModeDetailMasalah.addRow(new Object[]{
                            (rs.getString("kode_masalah") == null ? "" : rs.getString("kode_masalah")),
                            namaMasalah,
                            (rs.getString("waktu_eval") == null ? "" : rs.getString("waktu_eval")),
                            (rs.getString("keterangan") == null ? "BELUM TERATASI" : rs.getString("keterangan")),
                            (rs.getString("nip") == null ? "" : rs.getString("nip")),
                            (rs.getString("tanggal") == null ? "" : rs.getString("tanggal")),
                            (rs.getString("nipeval") == null ? "" : rs.getString("nipeval")),
                            (rs.getString("tanggal_eval") == null ? "" : rs.getString("tanggal_eval"))
                        });
                    }
                } finally {
                    if (rs != null) {
                        rs.close();
                    }
                    if (ps != null) {
                        ps.close();
                    }
                }
            }

            updateFilterCount();

        } catch (Exception e) {
            System.out.println("Notif : " + e);
            e.printStackTrace();
        }
    }

    private void tampildiagkep() {
        isMemuatDiagKep = true;
        try {
            WarnaTableSDKI.loadCustomWarna();
            Valid.tabelKosong(tabModeDiagKep);
            String searchText = TCariSkala1.getText().trim();
            String noRawat = TNoRw.getText().trim();

            if (tbMasalahDetailMasalah.getRowCount() == 0 || tbMasalahDetailMasalah.getSelectedRow() == -1) {
                return;
            }

            String kodeMasalah = tbMasalahDetailMasalah.getValueAt(tbMasalahDetailMasalah.getSelectedRow(), 0).toString().trim();
            if (kodeMasalah.isEmpty()) {
                return;
            }

            // ✅ PERBAIKAN: Ambil data tersimpan beserta item-nya dari database
            Set<String> dataTersimpan = new HashSet<>();
            Map<String, String> itemTersimpan = new HashMap<>();

            try {
                String querySimpan = "SELECT kode_diagkep, item FROM data_detail_diagnosa_kep WHERE no_rawat = ? AND kode_masalah = ?";
                try ( PreparedStatement psSimpan = koneksi.prepareStatement(querySimpan)) {
                    psSimpan.setString(1, noRawat);
                    psSimpan.setString(2, kodeMasalah);
                    try ( ResultSet rsSimpan = psSimpan.executeQuery()) {
                        while (rsSimpan.next()) {
                            String kode = rsSimpan.getString("kode_diagkep");
                            String item = rsSimpan.getString("item");
                            if (kode != null && !kode.trim().isEmpty()) {
                                String cleanKode = kode.replaceAll("\\u00A0", "").trim();
                                dataTersimpan.add(cleanKode);
                                itemTersimpan.put(cleanKode, item == null ? "" : item);

                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error load data tersimpan: " + e);
                e.printStackTrace();
            }

            String query;
            if (searchText.equals("")) {
                query = "select *, kode_rencana AS kode_diagkep from master_diagnosa_keperawatan "
                        + "where Kategori='SDKI' and kode_masalah = ? "
                        + "order by master_diagnosa_keperawatan.masalah_keperawatan asc";
            } else {
                query = "select *, kode_rencana AS kode_diagkep from master_diagnosa_keperawatan "
                        + "where Kategori='SDKI' and kode_masalah = ? AND ("
                        + "kategori LIKE ? OR entitas LIKE ? OR sub_entitas LIKE ? OR item LIKE ?) "
                        + "order by master_diagnosa_keperawatan.masalah_keperawatan asc";
            }

            try ( PreparedStatement ps = koneksi.prepareStatement(query)) {
                String kodemasalahKeperawatan = tbMasalahDetailMasalah.getValueAt(tbMasalahDetailMasalah.getSelectedRow(), 0).toString();
                ps.setString(1, kodemasalahKeperawatan);

                if (!searchText.isEmpty()) {
                    String pattern = "%" + searchText + "%";
                    ps.setString(2, pattern);
                    ps.setString(3, pattern);
                    ps.setString(4, pattern);
                    ps.setString(5, pattern);
                }

                try ( ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String kodeRencana = rs.getString("kode_rencana");
                        if (kodeRencana != null) kodeRencana = kodeRencana.replaceAll("\\u00A0", "").trim();
                        boolean isChecked = dataTersimpan.contains(kodeRencana);

                        // ✅ PERBAIKAN: Ambil item dari database jika sudah tersimpan, kalau tidak dari master
                        String itemValue = "";
                        if (isChecked && itemTersimpan.containsKey(kodeRencana)) {
                            // Jika sudah tersimpan, gunakan item dari database
                            itemValue = itemTersimpan.get(kodeRencana);
                        } else {
                            // Jika belum tersimpan, gunakan item dari master
                            itemValue = rs.getString("item");
                        }

                        tabModeDiagKep.addRow(new Object[]{
                            isChecked,
                            rs.getString("kode"),
                            rs.getString("masalah_keperawatan"),
                            rs.getString("kategori"),
                            rs.getString("entitas"),
                            rs.getString("sub_entitas"),
                            itemValue,
                            kodeRencana
                        });
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi: " + e);
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Gagal memuat data diagnosa: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            isMemuatDiagKep = false;
        }
    }

    /**
     * Auto-save item diagnosa keperawatan SDKI ke data_detail_diagnosa_kep
     * saat user selesai mengetik di in-place cell editor (kolom 6) atau fokus berpindah.
     */
    private void autoSaveDiagnosaKepItem(int row) {
        if (row < 0 || row >= tabModeDiagKep.getRowCount()) {
            return;
        }

        String noRawat = TNoRw.getText().trim();
        if (noRawat.isEmpty()) {
            return;
        }

        int selectedMasalahRow = tbMasalahDetailMasalah.getSelectedRow();
        if (selectedMasalahRow < 0) {
            return;
        }

        Object objKodeMasalah = tbMasalahDetailMasalah.getValueAt(selectedMasalahRow, 0);
        if (objKodeMasalah == null) {
            return;
        }
        String kodeMasalah = objKodeMasalah.toString().trim();
        if (kodeMasalah.isEmpty()) {
            return;
        }

        Object objKodeDiagkep = tabModeDiagKep.getValueAt(row, 7); // Kd Rencana (kode_diagkep)
        if (objKodeDiagkep == null) {
            return;
        }
        String kodeDiagkep = objKodeDiagkep.toString().replaceAll("\\u00A0", "").trim();
        if (kodeDiagkep.isEmpty()) {
            return;
        }

        Object objItem = tabModeDiagKep.getValueAt(row, 6);
        String item = (objItem == null) ? "" : objItem.toString().trim();
        if (item.length() > 255) {
            item = item.substring(0, 255);
        }

        isAutoSavingDiagKep = true;
        try {
            // Jika kolom P (checkbox) belum terceklis, otomatis aktifkan agar selaras dengan data tersimpan
            Object objP = tabModeDiagKep.getValueAt(row, 0);
            boolean isChecked = (objP instanceof Boolean) ? (Boolean) objP : false;
            if (!isChecked) {
                tabModeDiagKep.setValueAt(true, row, 0);
            }

            // Simpan / update ke database secara idempotent
            String sql = "INSERT INTO data_detail_diagnosa_kep (no_rawat, kode_diagkep, kode_masalah, item) "
                    + "VALUES (?, ?, ?, ?) "
                    + "ON DUPLICATE KEY UPDATE item = VALUES(item)";
            try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                ps.setString(1, noRawat);
                ps.setString(2, kodeDiagkep);
                ps.setString(3, kodeMasalah);
                ps.setString(4, item);
                ps.executeUpdate();
            }
        } catch (Exception ex) {
            System.out.println("Error auto-save detail diagnosa kep: " + ex);
        } finally {
            isAutoSavingDiagKep = false;
        }
    }

    private void tampilintervensi() {
        try {
            WarnaTableSDKI.loadCustomWarna();
            Valid.tabelKosong(tabModeIntervensi);
            String searchText = TCariSkala1.getText().trim();
            String noRawat = TNoRw.getText().trim();
            if (tbMasalahDetailMasalah.getRowCount() == 0 || tbMasalahDetailMasalah.getSelectedRow() == -1) {
                return;
            }
            String kodeMasalah = tbMasalahDetailMasalah.getValueAt(tbMasalahDetailMasalah.getSelectedRow(), 0).toString().trim();
            if (kodeMasalah.isEmpty()) {
                return;
            }

            Set<String> dataTersimpan = new HashSet<>();
            try {
                String querySimpan = "SELECT kode_slki FROM data_detail_slki WHERE no_rawat = ? AND kode_masalah = ?";
                try ( PreparedStatement psSimpan = koneksi.prepareStatement(querySimpan)) {
                    psSimpan.setString(1, noRawat);
                    psSimpan.setString(2, kodeMasalah);
                    try ( ResultSet rsSimpan = psSimpan.executeQuery()) {
                        while (rsSimpan.next()) {
                            String kode = rsSimpan.getString("kode_slki");
                            if (kode != null) dataTersimpan.add(kode.replaceAll("\\u00A0", "").trim());
                        }
                    }
                }
                } catch (Exception e) {
                    System.out.println("Error load data tersimpan (SLKI): " + e);
                    e.printStackTrace();
                }

                String query;
                if (searchText.equals("")) {
                    query = "select * from master_diagnosa_keperawatan "
                            + "where Kategori='SLKI' and kode_masalah = ? "
                            + "order by master_diagnosa_keperawatan.entitas desc,master_diagnosa_keperawatan.masalah_keperawatan asc";
                } else {
                    query = "select * from master_diagnosa_keperawatan "
                            + "where Kategori='SLKI' and kode_masalah = ? AND ("
                            + "kategori LIKE ? OR entitas LIKE ? OR sub_entitas LIKE ? OR item LIKE ?) "
                            + "order by master_diagnosa_keperawatan.entitas desc,master_diagnosa_keperawatan.masalah_keperawatan asc";
                }

                try ( PreparedStatement ps = koneksi.prepareStatement(query)) {
                    String masalahKeperawatan = tbMasalahDetailMasalah.getValueAt(tbMasalahDetailMasalah.getSelectedRow(), 0).toString();
                    ps.setString(1, masalahKeperawatan);
                    if (!searchText.isEmpty()) {
                        String pattern = "%" + searchText + "%";
                        ps.setString(2, pattern);
                        ps.setString(3, pattern);
                        ps.setString(4, pattern);
                        ps.setString(5, pattern);
                    }

                    try ( ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String kodeRencana = rs.getString("kode_rencana");
                            if (kodeRencana != null) kodeRencana = kodeRencana.replaceAll("\\u00A0", "").trim();
                            boolean isChecked = dataTersimpan.contains(kodeRencana);

                            tabModeIntervensi.addRow(new Object[]{
                                isChecked,
                                rs.getString("kode"),
                                rs.getString("masalah_keperawatan"),
                                rs.getString("kategori"),
                                rs.getString("entitas"),
                                rs.getString("sub_entitas"),
                                rs.getString("item"),
                                kodeRencana
                            });
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println("Notifikasi SLKI: " + e);
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Gagal memuat data intervensi: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
    }

    private void tampilevaluasi() {
        try {
            WarnaTableSDKI.loadCustomWarna();
            Valid.tabelKosong(tabModeEvaluasi);
            String searchText = TCariSkala1.getText().trim();
            String noRawat = TNoRw.getText().trim();
            if (tbMasalahDetailMasalah.getRowCount() == 0 || tbMasalahDetailMasalah.getSelectedRow() == -1) {
                return;
            }
            String kodeMasalah = tbMasalahDetailMasalah.getValueAt(tbMasalahDetailMasalah.getSelectedRow(), 0).toString().trim();
            if (kodeMasalah.isEmpty()) {
                return;
            }

            Set<String> dataTersimpan = new HashSet<>();
            try {
                String querySimpan = "SELECT kode_siki FROM data_detail_siki WHERE no_rawat = ? AND kode_masalah = ?";
                try ( PreparedStatement psSimpan = koneksi.prepareStatement(querySimpan)) {
                    psSimpan.setString(1, noRawat);
                    psSimpan.setString(2, kodeMasalah);
                    try ( ResultSet rsSimpan = psSimpan.executeQuery()) {
                        while (rsSimpan.next()) {
                            String kode = rsSimpan.getString("kode_siki");
                            if (kode != null) dataTersimpan.add(kode.replaceAll("\\u00A0", "").trim());
                        }
                    }
                }
                } catch (Exception e) {
                    System.out.println("Error load data tersimpan (SIKI): " + e);
                    e.printStackTrace();
                }

                String query;
                if (searchText.equals("")) {
                    query = "SELECT * FROM master_diagnosa_keperawatan "
                            + "WHERE Kategori='SIKI' AND kode_masalah = ? "
                            + "ORDER BY master_diagnosa_keperawatan.entitas desc,master_diagnosa_keperawatan.masalah_keperawatan ASC";
                } else {
                    query = "SELECT * FROM master_diagnosa_keperawatan "
                            + "WHERE Kategori='SIKI' AND kode_masalah = ? AND ("
                            + "kategori LIKE ? OR entitas LIKE ? OR sub_entitas LIKE ? OR item LIKE ?) "
                            + "ORDER BY master_diagnosa_keperawatan.entitas desc,master_diagnosa_keperawatan.masalah_keperawatan ASC";
                }

                try ( PreparedStatement ps = koneksi.prepareStatement(query)) {
                    String masalahKeperawatan = tbMasalahDetailMasalah.getValueAt(tbMasalahDetailMasalah.getSelectedRow(), 0).toString();
                    ps.setString(1, masalahKeperawatan);
                    if (!searchText.isEmpty()) {
                        String pattern = "%" + searchText + "%";
                        ps.setString(2, pattern);
                        ps.setString(3, pattern);
                        ps.setString(4, pattern);
                        ps.setString(5, pattern);
                    }

                    try ( ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String kodeRencana = rs.getString("kode_rencana");
                            if (kodeRencana != null) kodeRencana = kodeRencana.replaceAll("\\u00A0", "").trim();
                            boolean isChecked = dataTersimpan.contains(kodeRencana);

                            tabModeEvaluasi.addRow(new Object[]{
                                isChecked,
                                rs.getString("kode"),
                                rs.getString("masalah_keperawatan"),
                                rs.getString("kategori"),
                                rs.getString("entitas"),
                                rs.getString("sub_entitas"),
                                rs.getString("item"),
                                kodeRencana
                            });
                        }
                    }
                }

            } catch (Exception e) {
                System.out.println("Notifikasi SIKI: " + e);
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Gagal memuat data evaluasi: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
    }
    // METHOD PUBLIC - UNTUK DIPANGGIL DARI FORM LAIN

    /**
     * Method untuk membuka dialog diagnosa keperawatan dari form lain
     *
     * @param noRawat - Nomor rawat inap pasien
     * @param norm - Nomor rekam medis pasien
     * @param namaPasien - Nama pasien
     */
    public void isCek(String noRawat, String norm, String namaPasien) {
        // Set data pasien
        TNoRw.setText(noRawat);
        TNoRM.setText(norm);
        TPasien.setText(namaPasien);

        // Load data tambahan menggunakan method yang sudah ada
        isPsien();

        // Tampilkan data diagnosa
        getMasalah();
        resetFilterStatus();
        updateDefaultKategoriInput();
        tampildiagkep();
        tampilintervensi();
        tampilevaluasi();

        // Set dialog visible
        this.setVisible(true);
    }

    /**
     * Method overload - jika hanya perlu nomor rawat Data pasien akan di-load
     * otomatis
     *
     * @param noRawat - Nomor rawat inap pasien
     */
    public void isCek(String noRawat) {
        // Set nomor rawat
        TNoRw.setText(noRawat);

        // Load data pasien menggunakan method yang sudah ada
        isPsien();

        // Tampilkan data diagnosa
        getMasalah();
        resetFilterStatus();
        updateDefaultKategoriInput();
        tampildiagkep();
        tampilintervensi();
        tampilevaluasi();

        // Set dialog visible
        this.setVisible(true);
    }

    /**
     * Method untuk mengatur data pasien sebelum membuka dialog
     *
     * @param noRawat - Nomor rawat
     * @param noRM - Nomor rekam medis
     * @param namaPasien - Nama pasien
     */
    public void setData(String noRawat, String noRM, String namaPasien) {
        TNoRw.setText(noRawat);
        TNoRM.setText(noRM);
        TPasien.setText(namaPasien);
        updateDefaultKategoriInput();
    }

    /**
     * Method untuk menampilkan dialog setelah data di-set
     */
    public void tampilDialog() {
        isPsien();
        tampildiagkep();
        this.setVisible(true);
    }

    /**
     * Method untuk menutup dialog
     */
    public void tutup() {
        this.dispose();
    }

    public Object getTable() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    public JTable getTabelSDKI() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void tampil() {
        Valid.tabelKosong(tabModeDK);
        try {
            String keyword = "%" + TCari.getText().trim() + "%";
            ps = koneksi.prepareStatement(
                    "SELECT "
                    + "master_masalah_keperawatan.kode_masalah, "
                    + "master_masalah_keperawatan.nama_masalah, "
                    + "master_masalah_keperawatan.definisi "
                    + "FROM master_masalah_keperawatan "
                    + "WHERE master_masalah_keperawatan.nama_masalah LIKE ? "
                    + "OR master_masalah_keperawatan.definisi LIKE ? "
                    + "ORDER BY master_masalah_keperawatan.kode_masalah "
                    + "LIMIT 1000");

            try {
                ps.setString(1, keyword);
                ps.setString(2, keyword);

                rs = ps.executeQuery();
                while (rs.next()) {
                    tabModeDK.addRow(new Object[]{
                        false,
                        rs.getString("kode_masalah"),
                        rs.getString("nama_masalah"),
                        rs.getString("definisi")
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
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

    // ── HELPER FITUR FILTER STATUS MASALAH (TAHAP 4) ─────────────────────────

    private void initFilterStatus() {
        sorterDetailMasalah = new javax.swing.table.TableRowSorter<>(tabModeDetailMasalah);
        tbMasalahDetailMasalah.setRowSorter(sorterDetailMasalah);

        // Nonaktifkan sorting via klik header kolom agar urutan klinis tidak berubah tanpa sengaja
        for (int col = 0; col < tabModeDetailMasalah.getColumnCount(); col++) {
            sorterDetailMasalah.setSortable(col, false);
        }

        // Pasang renderer khusus pada kolom Status (index 3)
        tbMasalahDetailMasalah.getColumnModel().getColumn(3).setCellRenderer(new StatusTableCellRenderer());

        // Buat panel kontrol filter di atas tabel tbMasalahDetailMasalah
        widget.panelisi panelFilter = new widget.panelisi();
        panelFilter.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 4));
        panelFilter.setPreferredSize(new Dimension(318, 33));

        widget.Label lblStatus = new widget.Label();
        lblStatus.setText("Status :");
        lblStatus.setPreferredSize(new Dimension(45, 23));
        panelFilter.add(lblStatus);

        cmbFilterStatus = new widget.ComboBox();
        cmbFilterStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
            "Semua", "Teratasi", "Belum Teratasi"
        }));
        cmbFilterStatus.setSelectedIndex(2); // Default: "Belum Teratasi"
        cmbFilterStatus.setPreferredSize(new Dimension(115, 23));
        cmbFilterStatus.addActionListener(e -> {
            if (!isUpdatingFilter) {
                applyFilterStatus(cmbFilterStatus.getSelectedIndex());
            }
        });
        panelFilter.add(cmbFilterStatus);

        lblCountStatus = new widget.Label();
        lblCountStatus.setText("Menampilkan 0 dari 0 masalah");
        lblCountStatus.setPreferredSize(new Dimension(180, 23));
        lblCountStatus.setForeground(new Color(50, 50, 50));
        panelFilter.add(lblCountStatus);

        // Pasang panelFilter di atas Scroll17 tanpa mengubah blok GEN-BEGIN initComponents
        javax.swing.JPanel panelKiri = new javax.swing.JPanel(new java.awt.BorderLayout(1, 1));
        panelKiri.setOpaque(false);
        panelKiri.add(panelFilter, java.awt.BorderLayout.PAGE_START);

        jPanel1.remove(Scroll17);
        panelKiri.add(Scroll17, java.awt.BorderLayout.CENTER);
        jPanel1.add(panelKiri, 0);
        jPanel1.revalidate();
        jPanel1.repaint();

        // JPopupMenu Klik Kanan Filter
        PopupFilterStatus = new javax.swing.JPopupMenu();
        PopupFilterStatus.setName("PopupFilterStatus");

        menuFilterSemua = new javax.swing.JRadioButtonMenuItem("Semua");
        menuFilterTeratasi = new javax.swing.JRadioButtonMenuItem("Teratasi");
        menuFilterBelumTeratasi = new javax.swing.JRadioButtonMenuItem("Belum Teratasi");

        btnGroupFilter = new javax.swing.ButtonGroup();
        btnGroupFilter.add(menuFilterSemua);
        btnGroupFilter.add(menuFilterTeratasi);
        btnGroupFilter.add(menuFilterBelumTeratasi);

        menuFilterBelumTeratasi.setSelected(true);

        menuFilterSemua.addActionListener(e -> {
            if (!isUpdatingFilter) applyFilterStatus(0);
        });
        menuFilterTeratasi.addActionListener(e -> {
            if (!isUpdatingFilter) applyFilterStatus(1);
        });
        menuFilterBelumTeratasi.addActionListener(e -> {
            if (!isUpdatingFilter) applyFilterStatus(2);
        });

        PopupFilterStatus.add(menuFilterSemua);
        PopupFilterStatus.add(menuFilterTeratasi);
        PopupFilterStatus.add(menuFilterBelumTeratasi);

        // Listener klik kanan pada tbMasalahDetailMasalah
        tbMasalahDetailMasalah.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tampilkanPopupFilter(evt);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tampilkanPopupFilter(evt);
            }

            private void tampilkanPopupFilter(java.awt.event.MouseEvent evt) {
                if (evt.isPopupTrigger()) {
                    int row = tbMasalahDetailMasalah.rowAtPoint(evt.getPoint());
                    if (row >= 0 && row < tbMasalahDetailMasalah.getRowCount()) {
                        tbMasalahDetailMasalah.setRowSelectionInterval(row, row);
                        tampildiagkep();
                        tampilintervensi();
                        tampilevaluasi();
                    }
                    PopupFilterStatus.show(evt.getComponent(), evt.getX(), evt.getY());
                }
            }
        });

        applyFilterStatus(2);
    }

    private void applyFilterStatus(int filterIndex) {
        if (isUpdatingFilter) return;
        isUpdatingFilter = true;
        try {
            if (cmbFilterStatus != null && cmbFilterStatus.getSelectedIndex() != filterIndex) {
                cmbFilterStatus.setSelectedIndex(filterIndex);
            }

            if (sorterDetailMasalah != null) {
                if (filterIndex == 0) {
                    if (menuFilterSemua != null) menuFilterSemua.setSelected(true);
                    sorterDetailMasalah.setRowFilter(null);
                } else if (filterIndex == 1) {
                    if (menuFilterTeratasi != null) menuFilterTeratasi.setSelected(true);
                    sorterDetailMasalah.setRowFilter(new javax.swing.RowFilter<DefaultTableModel, Integer>() {
                        @Override
                        public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                            Object val = entry.getValue(3);
                            String s = (val == null) ? "" : val.toString().trim();
                            return !s.isEmpty() && !"BELUM TERATASI".equalsIgnoreCase(s);
                        }
                    });
                } else if (filterIndex == 2) {
                    if (menuFilterBelumTeratasi != null) menuFilterBelumTeratasi.setSelected(true);
                    sorterDetailMasalah.setRowFilter(new javax.swing.RowFilter<DefaultTableModel, Integer>() {
                        @Override
                        public boolean include(javax.swing.RowFilter.Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                            Object val = entry.getValue(3);
                            String s = (val == null) ? "" : val.toString().trim();
                            return s.isEmpty() || "BELUM TERATASI".equalsIgnoreCase(s);
                        }
                    });
                }
            }

            updateFilterCount();

            if (tbMasalahDetailMasalah.getSelectedRow() == -1 || tbMasalahDetailMasalah.getRowCount() == 0) {
                kosongkanDetailDiagIntervEval();
            } else {
                tampildiagkep();
                tampilintervensi();
                tampilevaluasi();
            }
        } finally {
            isUpdatingFilter = false;
        }
    }

    private void updateFilterCount() {
        int visibleCount = tbMasalahDetailMasalah.getRowCount();
        int totalCount = tabModeDetailMasalah.getRowCount();
        if (lblCountStatus != null) {
            lblCountStatus.setText("Menampilkan " + visibleCount + " dari " + totalCount + " masalah");
        }
    }

    private void kosongkanDetailDiagIntervEval() {
        if (tabModeDiagKep != null) Valid.tabelKosong(tabModeDiagKep);
        if (tabModeIntervensi != null) Valid.tabelKosong(tabModeIntervensi);
        if (tabModeEvaluasi != null) Valid.tabelKosong(tabModeEvaluasi);
    }

    public void resetFilterStatus() {
        if (cmbFilterStatus != null) {
            applyFilterStatus(2); // 2 = Belum Teratasi
        }
    }

    // ── HELPER FITUR KATEGORI INPUT MASALAH (PENDEKATAN B) ───────────────────

    private void initKategoriInput() {
        widget.panelisi panelKategori = new widget.panelisi();
        panelKategori.setName("panelKategoriInput");
        panelKategori.setPreferredSize(new Dimension(500, 30));
        panelKategori.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 3));

        widget.Label lblKategori = new widget.Label();
        lblKategori.setText("Kategori :");
        lblKategori.setPreferredSize(new Dimension(55, 23));
        panelKategori.add(lblKategori);

        rbRanap = new widget.RadioButton();
        rbRanap.setText("Rawat Inap (Ranap)");
        rbRanap.setPreferredSize(new Dimension(145, 23));

        rbRalan = new widget.RadioButton();
        rbRalan.setText("Rawat Jalan (Ralan)");
        rbRalan.setPreferredSize(new Dimension(145, 23));

        bgKategoriInput = new javax.swing.ButtonGroup();
        bgKategoriInput.add(rbRanap);
        bgKategoriInput.add(rbRalan);

        rbRanap.setSelected(true);

        panelKategori.add(rbRanap);
        panelKategori.add(rbRalan);

        jPanel4.add(panelKategori, java.awt.BorderLayout.PAGE_START);
        jPanel4.revalidate();
        jPanel4.repaint();

        updateDefaultKategoriInput();
    }

    public void updateDefaultKategoriInput() {
        if (rbRanap == null || rbRalan == null) return;
        String rw = TNoRw.getText().trim();
        if (rw.isEmpty()) return;

        String statusLanjut = Sequel.cariIsi("SELECT status_lanjut FROM reg_periksa WHERE no_rawat='" + rw + "'");
        boolean adaKamarInap = Sequel.cariInteger("SELECT COUNT(*) FROM kamar_inap WHERE no_rawat='" + rw + "'") > 0;
        boolean terdeteksiRanap = "Ranap".equalsIgnoreCase(statusLanjut) || adaKamarInap;

        if (terdeteksiRanap) {
            rbRanap.setSelected(true);
        } else {
            rbRalan.setSelected(true);
        }
    }

    private class StatusTableCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component component = super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);

            String valStr = (value == null) ? "" : value.toString().trim();
            boolean isBelum = valStr.isEmpty() || "BELUM TERATASI".equalsIgnoreCase(valStr);

            if (isSelected) {
                component.setBackground(table.getSelectionBackground());
                if (isBelum) {
                    component.setForeground(new Color(255, 120, 120));
                } else {
                    component.setForeground(new Color(130, 255, 130));
                }
                return component;
            }

            if (row % 2 == 1) {
                component.setBackground(new Color(195, 226, 230));
            } else {
                component.setBackground(new Color(255, 255, 255));
            }

            if (isBelum) {
                component.setForeground(new Color(190, 30, 30));
            } else {
                component.setForeground(new Color(0, 128, 0));
            }

            return component;
        }
    }

}
