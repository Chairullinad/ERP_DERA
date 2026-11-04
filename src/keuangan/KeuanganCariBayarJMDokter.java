package keuangan;

import com.itextpdf.html2pdf.HtmlConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;

public class KeuanganCariBayarJMDokter extends javax.swing.JDialog {

    private final DefaultTableModel tabMode;
    private DefaultTableModel tabModeRekap;
    private widget.Table tbRekapDokter;
    private widget.ScrollPane scrollPaneRekap;
    private javax.swing.JTabbedPane tabPane;
    private widget.Button BtnExport;
    private double totalRekapJM = 0, totalRekapSharing = 0, totalRekapNett = 0;
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private Connection koneksi = koneksiDB.condb();
    public DlgCariDokter dokter = new DlgCariDokter(null, false);
    private PreparedStatement ps, ps2, psrekening;
    private ResultSet rs, rs2, rsrekening;
    private String notagihan = "", tanggal = "", petugas = "";
    private double totaltagihan = 0;
    private boolean sukses = true, rincian = false;
    public Jurnal jur = new Jurnal();
    public File file;
    public FileWriter fileWriter;
    public String iyem, Beban_Jasa_Medik_Dokter_Tindakan_Ralan = "", Utang_Jasa_Medik_Dokter_Tindakan_Ralan = "", Beban_Jasa_Medik_Dokter_Tindakan_Ranap = "", Utang_Jasa_Medik_Dokter_Tindakan_Ranap = "",
            Beban_Jasa_Medik_Dokter_Laborat_Ralan = "", Utang_Jasa_Medik_Dokter_Laborat_Ralan = "", Beban_Jasa_Medik_Dokter_Laborat_Ranap = "", Utang_Jasa_Medik_Dokter_Laborat_Ranap = "",
            Beban_Jasa_Medik_Dokter_Radiologi_Ralan = "", Utang_Jasa_Medik_Dokter_Radiologi_Ralan = "", Beban_Jasa_Medik_Dokter_Radiologi_Ranap = "", Utang_Jasa_Medik_Dokter_Radiologi_Ranap = "",
            Beban_Jasa_Medik_Dokter_Operasi_Ralan = "", Utang_Jasa_Medik_Dokter_Operasi_Ralan = "", Beban_Jasa_Medik_Dokter_Operasi_Ranap = "", Utang_Jasa_Medik_Dokter_Operasi_Ranap = "",
            Bayar_JM_Dokter = Sequel.cariIsi("select set_akun.Bayar_JM_Dokter from set_akun"), koderekening = "";
    public ObjectMapper mapper = new ObjectMapper();
    public JsonNode root;
    public JsonNode response;
    public FileReader myObj;
    private int i = 0;

    /**
     * Creates new form DlgProgramStudi
     *
     * @param parent
     * @param modal
     */
    public KeuanganCariBayarJMDokter(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        Object[] row = {"Nomor J.M.", "Tanggal Bayar", "Akun Bayar", "Kode Dokter", "Nama Dokter", "Keterangan", "Total J.M."};
        tabMode = new DefaultTableModel(null, row) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }

            @Override
            public void addRow(Object[] rowData) {
                if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                    super.addRow(rowData);
                } else {
                    javax.swing.SwingUtilities.invokeLater(() -> super.addRow(rowData));
                }
            }

            @Override
            public void removeRow(int row) {
                if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                    super.removeRow(row);
                } else {
                    javax.swing.SwingUtilities.invokeLater(() -> super.removeRow(row));
                }
            }
        };
        tbDokter.setModel(tabMode);

        tbDokter.setPreferredScrollableViewportSize(new Dimension(800, 800));
        tbDokter.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 7; i++) {
            TableColumn column = tbDokter.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(100);
            } else if (i == 1) {
                column.setPreferredWidth(110);
            } else if (i == 2) {
                column.setPreferredWidth(210);
            } else if (i == 3) {
                column.setPreferredWidth(100);
            } else if (i == 4) {
                column.setPreferredWidth(150);
            } else if (i == 5) {
                column.setPreferredWidth(260);
            } else if (i == 6) {
                column.setPreferredWidth(100);
            }
        }
        tbDokter.setDefaultRenderer(Object.class, new WarnaTable());

        tbDokter.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    int row = tbDokter.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < tbDokter.getRowCount()) {
                        if (!tbDokter.isRowSelected(row)) {
                            tbDokter.setRowSelectionInterval(row, row);
                        }
                    }
                }
            }
        });

        // Setup TabbedPane untuk memisahkan Riwayat Bukti Pembayaran dan Rekap Per Dokter
        tabPane = new javax.swing.JTabbedPane();
        tabPane.setBackground(new java.awt.Color(255, 255, 254));
        tabPane.setFont(new java.awt.Font("Tahoma", 0, 11));
        tabPane.setForeground(new java.awt.Color(50, 50, 50));
        tabPane.setName("tabPane");

        // Model dan Tabel Rekap Dokter
        tabModeRekap = new DefaultTableModel(null, new Object[]{
            "No.", "Kode Dokter", "Nama Dokter", "Jasa Medis", "Sharing Income", "Nett Dokter", "% Sharing"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }

            @Override
            public void addRow(Object[] rowData) {
                if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                    super.addRow(rowData);
                } else {
                    javax.swing.SwingUtilities.invokeLater(() -> super.addRow(rowData));
                }
            }

            @Override
            public void removeRow(int row) {
                if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                    super.removeRow(row);
                } else {
                    javax.swing.SwingUtilities.invokeLater(() -> super.removeRow(row));
                }
            }
        };

        tbRekapDokter = new widget.Table();
        tbRekapDokter.setModel(tabModeRekap);
        tbRekapDokter.setPreferredScrollableViewportSize(new Dimension(800, 800));
        tbRekapDokter.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int k = 0; k < 7; k++) {
            TableColumn col = tbRekapDokter.getColumnModel().getColumn(k);
            if (k == 0) {
                col.setPreferredWidth(35);
            } else if (k == 1) {
                col.setPreferredWidth(85);
            } else if (k == 2) {
                col.setPreferredWidth(280);
            } else if (k == 3) {
                col.setPreferredWidth(125);
            } else if (k == 4) {
                col.setPreferredWidth(115);
            } else if (k == 5) {
                col.setPreferredWidth(125);
            } else if (k == 6) {
                col.setPreferredWidth(80);
            }
        }

        tbRekapDokter.setDefaultRenderer(Object.class, new WarnaTable() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 0 || column == 1 || column == 6) {
                    setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                } else if (column == 3 || column == 4 || column == 5) {
                    setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
                }
                return c;
            }
        });

        scrollPaneRekap = new widget.ScrollPane();
        scrollPaneRekap.setName("scrollPaneRekap");
        scrollPaneRekap.setOpaque(true);
        scrollPaneRekap.setViewportView(tbRekapDokter);

        // Pasang tab ke internalFrame1 menggantikan scrollPane1 langsung
        internalFrame1.remove(scrollPane1);
        tabPane.addTab("Riwayat Pembayaran", scrollPane1);
        tabPane.addTab("Rekap J.M. Per Dokter", scrollPaneRekap);
        internalFrame1.add(tabPane, java.awt.BorderLayout.CENTER);

        // Tombol Export Excel khusus untuk Tab Rekap J.M. Per Dokter
        BtnExport = new widget.Button();
        BtnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Export.png")));
        BtnExport.setMnemonic('X');
        BtnExport.setText("Export Excel");
        BtnExport.setToolTipText("Alt+X (Export Rekap ke Excel)");
        BtnExport.setName("BtnExport");
        BtnExport.setPreferredSize(new Dimension(120, 30));
        BtnExport.setVisible(false); // Default sembunyikan saat di Tab 1
        BtnExport.addActionListener(evt -> BtnExportActionPerformed(evt));
        BtnExport.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                    BtnExportActionPerformed(null);
                }
            }
        });

        // Sisipkan ke panelisi1 setelah BtnPrint, sebelum BtnKeluar
        int idxPrint = -1;
        for (int c = 0; c < panelisi1.getComponentCount(); c++) {
            if (panelisi1.getComponent(c) == BtnPrint) {
                idxPrint = c;
                break;
            }
        }
        if (idxPrint != -1) {
            panelisi1.add(BtnExport, idxPrint + 1);
        } else {
            panelisi1.add(BtnExport);
        }

        tabPane.addChangeListener(e -> {
            if (tabPane.getSelectedIndex() == 0) {
                LTotal.setText(Valid.SetAngka(totaltagihan));
                BtnHapus.setEnabled(akses.getbayar_jm_dokter());
                if (BtnExport != null) {
                    BtnExport.setVisible(false);
                }
            } else {
                LTotal.setText(Valid.SetAngka(totalRekapNett));
                BtnHapus.setEnabled(false);
                if (BtnExport != null) {
                    BtnExport.setVisible(true);
                }
            }
        });

        NoBayar.setDocument(new batasInput((byte) 20).getKata(NoBayar));
        kddokter.setDocument(new batasInput((byte) 20).getKata(kddokter));

        dokter.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (akses.getform().equals("KeuanganCariBayarJMDokter")) {
                    if (dokter.getTable().getSelectedRow() != -1) {
                        kddokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 0).toString());
                        nmdokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 1).toString());
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
                dokter.emptTeks();
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
            }
        });

        try {
            psrekening = koneksi.prepareStatement(
                    "select set_akun_ralan.Beban_Jasa_Medik_Dokter_Tindakan_Ralan,set_akun_ralan.Utang_Jasa_Medik_Dokter_Tindakan_Ralan,"
                    + "set_akun_ralan.Beban_Jasa_Medik_Dokter_Laborat_Ralan,set_akun_ralan.Utang_Jasa_Medik_Dokter_Laborat_Ralan,"
                    + "set_akun_ralan.Beban_Jasa_Medik_Dokter_Radiologi_Ralan,set_akun_ralan.Utang_Jasa_Medik_Dokter_Radiologi_Ralan,"
                    + "set_akun_ralan.Beban_Jasa_Medik_Dokter_Operasi_Ralan,set_akun_ralan.Utang_Jasa_Medik_Dokter_Operasi_Ralan from set_akun_ralan");
            try {
                rsrekening = psrekening.executeQuery();
                while (rsrekening.next()) {
                    Beban_Jasa_Medik_Dokter_Tindakan_Ralan = rsrekening.getString("Beban_Jasa_Medik_Dokter_Tindakan_Ralan");
                    Utang_Jasa_Medik_Dokter_Tindakan_Ralan = rsrekening.getString("Utang_Jasa_Medik_Dokter_Tindakan_Ralan");
                    Beban_Jasa_Medik_Dokter_Laborat_Ralan = rsrekening.getString("Beban_Jasa_Medik_Dokter_Laborat_Ralan");
                    Utang_Jasa_Medik_Dokter_Laborat_Ralan = rsrekening.getString("Utang_Jasa_Medik_Dokter_Laborat_Ralan");
                    Beban_Jasa_Medik_Dokter_Radiologi_Ralan = rsrekening.getString("Beban_Jasa_Medik_Dokter_Radiologi_Ralan");
                    Utang_Jasa_Medik_Dokter_Radiologi_Ralan = rsrekening.getString("Utang_Jasa_Medik_Dokter_Radiologi_Ralan");
                    Beban_Jasa_Medik_Dokter_Operasi_Ralan = rsrekening.getString("Beban_Jasa_Medik_Dokter_Operasi_Ralan");
                    Utang_Jasa_Medik_Dokter_Operasi_Ralan = rsrekening.getString("Utang_Jasa_Medik_Dokter_Operasi_Ralan");
                }
            } catch (Exception e) {
                System.out.println("Notif Rekening : " + e);
            } finally {
                if (rsrekening != null) {
                    rsrekening.close();
                }
                if (psrekening != null) {
                    psrekening.close();
                }
            }

            psrekening = koneksi.prepareStatement(
                    "select set_akun_ranap.Beban_Jasa_Medik_Dokter_Tindakan_Ranap,set_akun_ranap.Utang_Jasa_Medik_Dokter_Tindakan_Ranap,"
                    + "set_akun_ranap.Beban_Jasa_Medik_Dokter_Laborat_Ranap,set_akun_ranap.Utang_Jasa_Medik_Dokter_Laborat_Ranap,"
                    + "set_akun_ranap.Beban_Jasa_Medik_Dokter_Radiologi_Ranap,set_akun_ranap.Utang_Jasa_Medik_Dokter_Radiologi_Ranap,"
                    + "set_akun_ranap.Beban_Jasa_Medik_Dokter_Operasi_Ranap,set_akun_ranap.Utang_Jasa_Medik_Dokter_Operasi_Ranap from set_akun_ranap");
            try {
                rsrekening = psrekening.executeQuery();
                while (rsrekening.next()) {
                    Beban_Jasa_Medik_Dokter_Tindakan_Ranap = rsrekening.getString("Beban_Jasa_Medik_Dokter_Tindakan_Ranap");
                    Utang_Jasa_Medik_Dokter_Tindakan_Ranap = rsrekening.getString("Utang_Jasa_Medik_Dokter_Tindakan_Ranap");
                    Beban_Jasa_Medik_Dokter_Laborat_Ranap = rsrekening.getString("Beban_Jasa_Medik_Dokter_Laborat_Ranap");
                    Utang_Jasa_Medik_Dokter_Laborat_Ranap = rsrekening.getString("Utang_Jasa_Medik_Dokter_Laborat_Ranap");
                    Beban_Jasa_Medik_Dokter_Radiologi_Ranap = rsrekening.getString("Beban_Jasa_Medik_Dokter_Radiologi_Ranap");
                    Utang_Jasa_Medik_Dokter_Radiologi_Ranap = rsrekening.getString("Utang_Jasa_Medik_Dokter_Radiologi_Ranap");
                    Beban_Jasa_Medik_Dokter_Operasi_Ranap = rsrekening.getString("Beban_Jasa_Medik_Dokter_Operasi_Ranap");
                    Utang_Jasa_Medik_Dokter_Operasi_Ranap = rsrekening.getString("Utang_Jasa_Medik_Dokter_Operasi_Ranap");
                }
            } catch (Exception e) {
                System.out.println("Notif Rekening : " + e);
            } finally {
                if (rsrekening != null) {
                    rsrekening.close();
                }
                if (psrekening != null) {
                    psrekening.close();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        ChkInput.setSelected(false);
        isForm();
    }

    /**
     * This method is called from within the constructor to initialize the
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        ppRincian = new javax.swing.JMenuItem();
        ppRincian2 = new javax.swing.JMenuItem();
        ppSlipGajiPDF = new javax.swing.JMenuItem();
        internalFrame1 = new widget.InternalFrame();
        scrollPane1 = new widget.ScrollPane();
        tbDokter = new widget.Table();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        FormInput = new widget.panelisi();
        label15 = new widget.Label();
        NoBayar = new widget.TextBox();
        label16 = new widget.Label();
        kddokter = new widget.TextBox();
        nmdokter = new widget.TextBox();
        btnPegawai = new widget.Button();
        panelisi1 = new widget.panelisi();
        label11 = new widget.Label();
        Tanggal1 = new widget.Tanggal();
        label12 = new widget.Label();
        Tanggal2 = new widget.Tanggal();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        BtnGaji = new widget.Button();
        BtnHapus = new widget.Button();
        label9 = new widget.Label();
        LTotal = new widget.Label();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        ppRincian.setBackground(new java.awt.Color(255, 255, 254));
        ppRincian.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppRincian.setForeground(new java.awt.Color(50, 50, 50));
        ppRincian.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppRincian.setText("Sembunyikan Rincian Jasa Medis");
        ppRincian.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppRincian.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppRincian.setName("ppRincian"); // NOI18N
        ppRincian.setPreferredSize(new java.awt.Dimension(210, 26));
        ppRincian.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppRincianBtnPrintActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppRincian);

        ppRincian2.setBackground(new java.awt.Color(255, 255, 254));
        ppRincian2.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppRincian2.setForeground(new java.awt.Color(50, 50, 50));
        ppRincian2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppRincian2.setText("Tampilkan Rincian Jasa Medis");
        ppRincian2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppRincian2.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppRincian2.setName("ppRincian2"); // NOI18N
        ppRincian2.setPreferredSize(new java.awt.Dimension(210, 26));
        ppRincian2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppRincian2BtnPrintActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppRincian2);

        ppSlipGajiPDF.setBackground(new java.awt.Color(255, 255, 254));
        ppSlipGajiPDF.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppSlipGajiPDF.setForeground(new java.awt.Color(50, 50, 50));
        ppSlipGajiPDF.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        ppSlipGajiPDF.setText("Cetak Slip Gaji (PDF)");
        ppSlipGajiPDF.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppSlipGajiPDF.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppSlipGajiPDF.setName("ppSlipGajiPDF"); // NOI18N
        ppSlipGajiPDF.setPreferredSize(new java.awt.Dimension(210, 26));
        ppSlipGajiPDF.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppSlipGajiPDFActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppSlipGajiPDF);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Cari Bayar Jasa Medis Dokter ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        scrollPane1.setComponentPopupMenu(jPopupMenu1);
        scrollPane1.setName("scrollPane1"); // NOI18N
        scrollPane1.setOpaque(true);

        tbDokter.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbDokter.setToolTipText("Silahkan klik pada nomor tagihan untuk verifikasi pilihan");
        tbDokter.setComponentPopupMenu(jPopupMenu1);
        tbDokter.setName("tbDokter"); // NOI18N
        scrollPane1.setViewportView(tbDokter);

        internalFrame1.add(scrollPane1, java.awt.BorderLayout.CENTER);

        PanelInput.setBackground(new java.awt.Color(255, 255, 255));
        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 65));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('M');
        ChkInput.setText(".: Filter Data");
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
        PanelInput.add(ChkInput, java.awt.BorderLayout.PAGE_END);

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 74));
        FormInput.setLayout(null);

        label15.setText("Nomor J.M. :");
        label15.setName("label15"); // NOI18N
        label15.setPreferredSize(new java.awt.Dimension(60, 23));
        FormInput.add(label15);
        label15.setBounds(0, 10, 81, 23);

        NoBayar.setName("NoBayar"); // NOI18N
        NoBayar.setPreferredSize(new java.awt.Dimension(207, 23));
        NoBayar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NoBayarKeyPressed(evt);
            }
        });
        FormInput.add(NoBayar);
        NoBayar.setBounds(85, 10, 160, 23);

        label16.setText("Dokter :");
        label16.setName("label16"); // NOI18N
        label16.setPreferredSize(new java.awt.Dimension(60, 23));
        FormInput.add(label16);
        label16.setBounds(295, 10, 70, 23);

        kddokter.setEditable(false);
        kddokter.setName("kddokter"); // NOI18N
        kddokter.setPreferredSize(new java.awt.Dimension(80, 23));
        FormInput.add(kddokter);
        kddokter.setBounds(369, 10, 110, 23);

        nmdokter.setEditable(false);
        nmdokter.setName("nmdokter"); // NOI18N
        nmdokter.setPreferredSize(new java.awt.Dimension(207, 23));
        FormInput.add(nmdokter);
        nmdokter.setBounds(481, 10, 240, 23);

        btnPegawai.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPegawai.setMnemonic('1');
        btnPegawai.setToolTipText("Alt+1");
        btnPegawai.setName("btnPegawai"); // NOI18N
        btnPegawai.setPreferredSize(new java.awt.Dimension(28, 23));
        btnPegawai.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPegawaiActionPerformed(evt);
            }
        });
        FormInput.add(btnPegawai);
        btnPegawai.setBounds(724, 10, 28, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        panelisi1.setName("panelisi1"); // NOI18N
        panelisi1.setPreferredSize(new java.awt.Dimension(100, 56));
        panelisi1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        label11.setText("Tanggal :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(53, 23));
        panelisi1.add(label11);

        Tanggal1.setDisplayFormat("dd-MM-yyyy");
        Tanggal1.setName("Tanggal1"); // NOI18N
        Tanggal1.setPreferredSize(new java.awt.Dimension(90, 23));
        Tanggal1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Tanggal1KeyPressed(evt);
            }
        });
        panelisi1.add(Tanggal1);

        label12.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label12.setText("s.d.");
        label12.setName("label12"); // NOI18N
        label12.setPreferredSize(new java.awt.Dimension(25, 23));
        panelisi1.add(label12);

        Tanggal2.setDisplayFormat("dd-MM-yyyy");
        Tanggal2.setMinimumSize(new java.awt.Dimension(90, 23));
        Tanggal2.setName("Tanggal2"); // NOI18N
        Tanggal2.setPreferredSize(new java.awt.Dimension(90, 23));
        Tanggal2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Tanggal2KeyPressed(evt);
            }
        });
        panelisi1.add(Tanggal2);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('5');
        BtnCari.setToolTipText("Alt+5");
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
        panelisi1.add(BtnCari);

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
        panelisi1.add(BtnAll);

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
        panelisi1.add(BtnGaji);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus.setMnemonic('H');
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus"); // NOI18N
        BtnHapus.setPreferredSize(new java.awt.Dimension(28, 23));
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
        panelisi1.add(BtnHapus);

        label9.setText("Total :");
        label9.setName("label9"); // NOI18N
        label9.setPreferredSize(new java.awt.Dimension(40, 23));
        panelisi1.add(label9);

        LTotal.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LTotal.setText("0");
        LTotal.setName("LTotal"); // NOI18N
        LTotal.setPreferredSize(new java.awt.Dimension(105, 23));
        panelisi1.add(LTotal);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint"); // NOI18N
        BtnPrint.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintActionPerformed(evt);
            }
        });
        BtnPrint.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrintKeyPressed(evt);
            }
        });
        panelisi1.add(BtnPrint);

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

        internalFrame1.add(panelisi1, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dokter.dispose();
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            dispose();
        } else {
            Valid.pindah(evt, BtnPrint, BtnCari);
        }
}//GEN-LAST:event_BtnKeluarKeyPressed

    /*
private void KdKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TKdKeyPressed
    Valid.pindah(evt,BtnCari,Nm);
}//GEN-LAST:event_TKdKeyPressed
*/

    private void btnPegawaiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPegawaiActionPerformed
        akses.setform("KeuanganCariBayarJMDokter");
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setAlwaysOnTop(false);
        dokter.setVisible(true);
    }//GEN-LAST:event_btnPegawaiActionPerformed

    private void Tanggal1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tanggal1KeyPressed
        Valid.pindah(evt, NoBayar, kddokter);
    }//GEN-LAST:event_Tanggal1KeyPressed

    private void NoBayarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NoBayarKeyPressed
        Valid.pindah(evt, BtnKeluar, kddokter);
    }//GEN-LAST:event_NoBayarKeyPressed

    private void Tanggal2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tanggal2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Tanggal2KeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        tampil();
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnCariActionPerformed(null);
        } else {
            Valid.pindah(evt, btnPegawai, BtnAll);
        }
    }//GEN-LAST:event_BtnCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        NoBayar.setText("");
        kddokter.setText("");
        nmdokter.setText("");
        tampil();
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnAllActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnPrint, BtnKeluar);
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        if (tabPane != null && tabPane.getSelectedIndex() == 1) {
            if (tabModeRekap.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "Maaf, data rekap dokter masih kosong...!!!!");
            } else {
                try {
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    tbRekapDokter.print(JTable.PrintMode.FIT_WIDTH,
                            new java.text.MessageFormat("REKAPITULASI JASA MEDIS DOKTER (" + Tanggal1.getSelectedItem() + " s/d " + Tanggal2.getSelectedItem() + ")"),
                            new java.text.MessageFormat("Halaman {0}"));
                    this.setCursor(Cursor.getDefaultCursor());
                } catch (Exception e) {
                    System.out.println("Error print rekap dokter: " + e);
                }
            }
            return;
        }

        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnHapus.requestFocus();
        } else if (tabMode.getRowCount() != 0) {
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            Sequel.queryu("delete from temporary where temp37='" + akses.getalamatip() + "'");

            int row = tabMode.getRowCount();
            for (i = 0; i < row; i++) {
                Sequel.menyimpan("temporary", "'" + i + "','"
                        + tabMode.getValueAt(i, 0).toString() + "','"
                        + tabMode.getValueAt(i, 1).toString() + "','"
                        + tabMode.getValueAt(i, 2).toString() + "','"
                        + tabMode.getValueAt(i, 3).toString() + "','"
                        + tabMode.getValueAt(i, 4).toString() + "','"
                        + tabMode.getValueAt(i, 5).toString() + "','"
                        + tabMode.getValueAt(i, 6).toString() + "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','" + akses.getalamatip() + "'", "Transaksi Penagihan Piutang Pasien");
            }
            Sequel.menyimpan("temporary", "'" + i + "','','','TOTAL PEMBAYARAN :','','','','" + Valid.SetAngka(totaltagihan) + "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','" + akses.getalamatip() + "'", "Transaksi Penagihan Piutang Pasien");
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            Valid.MyReportqry("rptBayarJmDokter.jasper", "report", "::[ Data Rekap Pembayaran Jasa Medis Dokter ]::", "select * from temporary where temporary.temp37='" + akses.getalamatip() + "' order by temporary.no", param);
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnPrintActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnAll, BtnAll);
        }
    }//GEN-LAST:event_BtnPrintKeyPressed

    private void BtnExportActionPerformed(java.awt.event.ActionEvent evt) {
        if (tabModeRekap == null || tabModeRekap.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Data rekapitulasi jasa medis per dokter masih kosong...!");
            return;
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Simpan Export Excel Rekap Jasa Medis Dokter");
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xls)", "xls"));
            String defaultName = "Rekap_Jasa_Medis_Dokter_" + Valid.SetTgl(Tanggal1.getSelectedItem() + "").replaceAll("-", "") + "_sd_" + Valid.SetTgl(Tanggal2.getSelectedItem() + "").replaceAll("-", "") + ".xls";
            fileChooser.setSelectedFile(new java.io.File(defaultName));

            this.setCursor(Cursor.getDefaultCursor());
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection != javax.swing.JFileChooser.APPROVE_OPTION) {
                return;
            }

            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            java.io.File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".xls")) {
                file = new java.io.File(file.getParentFile(), file.getName() + ".xls");
            }

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html>\n<head>\n");
            html.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" />\n");
            html.append("<style>\n");
            html.append("  body { font-family: Tahoma, Arial, sans-serif; font-size: 11px; }\n");
            html.append("  .header { text-align: center; margin-bottom: 15px; }\n");
            html.append("  .title { font-size: 14px; font-weight: bold; }\n");
            html.append("  .subtitle { font-size: 11px; color: #555; }\n");
            html.append("  table { border-collapse: collapse; width: 100%; }\n");
            html.append("  th { background-color: #E6ECF5; border: 0.5pt solid #808080; padding: 6px; font-weight: bold; text-align: center; }\n");
            html.append("  td { border: 0.5pt solid #808080; padding: 5px; }\n");
            html.append("  .str { mso-number-format:\"\\@\"; text-align: center; }\n");
            html.append("  .num { mso-number-format:\"\\#\\,\\#\\#0\"; text-align: right; }\n");
            html.append("  .center { text-align: center; }\n");
            html.append("  .left { text-align: left; }\n");
            html.append("  .total { background-color: #F0F4F8; font-weight: bold; }\n");
            html.append("</style>\n");
            html.append("</head>\n<body>\n");

            html.append("<div class='header'>\n");
            html.append("  <div class='title'>").append(akses.getnamars()).append("</div>\n");
            html.append("  <div class='subtitle'>").append(akses.getalamatrs()).append(", ").append(akses.getkabupatenrs()).append(", ").append(akses.getpropinsirs()).append("</div>\n");
            html.append("  <div class='subtitle'>Kontak: ").append(akses.getkontakrs()).append(" | E-mail: ").append(akses.getemailrs()).append("</div><br/>\n");
            html.append("  <div class='title'>REKAPITULASI JASA MEDIS DOKTER</div>\n");
            html.append("  <div class='subtitle'>Periode: ").append(Tanggal1.getSelectedItem()).append(" s.d. ").append(Tanggal2.getSelectedItem()).append("</div>\n");
            html.append("</div>\n");

            html.append("<table>\n<thead>\n<tr>\n");
            html.append("  <th width='50'>No.</th>\n");
            html.append("  <th width='120'>Kode Dokter</th>\n");
            html.append("  <th width='300'>Nama Dokter</th>\n");
            html.append("  <th width='150'>Jasa Medis</th>\n");
            html.append("  <th width='150'>Sharing Income</th>\n");
            html.append("  <th width='150'>Nett Dokter</th>\n");
            html.append("  <th width='120'>% Sharing</th>\n");
            html.append("</tr>\n</thead>\n<tbody>\n");

            int rowCount = tabModeRekap.getRowCount();
            for (int r = 0; r < rowCount; r++) {
                String col0 = tabModeRekap.getValueAt(r, 0) == null ? "" : tabModeRekap.getValueAt(r, 0).toString().trim();
                String col1 = tabModeRekap.getValueAt(r, 1) == null ? "" : tabModeRekap.getValueAt(r, 1).toString().trim();
                String col2 = tabModeRekap.getValueAt(r, 2) == null ? "" : tabModeRekap.getValueAt(r, 2).toString().trim();
                String col3 = tabModeRekap.getValueAt(r, 3) == null ? "" : tabModeRekap.getValueAt(r, 3).toString().trim();
                String col4 = tabModeRekap.getValueAt(r, 4) == null ? "" : tabModeRekap.getValueAt(r, 4).toString().trim();
                String col5 = tabModeRekap.getValueAt(r, 5) == null ? "" : tabModeRekap.getValueAt(r, 5).toString().trim();
                String col6 = tabModeRekap.getValueAt(r, 6) == null ? "" : tabModeRekap.getValueAt(r, 6).toString().trim();

                boolean isTotalRow = col2.contains("TOTAL KESELURUHAN") || (col0.isEmpty() && col1.isEmpty());

                if (isTotalRow) {
                    html.append("<tr class='total'>\n");
                    html.append("  <td class='center' colspan='3'><b>").append(col2).append("</b></td>\n");
                    html.append("  <td class='num'><b>").append(cleanNumber(col3)).append("</b></td>\n");
                    html.append("  <td class='num'><b>").append(cleanNumber(col4)).append("</b></td>\n");
                    html.append("  <td class='num'><b>").append(cleanNumber(col5)).append("</b></td>\n");
                    html.append("  <td class='center'></td>\n");
                    html.append("</tr>\n");
                } else {
                    html.append("<tr>\n");
                    html.append("  <td class='center'>").append(col0).append("</td>\n");
                    html.append("  <td class='str'>").append(col1).append("</td>\n");
                    html.append("  <td class='left'>").append(col2).append("</td>\n");
                    html.append("  <td class='num'>").append(cleanNumber(col3)).append("</td>\n");
                    html.append("  <td class='num'>").append(cleanNumber(col4)).append("</td>\n");
                    html.append("  <td class='num'>").append(cleanNumber(col5)).append("</td>\n");
                    html.append("  <td class='center'>").append(col6).append("</td>\n");
                    html.append("</tr>\n");
                }
            }

            html.append("</tbody>\n</table>\n</body>\n</html>");

            try (java.io.OutputStreamWriter writer = new java.io.OutputStreamWriter(
                    new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write(html.toString());
                writer.flush();
            }

            JOptionPane.showMessageDialog(this,
                    "Data Rekap J.M. Per Dokter berhasil diexport ke:\n" + file.getAbsolutePath(),
                    "Export Excel Sukses", JOptionPane.INFORMATION_MESSAGE);

            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                    Desktop.getDesktop().open(file);
                } else {
                    Desktop.getDesktop().browse(file.toURI());
                }
            } catch (Exception ex) {
                System.out.println("Gagal membuka file otomatis: " + ex);
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Export Excel Rekap: " + e);
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat export excel: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private String cleanNumber(String val) {
        if (val == null || val.trim().isEmpty()) {
            return "0";
        }
        String cleaned = val.replace(".", "").replace(",", "").trim();
        try {
            long num = Long.parseLong(cleaned);
            return String.valueOf(num);
        } catch (Exception e) {
            try {
                double d = Double.parseDouble(cleaned);
                return String.valueOf(Math.round(d));
            } catch (Exception ex) {
                return val;
            }
        }
    }

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        tampil();
    }//GEN-LAST:event_formWindowOpened

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if (tabPane != null && tabPane.getSelectedIndex() == 1) {
            JOptionPane.showMessageDialog(null, "Pembatalan pembayaran hanya dapat dilakukan pada tab Riwayat Pembayaran!");
            return;
        }
        if (tbDokter.getSelectedRow() > -1) {
            if (tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString().trim().equals("")) {
                Valid.textKosong(BtnAll, "Nomor J.M.");
            } else {
                try {
                    ps = koneksi.prepareStatement(
                            "select bayar_jm_dokter.besar_bayar,bayar_jm_dokter.nama_bayar,bayar_jm_dokter.rawatjalan,bayar_jm_dokter.rawatinap,"
                            + "bayar_jm_dokter.labrawatjalan,bayar_jm_dokter.labrawatinap,bayar_jm_dokter.radrawatjalan,bayar_jm_dokter.radrawatinap,"
                            + "bayar_jm_dokter.operasiralan,bayar_jm_dokter.operasiranap from bayar_jm_dokter where bayar_jm_dokter.no_bayar=?");
                    try {
                        ps.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs = ps.executeQuery();
                        if (rs.next()) {
                            Sequel.AutoComitFalse();
                            sukses = true;

                            koderekening = "";
                            try {
                                myObj = new FileReader("./cache/akunbayarhutang.iyem");
                                root = mapper.readTree(myObj);
                                response = root.path("akunbayarhutang");
                                if (response.isArray()) {
                                    for (JsonNode list : response) {
                                        if (list.path("NamaAkun").asText().equals(rs.getString("nama_bayar"))) {
                                            koderekening = list.path("KodeRek").asText();
                                        }
                                    }
                                }
                                myObj.close();
                            } catch (Exception e) {
                                sukses = false;
                            }

                            if (sukses == true) {
                                Sequel.queryu("delete from tampjurnal");
                                if (rs.getDouble("rawatjalan") > 0) {
                                    Sequel.menyimpan("tampjurnal", "'" + Beban_Jasa_Medik_Dokter_Tindakan_Ralan + "','Beban Jasa Medik Dokter Tindakan Ralan','" + rs.getDouble("rawatjalan") + "','0'", "debet=debet+'" + rs.getDouble("rawatjalan") + "'", "kd_rek='" + Beban_Jasa_Medik_Dokter_Tindakan_Ralan + "'");
                                    Sequel.menyimpan("tampjurnal", "'" + Utang_Jasa_Medik_Dokter_Tindakan_Ralan + "','Utang Jasa Medik Dokter Tindakan Ralan','0','" + rs.getDouble("rawatjalan") + "'", "kredit=kredit+'" + rs.getDouble("rawatjalan") + "'", "kd_rek='" + Utang_Jasa_Medik_Dokter_Tindakan_Ralan + "'");
                                }
                                if (rs.getDouble("rawatinap") > 0) {
                                    Sequel.menyimpan("tampjurnal", "'" + Beban_Jasa_Medik_Dokter_Tindakan_Ranap + "','Beban Jasa Medik Dokter Tindakan Ranap','" + rs.getDouble("rawatinap") + "','0'", "debet=debet+'" + (rs.getDouble("rawatinap")) + "'", "kd_rek='" + Beban_Jasa_Medik_Dokter_Tindakan_Ranap + "'");
                                    Sequel.menyimpan("tampjurnal", "'" + Utang_Jasa_Medik_Dokter_Tindakan_Ranap + "','Utang Jasa Medik Dokter Tindakan Ranap','0','" + rs.getDouble("rawatinap") + "'", "kredit=kredit+'" + (rs.getDouble("rawatinap")) + "'", "kd_rek='" + Utang_Jasa_Medik_Dokter_Tindakan_Ranap + "'");
                                }
                                if (rs.getDouble("labrawatjalan") > 0) {
                                    Sequel.menyimpan("tampjurnal", "'" + Beban_Jasa_Medik_Dokter_Laborat_Ralan + "','Beban Jasa Medik Dokter Laborat Ralan','" + rs.getDouble("labrawatjalan") + "','0'", "debet=debet+'" + (rs.getDouble("labrawatjalan")) + "'", "kd_rek='" + Beban_Jasa_Medik_Dokter_Laborat_Ralan + "'");
                                    Sequel.menyimpan("tampjurnal", "'" + Utang_Jasa_Medik_Dokter_Laborat_Ralan + "','Utang Jasa Medik Dokter Laborat Ralan','0','" + rs.getDouble("labrawatjalan") + "'", "kredit=kredit+'" + (rs.getDouble("labrawatjalan")) + "'", "kd_rek='" + Utang_Jasa_Medik_Dokter_Laborat_Ralan + "'");
                                }
                                if (rs.getDouble("labrawatinap") > 0) {
                                    Sequel.menyimpan("tampjurnal", "'" + Beban_Jasa_Medik_Dokter_Laborat_Ranap + "','Beban Jasa Medik Dokter Laborat Ranap','" + rs.getDouble("labrawatinap") + "','0'", "debet=debet+'" + (rs.getDouble("labrawatinap")) + "'", "kd_rek='" + Beban_Jasa_Medik_Dokter_Laborat_Ranap + "'");
                                    Sequel.menyimpan("tampjurnal", "'" + Utang_Jasa_Medik_Dokter_Laborat_Ranap + "','Utang Jasa Medik Dokter Laborat Ranap','0','" + rs.getDouble("labrawatinap") + "'", "kredit=kredit+'" + (rs.getDouble("labrawatinap")) + "'", "kd_rek='" + Utang_Jasa_Medik_Dokter_Laborat_Ranap + "'");
                                }
                                if (rs.getDouble("radrawatjalan") > 0) {
                                    Sequel.menyimpan("tampjurnal", "'" + Beban_Jasa_Medik_Dokter_Radiologi_Ralan + "','Beban Jasa Medik Dokter Radiologi Ralan','" + rs.getDouble("radrawatjalan") + "','0'", "debet=debet+'" + (rs.getDouble("radrawatjalan")) + "'", "kd_rek='" + Beban_Jasa_Medik_Dokter_Radiologi_Ralan + "'");
                                    Sequel.menyimpan("tampjurnal", "'" + Utang_Jasa_Medik_Dokter_Radiologi_Ralan + "','Utang Jasa Medik Dokter Radiologi Ralan','0','" + rs.getDouble("radrawatjalan") + "'", "kredit=kredit+'" + (rs.getDouble("radrawatjalan")) + "'", "kd_rek='" + Utang_Jasa_Medik_Dokter_Radiologi_Ralan + "'");
                                }
                                if (rs.getDouble("radrawatinap") > 0) {
                                    Sequel.menyimpan("tampjurnal", "'" + Beban_Jasa_Medik_Dokter_Radiologi_Ranap + "','Beban Jasa Medik Dokter Radiologi Ranap','" + rs.getDouble("radrawatinap") + "','0'", "debet=debet+'" + (rs.getDouble("radrawatinap")) + "'", "kd_rek='" + Beban_Jasa_Medik_Dokter_Radiologi_Ranap + "'");
                                    Sequel.menyimpan("tampjurnal", "'" + Utang_Jasa_Medik_Dokter_Radiologi_Ranap + "','Utang Jasa Medik Dokter Radiologi Ranap','0','" + rs.getDouble("radrawatinap") + "'", "kredit=kredit+'" + (rs.getDouble("radrawatinap")) + "'", "kd_rek='" + Utang_Jasa_Medik_Dokter_Radiologi_Ranap + "'");
                                }
                                if (rs.getDouble("operasiralan") > 0) {
                                    Sequel.menyimpan("tampjurnal", "'" + Beban_Jasa_Medik_Dokter_Operasi_Ralan + "','Beban Jasa Medik Dokter Operasi Ralan','" + rs.getDouble("operasiralan") + "','0'", "debet=debet+'" + rs.getDouble("operasiralan") + "'", "kd_rek='" + Beban_Jasa_Medik_Dokter_Operasi_Ralan + "'");
                                    Sequel.menyimpan("tampjurnal", "'" + Utang_Jasa_Medik_Dokter_Operasi_Ralan + "','Utang Jasa Medik Dokter Operasi Ralan','0','" + rs.getDouble("operasiralan") + "'", "kredit=kredit+'" + rs.getDouble("operasiralan") + "'", "kd_rek='" + Utang_Jasa_Medik_Dokter_Operasi_Ralan + "'");
                                }
                                if (rs.getDouble("operasiranap") > 0) {
                                    Sequel.menyimpan("tampjurnal", "'" + Beban_Jasa_Medik_Dokter_Operasi_Ranap + "','Beban Jasa Medik Dokter Operasi Ranap','" + rs.getDouble("operasiranap") + "','0'", "debet=debet+'" + rs.getDouble("operasiranap") + "'", "kd_rek='" + Beban_Jasa_Medik_Dokter_Operasi_Ranap + "'");
                                    Sequel.menyimpan("tampjurnal", "'" + Utang_Jasa_Medik_Dokter_Operasi_Ranap + "','Utang Jasa Medik Dokter Operasi Ranap','0','" + rs.getDouble("operasiranap") + "'", "kredit=kredit+'" + rs.getDouble("operasiranap") + "'", "kd_rek='" + Utang_Jasa_Medik_Dokter_Operasi_Ranap + "'");
                                }
                                if (rs.getDouble("besar_bayar") > 0) {
                                    Sequel.menyimpan("tampjurnal", "'" + Bayar_JM_Dokter + "','Bayar JM Dokter','0','" + rs.getDouble("besar_bayar") + "'", "kredit=kredit+'" + (rs.getDouble("besar_bayar")) + "'", "kd_rek='" + Bayar_JM_Dokter + "'");
                                    Sequel.menyimpan("tampjurnal", "'" + koderekening + "','" + rs.getString("nama_bayar") + "','" + rs.getDouble("besar_bayar") + "','0'", "debet=debet+'" + (rs.getDouble("besar_bayar")) + "'", "kd_rek='" + koderekening + "'");
                                }
                                sukses = jur.simpanJurnal(tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString(), "U", "PEMBATALAN PEMBAYARAN JASA MEDIS DOKTER " + tbDokter.getValueAt(tbDokter.getSelectedRow(), 3).toString() + " " + tbDokter.getValueAt(tbDokter.getSelectedRow(), 4).toString() + ", OLEH " + akses.getkode());
                            }

                            if (sukses == true) {
                                Sequel.queryu2("delete from bayar_jm_dokter where no_bayar=?", 1, new String[]{tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString()});
                                tampil();
                                Sequel.Commit();
                            } else {
                                sukses = false;
                                JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat pemrosesan data, transaksi dibatalkan.\nPeriksa kembali data sebelum melanjutkan menyimpan..!!");
                                Sequel.RollBack();
                            }
                            Sequel.AutoComitTrue();
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
                    System.out.println("Notif : " + e);
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "Silahkan pilih Nomor J.M. yang mau dihapus..!");
        }
    }//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnHapusActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnGaji, BtnAll);
        }
    }//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnGajiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGajiActionPerformed
        if (tbDokter.getSelectedRow() > -1) {
            if (tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString().trim().equals("")) {
                Valid.textKosong(BtnAll, "Nomor J.M.");
            } else {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Sequel.queryu("delete from temporary where temp37='" + akses.getalamatip() + "'");
                i = 1;
                try {
                    //rawat jalan dr
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	pasien.nm_pasien, "
                            + "	bayar_rawat_jl_dr.tarif_tindakandr, "
                            + "	jns_perawatan.nm_perawatan, "
                            + "	bayar_rawat_jl_dr.tgl_perawatan, "
                            + "	bayar_rawat_jl_dr.jam_rawat, "
                            + "	reg_periksa.kd_pj, "
                            + "	bayar_rawat_jl_dr.kd_jenis_prw, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	pasien "
                            + "	INNER JOIN reg_periksa ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN bayar_rawat_jl_dr ON bayar_rawat_jl_dr.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN jns_perawatan ON bayar_rawat_jl_dr.kd_jenis_prw = jns_perawatan.kd_jenis_prw "
                            + "	INNER JOIN penjab ON jns_perawatan.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_rawat_jl_dr.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_rawat_jl_dr.tgl_perawatan ASC, "
                            + "	bayar_rawat_jl_dr.jam_rawat ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_perawatan") + " " + rs2.getString("jam_rawat") + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kd_jenis_prw") + "','"
                                    + rs2.getString("nm_perawatan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("tarif_tindakandr")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //rawat jalan drpr
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	pasien.nm_pasien, "
                            + "	bayar_rawat_jl_drpr.tarif_tindakandr, "
                            + "	jns_perawatan.nm_perawatan, "
                            + "	bayar_rawat_jl_drpr.tgl_perawatan, "
                            + "	bayar_rawat_jl_drpr.jam_rawat, "
                            + "	reg_periksa.kd_pj, "
                            + "	bayar_rawat_jl_drpr.kd_jenis_prw, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	pasien "
                            + "	INNER JOIN reg_periksa ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN bayar_rawat_jl_drpr ON bayar_rawat_jl_drpr.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN jns_perawatan ON bayar_rawat_jl_drpr.kd_jenis_prw = jns_perawatan.kd_jenis_prw "
                            + "	INNER JOIN penjab ON jns_perawatan.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_rawat_jl_drpr.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_rawat_jl_drpr.tgl_perawatan ASC, "
                            + "	bayar_rawat_jl_drpr.jam_rawat ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_perawatan") + " " + rs2.getString("jam_rawat") + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kd_jenis_prw") + "','"
                                    + rs2.getString("nm_perawatan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("tarif_tindakandr")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //rawat inap dr
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	pasien.nm_pasien, "
                            + "	bayar_rawat_inap_dr.tarif_tindakandr, "
                            + "	jns_perawatan_inap.nm_perawatan, "
                            + "	bayar_rawat_inap_dr.tgl_perawatan, "
                            + "	bayar_rawat_inap_dr.jam_rawat, "
                            + "	reg_periksa.kd_pj, "
                            + "	bayar_rawat_inap_dr.kd_jenis_prw, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	pasien "
                            + "	INNER JOIN reg_periksa ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN bayar_rawat_inap_dr ON bayar_rawat_inap_dr.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN jns_perawatan_inap ON bayar_rawat_inap_dr.kd_jenis_prw = jns_perawatan_inap.kd_jenis_prw "
                            + "	INNER JOIN penjab ON jns_perawatan_inap.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_rawat_inap_dr.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_rawat_inap_dr.tgl_perawatan ASC, "
                            + "	bayar_rawat_inap_dr.jam_rawat ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_perawatan") + " " + rs2.getString("jam_rawat") + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kd_jenis_prw") + "','"
                                    + rs2.getString("nm_perawatan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("tarif_tindakandr")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //rawat inap drpr
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	pasien.nm_pasien, "
                            + "	bayar_rawat_inap_drpr.tarif_tindakandr, "
                            + "	jns_perawatan_inap.nm_perawatan, "
                            + "	bayar_rawat_inap_drpr.tgl_perawatan, "
                            + "	bayar_rawat_inap_drpr.jam_rawat, "
                            + "	reg_periksa.kd_pj, "
                            + "	bayar_rawat_inap_drpr.kd_jenis_prw, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	pasien "
                            + "	INNER JOIN reg_periksa ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN bayar_rawat_inap_drpr ON bayar_rawat_inap_drpr.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN jns_perawatan_inap ON bayar_rawat_inap_drpr.kd_jenis_prw = jns_perawatan_inap.kd_jenis_prw "
                            + "	INNER JOIN penjab ON jns_perawatan_inap.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_rawat_inap_drpr.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_rawat_inap_drpr.tgl_perawatan ASC, "
                            + "	bayar_rawat_inap_drpr.jam_rawat ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_perawatan") + " " + rs2.getString("jam_rawat") + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kd_jenis_prw") + "','"
                                    + rs2.getString("nm_perawatan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("tarif_tindakandr")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //operasi operator 1
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	pasien.nm_pasien, "
                            + "	paket_operasi.nm_perawatan, "
                            + "	bayar_operasi_operator1.biayaoperator1, "
                            + "	bayar_operasi_operator1.tgl_operasi, "
                            + "	reg_periksa.kd_pj, "
                            + "	bayar_operasi_operator1.kode_paket, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	bayar_operasi_operator1 "
                            + "	INNER JOIN reg_periksa ON bayar_operasi_operator1.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN paket_operasi ON bayar_operasi_operator1.kode_paket = paket_operasi.kode_paket "
                            + "	INNER JOIN penjab ON paket_operasi.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_operasi_operator1.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_operasi_operator1.tgl_operasi ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_operasi").substring(0, 19) + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kode_paket") + "','"
                                    + rs2.getString("nm_perawatan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("biayaoperator1")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //operasi operator 2
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	pasien.nm_pasien, "
                            + "	paket_operasi.nm_perawatan, "
                            + "	bayar_operasi_operator2.biayaoperator2, "
                            + "	bayar_operasi_operator2.tgl_operasi, "
                            + "	reg_periksa.kd_pj, "
                            + "	bayar_operasi_operator2.kode_paket, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	bayar_operasi_operator2 "
                            + "	INNER JOIN reg_periksa ON bayar_operasi_operator2.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN paket_operasi ON bayar_operasi_operator2.kode_paket = paket_operasi.kode_paket "
                            + "	INNER JOIN penjab ON paket_operasi.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_operasi_operator2.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_operasi_operator2.tgl_operasi ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_operasi").substring(0, 19) + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kode_paket") + "','"
                                    + rs2.getString("nm_perawatan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("biayaoperator2")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //operasi operator 3
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	pasien.nm_pasien, "
                            + "	paket_operasi.nm_perawatan, "
                            + "	bayar_operasi_operator3.biayaoperator3, "
                            + "	bayar_operasi_operator3.tgl_operasi, "
                            + "	reg_periksa.kd_pj, "
                            + "	bayar_operasi_operator3.kode_paket, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	bayar_operasi_operator3 "
                            + "	INNER JOIN reg_periksa ON bayar_operasi_operator3.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN paket_operasi ON bayar_operasi_operator3.kode_paket = paket_operasi.kode_paket "
                            + "	INNER JOIN penjab ON paket_operasi.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_operasi_operator3.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_operasi_operator3.tgl_operasi ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_operasi").substring(0, 19) + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kode_paket") + "','"
                                    + rs2.getString("nm_perawatan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("biayaoperator3")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //operasi dokter_anak
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	pasien.nm_pasien, "
                            + "	paket_operasi.nm_perawatan, "
                            + "	bayar_operasi_dokter_anak.biayadokter_anak, "
                            + "	bayar_operasi_dokter_anak.tgl_operasi, "
                            + "	reg_periksa.kd_pj, "
                            + "	bayar_operasi_dokter_anak.kode_paket, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	bayar_operasi_dokter_anak "
                            + "	INNER JOIN reg_periksa ON bayar_operasi_dokter_anak.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN paket_operasi ON bayar_operasi_dokter_anak.kode_paket = paket_operasi.kode_paket "
                            + "	INNER JOIN penjab ON paket_operasi.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_operasi_dokter_anak.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_operasi_dokter_anak.tgl_operasi ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_operasi").substring(0, 19) + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kode_paket") + "','"
                                    + rs2.getString("nm_perawatan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("biayadokter_anak")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //operasi dokter_umum
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	pasien.nm_pasien, "
                            + "	paket_operasi.nm_perawatan, "
                            + "	bayar_operasi_dokter_umum.biaya_dokter_umum, "
                            + "	bayar_operasi_dokter_umum.tgl_operasi, "
                            + "	reg_periksa.kd_pj, "
                            + "	bayar_operasi_dokter_umum.kode_paket, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	bayar_operasi_dokter_umum "
                            + "	INNER JOIN reg_periksa ON bayar_operasi_dokter_umum.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN paket_operasi ON bayar_operasi_dokter_umum.kode_paket = paket_operasi.kode_paket "
                            + "	INNER JOIN penjab ON paket_operasi.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_operasi_dokter_umum.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_operasi_dokter_umum.tgl_operasi ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_operasi").substring(0, 19) + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kode_paket") + "','"
                                    + rs2.getString("nm_perawatan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("biaya_dokter_umum")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //operasi dokter_pjanak
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	pasien.nm_pasien, "
                            + "	paket_operasi.nm_perawatan, "
                            + "	bayar_operasi_dokter_pjanak.biaya_dokter_pjanak, "
                            + "	bayar_operasi_dokter_pjanak.tgl_operasi, "
                            + "	reg_periksa.kd_pj, "
                            + "	bayar_operasi_dokter_pjanak.kode_paket, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	bayar_operasi_dokter_pjanak "
                            + "	INNER JOIN reg_periksa ON bayar_operasi_dokter_pjanak.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN paket_operasi ON bayar_operasi_dokter_pjanak.kode_paket = paket_operasi.kode_paket "
                            + "	INNER JOIN penjab ON paket_operasi.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_operasi_dokter_pjanak.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_operasi_dokter_pjanak.tgl_operasi ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_operasi").substring(0, 19) + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kode_paket") + "','"
                                    + rs2.getString("nm_perawatan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("biaya_dokter_pjanak")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //operasi dokter_anestesi
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	pasien.nm_pasien, "
                            + "	paket_operasi.nm_perawatan, "
                            + "	bayar_operasi_dokter_anestesi.biayadokter_anestesi, "
                            + "	bayar_operasi_dokter_anestesi.tgl_operasi, "
                            + "	reg_periksa.kd_pj, "
                            + "	bayar_operasi_dokter_anestesi.kode_paket, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	bayar_operasi_dokter_anestesi "
                            + "	INNER JOIN reg_periksa ON bayar_operasi_dokter_anestesi.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN paket_operasi ON bayar_operasi_dokter_anestesi.kode_paket = paket_operasi.kode_paket "
                            + "	INNER JOIN penjab ON paket_operasi.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_operasi_dokter_anestesi.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_operasi_dokter_anestesi.tgl_operasi ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_operasi").substring(0, 19) + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kode_paket") + "','"
                                    + rs2.getString("nm_perawatan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("biayadokter_anestesi")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //dokter pj laborat
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	bayar_periksa_lab.tarif_tindakan_dokter, "
                            + "	pasien.nm_pasien, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	jns_perawatan_lab.nm_perawatan, "
                            + "	bayar_periksa_lab.tgl_periksa, "
                            + "	bayar_periksa_lab.jam, "
                            + "	bayar_periksa_lab.no_rawat, "
                            + "	bayar_periksa_lab.kd_jenis_prw, "
                            + "	reg_periksa.kd_pj, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	bayar_periksa_lab "
                            + "	INNER JOIN reg_periksa ON bayar_periksa_lab.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN jns_perawatan_lab ON bayar_periksa_lab.kd_jenis_prw = jns_perawatan_lab.kd_jenis_prw "
                            + "	INNER JOIN penjab ON jns_perawatan_lab.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_periksa_lab.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_periksa_lab.tgl_periksa ASC, "
                            + "	bayar_periksa_lab.jam ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_periksa") + " " + rs2.getString("jam") + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kd_jenis_prw") + "','"
                                    + rs2.getString("nm_perawatan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("tarif_tindakan_dokter")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //dokter detail laborat
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	bayar_detail_periksa_lab.bagian_dokter, "
                            + "	pasien.nm_pasien, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	template_laboratorium.Pemeriksaan, "
                            + "	bayar_detail_periksa_lab.tgl_periksa, "
                            + "	bayar_detail_periksa_lab.jam, "
                            + "	bayar_detail_periksa_lab.no_rawat, "
                            + "	bayar_detail_periksa_lab.kd_jenis_prw, "
                            + "	reg_periksa.kd_pj, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	bayar_detail_periksa_lab "
                            + "	INNER JOIN reg_periksa ON bayar_detail_periksa_lab.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN template_laboratorium ON bayar_detail_periksa_lab.id_template = template_laboratorium.id_template "
                            + "	INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_detail_periksa_lab.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_detail_periksa_lab.tgl_periksa ASC, "
                            + "	bayar_detail_periksa_lab.jam ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_periksa") + " " + rs2.getString("jam") + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kd_jenis_prw") + "','"
                                    + rs2.getString("Pemeriksaan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("bagian_dokter")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //dokter perujuk laborat
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	bayar_periksa_lab_perujuk.tarif_perujuk, "
                            + "	pasien.nm_pasien, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	jns_perawatan_lab.nm_perawatan, "
                            + "	bayar_periksa_lab_perujuk.tgl_periksa, "
                            + "	bayar_periksa_lab_perujuk.jam, "
                            + "	bayar_periksa_lab_perujuk.no_rawat, "
                            + "	bayar_periksa_lab_perujuk.kd_jenis_prw, "
                            + "	reg_periksa.kd_pj, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	bayar_periksa_lab_perujuk "
                            + "	INNER JOIN reg_periksa ON bayar_periksa_lab_perujuk.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN jns_perawatan_lab ON bayar_periksa_lab_perujuk.kd_jenis_prw = jns_perawatan_lab.kd_jenis_prw "
                            + "	INNER JOIN penjab ON jns_perawatan_lab.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_periksa_lab_perujuk.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_periksa_lab_perujuk.tgl_periksa ASC, "
                            + "	bayar_periksa_lab_perujuk.jam ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_periksa") + " " + rs2.getString("jam") + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kd_jenis_prw") + "','"
                                    + rs2.getString("nm_perawatan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("tarif_perujuk")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //dokter detail perujuk laborat
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	bayar_detail_periksa_lab_perujuk.bagian_perujuk, "
                            + "	pasien.nm_pasien, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	template_laboratorium.Pemeriksaan, "
                            + "	bayar_detail_periksa_lab_perujuk.tgl_periksa, "
                            + "	bayar_detail_periksa_lab_perujuk.jam, "
                            + "	bayar_detail_periksa_lab_perujuk.no_rawat, "
                            + "	bayar_detail_periksa_lab_perujuk.kd_jenis_prw, "
                            + "	reg_periksa.kd_pj, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	bayar_detail_periksa_lab_perujuk "
                            + "	INNER JOIN reg_periksa ON bayar_detail_periksa_lab_perujuk.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN template_laboratorium ON bayar_detail_periksa_lab_perujuk.id_template = template_laboratorium.id_template "
                            + "	INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_detail_periksa_lab_perujuk.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_detail_periksa_lab_perujuk.tgl_periksa ASC, "
                            + "	bayar_detail_periksa_lab_perujuk.jam ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_periksa") + " " + rs2.getString("jam") + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kd_jenis_prw") + "','"
                                    + rs2.getString("Pemeriksaan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("bagian_perujuk")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //dokter pj radiologi
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	bayar_periksa_radiologi.tarif_tindakan_dokter, "
                            + "	pasien.nm_pasien, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	jns_perawatan_radiologi.nm_perawatan, "
                            + "	bayar_periksa_radiologi.tgl_periksa, "
                            + "	bayar_periksa_radiologi.jam, "
                            + "	bayar_periksa_radiologi.no_rawat, "
                            + "	bayar_periksa_radiologi.kd_jenis_prw, "
                            + "	reg_periksa.kd_pj, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	bayar_periksa_radiologi "
                            + "	INNER JOIN reg_periksa ON bayar_periksa_radiologi.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN jns_perawatan_radiologi ON bayar_periksa_radiologi.kd_jenis_prw = jns_perawatan_radiologi.kd_jenis_prw "
                            + "	INNER JOIN penjab ON jns_perawatan_radiologi.kd_pj = penjab.kd_pj "
                            + "	AND reg_periksa.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_periksa_radiologi.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_periksa_radiologi.tgl_periksa ASC, "
                            + "	bayar_periksa_radiologi.jam ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_periksa") + " " + rs2.getString("jam") + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kd_jenis_prw") + "','"
                                    + rs2.getString("nm_perawatan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("tarif_tindakan_dokter")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    //dokter perujuk radiologi
                    ps2 = koneksi.prepareStatement(
                            "SELECT "
                            + "	bayar_periksa_radiologi_perujuk.tarif_perujuk, "
                            + "	pasien.nm_pasien, "
                            + "	reg_periksa.no_rawat, "
                            + "	reg_periksa.no_rkm_medis, "
                            + "	jns_perawatan_radiologi.nm_perawatan, "
                            + "	bayar_periksa_radiologi_perujuk.tgl_periksa, "
                            + "	bayar_periksa_radiologi_perujuk.jam, "
                            + "	bayar_periksa_radiologi_perujuk.no_rawat, "
                            + "	bayar_periksa_radiologi_perujuk.kd_jenis_prw, "
                            + "	reg_periksa.kd_pj, "
                            + "	penjab.png_jawab "
                            + "FROM "
                            + "	bayar_periksa_radiologi_perujuk "
                            + "	INNER JOIN reg_periksa ON bayar_periksa_radiologi_perujuk.no_rawat = reg_periksa.no_rawat "
                            + "	INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "	INNER JOIN jns_perawatan_radiologi ON bayar_periksa_radiologi_perujuk.kd_jenis_prw = jns_perawatan_radiologi.kd_jenis_prw "
                            + "	INNER JOIN penjab ON jns_perawatan_radiologi.kd_pj = penjab.kd_pj "
                            + "WHERE "
                            + "	bayar_periksa_radiologi_perujuk.no_bayar = ? "
                            + "ORDER BY "
                            + "	bayar_periksa_radiologi_perujuk.tgl_periksa ASC, "
                            + "	bayar_periksa_radiologi_perujuk.jam ASC");
                    try {
                        ps2.setString(1, tbDokter.getValueAt(tbDokter.getSelectedRow(), 0).toString());
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            Sequel.menyimpan("temporary", "'" + i + "','"
                                    + rs2.getString("tgl_periksa") + " " + rs2.getString("jam") + "','"
                                    + rs2.getString("no_rawat") + "','"
                                    + rs2.getString("no_rkm_medis") + "','"
                                    + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")" + "','"
                                    + rs2.getString("kd_jenis_prw") + "','"
                                    + rs2.getString("nm_perawatan") + "','"
                                    + Valid.SetAngka(rs2.getDouble("tarif_perujuk")) + "','"
                                    + rs2.getString("png_jawab") + "',"
                                    + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','',''", "JM Dokter"
                            );
                            i++;
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    } finally {
                        if (rs2 != null) {
                            rs2.close();
                        }
                        if (ps2 != null) {
                            ps2.close();
                        }
                    }
                    // Baris Jumlah dengan marker TOTAL di temp8 (Penjab) dan temp9 (Nominal)
                    Sequel.menyimpan("temporary", "'" + i + "','>> Jumlah :','','"
                            + "TOTAL','','','','"
                            + tbDokter.getValueAt(tbDokter.getSelectedRow(), 6).toString() + "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','" + akses.getalamatip() + "'", "JM Dokter"
                    );
                    Map<String, Object> param = new HashMap<>();
                    param.put("namars", akses.getnamars());
                    param.put("alamatrs", akses.getalamatrs());
                    param.put("kotars", akses.getkabupatenrs());
                    param.put("propinsirs", akses.getpropinsirs());
                    param.put("kontakrs", akses.getkontakrs());
                    param.put("emailrs", akses.getemailrs());
                    param.put("dokter", tbDokter.getValueAt(tbDokter.getSelectedRow(), 4).toString());
                    param.put("bulan", Valid.SetTgl3(tbDokter.getValueAt(tbDokter.getSelectedRow(), 1).toString()).substring(3, 10));
                    param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
                    Valid.MyReportqry("rptSlipBayarJMDokter.jasper", "report", "[ Slip J.M. Dokter  ]", "select * from temporary where temporary.temp37='" + akses.getalamatip() + "' order by temporary.no", param);
                } catch (Exception e) {
                    System.out.println("Notifikasi : " + e);
                }
                this.setCursor(Cursor.getDefaultCursor());
            }
        } else {
            JOptionPane.showMessageDialog(null, "Silahkan pilih Nomor J.M. yang mau dicetak slipnya..!");
        }
    }//GEN-LAST:event_BtnGajiActionPerformed

    private void BtnGajiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnGajiKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnGajiActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnCari, BtnAll);
        }
    }//GEN-LAST:event_BtnGajiKeyPressed

    private void ppRincianBtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppRincianBtnPrintActionPerformed
        rincian = false;
        tampil();
    }//GEN-LAST:event_ppRincianBtnPrintActionPerformed

    private void ppRincian2BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppRincian2BtnPrintActionPerformed
        rincian = true;
        tampil();
    }//GEN-LAST:event_ppRincian2BtnPrintActionPerformed

    private void ppSlipGajiPDFActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppSlipGajiPDFActionPerformed
        int[] selectedRows = tbDokter.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(null, "Silahkan pilih Nomor J.M. yang mau dicetak slipnya..!");
            return;
        }

        // Validasi kode dokter harus sama untuk seluruh baris yang dipilih
        String targetKdDokter = tbDokter.getValueAt(selectedRows[0], 3).toString().trim();
        String targetNmDokter = tbDokter.getValueAt(selectedRows[0], 4).toString().trim();

        if (targetKdDokter.equals("")) {
            JOptionPane.showMessageDialog(null, "Baris yang dipilih tidak memiliki Kode Dokter yang valid!");
            return;
        }

        for (int idx : selectedRows) {
            String kdDok = tbDokter.getValueAt(idx, 3).toString().trim();
            if (!kdDok.equals(targetKdDokter)) {
                String nmDokLain = tbDokter.getValueAt(idx, 4).toString().trim();
                JOptionPane.showMessageDialog(null,
                        "Nomor J.M. yang dipilih harus berasal dari dokter yang sama!\n"
                        + "Ditemukan dokter berbeda pada pilihan Anda:\n"
                        + "- " + targetNmDokter + " (" + targetKdDokter + ")\n"
                        + "- " + nmDokLain + " (" + kdDok + ")");
                return;
            }
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            String base64Logo = "";
            try {
                java.io.ByteArrayInputStream in = Sequel.cariGambar("select setting.logo from setting");
                if (in != null) {
                    java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[16384];
                    while ((nRead = in.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    base64Logo = "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(buffer.toByteArray());
                } else {
                    base64Logo = "setting/logo.jpg";
                }
            } catch (Exception ex) {
                base64Logo = "setting/logo.jpg";
            }

            java.util.List<String[]> listJM = new java.util.ArrayList<>();
            for (int idx : selectedRows) {
                String noBayar = tbDokter.getValueAt(idx, 0).toString().trim();
                String tglBayar = tbDokter.getValueAt(idx, 1).toString().trim();
                String keterangan = tbDokter.getValueAt(idx, 5).toString().trim();
                if (!noBayar.equals("")) {
                    listJM.add(new String[]{noBayar, tglBayar, keterangan});
                }
            }

            if (listJM.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Tidak ada Nomor J.M. yang valid untuk dicetak!");
                this.setCursor(Cursor.getDefaultCursor());
                return;
            }

            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Simpan PDF Slip Gaji J.M. Dokter");
            String defaultName;
            if (listJM.size() == 1) {
                defaultName = "SlipGajiJM_" + targetNmDokter.replaceAll("\\s+", "_") + "_" + listJM.get(0)[0].replaceAll("[^a-zA-Z0-9.-]", "_") + ".pdf";
            } else {
                defaultName = "SlipGajiJM_" + targetNmDokter.replaceAll("\\s+", "_") + "_" + listJM.size() + "NomorJM.pdf";
            }
            fileChooser.setSelectedFile(new java.io.File(defaultName));
            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection != javax.swing.JFileChooser.APPROVE_OPTION) {
                this.setCursor(Cursor.getDefaultCursor());
                return;
            }

            java.io.File f = fileChooser.getSelectedFile();
            if (!f.getName().toLowerCase().endsWith(".pdf")) {
                f = new java.io.File(f.getParentFile(), f.getName() + ".pdf");
            }

            StringBuilder html = new StringBuilder();
            html.append("<html>\n");
            html.append("<head>\n");
            html.append("<style>\n");
            html.append("  @page { size: landscape; margin: 15mm; }\n");
            html.append("  body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; color: #333; margin: 20px; }\n");
            html.append("  .header-table { width: 100%; border-bottom: 2px solid #333; margin-bottom: 15px; padding-bottom: 10px; }\n");
            html.append("  .header-table td { text-align: center; }\n");
            html.append("  .rs-name { font-size: 24px; font-weight: bold; color: #000; margin: 0; }\n");
            html.append("  .rs-address { font-size: 14px; color: #666; }\n");
            html.append("  .doc-info { width: 100%; margin-bottom: 15px; font-size: 14px; background: #fdfdfd; padding: 6px; border-left: 4px solid #0056b3; }\n");
            html.append("  .doc-info td { padding: 4px; }\n");
            html.append("  .jm-section { margin-top: 20px; margin-bottom: 25px; }\n");
            html.append("  .jm-header-box { background-color: #e3f2fd; border: 1px solid #90caf9; padding: 8px 12px; font-size: 13px; font-weight: bold; margin-bottom: 6px; border-radius: 4px; }\n");
            html.append("  .data-table { width: 100%; border-collapse: collapse; font-size: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.08); margin-bottom: 8px; }\n");
            html.append("  .data-table th { background-color: #f4f6f8; color: #333; font-weight: 600; padding: 8px 6px; border: 1px solid #ddd; text-align: center; }\n");
            html.append("  .data-table td { padding: 6px 8px; border: 1px solid #ddd; }\n");
            html.append("  .data-table tr:nth-child(even) { background-color: #fafafa; }\n");
            html.append("  .text-right { text-align: right; }\n");
            html.append("  .text-center { text-align: center; }\n");
            html.append("  .subtotal-row { background-color: #edf2f7 !important; font-weight: bold; }\n");
            html.append("  .grandtotal-table { width: 100%; border-collapse: collapse; font-size: 14px; margin-top: 25px; margin-bottom: 20px; }\n");
            html.append("  .grandtotal-table td { padding: 10px 12px; border: 2px solid #333; }\n");
            html.append("  .grandtotal-box { background-color: #d1e7dd; color: #0f5132; font-size: 16px; font-weight: bold; }\n");
            html.append("  .title-slip { font-size: 18px; font-weight: bold; margin-top: 10px; text-transform: uppercase; letter-spacing: 1px; color: #000; text-align: center; }\n");
            html.append("</style>\n");
            html.append("</head>\n");
            html.append("<body>\n");

            // Kop Surat
            html.append("<table class='header-table'>\n");
            html.append("<tr>\n");
            html.append("<td width='80px'><img src='").append(base64Logo).append("' width='70' height='70' alt='Logo'></td>\n");
            html.append("<td>\n");
            html.append("<div class='rs-name'>").append(akses.getnamars()).append("</div>\n");
            html.append("<div class='rs-address'>").append(akses.getalamatrs()).append(", ").append(akses.getkabupatenrs()).append(", ").append(akses.getpropinsirs()).append("</div>\n");
            html.append("<div class='rs-address'>").append(akses.getkontakrs()).append(", E-mail: ").append(akses.getemailrs()).append("</div>\n");
            html.append("</td>\n");
            html.append("<td width='80px'></td>\n");
            html.append("</tr>\n");
            html.append("</table>\n");

            html.append("<div class='title-slip'>SLIP JASA MEDIS DOKTER</div><br>\n");

            html.append("<table class='doc-info'>\n");
            html.append("<tr><td width='120'><b>Nama Dokter</b></td><td width='10'>:</td><td><b>").append(targetNmDokter).append("</b></td>");
            html.append("<td width='120'><b>Kode Dokter</b></td><td width='10'>:</td><td>").append(targetKdDokter).append("</td></tr>\n");
            html.append("<tr><td><b>Jumlah No. J.M.</b></td><td>:</td><td colspan='3'><b>").append(listJM.size()).append(" Dokumen Pembayaran</b></td></tr>\n");
            html.append("</table>\n");

            double grandTotalJasaMedis = 0;
            int totalTransaksiSemua = 0;

            for (String[] jmItem : listJM) {
                String noBayar = jmItem[0];
                String tglBayar = jmItem[1];
                String keterangan = jmItem[2];

                double subtotalJM = 0;
                int r = 1;

                html.append("<div class='jm-section'>\n");
                html.append("<div class='jm-header-box'>\n");
                html.append("Nomor J.M.: <span style='color:#0b5ed7;'>").append(noBayar).append("</span> &nbsp;&nbsp;|&nbsp;&nbsp; ");
                html.append("Tanggal Bayar: ").append(tglBayar).append(" &nbsp;&nbsp;|&nbsp;&nbsp; ");
                html.append("Keterangan: <span style='font-weight:normal;'>").append(keterangan.equals("") ? "-" : keterangan).append("</span>\n");
                html.append("</div>\n");

                html.append("<table class='data-table'>\n");
                html.append("<thead>\n");
                html.append("<tr>\n");
                html.append("<th style='width: 35px;'>No</th>\n");
                html.append("<th style='width: 90px;'>Tgl. Tindakan</th>\n");
                html.append("<th style='width: 65px;'>Jam</th>\n");
                html.append("<th style='width: 75px;'>No.RM</th>\n");
                html.append("<th>Nama Pasien</th>\n");
                html.append("<th>Tindakan Medis</th>\n");
                html.append("<th style='width: 105px;'>Jasa Medis</th>\n");
                html.append("<th style='width: 110px;'>Payor</th>\n");
                html.append("</tr>\n");
                html.append("</thead>\n");
                html.append("<tbody>\n");

                // Query 1: Rawat Jalan Dr
                ps2 = koneksi.prepareStatement(
                        "SELECT pasien.nm_pasien, bayar_rawat_jl_dr.tarif_tindakandr, jns_perawatan.nm_perawatan, "
                        + "bayar_rawat_jl_dr.tgl_perawatan, bayar_rawat_jl_dr.jam_rawat, reg_periksa.no_rkm_medis, penjab.png_jawab "
                        + "FROM pasien INNER JOIN reg_periksa ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                        + "INNER JOIN bayar_rawat_jl_dr ON bayar_rawat_jl_dr.no_rawat = reg_periksa.no_rawat "
                        + "INNER JOIN jns_perawatan ON bayar_rawat_jl_dr.kd_jenis_prw = jns_perawatan.kd_jenis_prw "
                        + "INNER JOIN penjab ON jns_perawatan.kd_pj = penjab.kd_pj "
                        + "WHERE bayar_rawat_jl_dr.no_bayar = ? "
                        + "ORDER BY bayar_rawat_jl_dr.tgl_perawatan ASC, bayar_rawat_jl_dr.jam_rawat ASC");
                try {
                    ps2.setString(1, noBayar);
                    rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        double jm = rs2.getDouble("tarif_tindakandr");
                        subtotalJM += jm;
                        html.append("<tr>\n");
                        html.append("<td class='text-center'>").append(r).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("tgl_perawatan")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("jam_rawat")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("no_rkm_medis")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_pasien")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_perawatan")).append("</td>\n");
                        html.append("<td class='text-right'>").append(Valid.SetAngka(jm)).append("</td>\n");
                        html.append("<td>").append(rs2.getString("png_jawab")).append("</td>\n");
                        html.append("</tr>\n");
                        r++;
                    }
                } finally {
                    if (rs2 != null) rs2.close();
                    if (ps2 != null) ps2.close();
                }

                // Query 2: Rawat Jalan DrPr
                ps2 = koneksi.prepareStatement(
                        "SELECT pasien.nm_pasien, bayar_rawat_jl_drpr.tarif_tindakandr, jns_perawatan.nm_perawatan, "
                        + "bayar_rawat_jl_drpr.tgl_perawatan, bayar_rawat_jl_drpr.jam_rawat, reg_periksa.no_rkm_medis, penjab.png_jawab "
                        + "FROM pasien INNER JOIN reg_periksa ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                        + "INNER JOIN bayar_rawat_jl_drpr ON bayar_rawat_jl_drpr.no_rawat = reg_periksa.no_rawat "
                        + "INNER JOIN jns_perawatan ON bayar_rawat_jl_drpr.kd_jenis_prw = jns_perawatan.kd_jenis_prw "
                        + "INNER JOIN penjab ON jns_perawatan.kd_pj = penjab.kd_pj "
                        + "WHERE bayar_rawat_jl_drpr.no_bayar = ? "
                        + "ORDER BY bayar_rawat_jl_drpr.tgl_perawatan ASC, bayar_rawat_jl_drpr.jam_rawat ASC");
                try {
                    ps2.setString(1, noBayar);
                    rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        double jm = rs2.getDouble("tarif_tindakandr");
                        subtotalJM += jm;
                        html.append("<tr>\n");
                        html.append("<td class='text-center'>").append(r).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("tgl_perawatan")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("jam_rawat")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("no_rkm_medis")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_pasien")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_perawatan")).append("</td>\n");
                        html.append("<td class='text-right'>").append(Valid.SetAngka(jm)).append("</td>\n");
                        html.append("<td>").append(rs2.getString("png_jawab")).append("</td>\n");
                        html.append("</tr>\n");
                        r++;
                    }
                } finally {
                    if (rs2 != null) rs2.close();
                    if (ps2 != null) ps2.close();
                }

                // Query 3: Rawat Inap Dr
                ps2 = koneksi.prepareStatement(
                        "SELECT pasien.nm_pasien, bayar_rawat_inap_dr.tarif_tindakandr, jns_perawatan_inap.nm_perawatan, "
                        + "bayar_rawat_inap_dr.tgl_perawatan, bayar_rawat_inap_dr.jam_rawat, reg_periksa.no_rkm_medis, penjab.png_jawab "
                        + "FROM pasien INNER JOIN reg_periksa ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                        + "INNER JOIN bayar_rawat_inap_dr ON bayar_rawat_inap_dr.no_rawat = reg_periksa.no_rawat "
                        + "INNER JOIN jns_perawatan_inap ON bayar_rawat_inap_dr.kd_jenis_prw = jns_perawatan_inap.kd_jenis_prw "
                        + "INNER JOIN penjab ON jns_perawatan_inap.kd_pj = penjab.kd_pj "
                        + "WHERE bayar_rawat_inap_dr.no_bayar = ? "
                        + "ORDER BY bayar_rawat_inap_dr.tgl_perawatan ASC, bayar_rawat_inap_dr.jam_rawat ASC");
                try {
                    ps2.setString(1, noBayar);
                    rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        double jm = rs2.getDouble("tarif_tindakandr");
                        subtotalJM += jm;
                        html.append("<tr>\n");
                        html.append("<td class='text-center'>").append(r).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("tgl_perawatan")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("jam_rawat")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("no_rkm_medis")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_pasien")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_perawatan")).append("</td>\n");
                        html.append("<td class='text-right'>").append(Valid.SetAngka(jm)).append("</td>\n");
                        html.append("<td>").append(rs2.getString("png_jawab")).append("</td>\n");
                        html.append("</tr>\n");
                        r++;
                    }
                } finally {
                    if (rs2 != null) rs2.close();
                    if (ps2 != null) ps2.close();
                }

                // Query 4: Rawat Inap DrPr
                ps2 = koneksi.prepareStatement(
                        "SELECT pasien.nm_pasien, bayar_rawat_inap_drpr.tarif_tindakandr, jns_perawatan_inap.nm_perawatan, "
                        + "bayar_rawat_inap_drpr.tgl_perawatan, bayar_rawat_inap_drpr.jam_rawat, reg_periksa.no_rkm_medis, penjab.png_jawab "
                        + "FROM pasien INNER JOIN reg_periksa ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                        + "INNER JOIN bayar_rawat_inap_drpr ON bayar_rawat_inap_drpr.no_rawat = reg_periksa.no_rawat "
                        + "INNER JOIN jns_perawatan_inap ON bayar_rawat_inap_drpr.kd_jenis_prw = jns_perawatan_inap.kd_jenis_prw "
                        + "INNER JOIN penjab ON jns_perawatan_inap.kd_pj = penjab.kd_pj "
                        + "WHERE bayar_rawat_inap_drpr.no_bayar = ? "
                        + "ORDER BY bayar_rawat_inap_drpr.tgl_perawatan ASC, bayar_rawat_inap_drpr.jam_rawat ASC");
                try {
                    ps2.setString(1, noBayar);
                    rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        double jm = rs2.getDouble("tarif_tindakandr");
                        subtotalJM += jm;
                        html.append("<tr>\n");
                        html.append("<td class='text-center'>").append(r).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("tgl_perawatan")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("jam_rawat")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("no_rkm_medis")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_pasien")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_perawatan")).append("</td>\n");
                        html.append("<td class='text-right'>").append(Valid.SetAngka(jm)).append("</td>\n");
                        html.append("<td>").append(rs2.getString("png_jawab")).append("</td>\n");
                        html.append("</tr>\n");
                        r++;
                    }
                } finally {
                    if (rs2 != null) rs2.close();
                    if (ps2 != null) ps2.close();
                }

                // Queries 5 s/d 11: Operasi
                String[][] opQueries = {
                    {"bayar_operasi_operator1", "biayaoperator1", "Operasi (Operator 1)"},
                    {"bayar_operasi_operator2", "biayaoperator2", "Operasi (Operator 2)"},
                    {"bayar_operasi_operator3", "biayaoperator3", "Operasi (Operator 3)"},
                    {"bayar_operasi_dokter_anak", "biayadokter_anak", "Operasi (Dr Anak)"},
                    {"bayar_operasi_dokter_umum", "biaya_dokter_umum", "Operasi (Dr Umum)"},
                    {"bayar_operasi_dokter_pjanak", "biaya_dokter_pjanak", "Operasi (Dr PJ Anak)"},
                    {"bayar_operasi_dokter_anestesi", "biayadokter_anestesi", "Operasi (Dr Anestesi)"}
                };

                for (String[] op : opQueries) {
                    String tbl = op[0];
                    String col = op[1];
                    String label = op[2];
                    ps2 = koneksi.prepareStatement(
                            "SELECT pasien.nm_pasien, paket_operasi.nm_perawatan, " + tbl + "." + col + ", "
                            + tbl + ".tgl_operasi, reg_periksa.no_rkm_medis, penjab.png_jawab "
                            + "FROM pasien INNER JOIN reg_periksa ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                            + "INNER JOIN " + tbl + " ON " + tbl + ".no_rawat = reg_periksa.no_rawat "
                            + "INNER JOIN paket_operasi ON " + tbl + ".kode_paket = paket_operasi.kode_paket "
                            + "INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj "
                            + "WHERE " + tbl + ".no_bayar = ? "
                            + "ORDER BY " + tbl + ".tgl_operasi ASC");
                    try {
                        ps2.setString(1, noBayar);
                        rs2 = ps2.executeQuery();
                        while (rs2.next()) {
                            double jm = rs2.getDouble(col);
                            subtotalJM += jm;
                            String tglOp = rs2.getString("tgl_operasi");
                            String tglS = tglOp != null && tglOp.length() >= 10 ? tglOp.substring(0, 10) : "";
                            String jamS = tglOp != null && tglOp.length() >= 19 ? tglOp.substring(11, 19) : "";
                            html.append("<tr>\n");
                            html.append("<td class='text-center'>").append(r).append("</td>\n");
                            html.append("<td class='text-center'>").append(tglS).append("</td>\n");
                            html.append("<td class='text-center'>").append(jamS).append("</td>\n");
                            html.append("<td class='text-center'>").append(rs2.getString("no_rkm_medis")).append("</td>\n");
                            html.append("<td>").append(rs2.getString("nm_pasien")).append("</td>\n");
                            html.append("<td>").append(rs2.getString("nm_perawatan")).append(" - ").append(label).append("</td>\n");
                            html.append("<td class='text-right'>").append(Valid.SetAngka(jm)).append("</td>\n");
                            html.append("<td>").append(rs2.getString("png_jawab")).append("</td>\n");
                            html.append("</tr>\n");
                            r++;
                        }
                    } finally {
                        if (rs2 != null) rs2.close();
                        if (ps2 != null) ps2.close();
                    }
                }

                // Query 12: Lab PJ
                ps2 = koneksi.prepareStatement(
                        "SELECT bayar_periksa_lab.tarif_tindakan_dokter, pasien.nm_pasien, reg_periksa.no_rkm_medis, "
                        + "jns_perawatan_lab.nm_perawatan, bayar_periksa_lab.tgl_periksa, bayar_periksa_lab.jam, penjab.png_jawab "
                        + "FROM bayar_periksa_lab INNER JOIN reg_periksa ON bayar_periksa_lab.no_rawat = reg_periksa.no_rawat "
                        + "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                        + "INNER JOIN jns_perawatan_lab ON bayar_periksa_lab.kd_jenis_prw = jns_perawatan_lab.kd_jenis_prw "
                        + "INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj "
                        + "WHERE bayar_periksa_lab.no_bayar = ? "
                        + "ORDER BY bayar_periksa_lab.tgl_periksa ASC, bayar_periksa_lab.jam ASC");
                try {
                    ps2.setString(1, noBayar);
                    rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        double jm = rs2.getDouble("tarif_tindakan_dokter");
                        subtotalJM += jm;
                        html.append("<tr>\n");
                        html.append("<td class='text-center'>").append(r).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("tgl_periksa")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("jam")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("no_rkm_medis")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_pasien")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_perawatan")).append(" (Lab PJ)</td>\n");
                        html.append("<td class='text-right'>").append(Valid.SetAngka(jm)).append("</td>\n");
                        html.append("<td>").append(rs2.getString("png_jawab")).append("</td>\n");
                        html.append("</tr>\n");
                        r++;
                    }
                } finally {
                    if (rs2 != null) rs2.close();
                    if (ps2 != null) ps2.close();
                }

                // Query 13: Detail Lab PJ
                ps2 = koneksi.prepareStatement(
                        "SELECT bayar_detail_periksa_lab.bagian_dokter, pasien.nm_pasien, reg_periksa.no_rkm_medis, "
                        + "template_laboratorium.Pemeriksaan, bayar_detail_periksa_lab.tgl_periksa, bayar_detail_periksa_lab.jam, penjab.png_jawab "
                        + "FROM bayar_detail_periksa_lab INNER JOIN reg_periksa ON bayar_detail_periksa_lab.no_rawat = reg_periksa.no_rawat "
                        + "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                        + "INNER JOIN template_laboratorium ON bayar_detail_periksa_lab.id_template = template_laboratorium.id_template "
                        + "INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj "
                        + "WHERE bayar_detail_periksa_lab.no_bayar = ? "
                        + "ORDER BY bayar_detail_periksa_lab.tgl_periksa ASC, bayar_detail_periksa_lab.jam ASC");
                try {
                    ps2.setString(1, noBayar);
                    rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        double jm = rs2.getDouble("bagian_dokter");
                        subtotalJM += jm;
                        html.append("<tr>\n");
                        html.append("<td class='text-center'>").append(r).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("tgl_periksa")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("jam")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("no_rkm_medis")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_pasien")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("Pemeriksaan")).append(" (Detail Lab PJ)</td>\n");
                        html.append("<td class='text-right'>").append(Valid.SetAngka(jm)).append("</td>\n");
                        html.append("<td>").append(rs2.getString("png_jawab")).append("</td>\n");
                        html.append("</tr>\n");
                        r++;
                    }
                } finally {
                    if (rs2 != null) rs2.close();
                    if (ps2 != null) ps2.close();
                }

                // Query 14: Lab Perujuk
                ps2 = koneksi.prepareStatement(
                        "SELECT bayar_periksa_lab_perujuk.tarif_perujuk, pasien.nm_pasien, reg_periksa.no_rkm_medis, "
                        + "jns_perawatan_lab.nm_perawatan, bayar_periksa_lab_perujuk.tgl_periksa, bayar_periksa_lab_perujuk.jam, penjab.png_jawab "
                        + "FROM bayar_periksa_lab_perujuk INNER JOIN reg_periksa ON bayar_periksa_lab_perujuk.no_rawat = reg_periksa.no_rawat "
                        + "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                        + "INNER JOIN jns_perawatan_lab ON bayar_periksa_lab_perujuk.kd_jenis_prw = jns_perawatan_lab.kd_jenis_prw "
                        + "INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj "
                        + "WHERE bayar_periksa_lab_perujuk.no_bayar = ? "
                        + "ORDER BY bayar_periksa_lab_perujuk.tgl_periksa ASC, bayar_periksa_lab_perujuk.jam ASC");
                try {
                    ps2.setString(1, noBayar);
                    rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        double jm = rs2.getDouble("tarif_perujuk");
                        subtotalJM += jm;
                        html.append("<tr>\n");
                        html.append("<td class='text-center'>").append(r).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("tgl_periksa")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("jam")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("no_rkm_medis")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_pasien")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_perawatan")).append(" (Lab Perujuk)</td>\n");
                        html.append("<td class='text-right'>").append(Valid.SetAngka(jm)).append("</td>\n");
                        html.append("<td>").append(rs2.getString("png_jawab")).append("</td>\n");
                        html.append("</tr>\n");
                        r++;
                    }
                } finally {
                    if (rs2 != null) rs2.close();
                    if (ps2 != null) ps2.close();
                }

                // Query 15: Detail Lab Perujuk
                ps2 = koneksi.prepareStatement(
                        "SELECT bayar_detail_periksa_lab_perujuk.bagian_perujuk, pasien.nm_pasien, reg_periksa.no_rkm_medis, "
                        + "template_laboratorium.Pemeriksaan, bayar_detail_periksa_lab_perujuk.tgl_periksa, bayar_detail_periksa_lab_perujuk.jam, penjab.png_jawab "
                        + "FROM bayar_detail_periksa_lab_perujuk INNER JOIN reg_periksa ON bayar_detail_periksa_lab_perujuk.no_rawat = reg_periksa.no_rawat "
                        + "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                        + "INNER JOIN template_laboratorium ON bayar_detail_periksa_lab_perujuk.id_template = template_laboratorium.id_template "
                        + "INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj "
                        + "WHERE bayar_detail_periksa_lab_perujuk.no_bayar = ? "
                        + "ORDER BY bayar_detail_periksa_lab_perujuk.tgl_periksa ASC, bayar_detail_periksa_lab_perujuk.jam ASC");
                try {
                    ps2.setString(1, noBayar);
                    rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        double jm = rs2.getDouble("bagian_perujuk");
                        subtotalJM += jm;
                        html.append("<tr>\n");
                        html.append("<td class='text-center'>").append(r).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("tgl_periksa")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("jam")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("no_rkm_medis")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_pasien")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("Pemeriksaan")).append(" (Detail Lab Perujuk)</td>\n");
                        html.append("<td class='text-right'>").append(Valid.SetAngka(jm)).append("</td>\n");
                        html.append("<td>").append(rs2.getString("png_jawab")).append("</td>\n");
                        html.append("</tr>\n");
                        r++;
                    }
                } finally {
                    if (rs2 != null) rs2.close();
                    if (ps2 != null) ps2.close();
                }

                // Query 16: Radiologi PJ
                ps2 = koneksi.prepareStatement(
                        "SELECT bayar_periksa_radiologi.tarif_tindakan_dokter, pasien.nm_pasien, reg_periksa.no_rkm_medis, "
                        + "jns_perawatan_radiologi.nm_perawatan, bayar_periksa_radiologi.tgl_periksa, bayar_periksa_radiologi.jam, penjab.png_jawab "
                        + "FROM bayar_periksa_radiologi INNER JOIN reg_periksa ON bayar_periksa_radiologi.no_rawat = reg_periksa.no_rawat "
                        + "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                        + "INNER JOIN jns_perawatan_radiologi ON bayar_periksa_radiologi.kd_jenis_prw = jns_perawatan_radiologi.kd_jenis_prw "
                        + "INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj "
                        + "WHERE bayar_periksa_radiologi.no_bayar = ? "
                        + "ORDER BY bayar_periksa_radiologi.tgl_periksa ASC, bayar_periksa_radiologi.jam ASC");
                try {
                    ps2.setString(1, noBayar);
                    rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        double jm = rs2.getDouble("tarif_tindakan_dokter");
                        subtotalJM += jm;
                        html.append("<tr>\n");
                        html.append("<td class='text-center'>").append(r).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("tgl_periksa")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("jam")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("no_rkm_medis")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_pasien")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_perawatan")).append(" (Radiologi PJ)</td>\n");
                        html.append("<td class='text-right'>").append(Valid.SetAngka(jm)).append("</td>\n");
                        html.append("<td>").append(rs2.getString("png_jawab")).append("</td>\n");
                        html.append("</tr>\n");
                        r++;
                    }
                } finally {
                    if (rs2 != null) rs2.close();
                    if (ps2 != null) ps2.close();
                }

                // Query 17: Radiologi Perujuk
                ps2 = koneksi.prepareStatement(
                        "SELECT bayar_periksa_radiologi_perujuk.tarif_perujuk, pasien.nm_pasien, reg_periksa.no_rkm_medis, "
                        + "jns_perawatan_radiologi.nm_perawatan, bayar_periksa_radiologi_perujuk.tgl_periksa, bayar_periksa_radiologi_perujuk.jam, penjab.png_jawab "
                        + "FROM bayar_periksa_radiologi_perujuk INNER JOIN reg_periksa ON bayar_periksa_radiologi_perujuk.no_rawat = reg_periksa.no_rawat "
                        + "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                        + "INNER JOIN jns_perawatan_radiologi ON bayar_periksa_radiologi_perujuk.kd_jenis_prw = jns_perawatan_radiologi.kd_jenis_prw "
                        + "INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj "
                        + "WHERE bayar_periksa_radiologi_perujuk.no_bayar = ? "
                        + "ORDER BY bayar_periksa_radiologi_perujuk.tgl_periksa ASC, bayar_periksa_radiologi_perujuk.jam ASC");
                try {
                    ps2.setString(1, noBayar);
                    rs2 = ps2.executeQuery();
                    while (rs2.next()) {
                        double jm = rs2.getDouble("tarif_perujuk");
                        subtotalJM += jm;
                        html.append("<tr>\n");
                        html.append("<td class='text-center'>").append(r).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("tgl_periksa")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("jam")).append("</td>\n");
                        html.append("<td class='text-center'>").append(rs2.getString("no_rkm_medis")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_pasien")).append("</td>\n");
                        html.append("<td>").append(rs2.getString("nm_perawatan")).append(" (Radiologi Perujuk)</td>\n");
                        html.append("<td class='text-right'>").append(Valid.SetAngka(jm)).append("</td>\n");
                        html.append("<td>").append(rs2.getString("png_jawab")).append("</td>\n");
                        html.append("</tr>\n");
                        r++;
                    }
                } finally {
                    if (rs2 != null) rs2.close();
                    if (ps2 != null) ps2.close();
                }

                html.append("<tr class='subtotal-row'>\n");
                html.append("<td colspan='6' class='text-right'>SUBTOTAL J.M. (").append(noBayar).append(") :</td>\n");
                html.append("<td class='text-right'>").append(Valid.SetAngka(subtotalJM)).append("</td>\n");
                html.append("<td></td>\n");
                html.append("</tr>\n");

                html.append("</tbody>\n");
                html.append("</table>\n");
                html.append("</div>\n");

                grandTotalJasaMedis += subtotalJM;
                totalTransaksiSemua += (r - 1);
            }

            // Grand Total Keseluruhan
            html.append("<table class='grandtotal-table'>\n");
            html.append("<tr class='grandtotal-box'>\n");
            html.append("<td style='width: 70%; text-align: right;'>GRAND TOTAL KESELURUHAN (").append(listJM.size()).append(" Nomor J.M., ").append(totalTransaksiSemua).append(" Tindakan) :</td>\n");
            html.append("<td style='width: 30%; text-align: right; font-size: 18px;'>Rp. ").append(Valid.SetAngka(grandTotalJasaMedis)).append("</td>\n");
            html.append("</tr>\n");
            html.append("</table>\n");

            // Tanda tangan
            html.append("<br><br>\n");
            html.append("<table style='width: 100%; font-size: 14px; border: none;'>\n");
            html.append("<tr>\n");
            html.append("<td style='width: 70%; border: none;'></td>\n");
            html.append("<td style='width: 30%; text-align: center; border: none;'>\n");
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMMM yyyy", new java.util.Locale("id", "ID"));
            String tglCetak = sdf.format(new java.util.Date());
            html.append("Boyolali, ").append(tglCetak).append("<br>\n");
            html.append("Manajemen RS Indriati Boyolali<br><br><br><br><br>\n");
            html.append("( .......................................... )\n");
            html.append("</td>\n");
            html.append("</tr>\n");
            html.append("</table>\n");

            html.append("</body>\n");
            html.append("</html>\n");

            HtmlConverter.convertToPdf(html.toString(), new java.io.FileOutputStream(f));
            java.awt.Desktop.getDesktop().browse(f.toURI());
        } catch (Exception e) {
            System.out.println("Notifikasi Cetak Slip PDF : " + e);
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }//GEN-LAST:event_ppSlipGajiPDFActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            KeuanganCariBayarJMDokter dialog = new KeuanganCariBayarJMDokter(new javax.swing.JFrame(), true);
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
    private widget.Button BtnAll;
    private widget.Button BtnCari;
    private widget.Button BtnGaji;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.CekBox ChkInput;
    private widget.panelisi FormInput;
    private widget.Label LTotal;
    private widget.TextBox NoBayar;
    private javax.swing.JPanel PanelInput;
    private widget.Tanggal Tanggal1;
    private widget.Tanggal Tanggal2;
    private widget.Button btnPegawai;
    private widget.InternalFrame internalFrame1;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.TextBox kddokter;
    private widget.Label label11;
    private widget.Label label12;
    private widget.Label label15;
    private widget.Label label16;
    private widget.Label label9;
    private widget.TextBox nmdokter;
    private widget.panelisi panelisi1;
    private javax.swing.JMenuItem ppRincian;
    private javax.swing.JMenuItem ppRincian2;
    private javax.swing.JMenuItem ppSlipGajiPDF;
    private widget.ScrollPane scrollPane1;
    private widget.Table tbDokter;
    // End of variables declaration//GEN-END:variables

    private javax.swing.SwingWorker<Void, Void> workerTampil = null;

    private void tampil() {
        if (workerTampil != null && !workerTampil.isDone()) {
            return;
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        BtnCari.setEnabled(false);
        Valid.tabelKosong(tabMode);
        if (tabModeRekap != null) {
            Valid.tabelKosong(tabModeRekap);
        }
        LTotal.setText("Memuat data...");

        workerTampil = new javax.swing.SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                prosesTampil();
                return null;
            }

            @Override
            protected void done() {
                try {
                    if (tabPane != null && tabPane.getSelectedIndex() == 1) {
                        LTotal.setText(Valid.SetAngka(totalRekapNett));
                    } else {
                        LTotal.setText(Valid.SetAngka(totaltagihan));
                    }
                } catch (Exception ignored) {
                }
                BtnCari.setEnabled(true);
                setCursor(Cursor.getDefaultCursor());
            }
        };
        workerTampil.execute();
    }

    private void prosesTampil() {
        try {
            notagihan = "";
            petugas = "";
            tanggal = " bayar_jm_dokter.tanggal between '" + Valid.SetTgl(Tanggal1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(Tanggal2.getSelectedItem() + "") + "' ";
            if (!NoBayar.getText().trim().equals("")) {
                notagihan = " and bayar_jm_dokter.no_bayar='" + NoBayar.getText() + "' ";
            }
            if (!nmdokter.getText().trim().equals("")) {
                petugas = " and concat(bayar_jm_dokter.kd_dokter,dokter.nm_dokter) like '%" + kddokter.getText() + nmdokter.getText() + "%' ";
            }

            ps = koneksi.prepareStatement(
                    "select bayar_jm_dokter.no_bayar,bayar_jm_dokter.tanggal,bayar_jm_dokter.kd_dokter,dokter.nm_dokter,bayar_jm_dokter.besar_bayar,bayar_jm_dokter.nama_bayar,bayar_jm_dokter.keterangan "
                    + "from bayar_jm_dokter inner join dokter on bayar_jm_dokter.kd_dokter=dokter.kd_dokter where " + tanggal + notagihan + petugas + " order by bayar_jm_dokter.tanggal");
            try {
                rs = ps.executeQuery();
                totaltagihan = 0;
                if (rincian == false) {
                    while (rs.next()) {
                        tabMode.addRow(new Object[]{
                            rs.getString("no_bayar"), rs.getString("tanggal"), rs.getString("nama_bayar"), rs.getString("kd_dokter"), rs.getString("nm_dokter"), rs.getString("keterangan"), Valid.SetAngka(rs.getDouble("besar_bayar"))
                        });
                        totaltagihan = totaltagihan + rs.getDouble("besar_bayar");
                    }
                } else {
                    while (rs.next()) {
                        totaltagihan = totaltagihan + rs.getDouble("besar_bayar");
                        tabMode.addRow(new Object[]{
                            rs.getString("no_bayar"), rs.getString("tanggal"), rs.getString("nama_bayar"), rs.getString("kd_dokter"), rs.getString("nm_dokter"), rs.getString("keterangan"), Valid.SetAngka(rs.getDouble("besar_bayar"))
                        });
                        tabMode.addRow(new Object[]{
                            "", "Tanggal & Jam", "Pasien", "No.Rawat", "Kode/ID", "Tindakan Medis", "Besar J.M."
                        });
                        //rawat jalan dr
                        ps2 = koneksi.prepareStatement(
                                "select pasien.nm_pasien,bayar_rawat_jl_dr.tarif_tindakandr,jns_perawatan.nm_perawatan,bayar_rawat_jl_dr.tgl_perawatan,"
                                + "bayar_rawat_jl_dr.jam_rawat,reg_periksa.kd_pj,bayar_rawat_jl_dr.kd_jenis_prw,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                                + "from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join bayar_rawat_jl_dr on bayar_rawat_jl_dr.no_rawat=reg_periksa.no_rawat "
                                + "inner join jns_perawatan on bayar_rawat_jl_dr.kd_jenis_prw=jns_perawatan.kd_jenis_prw "
                                + "where bayar_rawat_jl_dr.no_bayar=? order by bayar_rawat_jl_dr.tgl_perawatan,bayar_rawat_jl_dr.jam_rawat");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_perawatan") + " " + rs2.getString("jam_rawat"), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kd_jenis_prw") + " (Ralan)", rs2.getString("nm_perawatan"), Valid.SetAngka(rs2.getDouble("tarif_tindakandr"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //rawat jalan drpr
                        ps2 = koneksi.prepareStatement(
                                "select pasien.nm_pasien,bayar_rawat_jl_drpr.tarif_tindakandr,jns_perawatan.nm_perawatan,bayar_rawat_jl_drpr.tgl_perawatan,"
                                + "bayar_rawat_jl_drpr.jam_rawat,reg_periksa.kd_pj,bayar_rawat_jl_drpr.kd_jenis_prw,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                                + "from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join bayar_rawat_jl_drpr on bayar_rawat_jl_drpr.no_rawat=reg_periksa.no_rawat "
                                + "inner join jns_perawatan on bayar_rawat_jl_drpr.kd_jenis_prw=jns_perawatan.kd_jenis_prw "
                                + "where bayar_rawat_jl_drpr.no_bayar=? order by bayar_rawat_jl_drpr.tgl_perawatan,bayar_rawat_jl_drpr.jam_rawat");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_perawatan") + " " + rs2.getString("jam_rawat"), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kd_jenis_prw") + " (Ralan)", rs2.getString("nm_perawatan"), Valid.SetAngka(rs2.getDouble("tarif_tindakandr"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //rawat inap dr
                        ps2 = koneksi.prepareStatement(
                                "select pasien.nm_pasien,bayar_rawat_inap_dr.tarif_tindakandr,jns_perawatan_inap.nm_perawatan,bayar_rawat_inap_dr.tgl_perawatan,"
                                + "bayar_rawat_inap_dr.jam_rawat,reg_periksa.kd_pj,bayar_rawat_inap_dr.kd_jenis_prw,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                                + "from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join bayar_rawat_inap_dr on bayar_rawat_inap_dr.no_rawat=reg_periksa.no_rawat "
                                + "inner join jns_perawatan_inap on bayar_rawat_inap_dr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw "
                                + "where bayar_rawat_inap_dr.no_bayar=? order by bayar_rawat_inap_dr.tgl_perawatan,bayar_rawat_inap_dr.jam_rawat");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_perawatan") + " " + rs2.getString("jam_rawat"), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kd_jenis_prw") + " (Ranap)", rs2.getString("nm_perawatan"), Valid.SetAngka(rs2.getDouble("tarif_tindakandr"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //rawat inap drpr
                        ps2 = koneksi.prepareStatement(
                                "select pasien.nm_pasien,bayar_rawat_inap_drpr.tarif_tindakandr,jns_perawatan_inap.nm_perawatan,bayar_rawat_inap_drpr.tgl_perawatan,"
                                + "bayar_rawat_inap_drpr.jam_rawat,reg_periksa.kd_pj,bayar_rawat_inap_drpr.kd_jenis_prw,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                                + "from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join bayar_rawat_inap_drpr on bayar_rawat_inap_drpr.no_rawat=reg_periksa.no_rawat "
                                + "inner join jns_perawatan_inap on bayar_rawat_inap_drpr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw "
                                + "where bayar_rawat_inap_drpr.no_bayar=? order by bayar_rawat_inap_drpr.tgl_perawatan,bayar_rawat_inap_drpr.jam_rawat");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_perawatan") + " " + rs2.getString("jam_rawat"), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kd_jenis_prw") + " (Ranap)", rs2.getString("nm_perawatan"), Valid.SetAngka(rs2.getDouble("tarif_tindakandr"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //operasi operator 1
                        ps2 = koneksi.prepareStatement(
                                "select pasien.nm_pasien,paket_operasi.nm_perawatan,bayar_operasi_operator1.biayaoperator1,bayar_operasi_operator1.tgl_operasi,"
                                + "reg_periksa.kd_pj,bayar_operasi_operator1.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                                + "from bayar_operasi_operator1 inner join reg_periksa on bayar_operasi_operator1.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join paket_operasi on bayar_operasi_operator1.kode_paket=paket_operasi.kode_paket "
                                + "where bayar_operasi_operator1.no_bayar=? order by bayar_operasi_operator1.tgl_operasi");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_operasi").substring(0, 19), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kode_paket") + " (Operasi)", rs2.getString("nm_perawatan"), Valid.SetAngka(rs2.getDouble("biayaoperator1"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //operasi operator 2
                        ps2 = koneksi.prepareStatement(
                                "select pasien.nm_pasien,paket_operasi.nm_perawatan,bayar_operasi_operator2.biayaoperator2,bayar_operasi_operator2.tgl_operasi,"
                                + "reg_periksa.kd_pj,bayar_operasi_operator2.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                                + "from bayar_operasi_operator2 inner join reg_periksa on bayar_operasi_operator2.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join paket_operasi on bayar_operasi_operator2.kode_paket=paket_operasi.kode_paket "
                                + "where bayar_operasi_operator2.no_bayar=? order by bayar_operasi_operator2.tgl_operasi");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_operasi").substring(0, 19), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kode_paket") + " (Operasi)", rs2.getString("nm_perawatan"), Valid.SetAngka(rs2.getDouble("biayaoperator2"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //operasi operator 3
                        ps2 = koneksi.prepareStatement(
                                "select pasien.nm_pasien,paket_operasi.nm_perawatan,bayar_operasi_operator3.biayaoperator3,bayar_operasi_operator3.tgl_operasi,"
                                + "reg_periksa.kd_pj,bayar_operasi_operator3.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                                + "from bayar_operasi_operator3 inner join reg_periksa on bayar_operasi_operator3.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join paket_operasi on bayar_operasi_operator3.kode_paket=paket_operasi.kode_paket "
                                + "where bayar_operasi_operator3.no_bayar=? order by bayar_operasi_operator3.tgl_operasi");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_operasi").substring(0, 19), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kode_paket") + " (Operasi)", rs2.getString("nm_perawatan"), Valid.SetAngka(rs2.getDouble("biayaoperator3"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //operasi dokter_anak
                        ps2 = koneksi.prepareStatement(
                                "select pasien.nm_pasien,paket_operasi.nm_perawatan,bayar_operasi_dokter_anak.biayadokter_anak,bayar_operasi_dokter_anak.tgl_operasi,"
                                + "reg_periksa.kd_pj,bayar_operasi_dokter_anak.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                                + "from bayar_operasi_dokter_anak inner join reg_periksa on bayar_operasi_dokter_anak.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join paket_operasi on bayar_operasi_dokter_anak.kode_paket=paket_operasi.kode_paket "
                                + "where bayar_operasi_dokter_anak.no_bayar=? order by bayar_operasi_dokter_anak.tgl_operasi");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_operasi").substring(0, 19), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kode_paket") + " (Operasi)", rs2.getString("nm_perawatan"), Valid.SetAngka(rs2.getDouble("biayadokter_anak"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //operasi dokter_umum
                        ps2 = koneksi.prepareStatement(
                                "select pasien.nm_pasien,paket_operasi.nm_perawatan,bayar_operasi_dokter_umum.biaya_dokter_umum,bayar_operasi_dokter_umum.tgl_operasi,"
                                + "reg_periksa.kd_pj,bayar_operasi_dokter_umum.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                                + "from bayar_operasi_dokter_umum inner join reg_periksa on bayar_operasi_dokter_umum.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join paket_operasi on bayar_operasi_dokter_umum.kode_paket=paket_operasi.kode_paket "
                                + "where bayar_operasi_dokter_umum.no_bayar=? order by bayar_operasi_dokter_umum.tgl_operasi");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_operasi").substring(0, 19), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kode_paket") + " (Operasi)", rs2.getString("nm_perawatan"), Valid.SetAngka(rs2.getDouble("biaya_dokter_umum"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //operasi dokter_pjanak
                        ps2 = koneksi.prepareStatement(
                                "select pasien.nm_pasien,paket_operasi.nm_perawatan,bayar_operasi_dokter_pjanak.biaya_dokter_pjanak,bayar_operasi_dokter_pjanak.tgl_operasi,"
                                + "reg_periksa.kd_pj,bayar_operasi_dokter_pjanak.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                                + "from bayar_operasi_dokter_pjanak inner join reg_periksa on bayar_operasi_dokter_pjanak.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join paket_operasi on bayar_operasi_dokter_pjanak.kode_paket=paket_operasi.kode_paket "
                                + "where bayar_operasi_dokter_pjanak.no_bayar=? order by bayar_operasi_dokter_pjanak.tgl_operasi");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_operasi").substring(0, 19), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kode_paket") + " (Operasi)", rs2.getString("nm_perawatan"), Valid.SetAngka(rs2.getDouble("biaya_dokter_pjanak"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //operasi dokter_anestesi
                        ps2 = koneksi.prepareStatement(
                                "select pasien.nm_pasien,paket_operasi.nm_perawatan,bayar_operasi_dokter_anestesi.biayadokter_anestesi,bayar_operasi_dokter_anestesi.tgl_operasi,"
                                + "reg_periksa.kd_pj,bayar_operasi_dokter_anestesi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis "
                                + "from bayar_operasi_dokter_anestesi inner join reg_periksa on bayar_operasi_dokter_anestesi.no_rawat=reg_periksa.no_rawat "
                                + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + "inner join paket_operasi on bayar_operasi_dokter_anestesi.kode_paket=paket_operasi.kode_paket "
                                + "where bayar_operasi_dokter_anestesi.no_bayar=? order by bayar_operasi_dokter_anestesi.tgl_operasi");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_operasi").substring(0, 19), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kode_paket") + " (Operasi)", rs2.getString("nm_perawatan"), Valid.SetAngka(rs2.getDouble("biayadokter_anestesi"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //dokter pj laborat
                        ps2 = koneksi.prepareStatement(
                                "select bayar_periksa_lab.tarif_tindakan_dokter,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,"
                                + "jns_perawatan_lab.nm_perawatan,bayar_periksa_lab.tgl_periksa,bayar_periksa_lab.jam,bayar_periksa_lab.no_rawat,bayar_periksa_lab.kd_jenis_prw,reg_periksa.kd_pj "
                                + " from bayar_periksa_lab inner join reg_periksa on bayar_periksa_lab.no_rawat=reg_periksa.no_rawat "
                                + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + " inner join jns_perawatan_lab on bayar_periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "
                                + "where bayar_periksa_lab.no_bayar=? order by bayar_periksa_lab.tgl_periksa,bayar_periksa_lab.jam");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_periksa") + " " + rs2.getString("jam"), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kd_jenis_prw") + " (Laborat)", rs2.getString("nm_perawatan"), Valid.SetAngka(rs2.getDouble("tarif_tindakan_dokter"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //dokter detail laborat
                        ps2 = koneksi.prepareStatement(
                                "select bayar_detail_periksa_lab.bagian_dokter,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,"
                                + "template_laboratorium.Pemeriksaan,bayar_detail_periksa_lab.tgl_periksa,bayar_detail_periksa_lab.jam,"
                                + "bayar_detail_periksa_lab.no_rawat,bayar_detail_periksa_lab.kd_jenis_prw,reg_periksa.kd_pj "
                                + " from bayar_detail_periksa_lab inner join reg_periksa on bayar_detail_periksa_lab.no_rawat=reg_periksa.no_rawat "
                                + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + " inner join template_laboratorium on bayar_detail_periksa_lab.id_template=template_laboratorium.id_template "
                                + "where bayar_detail_periksa_lab.no_bayar=? order by bayar_detail_periksa_lab.tgl_periksa,bayar_detail_periksa_lab.jam");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_periksa") + " " + rs2.getString("jam"), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kd_jenis_prw") + " (Laborat)", rs2.getString("Pemeriksaan"), Valid.SetAngka(rs2.getDouble("bagian_dokter"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //dokter perujuk laborat
                        ps2 = koneksi.prepareStatement(
                                "select bayar_periksa_lab_perujuk.tarif_perujuk,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,"
                                + "jns_perawatan_lab.nm_perawatan,bayar_periksa_lab_perujuk.tgl_periksa,bayar_periksa_lab_perujuk.jam,bayar_periksa_lab_perujuk.no_rawat,bayar_periksa_lab_perujuk.kd_jenis_prw,reg_periksa.kd_pj "
                                + " from bayar_periksa_lab_perujuk inner join reg_periksa on bayar_periksa_lab_perujuk.no_rawat=reg_periksa.no_rawat "
                                + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + " inner join jns_perawatan_lab on bayar_periksa_lab_perujuk.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "
                                + "where bayar_periksa_lab_perujuk.no_bayar=? order by bayar_periksa_lab_perujuk.tgl_periksa,bayar_periksa_lab_perujuk.jam");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_periksa") + " " + rs2.getString("jam"), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kd_jenis_prw") + " (Laborat)", rs2.getString("nm_perawatan"), Valid.SetAngka(rs2.getDouble("tarif_perujuk"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //dokter detail perujuk laborat
                        ps2 = koneksi.prepareStatement(
                                "select bayar_detail_periksa_lab_perujuk.bagian_perujuk,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,"
                                + "template_laboratorium.Pemeriksaan,bayar_detail_periksa_lab_perujuk.tgl_periksa,bayar_detail_periksa_lab_perujuk.jam,"
                                + "bayar_detail_periksa_lab_perujuk.no_rawat,bayar_detail_periksa_lab_perujuk.kd_jenis_prw,reg_periksa.kd_pj "
                                + " from bayar_detail_periksa_lab_perujuk inner join reg_periksa on bayar_detail_periksa_lab_perujuk.no_rawat=reg_periksa.no_rawat "
                                + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + " inner join template_laboratorium on bayar_detail_periksa_lab_perujuk.id_template=template_laboratorium.id_template "
                                + "where bayar_detail_periksa_lab_perujuk.no_bayar=? order by bayar_detail_periksa_lab_perujuk.tgl_periksa,bayar_detail_periksa_lab_perujuk.jam");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_periksa") + " " + rs2.getString("jam"), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kd_jenis_prw") + " (Laborat)", rs2.getString("Pemeriksaan"), Valid.SetAngka(rs2.getDouble("bagian_perujuk"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //dokter pj radiologi
                        ps2 = koneksi.prepareStatement(
                                "select bayar_periksa_radiologi.tarif_tindakan_dokter,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,"
                                + "jns_perawatan_radiologi.nm_perawatan,bayar_periksa_radiologi.tgl_periksa,bayar_periksa_radiologi.jam,bayar_periksa_radiologi.no_rawat,bayar_periksa_radiologi.kd_jenis_prw,reg_periksa.kd_pj "
                                + " from bayar_periksa_radiologi inner join reg_periksa on bayar_periksa_radiologi.no_rawat=reg_periksa.no_rawat "
                                + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + " inner join jns_perawatan_radiologi on bayar_periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "
                                + "where bayar_periksa_radiologi.no_bayar=? order by bayar_periksa_radiologi.tgl_periksa,bayar_periksa_radiologi.jam");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_periksa") + " " + rs2.getString("jam"), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kd_jenis_prw") + " (Radiologi)", rs2.getString("nm_perawatan"), Valid.SetAngka(rs2.getDouble("tarif_tindakan_dokter"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                        //dokter perujuk radiologi
                        ps2 = koneksi.prepareStatement(
                                "select bayar_periksa_radiologi_perujuk.tarif_perujuk,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,"
                                + "jns_perawatan_radiologi.nm_perawatan,bayar_periksa_radiologi_perujuk.tgl_periksa,bayar_periksa_radiologi_perujuk.jam,bayar_periksa_radiologi_perujuk.no_rawat,bayar_periksa_radiologi_perujuk.kd_jenis_prw,reg_periksa.kd_pj "
                                + " from bayar_periksa_radiologi_perujuk inner join reg_periksa on bayar_periksa_radiologi_perujuk.no_rawat=reg_periksa.no_rawat "
                                + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                                + " inner join jns_perawatan_radiologi on bayar_periksa_radiologi_perujuk.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "
                                + "where bayar_periksa_radiologi_perujuk.no_bayar=? order by bayar_periksa_radiologi_perujuk.tgl_periksa,bayar_periksa_radiologi_perujuk.jam");
                        try {
                            ps2.setString(1, rs.getString("no_bayar"));
                            rs2 = ps2.executeQuery();
                            while (rs2.next()) {
                                tabMode.addRow(new Object[]{
                                    "", rs2.getString("tgl_periksa") + " " + rs2.getString("jam"), rs2.getString("no_rkm_medis") + " " + rs2.getString("nm_pasien") + " (" + rs2.getString("kd_pj") + ")",
                                    rs2.getString("no_rawat"), rs2.getString("kd_jenis_prw") + " (Radiologi)", rs2.getString("nm_perawatan"), Valid.SetAngka(rs2.getDouble("tarif_perujuk"))
                                });
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi : " + e);
                        } finally {
                            if (rs2 != null) {
                                rs2.close();
                            }
                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                    }
                }

                tampilRekap();
                if (tabPane != null && tabPane.getSelectedIndex() == 1) {
                    LTotal.setText(Valid.SetAngka(totalRekapNett));
                } else {
                    LTotal.setText(Valid.SetAngka(totaltagihan));
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

    private void tampilRekap() {
        if (tabModeRekap == null) {
            return;
        }
        Valid.tabelKosong(tabModeRekap);
        totalRekapJM = 0;
        totalRekapSharing = 0;
        totalRekapNett = 0;

        PreparedStatement psRekap = null;
        ResultSet rsRekap = null;
        PreparedStatement psMap = null;
        ResultSet rsMap = null;

        try {
            String wNoTagihan = "";
            String wPetugas = "";
            String wTanggal = " bayar_jm_dokter.tanggal between '" + Valid.SetTgl(Tanggal1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(Tanggal2.getSelectedItem() + "") + "' ";
            if (!NoBayar.getText().trim().equals("")) {
                wNoTagihan = " and bayar_jm_dokter.no_bayar='" + NoBayar.getText().trim() + "' ";
            }
            if (!nmdokter.getText().trim().equals("")) {
                wPetugas = " and concat(bayar_jm_dokter.kd_dokter,dokter.nm_dokter) like '%" + kddokter.getText().trim() + nmdokter.getText().trim() + "%' ";
            }

            // Cache mapping dokter agar kalkulasi cepat
            Map<String, double[]> cacheMapping = new HashMap<>();
            try {
                psMap = koneksi.prepareStatement("SELECT kd_dokter, bagiandokter, bagianrs FROM maping_jm_dokter");
                rsMap = psMap.executeQuery();
                while (rsMap.next()) {
                    cacheMapping.put(rsMap.getString("kd_dokter").trim(), new double[]{
                        rsMap.getDouble("bagiandokter"),
                        rsMap.getDouble("bagianrs")
                    });
                }
            } catch (Exception ignored) {
            } finally {
                if (rsMap != null) try { rsMap.close(); } catch (Exception ignored) {}
                if (psMap != null) try { psMap.close(); } catch (Exception ignored) {}
            }

            String sqlRekap = "SELECT bayar_jm_dokter.kd_dokter, dokter.nm_dokter, SUM(bayar_jm_dokter.besar_bayar) AS total_nett "
                    + "FROM bayar_jm_dokter "
                    + "INNER JOIN dokter ON bayar_jm_dokter.kd_dokter = dokter.kd_dokter "
                    + "WHERE " + wTanggal + wNoTagihan + wPetugas + " "
                    + "GROUP BY bayar_jm_dokter.kd_dokter, dokter.nm_dokter "
                    + "ORDER BY dokter.nm_dokter ASC";

            psRekap = koneksi.prepareStatement(sqlRekap);
            rsRekap = psRekap.executeQuery();

            int noUrut = 1;
            while (rsRekap.next()) {
                String kdDok = rsRekap.getString("kd_dokter");
                String nmDok = rsRekap.getString("nm_dokter");
                double nett = rsRekap.getDouble("total_nett");

                double persenDokter = 80.0;
                double persenRS = 20.0;
                if (cacheMapping.containsKey(kdDok.trim())) {
                    double[] map = cacheMapping.get(kdDok.trim());
                    persenDokter = map[0];
                    persenRS = map[1];
                }

                // Kalkulasi Jasa Medis (Bruto) dan Sharing Income
                double bruto = (persenDokter > 0) ? Math.round((nett / persenDokter) * 100.0) : nett;
                double sharing = bruto - nett;
                if (sharing < 0) {
                    sharing = 0;
                }

                totalRekapJM += bruto;
                totalRekapSharing += sharing;
                totalRekapNett += nett;

                tabModeRekap.addRow(new Object[]{
                    noUrut++,
                    kdDok,
                    nmDok,
                    Valid.SetAngka(bruto),
                    Valid.SetAngka(sharing),
                    Valid.SetAngka(nett),
                    Math.round(persenRS) + "% / " + Math.round(persenDokter) + "%"
                });
            }

            // Tambahkan baris total di bagian bawah tabel rekap jika ada data
            if (noUrut > 1) {
                tabModeRekap.addRow(new Object[]{
                    "",
                    "",
                    ">> TOTAL KESELURUHAN :",
                    Valid.SetAngka(totalRekapJM),
                    Valid.SetAngka(totalRekapSharing),
                    Valid.SetAngka(totalRekapNett),
                    ""
                });
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Rekap Dokter : " + e);
        } finally {
            if (rsRekap != null) try { rsRekap.close(); } catch (Exception ignored) {}
            if (psRekap != null) try { psRekap.close(); } catch (Exception ignored) {}
        }
    }

    public void emptTeks() {
        NoBayar.setText("");
        kddokter.setText("");
        nmdokter.setText("");
        BtnAll.requestFocus();
    }

    public void isCek() {
        BtnHapus.setEnabled(akses.getbayar_jm_dokter());
    }

    private void isForm() {
        if (ChkInput.isSelected() == true) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 65));
            FormInput.setVisible(true);
            ChkInput.setVisible(true);
        } else if (ChkInput.isSelected() == false) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 20));
            FormInput.setVisible(false);
            ChkInput.setVisible(true);
        }
    }
}
