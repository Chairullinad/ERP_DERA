package keuangan;

import com.fasterxml.jackson.databind.JsonNode;
import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
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
import java.util.HashMap;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import simrskhanza.DlgCariCaraBayar;
import com.itextpdf.html2pdf.HtmlConverter;

/**
 *
 * @author perpustakaan
 */
public final class KeuanganBayarJMDokter extends javax.swing.JDialog {

    private final DefaultTableModel tabMode;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private PreparedStatement ps, psrawatjalandr, psrawatjalandrpr, psrawatinapdr, psrawatinapdrpr, psbiayaoperator1, psbiayaoperator2, psbiayaoperator3, psbiayadokter_anak,
            psbiayadokter_anestesi, psdetaillab, psperiksa_lab, psperiksa_radiologi, psperiksa_lab2, psdetaillab2, psperiksa_radiologi2, psbiaya_dokter_pjanak, psbiaya_dokter_umum, pscekCOB;
    private ResultSet rs, rsrawatjalandr, rsrawatjalandrpr, rsrawatinapdr, rsrawatinapdrpr, rsbiayaoperator1, rsbiayaoperator2, rsbiayaoperator3, rsbiayadokter_anak,
            rsbiayadokter_anestesi, rsdetaillab, rsperiksa_lab, rsperiksa_radiologi, rsbiaya_dokter_pjanak, rsbiaya_dokter_umum, rsCekCOB;
    private DlgCariCaraBayar carabayar = new DlgCariCaraBayar(null, false);
    private int row = 0, i = 0;
    private String filterbilling = "";
    private double total = 0, bayar = 0, totalrawatjalan = 0, totalrawatinap = 0, totallabrawatjalan = 0, totallabrawatinap = 0, totalradrawatjalan = 0, totalradrawatinap = 0, totaloperasirawatjalan = 0, totaloperasirawatinap = 0;
    private boolean sukses = true;
    private KeuanganCariBayarJMDokter form = new KeuanganCariBayarJMDokter(null, false);
    private KeuanganMappingJMDokter mappingJMDokter = new KeuanganMappingJMDokter(null, false);
    private Map<String, Double> jmMappingCache = new HashMap<>();
    private Map<String, Double> rsMappingCache = new HashMap<>();
    private double currentBruto = 0;
    private double currentSharing = 0;
    // Set untuk mencegah diskon ganda pada tindakan operasi yang sama di 1 no_rawat
    // (data lama yang belum punya entri di tabel diskon_ibs)
    private java.util.Set<String> sdahHitungDiskon = new java.util.HashSet<>();
    private java.util.Set<String> sdahTampilDiskon = new java.util.HashSet<>();

    /**
     * Creates new form DlgLhtBiaya
     *
     * @param parent
     * @param modal
     */
    public KeuanganBayarJMDokter(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        ChkMapping.setSelected(true);
        this.setLocation(8, 1);
        setSize(885, 674);

        tabMode = new DefaultTableModel(null, new Object[]{
            "P", "Tanggal Trx", "Jam Trx", "No.Rawat", "No.RM", "Nama Pasien", "ID Tarif", "Tindakan Medis",
            "Status", "Bruto", "Sharing", "Jasa Medis", "Id Detail", "Payor", "Tgl Pelunasan/Tgl Billing",
            "Kode Dokter", "Nama Dokter", "No. Nota"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                boolean a = false;
                if ((colIndex == 0) || (colIndex == 17)) {
                    a = true;
                }
                return a;
            }
            Class[] types = new Class[]{
                java.lang.Boolean.class, //"P"
                java.lang.Object.class, //"Tanggal Trx"
                java.lang.Object.class, //"Jam Trx"
                java.lang.Object.class, //"No.Rawat"
                java.lang.Object.class, //"No.RM"
                java.lang.Object.class, //"Nama Pasien"
                java.lang.Object.class, //"ID Tarif"
                java.lang.Object.class, //"Tindakan Medis"
                java.lang.Object.class, //"Status"
                java.lang.Double.class, //"Bruto"
                java.lang.Double.class, //"Sharing"
                java.lang.Double.class,//"Jasa Medis"
                java.lang.Object.class,
                java.lang.Object.class,
                java.lang.Object.class, //"Tgl Pelunasan/Tgl Billing"
                java.lang.Object.class, //"Kode Dokter"
                java.lang.Object.class, //"Nama Dokter"
                java.lang.Object.class //"No. Nota"
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            @Override
            public void addRow(Object[] rowData) {
                if (rowData != null) {
                    if (rowData.length > 13 && !isFilterCOBPass(rowData[13])) {
                        return;
                    }
                    if (rowData.length > 7 && !isFilterPotonganPass(rowData[7])) {
                        return;
                    }
                }
                if (javax.swing.SwingUtilities.isEventDispatchThread()) {
                    super.addRow(rowData);
                } else {
                    javax.swing.SwingUtilities.invokeLater(() -> super.addRow(rowData));
                }
            }
        };
        tbBangsal.setModel(tabMode);
        //tbBangsal.setDefaultRenderer(Object.class, new WarnaTable(jPanel2.getBackground(),tbBangsal.getBackground()));
        tbBangsal.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbBangsal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < 18; i++) {
            TableColumn column = tbBangsal.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(22);
            } else if (i == 1) {
                column.setPreferredWidth(80);
            } else if (i == 2) {
                column.setPreferredWidth(65);
            } else if (i == 3) {
                column.setPreferredWidth(115);
            } else if (i == 4) {
                column.setPreferredWidth(70);
            } else if (i == 5) {
                column.setPreferredWidth(180);
            } else if (i == 6) {
                column.setPreferredWidth(70);
            } else if (i == 7) {
                column.setPreferredWidth(240);
            } else if (i == 8) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 9) {
                column.setPreferredWidth(80);
            } else if (i == 10) {
                column.setPreferredWidth(80);
            } else if (i == 11) {
                column.setPreferredWidth(80);
            } else if (i == 12) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 13) {
                column.setPreferredWidth(250);
            } else if (i == 14) {
                column.setPreferredWidth(80);
            } else if (i == 15) {
                column.setPreferredWidth(85);
            } else if (i == 16) {
                column.setPreferredWidth(170);
            } else if (i == 17) {
                column.setPreferredWidth(120);
            }
        }
        tbBangsal.getColumnModel().getColumn(17).setCellRenderer(new ButtonRenderNota());
        tbBangsal.getColumnModel().getColumn(17).setCellEditor(new ButtonEditorNota(new javax.swing.JTextField()));
        tbBangsal.setDefaultRenderer(Object.class, new WarnaTable());
        tbBangsal.setRowSorter(null);

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
                    NmCaraBayar.setText(carabayar.getTable().getValueAt(carabayar.getTable().getSelectedRow(), 2).toString());
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

        // Fallback guard agar filter COB tetap terpasang jika NetBeans me-regenerate initComponents()
        if (cmbCOB == null) {
            jLabelCOB = new widget.Label();
            jLabelCOB.setText("COB :");
            jLabelCOB.setName("jLabelCOB");
            jLabelCOB.setPreferredSize(new java.awt.Dimension(40, 23));
            panelisi5.add(jLabelCOB);

            cmbCOB = new widget.ComboBox();
            cmbCOB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Tanpa COB JKN", "Non COB", "Hanya COB", "COB JKN", "COB Non JKN" }));
            cmbCOB.setName("cmbCOB");
            cmbCOB.setPreferredSize(new java.awt.Dimension(135, 23));
            cmbCOB.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    cmbCOBItemStateChanged(evt);
                }
            });
            panelisi5.add(cmbCOB);
            panelisi5.revalidate();
        }

        // Fallback guard agar filter Potongan tetap terpasang jika NetBeans me-regenerate initComponents()
        if (cmbPotongan == null) {
            jLabelPotongan = new widget.Label();
            jLabelPotongan.setText("Potongan :");
            jLabelPotongan.setName("jLabelPotongan");
            jLabelPotongan.setPreferredSize(new java.awt.Dimension(60, 23));
            panelisi5.add(jLabelPotongan);

            cmbPotongan = new widget.ComboBox();
            cmbPotongan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Ada Potongan", "Tanpa Potongan" }));
            cmbPotongan.setName("cmbPotongan");
            cmbPotongan.setPreferredSize(new java.awt.Dimension(110, 23));
            cmbPotongan.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent evt) {
                    cmbPotonganItemStateChanged(evt);
                }
            });
            panelisi5.add(cmbPotongan);
            panelisi5.revalidate();
        }
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
        ppSlipGajiHTML = new javax.swing.JMenuItem();
        ppPiutangHTML = new javax.swing.JMenuItem();
        KdCaraBayar = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbBangsal = new widget.Table();
        jPanel1 = new javax.swing.JPanel();
        panelisi3 = new widget.panelisi();
        label19 = new widget.Label();
        NmCaraBayar = new widget.TextBox();
        BtnCaraBayarRalanDokter = new widget.Button();
        label17 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        BtnGaji = new widget.Button();
        label18 = new widget.Label();
        panelisi1 = new widget.panelisi();
        jLabel10 = new javax.swing.JLabel();
        LCount = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        LCount1 = new javax.swing.JLabel();
        BtnSimpan = new widget.Button();
        BtnCari1 = new widget.Button();
        BtnExport = new widget.Button();
        BtnKeluar = new widget.Button();
        panelisi5 = new widget.panelisi();
        label11 = new widget.Label();
        TglBilling1 = new widget.Tanggal();
        label20 = new widget.Label();
        TglBilling2 = new widget.Tanggal();
        chkRalan = new widget.CekBox();
        chkRanap = new widget.CekBox();
        chkOperasi = new widget.CekBox();
        chkLaborat = new widget.CekBox();
        chkRadiologi = new widget.CekBox();
        jLabel12 = new widget.Label();
        cmbStatus = new widget.ComboBox();
        jLabelCOB = new widget.Label();
        cmbCOB = new widget.ComboBox();
        jLabelPotongan = new widget.Label();
        cmbPotongan = new widget.ComboBox();
        panelisi4 = new widget.panelisi();
        label32 = new widget.Label();
        label36 = new widget.Label();
        NoTagihan = new widget.TextBox();
        label16 = new widget.Label();
        kddokter = new widget.TextBox();
        nmdokter = new widget.TextBox();
        Tanggal = new widget.Tanggal();
        BtnPetugas = new widget.Button();
        ChkSemuaDokter = new widget.CekBox();
        Keterangan = new widget.TextBox();
        label39 = new widget.Label();
        jLabel13 = new widget.Label();
        AkunBayar = new widget.ComboBox();
        BtnAll1 = new widget.Button();
        BtnPetugas1 = new widget.Button();
        ChkMapping = new widget.CekBox();

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

        ppSlipGajiHTML.setBackground(new java.awt.Color(255, 255, 254));
        ppSlipGajiHTML.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppSlipGajiHTML.setForeground(new java.awt.Color(50, 50, 50));
        ppSlipGajiHTML.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        ppSlipGajiHTML.setText("Cetak Slip Gaji (PDF)");
        ppSlipGajiHTML.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppSlipGajiHTML.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppSlipGajiHTML.setName("ppSlipGajiHTML"); // NOI18N
        ppSlipGajiHTML.setPreferredSize(new java.awt.Dimension(200, 25));
        ppSlipGajiHTML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppSlipGajiHTMLActionPerformed(evt);
            }
        });
        Popup.add(ppSlipGajiHTML);
        
        ppPiutangHTML.setBackground(new java.awt.Color(255, 255, 254));
        ppPiutangHTML.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppPiutangHTML.setForeground(new java.awt.Color(50, 50, 50));
        ppPiutangHTML.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        ppPiutangHTML.setText("Cetak Piutang Jasa Medis (PDF)");
        ppPiutangHTML.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppPiutangHTML.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppPiutangHTML.setName("ppPiutangHTML"); // NOI18N
        ppPiutangHTML.setPreferredSize(new java.awt.Dimension(220, 25));
        ppPiutangHTML.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppPiutangHTMLActionPerformed(evt);
            }
        });
        Popup.add(ppPiutangHTML);

        KdCaraBayar.setEditable(false);
        KdCaraBayar.setName("KdCaraBayar"); // NOI18N
        KdCaraBayar.setPreferredSize(new java.awt.Dimension(50, 23));

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

        Scroll.setComponentPopupMenu(Popup);
        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbBangsal.setToolTipText("Silahkan centang di depan untuk memilih data hutang yang mau dibayar");
        tbBangsal.setComponentPopupMenu(Popup);
        tbBangsal.setName("tbBangsal"); // NOI18N
        tbBangsal.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbBangsalMouseClicked(evt);
            }
        });
        tbBangsal.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                tbBangsalPropertyChange(evt);
            }
        });
        tbBangsal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbBangsalKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbBangsal);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(816, 145));
        jPanel1.setLayout(new java.awt.BorderLayout(1, 1));

        panelisi3.setName("panelisi3"); // NOI18N
        panelisi3.setPreferredSize(new java.awt.Dimension(100, 44));
        panelisi3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        label19.setText("Cara Bayar :");
        label19.setName("label19"); // NOI18N
        label19.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi3.add(label19);

        NmCaraBayar.setEditable(false);
        NmCaraBayar.setName("NmCaraBayar"); // NOI18N
        NmCaraBayar.setPreferredSize(new java.awt.Dimension(175, 23));
        panelisi3.add(NmCaraBayar);

        BtnCaraBayarRalanDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnCaraBayarRalanDokter.setMnemonic('3');
        BtnCaraBayarRalanDokter.setToolTipText("Alt+3");
        BtnCaraBayarRalanDokter.setName("BtnCaraBayarRalanDokter"); // NOI18N
        BtnCaraBayarRalanDokter.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCaraBayarRalanDokter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCaraBayarRalanDokterActionPerformed(evt);
            }
        });
        panelisi3.add(BtnCaraBayarRalanDokter);

        label17.setText("Key Word :");
        label17.setName("label17"); // NOI18N
        label17.setPreferredSize(new java.awt.Dimension(85, 23));
        panelisi3.add(label17);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(200, 23));
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
        BtnAll.setText("Reset Filter");
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setFont(new java.awt.Font("Roboto", 0, 12)); // NOI18N
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(100, 23));
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
        BtnGaji.setText("Slip Gaji");
        BtnGaji.setToolTipText("Alt+2");
        BtnGaji.setFont(new java.awt.Font("Roboto", 0, 12)); // NOI18N
        BtnGaji.setName("BtnGaji"); // NOI18N
        BtnGaji.setPreferredSize(new java.awt.Dimension(100, 23));
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

        label18.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        label18.setText("Data yang ditampilkan sudah exclude JKN");
        label18.setFont(new java.awt.Font("Roboto", 3, 12)); // NOI18N
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(250, 23));
        panelisi3.add(label18);

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

        BtnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Export.png"))); // NOI18N
        BtnExport.setMnemonic('X');
        BtnExport.setText("Export");
        BtnExport.setToolTipText("Alt+X");
        BtnExport.setName("BtnExport"); // NOI18N
        BtnExport.setPreferredSize(new java.awt.Dimension(100, 30));
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

        panelisi5.setName("panelisi5"); // NOI18N
        panelisi5.setPreferredSize(new java.awt.Dimension(100, 44));
        panelisi5.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        label11.setText("Tgl Billing :");
        label11.setName("label11"); // NOI18N
        label11.setPreferredSize(new java.awt.Dimension(65, 23));
        panelisi5.add(label11);

        TglBilling1.setDisplayFormat("dd-MM-yyyy");
        TglBilling1.setFont(new java.awt.Font("Roboto", 0, 12)); // NOI18N
        TglBilling1.setName("TglBilling1"); // NOI18N
        TglBilling1.setPreferredSize(new java.awt.Dimension(85, 23));
        TglBilling1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TglBilling1KeyPressed(evt);
            }
        });
        panelisi5.add(TglBilling1);

        label20.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label20.setText("-");
        label20.setName("label20"); // NOI18N
        label20.setPreferredSize(new java.awt.Dimension(10, 23));
        panelisi5.add(label20);

        TglBilling2.setDisplayFormat("dd-MM-yyyy");
        TglBilling2.setFont(new java.awt.Font("Roboto", 0, 12)); // NOI18N
        TglBilling2.setName("TglBilling2"); // NOI18N
        TglBilling2.setPreferredSize(new java.awt.Dimension(85, 23));
        TglBilling2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TglBilling2KeyPressed(evt);
            }
        });
        panelisi5.add(TglBilling2);

        chkRalan.setSelected(true);
        chkRalan.setText("Rawat Jalan");
        chkRalan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chkRalan.setName("chkRalan"); // NOI18N
        chkRalan.setOpaque(false);
        chkRalan.setPreferredSize(new java.awt.Dimension(100, 23));
        chkRalan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkRalanActionPerformed(evt);
            }
        });
        panelisi5.add(chkRalan);

        chkRanap.setSelected(true);
        chkRanap.setText("Rawat Inap");
        chkRanap.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chkRanap.setName("chkRanap"); // NOI18N
        chkRanap.setOpaque(false);
        chkRanap.setPreferredSize(new java.awt.Dimension(100, 23));
        chkRanap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkRanapActionPerformed(evt);
            }
        });
        panelisi5.add(chkRanap);

        chkOperasi.setSelected(true);
        chkOperasi.setText("Operasi");
        chkOperasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chkOperasi.setName("chkOperasi"); // NOI18N
        chkOperasi.setOpaque(false);
        chkOperasi.setPreferredSize(new java.awt.Dimension(85, 23));
        chkOperasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkOperasiActionPerformed(evt);
            }
        });
        panelisi5.add(chkOperasi);

        chkLaborat.setSelected(true);
        chkLaborat.setText("Laboratorium");
        chkLaborat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chkLaborat.setName("chkLaborat"); // NOI18N
        chkLaborat.setOpaque(false);
        chkLaborat.setPreferredSize(new java.awt.Dimension(110, 23));
        chkLaborat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkLaboratActionPerformed(evt);
            }
        });
        panelisi5.add(chkLaborat);

        chkRadiologi.setSelected(true);
        chkRadiologi.setText("Radiologi");
        chkRadiologi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        chkRadiologi.setName("chkRadiologi"); // NOI18N
        chkRadiologi.setOpaque(false);
        chkRadiologi.setPreferredSize(new java.awt.Dimension(90, 23));
        chkRadiologi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkRadiologiActionPerformed(evt);
            }
        });
        panelisi5.add(chkRadiologi);

        jLabel12.setText("Status :");
        jLabel12.setName("jLabel12"); // NOI18N
        jLabel12.setPreferredSize(new java.awt.Dimension(55, 23));
        panelisi5.add(jLabel12);

        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Pilih Kategori", "Piutang Sudah Lunas", "Piutang Belum Lunas atau Ditagihkan", "Sudah Bayar Non Piutang", " " }));
        cmbStatus.setName("cmbStatus"); // NOI18N
        cmbStatus.setPreferredSize(new java.awt.Dimension(125, 23));
        panelisi5.add(cmbStatus);

        jLabelCOB.setText("COB :");
        jLabelCOB.setName("jLabelCOB"); // NOI18N
        jLabelCOB.setPreferredSize(new java.awt.Dimension(40, 23));
        panelisi5.add(jLabelCOB);

        cmbCOB.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Tanpa COB JKN", "Non COB", "Hanya COB", "COB JKN", "COB Non JKN" }));
        cmbCOB.setName("cmbCOB"); // NOI18N
        cmbCOB.setPreferredSize(new java.awt.Dimension(135, 23));
        cmbCOB.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbCOBItemStateChanged(evt);
            }
        });
        panelisi5.add(cmbCOB);

        jLabelPotongan.setText("Potongan :");
        jLabelPotongan.setName("jLabelPotongan"); // NOI18N
        jLabelPotongan.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi5.add(jLabelPotongan);

        cmbPotongan.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Ada Potongan", "Tanpa Potongan" }));
        cmbPotongan.setName("cmbPotongan"); // NOI18N
        cmbPotongan.setPreferredSize(new java.awt.Dimension(110, 23));
        cmbPotongan.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbPotonganItemStateChanged(evt);
            }
        });
        panelisi5.add(cmbPotongan);

        jPanel1.add(panelisi5, java.awt.BorderLayout.PAGE_START);

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
        kddokter.setBounds(437, 10, 85, 23);

        nmdokter.setEditable(false);
        nmdokter.setName("nmdokter"); // NOI18N
        nmdokter.setPreferredSize(new java.awt.Dimension(207, 23));
        panelisi4.add(nmdokter);
        nmdokter.setBounds(524, 10, 160, 23);

        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                TanggalItemStateChanged(evt);
            }
        });
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
        BtnPetugas.setBounds(686, 10, 28, 23);

        ChkSemuaDokter.setText("Semua Dr");
        ChkSemuaDokter.setName("ChkSemuaDokter"); // NOI18N
        ChkSemuaDokter.setPreferredSize(new java.awt.Dimension(95, 23));
        ChkSemuaDokter.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ChkSemuaDokterItemStateChanged(evt);
            }
        });
        panelisi4.add(ChkSemuaDokter);
        ChkSemuaDokter.setBounds(718, 10, 95, 23);

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

        BtnPetugas1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPetugas1.setMnemonic('1');
        BtnPetugas1.setText("Mapping JM");
        BtnPetugas1.setToolTipText("ALt+1");
        BtnPetugas1.setName("BtnPetugas1"); // NOI18N
        BtnPetugas1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPetugas1ActionPerformed(evt);
            }
        });
        BtnPetugas1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPetugas1KeyPressed(evt);
            }
        });
        panelisi4.add(BtnPetugas1);
        BtnPetugas1.setBounds(818, 10, 120, 23);

        ChkMapping.setText("Hitung berdasarkan mapping");
        ChkMapping.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkMapping.setName("ChkMapping"); // NOI18N
        ChkMapping.setOpaque(false);
        ChkMapping.setPreferredSize(new java.awt.Dimension(100, 23));
        ChkMapping.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkMappingActionPerformed(evt);
            }
        });
        panelisi4.add(ChkMapping);
        ChkMapping.setBounds(770, 40, 200, 23);

        internalFrame1.add(panelisi4, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
        KdCaraBayar.setText("");
        NmCaraBayar.setText("");
        if (cmbCOB != null) {
            cmbCOB.setSelectedIndex(0);
        }
        if (cmbPotongan != null) {
            cmbPotongan.setSelectedIndex(0);
        }
        prosesCari();
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnAllActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnCari1, BtnKeluar);
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
        if (cmbStatus.getSelectedIndex() != 0) {
            prosesCari();
        } else {
            JOptionPane.showMessageDialog(null, "Maaf,pilih data kategori / status terlebih dahulu");
        }
}//GEN-LAST:event_BtnCariActionPerformed

private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            prosesCari();
        } else {
            Valid.pindah(evt, TKd, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    /**
     * Pembulatan ke atas dengan 2 digit desimal agar hasil kalkulasi konsisten
     */
    private double roundTwoDecimalsUp(double val) {
        return Math.ceil(val * 100.0) / 100.0;
    }

    /**
     * Calculate JM tariff based on doctor mapping If mapping exists, calculate
     * using bagiandokter percentage If no mapping, return original tariff Uses
     * caching to avoid repeated database queries
     *
     * @param kddokter Doctor code
     * @param originalTariff Original tariff amount from ResultSet
     * @return Calculated JM tariff (doctor's share only)
     */
    private double hitungJMDokter(String kddokter, double originalTariff) {
        // Set Bruto (original tariff)
        currentBruto = originalTariff;
        currentSharing = 0;

        // If ChkMapping is not selected, return original tariff without calculation
        if (!ChkMapping.isSelected()) {
            return originalTariff;
        }

        // Validation: null/blank kd_dokter or zero/negative tariff
        if (kddokter == null || kddokter.trim().isEmpty() || originalTariff <= 0) {
            return originalTariff;
        }

        String kdDokterTrimmed = kddokter.trim();

        // Check cache first for dokter percentage
        Double persenDokter = jmMappingCache.get(kdDokterTrimmed);
        Double persenRS = rsMappingCache.get(kdDokterTrimmed);

        if (persenDokter != null && persenDokter > 0) {
            // Calculate doctor's share
            double jmTariff = roundTwoDecimalsUp((originalTariff * persenDokter) / 100.0);
            // Balancing: calculate hospital's share as remainder
            if (persenRS != null && persenRS > 0) {
                currentSharing = roundTwoDecimalsUp(originalTariff - jmTariff);
                if (currentSharing < 0) {
                    currentSharing = 0;
                }
            }
            return jmTariff;
        }

        // Query database and cache result
        try {
            String sql = "SELECT bagiandokter, bagianrs FROM maping_jm_dokter WHERE kd_dokter = ?";
            PreparedStatement psMapping = koneksi.prepareStatement(sql);
            psMapping.setString(1, kdDokterTrimmed);
            ResultSet rsMapping = psMapping.executeQuery();

            double jmTariff = originalTariff;
            if (rsMapping.next()) {
                persenDokter = rsMapping.getDouble("bagiandokter");
                persenRS = rsMapping.getDouble("bagianrs");

                if (persenDokter > 0) {
                    jmTariff = roundTwoDecimalsUp((originalTariff * persenDokter) / 100.0);
                    jmMappingCache.put(kdDokterTrimmed, persenDokter);
                }

                if (persenRS > 0) {
                    currentSharing = roundTwoDecimalsUp(originalTariff - jmTariff);
                    if (currentSharing < 0) {
                        currentSharing = 0;
                    }
                    rsMappingCache.put(kdDokterTrimmed, persenRS);
                }

                if (persenDokter == null || persenDokter <= 0) {
                    jmMappingCache.put(kdDokterTrimmed, null);
                }
                if (persenRS == null || persenRS <= 0) {
                    rsMappingCache.put(kdDokterTrimmed, null);
                }
            } else {
                jmMappingCache.put(kdDokterTrimmed, null); // Cache absence of mapping
                rsMappingCache.put(kdDokterTrimmed, null);
            }

            rsMapping.close();
            psMapping.close();
            return jmTariff;

        } catch (SQLException e) {
            System.out.println("Error calculating JM for doctor " + kddokter + ": " + e);
            return originalTariff; // Fallback to original on error
        }
    }

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        try {
            if (Valid.daysOld("./cache/akunbayarhutang.iyem") < 8) {
                tampilAkunBayar2();
            } else {
                tampilAkunBayar();
            }
        } catch (Exception e) {
        }
    }//GEN-LAST:event_formWindowOpened

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (ChkSemuaDokter.isSelected()) {
            this.setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(null, "Penyimpanan pembayaran jasa medis hanya dapat diproses per dokter spesifik demi integritas pencatatan tagihan dan jurnal akuntansi!\nSilakan hilangkan centang 'Semua Dr' dan pilih dokter terkait.");
            return;
        }
        if (NoTagihan.getText().trim().equals("")) {
            Valid.textKosong(NoTagihan, "No.Bayar");
        } else if (kddokter.getText().trim().equals("") || nmdokter.getText().trim().equals("")) {
            Valid.textKosong(kddokter, "Petugas");
        } else if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda simpan...!!!!");
        } else if (Keterangan.getText().trim().equals("")) {
            Valid.textKosong(Keterangan, "Keterangan");
        } else if (bayar <= 0) {
            JOptionPane.showMessageDialog(null, "Maaf, Silahkan masukkan tagihan...!!!!");
            tbBangsal.requestFocus();
        } else {
            i = JOptionPane.showConfirmDialog(rootPane, "Eeiiiiiits, udah bener belum data yang mau disimpan..??", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (i == JOptionPane.YES_OPTION) {
                Sequel.AutoComitFalse();
                sukses = true;

                form.koderekening = "";
                try {
                    form.myObj = new FileReader("./cache/akunbayarhutang.iyem");
                    form.root = form.mapper.readTree(form.myObj);
                    form.response = form.root.path("akunbayarhutang");
                    if (form.response.isArray()) {
                        for (JsonNode list : form.response) {
                            if (list.path("NamaAkun").asText().equals(AkunBayar.getSelectedItem().toString())) {
                                form.koderekening = list.path("KodeRek").asText();
                            }
                        }
                    }
                    form.myObj.close();
                } catch (Exception e) {
                    sukses = false;
                }

                if (Sequel.menyimpantf2("bayar_jm_dokter", "?,?,?,?,?,?,?,?,?,?,?,?,?,?", "No.Tagihan", 14, new String[]{
                    NoTagihan.getText(), Valid.SetTgl(Tanggal.getSelectedItem() + ""), kddokter.getText(), bayar + "", AkunBayar.getSelectedItem().toString(), Keterangan.getText(),
                    totalrawatjalan + "", totalrawatinap + "", totallabrawatjalan + "", totallabrawatinap + "", totalradrawatjalan + "", totalradrawatinap + "", totaloperasirawatjalan + "", totaloperasirawatinap + ""
                }) == true) {
                    row = tbBangsal.getRowCount();
                    for (i = 0; i < row; i++) {
                        //"P"0,"Tanggal"1,"Jam"2,"No.Rawat"3,"No.RM"4,"Nama Pasien"5,"Kode "6,"Tindakan Medis"7,"Status"8,"Jasa Medis"9
                        if (tabMode.getValueAt(i, 0).toString().equals("true")) {
                            // Sanitasi kode paket agar murni mengambil kode tanpa deskripsi tindakan (mencegah data truncation pada kolom kode_paket)
                            String kodePaket = tbBangsal.getValueAt(i, 6).toString().trim();
                            if (kodePaket.contains(" ")) {
                                kodePaket = kodePaket.substring(0, kodePaket.indexOf(" ")).trim();
                            }

                            if (tbBangsal.getValueAt(i, 8).toString().equals("Rawat Jalan Dr")) {
                                if (Sequel.menyimpantf2("bayar_rawat_jl_dr", "?,?,?,?,?,?", 6, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Rawat Jalan DrPr")) {
                                if (Sequel.menyimpantf2("bayar_rawat_jl_drpr", "?,?,?,?,?,?", 6, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Rawat Inap Dr")) {
                                if (Sequel.menyimpantf2("bayar_rawat_inap_dr", "?,?,?,?,?,?", 6, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Rawat Inap DrPr")) {
                                if (Sequel.menyimpantf2("bayar_rawat_inap_drpr", "?,?,?,?,?,?", 6, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Laborat Ralan PJ")) {
                                if (Sequel.menyimpantf2("bayar_periksa_lab", "?,?,?,?,?,?", 6, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Laborat Ralan PJ Detail")) {
                                if (Sequel.menyimpantf2("bayar_detail_periksa_lab", "?,?,?,?,?,?,?", 7, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 10).toString(),
                                    tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Laborat Ralan Perujuk")) {
                                if (Sequel.menyimpantf2("bayar_periksa_lab_perujuk", "?,?,?,?,?,?", 6, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Laborat Ralan Perujuk Detail")) {
                                if (Sequel.menyimpantf2("bayar_detail_periksa_lab_perujuk", "?,?,?,?,?,?,?", 7, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 10).toString(),
                                    tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Laborat Ranap PJ")) {
                                if (Sequel.menyimpantf2("bayar_periksa_lab", "?,?,?,?,?,?", 6, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Laborat Ranap PJ Detail")) {
                                if (Sequel.menyimpantf2("bayar_detail_periksa_lab", "?,?,?,?,?,?,?", 7, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 10).toString(),
                                    tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Laborat Ranap Perujuk")) {
                                if (Sequel.menyimpantf2("bayar_periksa_lab_perujuk", "?,?,?,?,?,?", 6, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Laborat Ranap Perujuk Detail")) {
                                if (Sequel.menyimpantf2("bayar_detail_periksa_lab_perujuk", "?,?,?,?,?,?,?", 7, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 10).toString(),
                                    tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Radiologi Ralan PJ")) {
                                if (Sequel.menyimpantf2("bayar_periksa_radiologi", "?,?,?,?,?,?", 6, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Radiologi Ralan Perujuk")) {
                                if (Sequel.menyimpantf2("bayar_periksa_radiologi_perujuk", "?,?,?,?,?,?", 6, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Radiologi Ranap PJ")) {
                                if (Sequel.menyimpantf2("bayar_periksa_radiologi", "?,?,?,?,?,?", 6, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Radiologi Ranap Perujuk")) {
                                if (Sequel.menyimpantf2("bayar_periksa_radiologi_perujuk", "?,?,?,?,?,?", 6, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), tbBangsal.getValueAt(i, 6).toString(),
                                    tbBangsal.getValueAt(i, 1).toString(), tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Operasi Ralan Op1")) {
                                if (Sequel.menyimpantf2("bayar_operasi_operator1", "?,?,?,?,?", 5, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), kodePaket,
                                    tbBangsal.getValueAt(i, 1).toString() + " " + tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Operasi Ralan Op2")) {
                                if (Sequel.menyimpantf2("bayar_operasi_operator2", "?,?,?,?,?", 5, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), kodePaket,
                                    tbBangsal.getValueAt(i, 1).toString() + " " + tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Operasi Ralan Op3")) {
                                if (Sequel.menyimpantf2("bayar_operasi_operator3", "?,?,?,?,?", 5, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), kodePaket,
                                    tbBangsal.getValueAt(i, 1).toString() + " " + tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Operasi Ralan dr Anak")) {
                                if (Sequel.menyimpantf2("bayar_operasi_dokter_anak", "?,?,?,?,?", 5, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), kodePaket,
                                    tbBangsal.getValueAt(i, 1).toString() + " " + tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Operasi Ralan dr Anastesi")) {
                                if (Sequel.menyimpantf2("bayar_operasi_dokter_anestesi", "?,?,?,?,?", 5, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), kodePaket,
                                    tbBangsal.getValueAt(i, 1).toString() + " " + tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Operasi Ralan dr PJ Anak")) {
                                if (Sequel.menyimpantf2("bayar_operasi_dokter_pjanak", "?,?,?,?,?", 5, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), kodePaket,
                                    tbBangsal.getValueAt(i, 1).toString() + " " + tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Operasi Ralan dr Umum")) {
                                if (Sequel.menyimpantf2("bayar_operasi_dokter_umum", "?,?,?,?,?", 5, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), kodePaket,
                                    tbBangsal.getValueAt(i, 1).toString() + " " + tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Operasi Ranap Op1")) {
                                if (Sequel.menyimpantf2("bayar_operasi_operator1", "?,?,?,?,?", 5, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), kodePaket,
                                    tbBangsal.getValueAt(i, 1).toString() + " " + tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Operasi Ranap Op2")) {
                                if (Sequel.menyimpantf2("bayar_operasi_operator2", "?,?,?,?,?", 5, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), kodePaket,
                                    tbBangsal.getValueAt(i, 1).toString() + " " + tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Operasi Ranap Op3")) {
                                if (Sequel.menyimpantf2("bayar_operasi_operator3", "?,?,?,?,?", 5, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), kodePaket,
                                    tbBangsal.getValueAt(i, 1).toString() + " " + tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Operasi Ranap dr Anak")) {
                                if (Sequel.menyimpantf2("bayar_operasi_dokter_anak", "?,?,?,?,?", 5, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), kodePaket,
                                    tbBangsal.getValueAt(i, 1).toString() + " " + tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Operasi Ranap dr Anastesi")) {
                                if (Sequel.menyimpantf2("bayar_operasi_dokter_anestesi", "?,?,?,?,?", 5, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), kodePaket,
                                    tbBangsal.getValueAt(i, 1).toString() + " " + tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Operasi Ranap dr PJ Anak")) {
                                if (Sequel.menyimpantf2("bayar_operasi_dokter_pjanak", "?,?,?,?,?", 5, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), kodePaket,
                                    tbBangsal.getValueAt(i, 1).toString() + " " + tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            } else if (tbBangsal.getValueAt(i, 8).toString().equals("Operasi Ranap dr Umum")) {
                                if (Sequel.menyimpantf2("bayar_operasi_dokter_umum", "?,?,?,?,?", 5, new String[]{
                                    NoTagihan.getText(), tbBangsal.getValueAt(i, 3).toString(), kodePaket,
                                    tbBangsal.getValueAt(i, 1).toString() + " " + tbBangsal.getValueAt(i, 2).toString(), tbBangsal.getValueAt(i, 9).toString()
                                }) == false) {
                                    sukses = false;
                                }
                            }
                        }
                    }
                } else {
                    sukses = false;
                    JOptionPane.showMessageDialog(rootPane, "Gagal Menyimpan, kemungkinan Nomor J.M. sudah ada sebelumnya...!!");
                }

                if (sukses == true) {
                    Sequel.queryu("delete from tampjurnal");

                    /* JURNAL LAMA (BAWAAN KHANZA) - DIKOMEN:
                    if (totalrawatjalan > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Tindakan_Ralan + "','Utang Jasa Medik Dokter Tindakan Ralan','" + totalrawatjalan + "','0'", "debet=debet+'" + (totalrawatjalan) + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Tindakan_Ralan + "'");
                        Sequel.menyimpan("tampjurnal", "'" + form.Beban_Jasa_Medik_Dokter_Tindakan_Ralan + "','Beban Jasa Medik Dokter Tindakan Ralan','0','" + totalrawatjalan + "'", "kredit=kredit+'" + (totalrawatjalan) + "'", "kd_rek='" + form.Beban_Jasa_Medik_Dokter_Tindakan_Ralan + "'");
                    }
                    if (totalrawatinap > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Tindakan_Ranap + "','Utang Jasa Medik Dokter Tindakan Ranap','" + totalrawatinap + "','0'", "debet=debet+'" + (totalrawatinap) + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Tindakan_Ranap + "'");
                        Sequel.menyimpan("tampjurnal", "'" + form.Beban_Jasa_Medik_Dokter_Tindakan_Ranap + "','Beban Jasa Medik Dokter Tindakan Ranap','0','" + totalrawatinap + "'", "kredit=kredit+'" + (totalrawatinap) + "'", "kd_rek='" + form.Beban_Jasa_Medik_Dokter_Tindakan_Ranap + "'");
                    }
                    if (totallabrawatjalan > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Laborat_Ralan + "','Utang Jasa Medik Dokter Laborat Ralan','" + totallabrawatjalan + "','0'", "debet=debet+'" + (totallabrawatjalan) + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Laborat_Ralan + "'");
                        Sequel.menyimpan("tampjurnal", "'" + form.Beban_Jasa_Medik_Dokter_Laborat_Ralan + "','Beban Jasa Medik Dokter Laborat Ralan','0','" + totallabrawatjalan + "'", "kredit=kredit+'" + (totallabrawatjalan) + "'", "kd_rek='" + form.Beban_Jasa_Medik_Dokter_Laborat_Ralan + "'");
                    }
                    if (totallabrawatinap > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Beban_Jasa_Medik_Dokter_Laborat_Ranap + "','Beban Jasa Medik Dokter Laborat Ranap','0','" + totallabrawatinap + "'", "kredit=kredit+'" + (totallabrawatinap) + "'", "kd_rek='" + form.Beban_Jasa_Medik_Dokter_Laborat_Ranap + "'");
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Laborat_Ranap + "','Utang Jasa Medik Dokter Laborat Ranap','" + totallabrawatinap + "','0'", "debet=debet+'" + (totallabrawatinap) + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Laborat_Ranap + "'");
                    }
                    if (totalradrawatjalan > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Beban_Jasa_Medik_Dokter_Radiologi_Ralan + "','Beban Jasa Medik Dokter Radiologi Ralan','0','" + totalradrawatjalan + "'", "kredit=kredit+'" + (totalradrawatjalan) + "'", "kd_rek='" + form.Beban_Jasa_Medik_Dokter_Radiologi_Ralan + "'");
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Radiologi_Ralan + "','Utang Jasa Medik Dokter Radiologi Ralan','" + totalradrawatjalan + "','0'", "debet=debet+'" + (totalradrawatjalan) + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Radiologi_Ralan + "'");
                    }
                    if (totalradrawatinap > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Beban_Jasa_Medik_Dokter_Radiologi_Ranap + "','Beban Jasa Medik Dokter Radiologi Ranap','0','" + totalradrawatinap + "'", "kredit=kredit+'" + (totalradrawatinap) + "'", "kd_rek='" + form.Beban_Jasa_Medik_Dokter_Radiologi_Ranap + "'");
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Radiologi_Ranap + "','Utang Jasa Medik Dokter Radiologi Ranap','" + totalradrawatinap + "','0'", "debet=debet+'" + (totalradrawatinap) + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Radiologi_Ranap + "'");
                    }
                    if (totaloperasirawatjalan > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Beban_Jasa_Medik_Dokter_Operasi_Ralan + "','Beban Jasa Medik Dokter Operasi Ralan','0','" + totaloperasirawatjalan + "'", "kredit=kredit+'" + totaloperasirawatjalan + "'", "kd_rek='" + form.Beban_Jasa_Medik_Dokter_Operasi_Ralan + "'");
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Operasi_Ralan + "','Utang Jasa Medik Dokter Operasi Ralan','" + totaloperasirawatjalan + "','0'", "debet=debet+'" + totaloperasirawatjalan + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Operasi_Ralan + "'");
                    }
                    if (totaloperasirawatinap > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Beban_Jasa_Medik_Dokter_Operasi_Ranap + "','Beban Jasa Medik Dokter Operasi Ranap','0','" + totaloperasirawatinap + "'", "kredit=kredit+'" + totaloperasirawatinap + "'", "kd_rek='" + form.Beban_Jasa_Medik_Dokter_Operasi_Ranap + "'");
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Operasi_Ranap + "','Utang Jasa Medik Dokter Operasi Ranap','" + totaloperasirawatinap + "','0'", "debet=debet+'" + totaloperasirawatinap + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Operasi_Ranap + "'");
                    }
                    if (bayar > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Bayar_JM_Dokter + "','Bayar JM Dokter','" + bayar + "','0'", "debet=debet+'" + (bayar) + "'", "kd_rek='" + form.Bayar_JM_Dokter + "'");
                        Sequel.menyimpan("tampjurnal", "'" + form.koderekening + "','" + AkunBayar.getSelectedItem().toString() + "','0','" + bayar + "'", "kredit=kredit+'" + (bayar) + "'", "kd_rek='" + form.koderekening + "'");
                    }
                     */
                    // JURNAL BARU: PELUNASAN UTANG JASA MEDIS DOKTER (DEBET: UTANG JM, KREDIT: KAS/BANK)
                    // Debet: Utang Jasa Medik Dokter sesuai kelompok layanan yang dibayarkan
                    if (totalrawatjalan > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Tindakan_Ralan + "','Utang Jasa Medik Dokter Tindakan Ralan','" + totalrawatjalan + "','0'", "debet=debet+'" + (totalrawatjalan) + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Tindakan_Ralan + "'");
                    }
                    if (totalrawatinap > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Tindakan_Ranap + "','Utang Jasa Medik Dokter Tindakan Ranap','" + totalrawatinap + "','0'", "debet=debet+'" + (totalrawatinap) + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Tindakan_Ranap + "'");
                    }
                    if (totallabrawatjalan > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Laborat_Ralan + "','Utang Jasa Medik Dokter Laborat Ralan','" + totallabrawatjalan + "','0'", "debet=debet+'" + (totallabrawatjalan) + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Laborat_Ralan + "'");
                    }
                    if (totallabrawatinap > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Laborat_Ranap + "','Utang Jasa Medik Dokter Laborat Ranap','" + totallabrawatinap + "','0'", "debet=debet+'" + (totallabrawatinap) + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Laborat_Ranap + "'");
                    }
                    if (totalradrawatjalan > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Radiologi_Ralan + "','Utang Jasa Medik Dokter Radiologi Ralan','" + totalradrawatjalan + "','0'", "debet=debet+'" + (totalradrawatjalan) + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Radiologi_Ralan + "'");
                    }
                    if (totalradrawatinap > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Radiologi_Ranap + "','Utang Jasa Medik Dokter Radiologi Ranap','" + totalradrawatinap + "','0'", "debet=debet+'" + (totalradrawatinap) + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Radiologi_Ranap + "'");
                    }
                    if (totaloperasirawatjalan > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Operasi_Ralan + "','Utang Jasa Medik Dokter Operasi Ralan','" + totaloperasirawatjalan + "','0'", "debet=debet+'" + totaloperasirawatjalan + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Operasi_Ralan + "'");
                    }
                    if (totaloperasirawatinap > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.Utang_Jasa_Medik_Dokter_Operasi_Ranap + "','Utang Jasa Medik Dokter Operasi Ranap','" + totaloperasirawatinap + "','0'", "debet=debet+'" + totaloperasirawatinap + "'", "kd_rek='" + form.Utang_Jasa_Medik_Dokter_Operasi_Ranap + "'");
                    }
                    // Kredit: Rekening Kas/Bank pengeluaran uang bayar JM
                    if (bayar > 0) {
                        Sequel.menyimpan("tampjurnal", "'" + form.koderekening + "','" + AkunBayar.getSelectedItem().toString() + "','0','" + bayar + "'", "kredit=kredit+'" + (bayar) + "'", "kd_rek='" + form.koderekening + "'");
                    }

                    sukses = form.jur.simpanJurnal(NoTagihan.getText(), "U", "PEMBAYARAN JASA MEDIS DOKTER " + kddokter.getText() + " " + nmdokter.getText() + ", OLEH " + akses.getkode());
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
        for (row = 0; row < tbBangsal.getRowCount(); row++) {
            tbBangsal.setValueAt(false, row, 0);
        }
        getData();
    }//GEN-LAST:event_ppBersihkanActionPerformed

    private void ppSemuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppSemuaActionPerformed
        for (row = 0; row < tbBangsal.getRowCount(); row++) {
            tbBangsal.setValueAt(true, row, 0);
        }
        getData();
    }//GEN-LAST:event_ppSemuaActionPerformed

    private void ppSlipGajiHTMLActionPerformed(java.awt.event.ActionEvent evt) {
        if (kddokter.getText().trim().equals("") || nmdokter.getText().trim().equals("")) {
            Valid.textKosong(kddokter, "Dokter");
        } else {
            if (bayar == 0) {
                JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih jasa medis yang mau dibayar...!!!!");
            } else if (tabMode.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            } else if (tabMode.getRowCount() != 0) {
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

                    javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
                    fileChooser.setDialogTitle("Simpan PDF Slip Gaji");
                    String defaultName = "SlipGajiJM_" + nmdokter.getText().trim().replaceAll("\\s+", "_") + ".pdf";
                    fileChooser.setSelectedFile(new java.io.File(defaultName));
                    int userSelection = fileChooser.showSaveDialog(null);

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
                    html.append("  .doc-info { width: 100%; margin-bottom: 20px; font-size: 14px; }\n");
                    html.append("  .doc-info td { padding: 4px; }\n");
                    html.append("  .data-table { width: 100%; border-collapse: collapse; font-size: 13px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }\n");
                    html.append("  .data-table th { background-color: #f4f6f8; color: #333; font-weight: 600; padding: 10px 8px; border: 1px solid #ddd; text-align: center; }\n");
                    html.append("  .data-table td { padding: 8px; border: 1px solid #ddd; }\n");
                    html.append("  .data-table tr:nth-child(even) { background-color: #fafafa; }\n");
                    html.append("  .data-table tr:hover { background-color: #f1f1f1; }\n");
                    html.append("  .text-right { text-align: right; }\n");
                    html.append("  .text-center { text-align: center; }\n");
                    html.append("  .total-row { background-color: #e9ecef !important; font-weight: bold; }\n");
                    html.append("  .title-slip { font-size: 18px; font-weight: bold; margin-top: 15px; text-transform: uppercase; letter-spacing: 1px; color: #000; }\n");
                    html.append("</style>\n");
                    html.append("</head>\n");
                    html.append("<body>\n");

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

                    html.append("<div class='text-center title-slip'>JASA MEDIS DOKTER</div><br>\n");

                    html.append("<table class='doc-info'>\n");
                    html.append("<tr><td width='120'><b>Nama Dokter</b></td><td width='10'>:</td><td><b>").append(nmdokter.getText()).append("</b></td></tr>\n");
                    html.append("<tr><td><b>Bulan/Periode</b></td><td>:</td><td><b>").append(Tanggal.getSelectedItem().toString().substring(3, 10)).append("</b></td></tr>\n");
                    html.append("</table>\n");

                    html.append("<table class='data-table'>\n");
                    html.append("<thead>\n");
                    html.append("<tr>\n");
                    html.append("<th>No</th>\n");
                    html.append("<th style='width: 12%;'>Tgl. Tindakan</th>\n");
                    html.append("<th>Jam</th>\n");
                    html.append("<th>No.RM</th>\n");
                    html.append("<th>Nama Pasien</th>\n");
                    html.append("<th>Tindakan Medis</th>\n");
                    html.append("<th>Bruto</th>\n");
                    html.append("<th>Sharing</th>\n");
                    html.append("<th>Jasa Medis</th>\n");
                    html.append("<th>Payor</th>\n");
                    html.append("</tr>\n");
                    html.append("</thead>\n");
                    html.append("<tbody>\n");

                    double totalBruto = 0;
                    double totalSharing = 0;
                    double totalJasaMedis = 0;
                    int r = 1;

                    for (int i = 0; i < tabMode.getRowCount(); i++) {
                        if (tabMode.getValueAt(i, 0).toString().equals("true")) {
                            double bruto = Double.parseDouble(tabMode.getValueAt(i, 9).toString().replaceAll("[^\\d.]", ""));
                            double sharing = Double.parseDouble(tabMode.getValueAt(i, 10).toString().replaceAll("[^\\d.]", ""));
                            double jasaMedis = Double.parseDouble(tabMode.getValueAt(i, 11).toString().replaceAll("[^\\d.]", ""));

                            totalBruto += bruto;
                            totalSharing += sharing;
                            totalJasaMedis += jasaMedis;

                            html.append("<tr>\n");
                            html.append("<td class='text-center'>").append(r).append("</td>\n");
                            html.append("<td class='text-center'>").append(tabMode.getValueAt(i, 1).toString()).append("</td>\n");
                            html.append("<td class='text-center'>").append(tabMode.getValueAt(i, 2).toString()).append("</td>\n");
                            html.append("<td class='text-center'>").append(tabMode.getValueAt(i, 4).toString()).append("</td>\n");
                            html.append("<td>").append(tabMode.getValueAt(i, 5).toString()).append("</td>\n");
                            html.append("<td>").append(tabMode.getValueAt(i, 7).toString()).append("</td>\n"); // Mengambil nama tindakan medis (kolom 7) bukan kodenya (kolom 6)
                            html.append("<td class='text-right'>").append(Valid.SetAngka(bruto)).append("</td>\n");
                            html.append("<td class='text-right'>").append(Valid.SetAngka(sharing)).append("</td>\n");
                            html.append("<td class='text-right'>").append(Valid.SetAngka(jasaMedis)).append("</td>\n");
                            html.append("<td>").append(tabMode.getValueAt(i, 13).toString()).append("</td>\n");
                            html.append("</tr>\n");
                            r++;
                        }
                    }

                    html.append("<tr class='total-row'>\n");
                    html.append("<td colspan='6' class='text-right'>TOTAL KESELURUHAN :</td>\n");
                    html.append("<td class='text-right'>").append(Valid.SetAngka(totalBruto)).append("</td>\n");
                    html.append("<td class='text-right'>").append(Valid.SetAngka(totalSharing)).append("</td>\n");
                    html.append("<td class='text-right'>").append(Valid.SetAngka(totalJasaMedis)).append("</td>\n");
                    html.append("<td></td>\n");
                    html.append("</tr>\n");

                    html.append("</tbody>\n");
                    html.append("</table>\n");

                    // Menambahkan signature / Tanda Tangan Manajemen
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
                    System.out.println("Notifikasi Cetak HTML : " + e);
                }
                this.setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private void ppPiutangHTMLActionPerformed(java.awt.event.ActionEvent evt) {
        if (kddokter.getText().trim().equals("") || nmdokter.getText().trim().equals("")) {
            Valid.textKosong(kddokter, "Dokter");
        } else {
            if (bayar == 0) {
                JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih jasa medis yang mau dibayar...!!!!");
            } else if (tabMode.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            } else if (tabMode.getRowCount() != 0) {
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

                    javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
                    fileChooser.setDialogTitle("Simpan PDF Piutang Jasa Medis");
                    String defaultName = "PiutangJM_" + nmdokter.getText().trim().replaceAll("\\s+", "_") + ".pdf";
                    fileChooser.setSelectedFile(new java.io.File(defaultName));
                    int userSelection = fileChooser.showSaveDialog(null);

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
                    html.append("  .doc-info { width: 100%; margin-bottom: 20px; font-size: 14px; }\n");
                    html.append("  .doc-info td { padding: 4px; }\n");
                    html.append("  .data-table { width: 100%; border-collapse: collapse; font-size: 13px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }\n");
                    html.append("  .data-table th { background-color: #f4f6f8; color: #333; font-weight: 600; padding: 10px 8px; border: 1px solid #ddd; text-align: center; }\n");
                    html.append("  .data-table td { padding: 8px; border: 1px solid #ddd; }\n");
                    html.append("  .data-table tr:nth-child(even) { background-color: #fafafa; }\n");
                    html.append("  .data-table tr:hover { background-color: #f1f1f1; }\n");
                    html.append("  .text-right { text-align: right; }\n");
                    html.append("  .text-center { text-align: center; }\n");
                    html.append("  .total-row { background-color: #e9ecef !important; font-weight: bold; }\n");
                    html.append("  .title-slip { font-size: 18px; font-weight: bold; margin-top: 15px; text-transform: uppercase; letter-spacing: 1px; color: #000; }\n");
                    html.append("</style>\n");
                    html.append("</head>\n");
                    html.append("<body>\n");

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

                    html.append("<div class='text-center title-slip'>PIUTANG JASA MEDIS DOKTER</div><br>\n");

                    html.append("<table class='doc-info'>\n");
                    html.append("<tr><td width='120'><b>Nama Dokter</b></td><td width='10'>:</td><td><b>").append(nmdokter.getText()).append("</b></td></tr>\n");
                    html.append("<tr><td><b>Bulan/Periode</b></td><td>:</td><td><b>").append(Tanggal.getSelectedItem().toString().substring(3, 10)).append("</b></td></tr>\n");
                    html.append("</table>\n");

                    html.append("<table class='data-table'>\n");
                    html.append("<thead>\n");
                    html.append("<tr>\n");
                    html.append("<th>No</th>\n");
                    html.append("<th style='width: 12%;'>Tgl. Tindakan</th>\n");
                    html.append("<th>Jam</th>\n");
                    html.append("<th>No.RM</th>\n");
                    html.append("<th>Nama Pasien</th>\n");
                    html.append("<th>Tindakan Medis</th>\n");
                    html.append("<th>Bruto</th>\n");
                    html.append("<th>Sharing</th>\n");
                    html.append("<th>Jasa Medis</th>\n");
                    html.append("<th>Payor</th>\n");
                    html.append("</tr>\n");
                    html.append("</thead>\n");
                    html.append("<tbody>\n");

                    double totalBruto = 0;
                    double totalSharing = 0;
                    double totalJasaMedis = 0;
                    int r = 1;

                    for (int i = 0; i < tabMode.getRowCount(); i++) {
                        if (tabMode.getValueAt(i, 0).toString().equals("true")) {
                            double bruto = Double.parseDouble(tabMode.getValueAt(i, 9).toString().replaceAll("[^\\d.]", ""));
                            double sharing = Double.parseDouble(tabMode.getValueAt(i, 10).toString().replaceAll("[^\\d.]", ""));
                            double jasaMedis = Double.parseDouble(tabMode.getValueAt(i, 11).toString().replaceAll("[^\\d.]", ""));

                            totalBruto += bruto;
                            totalSharing += sharing;
                            totalJasaMedis += jasaMedis;

                            html.append("<tr>\n");
                            html.append("<td class='text-center'>").append(r).append("</td>\n");
                            html.append("<td class='text-center'>").append(tabMode.getValueAt(i, 1).toString()).append("</td>\n");
                            html.append("<td class='text-center'>").append(tabMode.getValueAt(i, 2).toString()).append("</td>\n");
                            html.append("<td class='text-center'>").append(tabMode.getValueAt(i, 4).toString()).append("</td>\n");
                            html.append("<td>").append(tabMode.getValueAt(i, 5).toString()).append("</td>\n");
                            html.append("<td>").append(tabMode.getValueAt(i, 7).toString()).append("</td>\n"); // Mengambil nama tindakan medis (kolom 7) bukan kodenya (kolom 6)
                            html.append("<td class='text-right'>").append(Valid.SetAngka(bruto)).append("</td>\n");
                            html.append("<td class='text-right'>").append(Valid.SetAngka(sharing)).append("</td>\n");
                            html.append("<td class='text-right'>").append(Valid.SetAngka(jasaMedis)).append("</td>\n");
                            html.append("<td>").append(tabMode.getValueAt(i, 13).toString()).append("</td>\n");
                            html.append("</tr>\n");
                            r++;
                        }
                    }

                    html.append("<tr class='total-row'>\n");
                    html.append("<td colspan='6' class='text-right'>TOTAL KESELURUHAN :</td>\n");
                    html.append("<td class='text-right'>").append(Valid.SetAngka(totalBruto)).append("</td>\n");
                    html.append("<td class='text-right'>").append(Valid.SetAngka(totalSharing)).append("</td>\n");
                    html.append("<td class='text-right'>").append(Valid.SetAngka(totalJasaMedis)).append("</td>\n");
                    html.append("<td></td>\n");
                    html.append("</tr>\n");

                    html.append("</tbody>\n");
                    html.append("</table>\n");

                    // Menambahkan signature / Tanda Tangan Manajemen
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
                    System.out.println("Notifikasi Cetak HTML : " + e);
                }
                this.setCursor(Cursor.getDefaultCursor());
            }
        }
    }

    private void BtnCari1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCari1ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        form.emptTeks();
        form.isCek();
        form.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        form.setLocationRelativeTo(internalFrame1);
        form.setAlwaysOnTop(false);
        form.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnCari1ActionPerformed

    private void BtnCari1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCari1KeyPressed
        Valid.pindah(evt, BtnSimpan, BtnExport);
    }//GEN-LAST:event_BtnCari1KeyPressed

    private void BtnExportActionPerformed(java.awt.event.ActionEvent evt) {
        if (tabMode == null || tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Data rincian jasa medis dokter masih kosong...!");
            return;
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            javax.swing.JFileChooser fileChooser = new javax.swing.JFileChooser();
            fileChooser.setDialogTitle("Simpan Export Excel Rincian Jasa Medis Dokter");

            // Atur direktori awal ke Documents atau Desktop pengguna
            java.io.File defaultDir = new java.io.File(System.getProperty("user.home"), "Documents");
            if (!defaultDir.exists()) {
                defaultDir = new java.io.File(System.getProperty("user.home"), "Desktop");
            }
            if (defaultDir.exists()) {
                fileChooser.setCurrentDirectory(defaultDir);
            }

            String namaDokterFile = ChkSemuaDokter.isSelected() ? "Semua_Dokter" : nmdokter.getText().trim().replaceAll("[^a-zA-Z0-9.-]", "_");
            String statusFile = cmbStatus.getSelectedItem() != null ? cmbStatus.getSelectedItem().toString().trim().replaceAll("[^a-zA-Z0-9.-]", "_") : "Semua";
            String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String defaultName = "Rincian_JM_" + namaDokterFile + "_" + statusFile + "_" + Valid.SetTgl(TglBilling1.getSelectedItem() + "") + "_sd_" + Valid.SetTgl(TglBilling2.getSelectedItem() + "") + "_" + timestamp + ".xlsx";

            fileChooser.setSelectedFile(new java.io.File(defaultDir.exists() ? defaultDir : new java.io.File("."), defaultName));
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

            this.setCursor(Cursor.getDefaultCursor());
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection != javax.swing.JFileChooser.APPROVE_OPTION) {
                return;
            }

            java.io.File f = fileChooser.getSelectedFile();
            if (f == null) {
                return;
            }
            if (!f.getAbsolutePath().toLowerCase().endsWith(".xlsx")) {
                f = new java.io.File(f.getAbsolutePath() + ".xlsx");
            }

            // File Lock Guard: Cek apakah berkas sedang dibuka di Microsoft Excel
            if (f.exists()) {
                try (java.io.FileOutputStream testFos = new java.io.FileOutputStream(f, true)) {
                    // Berkas dapat diakses dan ditulisi
                } catch (Exception exLock) {
                    JOptionPane.showMessageDialog(null, "Berkas '" + f.getName() + "' sedang dibuka oleh aplikasi lain (Microsoft Excel)!\nSilakan tutup berkas tersebut di Excel terlebih dahulu atau simpan dengan nama yang berbeda.", "Berkas Terkunci", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            // Export native OpenXML .xlsx via pure ZipOutputStream (zero external dependency, bebas konflik JAR POI)
            try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(new java.io.FileOutputStream(f))) {
                java.nio.charset.Charset utf8 = java.nio.charset.StandardCharsets.UTF_8;

                // 1. [Content_Types].xml
                zos.putNextEntry(new java.util.zip.ZipEntry("[Content_Types].xml"));
                byte[] ctBytes = ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                        + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n"
                        + "  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n"
                        + "  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n"
                        + "  <Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>\n"
                        + "  <Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>\n"
                        + "  <Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>\n"
                        + "</Types>").getBytes(utf8);
                zos.write(ctBytes);
                zos.closeEntry();

                // 2. _rels/.rels
                zos.putNextEntry(new java.util.zip.ZipEntry("_rels/.rels"));
                byte[] relsBytes = ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                        + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n"
                        + "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>\n"
                        + "</Relationships>").getBytes(utf8);
                zos.write(relsBytes);
                zos.closeEntry();

                // 3. xl/_rels/workbook.xml.rels
                zos.putNextEntry(new java.util.zip.ZipEntry("xl/_rels/workbook.xml.rels"));
                byte[] wbRelsBytes = ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                        + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n"
                        + "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>\n"
                        + "  <Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>\n"
                        + "</Relationships>").getBytes(utf8);
                zos.write(wbRelsBytes);
                zos.closeEntry();

                // 4. xl/workbook.xml
                zos.putNextEntry(new java.util.zip.ZipEntry("xl/workbook.xml"));
                byte[] wbBytes = ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                        + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">\n"
                        + "  <sheets>\n"
                        + "    <sheet name=\"Jasa Medis Dokter\" sheetId=\"1\" r:id=\"rId1\"/>\n"
                        + "  </sheets>\n"
                        + "</workbook>").getBytes(utf8);
                zos.write(wbBytes);
                zos.closeEntry();

                // 5. xl/styles.xml
                zos.putNextEntry(new java.util.zip.ZipEntry("xl/styles.xml"));
                byte[] stylesBytes = ("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                        + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n"
                        + "  <fonts count=\"5\">\n"
                        + "    <font><name val=\"Tahoma\"/><sz val=\"10\"/></font>\n"
                        + "    <font><b/><name val=\"Tahoma\"/><sz val=\"10\"/></font>\n"
                        + "    <font><b/><name val=\"Tahoma\"/><sz val=\"12\"/></font>\n"
                        + "    <font><b/><name val=\"Tahoma\"/><sz val=\"14\"/></font>\n"
                        + "    <font><b/><color rgb=\"FFFFFFFF\"/><name val=\"Tahoma\"/><sz val=\"10\"/></font>\n"
                        + "  </fonts>\n"
                        + "  <fills count=\"4\">\n"
                        + "    <fill><patternFill patternType=\"none\"/></fill>\n"
                        + "    <fill><patternFill patternType=\"gray125\"/></fill>\n"
                        + "    <fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF2E8B57\"/></patternFill></fill>\n"
                        + "    <fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFE0E0E0\"/></patternFill></fill>\n"
                        + "  </fills>\n"
                        + "  <borders count=\"3\">\n"
                        + "    <border><left/><right/><top/><bottom/><diagonal/></border>\n"
                        + "    <border>\n"
                        + "      <left style=\"thin\"><color rgb=\"FF808080\"/></left>\n"
                        + "      <right style=\"thin\"><color rgb=\"FF808080\"/></right>\n"
                        + "      <top style=\"thin\"><color rgb=\"FF808080\"/></top>\n"
                        + "      <bottom style=\"thin\"><color rgb=\"FF808080\"/></bottom>\n"
                        + "      <diagonal/>\n"
                        + "    </border>\n"
                        + "    <border>\n"
                        + "      <left style=\"thin\"><color rgb=\"FF808080\"/></left>\n"
                        + "      <right style=\"thin\"><color rgb=\"FF808080\"/></right>\n"
                        + "      <top style=\"thin\"><color rgb=\"FF808080\"/></top>\n"
                        + "      <bottom style=\"double\"><color rgb=\"FF808080\"/></bottom>\n"
                        + "      <diagonal/>\n"
                        + "    </border>\n"
                        + "  </borders>\n"
                        + "  <cellStyleXfs count=\"1\">\n"
                        + "    <xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/>\n"
                        + "  </cellStyleXfs>\n"
                        + "  <cellXfs count=\"13\">\n"
                        + "    <xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>\n"
                        + "    <xf numFmtId=\"0\" fontId=\"3\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>\n"
                        + "    <xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>\n"
                        + "    <xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>\n"
                        + "    <xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"/>\n"
                        + "    <xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>\n"
                        + "    <xf numFmtId=\"0\" fontId=\"4\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>\n"
                        + "    <xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment vertical=\"center\"/></xf>\n"
                        + "    <xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>\n"
                        + "    <xf numFmtId=\"3\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyNumberFormat=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>\n"
                        + "    <xf numFmtId=\"0\" fontId=\"1\" fillId=\"3\" borderId=\"2\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>\n"
                        + "    <xf numFmtId=\"3\" fontId=\"1\" fillId=\"3\" borderId=\"2\" xfId=\"0\" applyNumberFormat=\"1\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>\n"
                        + "    <xf numFmtId=\"0\" fontId=\"1\" fillId=\"3\" borderId=\"2\" xfId=\"0\" applyFill=\"1\" applyBorder=\"1\"/>\n"
                        + "  </cellXfs>\n"
                        + "</styleSheet>").getBytes(utf8);
                zos.write(stylesBytes);
                zos.closeEntry();

                // 6. xl/worksheets/sheet1.xml
                zos.putNextEntry(new java.util.zip.ZipEntry("xl/worksheets/sheet1.xml"));
                java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(zos, utf8);
                java.io.BufferedWriter bw = new java.io.BufferedWriter(osw);
                bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n");
                bw.write("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n");
                bw.write("  <cols>\n");
                bw.write("    <col min=\"1\" max=\"1\" width=\"6\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"2\" max=\"2\" width=\"13\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"3\" max=\"3\" width=\"10\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"4\" max=\"4\" width=\"20\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"5\" max=\"5\" width=\"12\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"6\" max=\"6\" width=\"25\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"7\" max=\"7\" width=\"15\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"8\" max=\"8\" width=\"35\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"9\" max=\"9\" width=\"20\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"10\" max=\"10\" width=\"16\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"11\" max=\"11\" width=\"16\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"12\" max=\"12\" width=\"16\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"13\" max=\"13\" width=\"22\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"14\" max=\"14\" width=\"13\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"15\" max=\"15\" width=\"14\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"16\" max=\"16\" width=\"25\" customWidth=\"1\"/>\n");
                bw.write("    <col min=\"17\" max=\"17\" width=\"16\" customWidth=\"1\"/>\n");
                bw.write("  </cols>\n");
                bw.write("  <sheetData>\n");

                int rXml = 1;
                // Header RS
                bw.write("    <row r=\"" + (rXml++) + "\"><c r=\"A1\" s=\"1\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(akses.getnamars().toUpperCase()) + "</t></is></c></row>\n");
                bw.write("    <row r=\"" + (rXml++) + "\"><c r=\"A2\" s=\"2\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(akses.getalamatrs() + ", " + akses.getkabupatenrs() + ", " + akses.getpropinsirs() + " | Kontak: " + akses.getkontakrs()) + "</t></is></c></row>\n");
                bw.write("    <row r=\"" + (rXml++) + "\"><c r=\"A3\" s=\"3\" t=\"inlineStr\"><is><t>LAPORAN RINCIAN JASA MEDIS DOKTER</t></is></c></row>\n");
                bw.write("    <row r=\"" + (rXml++) + "\"/>\n");

                // Filters
                String[][] filters = {
                    {"Dokter", ": " + (ChkSemuaDokter.isSelected() ? "SEMUA DOKTER" : (kddokter.getText() + " - " + nmdokter.getText()))},
                    {"Periode Billing", ": " + TglBilling1.getSelectedItem() + " s.d. " + TglBilling2.getSelectedItem()},
                    {"Status / Kategori", ": " + cmbStatus.getSelectedItem()},
                    {"Cara Bayar", ": " + (NmCaraBayar.getText().trim().isEmpty() ? "Semua Cara Bayar" : NmCaraBayar.getText())}
                };

                for (String[] flt : filters) {
                    bw.write("    <row r=\"" + rXml + "\">");
                    bw.write("<c r=\"A" + rXml + "\" s=\"4\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(flt[0]) + "</t></is></c>");
                    bw.write("<c r=\"B" + rXml + "\" s=\"5\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(flt[1]) + "</t></is></c>");
                    bw.write("</row>\n");
                    rXml++;
                }

                bw.write("    <row r=\"" + (rXml++) + "\"/>\n");

                // Table Headers
                String[] headers = {
                    "No", "Tanggal Trx", "Jam Trx", "No. Rawat", "No. RM", "Nama Pasien",
                    "ID Tarif", "Tindakan Medis", "Status", "Bruto", "Sharing", "Jasa Medis",
                    "Payor", "Tgl Billing", "Kode Dokter", "Nama Dokter", "No. Nota"
                };
                String[] colsLetter = {"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q"};

                bw.write("    <row r=\"" + rXml + "\" ht=\"24\" customHeight=\"1\">");
                for (int h = 0; h < headers.length; h++) {
                    bw.write("<c r=\"" + colsLetter[h] + rXml + "\" s=\"6\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(headers[h]) + "</t></is></c>");
                }
                bw.write("</row>\n");
                rXml++;

                double totBruto = 0;
                double totSharing = 0;
                double totJM = 0;
                int noUrut = 1;

                for (int r = 0; r < tabMode.getRowCount(); r++) {
                    double valBruto = 0;
                    double valSharing = 0;
                    double valJM = 0;
                    try {
                        valBruto = Double.parseDouble(tabMode.getValueAt(r, 9).toString());
                    } catch (Exception ex) {}
                    try {
                        valSharing = Double.parseDouble(tabMode.getValueAt(r, 10).toString());
                    } catch (Exception ex) {}
                    try {
                        valJM = Double.parseDouble(tabMode.getValueAt(r, 11).toString());
                    } catch (Exception ex) {}

                    totBruto += valBruto;
                    totSharing += valSharing;
                    totJM += valJM;

                    bw.write("    <row r=\"" + rXml + "\">");
                    // 0: No
                    bw.write("<c r=\"A" + rXml + "\" s=\"8\"><v>" + (noUrut++) + "</v></c>");
                    // 1: Tanggal Trx
                    bw.write("<c r=\"B" + rXml + "\" s=\"8\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(tabMode.getValueAt(r, 1) != null ? tabMode.getValueAt(r, 1).toString() : "") + "</t></is></c>");
                    // 2: Jam Trx
                    bw.write("<c r=\"C" + rXml + "\" s=\"8\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(tabMode.getValueAt(r, 2) != null ? tabMode.getValueAt(r, 2).toString() : "") + "</t></is></c>");
                    // 3: No. Rawat
                    bw.write("<c r=\"D" + rXml + "\" s=\"8\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(tabMode.getValueAt(r, 3) != null ? tabMode.getValueAt(r, 3).toString() : "") + "</t></is></c>");
                    // 4: No. RM
                    bw.write("<c r=\"E" + rXml + "\" s=\"8\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(tabMode.getValueAt(r, 4) != null ? tabMode.getValueAt(r, 4).toString() : "") + "</t></is></c>");
                    // 5: Nama Pasien
                    bw.write("<c r=\"F" + rXml + "\" s=\"7\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(tabMode.getValueAt(r, 5) != null ? tabMode.getValueAt(r, 5).toString() : "") + "</t></is></c>");
                    // 6: ID Tarif
                    bw.write("<c r=\"G" + rXml + "\" s=\"8\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(tabMode.getValueAt(r, 6) != null ? tabMode.getValueAt(r, 6).toString() : "") + "</t></is></c>");
                    // 7: Tindakan Medis
                    bw.write("<c r=\"H" + rXml + "\" s=\"7\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(tabMode.getValueAt(r, 7) != null ? tabMode.getValueAt(r, 7).toString() : "") + "</t></is></c>");
                    // 8: Status
                    bw.write("<c r=\"I" + rXml + "\" s=\"7\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(tabMode.getValueAt(r, 8) != null ? tabMode.getValueAt(r, 8).toString() : "") + "</t></is></c>");
                    // 9: Bruto (Numeric)
                    bw.write("<c r=\"J" + rXml + "\" s=\"9\"><v>" + valBruto + "</v></c>");
                    // 10: Sharing (Numeric)
                    bw.write("<c r=\"K" + rXml + "\" s=\"9\"><v>" + valSharing + "</v></c>");
                    // 11: Jasa Medis (Numeric)
                    bw.write("<c r=\"L" + rXml + "\" s=\"9\"><v>" + valJM + "</v></c>");
                    // 12: Payor
                    bw.write("<c r=\"M" + rXml + "\" s=\"7\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(tabMode.getValueAt(r, 13) != null ? tabMode.getValueAt(r, 13).toString() : "") + "</t></is></c>");
                    // 13: Tgl Billing
                    bw.write("<c r=\"N" + rXml + "\" s=\"8\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(tabMode.getValueAt(r, 14) != null ? tabMode.getValueAt(r, 14).toString() : "") + "</t></is></c>");
                    // 14: Kode Dokter
                    bw.write("<c r=\"O" + rXml + "\" s=\"8\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(tabMode.getValueAt(r, 15) != null ? tabMode.getValueAt(r, 15).toString() : "") + "</t></is></c>");
                    // 15: Nama Dokter
                    bw.write("<c r=\"P" + rXml + "\" s=\"7\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(tabMode.getValueAt(r, 16) != null ? tabMode.getValueAt(r, 16).toString() : "") + "</t></is></c>");
                    // 16: No. Nota
                    bw.write("<c r=\"Q" + rXml + "\" s=\"8\" t=\"inlineStr\"><is><t>" + escapeXmlXlsx(tabMode.getValueAt(r, 17) != null ? tabMode.getValueAt(r, 17).toString() : "") + "</t></is></c>");

                    bw.write("</row>\n");
                    rXml++;
                }

                // Total Row
                bw.write("    <row r=\"" + rXml + "\" ht=\"20\" customHeight=\"1\">");
                for (int col = 0; col < 8; col++) {
                    bw.write("<c r=\"" + colsLetter[col] + rXml + "\" s=\"10\"/>");
                }
                bw.write("<c r=\"I" + rXml + "\" s=\"10\" t=\"inlineStr\"><is><t>TOTAL :</t></is></c>");
                bw.write("<c r=\"J" + rXml + "\" s=\"11\"><v>" + totBruto + "</v></c>");
                bw.write("<c r=\"K" + rXml + "\" s=\"11\"><v>" + totSharing + "</v></c>");
                bw.write("<c r=\"L" + rXml + "\" s=\"11\"><v>" + totJM + "</v></c>");
                for (int col = 12; col <= 16; col++) {
                    bw.write("<c r=\"" + colsLetter[col] + rXml + "\" s=\"12\"/>");
                }
                bw.write("</row>\n");

                bw.write("  </sheetData>\n");
                bw.write("</worksheet>");
                bw.flush();
                osw.flush();
                zos.closeEntry();
            }

            this.setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(null, "Export data berhasil disimpan di:\n" + f.getAbsolutePath(), "Export Sukses", JOptionPane.INFORMATION_MESSAGE);
            try {
                if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
                    java.awt.Desktop.getDesktop().open(f);
                }
            } catch (Exception ex) {
                System.err.println("Gagal auto-open berkas excel: " + ex.getMessage());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat export data: " + e.getMessage(), "Error Export", JOptionPane.ERROR_MESSAGE);
        } finally {
            this.setCursor(Cursor.getDefaultCursor());
        }
    }

    private String escapeXmlXlsx(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // Hanya izinkan karakter XML 1.0 yang valid (#x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD])
            if (c == 0x9 || c == 0xA || c == 0xD || (c >= 0x20 && c <= 0xD7FF) || (c >= 0xE000 && c <= 0xFFFD)) {
                if (c == '&') {
                    sb.append("&amp;");
                } else if (c == '<') {
                    sb.append("&lt;");
                } else if (c == '>') {
                    sb.append("&gt;");
                } else if (c == '\"') {
                    sb.append("&quot;");
                } else if (c == '\'') {
                    sb.append("&apos;");
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }


    private void BtnExportKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnExportKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnExportActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnCari1, BtnKeluar);
        }
    }//GEN-LAST:event_BtnExportKeyPressed

    private void ChkSemuaDokterItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ChkSemuaDokterItemStateChanged
        if (ChkSemuaDokter.isSelected()) {
            kddokter.setText("");
            nmdokter.setText("SEMUA DOKTER");
            BtnPetugas.setEnabled(false);
        } else {
            if (nmdokter.getText().equals("SEMUA DOKTER")) {
                kddokter.setText("");
                nmdokter.setText("");
            }
            BtnPetugas.setEnabled(true);
        }
    }//GEN-LAST:event_ChkSemuaDokterItemStateChanged

    private void chkRalanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkRalanActionPerformed
        prosesCari();
    }//GEN-LAST:event_chkRalanActionPerformed

    private void chkRanapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkRanapActionPerformed
        prosesCari();
    }//GEN-LAST:event_chkRanapActionPerformed

    private void chkOperasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkOperasiActionPerformed
        prosesCari();
    }//GEN-LAST:event_chkOperasiActionPerformed

    private void chkLaboratActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkLaboratActionPerformed
        prosesCari();
    }//GEN-LAST:event_chkLaboratActionPerformed

    private void chkRadiologiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkRadiologiActionPerformed
        prosesCari();
    }//GEN-LAST:event_chkRadiologiActionPerformed

    private void BtnCaraBayarRalanDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCaraBayarRalanDokterActionPerformed
        carabayar.isCek();
        carabayar.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        carabayar.setLocationRelativeTo(internalFrame1);
        carabayar.setAlwaysOnTop(false);
        carabayar.setVisible(true);
    }//GEN-LAST:event_BtnCaraBayarRalanDokterActionPerformed

    private void BtnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPetugasActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        akses.setform("KeuanganBayarJMDokter");
        form.dokter.emptTeks();
        form.dokter.isCek();
        form.dokter.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        form.dokter.setLocationRelativeTo(internalFrame1);
        form.dokter.setAlwaysOnTop(false);
        form.dokter.setVisible(true);
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

    private void tbBangsalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbBangsalKeyPressed
        if (tabMode.getRowCount() != 0) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                getData();
            }
        }
    }//GEN-LAST:event_tbBangsalKeyPressed

    private void tbBangsalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbBangsalMouseClicked
        if (tabMode.getRowCount() != 0) {
            if (tbBangsal.getSelectedColumn() == 0) {
                getData();
            }
        }
    }//GEN-LAST:event_tbBangsalMouseClicked

    private void tbBangsalPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tbBangsalPropertyChange
        if (this.isVisible() == true) {
            getData();
        }
    }//GEN-LAST:event_tbBangsalPropertyChange

    private void BtnPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPetugasKeyPressed
        Valid.pindah(evt, AkunBayar, Keterangan);
    }//GEN-LAST:event_BtnPetugasKeyPressed

    private void BtnGajiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGajiActionPerformed
        if (kddokter.getText().trim().equals("") || nmdokter.getText().trim().equals("")) {
            Valid.textKosong(kddokter, "Dokter");
        } else {
            if (bayar == 0) {
                JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih jasa medis yang mau dibayar...!!!!");
            } else if (tabMode.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            } else if (tabMode.getRowCount() != 0) {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Sequel.queryu("delete from temporary where temp37='" + akses.getalamatip() + "'");
                double totalBruto = 0;
                double totalSharing = 0;
                double totalJasaMedis = 0;
                row = 1;
                for (i = 0; i < tabMode.getRowCount(); i++) {
                    if (tabMode.getValueAt(i, 0).toString().equals("true")) {
                        // Mengambil nilai nominal dan membersihkan format non-angka
                        double bruto = Double.parseDouble(tabMode.getValueAt(i, 9).toString().replaceAll("[^\\d.]", ""));
                        double sharing = Double.parseDouble(tabMode.getValueAt(i, 10).toString().replaceAll("[^\\d.]", ""));
                        double jasaMedis = Double.parseDouble(tabMode.getValueAt(i, 11).toString().replaceAll("[^\\d.]", ""));

                        Sequel.menyimpan("temporary", "'" + row + "','"
                                + tabMode.getValueAt(i, 1).toString().replaceAll("'", "`") + " " + tabMode.getValueAt(i, 2).toString().replaceAll("'", "`") + "','" // Gabungan Tanggal & Jam
                                + tabMode.getValueAt(i, 4).toString().replaceAll("'", "`") + "','" // No.RM
                                + tabMode.getValueAt(i, 5).toString().replaceAll("'", "`") + "','" // Nama Pasien (Sesuai urutan kolom lama jika RM tidak ingin ditampilkan di kolom ini)
                                + tabMode.getValueAt(i, 6).toString().replaceAll("'", "`") + "','" // Tindakan Medis
                                + tabMode.getValueAt(i, 7).toString().replaceAll("'", "`") + "','" // Tindakan Medis
                                + Valid.SetAngka(bruto) + "','" // Masuk ke temp6
                                + Valid.SetAngka(sharing) + "','" // Masuk ke temp7
                                + Valid.SetAngka(jasaMedis) + "','" // Masuk ke temp8
                                + tabMode.getValueAt(i, 13).toString().replaceAll("'", "`") + "'," // Payor
                                + "'','','','','','','','','','','','','','','','','','','','','','','','','','','','" + akses.getalamatip() + "'", "JM Dokter"
                        );
                        row++;
                    }
                }
                row++;
                Sequel.menyimpan("temporary", "'" + row + "','>> Total Bruto :','','','','','','" + Valid.SetAngka(totalBruto)
                        + "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','" + akses.getalamatip() + "'", "JM Dokter"
                );

                row++;
                Sequel.menyimpan("temporary", "'" + row + "','>> Total Sharing :','','','','','','" + Valid.SetAngka(totalSharing)
                        + "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','','" + akses.getalamatip() + "'", "JM Dokter"
                );
                // Baris Total
                row++;
                Sequel.menyimpan("temporary", "'" + row + "','>> Jumlah :','','','','','','','" + LCount1.getText()
                        + "','','','','','','','','','','','','','','','','','','','','','','','','','','','','','" + akses.getalamatip() + "'", "JM Dokter"
                );

                Map<String, Object> param = new HashMap<>();
                param.put("namars", akses.getnamars());
                param.put("alamatrs", akses.getalamatrs());
                param.put("kotars", akses.getkabupatenrs());
                param.put("propinsirs", akses.getpropinsirs());
                param.put("kontakrs", akses.getkontakrs());
                param.put("emailrs", akses.getemailrs());
                param.put("dokter", nmdokter.getText());
                param.put("bulan", Tanggal.getSelectedItem().toString().substring(3, 10));
                param.put("logo", Sequel.cariGambar("select setting.logo from setting"));

                Valid.MyReportqry("rptSlipPiutangBayarJMDokter.jasper", "report", "[ Slip J.M. Dokter  ]", "select * from temporary where temporary.temp37='" + akses.getalamatip() + "' order by temporary.no", param);
                this.setCursor(Cursor.getDefaultCursor());
            }
        }
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
    }//GEN-LAST:event_BtnAll1ActionPerformed

    private void TanggalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_TanggalItemStateChanged
        autoNomor();
    }//GEN-LAST:event_TanggalItemStateChanged

    private void TglBilling1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TglBilling1KeyPressed
        Valid.pindah(evt, BtnKeluar, TglBilling2);
    }//GEN-LAST:event_TglBilling1KeyPressed

    private void TglBilling2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TglBilling2KeyPressed
        Valid.pindah(evt, TglBilling1, kddokter);
    }//GEN-LAST:event_TglBilling2KeyPressed

    private void BtnPetugas1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPetugas1ActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        akses.setform("KeuanganBayarJMDokter");
        mappingJMDokter.emptTeks();
        mappingJMDokter.isCek();
        mappingJMDokter.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        mappingJMDokter.setLocationRelativeTo(internalFrame1);
        mappingJMDokter.setAlwaysOnTop(false);
        mappingJMDokter.setVisible(true);
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnPetugas1ActionPerformed

    private void BtnPetugas1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPetugas1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnPetugas1KeyPressed

    private void ChkMappingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkMappingActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ChkMappingActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            KeuanganBayarJMDokter dialog = new KeuanganBayarJMDokter(new javax.swing.JFrame(), true);
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
    private widget.Button BtnCaraBayarRalanDokter;
    private widget.Button BtnCari;
    private widget.Button BtnCari1;
    private widget.Button BtnExport;
    private widget.Button BtnGaji;
    private widget.Button BtnKeluar;
    private widget.Button BtnPetugas;
    private widget.Button BtnPetugas1;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkMapping;
    private widget.CekBox ChkSemuaDokter;
    private widget.TextBox KdCaraBayar;
    private widget.TextBox Keterangan;
    private javax.swing.JLabel LCount;
    private javax.swing.JLabel LCount1;
    private widget.TextBox NmCaraBayar;
    private widget.TextBox NoTagihan;
    private javax.swing.JPopupMenu Popup;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.TextBox TKd;
    private widget.Tanggal Tanggal;
    private widget.Tanggal TglBilling1;
    private widget.Tanggal TglBilling2;
    private widget.CekBox chkLaborat;
    private widget.CekBox chkOperasi;
    private widget.CekBox chkRadiologi;
    private widget.CekBox chkRalan;
    private widget.CekBox chkRanap;
    private widget.ComboBox cmbCOB;
    private widget.ComboBox cmbStatus;
    private widget.InternalFrame internalFrame1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabelCOB;
    private widget.Label jLabelPotongan;
    private widget.ComboBox cmbPotongan;
    private javax.swing.JPanel jPanel1;
    private widget.TextBox kddokter;
    private widget.Label label11;
    private widget.Label label16;
    private widget.Label label17;
    private widget.Label label18;
    private widget.Label label19;
    private widget.Label label20;
    private widget.Label label32;
    private widget.Label label36;
    private widget.Label label39;
    private widget.TextBox nmdokter;
    private widget.panelisi panelisi1;
    private widget.panelisi panelisi3;
    private widget.panelisi panelisi4;
    private widget.panelisi panelisi5;
    private javax.swing.JMenuItem ppBersihkan;
    private javax.swing.JMenuItem ppSemua;
    private javax.swing.JMenuItem ppSlipGajiHTML;
    private javax.swing.JMenuItem ppPiutangHTML;
    private widget.Table tbBangsal;
    // End of variables declaration//GEN-END:variables

    private java.util.Map<String, String> cacheDokter = new java.util.HashMap<>();

    private void loadCacheDokter() {
        cacheDokter.clear();
        jmMappingCache.clear();
        rsMappingCache.clear();
        try {
            java.sql.PreparedStatement psDok = koneksi.prepareStatement("select kd_dokter, nm_dokter from dokter");
            java.sql.ResultSet rsDok = psDok.executeQuery();
            while (rsDok.next()) {
                String kd = rsDok.getString("kd_dokter");
                String nm = rsDok.getString("nm_dokter");
                if (kd != null) {
                    cacheDokter.put(kd, nm);
                    cacheDokter.put(kd.trim(), nm);
                }
            }
            rsDok.close();
            psDok.close();

            java.sql.PreparedStatement psMap = koneksi.prepareStatement("select kd_dokter, bagiandokter, bagianrs from maping_jm_dokter");
            java.sql.ResultSet rsMap = psMap.executeQuery();
            while (rsMap.next()) {
                String kd = rsMap.getString("kd_dokter");
                if (kd != null && !kd.trim().isEmpty()) {
                    jmMappingCache.put(kd.trim(), rsMap.getDouble("bagiandokter"));
                    rsMappingCache.put(kd.trim(), rsMap.getDouble("bagianrs"));
                }
            }
            rsMap.close();
            psMap.close();
        } catch (Exception e) {
            System.out.println("Notifikasi loadCacheDokter : " + e);
        }
    }

    private String getNamaDokter(String kdDokter) {
        if (kdDokter == null) {
            return "";
        }
        String kd = kdDokter.trim();
        if (kd.isEmpty() || kd.equals("-")) {
            return "-";
        }
        if (cacheDokter != null) {
            String nm = cacheDokter.get(kd);
            if (nm != null && !nm.trim().isEmpty()) {
                return nm;
            }
            nm = cacheDokter.get(kdDokter);
            if (nm != null && !nm.trim().isEmpty()) {
                return nm;
            }
        }
        String namaDb = Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", kd);
        if (namaDb == null || namaDb.trim().isEmpty()) {
            namaDb = Sequel.cariIsi("select nm_dokter from dokter where kd_dokter=?", kdDokter);
        }
        if (namaDb == null || namaDb.trim().isEmpty()) {
            namaDb = Sequel.cariIsi("select nama from pegawai where nik=?", kd);
        }
        if (namaDb == null || namaDb.trim().isEmpty()) {
            namaDb = Sequel.cariIsi("select nama from pegawai where nik=?", kdDokter);
        }
        if (namaDb == null || namaDb.trim().isEmpty()) {
            namaDb = Sequel.cariIsi("select nama from petugas where nip=?", kd);
        }
        if (namaDb == null || namaDb.trim().isEmpty()) {
            namaDb = Sequel.cariIsi("select nama from petugas where nip=?", kdDokter);
        }
        if (namaDb != null && !namaDb.trim().isEmpty()) {
            if (cacheDokter != null) {
                cacheDokter.put(kd, namaDb);
                cacheDokter.put(kdDokter, namaDb);
            }
            return namaDb;
        }
        return kd;
    }

    private javax.swing.SwingWorker<Void, Void> workerCari = null;

    private void prosesCari() {
        if ((kddokter.getText().trim().equals("") || nmdokter.getText().trim().equals("")) && !ChkSemuaDokter.isSelected()) {
            JOptionPane.showMessageDialog(null, "Silahkan pilih dokter yang mau dibayarkan jasa medisnya....!!!");
            return;
        }

        if (workerCari != null && !workerCari.isDone()) {
            JOptionPane.showMessageDialog(null, "Proses pencarian sedang berjalan, mohon tunggu sebentar...");
            return;
        }

        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        BtnCari.setEnabled(false);
        Valid.tabelKosong(tabMode);
        LCount.setText("Memuat data...");

        final String statusPilihan = cmbStatus.getSelectedItem() != null ? cmbStatus.getSelectedItem().toString() : "";
        final String tgl1 = Valid.SetTgl(TglBilling1.getSelectedItem().toString());
        final String tgl2 = Valid.SetTgl(TglBilling2.getSelectedItem().toString());
        final String cariText = TCari.getText().trim();

        workerCari = new javax.swing.SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                loadCacheDokter();
                sdahHitungDiskon.clear();
                sdahTampilDiskon.clear();

                // Tambahan Debug: Cek apakah input di text pencarian adalah no_rawat
                if (!cariText.isEmpty() && cariText.length() > 5) {
                    debugNoRawat(cariText);
                }

                filterbilling = "reg_periksa.no_rawat IN (select no_rawat from billing where tgl_byr between '" + tgl1 + "' AND '" + tgl2 + "' ) AND ";
                if (statusPilihan.equals("Semua")) {
                    prosesCariSemua();
                } else if (statusPilihan.equals("Piutang Belum Lunas atau Ditagihkan")) {
                    prosesCariPiutangBelumLunas();
                } else if (statusPilihan.equals("Piutang Sudah Lunas")) {
                    prosesCariPiutangSudahLunas();
                } else if (statusPilihan.equals("Sudah Bayar Non Piutang")) {
                    prosesCariSudahBayarNonPiutang();
                } else if (statusPilihan.equals("Belum Terclosing Kasir")) {
                    prosesCariBelumTerclosing();
                }

                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (Exception e) {
                    System.out.println("Notifikasi workerCari : " + e);
                } finally {
                    urutkanTabel();
                    hitungTotalTampil();
                    BtnCari.setEnabled(true);
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };

        workerCari.execute();
    }

    /**
     * Mengurutkan data pada tabMode secara kronologis berdasarkan: 1. Tanggal
     * Transaksi (Kolom 1) 2. No. Rawat (Kolom 3) 3. Jam Transaksi (Kolom 2)
     */
    private boolean isFilterCOBPass(Object payorObj) {
        if (cmbCOB == null || cmbCOB.getSelectedIndex() <= 0) {
            return true; // "Semua"
        }
        String payor = (payorObj != null) ? payorObj.toString() : "";
        boolean isCOB = payor.contains(" COB "); // Mencakup " COB " dan " COB JKN "
        boolean isCOBJKN = payor.contains(" COB JKN ");

        int idx = cmbCOB.getSelectedIndex();
        if (idx == 1) { // "Tanpa COB JKN" (Semua data kecuali COB JKN: Non-COB ikut, COB non-JKN ikut)
            return !isCOBJKN;
        } else if (idx == 2) { // "Non COB" (Hanya data murni bukan COB)
            return !isCOB;
        } else if (idx == 3) { // "Hanya COB" (Semua transaksi multi-penjamin COB)
            return isCOB;
        } else if (idx == 4) { // "COB JKN" (Khusus transaksi COB yang melibatkan BPJS Kesehatan / JKN)
            return isCOBJKN;
        } else if (idx == 5) { // "COB Non JKN" (Khusus transaksi COB selain JKN, misal Jasa Raharja + BPJS TK)
            return isCOB && !isCOBJKN;
        }
        return true;
    }

    private void hitungTotalTampil() {
        double totalJM = 0;
        int rowCount = tabMode.getRowCount();
        for (int r = 0; r < rowCount; r++) {
            Object val = tabMode.getValueAt(r, 11);
            if (val instanceof Number) {
                totalJM += ((Number) val).doubleValue();
            } else if (val != null) {
                try {
                    totalJM += Double.parseDouble(val.toString());
                } catch (Exception ignored) {
                }
            }
        }
        total = totalJM;
        LCount.setText(Valid.SetAngka(totalJM));
    }

    private void cmbCOBItemStateChanged(java.awt.event.ItemEvent evt) {
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            if (cmbStatus != null && cmbStatus.getSelectedIndex() != 0) {
                prosesCari();
            }
        }
    }

    private boolean isFilterPotonganPass(Object tindakanObj) {
        if (cmbPotongan == null || cmbPotongan.getSelectedIndex() <= 0) {
            return true;
        }
        String tindakan = (tindakanObj != null) ? tindakanObj.toString() : "";
        boolean hasPotongan = tindakan.contains(" | ");
        if (cmbPotongan.getSelectedIndex() == 1) {
            return hasPotongan;
        } else if (cmbPotongan.getSelectedIndex() == 2) {
            return !hasPotongan;
        }
        return true;
    }

    private void cmbPotonganItemStateChanged(java.awt.event.ItemEvent evt) {
        if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
            if (cmbStatus != null && cmbStatus.getSelectedIndex() != 0) {
                prosesCari();
            }
        }
    }

    private void urutkanTabel() {
        if (tabMode.getRowCount() <= 1) {
            return;
        }
        java.util.Vector data = tabMode.getDataVector();
        java.util.Collections.sort(data, new java.util.Comparator<java.util.Vector>() {
            @Override
            public int compare(java.util.Vector r1, java.util.Vector r2) {
                // Kolom 1: Tanggal Trx
                String tgl1 = r1.get(1) != null ? r1.get(1).toString() : "";
                String tgl2 = r2.get(1) != null ? r2.get(1).toString() : "";
                int compTgl = tgl1.compareTo(tgl2);
                if (compTgl != 0) {
                    return compTgl;
                }

                // Kolom 3: No. Rawat
                String rawat1 = r1.get(3) != null ? r1.get(3).toString() : "";
                String rawat2 = r2.get(3) != null ? r2.get(3).toString() : "";
                int compRawat = rawat1.compareTo(rawat2);
                if (compRawat != 0) {
                    return compRawat;
                }

                // Kolom 2: Jam Trx
                String jam1 = r1.get(2) != null ? r1.get(2).toString() : "";
                String jam2 = r2.get(2) != null ? r2.get(2).toString() : "";
                return jam1.compareTo(jam2);
            }
        });
        tabMode.fireTableDataChanged();
    }

    /**
     * Mendapatkan target filter kode dokter untuk query SQL. Mengembalikan "%"
     * jika checkbox ChkSemuaDokter aktif, atau kddokter.getText() jika
     * non-aktif.
     */
    private String getTargetDokter() {
        if (ChkSemuaDokter != null && ChkSemuaDokter.isSelected()) {
            return "%";
        }
        String kd = kddokter.getText().trim();
        // Exact match tanpa wildcard '%' di akhir agar kode D016 tidak mencocokkan D0163/D0162
        return kd.isEmpty() ? "%" : kd;
    }

    private void debugNoRawat(String noRawat) {
        if (noRawat == null || noRawat.trim().isEmpty()) {
            return;
        }
        System.out.println("\n=== DEBUG PENCARIAN NO RAWAT: " + noRawat + " ===");
        try {
            // rawat_jl_dr
            String q1 = "select count(*) as cnt from rawat_jl_dr where no_rawat=?";
            PreparedStatement p1 = koneksi.prepareStatement(q1);
            p1.setString(1, noRawat);
            ResultSet r1 = p1.executeQuery();
            if (r1.next()) {
                System.out.println("- Di tabel rawat_jl_dr: " + r1.getInt("cnt") + " tindakan");
            }
            r1.close();
            p1.close();

            // rawat_inap_dr
            String q2 = "select count(*) as cnt from rawat_inap_dr where no_rawat=?";
            PreparedStatement p2 = koneksi.prepareStatement(q2);
            p2.setString(1, noRawat);
            ResultSet r2 = p2.executeQuery();
            if (r2.next()) {
                System.out.println("- Di tabel rawat_inap_dr: " + r2.getInt("cnt") + " tindakan");
            }
            r2.close();
            p2.close();

            // operasi
            String q3 = "select count(*) as cnt from operasi where no_rawat=?";
            PreparedStatement p3 = koneksi.prepareStatement(q3);
            p3.setString(1, noRawat);
            ResultSet r3 = p3.executeQuery();
            if (r3.next()) {
                System.out.println("- Di tabel operasi: " + r3.getInt("cnt") + " tindakan");
            }
            r3.close();
            p3.close();

            // periksa_lab
            String q4 = "select count(*) as cnt from periksa_lab where no_rawat=?";
            PreparedStatement p4 = koneksi.prepareStatement(q4);
            p4.setString(1, noRawat);
            ResultSet r4 = p4.executeQuery();
            if (r4.next()) {
                System.out.println("- Di tabel periksa_lab: " + r4.getInt("cnt") + " tindakan");
            }
            r4.close();
            p4.close();

            // periksa_radiologi
            String q5 = "select count(*) as cnt from periksa_radiologi where no_rawat=?";
            PreparedStatement p5 = koneksi.prepareStatement(q5);
            p5.setString(1, noRawat);
            ResultSet r5 = p5.executeQuery();
            if (r5.next()) {
                System.out.println("- Di tabel periksa_radiologi: " + r5.getInt("cnt") + " tindakan");
            }
            r5.close();
            p5.close();

            // billing
            String q6 = "select status, tgl_byr from billing where no_rawat=? limit 1";
            PreparedStatement p6 = koneksi.prepareStatement(q6);
            p6.setString(1, noRawat);
            ResultSet r6 = p6.executeQuery();
            if (r6.next()) {
                System.out.println("- Status di tabel billing: " + r6.getString("status") + " (Tgl Bayar: " + r6.getString("tgl_byr") + ")");
            } else {
                System.out.println("- Tidak ada record di tabel billing");
            }
            r6.close();
            p6.close();

            System.out.println("===================================================\n");
        } catch (Exception e) {
            System.out.println("Error saat debug No Rawat: " + e.getMessage());
        }
    }

    private void getData() {
        row = tbBangsal.getRowCount();
        bayar = 0;
        totalrawatjalan = 0;
        totalrawatinap = 0;
        totallabrawatjalan = 0;
        totallabrawatinap = 0;
        totalradrawatjalan = 0;
        totalradrawatinap = 0;
        totaloperasirawatjalan = 0;
        totaloperasirawatinap = 0;
        for (i = 0; i < row; i++) {
            if (tbBangsal.getValueAt(i, 0).toString().equals("true")) {
                if (tbBangsal.getValueAt(i, 8).toString().contains("Rawat Jalan")) {
                    totalrawatjalan = totalrawatjalan + Double.parseDouble(tbBangsal.getValueAt(i, 9).toString());
                } else if (tbBangsal.getValueAt(i, 8).toString().contains("Rawat Inap")) {
                    totalrawatinap = totalrawatinap + Double.parseDouble(tbBangsal.getValueAt(i, 9).toString());
                } else if (tbBangsal.getValueAt(i, 8).toString().contains("Laborat Ralan")) {
                    totallabrawatjalan = totallabrawatjalan + Double.parseDouble(tbBangsal.getValueAt(i, 9).toString());
                } else if (tbBangsal.getValueAt(i, 8).toString().contains("Laborat Ranap")) {
                    totallabrawatinap = totallabrawatinap + Double.parseDouble(tbBangsal.getValueAt(i, 9).toString());
                } else if (tbBangsal.getValueAt(i, 8).toString().contains("Radiologi Ralan")) {
                    totalradrawatjalan = totalradrawatjalan + Double.parseDouble(tbBangsal.getValueAt(i, 9).toString());
                } else if (tbBangsal.getValueAt(i, 8).toString().contains("Radiologi Ranap")) {
                    totalradrawatinap = totalradrawatinap + Double.parseDouble(tbBangsal.getValueAt(i, 9).toString());
                } else if (tbBangsal.getValueAt(i, 8).toString().contains("Operasi Ralan")) {
                    totaloperasirawatjalan = totaloperasirawatjalan + Double.parseDouble(tbBangsal.getValueAt(i, 9).toString());
                } else if (tbBangsal.getValueAt(i, 8).toString().contains("Operasi Ranap")) {
                    totaloperasirawatinap = totaloperasirawatinap + Double.parseDouble(tbBangsal.getValueAt(i, 9).toString());
                }

                bayar = bayar + Double.parseDouble(tbBangsal.getValueAt(i, 9).toString());
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
                    "JMB" + Tanggal.getSelectedItem().toString().substring(6, 10) + Tanggal.getSelectedItem().toString().substring(3, 5) + Tanggal.getSelectedItem().toString().substring(0, 2), 3, NoTagihan);
        } else {
            // Use current date when Tanggal is null
            java.util.Date currentDate = new java.util.Date();
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = dateFormat.format(currentDate);
            Valid.autoNomer3("select ifnull(MAX(CONVERT(RIGHT(bayar_jm_dokter.no_bayar,3),signed)),0) from bayar_jm_dokter where bayar_jm_dokter.tanggal='" + Valid.SetTgl(formattedDate) + "' ",
                    "JMB" + formattedDate.substring(0, 4) + formattedDate.substring(5, 7) + formattedDate.substring(8, 10), 3, NoTagihan);
        }
    }

    public void isCek() {
        autoNomor();
        TCari.requestFocus();
        BtnSimpan.setEnabled(akses.getbayar_jm_dokter());
    }

    private void tampilAkunBayar() {
        try {
            form.file = new File("./cache/akunbayarhutang.iyem");
            form.file.createNewFile();
            form.fileWriter = new FileWriter(form.file);
            form.iyem = "";
            ps = koneksi.prepareStatement("select * from akun_bayar_hutang order by akun_bayar_hutang.nama_bayar");
            try {
                rs = ps.executeQuery();
                AkunBayar.removeAllItems();
                while (rs.next()) {
                    AkunBayar.addItem(rs.getString(1).replaceAll("\"", ""));
                    form.iyem = form.iyem + "{\"NamaAkun\":\"" + rs.getString(1).replaceAll("\"", "") + "\",\"KodeRek\":\"" + rs.getString(2) + "\"},";
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

            form.fileWriter.write("{\"akunbayarhutang\":[" + form.iyem.substring(0, form.iyem.length() - 1) + "]}");
            form.fileWriter.flush();
            form.fileWriter.close();
            form.iyem = null;
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void tampilAkunBayar2() {
        try {
            form.myObj = new FileReader("./cache/akunbayarhutang.iyem");
            form.root = form.mapper.readTree(form.myObj);
            form.response = form.root.path("akunbayarhutang");
            if (form.response.isArray()) {
                for (JsonNode list : form.response) {
                    AkunBayar.addItem(list.path("NamaAkun").asText().replaceAll("\"", ""));
                }
            }
            form.myObj.close();
        } catch (Exception ex) {
            System.out.println("Notifikasi : " + ex);
        }
    }

    private void prosesCariSemua() {
        try {
            total = 0;
            if (chkRalan.isSelected() == true) {
                //rawat jalan
                psrawatjalandr = koneksi.prepareStatement("select pasien.nm_pasien,rawat_jl_dr.biaya_rawat,jns_perawatan.tarif_tindakandr as master_tarif_dr,"
                        + "jns_perawatan.nm_perawatan,rawat_jl_dr.tgl_perawatan,rawat_jl_dr.jam_rawat,reg_periksa.kd_pj,rawat_jl_dr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_jl_dr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_jl_dr on rawat_jl_dr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan on rawat_jl_dr.kd_jenis_prw=jns_perawatan.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where rawat_jl_dr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_jl_dr.tarif_tindakandr>0 and "
                        + "concat(rawat_jl_dr.no_rawat,rawat_jl_dr.kd_jenis_prw,rawat_jl_dr.tgl_perawatan,rawat_jl_dr.jam_rawat,rawat_jl_dr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_jl_dr.no_rawat,bayar_rawat_jl_dr.kd_jenis_prw,bayar_rawat_jl_dr.tgl_perawatan,bayar_rawat_jl_dr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_jl_dr on bayar_rawat_jl_dr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_jl_dr.tgl_perawatan like ?)")
                        + "order by reg_periksa.tgl_registrasi,jns_perawatan.nm_perawatan");
                psrawatjalandrpr = koneksi.prepareStatement("select pasien.nm_pasien,rawat_jl_drpr.biaya_rawat,jns_perawatan.tarif_tindakandr as master_tarif_dr,"
                        + "jns_perawatan.nm_perawatan,rawat_jl_drpr.tgl_perawatan,rawat_jl_drpr.jam_rawat,reg_periksa.kd_pj,rawat_jl_drpr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_jl_drpr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_jl_drpr on rawat_jl_drpr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan on rawat_jl_drpr.kd_jenis_prw=jns_perawatan.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where rawat_jl_drpr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_jl_drpr.tarif_tindakandr>0 and "
                        + "concat(rawat_jl_drpr.no_rawat,rawat_jl_drpr.kd_jenis_prw,rawat_jl_drpr.tgl_perawatan,rawat_jl_drpr.jam_rawat,rawat_jl_drpr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_jl_drpr.no_rawat,bayar_rawat_jl_drpr.kd_jenis_prw,bayar_rawat_jl_drpr.tgl_perawatan,bayar_rawat_jl_drpr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_jl_drpr on bayar_rawat_jl_drpr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_jl_drpr.tgl_perawatan like ?)")
                        + "order by reg_periksa.tgl_registrasi,jns_perawatan.nm_perawatan");
                try {
                    psrawatjalandr.setString(1, getTargetDokter());
                    psrawatjalandr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatjalandr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatjalandr = psrawatjalandr.executeQuery();

                    psrawatjalandrpr.setString(1, getTargetDokter());
                    psrawatjalandrpr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatjalandrpr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatjalandrpr = psrawatjalandrpr.executeQuery();

                    while (rsrawatjalandr.next()) {
                        String tglTrx = rsrawatjalandr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatjalandr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatjalandr.getString("kd_dokter"), tarifAsli, rsrawatjalandr.getString("no_rawat"), rsrawatjalandr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatjalandr.getString("jam_rawat"), rsrawatjalandr.getString("no_rawat"), rsrawatjalandr.getString("no_rkm_medis"),
                            rsrawatjalandr.getString("nm_pasien"), rsrawatjalandr.getString("kd_jenis_prw"), cekPotongan(rsrawatjalandr.getString("no_rawat"), rsrawatjalandr.getString("nm_perawatan")),
                            "Rawat Jalan Dr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatjalandr.getString("kd_pj") + "'") + isCekCOB(rsrawatjalandr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatjalandr.getString("no_rawat") + "' LIMIT 1"), rsrawatjalandr.getString("kd_dokter"), getNamaDokter(rsrawatjalandr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
                    }

                    while (rsrawatjalandrpr.next()) {
                        String tglTrx = rsrawatjalandrpr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatjalandrpr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatjalandrpr.getString("kd_dokter"), tarifAsli, rsrawatjalandrpr.getString("no_rawat"), rsrawatjalandrpr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatjalandrpr.getString("jam_rawat"), rsrawatjalandrpr.getString("no_rawat"), rsrawatjalandrpr.getString("no_rkm_medis"),
                            rsrawatjalandrpr.getString("nm_pasien"), rsrawatjalandrpr.getString("kd_jenis_prw"), cekPotongan(rsrawatjalandrpr.getString("no_rawat"), rsrawatjalandrpr.getString("nm_perawatan")),
                            "Rawat Jalan DrPr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatjalandrpr.getString("kd_pj") + "'") + isCekCOB(rsrawatjalandrpr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatjalandrpr.getString("no_rawat") + "' LIMIT 1"), rsrawatjalandrpr.getString("kd_dokter"), getNamaDokter(rsrawatjalandrpr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
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
            }

            if (chkRanap.isSelected() == true) {
                //rawat inap
                psrawatinapdr = koneksi.prepareStatement(
                        "select pasien.nm_pasien,jns_perawatan_inap.nm_perawatan,rawat_inap_dr.biaya_rawat,jns_perawatan_inap.tarif_tindakandr as master_tarif_dr,"
                        + "rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,reg_periksa.kd_pj,rawat_inap_dr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_inap_dr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_inap_dr on rawat_inap_dr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan_inap on rawat_inap_dr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where rawat_inap_dr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_inap_dr.tarif_tindakandr>0 and "
                        + "concat(rawat_inap_dr.no_rawat,rawat_inap_dr.kd_jenis_prw,rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,rawat_inap_dr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_inap_dr.no_rawat,bayar_rawat_inap_dr.kd_jenis_prw,bayar_rawat_inap_dr.tgl_perawatan,bayar_rawat_inap_dr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_inap_dr on bayar_rawat_inap_dr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_inap.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_inap_dr.tgl_perawatan like ?)")
                        + "order by rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,jns_perawatan_inap.nm_perawatan  ");
                psrawatinapdrpr = koneksi.prepareStatement(
                        "select pasien.nm_pasien,jns_perawatan_inap.nm_perawatan,rawat_inap_drpr.biaya_rawat,jns_perawatan_inap.tarif_tindakandr as master_tarif_dr,"
                        + "rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,reg_periksa.kd_pj,rawat_inap_drpr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_inap_drpr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_inap_drpr on rawat_inap_drpr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan_inap on rawat_inap_drpr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where rawat_inap_drpr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_inap_drpr.tarif_tindakandr>0 and "
                        + "concat(rawat_inap_drpr.no_rawat,rawat_inap_drpr.kd_jenis_prw,rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,rawat_inap_drpr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_inap_drpr.no_rawat,bayar_rawat_inap_drpr.kd_jenis_prw,bayar_rawat_inap_drpr.tgl_perawatan,bayar_rawat_inap_drpr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_inap_drpr on bayar_rawat_inap_drpr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_inap.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_inap_drpr.tgl_perawatan like ?)")
                        + "order by rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,jns_perawatan_inap.nm_perawatan  ");
                try {
                    psrawatinapdr.setString(1, getTargetDokter());
                    psrawatinapdr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatinapdr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatinapdr = psrawatinapdr.executeQuery();

                    psrawatinapdrpr.setString(1, getTargetDokter());
                    psrawatinapdrpr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatinapdrpr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatinapdrpr = psrawatinapdrpr.executeQuery();

                    while (rsrawatinapdr.next()) {
                        String tglTrx = rsrawatinapdr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatinapdr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatinapdr.getString("kd_dokter"), tarifAsli, rsrawatinapdr.getString("no_rawat"), rsrawatinapdr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatinapdr.getString("jam_rawat"), rsrawatinapdr.getString("no_rawat"), rsrawatinapdr.getString("no_rkm_medis"),
                            rsrawatinapdr.getString("nm_pasien"), rsrawatinapdr.getString("kd_jenis_prw"), cekPotongan(rsrawatinapdr.getString("no_rawat"), rsrawatinapdr.getString("nm_perawatan")),
                            "Rawat Inap Dr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatinapdr.getString("kd_pj") + "'") + isCekCOB(rsrawatinapdr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatinapdr.getString("no_rawat") + "' LIMIT 1"), rsrawatinapdr.getString("kd_dokter"), getNamaDokter(rsrawatinapdr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
                    }

                    while (rsrawatinapdrpr.next()) {
                        String tglTrx = rsrawatinapdrpr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatinapdrpr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatinapdrpr.getString("kd_dokter"), tarifAsli, rsrawatinapdrpr.getString("no_rawat"), rsrawatinapdrpr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatinapdrpr.getString("jam_rawat"), rsrawatinapdrpr.getString("no_rawat"), rsrawatinapdrpr.getString("no_rkm_medis"),
                            rsrawatinapdrpr.getString("nm_pasien"), rsrawatinapdrpr.getString("kd_jenis_prw"), cekPotongan(rsrawatinapdrpr.getString("no_rawat"), rsrawatinapdrpr.getString("nm_perawatan")),
                            "Rawat Inap DrPr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatinapdrpr.getString("kd_pj") + "'") + isCekCOB(rsrawatinapdrpr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatinapdrpr.getString("no_rawat") + "' LIMIT 1"), rsrawatinapdrpr.getString("kd_dokter"), getNamaDokter(rsrawatinapdrpr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
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
            }

            if (chkOperasi.isSelected() == true) {
                psbiayaoperator1 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator1,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.operator1 as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where operasi.operator1 like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayaoperator1>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.operator1) not in "
                        + "(select concat(bayar_operasi_operator1.no_rawat,bayar_operasi_operator1.kode_paket,bayar_operasi_operator1.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_operator1 on bayar_operasi_operator1.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayaoperator2 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator2,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.operator2 as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where operasi.operator2 like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayaoperator2>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.operator2) not in "
                        + "(select concat(bayar_operasi_operator2.no_rawat,bayar_operasi_operator2.kode_paket,bayar_operasi_operator2.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_operator2 on bayar_operasi_operator2.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayaoperator3 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator3,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.operator3 as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where operasi.operator3 like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayaoperator3>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.operator3) not in "
                        + "(select concat(bayar_operasi_operator3.no_rawat,bayar_operasi_operator3.kode_paket,bayar_operasi_operator3.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_operator3 on bayar_operasi_operator3.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayadokter_anak = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayadokter_anak,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_anak as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where operasi.dokter_anak like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayadokter_anak>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_anak) not in "
                        + "(select concat(bayar_operasi_dokter_anak.no_rawat,bayar_operasi_dokter_anak.kode_paket,bayar_operasi_dokter_anak.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_anak on bayar_operasi_dokter_anak.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiaya_dokter_umum = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biaya_dokter_umum,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_umum as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where operasi.dokter_umum like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biaya_dokter_umum>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_umum) not in "
                        + "(select concat(bayar_operasi_dokter_umum.no_rawat,bayar_operasi_dokter_umum.kode_paket,bayar_operasi_dokter_umum.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_umum on bayar_operasi_dokter_umum.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiaya_dokter_pjanak = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biaya_dokter_pjanak,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_pjanak as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where operasi.dokter_pjanak like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biaya_dokter_pjanak>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_pjanak) not in "
                        + "(select concat(bayar_operasi_dokter_pjanak.no_rawat,bayar_operasi_dokter_pjanak.kode_paket,bayar_operasi_dokter_pjanak.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_pjanak on bayar_operasi_dokter_pjanak.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayadokter_anestesi = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayadokter_anestesi,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_anestesi as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where operasi.dokter_anestesi like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayadokter_anestesi>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_anestesi) not in "
                        + "(select concat(bayar_operasi_dokter_anestesi.no_rawat,bayar_operasi_dokter_anestesi.kode_paket,bayar_operasi_dokter_anestesi.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_anestesi on bayar_operasi_dokter_anestesi.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                try {
                    psbiayaoperator1.setString(1, getTargetDokter());
                    psbiayaoperator1.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayaoperator1.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayaoperator1 = psbiayaoperator1.executeQuery();

                    psbiayaoperator2.setString(1, getTargetDokter());
                    psbiayaoperator2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayaoperator2.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayaoperator2 = psbiayaoperator2.executeQuery();

                    psbiayaoperator3.setString(1, getTargetDokter());
                    psbiayaoperator3.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayaoperator3.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayaoperator3 = psbiayaoperator3.executeQuery();

                    psbiayadokter_anak.setString(1, getTargetDokter());
                    psbiayadokter_anak.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayadokter_anak.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayadokter_anak = psbiayadokter_anak.executeQuery();

                    psbiaya_dokter_umum.setString(1, getTargetDokter());
                    psbiaya_dokter_umum.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiaya_dokter_umum.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiaya_dokter_umum = psbiaya_dokter_umum.executeQuery();

                    psbiaya_dokter_pjanak.setString(1, getTargetDokter());
                    psbiaya_dokter_pjanak.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiaya_dokter_pjanak.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiaya_dokter_pjanak = psbiaya_dokter_pjanak.executeQuery();

                    psbiayadokter_anestesi.setString(1, getTargetDokter());
                    psbiayadokter_anestesi.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayadokter_anestesi.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayadokter_anestesi = psbiayadokter_anestesi.executeQuery();

                    // Reset Set tracker diskon setiap kali blok operasi di-load
                    sdahHitungDiskon.clear();
                    sdahTampilDiskon.clear();

                    while (rsbiayaoperator1.next()) {
                        String tglOpOp1 = rsbiayaoperator1.getString("tgl_operasi");
                        String kodeOp1 = rsbiayaoperator1.getString("kode_paket");
                        double tarifMurniOp1 = rsbiayaoperator1.getDouble("biayaoperator1");
                        double[] calcOp1 = hitungJMOperasiPotongan(rsbiayaoperator1.getString("no_rawat"), rsbiayaoperator1.getString("nm_perawatan"), rsbiayaoperator1.getString("kd_dokter"), tarifMurniOp1, kodeOp1, tglOpOp1);
                        double brutoOp1 = calcOp1[0];
                        double sharingOp1 = calcOp1[1];
                        double jmOp1 = calcOp1[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpOp1), getJamOnly(tglOpOp1), rsbiayaoperator1.getString("no_rawat"), rsbiayaoperator1.getString("no_rkm_medis"),
                            rsbiayaoperator1.getString("nm_pasien"), kodeOp1, cekPotongan(rsbiayaoperator1.getString("no_rawat"), rsbiayaoperator1.getString("nm_perawatan"), kodeOp1, tglOpOp1, rsbiayaoperator1.getString("kd_dokter")) + "(Operator 1)",
                            "Operasi " + rsbiayaoperator1.getString("status") + " Op1", brutoOp1, sharingOp1, jmOp1, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayaoperator1.getString("kd_pj") + "'") + isCekCOB(rsbiayaoperator1.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayaoperator1.getString("no_rawat") + "' LIMIT 1"), rsbiayaoperator1.getString("kd_dokter"), getNamaDokter(rsbiayaoperator1.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmOp1;
                    }

                    while (rsbiayaoperator2.next()) {
                        String tglOpOp2 = rsbiayaoperator2.getString("tgl_operasi");
                        String kodeOp2 = rsbiayaoperator2.getString("kode_paket");
                        double tarifMurniOp2 = rsbiayaoperator2.getDouble("biayaoperator2");
                        double[] calcOp2 = hitungJMOperasiPotongan(rsbiayaoperator2.getString("no_rawat"), rsbiayaoperator2.getString("nm_perawatan"), rsbiayaoperator2.getString("kd_dokter"), tarifMurniOp2, kodeOp2, tglOpOp2);
                        double brutoOp2 = calcOp2[0];
                        double sharingOp2 = calcOp2[1];
                        double jmOp2 = calcOp2[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpOp2), getJamOnly(tglOpOp2), rsbiayaoperator2.getString("no_rawat"), rsbiayaoperator2.getString("no_rkm_medis"),
                            rsbiayaoperator2.getString("nm_pasien"), kodeOp2, cekPotongan(rsbiayaoperator2.getString("no_rawat"), rsbiayaoperator2.getString("nm_perawatan"), kodeOp2, tglOpOp2, rsbiayaoperator2.getString("kd_dokter")) + "(Operator 2)",
                            "Operasi " + rsbiayaoperator2.getString("status") + " Op2", brutoOp2, sharingOp2, jmOp2, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayaoperator2.getString("kd_pj") + "'") + isCekCOB(rsbiayaoperator2.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayaoperator2.getString("no_rawat") + "' LIMIT 1"), rsbiayaoperator2.getString("kd_dokter"), getNamaDokter(rsbiayaoperator2.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmOp2;
                    }

                    while (rsbiayaoperator3.next()) {
                        String tglOpOp3 = rsbiayaoperator3.getString("tgl_operasi");
                        String kodeOp3 = rsbiayaoperator3.getString("kode_paket");
                        double tarifMurniOp3 = rsbiayaoperator3.getDouble("biayaoperator3");
                        double[] calcOp3 = hitungJMOperasiPotongan(rsbiayaoperator3.getString("no_rawat"), rsbiayaoperator3.getString("nm_perawatan"), rsbiayaoperator3.getString("kd_dokter"), tarifMurniOp3, kodeOp3, tglOpOp3);
                        double brutoOp3 = calcOp3[0];
                        double sharingOp3 = calcOp3[1];
                        double jmOp3 = calcOp3[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpOp3), getJamOnly(tglOpOp3), rsbiayaoperator3.getString("no_rawat"), rsbiayaoperator3.getString("no_rkm_medis"),
                            rsbiayaoperator3.getString("nm_pasien"), kodeOp3, cekPotongan(rsbiayaoperator3.getString("no_rawat"), rsbiayaoperator3.getString("nm_perawatan"), kodeOp3, tglOpOp3, rsbiayaoperator3.getString("kd_dokter")) + "(Operator 3)",
                            "Operasi " + rsbiayaoperator3.getString("status") + " Op3", brutoOp3, sharingOp3, jmOp3, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayaoperator3.getString("kd_pj") + "'") + isCekCOB(rsbiayaoperator3.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayaoperator3.getString("no_rawat") + "' LIMIT 1"), rsbiayaoperator3.getString("kd_dokter"), getNamaDokter(rsbiayaoperator3.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmOp3;
                    }

                    while (rsbiayadokter_anak.next()) {
                        String tglOpDrAnak = rsbiayadokter_anak.getString("tgl_operasi");
                        String kodeDrAnak = rsbiayadokter_anak.getString("kode_paket");
                        double tarifMurniDrAnak = rsbiayadokter_anak.getDouble("biayadokter_anak");
                        double[] calcDrAnak = hitungJMOperasiPotongan(rsbiayadokter_anak.getString("no_rawat"), rsbiayadokter_anak.getString("nm_perawatan"), rsbiayadokter_anak.getString("kd_dokter"), tarifMurniDrAnak, kodeDrAnak, tglOpDrAnak);
                        double brutoDrAnak = calcDrAnak[0];
                        double sharingDrAnak = calcDrAnak[1];
                        double jmDrAnak = calcDrAnak[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrAnak), getJamOnly(tglOpDrAnak), rsbiayadokter_anak.getString("no_rawat"), rsbiayadokter_anak.getString("no_rkm_medis"),
                            rsbiayadokter_anak.getString("nm_pasien"), kodeDrAnak, cekPotongan(rsbiayadokter_anak.getString("no_rawat"), rsbiayadokter_anak.getString("nm_perawatan"), kodeDrAnak, tglOpDrAnak, rsbiayadokter_anak.getString("kd_dokter")) + "(dr Anak)",
                            "Operasi " + rsbiayadokter_anak.getString("status") + " dr Anak", brutoDrAnak, sharingDrAnak, jmDrAnak, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayadokter_anak.getString("kd_pj") + "'") + isCekCOB(rsbiayadokter_anak.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayadokter_anak.getString("no_rawat") + "' LIMIT 1"), rsbiayadokter_anak.getString("kd_dokter"), getNamaDokter(rsbiayadokter_anak.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrAnak;
                    }

                    while (rsbiayadokter_anestesi.next()) {
                        String tglOpDrAnestesi = rsbiayadokter_anestesi.getString("tgl_operasi");
                        String kodeDrAnestesi = rsbiayadokter_anestesi.getString("kode_paket");
                        double tarifMurniDrAnestesi = rsbiayadokter_anestesi.getDouble("biayadokter_anestesi");
                        double[] calcDrAnestesi = hitungJMOperasiPotongan(rsbiayadokter_anestesi.getString("no_rawat"), rsbiayadokter_anestesi.getString("nm_perawatan"), rsbiayadokter_anestesi.getString("kd_dokter"), tarifMurniDrAnestesi, kodeDrAnestesi, tglOpDrAnestesi);
                        double brutoDrAnestesi = calcDrAnestesi[0];
                        double sharingDrAnestesi = calcDrAnestesi[1];
                        double jmDrAnestesi = calcDrAnestesi[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrAnestesi), getJamOnly(tglOpDrAnestesi), rsbiayadokter_anestesi.getString("no_rawat"), rsbiayadokter_anestesi.getString("no_rkm_medis"),
                            rsbiayadokter_anestesi.getString("nm_pasien"), kodeDrAnestesi, cekPotongan(rsbiayadokter_anestesi.getString("no_rawat"), rsbiayadokter_anestesi.getString("nm_perawatan"), kodeDrAnestesi, tglOpDrAnestesi, rsbiayadokter_anestesi.getString("kd_dokter")) + "(dr Anestesi)",
                            "Operasi " + rsbiayadokter_anestesi.getString("status") + " dr Anastesi", brutoDrAnestesi, sharingDrAnestesi, jmDrAnestesi, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayadokter_anestesi.getString("kd_pj") + "'") + isCekCOB(rsbiayadokter_anestesi.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayadokter_anestesi.getString("no_rawat") + "' LIMIT 1"), rsbiayadokter_anestesi.getString("kd_dokter"), getNamaDokter(rsbiayadokter_anestesi.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrAnestesi;
                    }

                    while (rsbiaya_dokter_pjanak.next()) {
                        String tglOpDrPjAnak = rsbiaya_dokter_pjanak.getString("tgl_operasi");
                        String kodeDrPjAnak = rsbiaya_dokter_pjanak.getString("kode_paket");
                        double tarifMurniDrPjAnak = rsbiaya_dokter_pjanak.getDouble("biaya_dokter_pjanak");
                        double[] calcDrPjAnak = hitungJMOperasiPotongan(rsbiaya_dokter_pjanak.getString("no_rawat"), rsbiaya_dokter_pjanak.getString("nm_perawatan"), rsbiaya_dokter_pjanak.getString("kd_dokter"), tarifMurniDrPjAnak, kodeDrPjAnak, tglOpDrPjAnak);
                        double brutoDrPjAnak = calcDrPjAnak[0];
                        double sharingDrPjAnak = calcDrPjAnak[1];
                        double jmDrPjAnak = calcDrPjAnak[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrPjAnak), getJamOnly(tglOpDrPjAnak), rsbiaya_dokter_pjanak.getString("no_rawat"), rsbiaya_dokter_pjanak.getString("no_rkm_medis"),
                            rsbiaya_dokter_pjanak.getString("nm_pasien"), kodeDrPjAnak, cekPotongan(rsbiaya_dokter_pjanak.getString("no_rawat"), rsbiaya_dokter_pjanak.getString("nm_perawatan"), kodeDrPjAnak, tglOpDrPjAnak, rsbiaya_dokter_pjanak.getString("kd_dokter")) + "(dr Pj Anak)",
                            "Operasi " + rsbiaya_dokter_pjanak.getString("status") + " dr PJ Anak", brutoDrPjAnak, sharingDrPjAnak, jmDrPjAnak, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiaya_dokter_pjanak.getString("kd_pj") + "'") + isCekCOB(rsbiaya_dokter_pjanak.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiaya_dokter_pjanak.getString("no_rawat") + "' LIMIT 1"), rsbiaya_dokter_pjanak.getString("kd_dokter"), getNamaDokter(rsbiaya_dokter_pjanak.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrPjAnak;
                    }

                    while (rsbiaya_dokter_umum.next()) {
                        String tglOpDrUmum = rsbiaya_dokter_umum.getString("tgl_operasi");
                        double tarifMurniDrUmum = rsbiaya_dokter_umum.getDouble("biaya_dokter_umum");
                        double[] calcDrUmum = hitungJMOperasiPotongan(rsbiaya_dokter_umum.getString("no_rawat"), rsbiaya_dokter_umum.getString("nm_perawatan"), rsbiaya_dokter_umum.getString("kd_dokter"), tarifMurniDrUmum, rsbiaya_dokter_umum.getString("kode_paket"), tglOpDrUmum);
                        double brutoDrUmum = calcDrUmum[0];
                        double sharingDrUmum = calcDrUmum[1];
                        double jmDrUmum = calcDrUmum[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrUmum), getJamOnly(tglOpDrUmum), rsbiaya_dokter_umum.getString("no_rawat"), rsbiaya_dokter_umum.getString("no_rkm_medis"),
                            rsbiaya_dokter_umum.getString("nm_pasien"), rsbiaya_dokter_umum.getString("kode_paket"), cekPotongan(rsbiaya_dokter_umum.getString("no_rawat"), rsbiaya_dokter_umum.getString("nm_perawatan"), rsbiaya_dokter_umum.getString("kode_paket"), tglOpDrUmum, rsbiaya_dokter_umum.getString("kd_dokter")) + "(dr Umum)",
                            "Operasi " + rsbiaya_dokter_umum.getString("status") + " dr Umum", brutoDrUmum, sharingDrUmum, jmDrUmum, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiaya_dokter_umum.getString("kd_pj") + "'") + isCekCOB(rsbiaya_dokter_umum.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiaya_dokter_umum.getString("no_rawat") + "' LIMIT 1"), rsbiaya_dokter_umum.getString("kd_dokter"), getNamaDokter(rsbiaya_dokter_umum.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrUmum;
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
                    if (rsbiayadokter_anestesi != null) {
                        rsbiayadokter_anestesi.close();
                    }
                    if (psbiayadokter_anestesi != null) {
                        psbiayadokter_anestesi.close();
                    }
                }
            }

            if (chkLaborat.isSelected() == true) {
                //periksa lab
                psperiksa_lab = koneksi.prepareStatement(
                        "select periksa_lab.tarif_tindakan_dokter,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_lab.status,"
                        + "jns_perawatan_lab.nm_perawatan,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,reg_periksa.kd_pj,periksa_lab.kd_dokter "
                        + " from periksa_lab inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " where periksa_lab.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_lab.tarif_tindakan_dokter>0 and "
                        + " concat(periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.kd_dokter) not in "
                        + " (select concat(bayar_periksa_lab.no_rawat,bayar_periksa_lab.kd_jenis_prw,bayar_periksa_lab.tgl_periksa,bayar_periksa_lab.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_lab on bayar_periksa_lab.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or  jns_perawatan_lab.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam,jns_perawatan_lab.nm_perawatan  ");
                try {
                    psperiksa_lab.setString(1, getTargetDokter());
                    psperiksa_lab.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_lab.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_lab = psperiksa_lab.executeQuery();

                    while (rsperiksa_lab.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_lab.getString("kd_dokter"), rsperiksa_lab.getDouble("tarif_tindakan_dokter"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_lab.getString("tgl_periksa"), rsperiksa_lab.getString("jam"), rsperiksa_lab.getString("no_rawat"), rsperiksa_lab.getString("no_rkm_medis"),
                            rsperiksa_lab.getString("nm_pasien") + " (" + rsperiksa_lab.getString("kd_pj") + ")", rsperiksa_lab.getString("kd_jenis_prw"), rsperiksa_lab.getString("nm_perawatan"),
                            "Laborat " + rsperiksa_lab.getString("status") + " PJ", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_lab.getString("kd_pj") + "'") + isCekCOB(rsperiksa_lab.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsperiksa_lab.getString("no_rawat") + "' LIMIT 1"), rsperiksa_lab.getString("kd_dokter"), getNamaDokter(rsperiksa_lab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Lab : " + e);
                } finally {
                    if (rsperiksa_lab != null) {
                        rsperiksa_lab.close();
                    }
                    if (psperiksa_lab != null) {
                        psperiksa_lab.close();
                    }
                }

                //detail periksa lab
                psdetaillab = koneksi.prepareStatement(
                        "select detail_periksa_lab.bagian_dokter,pasien.nm_pasien,periksa_lab.status,detail_periksa_lab.id_template,"
                        + "template_laboratorium.Pemeriksaan,reg_periksa.kd_pj,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                        + "periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.kd_jenis_prw,periksa_lab.kd_dokter "
                        + "from detail_periksa_lab inner join periksa_lab on periksa_lab.no_rawat=detail_periksa_lab.no_rawat "
                        + "and periksa_lab.kd_jenis_prw=detail_periksa_lab.kd_jenis_prw "
                        + "and periksa_lab.tgl_periksa=detail_periksa_lab.tgl_periksa "
                        + "and periksa_lab.jam=detail_periksa_lab.jam "
                        + "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where periksa_lab.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and detail_periksa_lab.bagian_dokter>0 and "
                        + "concat(detail_periksa_lab.no_rawat,detail_periksa_lab.kd_jenis_prw,detail_periksa_lab.tgl_periksa,detail_periksa_lab.jam,detail_periksa_lab.id_template,periksa_lab.kd_dokter) not in "
                        + "(select concat(bayar_detail_periksa_lab.no_rawat,bayar_detail_periksa_lab.kd_jenis_prw,bayar_detail_periksa_lab.tgl_periksa,bayar_detail_periksa_lab.jam,bayar_detail_periksa_lab.id_template,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_detail_periksa_lab on bayar_detail_periksa_lab.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or template_laboratorium.Pemeriksaan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam");
                try {
                    psdetaillab.setString(1, getTargetDokter());
                    psdetaillab.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psdetaillab.setString(3, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(4, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(5, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(6, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsdetaillab = psdetaillab.executeQuery();

                    while (rsdetaillab.next()) {
                        double jmTariff = hitungJMDokter(rsdetaillab.getString("kd_dokter"), rsdetaillab.getDouble("bagian_dokter"));
                        tabMode.addRow(new Object[]{
                            false, rsdetaillab.getString("tgl_periksa"), rsdetaillab.getString("jam"), rsdetaillab.getString("no_rawat"), rsdetaillab.getString("no_rkm_medis"),
                            rsdetaillab.getString("nm_pasien") + " (" + rsdetaillab.getString("kd_pj") + ")", rsdetaillab.getString("kd_jenis_prw"), rsdetaillab.getString("Pemeriksaan"),
                            "Laborat " + rsdetaillab.getString("status") + " PJ Detail", currentBruto, currentSharing, jmTariff, rsdetaillab.getString("id_template"), Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsdetaillab.getString("kd_pj") + "'") + isCekCOB(rsdetaillab.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsdetaillab.getString("no_rawat") + "' LIMIT 1"), rsdetaillab.getString("kd_dokter"), getNamaDokter(rsdetaillab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Detail Lab : " + e);
                } finally {
                    if (rsdetaillab != null) {
                        rsdetaillab.close();
                    }
                    if (psdetaillab != null) {
                        psdetaillab.close();
                    }
                }

                //periksa lab perujuk
                psperiksa_lab2 = koneksi.prepareStatement(
                        "select periksa_lab.tarif_perujuk,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_lab.status,"
                        + "jns_perawatan_lab.nm_perawatan,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,reg_periksa.kd_pj,periksa_lab.dokter_perujuk as kd_dokter "
                        + " from periksa_lab inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " where  periksa_lab.dokter_perujuk like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_lab.tarif_perujuk>0 and "
                        + " concat(periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.dokter_perujuk) not in "
                        + " (select concat(bayar_periksa_lab_perujuk.no_rawat,bayar_periksa_lab_perujuk.kd_jenis_prw,bayar_periksa_lab_perujuk.tgl_periksa,bayar_periksa_lab_perujuk.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_lab_perujuk on bayar_periksa_lab_perujuk.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or  jns_perawatan_lab.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam,jns_perawatan_lab.nm_perawatan  ");
                try {
                    psperiksa_lab2.setString(1, getTargetDokter());
                    psperiksa_lab2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_lab2.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_lab = psperiksa_lab2.executeQuery();

                    while (rsperiksa_lab.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_lab.getString("kd_dokter"), rsperiksa_lab.getDouble("tarif_perujuk"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_lab.getString("tgl_periksa"), rsperiksa_lab.getString("jam"), rsperiksa_lab.getString("no_rawat"), rsperiksa_lab.getString("no_rkm_medis"),
                            rsperiksa_lab.getString("nm_pasien") + " (" + rsperiksa_lab.getString("kd_pj") + ")", rsperiksa_lab.getString("kd_jenis_prw"), rsperiksa_lab.getString("nm_perawatan"),
                            "Laborat " + rsperiksa_lab.getString("status") + " Perujuk", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_lab.getString("kd_pj") + "'") + isCekCOB(rsperiksa_lab.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsperiksa_lab.getString("no_rawat") + "' LIMIT 1"), rsperiksa_lab.getString("kd_dokter"), getNamaDokter(rsperiksa_lab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Perujuk Lab : " + e);
                } finally {
                    if (rsperiksa_lab != null) {
                        rsperiksa_lab.close();
                    }
                    if (psperiksa_lab2 != null) {
                        psperiksa_lab2.close();
                    }
                }

                psdetaillab2 = koneksi.prepareStatement(
                        "select detail_periksa_lab.bagian_perujuk,pasien.nm_pasien,periksa_lab.status,detail_periksa_lab.id_template,"
                        + "template_laboratorium.Pemeriksaan,reg_periksa.kd_pj,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                        + "periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.kd_jenis_prw,periksa_lab.dokter_perujuk as kd_dokter "
                        + "from detail_periksa_lab inner join periksa_lab on periksa_lab.no_rawat=detail_periksa_lab.no_rawat "
                        + "and periksa_lab.kd_jenis_prw=detail_periksa_lab.kd_jenis_prw "
                        + "and periksa_lab.tgl_periksa=detail_periksa_lab.tgl_periksa "
                        + "and periksa_lab.jam=detail_periksa_lab.jam "
                        + "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where periksa_lab.dokter_perujuk like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and detail_periksa_lab.bagian_perujuk>0 and "
                        + "concat(detail_periksa_lab.no_rawat,detail_periksa_lab.kd_jenis_prw,detail_periksa_lab.tgl_periksa,detail_periksa_lab.jam,detail_periksa_lab.id_template,periksa_lab.dokter_perujuk) not in "
                        + "(select concat(bayar_detail_periksa_lab_perujuk.no_rawat,bayar_detail_periksa_lab_perujuk.kd_jenis_prw,bayar_detail_periksa_lab_perujuk.tgl_periksa,bayar_detail_periksa_lab_perujuk.jam,bayar_detail_periksa_lab_perujuk.id_template,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_detail_periksa_lab_perujuk on bayar_detail_periksa_lab_perujuk.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or template_laboratorium.Pemeriksaan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam");
                try {
                    psdetaillab2.setString(1, getTargetDokter());
                    psdetaillab2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psdetaillab2.setString(3, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(4, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(5, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(6, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsdetaillab = psdetaillab2.executeQuery();

                    while (rsdetaillab.next()) {
                        double jmTariff = hitungJMDokter(rsdetaillab.getString("kd_dokter"), rsdetaillab.getDouble("bagian_perujuk"));
                        tabMode.addRow(new Object[]{
                            false, rsdetaillab.getString("tgl_periksa"), rsdetaillab.getString("jam"), rsdetaillab.getString("no_rawat"), rsdetaillab.getString("no_rkm_medis"),
                            rsdetaillab.getString("nm_pasien") + " (" + rsdetaillab.getString("kd_pj") + ")", rsdetaillab.getString("kd_jenis_prw"), rsdetaillab.getString("Pemeriksaan"),
                            "Laborat " + rsdetaillab.getString("status") + " Perujuk Detail", currentBruto, currentSharing, jmTariff, rsdetaillab.getString("id_template"), Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsdetaillab.getString("kd_pj") + "'") + isCekCOB(rsdetaillab.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsdetaillab.getString("no_rawat") + "' LIMIT 1"), rsdetaillab.getString("kd_dokter"), getNamaDokter(rsdetaillab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Detail Perujuk : " + e);
                } finally {
                    if (rsdetaillab != null) {
                        rsdetaillab.close();
                    }
                    if (psdetaillab2 != null) {
                        psdetaillab2.close();
                    }
                }
            }

            if (chkRadiologi.isSelected() == true) {
                //periksa radiologi
                psperiksa_radiologi = koneksi.prepareStatement("select periksa_radiologi.tarif_tindakan_dokter,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_radiologi.status,"
                        + "jns_perawatan_radiologi.nm_perawatan,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,reg_periksa.kd_pj,periksa_radiologi.kd_dokter "
                        + " from periksa_radiologi inner join reg_periksa on periksa_radiologi.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_radiologi on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " where periksa_radiologi.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_radiologi.tarif_tindakan_dokter>0 and"
                        + " concat(periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.kd_dokter) not in "
                        + " (select concat(bayar_periksa_radiologi.no_rawat,bayar_periksa_radiologi.kd_jenis_prw,bayar_periksa_radiologi.tgl_periksa,bayar_periksa_radiologi.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_radiologi on bayar_periksa_radiologi.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_radiologi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_radiologi.tgl_periksa like ?)")
                        + "order by periksa_radiologi.tgl_periksa,periksa_radiologi.jam,jns_perawatan_radiologi.nm_perawatan  ");
                try {
                    psperiksa_radiologi.setString(1, getTargetDokter());
                    psperiksa_radiologi.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_radiologi.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_radiologi = psperiksa_radiologi.executeQuery();

                    while (rsperiksa_radiologi.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_radiologi.getString("kd_dokter"), rsperiksa_radiologi.getDouble("tarif_tindakan_dokter"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_radiologi.getString("tgl_periksa"), rsperiksa_radiologi.getString("jam"), rsperiksa_radiologi.getString("no_rawat"), rsperiksa_radiologi.getString("no_rkm_medis"),
                            rsperiksa_radiologi.getString("nm_pasien"), rsperiksa_radiologi.getString("kd_jenis_prw"), rsperiksa_radiologi.getString("nm_perawatan"),
                            "Radiologi " + rsperiksa_radiologi.getString("status") + " PJ", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_radiologi.getString("kd_pj") + "'") + isCekCOB(rsperiksa_radiologi.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsperiksa_radiologi.getString("no_rawat") + "' LIMIT 1"), rsperiksa_radiologi.getString("kd_dokter"), getNamaDokter(rsperiksa_radiologi.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Radiologi : " + e);
                } finally {
                    if (rsperiksa_radiologi != null) {
                        rsperiksa_radiologi.close();
                    }
                    if (psperiksa_radiologi != null) {
                        psperiksa_radiologi.close();
                    }
                }

                //periksa radiologi
                psperiksa_radiologi2 = koneksi.prepareStatement("select periksa_radiologi.tarif_perujuk,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_radiologi.status,"
                        + "jns_perawatan_radiologi.nm_perawatan,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,reg_periksa.kd_pj,periksa_radiologi.dokter_perujuk as kd_dokter "
                        + " from periksa_radiologi inner join reg_periksa on periksa_radiologi.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_radiologi on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " where periksa_radiologi.dokter_perujuk like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_radiologi.tarif_perujuk>0 and "
                        + " concat(periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.dokter_perujuk) not in "
                        + " (select concat(bayar_periksa_radiologi_perujuk.no_rawat,bayar_periksa_radiologi_perujuk.kd_jenis_prw,bayar_periksa_radiologi_perujuk.tgl_periksa,bayar_periksa_radiologi_perujuk.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_radiologi_perujuk on bayar_periksa_radiologi_perujuk.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_radiologi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_radiologi.tgl_periksa like ?)")
                        + "order by periksa_radiologi.tgl_periksa,periksa_radiologi.jam,jns_perawatan_radiologi.nm_perawatan  ");
                try {
                    psperiksa_radiologi2.setString(1, getTargetDokter());
                    psperiksa_radiologi2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_radiologi2.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_radiologi = psperiksa_radiologi2.executeQuery();

                    while (rsperiksa_radiologi.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_radiologi.getString("kd_dokter"), rsperiksa_radiologi.getDouble("tarif_perujuk"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_radiologi.getString("tgl_periksa"), rsperiksa_radiologi.getString("jam"), rsperiksa_radiologi.getString("no_rawat"), rsperiksa_radiologi.getString("no_rkm_medis"),
                            rsperiksa_radiologi.getString("nm_pasien"), rsperiksa_radiologi.getString("kd_jenis_prw"), rsperiksa_radiologi.getString("nm_perawatan"),
                            "Radiologi " + rsperiksa_radiologi.getString("status") + " Perujuk", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_radiologi.getString("kd_pj") + "'") + isCekCOB(rsperiksa_radiologi.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsperiksa_radiologi.getString("no_rawat") + "' LIMIT 1"), rsperiksa_radiologi.getString("kd_dokter"), getNamaDokter(rsperiksa_radiologi.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Perujuk Radiologi : " + e);
                } finally {
                    if (rsperiksa_radiologi != null) {
                        rsperiksa_radiologi.close();
                    }
                    if (psperiksa_radiologi2 != null) {
                        psperiksa_radiologi2.close();
                    }
                }
            }

            LCount.setText(Valid.SetAngka(total));
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void prosesCariPiutangBelumLunas() {
        jmMappingCache.clear();
        try {
            total = 0;
            if (chkRalan.isSelected() == true) {
                //rawat jalan
                psrawatjalandr = koneksi.prepareStatement("select pasien.nm_pasien,rawat_jl_dr.biaya_rawat,jns_perawatan.tarif_tindakandr as master_tarif_dr,"
                        + "jns_perawatan.nm_perawatan,rawat_jl_dr.tgl_perawatan,rawat_jl_dr.jam_rawat,reg_periksa.kd_pj,rawat_jl_dr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_jl_dr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_jl_dr on rawat_jl_dr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan on rawat_jl_dr.kd_jenis_prw=jns_perawatan.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and rawat_jl_dr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_jl_dr.tarif_tindakandr>0 and "
                        + "concat(rawat_jl_dr.no_rawat,rawat_jl_dr.kd_jenis_prw,rawat_jl_dr.tgl_perawatan,rawat_jl_dr.jam_rawat,rawat_jl_dr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_jl_dr.no_rawat,bayar_rawat_jl_dr.kd_jenis_prw,bayar_rawat_jl_dr.tgl_perawatan,bayar_rawat_jl_dr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_jl_dr on bayar_rawat_jl_dr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_jl_dr.tgl_perawatan like ?)")
                        + "order by reg_periksa.tgl_registrasi,jns_perawatan.nm_perawatan");
                psrawatjalandrpr = koneksi.prepareStatement("select pasien.nm_pasien,rawat_jl_drpr.biaya_rawat,jns_perawatan.tarif_tindakandr as master_tarif_dr,"
                        + "jns_perawatan.nm_perawatan,rawat_jl_drpr.tgl_perawatan,rawat_jl_drpr.jam_rawat,reg_periksa.kd_pj,rawat_jl_drpr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_jl_drpr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_jl_drpr on rawat_jl_drpr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan on rawat_jl_drpr.kd_jenis_prw=jns_perawatan.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and rawat_jl_drpr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_jl_drpr.tarif_tindakandr>0 and "
                        + "concat(rawat_jl_drpr.no_rawat,rawat_jl_drpr.kd_jenis_prw,rawat_jl_drpr.tgl_perawatan,rawat_jl_drpr.jam_rawat,rawat_jl_drpr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_jl_drpr.no_rawat,bayar_rawat_jl_drpr.kd_jenis_prw,bayar_rawat_jl_drpr.tgl_perawatan,bayar_rawat_jl_drpr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_jl_drpr on bayar_rawat_jl_drpr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_jl_drpr.tgl_perawatan like ?)")
                        + "order by reg_periksa.tgl_registrasi,jns_perawatan.nm_perawatan");
                try {
                    psrawatjalandr.setString(1, getTargetDokter());
                    psrawatjalandr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatjalandr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatjalandr = psrawatjalandr.executeQuery();

                    psrawatjalandrpr.setString(1, getTargetDokter());
                    psrawatjalandrpr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatjalandrpr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatjalandrpr = psrawatjalandrpr.executeQuery();

                    while (rsrawatjalandr.next()) {
                        String tglTrx = rsrawatjalandr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatjalandr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatjalandr.getString("kd_dokter"), tarifAsli, rsrawatjalandr.getString("no_rawat"), rsrawatjalandr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatjalandr.getString("jam_rawat"), rsrawatjalandr.getString("no_rawat"), rsrawatjalandr.getString("no_rkm_medis"),
                            rsrawatjalandr.getString("nm_pasien"), rsrawatjalandr.getString("kd_jenis_prw"), cekPotongan(rsrawatjalandr.getString("no_rawat"), rsrawatjalandr.getString("nm_perawatan")),
                            "Rawat Jalan Dr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatjalandr.getString("kd_pj") + "'") + isCekCOB(rsrawatjalandr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatjalandr.getString("no_rawat") + "' LIMIT 1"), rsrawatjalandr.getString("kd_dokter"), getNamaDokter(rsrawatjalandr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
                    }

                    while (rsrawatjalandrpr.next()) {
                        String tglTrx = rsrawatjalandrpr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatjalandrpr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatjalandrpr.getString("kd_dokter"), tarifAsli, rsrawatjalandrpr.getString("no_rawat"), rsrawatjalandrpr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatjalandrpr.getString("jam_rawat"), rsrawatjalandrpr.getString("no_rawat"), rsrawatjalandrpr.getString("no_rkm_medis"),
                            rsrawatjalandrpr.getString("nm_pasien"), rsrawatjalandrpr.getString("kd_jenis_prw"), cekPotongan(rsrawatjalandrpr.getString("no_rawat"), rsrawatjalandrpr.getString("nm_perawatan")),
                            "Rawat Jalan DrPr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatjalandrpr.getString("kd_pj") + "'") + isCekCOB(rsrawatjalandrpr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatjalandrpr.getString("no_rawat") + "' LIMIT 1"), rsrawatjalandrpr.getString("kd_dokter"), getNamaDokter(rsrawatjalandrpr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
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
            }

            if (chkRanap.isSelected() == true) {
                //rawat inap
                psrawatinapdr = koneksi.prepareStatement(
                        "select pasien.nm_pasien,jns_perawatan_inap.nm_perawatan,rawat_inap_dr.biaya_rawat,jns_perawatan_inap.tarif_tindakandr as master_tarif_dr,"
                        + "rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,reg_periksa.kd_pj,rawat_inap_dr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_inap_dr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_inap_dr on rawat_inap_dr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan_inap on rawat_inap_dr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and rawat_inap_dr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_inap_dr.tarif_tindakandr>0 and "
                        + "concat(rawat_inap_dr.no_rawat,rawat_inap_dr.kd_jenis_prw,rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,rawat_inap_dr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_inap_dr.no_rawat,bayar_rawat_inap_dr.kd_jenis_prw,bayar_rawat_inap_dr.tgl_perawatan,bayar_rawat_inap_dr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_inap_dr on bayar_rawat_inap_dr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_inap.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_inap_dr.tgl_perawatan like ?)")
                        + "order by rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,jns_perawatan_inap.nm_perawatan  ");
                psrawatinapdrpr = koneksi.prepareStatement(
                        "select pasien.nm_pasien,jns_perawatan_inap.nm_perawatan,rawat_inap_drpr.biaya_rawat,jns_perawatan_inap.tarif_tindakandr as master_tarif_dr,"
                        + "rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,reg_periksa.kd_pj,rawat_inap_drpr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_inap_drpr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_inap_drpr on rawat_inap_drpr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan_inap on rawat_inap_drpr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and rawat_inap_drpr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_inap_drpr.tarif_tindakandr>0 and "
                        + "concat(rawat_inap_drpr.no_rawat,rawat_inap_drpr.kd_jenis_prw,rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,rawat_inap_drpr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_inap_drpr.no_rawat,bayar_rawat_inap_drpr.kd_jenis_prw,bayar_rawat_inap_drpr.tgl_perawatan,bayar_rawat_inap_drpr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_inap_drpr on bayar_rawat_inap_drpr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_inap.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_inap_drpr.tgl_perawatan like ?)")
                        + "order by rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,jns_perawatan_inap.nm_perawatan  ");
                try {
                    psrawatinapdr.setString(1, getTargetDokter());
                    psrawatinapdr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatinapdr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatinapdr = psrawatinapdr.executeQuery();

                    psrawatinapdrpr.setString(1, getTargetDokter());
                    psrawatinapdrpr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatinapdrpr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatinapdrpr = psrawatinapdrpr.executeQuery();

                    while (rsrawatinapdr.next()) {
                        String tglTrx = rsrawatinapdr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatinapdr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatinapdr.getString("kd_dokter"), tarifAsli, rsrawatinapdr.getString("no_rawat"), rsrawatinapdr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatinapdr.getString("jam_rawat"), rsrawatinapdr.getString("no_rawat"), rsrawatinapdr.getString("no_rkm_medis"),
                            rsrawatinapdr.getString("nm_pasien"), rsrawatinapdr.getString("kd_jenis_prw"), cekPotongan(rsrawatinapdr.getString("no_rawat"), rsrawatinapdr.getString("nm_perawatan")),
                            "Rawat Inap Dr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatinapdr.getString("kd_pj") + "'") + isCekCOB(rsrawatinapdr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatinapdr.getString("no_rawat") + "' LIMIT 1"), rsrawatinapdr.getString("kd_dokter"), getNamaDokter(rsrawatinapdr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
                    }

                    while (rsrawatinapdrpr.next()) {
                        String tglTrx = rsrawatinapdrpr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatinapdrpr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatinapdrpr.getString("kd_dokter"), tarifAsli, rsrawatinapdrpr.getString("no_rawat"), rsrawatinapdrpr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatinapdrpr.getString("jam_rawat"), rsrawatinapdrpr.getString("no_rawat"), rsrawatinapdrpr.getString("no_rkm_medis"),
                            rsrawatinapdrpr.getString("nm_pasien"), rsrawatinapdrpr.getString("kd_jenis_prw"), cekPotongan(rsrawatinapdrpr.getString("no_rawat"), rsrawatinapdrpr.getString("nm_perawatan")),
                            "Rawat Inap DrPr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatinapdrpr.getString("kd_pj") + "'") + isCekCOB(rsrawatinapdrpr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatinapdrpr.getString("no_rawat") + "' LIMIT 1"), rsrawatinapdrpr.getString("kd_dokter"), getNamaDokter(rsrawatinapdrpr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
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
            }

            if (chkOperasi.isSelected() == true) {
                psbiayaoperator1 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator1,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.operator1 as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and operasi.operator1 like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayaoperator1>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.operator1) not in "
                        + "(select concat(bayar_operasi_operator1.no_rawat,bayar_operasi_operator1.kode_paket,bayar_operasi_operator1.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_operator1 on bayar_operasi_operator1.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayaoperator2 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator2,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.operator2 as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and operasi.operator2 like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayaoperator2>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.operator2) not in "
                        + "(select concat(bayar_operasi_operator2.no_rawat,bayar_operasi_operator2.kode_paket,bayar_operasi_operator2.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_operator2 on bayar_operasi_operator2.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayaoperator3 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator3,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.operator3 as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and operasi.operator3 like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayaoperator3>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.operator3) not in "
                        + "(select concat(bayar_operasi_operator3.no_rawat,bayar_operasi_operator3.kode_paket,bayar_operasi_operator3.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_operator3 on bayar_operasi_operator3.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayadokter_anak = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayadokter_anak,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_anak as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and operasi.dokter_anak like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayadokter_anak>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_anak) not in "
                        + "(select concat(bayar_operasi_dokter_anak.no_rawat,bayar_operasi_dokter_anak.kode_paket,bayar_operasi_dokter_anak.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_anak on bayar_operasi_dokter_anak.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiaya_dokter_umum = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biaya_dokter_umum,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_umum as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and operasi.dokter_umum like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biaya_dokter_umum>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_umum) not in "
                        + "(select concat(bayar_operasi_dokter_umum.no_rawat,bayar_operasi_dokter_umum.kode_paket,bayar_operasi_dokter_umum.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_umum on bayar_operasi_dokter_umum.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiaya_dokter_pjanak = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biaya_dokter_pjanak,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_pjanak as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and operasi.dokter_pjanak like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biaya_dokter_pjanak>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_pjanak) not in "
                        + "(select concat(bayar_operasi_dokter_pjanak.no_rawat,bayar_operasi_dokter_pjanak.kode_paket,bayar_operasi_dokter_pjanak.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_pjanak on bayar_operasi_dokter_pjanak.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayadokter_anestesi = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayadokter_anestesi,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_anestesi as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and operasi.dokter_anestesi like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayadokter_anestesi>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_anestesi) not in "
                        + "(select concat(bayar_operasi_dokter_anestesi.no_rawat,bayar_operasi_dokter_anestesi.kode_paket,bayar_operasi_dokter_anestesi.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_anestesi on bayar_operasi_dokter_anestesi.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                try {
                    psbiayaoperator1.setString(1, getTargetDokter());
                    psbiayaoperator1.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayaoperator1.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayaoperator1 = psbiayaoperator1.executeQuery();

                    psbiayaoperator2.setString(1, getTargetDokter());
                    psbiayaoperator2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayaoperator2.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayaoperator2 = psbiayaoperator2.executeQuery();

                    psbiayaoperator3.setString(1, getTargetDokter());
                    psbiayaoperator3.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayaoperator3.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayaoperator3 = psbiayaoperator3.executeQuery();

                    psbiayadokter_anak.setString(1, getTargetDokter());
                    psbiayadokter_anak.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayadokter_anak.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayadokter_anak = psbiayadokter_anak.executeQuery();

                    psbiaya_dokter_umum.setString(1, getTargetDokter());
                    psbiaya_dokter_umum.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiaya_dokter_umum.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiaya_dokter_umum = psbiaya_dokter_umum.executeQuery();

                    psbiaya_dokter_pjanak.setString(1, getTargetDokter());
                    psbiaya_dokter_pjanak.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiaya_dokter_pjanak.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiaya_dokter_pjanak = psbiaya_dokter_pjanak.executeQuery();

                    psbiayadokter_anestesi.setString(1, getTargetDokter());
                    psbiayadokter_anestesi.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayadokter_anestesi.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayadokter_anestesi = psbiayadokter_anestesi.executeQuery();

                    while (rsbiayaoperator1.next()) {
                        String tglOpOp1 = rsbiayaoperator1.getString("tgl_operasi");
                        double tarifMurniOp1 = rsbiayaoperator1.getDouble("biayaoperator1");
                        double[] calcOp1 = hitungJMOperasiPotongan(rsbiayaoperator1.getString("no_rawat"), rsbiayaoperator1.getString("nm_perawatan"), rsbiayaoperator1.getString("kd_dokter"), tarifMurniOp1, rsbiayaoperator1.getString("kode_paket"), tglOpOp1);
                        double brutoOp1 = calcOp1[0];
                        double sharingOp1 = calcOp1[1];
                        double jmOp1 = calcOp1[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpOp1), getJamOnly(tglOpOp1), rsbiayaoperator1.getString("no_rawat"), rsbiayaoperator1.getString("no_rkm_medis"),
                            rsbiayaoperator1.getString("nm_pasien"), rsbiayaoperator1.getString("kode_paket"), cekPotongan(rsbiayaoperator1.getString("no_rawat"), rsbiayaoperator1.getString("nm_perawatan"), rsbiayaoperator1.getString("kode_paket"), tglOpOp1, rsbiayaoperator1.getString("kd_dokter")) + "(Operator 1)",
                            "Operasi " + rsbiayaoperator1.getString("status") + " Op1", brutoOp1, sharingOp1, jmOp1, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayaoperator1.getString("kd_pj") + "'") + isCekCOB(rsbiayaoperator1.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayaoperator1.getString("no_rawat") + "' LIMIT 1"), rsbiayaoperator1.getString("kd_dokter"), getNamaDokter(rsbiayaoperator1.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmOp1;
                    }

                    while (rsbiayaoperator2.next()) {
                        String tglOpOp2 = rsbiayaoperator2.getString("tgl_operasi");
                        double tarifMurniOp2 = rsbiayaoperator2.getDouble("biayaoperator2");
                        double[] calcOp2 = hitungJMOperasiPotongan(rsbiayaoperator2.getString("no_rawat"), rsbiayaoperator2.getString("nm_perawatan"), rsbiayaoperator2.getString("kd_dokter"), tarifMurniOp2, rsbiayaoperator2.getString("kode_paket"), tglOpOp2);
                        double brutoOp2 = calcOp2[0];
                        double sharingOp2 = calcOp2[1];
                        double jmOp2 = calcOp2[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpOp2), getJamOnly(tglOpOp2), rsbiayaoperator2.getString("no_rawat"), rsbiayaoperator2.getString("no_rkm_medis"),
                            rsbiayaoperator2.getString("nm_pasien"), rsbiayaoperator2.getString("kode_paket"), cekPotongan(rsbiayaoperator2.getString("no_rawat"), rsbiayaoperator2.getString("nm_perawatan"), rsbiayaoperator2.getString("kode_paket"), tglOpOp2, rsbiayaoperator2.getString("kd_dokter")) + "(Operator 2)",
                            "Operasi " + rsbiayaoperator2.getString("status") + " Op2", brutoOp2, sharingOp2, jmOp2, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayaoperator2.getString("kd_pj") + "'") + isCekCOB(rsbiayaoperator2.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayaoperator2.getString("no_rawat") + "' LIMIT 1"), rsbiayaoperator2.getString("kd_dokter"), getNamaDokter(rsbiayaoperator2.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmOp2;
                    }

                    while (rsbiayaoperator3.next()) {
                        String tglOpOp3 = rsbiayaoperator3.getString("tgl_operasi");
                        double tarifMurniOp3 = rsbiayaoperator3.getDouble("biayaoperator3");
                        double[] calcOp3 = hitungJMOperasiPotongan(rsbiayaoperator3.getString("no_rawat"), rsbiayaoperator3.getString("nm_perawatan"), rsbiayaoperator3.getString("kd_dokter"), tarifMurniOp3, rsbiayaoperator3.getString("kode_paket"), tglOpOp3);
                        double brutoOp3 = calcOp3[0];
                        double sharingOp3 = calcOp3[1];
                        double jmOp3 = calcOp3[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpOp3), getJamOnly(tglOpOp3), rsbiayaoperator3.getString("no_rawat"), rsbiayaoperator3.getString("no_rkm_medis"),
                            rsbiayaoperator3.getString("nm_pasien"), rsbiayaoperator3.getString("kode_paket"), cekPotongan(rsbiayaoperator3.getString("no_rawat"), rsbiayaoperator3.getString("nm_perawatan"), rsbiayaoperator3.getString("kode_paket"), tglOpOp3, rsbiayaoperator3.getString("kd_dokter")) + "(Operator 3)",
                            "Operasi " + rsbiayaoperator3.getString("status") + " Op3", brutoOp3, sharingOp3, jmOp3, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayaoperator3.getString("kd_pj") + "'") + isCekCOB(rsbiayaoperator3.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayaoperator3.getString("no_rawat") + "' LIMIT 1"), rsbiayaoperator3.getString("kd_dokter"), getNamaDokter(rsbiayaoperator3.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmOp3;
                    }

                    while (rsbiayadokter_anak.next()) {
                        String tglOpDrAnak = rsbiayadokter_anak.getString("tgl_operasi");
                        double tarifMurniDrAnak = rsbiayadokter_anak.getDouble("biayadokter_anak");
                        double[] calcDrAnak = hitungJMOperasiPotongan(rsbiayadokter_anak.getString("no_rawat"), rsbiayadokter_anak.getString("nm_perawatan"), rsbiayadokter_anak.getString("kd_dokter"), tarifMurniDrAnak, rsbiayadokter_anak.getString("kode_paket"), tglOpDrAnak);
                        double brutoDrAnak = calcDrAnak[0];
                        double sharingDrAnak = calcDrAnak[1];
                        double jmDrAnak = calcDrAnak[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrAnak), getJamOnly(tglOpDrAnak), rsbiayadokter_anak.getString("no_rawat"), rsbiayadokter_anak.getString("no_rkm_medis"),
                            rsbiayadokter_anak.getString("nm_pasien"), rsbiayadokter_anak.getString("kode_paket"), cekPotongan(rsbiayadokter_anak.getString("no_rawat"), rsbiayadokter_anak.getString("nm_perawatan"), rsbiayadokter_anak.getString("kode_paket"), tglOpDrAnak, rsbiayadokter_anak.getString("kd_dokter")) + "(dr Anak)",
                            "Operasi " + rsbiayadokter_anak.getString("status") + " dr Anak", brutoDrAnak, sharingDrAnak, jmDrAnak, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayadokter_anak.getString("kd_pj") + "'") + isCekCOB(rsbiayadokter_anak.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayadokter_anak.getString("no_rawat") + "' LIMIT 1"), rsbiayadokter_anak.getString("kd_dokter"), getNamaDokter(rsbiayadokter_anak.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrAnak;
                    }

                    while (rsbiayadokter_anestesi.next()) {
                        String tglOpDrAnestesi = rsbiayadokter_anestesi.getString("tgl_operasi");
                        double tarifMurniDrAnestesi = rsbiayadokter_anestesi.getDouble("biayadokter_anestesi");
                        double[] calcDrAnestesi = hitungJMOperasiPotongan(rsbiayadokter_anestesi.getString("no_rawat"), rsbiayadokter_anestesi.getString("nm_perawatan"), rsbiayadokter_anestesi.getString("kd_dokter"), tarifMurniDrAnestesi, rsbiayadokter_anestesi.getString("kode_paket"), tglOpDrAnestesi);
                        double brutoDrAnestesi = calcDrAnestesi[0];
                        double sharingDrAnestesi = calcDrAnestesi[1];
                        double jmDrAnestesi = calcDrAnestesi[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrAnestesi), getJamOnly(tglOpDrAnestesi), rsbiayadokter_anestesi.getString("no_rawat"), rsbiayadokter_anestesi.getString("no_rkm_medis"),
                            rsbiayadokter_anestesi.getString("nm_pasien"), rsbiayadokter_anestesi.getString("kode_paket"), cekPotongan(rsbiayadokter_anestesi.getString("no_rawat"), rsbiayadokter_anestesi.getString("nm_perawatan"), rsbiayadokter_anestesi.getString("kode_paket"), tglOpDrAnestesi, rsbiayadokter_anestesi.getString("kd_dokter")) + "(dr Anestesi)",
                            "Operasi " + rsbiayadokter_anestesi.getString("status") + " dr Anastesi", brutoDrAnestesi, sharingDrAnestesi, jmDrAnestesi, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayadokter_anestesi.getString("kd_pj") + "'") + isCekCOB(rsbiayadokter_anestesi.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayadokter_anestesi.getString("no_rawat") + "' LIMIT 1"), rsbiayadokter_anestesi.getString("kd_dokter"), getNamaDokter(rsbiayadokter_anestesi.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrAnestesi;
                    }

                    while (rsbiaya_dokter_pjanak.next()) {
                        String tglOpDrPjAnak = rsbiaya_dokter_pjanak.getString("tgl_operasi");
                        double tarifMurniDrPjAnak = rsbiaya_dokter_pjanak.getDouble("biaya_dokter_pjanak");
                        double[] calcDrPjAnak = hitungJMOperasiPotongan(rsbiaya_dokter_pjanak.getString("no_rawat"), rsbiaya_dokter_pjanak.getString("nm_perawatan"), rsbiaya_dokter_pjanak.getString("kd_dokter"), tarifMurniDrPjAnak, rsbiaya_dokter_pjanak.getString("kode_paket"), tglOpDrPjAnak);
                        double brutoDrPjAnak = calcDrPjAnak[0];
                        double sharingDrPjAnak = calcDrPjAnak[1];
                        double jmDrPjAnak = calcDrPjAnak[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrPjAnak), getJamOnly(tglOpDrPjAnak), rsbiaya_dokter_pjanak.getString("no_rawat"), rsbiaya_dokter_pjanak.getString("no_rkm_medis"),
                            rsbiaya_dokter_pjanak.getString("nm_pasien"), rsbiaya_dokter_pjanak.getString("kode_paket"), cekPotongan(rsbiaya_dokter_pjanak.getString("no_rawat"), rsbiaya_dokter_pjanak.getString("nm_perawatan"), rsbiaya_dokter_pjanak.getString("kode_paket"), tglOpDrPjAnak, rsbiaya_dokter_pjanak.getString("kd_dokter")) + "(dr Pj Anak)",
                            "Operasi " + rsbiaya_dokter_pjanak.getString("status") + " dr PJ Anak", brutoDrPjAnak, sharingDrPjAnak, jmDrPjAnak, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiaya_dokter_pjanak.getString("kd_pj") + "'") + isCekCOB(rsbiaya_dokter_pjanak.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiaya_dokter_pjanak.getString("no_rawat") + "' LIMIT 1"), rsbiaya_dokter_pjanak.getString("kd_dokter"), getNamaDokter(rsbiaya_dokter_pjanak.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrPjAnak;
                    }

                    while (rsbiaya_dokter_umum.next()) {
                        String tglOpDrUmum = rsbiaya_dokter_umum.getString("tgl_operasi");
                        double tarifMurniDrUmum = rsbiaya_dokter_umum.getDouble("biaya_dokter_umum");
                        double[] calcDrUmum = hitungJMOperasiPotongan(rsbiaya_dokter_umum.getString("no_rawat"), rsbiaya_dokter_umum.getString("nm_perawatan"), rsbiaya_dokter_umum.getString("kd_dokter"), tarifMurniDrUmum, rsbiaya_dokter_umum.getString("kode_paket"), tglOpDrUmum);
                        double brutoDrUmum = calcDrUmum[0];
                        double sharingDrUmum = calcDrUmum[1];
                        double jmDrUmum = calcDrUmum[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrUmum), getJamOnly(tglOpDrUmum), rsbiaya_dokter_umum.getString("no_rawat"), rsbiaya_dokter_umum.getString("no_rkm_medis"),
                            rsbiaya_dokter_umum.getString("nm_pasien"), rsbiaya_dokter_umum.getString("kode_paket"), cekPotongan(rsbiaya_dokter_umum.getString("no_rawat"), rsbiaya_dokter_umum.getString("nm_perawatan"), rsbiaya_dokter_umum.getString("kode_paket"), tglOpDrUmum, rsbiaya_dokter_umum.getString("kd_dokter")) + "(dr Umum)",
                            "Operasi " + rsbiaya_dokter_umum.getString("status") + " dr Umum", brutoDrUmum, sharingDrUmum, jmDrUmum, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiaya_dokter_umum.getString("kd_pj") + "'") + isCekCOB(rsbiaya_dokter_umum.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiaya_dokter_umum.getString("no_rawat") + "' LIMIT 1"), rsbiaya_dokter_umum.getString("kd_dokter"), getNamaDokter(rsbiaya_dokter_umum.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrUmum;
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
                    if (rsbiayadokter_anestesi != null) {
                        rsbiayadokter_anestesi.close();
                    }
                    if (psbiayadokter_anestesi != null) {
                        psbiayadokter_anestesi.close();
                    }
                }
            }

            if (chkLaborat.isSelected() == true) {
                //periksa lab
                psperiksa_lab = koneksi.prepareStatement(
                        "select periksa_lab.tarif_tindakan_dokter,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_lab.status,"
                        + "jns_perawatan_lab.nm_perawatan,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,reg_periksa.kd_pj,periksa_lab.kd_dokter "
                        + " from periksa_lab inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + " where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and periksa_lab.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_lab.tarif_tindakan_dokter>0 and "
                        + " concat(periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.kd_dokter) not in "
                        + " (select concat(bayar_periksa_lab.no_rawat,bayar_periksa_lab.kd_jenis_prw,bayar_periksa_lab.tgl_periksa,bayar_periksa_lab.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_lab on bayar_periksa_lab.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or  jns_perawatan_lab.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam,jns_perawatan_lab.nm_perawatan  ");
                try {
                    psperiksa_lab.setString(1, getTargetDokter());
                    psperiksa_lab.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_lab.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_lab = psperiksa_lab.executeQuery();

                    while (rsperiksa_lab.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_lab.getString("kd_dokter"), rsperiksa_lab.getDouble("tarif_tindakan_dokter"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_lab.getString("tgl_periksa"), rsperiksa_lab.getString("jam"), rsperiksa_lab.getString("no_rawat"), rsperiksa_lab.getString("no_rkm_medis"),
                            rsperiksa_lab.getString("nm_pasien") + " (" + rsperiksa_lab.getString("kd_pj") + ")", rsperiksa_lab.getString("kd_jenis_prw"), rsperiksa_lab.getString("nm_perawatan"),
                            "Laborat " + rsperiksa_lab.getString("status") + " PJ", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_lab.getString("kd_pj") + "'") + isCekCOB(rsperiksa_lab.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsperiksa_lab.getString("no_rawat") + "' LIMIT 1"), rsperiksa_lab.getString("kd_dokter"), getNamaDokter(rsperiksa_lab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Lab : " + e);
                } finally {
                    if (rsperiksa_lab != null) {
                        rsperiksa_lab.close();
                    }
                    if (psperiksa_lab != null) {
                        psperiksa_lab.close();
                    }
                }

                //detail periksa lab
                psdetaillab = koneksi.prepareStatement(
                        "select detail_periksa_lab.bagian_dokter,pasien.nm_pasien,periksa_lab.status,detail_periksa_lab.id_template,"
                        + "template_laboratorium.Pemeriksaan,reg_periksa.kd_pj,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                        + "periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.kd_jenis_prw,periksa_lab.kd_dokter "
                        + "from detail_periksa_lab inner join periksa_lab on periksa_lab.no_rawat=detail_periksa_lab.no_rawat "
                        + "and periksa_lab.kd_jenis_prw=detail_periksa_lab.kd_jenis_prw "
                        + "and periksa_lab.tgl_periksa=detail_periksa_lab.tgl_periksa "
                        + "and periksa_lab.jam=detail_periksa_lab.jam "
                        + "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and periksa_lab.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and detail_periksa_lab.bagian_dokter>0 and "
                        + "concat(detail_periksa_lab.no_rawat,detail_periksa_lab.kd_jenis_prw,detail_periksa_lab.tgl_periksa,detail_periksa_lab.jam,detail_periksa_lab.id_template,periksa_lab.kd_dokter) not in "
                        + "(select concat(bayar_detail_periksa_lab.no_rawat,bayar_detail_periksa_lab.kd_jenis_prw,bayar_detail_periksa_lab.tgl_periksa,bayar_detail_periksa_lab.jam,bayar_detail_periksa_lab.id_template,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_detail_periksa_lab on bayar_detail_periksa_lab.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or template_laboratorium.Pemeriksaan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam");
                try {
                    psdetaillab.setString(1, getTargetDokter());
                    psdetaillab.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psdetaillab.setString(3, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(4, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(5, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(6, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsdetaillab = psdetaillab.executeQuery();

                    while (rsdetaillab.next()) {
                        double jmTariff = hitungJMDokter(rsdetaillab.getString("kd_dokter"), rsdetaillab.getDouble("bagian_dokter"));
                        tabMode.addRow(new Object[]{
                            false, rsdetaillab.getString("tgl_periksa"), rsdetaillab.getString("jam"), rsdetaillab.getString("no_rawat"), rsdetaillab.getString("no_rkm_medis"),
                            rsdetaillab.getString("nm_pasien") + " (" + rsdetaillab.getString("kd_pj") + ")", rsdetaillab.getString("kd_jenis_prw"), rsdetaillab.getString("Pemeriksaan"),
                            "Laborat " + rsdetaillab.getString("status") + " PJ Detail", currentBruto, currentSharing, jmTariff, rsdetaillab.getString("id_template"), Sequel.cariIsi("select png_jawab from penjab where kd_pj=\'" + rsdetaillab.getString("kd_pj") + "\'") + isCekCOB(rsdetaillab.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat=\'" + rsdetaillab.getString("no_rawat") + "\' LIMIT 1"), rsdetaillab.getString("kd_dokter"), getNamaDokter(rsdetaillab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Detail Lab : " + e);
                } finally {
                    if (rsdetaillab != null) {
                        rsdetaillab.close();
                    }
                    if (psdetaillab != null) {
                        psdetaillab.close();
                    }
                }

                //periksa lab perujuk
                psperiksa_lab2 = koneksi.prepareStatement(
                        "select periksa_lab.tarif_perujuk,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_lab.status,"
                        + "jns_perawatan_lab.nm_perawatan,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,reg_periksa.kd_pj,periksa_lab.dokter_perujuk as kd_dokter "
                        + " from periksa_lab inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + " where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and periksa_lab.dokter_perujuk like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_lab.tarif_perujuk>0 and "
                        + " concat(periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.dokter_perujuk) not in "
                        + " (select concat(bayar_periksa_lab_perujuk.no_rawat,bayar_periksa_lab_perujuk.kd_jenis_prw,bayar_periksa_lab_perujuk.tgl_periksa,bayar_periksa_lab_perujuk.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_lab_perujuk on bayar_periksa_lab_perujuk.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or  jns_perawatan_lab.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam,jns_perawatan_lab.nm_perawatan  ");
                try {
                    psperiksa_lab2.setString(1, getTargetDokter());
                    psperiksa_lab2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_lab2.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_lab = psperiksa_lab2.executeQuery();

                    while (rsperiksa_lab.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_lab.getString("kd_dokter"), rsperiksa_lab.getDouble("tarif_perujuk"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_lab.getString("tgl_periksa"), rsperiksa_lab.getString("jam"), rsperiksa_lab.getString("no_rawat"), rsperiksa_lab.getString("no_rkm_medis"),
                            rsperiksa_lab.getString("nm_pasien") + " (" + rsperiksa_lab.getString("kd_pj") + ")", rsperiksa_lab.getString("kd_jenis_prw"), rsperiksa_lab.getString("nm_perawatan"),
                            "Laborat " + rsperiksa_lab.getString("status") + " Perujuk", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_lab.getString("kd_pj") + "'") + isCekCOB(rsperiksa_lab.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsperiksa_lab.getString("no_rawat") + "' LIMIT 1"), rsperiksa_lab.getString("kd_dokter"), getNamaDokter(rsperiksa_lab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Perujuk Lab : " + e);
                } finally {
                    if (rsperiksa_lab != null) {
                        rsperiksa_lab.close();
                    }
                    if (psperiksa_lab2 != null) {
                        psperiksa_lab2.close();
                    }
                }

                psdetaillab2 = koneksi.prepareStatement(
                        "select detail_periksa_lab.bagian_perujuk,pasien.nm_pasien,periksa_lab.status,detail_periksa_lab.id_template,"
                        + "template_laboratorium.Pemeriksaan,reg_periksa.kd_pj,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                        + "periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.kd_jenis_prw,periksa_lab.dokter_perujuk as kd_dokter "
                        + "from detail_periksa_lab inner join periksa_lab on periksa_lab.no_rawat=detail_periksa_lab.no_rawat "
                        + "and periksa_lab.kd_jenis_prw=detail_periksa_lab.kd_jenis_prw "
                        + "and periksa_lab.tgl_periksa=detail_periksa_lab.tgl_periksa "
                        + "and periksa_lab.jam=detail_periksa_lab.jam "
                        + "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and periksa_lab.dokter_perujuk like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and detail_periksa_lab.bagian_perujuk>0 and "
                        + "concat(detail_periksa_lab.no_rawat,detail_periksa_lab.kd_jenis_prw,detail_periksa_lab.tgl_periksa,detail_periksa_lab.jam,detail_periksa_lab.id_template,periksa_lab.dokter_perujuk) not in "
                        + "(select concat(bayar_detail_periksa_lab_perujuk.no_rawat,bayar_detail_periksa_lab_perujuk.kd_jenis_prw,bayar_detail_periksa_lab_perujuk.tgl_periksa,bayar_detail_periksa_lab_perujuk.jam,bayar_detail_periksa_lab_perujuk.id_template,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_detail_periksa_lab_perujuk on bayar_detail_periksa_lab_perujuk.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or template_laboratorium.Pemeriksaan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam");
                try {
                    psdetaillab2.setString(1, getTargetDokter());
                    psdetaillab2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psdetaillab2.setString(3, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(4, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(5, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(6, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsdetaillab = psdetaillab2.executeQuery();

                    while (rsdetaillab.next()) {
                        double jmTariff = hitungJMDokter(rsdetaillab.getString("kd_dokter"), rsdetaillab.getDouble("bagian_perujuk"));
                        tabMode.addRow(new Object[]{
                            false, rsdetaillab.getString("tgl_periksa"), rsdetaillab.getString("jam"), rsdetaillab.getString("no_rawat"), rsdetaillab.getString("no_rkm_medis"),
                            rsdetaillab.getString("nm_pasien") + " (" + rsdetaillab.getString("kd_pj") + ")", rsdetaillab.getString("kd_jenis_prw"), rsdetaillab.getString("Pemeriksaan"),
                            "Laborat " + rsdetaillab.getString("status") + " Perujuk Detail", currentBruto, currentSharing, jmTariff, rsdetaillab.getString("id_template"), Sequel.cariIsi("select png_jawab from penjab where kd_pj=\'" + rsdetaillab.getString("kd_pj") + "\'") + isCekCOB(rsdetaillab.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat=\'" + rsdetaillab.getString("no_rawat") + "\' LIMIT 1"), rsdetaillab.getString("kd_dokter"), getNamaDokter(rsdetaillab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Detail Perujuk : " + e);
                } finally {
                    if (rsdetaillab != null) {
                        rsdetaillab.close();
                    }
                    if (psdetaillab2 != null) {
                        psdetaillab2.close();
                    }
                }
            }

            if (chkRadiologi.isSelected() == true) {
                //periksa radiologi
                psperiksa_radiologi = koneksi.prepareStatement("select periksa_radiologi.tarif_tindakan_dokter,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_radiologi.status,"
                        + "jns_perawatan_radiologi.nm_perawatan,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,reg_periksa.kd_pj,periksa_radiologi.kd_dokter "
                        + " from periksa_radiologi inner join reg_periksa on periksa_radiologi.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_radiologi on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + " where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and periksa_radiologi.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_radiologi.tarif_tindakan_dokter>0 and"
                        + " concat(periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.kd_dokter) not in "
                        + " (select concat(bayar_periksa_radiologi.no_rawat,bayar_periksa_radiologi.kd_jenis_prw,bayar_periksa_radiologi.tgl_periksa,bayar_periksa_radiologi.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_radiologi on bayar_periksa_radiologi.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_radiologi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_radiologi.tgl_periksa like ?)")
                        + "order by periksa_radiologi.tgl_periksa,periksa_radiologi.jam,jns_perawatan_radiologi.nm_perawatan  ");
                try {
                    psperiksa_radiologi.setString(1, getTargetDokter());
                    psperiksa_radiologi.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_radiologi.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_radiologi = psperiksa_radiologi.executeQuery();

                    while (rsperiksa_radiologi.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_radiologi.getString("kd_dokter"), rsperiksa_radiologi.getDouble("tarif_tindakan_dokter"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_radiologi.getString("tgl_periksa"), rsperiksa_radiologi.getString("jam"), rsperiksa_radiologi.getString("no_rawat"), rsperiksa_radiologi.getString("no_rkm_medis"),
                            rsperiksa_radiologi.getString("nm_pasien"), rsperiksa_radiologi.getString("kd_jenis_prw"), rsperiksa_radiologi.getString("nm_perawatan"),
                            "Radiologi " + rsperiksa_radiologi.getString("status") + " PJ", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_radiologi.getString("kd_pj") + "'") + isCekCOB(rsperiksa_radiologi.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsperiksa_radiologi.getString("no_rawat") + "' LIMIT 1"), rsperiksa_radiologi.getString("kd_dokter"), getNamaDokter(rsperiksa_radiologi.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Radiologi : " + e);
                } finally {
                    if (rsperiksa_radiologi != null) {
                        rsperiksa_radiologi.close();
                    }
                    if (psperiksa_radiologi != null) {
                        psperiksa_radiologi.close();
                    }
                }

                //periksa radiologi
                psperiksa_radiologi2 = koneksi.prepareStatement("select periksa_radiologi.tarif_perujuk,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_radiologi.status,"
                        + "jns_perawatan_radiologi.nm_perawatan,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,reg_periksa.kd_pj,periksa_radiologi.dokter_perujuk as kd_dokter "
                        + " from periksa_radiologi inner join reg_periksa on periksa_radiologi.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_radiologi on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + " where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and (piutang_pasien.status='Belum Lunas' OR piutang_pasien.status='Ditagihkan') and periksa_radiologi.dokter_perujuk like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_radiologi.tarif_perujuk>0 and "
                        + " concat(periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.dokter_perujuk) not in "
                        + " (select concat(bayar_periksa_radiologi_perujuk.no_rawat,bayar_periksa_radiologi_perujuk.kd_jenis_prw,bayar_periksa_radiologi_perujuk.tgl_periksa,bayar_periksa_radiologi_perujuk.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_radiologi_perujuk on bayar_periksa_radiologi_perujuk.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_radiologi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_radiologi.tgl_periksa like ?)")
                        + "order by periksa_radiologi.tgl_periksa,periksa_radiologi.jam,jns_perawatan_radiologi.nm_perawatan  ");
                try {
                    psperiksa_radiologi2.setString(1, getTargetDokter());
                    psperiksa_radiologi2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_radiologi2.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_radiologi = psperiksa_radiologi2.executeQuery();

                    while (rsperiksa_radiologi.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_radiologi.getString("kd_dokter"), rsperiksa_radiologi.getDouble("tarif_perujuk"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_radiologi.getString("tgl_periksa"), rsperiksa_radiologi.getString("jam"), rsperiksa_radiologi.getString("no_rawat"), rsperiksa_radiologi.getString("no_rkm_medis"),
                            rsperiksa_radiologi.getString("nm_pasien"), rsperiksa_radiologi.getString("kd_jenis_prw"), rsperiksa_radiologi.getString("nm_perawatan"),
                            "Radiologi " + rsperiksa_radiologi.getString("status") + " Perujuk", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_radiologi.getString("kd_pj") + "'") + isCekCOB(rsperiksa_radiologi.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsperiksa_radiologi.getString("no_rawat") + "' LIMIT 1"), rsperiksa_radiologi.getString("kd_dokter"), getNamaDokter(rsperiksa_radiologi.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Perujuk Radiologi : " + e);
                } finally {
                    if (rsperiksa_radiologi != null) {
                        rsperiksa_radiologi.close();
                    }
                    if (psperiksa_radiologi2 != null) {
                        psperiksa_radiologi2.close();
                    }
                }
            }

            LCount.setText(Valid.SetAngka(total));
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void prosesCariPiutangSudahLunas() {
        jmMappingCache.clear();
        try {
            total = 0;
            if (chkRalan.isSelected() == true) {
                //rawat jalan
                psrawatjalandr = koneksi.prepareStatement("select pasien.nm_pasien,rawat_jl_dr.biaya_rawat,jns_perawatan.tarif_tindakandr as master_tarif_dr,"
                        + "jns_perawatan.nm_perawatan,rawat_jl_dr.tgl_perawatan,rawat_jl_dr.jam_rawat,reg_periksa.kd_pj,rawat_jl_dr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_jl_dr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_jl_dr on rawat_jl_dr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan on rawat_jl_dr.kd_jenis_prw=jns_perawatan.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and rawat_jl_dr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_jl_dr.tarif_tindakandr>0 and "
                        + "concat(rawat_jl_dr.no_rawat,rawat_jl_dr.kd_jenis_prw,rawat_jl_dr.tgl_perawatan,rawat_jl_dr.jam_rawat,rawat_jl_dr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_jl_dr.no_rawat,bayar_rawat_jl_dr.kd_jenis_prw,bayar_rawat_jl_dr.tgl_perawatan,bayar_rawat_jl_dr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_jl_dr on bayar_rawat_jl_dr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_jl_dr.tgl_perawatan like ?)")
                        + "order by reg_periksa.tgl_registrasi,jns_perawatan.nm_perawatan");
                psrawatjalandrpr = koneksi.prepareStatement("select pasien.nm_pasien,rawat_jl_drpr.biaya_rawat,jns_perawatan.tarif_tindakandr as master_tarif_dr,"
                        + "jns_perawatan.nm_perawatan,rawat_jl_drpr.tgl_perawatan,rawat_jl_drpr.jam_rawat,reg_periksa.kd_pj,rawat_jl_drpr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_jl_drpr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_jl_drpr on rawat_jl_drpr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan on rawat_jl_drpr.kd_jenis_prw=jns_perawatan.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and rawat_jl_drpr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_jl_drpr.tarif_tindakandr>0 and "
                        + "concat(rawat_jl_drpr.no_rawat,rawat_jl_drpr.kd_jenis_prw,rawat_jl_drpr.tgl_perawatan,rawat_jl_drpr.jam_rawat,rawat_jl_drpr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_jl_drpr.no_rawat,bayar_rawat_jl_drpr.kd_jenis_prw,bayar_rawat_jl_drpr.tgl_perawatan,bayar_rawat_jl_drpr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_jl_drpr on bayar_rawat_jl_drpr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_jl_drpr.tgl_perawatan like ?)")
                        + "order by reg_periksa.tgl_registrasi,jns_perawatan.nm_perawatan");
                try {
                    psrawatjalandr.setString(1, getTargetDokter());
                    psrawatjalandr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatjalandr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatjalandr = psrawatjalandr.executeQuery();

                    psrawatjalandrpr.setString(1, getTargetDokter());
                    psrawatjalandrpr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatjalandrpr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatjalandrpr = psrawatjalandrpr.executeQuery();

                    while (rsrawatjalandr.next()) {
                        String tglTrx = rsrawatjalandr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatjalandr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatjalandr.getString("kd_dokter"), tarifAsli, rsrawatjalandr.getString("no_rawat"), rsrawatjalandr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatjalandr.getString("jam_rawat"), rsrawatjalandr.getString("no_rawat"), rsrawatjalandr.getString("no_rkm_medis"),
                            rsrawatjalandr.getString("nm_pasien"), rsrawatjalandr.getString("kd_jenis_prw"), cekPotongan(rsrawatjalandr.getString("no_rawat"), rsrawatjalandr.getString("nm_perawatan")),
                            "Rawat Jalan Dr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatjalandr.getString("kd_pj") + "'") + isCekCOB(rsrawatjalandr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatjalandr.getString("no_rawat") + "' LIMIT 1"), rsrawatjalandr.getString("kd_dokter"), getNamaDokter(rsrawatjalandr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
                    }

                    while (rsrawatjalandrpr.next()) {
                        String tglTrx = rsrawatjalandrpr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatjalandrpr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatjalandrpr.getString("kd_dokter"), tarifAsli, rsrawatjalandrpr.getString("no_rawat"), rsrawatjalandrpr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatjalandrpr.getString("jam_rawat"), rsrawatjalandrpr.getString("no_rawat"), rsrawatjalandrpr.getString("no_rkm_medis"),
                            rsrawatjalandrpr.getString("nm_pasien"), rsrawatjalandrpr.getString("kd_jenis_prw"), cekPotongan(rsrawatjalandrpr.getString("no_rawat"), rsrawatjalandrpr.getString("nm_perawatan")),
                            "Rawat Jalan DrPr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatjalandrpr.getString("kd_pj") + "'") + isCekCOB(rsrawatjalandrpr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatjalandrpr.getString("no_rawat") + "' LIMIT 1"), rsrawatjalandrpr.getString("kd_dokter"), getNamaDokter(rsrawatjalandrpr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
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
            }

            if (chkRanap.isSelected() == true) {
                //rawat inap
                psrawatinapdr = koneksi.prepareStatement(
                        "select pasien.nm_pasien,jns_perawatan_inap.nm_perawatan,rawat_inap_dr.biaya_rawat,jns_perawatan_inap.tarif_tindakandr as master_tarif_dr,"
                        + "rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,reg_periksa.kd_pj,rawat_inap_dr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_inap_dr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_inap_dr on rawat_inap_dr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan_inap on rawat_inap_dr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and rawat_inap_dr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_inap_dr.tarif_tindakandr>0 and "
                        + "concat(rawat_inap_dr.no_rawat,rawat_inap_dr.kd_jenis_prw,rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,rawat_inap_dr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_inap_dr.no_rawat,bayar_rawat_inap_dr.kd_jenis_prw,bayar_rawat_inap_dr.tgl_perawatan,bayar_rawat_inap_dr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_inap_dr on bayar_rawat_inap_dr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_inap.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_inap_dr.tgl_perawatan like ?)")
                        + "order by rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,jns_perawatan_inap.nm_perawatan  ");
                psrawatinapdrpr = koneksi.prepareStatement(
                        "select pasien.nm_pasien,jns_perawatan_inap.nm_perawatan,rawat_inap_drpr.biaya_rawat,jns_perawatan_inap.tarif_tindakandr as master_tarif_dr,"
                        + "rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,reg_periksa.kd_pj,rawat_inap_drpr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_inap_drpr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_inap_drpr on rawat_inap_drpr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan_inap on rawat_inap_drpr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and rawat_inap_drpr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_inap_drpr.tarif_tindakandr>0 and "
                        + "concat(rawat_inap_drpr.no_rawat,rawat_inap_drpr.kd_jenis_prw,rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,rawat_inap_drpr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_inap_drpr.no_rawat,bayar_rawat_inap_drpr.kd_jenis_prw,bayar_rawat_inap_drpr.tgl_perawatan,bayar_rawat_inap_drpr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_inap_drpr on bayar_rawat_inap_drpr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_inap.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_inap_drpr.tgl_perawatan like ?)")
                        + "order by rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,jns_perawatan_inap.nm_perawatan  ");
                try {
                    psrawatinapdr.setString(1, getTargetDokter());
                    psrawatinapdr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatinapdr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatinapdr = psrawatinapdr.executeQuery();

                    psrawatinapdrpr.setString(1, getTargetDokter());
                    psrawatinapdrpr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatinapdrpr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatinapdrpr = psrawatinapdrpr.executeQuery();

                    while (rsrawatinapdr.next()) {
                        String tglTrx = rsrawatinapdr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatinapdr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatinapdr.getString("kd_dokter"), tarifAsli, rsrawatinapdr.getString("no_rawat"), rsrawatinapdr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatinapdr.getString("jam_rawat"), rsrawatinapdr.getString("no_rawat"), rsrawatinapdr.getString("no_rkm_medis"),
                            rsrawatinapdr.getString("nm_pasien"), rsrawatinapdr.getString("kd_jenis_prw"), cekPotongan(rsrawatinapdr.getString("no_rawat"), rsrawatinapdr.getString("nm_perawatan")),
                            "Rawat Inap Dr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatinapdr.getString("kd_pj") + "'") + isCekCOB(rsrawatinapdr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatinapdr.getString("no_rawat") + "' LIMIT 1"), rsrawatinapdr.getString("kd_dokter"), getNamaDokter(rsrawatinapdr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
                    }

                    while (rsrawatinapdrpr.next()) {
                        String tglTrx = rsrawatinapdrpr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatinapdrpr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatinapdrpr.getString("kd_dokter"), tarifAsli, rsrawatinapdrpr.getString("no_rawat"), rsrawatinapdrpr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatinapdrpr.getString("jam_rawat"), rsrawatinapdrpr.getString("no_rawat"), rsrawatinapdrpr.getString("no_rkm_medis"),
                            rsrawatinapdrpr.getString("nm_pasien"), rsrawatinapdrpr.getString("kd_jenis_prw"), cekPotongan(rsrawatinapdrpr.getString("no_rawat"), rsrawatinapdrpr.getString("nm_perawatan")),
                            "Rawat Inap DrPr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatinapdrpr.getString("kd_pj") + "'") + isCekCOB(rsrawatinapdrpr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatinapdrpr.getString("no_rawat") + "' LIMIT 1"), rsrawatinapdrpr.getString("kd_dokter"), getNamaDokter(rsrawatinapdrpr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
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
            }

            if (chkOperasi.isSelected() == true) {
                psbiayaoperator1 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator1,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.operator1 as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and operasi.operator1 like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayaoperator1>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.operator1) not in "
                        + "(select concat(bayar_operasi_operator1.no_rawat,bayar_operasi_operator1.kode_paket,bayar_operasi_operator1.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_operator1 on bayar_operasi_operator1.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayaoperator2 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator2,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.operator2 as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and operasi.operator2 like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayaoperator2>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.operator2) not in "
                        + "(select concat(bayar_operasi_operator2.no_rawat,bayar_operasi_operator2.kode_paket,bayar_operasi_operator2.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_operator2 on bayar_operasi_operator2.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayaoperator3 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator3,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.operator3 as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and operasi.operator3 like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayaoperator3>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.operator3) not in "
                        + "(select concat(bayar_operasi_operator3.no_rawat,bayar_operasi_operator3.kode_paket,bayar_operasi_operator3.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_operator3 on bayar_operasi_operator3.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayadokter_anak = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayadokter_anak,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_anak as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and operasi.dokter_anak like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayadokter_anak>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_anak) not in "
                        + "(select concat(bayar_operasi_dokter_anak.no_rawat,bayar_operasi_dokter_anak.kode_paket,bayar_operasi_dokter_anak.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_anak on bayar_operasi_dokter_anak.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiaya_dokter_umum = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biaya_dokter_umum,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_umum as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and operasi.dokter_umum like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biaya_dokter_umum>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_umum) not in "
                        + "(select concat(bayar_operasi_dokter_umum.no_rawat,bayar_operasi_dokter_umum.kode_paket,bayar_operasi_dokter_umum.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_umum on bayar_operasi_dokter_umum.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiaya_dokter_pjanak = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biaya_dokter_pjanak,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_pjanak as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and operasi.dokter_pjanak like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biaya_dokter_pjanak>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_pjanak) not in "
                        + "(select concat(bayar_operasi_dokter_pjanak.no_rawat,bayar_operasi_dokter_pjanak.kode_paket,bayar_operasi_dokter_pjanak.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_pjanak on bayar_operasi_dokter_pjanak.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayadokter_anestesi = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayadokter_anestesi,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_anestesi as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and operasi.dokter_anestesi like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayadokter_anestesi>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_anestesi) not in "
                        + "(select concat(bayar_operasi_dokter_anestesi.no_rawat,bayar_operasi_dokter_anestesi.kode_paket,bayar_operasi_dokter_anestesi.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_anestesi on bayar_operasi_dokter_anestesi.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                try {
                    psbiayaoperator1.setString(1, getTargetDokter());
                    psbiayaoperator1.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayaoperator1.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayaoperator1 = psbiayaoperator1.executeQuery();

                    psbiayaoperator2.setString(1, getTargetDokter());
                    psbiayaoperator2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayaoperator2.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayaoperator2 = psbiayaoperator2.executeQuery();

                    psbiayaoperator3.setString(1, getTargetDokter());
                    psbiayaoperator3.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayaoperator3.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayaoperator3 = psbiayaoperator3.executeQuery();

                    psbiayadokter_anak.setString(1, getTargetDokter());
                    psbiayadokter_anak.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayadokter_anak.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayadokter_anak = psbiayadokter_anak.executeQuery();

                    psbiaya_dokter_umum.setString(1, getTargetDokter());
                    psbiaya_dokter_umum.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiaya_dokter_umum.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiaya_dokter_umum = psbiaya_dokter_umum.executeQuery();

                    psbiaya_dokter_pjanak.setString(1, getTargetDokter());
                    psbiaya_dokter_pjanak.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiaya_dokter_pjanak.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiaya_dokter_pjanak = psbiaya_dokter_pjanak.executeQuery();

                    psbiayadokter_anestesi.setString(1, getTargetDokter());
                    psbiayadokter_anestesi.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayadokter_anestesi.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayadokter_anestesi = psbiayadokter_anestesi.executeQuery();

                    while (rsbiayaoperator1.next()) {
                        String tglOpOp1 = rsbiayaoperator1.getString("tgl_operasi");
                        double tarifMurniOp1 = rsbiayaoperator1.getDouble("biayaoperator1");
                        double[] calcOp1 = hitungJMOperasiPotongan(rsbiayaoperator1.getString("no_rawat"), rsbiayaoperator1.getString("nm_perawatan"), rsbiayaoperator1.getString("kd_dokter"), tarifMurniOp1, rsbiayaoperator1.getString("kode_paket"), tglOpOp1);
                        double brutoOp1 = calcOp1[0];
                        double sharingOp1 = calcOp1[1];
                        double jmOp1 = calcOp1[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpOp1), getJamOnly(tglOpOp1), rsbiayaoperator1.getString("no_rawat"), rsbiayaoperator1.getString("no_rkm_medis"),
                            rsbiayaoperator1.getString("nm_pasien"), rsbiayaoperator1.getString("kode_paket"), cekPotongan(rsbiayaoperator1.getString("no_rawat"), rsbiayaoperator1.getString("nm_perawatan"), rsbiayaoperator1.getString("kode_paket"), tglOpOp1, rsbiayaoperator1.getString("kd_dokter")) + "(Operator 1)",
                            "Operasi " + rsbiayaoperator1.getString("status") + " Op1", brutoOp1, sharingOp1, jmOp1, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayaoperator1.getString("kd_pj") + "'") + isCekCOB(rsbiayaoperator1.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayaoperator1.getString("no_rawat") + "' LIMIT 1"), rsbiayaoperator1.getString("kd_dokter"), getNamaDokter(rsbiayaoperator1.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmOp1;
                    }

                    while (rsbiayaoperator2.next()) {
                        String tglOpOp2 = rsbiayaoperator2.getString("tgl_operasi");
                        double tarifMurniOp2 = rsbiayaoperator2.getDouble("biayaoperator2");
                        double[] calcOp2 = hitungJMOperasiPotongan(rsbiayaoperator2.getString("no_rawat"), rsbiayaoperator2.getString("nm_perawatan"), rsbiayaoperator2.getString("kd_dokter"), tarifMurniOp2, rsbiayaoperator2.getString("kode_paket"), tglOpOp2);
                        double brutoOp2 = calcOp2[0];
                        double sharingOp2 = calcOp2[1];
                        double jmOp2 = calcOp2[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpOp2), getJamOnly(tglOpOp2), rsbiayaoperator2.getString("no_rawat"), rsbiayaoperator2.getString("no_rkm_medis"),
                            rsbiayaoperator2.getString("nm_pasien"), rsbiayaoperator2.getString("kode_paket"), cekPotongan(rsbiayaoperator2.getString("no_rawat"), rsbiayaoperator2.getString("nm_perawatan"), rsbiayaoperator2.getString("kode_paket"), tglOpOp2, rsbiayaoperator2.getString("kd_dokter")) + "(Operator 2)",
                            "Operasi " + rsbiayaoperator2.getString("status") + " Op2", brutoOp2, sharingOp2, jmOp2, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayaoperator2.getString("kd_pj") + "'") + isCekCOB(rsbiayaoperator2.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayaoperator2.getString("no_rawat") + "' LIMIT 1"), rsbiayaoperator2.getString("kd_dokter"), getNamaDokter(rsbiayaoperator2.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmOp2;
                    }

                    while (rsbiayaoperator3.next()) {
                        String tglOpOp3 = rsbiayaoperator3.getString("tgl_operasi");
                        double tarifMurniOp3 = rsbiayaoperator3.getDouble("biayaoperator3");
                        double[] calcOp3 = hitungJMOperasiPotongan(rsbiayaoperator3.getString("no_rawat"), rsbiayaoperator3.getString("nm_perawatan"), rsbiayaoperator3.getString("kd_dokter"), tarifMurniOp3, rsbiayaoperator3.getString("kode_paket"), tglOpOp3);
                        double brutoOp3 = calcOp3[0];
                        double sharingOp3 = calcOp3[1];
                        double jmOp3 = calcOp3[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpOp3), getJamOnly(tglOpOp3), rsbiayaoperator3.getString("no_rawat"), rsbiayaoperator3.getString("no_rkm_medis"),
                            rsbiayaoperator3.getString("nm_pasien"), rsbiayaoperator3.getString("kode_paket"), cekPotongan(rsbiayaoperator3.getString("no_rawat"), rsbiayaoperator3.getString("nm_perawatan"), rsbiayaoperator3.getString("kode_paket"), tglOpOp3, rsbiayaoperator3.getString("kd_dokter")) + "(Operator 3)",
                            "Operasi " + rsbiayaoperator3.getString("status") + " Op3", brutoOp3, sharingOp3, jmOp3, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayaoperator3.getString("kd_pj") + "'") + isCekCOB(rsbiayaoperator3.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayaoperator3.getString("no_rawat") + "' LIMIT 1"), rsbiayaoperator3.getString("kd_dokter"), getNamaDokter(rsbiayaoperator3.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmOp3;
                    }

                    while (rsbiayadokter_anak.next()) {
                        String tglOpDrAnak = rsbiayadokter_anak.getString("tgl_operasi");
                        double tarifMurniDrAnak = rsbiayadokter_anak.getDouble("biayadokter_anak");
                        double[] calcDrAnak = hitungJMOperasiPotongan(rsbiayadokter_anak.getString("no_rawat"), rsbiayadokter_anak.getString("nm_perawatan"), rsbiayadokter_anak.getString("kd_dokter"), tarifMurniDrAnak, rsbiayadokter_anak.getString("kode_paket"), tglOpDrAnak);
                        double brutoDrAnak = calcDrAnak[0];
                        double sharingDrAnak = calcDrAnak[1];
                        double jmDrAnak = calcDrAnak[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrAnak), getJamOnly(tglOpDrAnak), rsbiayadokter_anak.getString("no_rawat"), rsbiayadokter_anak.getString("no_rkm_medis"),
                            rsbiayadokter_anak.getString("nm_pasien"), rsbiayadokter_anak.getString("kode_paket"), cekPotongan(rsbiayadokter_anak.getString("no_rawat"), rsbiayadokter_anak.getString("nm_perawatan"), rsbiayadokter_anak.getString("kode_paket"), tglOpDrAnak, rsbiayadokter_anak.getString("kd_dokter")) + "(dr Anak)",
                            "Operasi " + rsbiayadokter_anak.getString("status") + " dr Anak", brutoDrAnak, sharingDrAnak, jmDrAnak, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayadokter_anak.getString("kd_pj") + "'") + isCekCOB(rsbiayadokter_anak.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayadokter_anak.getString("no_rawat") + "' LIMIT 1"), rsbiayadokter_anak.getString("kd_dokter"), getNamaDokter(rsbiayadokter_anak.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrAnak;
                    }

                    while (rsbiayadokter_anestesi.next()) {
                        String tglOpDrAnestesi = rsbiayadokter_anestesi.getString("tgl_operasi");
                        double tarifMurniDrAnestesi = rsbiayadokter_anestesi.getDouble("biayadokter_anestesi");
                        double[] calcDrAnestesi = hitungJMOperasiPotongan(rsbiayadokter_anestesi.getString("no_rawat"), rsbiayadokter_anestesi.getString("nm_perawatan"), rsbiayadokter_anestesi.getString("kd_dokter"), tarifMurniDrAnestesi, rsbiayadokter_anestesi.getString("kode_paket"), tglOpDrAnestesi);
                        double brutoDrAnestesi = calcDrAnestesi[0];
                        double sharingDrAnestesi = calcDrAnestesi[1];
                        double jmDrAnestesi = calcDrAnestesi[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrAnestesi), getJamOnly(tglOpDrAnestesi), rsbiayadokter_anestesi.getString("no_rawat"), rsbiayadokter_anestesi.getString("no_rkm_medis"),
                            rsbiayadokter_anestesi.getString("nm_pasien"), rsbiayadokter_anestesi.getString("kode_paket"), cekPotongan(rsbiayadokter_anestesi.getString("no_rawat"), rsbiayadokter_anestesi.getString("nm_perawatan"), rsbiayadokter_anestesi.getString("kode_paket"), tglOpDrAnestesi, rsbiayadokter_anestesi.getString("kd_dokter")) + "(dr Anestesi)",
                            "Operasi " + rsbiayadokter_anestesi.getString("status") + " dr Anastesi", brutoDrAnestesi, sharingDrAnestesi, jmDrAnestesi, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayadokter_anestesi.getString("kd_pj") + "'") + isCekCOB(rsbiayadokter_anestesi.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayadokter_anestesi.getString("no_rawat") + "' LIMIT 1"), rsbiayadokter_anestesi.getString("kd_dokter"), getNamaDokter(rsbiayadokter_anestesi.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrAnestesi;
                    }

                    while (rsbiaya_dokter_pjanak.next()) {
                        String tglOpDrPjAnak = rsbiaya_dokter_pjanak.getString("tgl_operasi");
                        double tarifMurniDrPjAnak = rsbiaya_dokter_pjanak.getDouble("biaya_dokter_pjanak");
                        double[] calcDrPjAnak = hitungJMOperasiPotongan(rsbiaya_dokter_pjanak.getString("no_rawat"), rsbiaya_dokter_pjanak.getString("nm_perawatan"), rsbiaya_dokter_pjanak.getString("kd_dokter"), tarifMurniDrPjAnak, rsbiaya_dokter_pjanak.getString("kode_paket"), tglOpDrPjAnak);
                        double brutoDrPjAnak = calcDrPjAnak[0];
                        double sharingDrPjAnak = calcDrPjAnak[1];
                        double jmDrPjAnak = calcDrPjAnak[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrPjAnak), getJamOnly(tglOpDrPjAnak), rsbiaya_dokter_pjanak.getString("no_rawat"), rsbiaya_dokter_pjanak.getString("no_rkm_medis"),
                            rsbiaya_dokter_pjanak.getString("nm_pasien"), rsbiaya_dokter_pjanak.getString("kode_paket"), cekPotongan(rsbiaya_dokter_pjanak.getString("no_rawat"), rsbiaya_dokter_pjanak.getString("nm_perawatan"), rsbiaya_dokter_pjanak.getString("kode_paket"), tglOpDrPjAnak, rsbiaya_dokter_pjanak.getString("kd_dokter")) + "(dr Pj Anak)",
                            "Operasi " + rsbiaya_dokter_pjanak.getString("status") + " dr PJ Anak", brutoDrPjAnak, sharingDrPjAnak, jmDrPjAnak, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiaya_dokter_pjanak.getString("kd_pj") + "'") + isCekCOB(rsbiaya_dokter_pjanak.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiaya_dokter_pjanak.getString("no_rawat") + "' LIMIT 1"), rsbiaya_dokter_pjanak.getString("kd_dokter"), getNamaDokter(rsbiaya_dokter_pjanak.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrPjAnak;
                    }

                    while (rsbiaya_dokter_umum.next()) {
                        String tglOpDrUmum = rsbiaya_dokter_umum.getString("tgl_operasi");
                        double tarifMurniDrUmum = rsbiaya_dokter_umum.getDouble("biaya_dokter_umum");
                        double[] calcDrUmum = hitungJMOperasiPotongan(rsbiaya_dokter_umum.getString("no_rawat"), rsbiaya_dokter_umum.getString("nm_perawatan"), rsbiaya_dokter_umum.getString("kd_dokter"), tarifMurniDrUmum, rsbiaya_dokter_umum.getString("kode_paket"), tglOpDrUmum);
                        double brutoDrUmum = calcDrUmum[0];
                        double sharingDrUmum = calcDrUmum[1];
                        double jmDrUmum = calcDrUmum[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrUmum), getJamOnly(tglOpDrUmum), rsbiaya_dokter_umum.getString("no_rawat"), rsbiaya_dokter_umum.getString("no_rkm_medis"),
                            rsbiaya_dokter_umum.getString("nm_pasien"), rsbiaya_dokter_umum.getString("kode_paket"), cekPotongan(rsbiaya_dokter_umum.getString("no_rawat"), rsbiaya_dokter_umum.getString("nm_perawatan"), rsbiaya_dokter_umum.getString("kode_paket"), tglOpDrUmum, rsbiaya_dokter_umum.getString("kd_dokter")) + "(dr Umum)",
                            "Operasi " + rsbiaya_dokter_umum.getString("status") + " dr Umum", brutoDrUmum, sharingDrUmum, jmDrUmum, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiaya_dokter_umum.getString("kd_pj") + "'") + isCekCOB(rsbiaya_dokter_umum.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiaya_dokter_umum.getString("no_rawat") + "' LIMIT 1"), rsbiaya_dokter_umum.getString("kd_dokter"), getNamaDokter(rsbiaya_dokter_umum.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrUmum;
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
                    if (rsbiayadokter_anestesi != null) {
                        rsbiayadokter_anestesi.close();
                    }
                    if (psbiayadokter_anestesi != null) {
                        psbiayadokter_anestesi.close();
                    }
                }
            }

            if (chkLaborat.isSelected() == true) {
                //periksa lab
                psperiksa_lab = koneksi.prepareStatement(
                        "select periksa_lab.tarif_tindakan_dokter,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_lab.status,"
                        + "jns_perawatan_lab.nm_perawatan,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,reg_periksa.kd_pj,periksa_lab.kd_dokter "
                        + " from periksa_lab inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + " where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and periksa_lab.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_lab.tarif_tindakan_dokter>0 and "
                        + " concat(periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.kd_dokter) not in "
                        + " (select concat(bayar_periksa_lab.no_rawat,bayar_periksa_lab.kd_jenis_prw,bayar_periksa_lab.tgl_periksa,bayar_periksa_lab.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_lab on bayar_periksa_lab.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or  jns_perawatan_lab.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam,jns_perawatan_lab.nm_perawatan  ");
                try {
                    psperiksa_lab.setString(1, getTargetDokter());
                    psperiksa_lab.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_lab.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_lab = psperiksa_lab.executeQuery();

                    while (rsperiksa_lab.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_lab.getString("kd_dokter"), rsperiksa_lab.getDouble("tarif_tindakan_dokter"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_lab.getString("tgl_periksa"), rsperiksa_lab.getString("jam"), rsperiksa_lab.getString("no_rawat"), rsperiksa_lab.getString("no_rkm_medis"),
                            rsperiksa_lab.getString("nm_pasien") + " (" + rsperiksa_lab.getString("kd_pj") + ")", rsperiksa_lab.getString("kd_jenis_prw"), rsperiksa_lab.getString("nm_perawatan"),
                            "Laborat " + rsperiksa_lab.getString("status") + " PJ", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_lab.getString("kd_pj") + "'") + isCekCOB(rsperiksa_lab.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_bayar from bayar_piutang where no_rawat='" + rsperiksa_lab.getString("no_rawat") + "'"), rsperiksa_lab.getString("kd_dokter"), getNamaDokter(rsperiksa_lab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Lab : " + e);
                } finally {
                    if (rsperiksa_lab != null) {
                        rsperiksa_lab.close();
                    }
                    if (psperiksa_lab != null) {
                        psperiksa_lab.close();
                    }
                }

                //detail periksa lab
                psdetaillab = koneksi.prepareStatement(
                        "select detail_periksa_lab.bagian_dokter,pasien.nm_pasien,periksa_lab.status,detail_periksa_lab.id_template,"
                        + "template_laboratorium.Pemeriksaan,reg_periksa.kd_pj,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                        + "periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.kd_jenis_prw,periksa_lab.kd_dokter "
                        + "from detail_periksa_lab inner join periksa_lab on periksa_lab.no_rawat=detail_periksa_lab.no_rawat "
                        + "and periksa_lab.kd_jenis_prw=detail_periksa_lab.kd_jenis_prw "
                        + "and periksa_lab.tgl_periksa=detail_periksa_lab.tgl_periksa "
                        + "and periksa_lab.jam=detail_periksa_lab.jam "
                        + "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and periksa_lab.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and detail_periksa_lab.bagian_dokter>0 and "
                        + "concat(detail_periksa_lab.no_rawat,detail_periksa_lab.kd_jenis_prw,detail_periksa_lab.tgl_periksa,detail_periksa_lab.jam,detail_periksa_lab.id_template,periksa_lab.kd_dokter) not in "
                        + "(select concat(bayar_detail_periksa_lab.no_rawat,bayar_detail_periksa_lab.kd_jenis_prw,bayar_detail_periksa_lab.tgl_periksa,bayar_detail_periksa_lab.jam,bayar_detail_periksa_lab.id_template,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_detail_periksa_lab on bayar_detail_periksa_lab.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or template_laboratorium.Pemeriksaan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam");
                try {
                    psdetaillab.setString(1, getTargetDokter());
                    psdetaillab.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psdetaillab.setString(3, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(4, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(5, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(6, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsdetaillab = psdetaillab.executeQuery();

                    while (rsdetaillab.next()) {
                        double jmTariff = hitungJMDokter(rsdetaillab.getString("kd_dokter"), rsdetaillab.getDouble("bagian_dokter"));
                        tabMode.addRow(new Object[]{
                            false, rsdetaillab.getString("tgl_periksa"), rsdetaillab.getString("jam"), rsdetaillab.getString("no_rawat"), rsdetaillab.getString("no_rkm_medis"),
                            rsdetaillab.getString("nm_pasien") + " (" + rsdetaillab.getString("kd_pj") + ")", rsdetaillab.getString("kd_jenis_prw"), rsdetaillab.getString("Pemeriksaan"),
                            "Laborat " + rsdetaillab.getString("status") + " PJ Detail", currentBruto, currentSharing, jmTariff, rsdetaillab.getString("id_template"), Sequel.cariIsi("select png_jawab from penjab where kd_pj=\'" + rsdetaillab.getString("kd_pj") + "\'") + isCekCOB(rsdetaillab.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat=\'" + rsdetaillab.getString("no_rawat") + "\' LIMIT 1"), rsdetaillab.getString("kd_dokter"), getNamaDokter(rsdetaillab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Detail Lab : " + e);
                } finally {
                    if (rsdetaillab != null) {
                        rsdetaillab.close();
                    }
                    if (psdetaillab != null) {
                        psdetaillab.close();
                    }
                }

                //periksa lab perujuk
                psperiksa_lab2 = koneksi.prepareStatement(
                        "select periksa_lab.tarif_perujuk,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_lab.status,"
                        + "jns_perawatan_lab.nm_perawatan,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,reg_periksa.kd_pj,periksa_lab.dokter_perujuk as kd_dokter "
                        + " from periksa_lab inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + " where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and periksa_lab.dokter_perujuk like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_lab.tarif_perujuk>0 and "
                        + " concat(periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.dokter_perujuk) not in "
                        + " (select concat(bayar_periksa_lab_perujuk.no_rawat,bayar_periksa_lab_perujuk.kd_jenis_prw,bayar_periksa_lab_perujuk.tgl_periksa,bayar_periksa_lab_perujuk.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_lab_perujuk on bayar_periksa_lab_perujuk.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or  jns_perawatan_lab.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam,jns_perawatan_lab.nm_perawatan  ");
                try {
                    psperiksa_lab2.setString(1, getTargetDokter());
                    psperiksa_lab2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_lab2.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_lab = psperiksa_lab2.executeQuery();

                    while (rsperiksa_lab.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_lab.getString("kd_dokter"), rsperiksa_lab.getDouble("tarif_perujuk"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_lab.getString("tgl_periksa"), rsperiksa_lab.getString("jam"), rsperiksa_lab.getString("no_rawat"), rsperiksa_lab.getString("no_rkm_medis"),
                            rsperiksa_lab.getString("nm_pasien") + " (" + rsperiksa_lab.getString("kd_pj") + ")", rsperiksa_lab.getString("kd_jenis_prw"), rsperiksa_lab.getString("nm_perawatan"),
                            "Laborat " + rsperiksa_lab.getString("status") + " Perujuk", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_lab.getString("kd_pj") + "'") + isCekCOB(rsperiksa_lab.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_bayar from bayar_piutang where no_rawat='" + rsperiksa_lab.getString("no_rawat") + "'"), rsperiksa_lab.getString("kd_dokter"), getNamaDokter(rsperiksa_lab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Perujuk Lab : " + e);
                } finally {
                    if (rsperiksa_lab != null) {
                        rsperiksa_lab.close();
                    }
                    if (psperiksa_lab2 != null) {
                        psperiksa_lab2.close();
                    }
                }

                psdetaillab2 = koneksi.prepareStatement(
                        "select detail_periksa_lab.bagian_perujuk,pasien.nm_pasien,periksa_lab.status,detail_periksa_lab.id_template,"
                        + "template_laboratorium.Pemeriksaan,reg_periksa.kd_pj,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                        + "periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.kd_jenis_prw,periksa_lab.dokter_perujuk as kd_dokter "
                        + "from detail_periksa_lab inner join periksa_lab on periksa_lab.no_rawat=detail_periksa_lab.no_rawat "
                        + "and periksa_lab.kd_jenis_prw=detail_periksa_lab.kd_jenis_prw "
                        + "and periksa_lab.tgl_periksa=detail_periksa_lab.tgl_periksa "
                        + "and periksa_lab.jam=detail_periksa_lab.jam "
                        + "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and periksa_lab.dokter_perujuk like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and detail_periksa_lab.bagian_perujuk>0 and "
                        + "concat(detail_periksa_lab.no_rawat,detail_periksa_lab.kd_jenis_prw,detail_periksa_lab.tgl_periksa,detail_periksa_lab.jam,detail_periksa_lab.id_template,periksa_lab.dokter_perujuk) not in "
                        + "(select concat(bayar_detail_periksa_lab_perujuk.no_rawat,bayar_detail_periksa_lab_perujuk.kd_jenis_prw,bayar_detail_periksa_lab_perujuk.tgl_periksa,bayar_detail_periksa_lab_perujuk.jam,bayar_detail_periksa_lab_perujuk.id_template,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_detail_periksa_lab_perujuk on bayar_detail_periksa_lab_perujuk.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or template_laboratorium.Pemeriksaan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam");
                try {
                    psdetaillab2.setString(1, getTargetDokter());
                    psdetaillab2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psdetaillab2.setString(3, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(4, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(5, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(6, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsdetaillab = psdetaillab2.executeQuery();

                    while (rsdetaillab.next()) {
                        double jmTariff = hitungJMDokter(rsdetaillab.getString("kd_dokter"), rsdetaillab.getDouble("bagian_perujuk"));
                        tabMode.addRow(new Object[]{
                            false, rsdetaillab.getString("tgl_periksa"), rsdetaillab.getString("jam"), rsdetaillab.getString("no_rawat"), rsdetaillab.getString("no_rkm_medis"),
                            rsdetaillab.getString("nm_pasien") + " (" + rsdetaillab.getString("kd_pj") + ")", rsdetaillab.getString("kd_jenis_prw"), rsdetaillab.getString("Pemeriksaan"),
                            "Laborat " + rsdetaillab.getString("status") + " Perujuk Detail", currentBruto, currentSharing, jmTariff, rsdetaillab.getString("id_template"), Sequel.cariIsi("select png_jawab from penjab where kd_pj=\'" + rsdetaillab.getString("kd_pj") + "\'") + isCekCOB(rsdetaillab.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat=\'" + rsdetaillab.getString("no_rawat") + "\' LIMIT 1"), rsdetaillab.getString("kd_dokter"), getNamaDokter(rsdetaillab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Detail Perujuk : " + e);
                } finally {
                    if (rsdetaillab != null) {
                        rsdetaillab.close();
                    }
                    if (psdetaillab2 != null) {
                        psdetaillab2.close();
                    }
                }
            }

            if (chkRadiologi.isSelected() == true) {
                //periksa radiologi
                psperiksa_radiologi = koneksi.prepareStatement("select periksa_radiologi.tarif_tindakan_dokter,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_radiologi.status,"
                        + "jns_perawatan_radiologi.nm_perawatan,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,reg_periksa.kd_pj,periksa_radiologi.kd_dokter "
                        + " from periksa_radiologi inner join reg_periksa on periksa_radiologi.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_radiologi on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + " where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and periksa_radiologi.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_radiologi.tarif_tindakan_dokter>0 and"
                        + " concat(periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.kd_dokter) not in "
                        + " (select concat(bayar_periksa_radiologi.no_rawat,bayar_periksa_radiologi.kd_jenis_prw,bayar_periksa_radiologi.tgl_periksa,bayar_periksa_radiologi.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_radiologi on bayar_periksa_radiologi.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_radiologi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_radiologi.tgl_periksa like ?)")
                        + "order by periksa_radiologi.tgl_periksa,periksa_radiologi.jam,jns_perawatan_radiologi.nm_perawatan  ");

                //System.out.println(psperiksa_radiologi.toString());
                try {
                    psperiksa_radiologi.setString(1, getTargetDokter());
                    psperiksa_radiologi.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_radiologi.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(7, "%" + TCari.getText().trim() + "%");
                    }

                    // System.out.println(psperiksa_radiologi.toString());
                    rsperiksa_radiologi = psperiksa_radiologi.executeQuery();

                    while (rsperiksa_radiologi.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_radiologi.getString("kd_dokter"), rsperiksa_radiologi.getDouble("tarif_tindakan_dokter"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_radiologi.getString("tgl_periksa"), rsperiksa_radiologi.getString("jam"), rsperiksa_radiologi.getString("no_rawat"), rsperiksa_radiologi.getString("no_rkm_medis"),
                            rsperiksa_radiologi.getString("nm_pasien"), rsperiksa_radiologi.getString("kd_jenis_prw"), rsperiksa_radiologi.getString("nm_perawatan"),
                            "Radiologi " + rsperiksa_radiologi.getString("status") + " PJ", currentBruto, currentSharing, jmTariff, "",
                            Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_radiologi.getString("kd_pj") + "'") + isCekCOB(rsperiksa_radiologi.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_bayar from bayar_piutang where no_rawat='" + rsperiksa_radiologi.getString("no_rawat") + "'"), rsperiksa_radiologi.getString("kd_dokter"), getNamaDokter(rsperiksa_radiologi.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Radiologi : " + e);
                } finally {
                    if (rsperiksa_radiologi != null) {
                        rsperiksa_radiologi.close();
                    }
                    if (psperiksa_radiologi != null) {
                        psperiksa_radiologi.close();
                    }
                }

                //periksa radiologi
                psperiksa_radiologi2 = koneksi.prepareStatement("select periksa_radiologi.tarif_perujuk,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_radiologi.status,"
                        + "jns_perawatan_radiologi.nm_perawatan,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,reg_periksa.kd_pj,periksa_radiologi.dokter_perujuk as kd_dokter "
                        + " from periksa_radiologi inner join reg_periksa on periksa_radiologi.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_radiologi on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " inner join piutang_pasien on reg_periksa.no_rawat=piutang_pasien.no_rawat "
                        + " where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and piutang_pasien.status='Lunas' and periksa_radiologi.dokter_perujuk like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_radiologi.tarif_perujuk>0 and "
                        + " concat(periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.dokter_perujuk) not in "
                        + " (select concat(bayar_periksa_radiologi_perujuk.no_rawat,bayar_periksa_radiologi_perujuk.kd_jenis_prw,bayar_periksa_radiologi_perujuk.tgl_periksa,bayar_periksa_radiologi_perujuk.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_radiologi_perujuk on bayar_periksa_radiologi_perujuk.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_radiologi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_radiologi.tgl_periksa like ?)")
                        + "order by periksa_radiologi.tgl_periksa,periksa_radiologi.jam,jns_perawatan_radiologi.nm_perawatan  ");
                try {
                    psperiksa_radiologi2.setString(1, getTargetDokter());
                    psperiksa_radiologi2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_radiologi2.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_radiologi = psperiksa_radiologi2.executeQuery();

                    while (rsperiksa_radiologi.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_radiologi.getString("kd_dokter"), rsperiksa_radiologi.getDouble("tarif_perujuk"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_radiologi.getString("tgl_periksa"), rsperiksa_radiologi.getString("jam"), rsperiksa_radiologi.getString("no_rawat"), rsperiksa_radiologi.getString("no_rkm_medis"),
                            rsperiksa_radiologi.getString("nm_pasien"), rsperiksa_radiologi.getString("kd_jenis_prw"), rsperiksa_radiologi.getString("nm_perawatan"),
                            "Radiologi " + rsperiksa_radiologi.getString("status") + " Perujuk", currentBruto, currentSharing, jmTariff, "",
                            Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_radiologi.getString("kd_pj") + "'") + isCekCOB(rsperiksa_radiologi.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_bayar from bayar_piutang where no_rawat='" + rsperiksa_radiologi.getString("no_rawat") + "'"), rsperiksa_radiologi.getString("kd_dokter"), getNamaDokter(rsperiksa_radiologi.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Perujuk Radiologi : " + e);
                } finally {
                    if (rsperiksa_radiologi != null) {
                        rsperiksa_radiologi.close();
                    }
                    if (psperiksa_radiologi2 != null) {
                        psperiksa_radiologi2.close();
                    }
                }
            }

            LCount.setText(Valid.SetAngka(total));
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void prosesCariSudahBayarNonPiutang() {
        jmMappingCache.clear();
        try {
            total = 0;
            if (chkRalan.isSelected() == true) {
                //rawat jalan
                psrawatjalandr = koneksi.prepareStatement("select pasien.nm_pasien,rawat_jl_dr.biaya_rawat,jns_perawatan.tarif_tindakandr as master_tarif_dr,"
                        + "jns_perawatan.nm_perawatan,rawat_jl_dr.tgl_perawatan,rawat_jl_dr.jam_rawat,reg_periksa.kd_pj,rawat_jl_dr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_jl_dr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_jl_dr on rawat_jl_dr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan on rawat_jl_dr.kd_jenis_prw=jns_perawatan.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and rawat_jl_dr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_jl_dr.tarif_tindakandr>0 and "
                        + "concat(rawat_jl_dr.no_rawat,rawat_jl_dr.kd_jenis_prw,rawat_jl_dr.tgl_perawatan,rawat_jl_dr.jam_rawat,rawat_jl_dr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_jl_dr.no_rawat,bayar_rawat_jl_dr.kd_jenis_prw,bayar_rawat_jl_dr.tgl_perawatan,bayar_rawat_jl_dr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_jl_dr on bayar_rawat_jl_dr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_jl_dr.tgl_perawatan like ?)")
                        + "order by reg_periksa.tgl_registrasi,jns_perawatan.nm_perawatan");
                psrawatjalandrpr = koneksi.prepareStatement("select pasien.nm_pasien,rawat_jl_drpr.biaya_rawat,jns_perawatan.tarif_tindakandr as master_tarif_dr,"
                        + "jns_perawatan.nm_perawatan,rawat_jl_drpr.tgl_perawatan,rawat_jl_drpr.jam_rawat,reg_periksa.kd_pj,rawat_jl_drpr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_jl_drpr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_jl_drpr on rawat_jl_drpr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan on rawat_jl_drpr.kd_jenis_prw=jns_perawatan.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and rawat_jl_drpr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_jl_drpr.tarif_tindakandr>0 and "
                        + "concat(rawat_jl_drpr.no_rawat,rawat_jl_drpr.kd_jenis_prw,rawat_jl_drpr.tgl_perawatan,rawat_jl_drpr.jam_rawat,rawat_jl_drpr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_jl_drpr.no_rawat,bayar_rawat_jl_drpr.kd_jenis_prw,bayar_rawat_jl_drpr.tgl_perawatan,bayar_rawat_jl_drpr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_jl_drpr on bayar_rawat_jl_drpr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_jl_drpr.tgl_perawatan like ?)")
                        + "order by reg_periksa.tgl_registrasi,jns_perawatan.nm_perawatan");
                try {
                    psrawatjalandr.setString(1, getTargetDokter());
                    psrawatjalandr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatjalandr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatjalandr = psrawatjalandr.executeQuery();

                    psrawatjalandrpr.setString(1, getTargetDokter());
                    psrawatjalandrpr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatjalandrpr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatjalandrpr = psrawatjalandrpr.executeQuery();

                    while (rsrawatjalandr.next()) {
                        String tglTrx = rsrawatjalandr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatjalandr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatjalandr.getString("kd_dokter"), tarifAsli, rsrawatjalandr.getString("no_rawat"), rsrawatjalandr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatjalandr.getString("jam_rawat"), rsrawatjalandr.getString("no_rawat"), rsrawatjalandr.getString("no_rkm_medis"),
                            rsrawatjalandr.getString("nm_pasien"), rsrawatjalandr.getString("kd_jenis_prw"), cekPotongan(rsrawatjalandr.getString("no_rawat"), rsrawatjalandr.getString("nm_perawatan")),
                            "Rawat Jalan Dr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatjalandr.getString("kd_pj") + "'") + isCekCOB(rsrawatjalandr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatjalandr.getString("no_rawat") + "' LIMIT 1"), rsrawatjalandr.getString("kd_dokter"), getNamaDokter(rsrawatjalandr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
                    }

                    while (rsrawatjalandrpr.next()) {
                        String tglTrx = rsrawatjalandrpr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatjalandrpr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatjalandrpr.getString("kd_dokter"), tarifAsli, rsrawatjalandrpr.getString("no_rawat"), rsrawatjalandrpr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatjalandrpr.getString("jam_rawat"), rsrawatjalandrpr.getString("no_rawat"), rsrawatjalandrpr.getString("no_rkm_medis"),
                            rsrawatjalandrpr.getString("nm_pasien"), rsrawatjalandrpr.getString("kd_jenis_prw"), cekPotongan(rsrawatjalandrpr.getString("no_rawat"), rsrawatjalandrpr.getString("nm_perawatan")),
                            "Rawat Jalan DrPr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatjalandrpr.getString("kd_pj") + "'") + isCekCOB(rsrawatjalandrpr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatjalandrpr.getString("no_rawat") + "' LIMIT 1"), rsrawatjalandrpr.getString("kd_dokter"), getNamaDokter(rsrawatjalandrpr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
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
            }

            if (chkRanap.isSelected() == true) {
                //rawat inap
                psrawatinapdr = koneksi.prepareStatement(
                        "select pasien.nm_pasien,jns_perawatan_inap.nm_perawatan,rawat_inap_dr.biaya_rawat,jns_perawatan_inap.tarif_tindakandr as master_tarif_dr,"
                        + "rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,reg_periksa.kd_pj,rawat_inap_dr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_inap_dr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_inap_dr on rawat_inap_dr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan_inap on rawat_inap_dr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and rawat_inap_dr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_inap_dr.tarif_tindakandr>0 and "
                        + "concat(rawat_inap_dr.no_rawat,rawat_inap_dr.kd_jenis_prw,rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,rawat_inap_dr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_inap_dr.no_rawat,bayar_rawat_inap_dr.kd_jenis_prw,bayar_rawat_inap_dr.tgl_perawatan,bayar_rawat_inap_dr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_inap_dr on bayar_rawat_inap_dr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_inap.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_inap_dr.tgl_perawatan like ?)")
                        + "order by rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,jns_perawatan_inap.nm_perawatan  ");
                psrawatinapdrpr = koneksi.prepareStatement(
                        "select pasien.nm_pasien,jns_perawatan_inap.nm_perawatan,rawat_inap_drpr.biaya_rawat,jns_perawatan_inap.tarif_tindakandr as master_tarif_dr,"
                        + "rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,reg_periksa.kd_pj,rawat_inap_drpr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_inap_drpr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_inap_drpr on rawat_inap_drpr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan_inap on rawat_inap_drpr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and rawat_inap_drpr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_inap_drpr.tarif_tindakandr>0 and "
                        + "concat(rawat_inap_drpr.no_rawat,rawat_inap_drpr.kd_jenis_prw,rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,rawat_inap_drpr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_inap_drpr.no_rawat,bayar_rawat_inap_drpr.kd_jenis_prw,bayar_rawat_inap_drpr.tgl_perawatan,bayar_rawat_inap_drpr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_inap_drpr on bayar_rawat_inap_drpr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_inap.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_inap_drpr.tgl_perawatan like ?)")
                        + "order by rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,jns_perawatan_inap.nm_perawatan  ");
                try {
                    psrawatinapdr.setString(1, getTargetDokter());
                    psrawatinapdr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatinapdr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatinapdr = psrawatinapdr.executeQuery();

                    psrawatinapdrpr.setString(1, getTargetDokter());
                    psrawatinapdrpr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatinapdrpr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatinapdrpr = psrawatinapdrpr.executeQuery();

                    while (rsrawatinapdr.next()) {
                        String tglTrx = rsrawatinapdr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatinapdr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatinapdr.getString("kd_dokter"), tarifAsli, rsrawatinapdr.getString("no_rawat"), rsrawatinapdr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatinapdr.getString("jam_rawat"), rsrawatinapdr.getString("no_rawat"), rsrawatinapdr.getString("no_rkm_medis"),
                            rsrawatinapdr.getString("nm_pasien"), rsrawatinapdr.getString("kd_jenis_prw"), cekPotongan(rsrawatinapdr.getString("no_rawat"), rsrawatinapdr.getString("nm_perawatan")),
                            "Rawat Inap Dr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatinapdr.getString("kd_pj") + "'") + isCekCOB(rsrawatinapdr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatinapdr.getString("no_rawat") + "' LIMIT 1"), rsrawatinapdr.getString("kd_dokter"), getNamaDokter(rsrawatinapdr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
                    }

                    while (rsrawatinapdrpr.next()) {
                        String tglTrx = rsrawatinapdrpr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatinapdrpr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatinapdrpr.getString("kd_dokter"), tarifAsli, rsrawatinapdrpr.getString("no_rawat"), rsrawatinapdrpr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatinapdrpr.getString("jam_rawat"), rsrawatinapdrpr.getString("no_rawat"), rsrawatinapdrpr.getString("no_rkm_medis"),
                            rsrawatinapdrpr.getString("nm_pasien"), rsrawatinapdrpr.getString("kd_jenis_prw"), cekPotongan(rsrawatinapdrpr.getString("no_rawat"), rsrawatinapdrpr.getString("nm_perawatan")),
                            "Rawat Inap DrPr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatinapdrpr.getString("kd_pj") + "'") + isCekCOB(rsrawatinapdrpr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatinapdrpr.getString("no_rawat") + "' LIMIT 1"), rsrawatinapdrpr.getString("kd_dokter"), getNamaDokter(rsrawatinapdrpr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
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
            }

            if (chkOperasi.isSelected() == true) {
                psbiayaoperator1 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator1,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.operator1 as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and operasi.operator1 like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayaoperator1>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.operator1) not in "
                        + "(select concat(bayar_operasi_operator1.no_rawat,bayar_operasi_operator1.kode_paket,bayar_operasi_operator1.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_operator1 on bayar_operasi_operator1.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayaoperator2 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator2,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.operator2 as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and operasi.operator2 like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayaoperator2>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.operator2) not in "
                        + "(select concat(bayar_operasi_operator2.no_rawat,bayar_operasi_operator2.kode_paket,bayar_operasi_operator2.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_operator2 on bayar_operasi_operator2.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayaoperator3 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator3,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.operator3 as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and operasi.operator3 like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayaoperator3>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.operator3) not in "
                        + "(select concat(bayar_operasi_operator3.no_rawat,bayar_operasi_operator3.kode_paket,bayar_operasi_operator3.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_operator3 on bayar_operasi_operator3.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayadokter_anak = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayadokter_anak,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_anak as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and operasi.dokter_anak like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayadokter_anak>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_anak) not in "
                        + "(select concat(bayar_operasi_dokter_anak.no_rawat,bayar_operasi_dokter_anak.kode_paket,bayar_operasi_dokter_anak.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_anak on bayar_operasi_dokter_anak.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiaya_dokter_umum = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biaya_dokter_umum,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_umum as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and operasi.dokter_umum like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biaya_dokter_umum>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_umum) not in "
                        + "(select concat(bayar_operasi_dokter_umum.no_rawat,bayar_operasi_dokter_umum.kode_paket,bayar_operasi_dokter_umum.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_umum on bayar_operasi_dokter_umum.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiaya_dokter_pjanak = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biaya_dokter_pjanak,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_pjanak as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and operasi.dokter_pjanak like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biaya_dokter_pjanak>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_pjanak) not in "
                        + "(select concat(bayar_operasi_dokter_pjanak.no_rawat,bayar_operasi_dokter_pjanak.kode_paket,bayar_operasi_dokter_pjanak.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_pjanak on bayar_operasi_dokter_pjanak.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayadokter_anestesi = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayadokter_anestesi,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_anestesi as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and operasi.dokter_anestesi like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayadokter_anestesi>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_anestesi) not in "
                        + "(select concat(bayar_operasi_dokter_anestesi.no_rawat,bayar_operasi_dokter_anestesi.kode_paket,bayar_operasi_dokter_anestesi.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_anestesi on bayar_operasi_dokter_anestesi.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                try {
                    psbiayaoperator1.setString(1, getTargetDokter());
                    psbiayaoperator1.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayaoperator1.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayaoperator1 = psbiayaoperator1.executeQuery();

                    psbiayaoperator2.setString(1, getTargetDokter());
                    psbiayaoperator2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayaoperator2.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayaoperator2 = psbiayaoperator2.executeQuery();

                    psbiayaoperator3.setString(1, getTargetDokter());
                    psbiayaoperator3.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayaoperator3.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayaoperator3 = psbiayaoperator3.executeQuery();

                    psbiayadokter_anak.setString(1, getTargetDokter());
                    psbiayadokter_anak.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayadokter_anak.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayadokter_anak = psbiayadokter_anak.executeQuery();

                    psbiaya_dokter_umum.setString(1, getTargetDokter());
                    psbiaya_dokter_umum.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiaya_dokter_umum.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiaya_dokter_umum = psbiaya_dokter_umum.executeQuery();

                    psbiaya_dokter_pjanak.setString(1, getTargetDokter());
                    psbiaya_dokter_pjanak.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiaya_dokter_pjanak.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiaya_dokter_pjanak = psbiaya_dokter_pjanak.executeQuery();

                    psbiayadokter_anestesi.setString(1, getTargetDokter());
                    psbiayadokter_anestesi.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayadokter_anestesi.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayadokter_anestesi = psbiayadokter_anestesi.executeQuery();

                    while (rsbiayaoperator1.next()) {
                        String tglOpOp1 = rsbiayaoperator1.getString("tgl_operasi");
                        double tarifMurniOp1 = rsbiayaoperator1.getDouble("biayaoperator1");
                        double[] calcOp1 = hitungJMOperasiPotongan(rsbiayaoperator1.getString("no_rawat"), rsbiayaoperator1.getString("nm_perawatan"), rsbiayaoperator1.getString("kd_dokter"), tarifMurniOp1, rsbiayaoperator1.getString("kode_paket"), tglOpOp1);
                        double brutoOp1 = calcOp1[0];
                        double sharingOp1 = calcOp1[1];
                        double jmOp1 = calcOp1[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpOp1), getJamOnly(tglOpOp1), rsbiayaoperator1.getString("no_rawat"), rsbiayaoperator1.getString("no_rkm_medis"),
                            rsbiayaoperator1.getString("nm_pasien"), rsbiayaoperator1.getString("kode_paket"), cekPotongan(rsbiayaoperator1.getString("no_rawat"), rsbiayaoperator1.getString("nm_perawatan"), rsbiayaoperator1.getString("kode_paket"), tglOpOp1, rsbiayaoperator1.getString("kd_dokter")) + "(Operator 1)",
                            "Operasi " + rsbiayaoperator1.getString("status") + " Op1", brutoOp1, sharingOp1, jmOp1, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayaoperator1.getString("kd_pj") + "'") + isCekCOB(rsbiayaoperator1.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayaoperator1.getString("no_rawat") + "' LIMIT 1"), rsbiayaoperator1.getString("kd_dokter"), getNamaDokter(rsbiayaoperator1.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmOp1;
                    }

                    while (rsbiayaoperator2.next()) {
                        String tglOpOp2 = rsbiayaoperator2.getString("tgl_operasi");
                        double tarifMurniOp2 = rsbiayaoperator2.getDouble("biayaoperator2");
                        double[] calcOp2 = hitungJMOperasiPotongan(rsbiayaoperator2.getString("no_rawat"), rsbiayaoperator2.getString("nm_perawatan"), rsbiayaoperator2.getString("kd_dokter"), tarifMurniOp2, rsbiayaoperator2.getString("kode_paket"), tglOpOp2);
                        double brutoOp2 = calcOp2[0];
                        double sharingOp2 = calcOp2[1];
                        double jmOp2 = calcOp2[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpOp2), getJamOnly(tglOpOp2), rsbiayaoperator2.getString("no_rawat"), rsbiayaoperator2.getString("no_rkm_medis"),
                            rsbiayaoperator2.getString("nm_pasien"), rsbiayaoperator2.getString("kode_paket"), cekPotongan(rsbiayaoperator2.getString("no_rawat"), rsbiayaoperator2.getString("nm_perawatan"), rsbiayaoperator2.getString("kode_paket"), tglOpOp2, rsbiayaoperator2.getString("kd_dokter")) + "(Operator 2)",
                            "Operasi " + rsbiayaoperator2.getString("status") + " Op2", brutoOp2, sharingOp2, jmOp2, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayaoperator2.getString("kd_pj") + "'") + isCekCOB(rsbiayaoperator2.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayaoperator2.getString("no_rawat") + "' LIMIT 1"), rsbiayaoperator2.getString("kd_dokter"), getNamaDokter(rsbiayaoperator2.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmOp2;
                    }

                    while (rsbiayaoperator3.next()) {
                        String tglOpOp3 = rsbiayaoperator3.getString("tgl_operasi");
                        double tarifMurniOp3 = rsbiayaoperator3.getDouble("biayaoperator3");
                        double[] calcOp3 = hitungJMOperasiPotongan(rsbiayaoperator3.getString("no_rawat"), rsbiayaoperator3.getString("nm_perawatan"), rsbiayaoperator3.getString("kd_dokter"), tarifMurniOp3, rsbiayaoperator3.getString("kode_paket"), tglOpOp3);
                        double brutoOp3 = calcOp3[0];
                        double sharingOp3 = calcOp3[1];
                        double jmOp3 = calcOp3[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpOp3), getJamOnly(tglOpOp3), rsbiayaoperator3.getString("no_rawat"), rsbiayaoperator3.getString("no_rkm_medis"),
                            rsbiayaoperator3.getString("nm_pasien"), rsbiayaoperator3.getString("kode_paket"), cekPotongan(rsbiayaoperator3.getString("no_rawat"), rsbiayaoperator3.getString("nm_perawatan"), rsbiayaoperator3.getString("kode_paket"), tglOpOp3, rsbiayaoperator3.getString("kd_dokter")) + "(Operator 3)",
                            "Operasi " + rsbiayaoperator3.getString("status") + " Op3", brutoOp3, sharingOp3, jmOp3, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayaoperator3.getString("kd_pj") + "'") + isCekCOB(rsbiayaoperator3.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayaoperator3.getString("no_rawat") + "' LIMIT 1"), rsbiayaoperator3.getString("kd_dokter"), getNamaDokter(rsbiayaoperator3.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmOp3;
                    }

                    while (rsbiayadokter_anak.next()) {
                        String tglOpDrAnak = rsbiayadokter_anak.getString("tgl_operasi");
                        double tarifMurniDrAnak = rsbiayadokter_anak.getDouble("biayadokter_anak");
                        double[] calcDrAnak = hitungJMOperasiPotongan(rsbiayadokter_anak.getString("no_rawat"), rsbiayadokter_anak.getString("nm_perawatan"), rsbiayadokter_anak.getString("kd_dokter"), tarifMurniDrAnak, rsbiayadokter_anak.getString("kode_paket"), tglOpDrAnak);
                        double brutoDrAnak = calcDrAnak[0];
                        double sharingDrAnak = calcDrAnak[1];
                        double jmDrAnak = calcDrAnak[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrAnak), getJamOnly(tglOpDrAnak), rsbiayadokter_anak.getString("no_rawat"), rsbiayadokter_anak.getString("no_rkm_medis"),
                            rsbiayadokter_anak.getString("nm_pasien"), rsbiayadokter_anak.getString("kode_paket"), cekPotongan(rsbiayadokter_anak.getString("no_rawat"), rsbiayadokter_anak.getString("nm_perawatan"), rsbiayadokter_anak.getString("kode_paket"), tglOpDrAnak, rsbiayadokter_anak.getString("kd_dokter")) + "(dr Anak)",
                            "Operasi " + rsbiayadokter_anak.getString("status") + " dr Anak", brutoDrAnak, sharingDrAnak, jmDrAnak, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayadokter_anak.getString("kd_pj") + "'") + isCekCOB(rsbiayadokter_anak.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayadokter_anak.getString("no_rawat") + "' LIMIT 1"), rsbiayadokter_anak.getString("kd_dokter"), getNamaDokter(rsbiayadokter_anak.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrAnak;
                    }

                    while (rsbiayadokter_anestesi.next()) {
                        String tglOpDrAnestesi = rsbiayadokter_anestesi.getString("tgl_operasi");
                        double tarifMurniDrAnestesi = rsbiayadokter_anestesi.getDouble("biayadokter_anestesi");
                        double[] calcDrAnestesi = hitungJMOperasiPotongan(rsbiayadokter_anestesi.getString("no_rawat"), rsbiayadokter_anestesi.getString("nm_perawatan"), rsbiayadokter_anestesi.getString("kd_dokter"), tarifMurniDrAnestesi, rsbiayadokter_anestesi.getString("kode_paket"), tglOpDrAnestesi);
                        double brutoDrAnestesi = calcDrAnestesi[0];
                        double sharingDrAnestesi = calcDrAnestesi[1];
                        double jmDrAnestesi = calcDrAnestesi[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrAnestesi), getJamOnly(tglOpDrAnestesi), rsbiayadokter_anestesi.getString("no_rawat"), rsbiayadokter_anestesi.getString("no_rkm_medis"),
                            rsbiayadokter_anestesi.getString("nm_pasien"), rsbiayadokter_anestesi.getString("kode_paket"), cekPotongan(rsbiayadokter_anestesi.getString("no_rawat"), rsbiayadokter_anestesi.getString("nm_perawatan"), rsbiayadokter_anestesi.getString("kode_paket"), tglOpDrAnestesi, rsbiayadokter_anestesi.getString("kd_dokter")) + "(dr Anestesi)",
                            "Operasi " + rsbiayadokter_anestesi.getString("status") + " dr Anastesi", brutoDrAnestesi, sharingDrAnestesi, jmDrAnestesi, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayadokter_anestesi.getString("kd_pj") + "'") + isCekCOB(rsbiayadokter_anestesi.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayadokter_anestesi.getString("no_rawat") + "' LIMIT 1"), rsbiayadokter_anestesi.getString("kd_dokter"), getNamaDokter(rsbiayadokter_anestesi.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrAnestesi;
                    }

                    while (rsbiaya_dokter_pjanak.next()) {
                        String tglOpDrPjAnak = rsbiaya_dokter_pjanak.getString("tgl_operasi");
                        double tarifMurniDrPjAnak = rsbiaya_dokter_pjanak.getDouble("biaya_dokter_pjanak");
                        double[] calcDrPjAnak = hitungJMOperasiPotongan(rsbiaya_dokter_pjanak.getString("no_rawat"), rsbiaya_dokter_pjanak.getString("nm_perawatan"), rsbiaya_dokter_pjanak.getString("kd_dokter"), tarifMurniDrPjAnak, rsbiaya_dokter_pjanak.getString("kode_paket"), tglOpDrPjAnak);
                        double brutoDrPjAnak = calcDrPjAnak[0];
                        double sharingDrPjAnak = calcDrPjAnak[1];
                        double jmDrPjAnak = calcDrPjAnak[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrPjAnak), getJamOnly(tglOpDrPjAnak), rsbiaya_dokter_pjanak.getString("no_rawat"), rsbiaya_dokter_pjanak.getString("no_rkm_medis"),
                            rsbiaya_dokter_pjanak.getString("nm_pasien"), rsbiaya_dokter_pjanak.getString("kode_paket"), cekPotongan(rsbiaya_dokter_pjanak.getString("no_rawat"), rsbiaya_dokter_pjanak.getString("nm_perawatan"), rsbiaya_dokter_pjanak.getString("kode_paket"), tglOpDrPjAnak, rsbiaya_dokter_pjanak.getString("kd_dokter")) + "(dr Pj Anak)",
                            "Operasi " + rsbiaya_dokter_pjanak.getString("status") + " dr PJ Anak", brutoDrPjAnak, sharingDrPjAnak, jmDrPjAnak, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiaya_dokter_pjanak.getString("kd_pj") + "'") + isCekCOB(rsbiaya_dokter_pjanak.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiaya_dokter_pjanak.getString("no_rawat") + "' LIMIT 1"), rsbiaya_dokter_pjanak.getString("kd_dokter"), getNamaDokter(rsbiaya_dokter_pjanak.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrPjAnak;
                    }

                    while (rsbiaya_dokter_umum.next()) {
                        String tglOpDrUmum = rsbiaya_dokter_umum.getString("tgl_operasi");
                        double tarifMurniDrUmum = rsbiaya_dokter_umum.getDouble("biaya_dokter_umum");
                        double[] calcDrUmum = hitungJMOperasiPotongan(rsbiaya_dokter_umum.getString("no_rawat"), rsbiaya_dokter_umum.getString("nm_perawatan"), rsbiaya_dokter_umum.getString("kd_dokter"), tarifMurniDrUmum, rsbiaya_dokter_umum.getString("kode_paket"), tglOpDrUmum);
                        double brutoDrUmum = calcDrUmum[0];
                        double sharingDrUmum = calcDrUmum[1];
                        double jmDrUmum = calcDrUmum[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrUmum), getJamOnly(tglOpDrUmum), rsbiaya_dokter_umum.getString("no_rawat"), rsbiaya_dokter_umum.getString("no_rkm_medis"),
                            rsbiaya_dokter_umum.getString("nm_pasien"), rsbiaya_dokter_umum.getString("kode_paket"), cekPotongan(rsbiaya_dokter_umum.getString("no_rawat"), rsbiaya_dokter_umum.getString("nm_perawatan"), rsbiaya_dokter_umum.getString("kode_paket"), tglOpDrUmum, rsbiaya_dokter_umum.getString("kd_dokter")) + "(dr Umum)",
                            "Operasi " + rsbiaya_dokter_umum.getString("status") + " dr Umum", brutoDrUmum, sharingDrUmum, jmDrUmum, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiaya_dokter_umum.getString("kd_pj") + "'") + isCekCOB(rsbiaya_dokter_umum.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiaya_dokter_umum.getString("no_rawat") + "' LIMIT 1"), rsbiaya_dokter_umum.getString("kd_dokter"), getNamaDokter(rsbiaya_dokter_umum.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrUmum;
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
                    if (rsbiayadokter_anestesi != null) {
                        rsbiayadokter_anestesi.close();
                    }
                    if (psbiayadokter_anestesi != null) {
                        psbiayadokter_anestesi.close();
                    }
                }
            }

            if (chkLaborat.isSelected() == true) {
                //periksa lab
                psperiksa_lab = koneksi.prepareStatement(
                        "select periksa_lab.tarif_tindakan_dokter,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_lab.status,"
                        + "jns_perawatan_lab.nm_perawatan,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,reg_periksa.kd_pj,periksa_lab.kd_dokter "
                        + " from periksa_lab inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and periksa_lab.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_lab.tarif_tindakan_dokter>0 and "
                        + " concat(periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.kd_dokter) not in "
                        + " (select concat(bayar_periksa_lab.no_rawat,bayar_periksa_lab.kd_jenis_prw,bayar_periksa_lab.tgl_periksa,bayar_periksa_lab.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_lab on bayar_periksa_lab.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or  jns_perawatan_lab.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam,jns_perawatan_lab.nm_perawatan  ");
                try {
                    psperiksa_lab.setString(1, getTargetDokter());
                    psperiksa_lab.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_lab.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_lab = psperiksa_lab.executeQuery();

                    while (rsperiksa_lab.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_lab.getString("kd_dokter"), rsperiksa_lab.getDouble("tarif_tindakan_dokter"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_lab.getString("tgl_periksa"), rsperiksa_lab.getString("jam"), rsperiksa_lab.getString("no_rawat"), rsperiksa_lab.getString("no_rkm_medis"),
                            rsperiksa_lab.getString("nm_pasien") + " (" + rsperiksa_lab.getString("kd_pj") + ")", rsperiksa_lab.getString("kd_jenis_prw"), rsperiksa_lab.getString("nm_perawatan"),
                            "Laborat " + rsperiksa_lab.getString("status") + " PJ", currentBruto, currentSharing, jmTariff, "",
                            Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_lab.getString("kd_pj") + "'") + isCekCOB(rsperiksa_lab.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsperiksa_lab.getString("no_rawat") + "' LIMIT 1"), rsperiksa_lab.getString("kd_dokter"), getNamaDokter(rsperiksa_lab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Lab : " + e);
                } finally {
                    if (rsperiksa_lab != null) {
                        rsperiksa_lab.close();
                    }
                    if (psperiksa_lab != null) {
                        psperiksa_lab.close();
                    }
                }

                //detail periksa lab
                psdetaillab = koneksi.prepareStatement(
                        "select detail_periksa_lab.bagian_dokter,pasien.nm_pasien,periksa_lab.status,detail_periksa_lab.id_template,"
                        + "template_laboratorium.Pemeriksaan,reg_periksa.kd_pj,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                        + "periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.kd_jenis_prw,periksa_lab.kd_dokter "
                        + "from detail_periksa_lab inner join periksa_lab on periksa_lab.no_rawat=detail_periksa_lab.no_rawat "
                        + "and periksa_lab.kd_jenis_prw=detail_periksa_lab.kd_jenis_prw "
                        + "and periksa_lab.tgl_periksa=detail_periksa_lab.tgl_periksa "
                        + "and periksa_lab.jam=detail_periksa_lab.jam "
                        + "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and periksa_lab.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and detail_periksa_lab.bagian_dokter>0 and "
                        + "concat(detail_periksa_lab.no_rawat,detail_periksa_lab.kd_jenis_prw,detail_periksa_lab.tgl_periksa,detail_periksa_lab.jam,detail_periksa_lab.id_template,periksa_lab.kd_dokter) not in "
                        + "(select concat(bayar_detail_periksa_lab.no_rawat,bayar_detail_periksa_lab.kd_jenis_prw,bayar_detail_periksa_lab.tgl_periksa,bayar_detail_periksa_lab.jam,bayar_detail_periksa_lab.id_template,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_detail_periksa_lab on bayar_detail_periksa_lab.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or template_laboratorium.Pemeriksaan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam");
                try {
                    psdetaillab.setString(1, getTargetDokter());
                    psdetaillab.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psdetaillab.setString(3, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(4, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(5, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(6, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsdetaillab = psdetaillab.executeQuery();

                    while (rsdetaillab.next()) {
                        double jmTariff = hitungJMDokter(rsdetaillab.getString("kd_dokter"), rsdetaillab.getDouble("bagian_dokter"));
                        tabMode.addRow(new Object[]{
                            false, rsdetaillab.getString("tgl_periksa"), rsdetaillab.getString("jam"), rsdetaillab.getString("no_rawat"), rsdetaillab.getString("no_rkm_medis"),
                            rsdetaillab.getString("nm_pasien") + " (" + rsdetaillab.getString("kd_pj") + ")", rsdetaillab.getString("kd_jenis_prw"), rsdetaillab.getString("Pemeriksaan"),
                            "Laborat " + rsdetaillab.getString("status") + " PJ Detail", currentBruto, currentSharing, jmTariff, rsdetaillab.getString("id_template"), Sequel.cariIsi("select png_jawab from penjab where kd_pj=\'" + rsdetaillab.getString("kd_pj") + "\'") + isCekCOB(rsdetaillab.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat=\'" + rsdetaillab.getString("no_rawat") + "\' LIMIT 1"), rsdetaillab.getString("kd_dokter"), getNamaDokter(rsdetaillab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Detail Lab : " + e);
                } finally {
                    if (rsdetaillab != null) {
                        rsdetaillab.close();
                    }
                    if (psdetaillab != null) {
                        psdetaillab.close();
                    }
                }

                //periksa lab perujuk
                psperiksa_lab2 = koneksi.prepareStatement(
                        "select periksa_lab.tarif_perujuk,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_lab.status,"
                        + "jns_perawatan_lab.nm_perawatan,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,reg_periksa.kd_pj,periksa_lab.dokter_perujuk as kd_dokter "
                        + " from periksa_lab inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and periksa_lab.dokter_perujuk like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_lab.tarif_perujuk>0 and "
                        + " concat(periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.dokter_perujuk) not in "
                        + " (select concat(bayar_periksa_lab_perujuk.no_rawat,bayar_periksa_lab_perujuk.kd_jenis_prw,bayar_periksa_lab_perujuk.tgl_periksa,bayar_periksa_lab_perujuk.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_lab_perujuk on bayar_periksa_lab_perujuk.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or  jns_perawatan_lab.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam,jns_perawatan_lab.nm_perawatan  ");
                try {
                    psperiksa_lab2.setString(1, getTargetDokter());
                    psperiksa_lab2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_lab2.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_lab = psperiksa_lab2.executeQuery();

                    while (rsperiksa_lab.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_lab.getString("kd_dokter"), rsperiksa_lab.getDouble("tarif_perujuk"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_lab.getString("tgl_periksa"), rsperiksa_lab.getString("jam"), rsperiksa_lab.getString("no_rawat"), rsperiksa_lab.getString("no_rkm_medis"),
                            rsperiksa_lab.getString("nm_pasien") + " (" + rsperiksa_lab.getString("kd_pj") + ")", rsperiksa_lab.getString("kd_jenis_prw"), rsperiksa_lab.getString("nm_perawatan"),
                            "Laborat " + rsperiksa_lab.getString("status") + " Perujuk", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj=\'" + rsperiksa_lab.getString("kd_pj") + "\'") + isCekCOB(rsperiksa_lab.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat=\'" + rsperiksa_lab.getString("no_rawat") + "\' LIMIT 1"), rsperiksa_lab.getString("kd_dokter"), getNamaDokter(rsperiksa_lab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Perujuk Lab : " + e);
                } finally {
                    if (rsperiksa_lab != null) {
                        rsperiksa_lab.close();
                    }
                    if (psperiksa_lab2 != null) {
                        psperiksa_lab2.close();
                    }
                }

                psdetaillab2 = koneksi.prepareStatement(
                        "select detail_periksa_lab.bagian_perujuk,pasien.nm_pasien,periksa_lab.status,detail_periksa_lab.id_template,"
                        + "template_laboratorium.Pemeriksaan,reg_periksa.kd_pj,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                        + "periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.kd_jenis_prw,periksa_lab.dokter_perujuk as kd_dokter "
                        + "from detail_periksa_lab inner join periksa_lab on periksa_lab.no_rawat=detail_periksa_lab.no_rawat "
                        + "and periksa_lab.kd_jenis_prw=detail_periksa_lab.kd_jenis_prw "
                        + "and periksa_lab.tgl_periksa=detail_periksa_lab.tgl_periksa "
                        + "and periksa_lab.jam=detail_periksa_lab.jam "
                        + "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and periksa_lab.dokter_perujuk like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and detail_periksa_lab.bagian_perujuk>0 and "
                        + "concat(detail_periksa_lab.no_rawat,detail_periksa_lab.kd_jenis_prw,detail_periksa_lab.tgl_periksa,detail_periksa_lab.jam,detail_periksa_lab.id_template,periksa_lab.dokter_perujuk) not in "
                        + "(select concat(bayar_detail_periksa_lab_perujuk.no_rawat,bayar_detail_periksa_lab_perujuk.kd_jenis_prw,bayar_detail_periksa_lab_perujuk.tgl_periksa,bayar_detail_periksa_lab_perujuk.jam,bayar_detail_periksa_lab_perujuk.id_template,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_detail_periksa_lab_perujuk on bayar_detail_periksa_lab_perujuk.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or template_laboratorium.Pemeriksaan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam");
                try {
                    psdetaillab2.setString(1, getTargetDokter());
                    psdetaillab2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psdetaillab2.setString(3, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(4, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(5, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(6, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsdetaillab = psdetaillab2.executeQuery();

                    while (rsdetaillab.next()) {
                        double jmTariff = hitungJMDokter(rsdetaillab.getString("kd_dokter"), rsdetaillab.getDouble("bagian_perujuk"));
                        tabMode.addRow(new Object[]{
                            false, rsdetaillab.getString("tgl_periksa"), rsdetaillab.getString("jam"), rsdetaillab.getString("no_rawat"), rsdetaillab.getString("no_rkm_medis"),
                            rsdetaillab.getString("nm_pasien") + " (" + rsdetaillab.getString("kd_pj") + ")", rsdetaillab.getString("kd_jenis_prw"), rsdetaillab.getString("Pemeriksaan"),
                            "Laborat " + rsdetaillab.getString("status") + " Perujuk Detail", currentBruto, currentSharing, jmTariff, rsdetaillab.getString("id_template"), Sequel.cariIsi("select png_jawab from penjab where kd_pj=\'" + rsdetaillab.getString("kd_pj") + "\'") + isCekCOB(rsdetaillab.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat=\'" + rsdetaillab.getString("no_rawat") + "\' LIMIT 1"), rsdetaillab.getString("kd_dokter"), getNamaDokter(rsdetaillab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Detail Perujuk : " + e);
                } finally {
                    if (rsdetaillab != null) {
                        rsdetaillab.close();
                    }
                    if (psdetaillab2 != null) {
                        psdetaillab2.close();
                    }
                }
            }

            if (chkRadiologi.isSelected() == true) {
                //periksa radiologi
                psperiksa_radiologi = koneksi.prepareStatement("select periksa_radiologi.tarif_tindakan_dokter,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_radiologi.status,"
                        + "jns_perawatan_radiologi.nm_perawatan,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,reg_periksa.kd_pj,periksa_radiologi.kd_dokter "
                        + " from periksa_radiologi inner join reg_periksa on periksa_radiologi.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_radiologi on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and periksa_radiologi.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_radiologi.tarif_tindakan_dokter>0 and"
                        + " concat(periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.kd_dokter) not in "
                        + " (select concat(bayar_periksa_radiologi.no_rawat,bayar_periksa_radiologi.kd_jenis_prw,bayar_periksa_radiologi.tgl_periksa,bayar_periksa_radiologi.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_radiologi on bayar_periksa_radiologi.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_radiologi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_radiologi.tgl_periksa like ?)")
                        + "order by periksa_radiologi.tgl_periksa,periksa_radiologi.jam,jns_perawatan_radiologi.nm_perawatan  ");
                try {
                    psperiksa_radiologi.setString(1, getTargetDokter());
                    psperiksa_radiologi.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_radiologi.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_radiologi = psperiksa_radiologi.executeQuery();

                    while (rsperiksa_radiologi.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_radiologi.getString("kd_dokter"), rsperiksa_radiologi.getDouble("tarif_tindakan_dokter"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_radiologi.getString("tgl_periksa"), rsperiksa_radiologi.getString("jam"), rsperiksa_radiologi.getString("no_rawat"), rsperiksa_radiologi.getString("no_rkm_medis"),
                            rsperiksa_radiologi.getString("nm_pasien"), rsperiksa_radiologi.getString("kd_jenis_prw"), rsperiksa_radiologi.getString("nm_perawatan"),
                            "Radiologi " + rsperiksa_radiologi.getString("status") + " PJ", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_radiologi.getString("kd_pj") + "'") + isCekCOB(rsperiksa_radiologi.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsperiksa_radiologi.getString("no_rawat") + "' LIMIT 1"), rsperiksa_radiologi.getString("kd_dokter"), getNamaDokter(rsperiksa_radiologi.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Radiologi : " + e);
                } finally {
                    if (rsperiksa_radiologi != null) {
                        rsperiksa_radiologi.close();
                    }
                    if (psperiksa_radiologi != null) {
                        psperiksa_radiologi.close();
                    }
                }

                //periksa radiologi
                psperiksa_radiologi2 = koneksi.prepareStatement("select periksa_radiologi.tarif_perujuk,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_radiologi.status,"
                        + "jns_perawatan_radiologi.nm_perawatan,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,reg_periksa.kd_pj,periksa_radiologi.dokter_perujuk as kd_dokter "
                        + " from periksa_radiologi inner join reg_periksa on periksa_radiologi.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_radiologi on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " where " + filterbilling + " reg_periksa.status_bayar='Sudah Bayar' and reg_periksa.no_rawat not in (select no_rawat from piutang_pasien) and periksa_radiologi.dokter_perujuk like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_radiologi.tarif_perujuk>0 and "
                        + " concat(periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.dokter_perujuk) not in "
                        + " (select concat(bayar_periksa_radiologi_perujuk.no_rawat,bayar_periksa_radiologi_perujuk.kd_jenis_prw,bayar_periksa_radiologi_perujuk.tgl_periksa,bayar_periksa_radiologi_perujuk.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_radiologi_perujuk on bayar_periksa_radiologi_perujuk.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_radiologi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_radiologi.tgl_periksa like ?)")
                        + "order by periksa_radiologi.tgl_periksa,periksa_radiologi.jam,jns_perawatan_radiologi.nm_perawatan  ");
                try {
                    psperiksa_radiologi2.setString(1, getTargetDokter());
                    psperiksa_radiologi2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_radiologi2.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_radiologi = psperiksa_radiologi2.executeQuery();

                    while (rsperiksa_radiologi.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_radiologi.getString("kd_dokter"), rsperiksa_radiologi.getDouble("tarif_perujuk"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_radiologi.getString("tgl_periksa"), rsperiksa_radiologi.getString("jam"), rsperiksa_radiologi.getString("no_rawat"), rsperiksa_radiologi.getString("no_rkm_medis"),
                            rsperiksa_radiologi.getString("nm_pasien"), rsperiksa_radiologi.getString("kd_jenis_prw"), rsperiksa_radiologi.getString("nm_perawatan"),
                            "Radiologi " + rsperiksa_radiologi.getString("status") + " Perujuk", currentBruto, currentSharing, jmTariff, "",
                            Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_radiologi.getString("kd_pj") + "'") + isCekCOB(rsperiksa_radiologi.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsperiksa_radiologi.getString("no_rawat") + "' LIMIT 1"), rsperiksa_radiologi.getString("kd_dokter"), getNamaDokter(rsperiksa_radiologi.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Perujuk Radiologi : " + e);
                } finally {
                    if (rsperiksa_radiologi != null) {
                        rsperiksa_radiologi.close();
                    }
                    if (psperiksa_radiologi2 != null) {
                        psperiksa_radiologi2.close();
                    }
                }
            }

            LCount.setText(Valid.SetAngka(total));
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void prosesCariBelumTerclosing() {
        try {
            total = 0;
            if (chkRalan.isSelected() == true) {
                //rawat jalan
                psrawatjalandr = koneksi.prepareStatement("select pasien.nm_pasien,rawat_jl_dr.biaya_rawat,jns_perawatan.tarif_tindakandr as master_tarif_dr,"
                        + "jns_perawatan.nm_perawatan,rawat_jl_dr.tgl_perawatan,rawat_jl_dr.jam_rawat,reg_periksa.kd_pj,rawat_jl_dr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_jl_dr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_jl_dr on rawat_jl_dr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan on rawat_jl_dr.kd_jenis_prw=jns_perawatan.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where reg_periksa.status_bayar='Belum Bayar' and rawat_jl_dr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_jl_dr.tarif_tindakandr>0 and "
                        + "concat(rawat_jl_dr.no_rawat,rawat_jl_dr.kd_jenis_prw,rawat_jl_dr.tgl_perawatan,rawat_jl_dr.jam_rawat,rawat_jl_dr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_jl_dr.no_rawat,bayar_rawat_jl_dr.kd_jenis_prw,bayar_rawat_jl_dr.tgl_perawatan,bayar_rawat_jl_dr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_jl_dr on bayar_rawat_jl_dr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_jl_dr.tgl_perawatan like ?)")
                        + "order by reg_periksa.tgl_registrasi,jns_perawatan.nm_perawatan");
                psrawatjalandrpr = koneksi.prepareStatement("select pasien.nm_pasien,rawat_jl_drpr.biaya_rawat,jns_perawatan.tarif_tindakandr as master_tarif_dr,"
                        + "jns_perawatan.nm_perawatan,rawat_jl_drpr.tgl_perawatan,rawat_jl_drpr.jam_rawat,reg_periksa.kd_pj,rawat_jl_drpr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_jl_drpr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_jl_drpr on rawat_jl_drpr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan on rawat_jl_drpr.kd_jenis_prw=jns_perawatan.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where reg_periksa.status_bayar='Belum Bayar' and rawat_jl_drpr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_jl_drpr.tarif_tindakandr>0 and "
                        + "concat(rawat_jl_drpr.no_rawat,rawat_jl_drpr.kd_jenis_prw,rawat_jl_drpr.tgl_perawatan,rawat_jl_drpr.jam_rawat,rawat_jl_drpr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_jl_drpr.no_rawat,bayar_rawat_jl_drpr.kd_jenis_prw,bayar_rawat_jl_drpr.tgl_perawatan,bayar_rawat_jl_drpr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_jl_drpr on bayar_rawat_jl_drpr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_jl_drpr.tgl_perawatan like ?)")
                        + "order by reg_periksa.tgl_registrasi,jns_perawatan.nm_perawatan");
                try {
                    psrawatjalandr.setString(1, getTargetDokter());
                    psrawatjalandr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatjalandr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatjalandr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatjalandr = psrawatjalandr.executeQuery();

                    psrawatjalandrpr.setString(1, getTargetDokter());
                    psrawatjalandrpr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatjalandrpr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatjalandrpr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatjalandrpr = psrawatjalandrpr.executeQuery();

                    while (rsrawatjalandr.next()) {
                        String tglTrx = rsrawatjalandr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatjalandr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatjalandr.getString("kd_dokter"), tarifAsli, rsrawatjalandr.getString("no_rawat"), rsrawatjalandr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatjalandr.getString("jam_rawat"), rsrawatjalandr.getString("no_rawat"), rsrawatjalandr.getString("no_rkm_medis"),
                            rsrawatjalandr.getString("nm_pasien"), rsrawatjalandr.getString("kd_jenis_prw"), cekPotongan(rsrawatjalandr.getString("no_rawat"), rsrawatjalandr.getString("nm_perawatan")),
                            "Rawat Jalan Dr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatjalandr.getString("kd_pj") + "'") + isCekCOB(rsrawatjalandr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatjalandr.getString("no_rawat") + "' LIMIT 1"), rsrawatjalandr.getString("kd_dokter"), getNamaDokter(rsrawatjalandr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
                    }

                    while (rsrawatjalandrpr.next()) {
                        String tglTrx = rsrawatjalandrpr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatjalandrpr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatjalandrpr.getString("kd_dokter"), tarifAsli, rsrawatjalandrpr.getString("no_rawat"), rsrawatjalandrpr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatjalandrpr.getString("jam_rawat"), rsrawatjalandrpr.getString("no_rawat"), rsrawatjalandrpr.getString("no_rkm_medis"),
                            rsrawatjalandrpr.getString("nm_pasien"), rsrawatjalandrpr.getString("kd_jenis_prw"), cekPotongan(rsrawatjalandrpr.getString("no_rawat"), rsrawatjalandrpr.getString("nm_perawatan")),
                            "Rawat Jalan DrPr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatjalandrpr.getString("kd_pj") + "'") + isCekCOB(rsrawatjalandrpr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatjalandrpr.getString("no_rawat") + "' LIMIT 1"), rsrawatjalandrpr.getString("kd_dokter"), getNamaDokter(rsrawatjalandrpr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
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
            }

            if (chkRanap.isSelected() == true) {
                //rawat inap
                psrawatinapdr = koneksi.prepareStatement(
                        "select pasien.nm_pasien,jns_perawatan_inap.nm_perawatan,rawat_inap_dr.biaya_rawat,jns_perawatan_inap.tarif_tindakandr as master_tarif_dr,"
                        + "rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,reg_periksa.kd_pj,rawat_inap_dr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_inap_dr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_inap_dr on rawat_inap_dr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan_inap on rawat_inap_dr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where reg_periksa.status_bayar='Belum Bayar' and rawat_inap_dr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_inap_dr.tarif_tindakandr>0 and "
                        + "concat(rawat_inap_dr.no_rawat,rawat_inap_dr.kd_jenis_prw,rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,rawat_inap_dr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_inap_dr.no_rawat,bayar_rawat_inap_dr.kd_jenis_prw,bayar_rawat_inap_dr.tgl_perawatan,bayar_rawat_inap_dr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_inap_dr on bayar_rawat_inap_dr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_inap.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_inap_dr.tgl_perawatan like ?)")
                        + "order by rawat_inap_dr.tgl_perawatan,rawat_inap_dr.jam_rawat,jns_perawatan_inap.nm_perawatan  ");
                psrawatinapdrpr = koneksi.prepareStatement(
                        "select pasien.nm_pasien,jns_perawatan_inap.nm_perawatan,rawat_inap_drpr.biaya_rawat,jns_perawatan_inap.tarif_tindakandr as master_tarif_dr,"
                        + "rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,reg_periksa.kd_pj,rawat_inap_drpr.kd_jenis_prw, "
                        + "reg_periksa.no_rawat,reg_periksa.no_rkm_medis,rawat_inap_drpr.kd_dokter from pasien inner join reg_periksa on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join rawat_inap_drpr on rawat_inap_drpr.no_rawat=reg_periksa.no_rawat "
                        + "inner join jns_perawatan_inap on rawat_inap_drpr.kd_jenis_prw=jns_perawatan_inap.kd_jenis_prw "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where reg_periksa.status_bayar='Belum Bayar' and rawat_inap_drpr.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and rawat_inap_drpr.tarif_tindakandr>0 and "
                        + "concat(rawat_inap_drpr.no_rawat,rawat_inap_drpr.kd_jenis_prw,rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,rawat_inap_drpr.kd_dokter) not in "
                        + "(select concat(bayar_rawat_inap_drpr.no_rawat,bayar_rawat_inap_drpr.kd_jenis_prw,bayar_rawat_inap_drpr.tgl_perawatan,bayar_rawat_inap_drpr.jam_rawat,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_rawat_inap_drpr on bayar_rawat_inap_drpr.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_inap.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or rawat_inap_drpr.tgl_perawatan like ?)")
                        + "order by rawat_inap_drpr.tgl_perawatan,rawat_inap_drpr.jam_rawat,jns_perawatan_inap.nm_perawatan  ");
                try {
                    psrawatinapdr.setString(1, getTargetDokter());
                    psrawatinapdr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatinapdr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatinapdr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatinapdr = psrawatinapdr.executeQuery();

                    psrawatinapdrpr.setString(1, getTargetDokter());
                    psrawatinapdrpr.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psrawatinapdrpr.setString(3, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(4, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(5, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(6, "%" + TCari.getText().trim() + "%");
                        psrawatinapdrpr.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsrawatinapdrpr = psrawatinapdrpr.executeQuery();

                    while (rsrawatinapdr.next()) {
                        String tglTrx = rsrawatinapdr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatinapdr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatinapdr.getString("kd_dokter"), tarifAsli, rsrawatinapdr.getString("no_rawat"), rsrawatinapdr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatinapdr.getString("jam_rawat"), rsrawatinapdr.getString("no_rawat"), rsrawatinapdr.getString("no_rkm_medis"),
                            rsrawatinapdr.getString("nm_pasien"), rsrawatinapdr.getString("kd_jenis_prw"), cekPotongan(rsrawatinapdr.getString("no_rawat"), rsrawatinapdr.getString("nm_perawatan")),
                            "Rawat Inap Dr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatinapdr.getString("kd_pj") + "'") + isCekCOB(rsrawatinapdr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatinapdr.getString("no_rawat") + "' LIMIT 1"), rsrawatinapdr.getString("kd_dokter"), getNamaDokter(rsrawatinapdr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
                    }

                    while (rsrawatinapdrpr.next()) {
                        String tglTrx = rsrawatinapdrpr.getString("tgl_perawatan");
                        double tarifAsli = rsrawatinapdrpr.getDouble("master_tarif_dr");
                        double[] calc = hitungBrutoSharingNettoRalanWithPotongan(ChkMapping.isSelected(), rsrawatinapdrpr.getString("kd_dokter"), tarifAsli, rsrawatinapdrpr.getString("no_rawat"), rsrawatinapdrpr.getString("nm_perawatan"));
                        double bruto = calc[0];
                        double sharing = calc[1];
                        double jm = calc[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglTrx), rsrawatinapdrpr.getString("jam_rawat"), rsrawatinapdrpr.getString("no_rawat"), rsrawatinapdrpr.getString("no_rkm_medis"),
                            rsrawatinapdrpr.getString("nm_pasien"), rsrawatinapdrpr.getString("kd_jenis_prw"), cekPotongan(rsrawatinapdrpr.getString("no_rawat"), rsrawatinapdrpr.getString("nm_perawatan")),
                            "Rawat Inap DrPr", bruto, sharing, jm, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsrawatinapdrpr.getString("kd_pj") + "'") + isCekCOB(rsrawatinapdrpr.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsrawatinapdrpr.getString("no_rawat") + "' LIMIT 1"), rsrawatinapdrpr.getString("kd_dokter"), getNamaDokter(rsrawatinapdrpr.getString("kd_dokter")), "Nota"
                        });
                        total = total + jm;
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
            }

            if (chkOperasi.isSelected() == true) {
                psbiayaoperator1 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator1,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.operator1 as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where reg_periksa.status_bayar='Belum Bayar' and operasi.operator1 like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayaoperator1>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.operator1) not in "
                        + "(select concat(bayar_operasi_operator1.no_rawat,bayar_operasi_operator1.kode_paket,bayar_operasi_operator1.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_operator1 on bayar_operasi_operator1.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayaoperator2 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator2,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.operator2 as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where reg_periksa.status_bayar='Belum Bayar' and operasi.operator2 like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayaoperator2>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.operator2) not in "
                        + "(select concat(bayar_operasi_operator2.no_rawat,bayar_operasi_operator2.kode_paket,bayar_operasi_operator2.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_operator2 on bayar_operasi_operator2.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayaoperator3 = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayaoperator3,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.operator3 as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where reg_periksa.status_bayar='Belum Bayar' and operasi.operator3 like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayaoperator3>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.operator3) not in "
                        + "(select concat(bayar_operasi_operator3.no_rawat,bayar_operasi_operator3.kode_paket,bayar_operasi_operator3.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_operator3 on bayar_operasi_operator3.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayadokter_anak = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayadokter_anak,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_anak as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where reg_periksa.status_bayar='Belum Bayar' and operasi.dokter_anak like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayadokter_anak>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_anak) not in "
                        + "(select concat(bayar_operasi_dokter_anak.no_rawat,bayar_operasi_dokter_anak.kode_paket,bayar_operasi_dokter_anak.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_anak on bayar_operasi_dokter_anak.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiaya_dokter_umum = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biaya_dokter_umum,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_umum as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where reg_periksa.status_bayar='Belum Bayar' and operasi.dokter_umum like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biaya_dokter_umum>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_umum) not in "
                        + "(select concat(bayar_operasi_dokter_umum.no_rawat,bayar_operasi_dokter_umum.kode_paket,bayar_operasi_dokter_umum.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_umum on bayar_operasi_dokter_umum.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiaya_dokter_pjanak = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biaya_dokter_pjanak,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_pjanak as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where reg_periksa.status_bayar='Belum Bayar' and operasi.dokter_pjanak like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biaya_dokter_pjanak>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_pjanak) not in "
                        + "(select concat(bayar_operasi_dokter_pjanak.no_rawat,bayar_operasi_dokter_pjanak.kode_paket,bayar_operasi_dokter_pjanak.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_pjanak on bayar_operasi_dokter_pjanak.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                psbiayadokter_anestesi = koneksi.prepareStatement(
                        "select pasien.nm_pasien,paket_operasi.nm_perawatan,operasi.biayadokter_anestesi,operasi.status,"
                        + "operasi.tgl_operasi,reg_periksa.kd_pj,operasi.kode_paket,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,operasi.dokter_anestesi as kd_dokter "
                        + "from operasi inner join reg_periksa on operasi.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join paket_operasi on operasi.kode_paket=paket_operasi.kode_paket "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where reg_periksa.status_bayar='Belum Bayar' and operasi.dokter_anestesi like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and operasi.biayadokter_anestesi>0 and "
                        + "concat(operasi.no_rawat,operasi.kode_paket,operasi.tgl_operasi,operasi.dokter_anestesi) not in "
                        + "(select concat(bayar_operasi_dokter_anestesi.no_rawat,bayar_operasi_dokter_anestesi.kode_paket,bayar_operasi_dokter_anestesi.tgl_operasi,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_operasi_dokter_anestesi on bayar_operasi_dokter_anestesi.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or paket_operasi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or operasi.tgl_operasi like ?)")
                        + "order by operasi.tgl_operasi,paket_operasi.nm_perawatan");
                try {
                    psbiayaoperator1.setString(1, getTargetDokter());
                    psbiayaoperator1.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayaoperator1.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator1.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayaoperator1 = psbiayaoperator1.executeQuery();

                    psbiayaoperator2.setString(1, getTargetDokter());
                    psbiayaoperator2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayaoperator2.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayaoperator2 = psbiayaoperator2.executeQuery();

                    psbiayaoperator3.setString(1, getTargetDokter());
                    psbiayaoperator3.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayaoperator3.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayaoperator3.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayaoperator3 = psbiayaoperator3.executeQuery();

                    psbiayadokter_anak.setString(1, getTargetDokter());
                    psbiayadokter_anak.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayadokter_anak.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anak.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayadokter_anak = psbiayadokter_anak.executeQuery();

                    psbiaya_dokter_umum.setString(1, getTargetDokter());
                    psbiaya_dokter_umum.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiaya_dokter_umum.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_umum.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiaya_dokter_umum = psbiaya_dokter_umum.executeQuery();

                    psbiaya_dokter_pjanak.setString(1, getTargetDokter());
                    psbiaya_dokter_pjanak.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiaya_dokter_pjanak.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiaya_dokter_pjanak.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiaya_dokter_pjanak = psbiaya_dokter_pjanak.executeQuery();

                    psbiayadokter_anestesi.setString(1, getTargetDokter());
                    psbiayadokter_anestesi.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psbiayadokter_anestesi.setString(3, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(4, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(5, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(6, "%" + TCari.getText().trim() + "%");
                        psbiayadokter_anestesi.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsbiayadokter_anestesi = psbiayadokter_anestesi.executeQuery();

                    while (rsbiayaoperator1.next()) {
                        String tglOpOp1 = rsbiayaoperator1.getString("tgl_operasi");
                        double tarifMurniOp1 = rsbiayaoperator1.getDouble("biayaoperator1");
                        double[] calcOp1 = hitungJMOperasiPotongan(rsbiayaoperator1.getString("no_rawat"), rsbiayaoperator1.getString("nm_perawatan"), rsbiayaoperator1.getString("kd_dokter"), tarifMurniOp1, rsbiayaoperator1.getString("kode_paket"), tglOpOp1);
                        double brutoOp1 = calcOp1[0];
                        double sharingOp1 = calcOp1[1];
                        double jmOp1 = calcOp1[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpOp1), getJamOnly(tglOpOp1), rsbiayaoperator1.getString("no_rawat"), rsbiayaoperator1.getString("no_rkm_medis"),
                            rsbiayaoperator1.getString("nm_pasien"), rsbiayaoperator1.getString("kode_paket"), cekPotongan(rsbiayaoperator1.getString("no_rawat"), rsbiayaoperator1.getString("nm_perawatan"), rsbiayaoperator1.getString("kode_paket"), tglOpOp1, rsbiayaoperator1.getString("kd_dokter")) + "(Operator 1)",
                            "Operasi " + rsbiayaoperator1.getString("status") + " Op1", brutoOp1, sharingOp1, jmOp1, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayaoperator1.getString("kd_pj") + "'") + isCekCOB(rsbiayaoperator1.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayaoperator1.getString("no_rawat") + "' LIMIT 1"), rsbiayaoperator1.getString("kd_dokter"), getNamaDokter(rsbiayaoperator1.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmOp1;
                    }

                    while (rsbiayaoperator2.next()) {
                        String tglOpOp2 = rsbiayaoperator2.getString("tgl_operasi");
                        double tarifMurniOp2 = rsbiayaoperator2.getDouble("biayaoperator2");
                        double[] calcOp2 = hitungJMOperasiPotongan(rsbiayaoperator2.getString("no_rawat"), rsbiayaoperator2.getString("nm_perawatan"), rsbiayaoperator2.getString("kd_dokter"), tarifMurniOp2, rsbiayaoperator2.getString("kode_paket"), tglOpOp2);
                        double brutoOp2 = calcOp2[0];
                        double sharingOp2 = calcOp2[1];
                        double jmOp2 = calcOp2[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpOp2), getJamOnly(tglOpOp2), rsbiayaoperator2.getString("no_rawat"), rsbiayaoperator2.getString("no_rkm_medis"),
                            rsbiayaoperator2.getString("nm_pasien"), rsbiayaoperator2.getString("kode_paket"), cekPotongan(rsbiayaoperator2.getString("no_rawat"), rsbiayaoperator2.getString("nm_perawatan"), rsbiayaoperator2.getString("kode_paket"), tglOpOp2, rsbiayaoperator2.getString("kd_dokter")) + "(Operator 2)",
                            "Operasi " + rsbiayaoperator2.getString("status") + " Op2", brutoOp2, sharingOp2, jmOp2, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayaoperator2.getString("kd_pj") + "'") + isCekCOB(rsbiayaoperator2.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayaoperator2.getString("no_rawat") + "' LIMIT 1"), rsbiayaoperator2.getString("kd_dokter"), getNamaDokter(rsbiayaoperator2.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmOp2;
                    }

                    while (rsbiayaoperator3.next()) {
                        String tglOpOp3 = rsbiayaoperator3.getString("tgl_operasi");
                        double tarifMurniOp3 = rsbiayaoperator3.getDouble("biayaoperator3");
                        double[] calcOp3 = hitungJMOperasiPotongan(rsbiayaoperator3.getString("no_rawat"), rsbiayaoperator3.getString("nm_perawatan"), rsbiayaoperator3.getString("kd_dokter"), tarifMurniOp3, rsbiayaoperator3.getString("kode_paket"), tglOpOp3);
                        double brutoOp3 = calcOp3[0];
                        double sharingOp3 = calcOp3[1];
                        double jmOp3 = calcOp3[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpOp3), getJamOnly(tglOpOp3), rsbiayaoperator3.getString("no_rawat"), rsbiayaoperator3.getString("no_rkm_medis"),
                            rsbiayaoperator3.getString("nm_pasien"), rsbiayaoperator3.getString("kode_paket"), cekPotongan(rsbiayaoperator3.getString("no_rawat"), rsbiayaoperator3.getString("nm_perawatan"), rsbiayaoperator3.getString("kode_paket"), tglOpOp3, rsbiayaoperator3.getString("kd_dokter")) + "(Operator 3)",
                            "Operasi " + rsbiayaoperator3.getString("status") + " Op3", brutoOp3, sharingOp3, jmOp3, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayaoperator3.getString("kd_pj") + "'") + isCekCOB(rsbiayaoperator3.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayaoperator3.getString("no_rawat") + "' LIMIT 1"), rsbiayaoperator3.getString("kd_dokter"), getNamaDokter(rsbiayaoperator3.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmOp3;
                    }

                    while (rsbiayadokter_anak.next()) {
                        String tglOpDrAnak = rsbiayadokter_anak.getString("tgl_operasi");
                        double tarifMurniDrAnak = rsbiayadokter_anak.getDouble("biayadokter_anak");
                        double[] calcDrAnak = hitungJMOperasiPotongan(rsbiayadokter_anak.getString("no_rawat"), rsbiayadokter_anak.getString("nm_perawatan"), rsbiayadokter_anak.getString("kd_dokter"), tarifMurniDrAnak, rsbiayadokter_anak.getString("kode_paket"), tglOpDrAnak);
                        double brutoDrAnak = calcDrAnak[0];
                        double sharingDrAnak = calcDrAnak[1];
                        double jmDrAnak = calcDrAnak[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrAnak), getJamOnly(tglOpDrAnak), rsbiayadokter_anak.getString("no_rawat"), rsbiayadokter_anak.getString("no_rkm_medis"),
                            rsbiayadokter_anak.getString("nm_pasien"), rsbiayadokter_anak.getString("kode_paket"), cekPotongan(rsbiayadokter_anak.getString("no_rawat"), rsbiayadokter_anak.getString("nm_perawatan"), rsbiayadokter_anak.getString("kode_paket"), tglOpDrAnak, rsbiayadokter_anak.getString("kd_dokter")) + "(dr Anak)",
                            "Operasi " + rsbiayadokter_anak.getString("status") + " dr Anak", brutoDrAnak, sharingDrAnak, jmDrAnak, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayadokter_anak.getString("kd_pj") + "'") + isCekCOB(rsbiayadokter_anak.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayadokter_anak.getString("no_rawat") + "' LIMIT 1"), rsbiayadokter_anak.getString("kd_dokter"), getNamaDokter(rsbiayadokter_anak.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrAnak;
                    }

                    while (rsbiayadokter_anestesi.next()) {
                        String tglOpDrAnestesi = rsbiayadokter_anestesi.getString("tgl_operasi");
                        double tarifMurniDrAnestesi = rsbiayadokter_anestesi.getDouble("biayadokter_anestesi");
                        double[] calcDrAnestesi = hitungJMOperasiPotongan(rsbiayadokter_anestesi.getString("no_rawat"), rsbiayadokter_anestesi.getString("nm_perawatan"), rsbiayadokter_anestesi.getString("kd_dokter"), tarifMurniDrAnestesi, rsbiayadokter_anestesi.getString("kode_paket"), tglOpDrAnestesi);
                        double brutoDrAnestesi = calcDrAnestesi[0];
                        double sharingDrAnestesi = calcDrAnestesi[1];
                        double jmDrAnestesi = calcDrAnestesi[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrAnestesi), getJamOnly(tglOpDrAnestesi), rsbiayadokter_anestesi.getString("no_rawat"), rsbiayadokter_anestesi.getString("no_rkm_medis"),
                            rsbiayadokter_anestesi.getString("nm_pasien"), rsbiayadokter_anestesi.getString("kode_paket"), cekPotongan(rsbiayadokter_anestesi.getString("no_rawat"), rsbiayadokter_anestesi.getString("nm_perawatan"), rsbiayadokter_anestesi.getString("kode_paket"), tglOpDrAnestesi, rsbiayadokter_anestesi.getString("kd_dokter")) + "(dr Anestesi)",
                            "Operasi " + rsbiayadokter_anestesi.getString("status") + " dr Anastesi", brutoDrAnestesi, sharingDrAnestesi, jmDrAnestesi, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiayadokter_anestesi.getString("kd_pj") + "'") + isCekCOB(rsbiayadokter_anestesi.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiayadokter_anestesi.getString("no_rawat") + "' LIMIT 1"), rsbiayadokter_anestesi.getString("kd_dokter"), getNamaDokter(rsbiayadokter_anestesi.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrAnestesi;
                    }

                    while (rsbiaya_dokter_pjanak.next()) {
                        String tglOpDrPjAnak = rsbiaya_dokter_pjanak.getString("tgl_operasi");
                        double tarifMurniDrPjAnak = rsbiaya_dokter_pjanak.getDouble("biaya_dokter_pjanak");
                        double[] calcDrPjAnak = hitungJMOperasiPotongan(rsbiaya_dokter_pjanak.getString("no_rawat"), rsbiaya_dokter_pjanak.getString("nm_perawatan"), rsbiaya_dokter_pjanak.getString("kd_dokter"), tarifMurniDrPjAnak, rsbiaya_dokter_pjanak.getString("kode_paket"), tglOpDrPjAnak);
                        double brutoDrPjAnak = calcDrPjAnak[0];
                        double sharingDrPjAnak = calcDrPjAnak[1];
                        double jmDrPjAnak = calcDrPjAnak[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrPjAnak), getJamOnly(tglOpDrPjAnak), rsbiaya_dokter_pjanak.getString("no_rawat"), rsbiaya_dokter_pjanak.getString("no_rkm_medis"),
                            rsbiaya_dokter_pjanak.getString("nm_pasien"), rsbiaya_dokter_pjanak.getString("kode_paket"), cekPotongan(rsbiaya_dokter_pjanak.getString("no_rawat"), rsbiaya_dokter_pjanak.getString("nm_perawatan"), rsbiaya_dokter_pjanak.getString("kode_paket"), tglOpDrPjAnak, rsbiaya_dokter_pjanak.getString("kd_dokter")) + "(dr Pj Anak)",
                            "Operasi " + rsbiaya_dokter_pjanak.getString("status") + " dr PJ Anak", brutoDrPjAnak, sharingDrPjAnak, jmDrPjAnak, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiaya_dokter_pjanak.getString("kd_pj") + "'") + isCekCOB(rsbiaya_dokter_pjanak.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiaya_dokter_pjanak.getString("no_rawat") + "' LIMIT 1"), rsbiaya_dokter_pjanak.getString("kd_dokter"), getNamaDokter(rsbiaya_dokter_pjanak.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrPjAnak;
                    }

                    while (rsbiaya_dokter_umum.next()) {
                        String tglOpDrUmum = rsbiaya_dokter_umum.getString("tgl_operasi");
                        double tarifMurniDrUmum = rsbiaya_dokter_umum.getDouble("biaya_dokter_umum");
                        double[] calcDrUmum = hitungJMOperasiPotongan(rsbiaya_dokter_umum.getString("no_rawat"), rsbiaya_dokter_umum.getString("nm_perawatan"), rsbiaya_dokter_umum.getString("kd_dokter"), tarifMurniDrUmum, rsbiaya_dokter_umum.getString("kode_paket"), tglOpDrUmum);
                        double brutoDrUmum = calcDrUmum[0];
                        double sharingDrUmum = calcDrUmum[1];
                        double jmDrUmum = calcDrUmum[2];
                        tabMode.addRow(new Object[]{
                            false, getTglOnly(tglOpDrUmum), getJamOnly(tglOpDrUmum), rsbiaya_dokter_umum.getString("no_rawat"), rsbiaya_dokter_umum.getString("no_rkm_medis"),
                            rsbiaya_dokter_umum.getString("nm_pasien"), rsbiaya_dokter_umum.getString("kode_paket"), cekPotongan(rsbiaya_dokter_umum.getString("no_rawat"), rsbiaya_dokter_umum.getString("nm_perawatan"), rsbiaya_dokter_umum.getString("kode_paket"), tglOpDrUmum, rsbiaya_dokter_umum.getString("kd_dokter")) + "(dr Umum)",
                            "Operasi " + rsbiaya_dokter_umum.getString("status") + " dr Umum", brutoDrUmum, sharingDrUmum, jmDrUmum, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsbiaya_dokter_umum.getString("kd_pj") + "'") + isCekCOB(rsbiaya_dokter_umum.getString("no_rawat")), Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsbiaya_dokter_umum.getString("no_rawat") + "' LIMIT 1"), rsbiaya_dokter_umum.getString("kd_dokter"), getNamaDokter(rsbiaya_dokter_umum.getString("kd_dokter")), "Nota"
                        });
                        total = total + jmDrUmum;
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
                    if (rsbiayadokter_anestesi != null) {
                        rsbiayadokter_anestesi.close();
                    }
                    if (psbiayadokter_anestesi != null) {
                        psbiayadokter_anestesi.close();
                    }
                }
            }

            if (chkLaborat.isSelected() == true) {
                //periksa lab
                psperiksa_lab = koneksi.prepareStatement(
                        "select periksa_lab.tarif_tindakan_dokter,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_lab.status,"
                        + "jns_perawatan_lab.nm_perawatan,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,reg_periksa.kd_pj,periksa_lab.kd_dokter "
                        + " from periksa_lab inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " where reg_periksa.status_bayar='Belum Bayar' and periksa_lab.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_lab.tarif_tindakan_dokter>0 and "
                        + " concat(periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.kd_dokter) not in "
                        + " (select concat(bayar_periksa_lab.no_rawat,bayar_periksa_lab.kd_jenis_prw,bayar_periksa_lab.tgl_periksa,bayar_periksa_lab.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_lab on bayar_periksa_lab.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or  jns_perawatan_lab.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam,jns_perawatan_lab.nm_perawatan  ");
                try {
                    psperiksa_lab.setString(1, getTargetDokter());
                    psperiksa_lab.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_lab.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_lab = psperiksa_lab.executeQuery();

                    while (rsperiksa_lab.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_lab.getString("kd_dokter"), rsperiksa_lab.getDouble("tarif_tindakan_dokter"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_lab.getString("tgl_periksa"), rsperiksa_lab.getString("jam"), rsperiksa_lab.getString("no_rawat"), rsperiksa_lab.getString("no_rkm_medis"),
                            rsperiksa_lab.getString("nm_pasien") + " (" + rsperiksa_lab.getString("kd_pj") + ")", rsperiksa_lab.getString("kd_jenis_prw"), rsperiksa_lab.getString("nm_perawatan"),
                            "Laborat " + rsperiksa_lab.getString("status") + " PJ", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_lab.getString("kd_pj") + "'") + isCekCOB(rsperiksa_lab.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsperiksa_lab.getString("no_rawat") + "' LIMIT 1"), rsperiksa_lab.getString("kd_dokter"), getNamaDokter(rsperiksa_lab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Lab : " + e);
                } finally {
                    if (rsperiksa_lab != null) {
                        rsperiksa_lab.close();
                    }
                    if (psperiksa_lab != null) {
                        psperiksa_lab.close();
                    }
                }

                //detail periksa lab
                psdetaillab = koneksi.prepareStatement(
                        "select detail_periksa_lab.bagian_dokter,pasien.nm_pasien,periksa_lab.status,detail_periksa_lab.id_template,"
                        + "template_laboratorium.Pemeriksaan,reg_periksa.kd_pj,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                        + "periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.kd_jenis_prw,periksa_lab.kd_dokter "
                        + "from detail_periksa_lab inner join periksa_lab on periksa_lab.no_rawat=detail_periksa_lab.no_rawat "
                        + "and periksa_lab.kd_jenis_prw=detail_periksa_lab.kd_jenis_prw "
                        + "and periksa_lab.tgl_periksa=detail_periksa_lab.tgl_periksa "
                        + "and periksa_lab.jam=detail_periksa_lab.jam "
                        + "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where reg_periksa.status_bayar='Belum Bayar' and periksa_lab.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and detail_periksa_lab.bagian_dokter>0 and "
                        + "concat(detail_periksa_lab.no_rawat,detail_periksa_lab.kd_jenis_prw,detail_periksa_lab.tgl_periksa,detail_periksa_lab.jam,detail_periksa_lab.id_template,periksa_lab.kd_dokter) not in "
                        + "(select concat(bayar_detail_periksa_lab.no_rawat,bayar_detail_periksa_lab.kd_jenis_prw,bayar_detail_periksa_lab.tgl_periksa,bayar_detail_periksa_lab.jam,bayar_detail_periksa_lab.id_template,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_detail_periksa_lab on bayar_detail_periksa_lab.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or template_laboratorium.Pemeriksaan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam");
                try {
                    psdetaillab.setString(1, getTargetDokter());
                    psdetaillab.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psdetaillab.setString(3, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(4, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(5, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(6, "%" + TCari.getText().trim() + "%");
                        psdetaillab.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsdetaillab = psdetaillab.executeQuery();

                    while (rsdetaillab.next()) {
                        double jmTariff = hitungJMDokter(rsdetaillab.getString("kd_dokter"), rsdetaillab.getDouble("bagian_dokter"));
                        tabMode.addRow(new Object[]{
                            false, rsdetaillab.getString("tgl_periksa"), rsdetaillab.getString("jam"), rsdetaillab.getString("no_rawat"), rsdetaillab.getString("no_rkm_medis"),
                            rsdetaillab.getString("nm_pasien") + " (" + rsdetaillab.getString("kd_pj") + ")", rsdetaillab.getString("kd_jenis_prw"), rsdetaillab.getString("Pemeriksaan"),
                            "Laborat " + rsdetaillab.getString("status") + " PJ Detail", currentBruto, currentSharing, jmTariff, rsdetaillab.getString("id_template"), Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsdetaillab.getString("kd_pj") + "'") + isCekCOB(rsdetaillab.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsdetaillab.getString("no_rawat") + "' LIMIT 1"), rsdetaillab.getString("kd_dokter"), getNamaDokter(rsdetaillab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Detail Lab : " + e);
                } finally {
                    if (rsdetaillab != null) {
                        rsdetaillab.close();
                    }
                    if (psdetaillab != null) {
                        psdetaillab.close();
                    }
                }

                //periksa lab perujuk
                psperiksa_lab2 = koneksi.prepareStatement(
                        "select periksa_lab.tarif_perujuk,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_lab.status,"
                        + "jns_perawatan_lab.nm_perawatan,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,reg_periksa.kd_pj,periksa_lab.dokter_perujuk as kd_dokter "
                        + " from periksa_lab inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_lab on periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " where reg_periksa.status_bayar='Belum Bayar' and periksa_lab.dokter_perujuk like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_lab.tarif_perujuk>0 and "
                        + " concat(periksa_lab.no_rawat,periksa_lab.kd_jenis_prw,periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.dokter_perujuk) not in "
                        + " (select concat(bayar_periksa_lab_perujuk.no_rawat,bayar_periksa_lab_perujuk.kd_jenis_prw,bayar_periksa_lab_perujuk.tgl_periksa,bayar_periksa_lab_perujuk.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_lab_perujuk on bayar_periksa_lab_perujuk.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or  jns_perawatan_lab.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam,jns_perawatan_lab.nm_perawatan  ");
                try {
                    psperiksa_lab2.setString(1, getTargetDokter());
                    psperiksa_lab2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_lab2.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_lab2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_lab = psperiksa_lab2.executeQuery();

                    while (rsperiksa_lab.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_lab.getString("kd_dokter"), rsperiksa_lab.getDouble("tarif_perujuk"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_lab.getString("tgl_periksa"), rsperiksa_lab.getString("jam"), rsperiksa_lab.getString("no_rawat"), rsperiksa_lab.getString("no_rkm_medis"),
                            rsperiksa_lab.getString("nm_pasien") + " (" + rsperiksa_lab.getString("kd_pj") + ")", rsperiksa_lab.getString("kd_jenis_prw"), rsperiksa_lab.getString("nm_perawatan"),
                            "Laborat " + rsperiksa_lab.getString("status") + " Perujuk", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_lab.getString("kd_pj") + "'") + isCekCOB(rsperiksa_lab.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsperiksa_lab.getString("no_rawat") + "' LIMIT 1"), rsperiksa_lab.getString("kd_dokter"), getNamaDokter(rsperiksa_lab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Perujuk Lab : " + e);
                } finally {
                    if (rsperiksa_lab != null) {
                        rsperiksa_lab.close();
                    }
                    if (psperiksa_lab2 != null) {
                        psperiksa_lab2.close();
                    }
                }

                psdetaillab2 = koneksi.prepareStatement(
                        "select detail_periksa_lab.bagian_perujuk,pasien.nm_pasien,periksa_lab.status,detail_periksa_lab.id_template,"
                        + "template_laboratorium.Pemeriksaan,reg_periksa.kd_pj,reg_periksa.no_rawat,reg_periksa.no_rkm_medis, "
                        + "periksa_lab.tgl_periksa,periksa_lab.jam,periksa_lab.kd_jenis_prw,periksa_lab.dokter_perujuk as kd_dokter "
                        + "from detail_periksa_lab inner join periksa_lab on periksa_lab.no_rawat=detail_periksa_lab.no_rawat "
                        + "and periksa_lab.kd_jenis_prw=detail_periksa_lab.kd_jenis_prw "
                        + "and periksa_lab.tgl_periksa=detail_periksa_lab.tgl_periksa "
                        + "and periksa_lab.jam=detail_periksa_lab.jam "
                        + "inner join reg_periksa on periksa_lab.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join template_laboratorium on detail_periksa_lab.id_template=template_laboratorium.id_template "
                        + "inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + "where reg_periksa.status_bayar='Belum Bayar' and periksa_lab.dokter_perujuk like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and detail_periksa_lab.bagian_perujuk>0 and "
                        + "concat(detail_periksa_lab.no_rawat,detail_periksa_lab.kd_jenis_prw,detail_periksa_lab.tgl_periksa,detail_periksa_lab.jam,detail_periksa_lab.id_template,periksa_lab.dokter_perujuk) not in "
                        + "(select concat(bayar_detail_periksa_lab_perujuk.no_rawat,bayar_detail_periksa_lab_perujuk.kd_jenis_prw,bayar_detail_periksa_lab_perujuk.tgl_periksa,bayar_detail_periksa_lab_perujuk.jam,bayar_detail_periksa_lab_perujuk.id_template,bayar_jm_dokter.kd_dokter) "
                        + "from bayar_jm_dokter inner join bayar_detail_periksa_lab_perujuk on bayar_detail_periksa_lab_perujuk.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or template_laboratorium.Pemeriksaan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_lab.tgl_periksa like ?)")
                        + "order by periksa_lab.tgl_periksa,periksa_lab.jam");
                try {
                    psdetaillab2.setString(1, getTargetDokter());
                    psdetaillab2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psdetaillab2.setString(3, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(4, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(5, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(6, "%" + TCari.getText().trim() + "%");
                        psdetaillab2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsdetaillab = psdetaillab2.executeQuery();

                    while (rsdetaillab.next()) {
                        double jmTariff = hitungJMDokter(rsdetaillab.getString("kd_dokter"), rsdetaillab.getDouble("bagian_perujuk"));
                        tabMode.addRow(new Object[]{
                            false, rsdetaillab.getString("tgl_periksa"), rsdetaillab.getString("jam"), rsdetaillab.getString("no_rawat"), rsdetaillab.getString("no_rkm_medis"),
                            rsdetaillab.getString("nm_pasien") + " (" + rsdetaillab.getString("kd_pj") + ")", rsdetaillab.getString("kd_jenis_prw"), rsdetaillab.getString("Pemeriksaan"),
                            "Laborat " + rsdetaillab.getString("status") + " Perujuk Detail", currentBruto, currentSharing, jmTariff, rsdetaillab.getString("id_template"), Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsdetaillab.getString("kd_pj") + "'") + isCekCOB(rsdetaillab.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsdetaillab.getString("no_rawat") + "' LIMIT 1"), rsdetaillab.getString("kd_dokter"), getNamaDokter(rsdetaillab.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Detail Perujuk : " + e);
                } finally {
                    if (rsdetaillab != null) {
                        rsdetaillab.close();
                    }
                    if (psdetaillab2 != null) {
                        psdetaillab2.close();
                    }
                }
            }

            if (chkRadiologi.isSelected() == true) {
                //periksa radiologi
                psperiksa_radiologi = koneksi.prepareStatement("select periksa_radiologi.tarif_tindakan_dokter,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_radiologi.status,"
                        + "jns_perawatan_radiologi.nm_perawatan,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,reg_periksa.kd_pj,periksa_radiologi.kd_dokter "
                        + " from periksa_radiologi inner join reg_periksa on periksa_radiologi.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_radiologi on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " where reg_periksa.status_bayar='Belum Bayar' and periksa_radiologi.kd_dokter like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_radiologi.tarif_tindakan_dokter>0 and"
                        + " concat(periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.kd_dokter) not in "
                        + " (select concat(bayar_periksa_radiologi.no_rawat,bayar_periksa_radiologi.kd_jenis_prw,bayar_periksa_radiologi.tgl_periksa,bayar_periksa_radiologi.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_radiologi on bayar_periksa_radiologi.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_radiologi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_radiologi.tgl_periksa like ?)")
                        + "order by periksa_radiologi.tgl_periksa,periksa_radiologi.jam,jns_perawatan_radiologi.nm_perawatan  ");
                try {
                    psperiksa_radiologi.setString(1, getTargetDokter());
                    psperiksa_radiologi.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_radiologi.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_radiologi = psperiksa_radiologi.executeQuery();

                    while (rsperiksa_radiologi.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_radiologi.getString("kd_dokter"), rsperiksa_radiologi.getDouble("tarif_tindakan_dokter"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_radiologi.getString("tgl_periksa"), rsperiksa_radiologi.getString("jam"), rsperiksa_radiologi.getString("no_rawat"), rsperiksa_radiologi.getString("no_rkm_medis"),
                            rsperiksa_radiologi.getString("nm_pasien"), rsperiksa_radiologi.getString("kd_jenis_prw"), rsperiksa_radiologi.getString("nm_perawatan"),
                            "Radiologi " + rsperiksa_radiologi.getString("status") + " PJ", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_radiologi.getString("kd_pj") + "'") + isCekCOB(rsperiksa_radiologi.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsperiksa_radiologi.getString("no_rawat") + "' LIMIT 1"), rsperiksa_radiologi.getString("kd_dokter"), getNamaDokter(rsperiksa_radiologi.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Radiologi : " + e);
                } finally {
                    if (rsperiksa_radiologi != null) {
                        rsperiksa_radiologi.close();
                    }
                    if (psperiksa_radiologi != null) {
                        psperiksa_radiologi.close();
                    }
                }

                //periksa radiologi
                psperiksa_radiologi2 = koneksi.prepareStatement("select periksa_radiologi.tarif_perujuk,pasien.nm_pasien,reg_periksa.no_rawat,reg_periksa.no_rkm_medis,periksa_radiologi.status,"
                        + "jns_perawatan_radiologi.nm_perawatan,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,reg_periksa.kd_pj,periksa_radiologi.dokter_perujuk as kd_dokter "
                        + " from periksa_radiologi inner join reg_periksa on periksa_radiologi.no_rawat=reg_periksa.no_rawat "
                        + " inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + " inner join jns_perawatan_radiologi on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "
                        + " inner join penjab on reg_periksa.kd_pj=penjab.kd_pj "
                        + " where reg_periksa.status_bayar='Belum Bayar' and periksa_radiologi.dokter_perujuk like ? and reg_periksa.kd_pj!='BPJ' AND concat(reg_periksa.kd_pj,penjab.png_jawab) like ? and periksa_radiologi.tarif_perujuk>0 and "
                        + " concat(periksa_radiologi.no_rawat,periksa_radiologi.kd_jenis_prw,periksa_radiologi.tgl_periksa,periksa_radiologi.jam,periksa_radiologi.dokter_perujuk) not in "
                        + " (select concat(bayar_periksa_radiologi_perujuk.no_rawat,bayar_periksa_radiologi_perujuk.kd_jenis_prw,bayar_periksa_radiologi_perujuk.tgl_periksa,bayar_periksa_radiologi_perujuk.jam,bayar_jm_dokter.kd_dokter) "
                        + " from bayar_jm_dokter inner join bayar_periksa_radiologi_perujuk on bayar_periksa_radiologi_perujuk.no_bayar=bayar_jm_dokter.no_bayar) " + (TCari.getText().trim().equals("") ? ""
                        : " and (pasien.nm_pasien like ? or jns_perawatan_radiologi.nm_perawatan like ? or reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or periksa_radiologi.tgl_periksa like ?)")
                        + "order by periksa_radiologi.tgl_periksa,periksa_radiologi.jam,jns_perawatan_radiologi.nm_perawatan  ");
                try {
                    psperiksa_radiologi2.setString(1, getTargetDokter());
                    psperiksa_radiologi2.setString(2, "%" + KdCaraBayar.getText() + NmCaraBayar.getText() + "%");
                    if (!TCari.getText().trim().equals("")) {
                        psperiksa_radiologi2.setString(3, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(4, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(5, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(6, "%" + TCari.getText().trim() + "%");
                        psperiksa_radiologi2.setString(7, "%" + TCari.getText().trim() + "%");
                    }
                    rsperiksa_radiologi = psperiksa_radiologi2.executeQuery();

                    while (rsperiksa_radiologi.next()) {
                        double jmTariff = hitungJMDokter(rsperiksa_radiologi.getString("kd_dokter"), rsperiksa_radiologi.getDouble("tarif_perujuk"));
                        tabMode.addRow(new Object[]{
                            false, rsperiksa_radiologi.getString("tgl_periksa"), rsperiksa_radiologi.getString("jam"), rsperiksa_radiologi.getString("no_rawat"), rsperiksa_radiologi.getString("no_rkm_medis"),
                            rsperiksa_radiologi.getString("nm_pasien"), rsperiksa_radiologi.getString("kd_jenis_prw"), rsperiksa_radiologi.getString("nm_perawatan"),
                            "Radiologi " + rsperiksa_radiologi.getString("status") + " Perujuk", currentBruto, currentSharing, jmTariff, "", Sequel.cariIsi("select png_jawab from penjab where kd_pj='" + rsperiksa_radiologi.getString("kd_pj") + "'") + isCekCOB(rsperiksa_radiologi.getString("no_rawat")),
                            Sequel.cariIsi("select tgl_byr from billing where no_rawat='" + rsperiksa_radiologi.getString("no_rawat") + "' LIMIT 1"), rsperiksa_radiologi.getString("kd_dokter"), getNamaDokter(rsperiksa_radiologi.getString("kd_dokter")), "Nota"});
                        total = total + jmTariff;
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Perujuk Radiologi : " + e);
                } finally {
                    if (rsperiksa_radiologi != null) {
                        rsperiksa_radiologi.close();
                    }
                    if (psperiksa_radiologi2 != null) {
                        psperiksa_radiologi2.close();
                    }
                }
            }

            LCount.setText(Valid.SetAngka(total));
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    public String isCekCOB(String norawat) {
        if (norawat == null || norawat.trim().isEmpty()) {
            return "";
        }
        StringBuilder hasilCOB = new StringBuilder();
        int rowCount = 0;
        boolean hasBPJ = false;
        PreparedStatement pscekCOB = null;
        ResultSet rsCekCOB = null;

        try {
            pscekCOB = koneksi.prepareStatement(
                    "SELECT nama_bayar, kd_pj FROM detail_piutang_pasien WHERE no_rawat = ? ORDER BY kd_pj ASC"
            );
            pscekCOB.setString(1, norawat);
            rsCekCOB = pscekCOB.executeQuery();

            while (rsCekCOB.next()) {
                String nb = rsCekCOB.getString("nama_bayar");
                String kp = rsCekCOB.getString("kd_pj");
                if (kp == null) kp = "";
                if (nb == null) nb = "";

                // KETAT: Hanya kode penjamin 'BPJ' (BPJS Kesehatan / JKN). BPJS TK / Ketenagakerjaan BUKAN BPJ!
                if (kp.trim().equalsIgnoreCase("BPJ")) {
                    hasBPJ = true;
                }

                if (rowCount > 0) {
                    hasilCOB.append(" | ");
                }
                hasilCOB.append(nb.isEmpty() ? kp : nb);
                rowCount++;
            }

            // Return label COB JKN jika ada BPJ, atau COB biasa jika non-BPJ. Jika bukan COB (< 2 penjamin), return string kosong murni.
            if (rowCount >= 2) {
                return hasBPJ ? (" COB JKN " + hasilCOB.toString()) : (" COB " + hasilCOB.toString());
            } else {
                return "";
            }

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

    /**
     * Menampilkan info potongan untuk kolom tindakan di tabel. Prioritas:
     * diskon_ibs (exact match) → pengurangan_biaya (LIKE, hanya baris pertama
     * per kode_paket per no_rawat)
     */
    private String cekPotongan(String norawat, String namatindakan, String kodePaket, String tglOperasi) {
        return cekPotongan(norawat, namatindakan, kodePaket, tglOperasi, null);
    }

    private String cekPotongan(String norawat, String namatindakan, String kodePaket, String tglOperasi, String kdDokter) {
        if (namatindakan == null) {
            return "";
        }
        if (norawat == null || norawat.trim().isEmpty()) {
            return namatindakan;
        }

        String cleanTgl = (tglOperasi != null ? tglOperasi.trim() : "");
        if (cleanTgl.endsWith(".0")) {
            cleanTgl = cleanTgl.substring(0, cleanTgl.length() - 2);
        }
        String kdDokterTrimmed = (kdDokter == null) ? "" : kdDokter.trim();

        // --- Prioritas 1: cek diskon_ibs_detail (Potongan Komponen Dokter) ---
        if (kodePaket != null && !kodePaket.trim().isEmpty() && !kdDokterTrimmed.isEmpty()) {
            PreparedStatement psDetail = null;
            ResultSet rsDetail = null;
            try {
                boolean foundDetail = false;
                double besarDiskon = 0;
                double diskonPersen = 0;

                // 1. Coba exact match per tgl_operasi
                if (!cleanTgl.isEmpty()) {
                    psDetail = koneksi.prepareStatement(
                            "SELECT besar_diskon, diskon_persen FROM diskon_ibs_detail WHERE no_rawat = ? AND (tgl_operasi = ? OR tgl_operasi = ?) AND kode_paket = ? AND (kd_dokter = ? OR kd_dokter = ?)"
                    );
                    psDetail.setString(1, norawat.trim());
                    psDetail.setString(2, cleanTgl);
                    psDetail.setString(3, cleanTgl + ".0");
                    psDetail.setString(4, kodePaket.trim());
                    psDetail.setString(5, kdDokterTrimmed);
                    psDetail.setString(6, kdDokter != null ? kdDokter : kdDokterTrimmed);
                    rsDetail = psDetail.executeQuery();
                    if (rsDetail.next()) {
                        besarDiskon = rsDetail.getDouble("besar_diskon");
                        diskonPersen = rsDetail.getDouble("diskon_persen");
                        foundDetail = true;
                    }
                    rsDetail.close();
                    psDetail.close();
                }

                // 2. Fallback pencocokan tanpa tgl_operasi jika belum ketemu
                if (!foundDetail) {
                    psDetail = koneksi.prepareStatement(
                            "SELECT besar_diskon, diskon_persen FROM diskon_ibs_detail WHERE no_rawat = ? AND kode_paket = ? AND (kd_dokter = ? OR kd_dokter = ?) ORDER BY tgl_operasi ASC"
                    );
                    psDetail.setString(1, norawat.trim());
                    psDetail.setString(2, kodePaket.trim());
                    psDetail.setString(3, kdDokterTrimmed);
                    psDetail.setString(4, kdDokter != null ? kdDokter : kdDokterTrimmed);
                    rsDetail = psDetail.executeQuery();
                    if (rsDetail.next()) {
                        besarDiskon = rsDetail.getDouble("besar_diskon");
                        diskonPersen = rsDetail.getDouble("diskon_persen");
                        foundDetail = true;
                    }
                    rsDetail.close();
                    psDetail.close();
                }

                if (foundDetail) {
                    if (besarDiskon > 0) {
                        String strPersen = (diskonPersen > 0) ? (" [" + (diskonPersen == (long) diskonPersen ? String.format("%d", (long) diskonPersen) : String.format("%.2f", diskonPersen)) + "%]") : "";
                        return namatindakan.trim() + " | Diskon IBS: Rp " + Valid.SetAngka(besarDiskon) + strPersen;
                    } else {
                        return namatindakan.trim();
                    }
                }
            } catch (Exception ignored) {
            } finally {
                if (rsDetail != null) try { rsDetail.close(); } catch (Exception ignored) {}
                if (psDetail != null) try { psDetail.close(); } catch (Exception ignored) {}
            }
        }

        // --- Prioritas 2: Fallback ke logika lama diskon_ibs ---
        // Kunci unik per no_rawat + kode_paket / namatindakan + tgl_operasi untuk membedakan multi-tindakan pada tanggal/jam berbeda
        String setKey = norawat.trim() + "|"
                + (kodePaket != null && !kodePaket.trim().isEmpty() ? kodePaket.trim() : namatindakan.trim())
                + (!cleanTgl.isEmpty() ? "|" + cleanTgl : "");

        if (sdahTampilDiskon.contains(setKey)) {
            // Sudah pernah ditampilkan pada peran dokter sebelumnya dalam operasi ini
            return namatindakan;
        }

        StringBuilder hasilPotongan = new StringBuilder();
        boolean adaDiskonIbs = false;

        // --- Prioritas 1: cek diskon_ibs ---
        if (kodePaket != null && !kodePaket.trim().isEmpty()) {
            PreparedStatement psDiskonIbs = null;
            ResultSet rsDiskonIbs = null;
            try {
                boolean foundExact = false;
                double besar = 0;

                // 1. Coba exact match per tgl_operasi + kode_paket
                if (!cleanTgl.isEmpty()) {
                    psDiskonIbs = koneksi.prepareStatement(
                            "SELECT besar_pengurangan FROM diskon_ibs WHERE no_rawat = ? AND (tgl_operasi = ? OR tgl_operasi = ?) AND kode_paket = ?"
                    );
                    psDiskonIbs.setString(1, norawat.trim());
                    psDiskonIbs.setString(2, cleanTgl);
                    psDiskonIbs.setString(3, cleanTgl + ".0");
                    psDiskonIbs.setString(4, kodePaket.trim());
                    rsDiskonIbs = psDiskonIbs.executeQuery();
                    if (rsDiskonIbs.next()) {
                        besar = rsDiskonIbs.getDouble("besar_pengurangan");
                        foundExact = true;
                        sdahTampilDiskon.add(norawat.trim() + "|" + kodePaket.trim() + "|" + cleanTgl);
                    }
                    rsDiskonIbs.close();
                    psDiskonIbs.close();
                }

                // 2. Jika tanggal beda (belum ketemu exact), cari record diskon_ibs yang belum pernah terpakai
                if (!foundExact) {
                    psDiskonIbs = koneksi.prepareStatement(
                            "SELECT tgl_operasi, besar_pengurangan FROM diskon_ibs WHERE no_rawat = ? AND kode_paket = ? ORDER BY tgl_operasi ASC"
                    );
                    psDiskonIbs.setString(1, norawat.trim());
                    psDiskonIbs.setString(2, kodePaket.trim());
                    rsDiskonIbs = psDiskonIbs.executeQuery();
                    while (rsDiskonIbs.next()) {
                        String tglIbs = rsDiskonIbs.getString("tgl_operasi");
                        if (tglIbs != null && tglIbs.endsWith(".0")) {
                            tglIbs = tglIbs.substring(0, tglIbs.length() - 2);
                        }
                        String ibsKey = norawat.trim() + "|" + kodePaket.trim() + "|" + (tglIbs != null ? tglIbs.trim() : "");
                        if (!sdahTampilDiskon.contains(ibsKey)) {
                            besar = rsDiskonIbs.getDouble("besar_pengurangan");
                            sdahTampilDiskon.add(ibsKey); // tandai record diskon_ibs ini sudah terpakai
                            foundExact = true;
                            break;
                        }
                    }
                    rsDiskonIbs.close();
                    psDiskonIbs.close();
                }

                if (besar > 0) {
                    sdahTampilDiskon.add(setKey); // Tandai sudah ditampilkan agar tidak muncul di peran dokter lain pada jam yang sama
                    hasilPotongan.append(" | Potongan Operasi ").append(namatindakan.trim()).append(" | ").append(Valid.SetAngka(besar));
                    return namatindakan + hasilPotongan.toString();
                }
            } catch (Exception ignored) {
            } finally {
                try {
                    if (rsDiskonIbs != null) {
                        rsDiskonIbs.close();
                    }
                } catch (Exception ignored) {
                }
                try {
                    if (psDiskonIbs != null) {
                        psDiskonIbs.close();
                    }
                } catch (Exception ignored) {
                }
            }

            // Cek apakah no_rawat dan kode_paket ini memiliki record di diskon_ibs
            try {
                if (Sequel.cariInteger("SELECT count(*) FROM diskon_ibs WHERE no_rawat = '" + norawat.trim() + "' AND kode_paket = '" + kodePaket.trim() + "'") > 0) {
                    adaDiskonIbs = true;
                }
            } catch (Exception ignored) {
            }
        }

        if (adaDiskonIbs) {
            // Transaksi ini berbasis diskon_ibs tetapi baris ini bukan operasi yang berdiskon
            return namatindakan;
        }

        // --- Prioritas 2: fallback ke pengurangan_biaya (data lama tanpa diskon_ibs) ---
        PreparedStatement psCekPotongan = null;
        ResultSet rsCekPotongan = null;
        try {
            psCekPotongan = koneksi.prepareStatement(
                    "SELECT nama_pengurangan, besar_pengurangan FROM pengurangan_biaya WHERE no_rawat = ? AND nama_pengurangan LIKE ?"
            );
            psCekPotongan.setString(1, norawat.trim());
            psCekPotongan.setString(2, "%" + namatindakan.trim() + "%");
            rsCekPotongan = psCekPotongan.executeQuery();
            boolean adaPotongan = false;
            while (rsCekPotongan.next()) {
                String nm = rsCekPotongan.getString("nama_pengurangan");
                if (nm != null && !nm.trim().isEmpty()) {
                    String recordKey = norawat.trim() + "|" + nm.trim();
                    if (!sdahTampilDiskon.contains(recordKey)) {
                        double besar = rsCekPotongan.getDouble("besar_pengurangan");
                        hasilPotongan.append(" | ").append(nm.trim()).append(" | ").append(Valid.SetAngka(besar));
                        adaPotongan = true;
                        sdahTampilDiskon.add(recordKey); // tandai record pengurangan_biaya ini sudah ditampilkan
                    }
                }
            }
            if (adaPotongan) {
                sdahTampilDiskon.add(setKey); // tandai sudah ditampilkan
            }
            return namatindakan + hasilPotongan.toString();
        } catch (SQLException e) {
            System.err.println("Error checking potongan for norawat: " + norawat + " - " + e.getMessage());
            return namatindakan;
        } finally {
            try {
                if (rsCekPotongan != null) {
                    rsCekPotongan.close();
                }
                if (psCekPotongan != null) {
                    psCekPotongan.close();
                }
            } catch (SQLException ignored) {
            }
        }
    }

    private String getTglOnly(String tgl) {
        if (tgl == null) {
            return "";
        }
        String t = tgl.trim();
        return t.length() >= 10 ? t.substring(0, 10) : t;
    }

    private String getJamOnly(String tgl) {
        if (tgl == null) {
            return "";
        }
        String t = tgl.trim();
        return t.length() >= 19 ? t.substring(11, 19) : "";
    }

    private double getTotalPaketOperasi(String noRawat, String tglOperasi, String kodePaket) {
        double total = 0;
        if (noRawat == null || kodePaket == null) {
            return 0;
        }
        PreparedStatement ps = null;
        ResultSet rs = null;

        String tgl = (tglOperasi != null ? tglOperasi.trim() : "");
        if (tgl.endsWith(".0")) {
            tgl = tgl.substring(0, tgl.length() - 2);
        }

        try {
            if (!tgl.isEmpty()) {
                ps = koneksi.prepareStatement(
                        "SELECT (biayaoperator1+biayaoperator2+biayaoperator3+biayaasisten_operator1+biayaasisten_operator2+biayaasisten_operator3+biayainstrumen+"
                        + "biayadokter_anak+biayaperawaat_resusitas+biayadokter_anestesi+"
                        + "biayaasisten_anestesi+biayaasisten_anestesi2+biayabidan+biayabidan2+biayabidan3+"
                        + "biayaperawat_luar+biayaalat+biaya_dokter_pjanak+biaya_dokter_umum+"
                        + "biayasewaok+akomodasi+bagian_rs+biaya_omloop+biaya_omloop2+biaya_omloop3+biaya_omloop4+biaya_omloop5+biayasarpras) as total "
                        + "FROM operasi WHERE no_rawat=? AND (tgl_operasi=? OR tgl_operasi=?) AND kode_paket=?"
                );
                ps.setString(1, noRawat.trim());
                ps.setString(2, tgl);
                ps.setString(3, tgl + ".0");
                ps.setString(4, kodePaket.trim());
                rs = ps.executeQuery();
                if (rs.next()) {
                    total = rs.getDouble("total");
                }
                rs.close();
                ps.close();
            }

            // Fallback jika tidak ditemukan dengan tanggal (misal tanggal beda): cari berdasarkan no_rawat dan kode_paket
            if (total == 0) {
                ps = koneksi.prepareStatement(
                        "SELECT (biayaoperator1+biayaoperator2+biayaoperator3+biayaasisten_operator1+biayaasisten_operator2+biayaasisten_operator3+biayainstrumen+"
                        + "biayadokter_anak+biayaperawaat_resusitas+biayadokter_anestesi+"
                        + "biayaasisten_anestesi+biayaasisten_anestesi2+biayabidan+biayabidan2+biayabidan3+"
                        + "biayaperawat_luar+biayaalat+biaya_dokter_pjanak+biaya_dokter_umum+"
                        + "biayasewaok+akomodasi+bagian_rs+biaya_omloop+biaya_omloop2+biaya_omloop3+biaya_omloop4+biaya_omloop5+biayasarpras) as total "
                        + "FROM operasi WHERE no_rawat=? AND kode_paket=? ORDER BY tgl_operasi DESC LIMIT 1"
                );
                ps.setString(1, noRawat.trim());
                ps.setString(2, kodePaket.trim());
                rs = ps.executeQuery();
                if (rs.next()) {
                    total = rs.getDouble("total");
                }
                rs.close();
                ps.close();
            }

        } catch (Exception e) {
            System.err.println("Error getTotalPaketOperasi: " + e.getMessage());
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception e) {
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
            }
        }
        return total;
    }

    private double[] hitungJMOperasiPotongan(String norawat, String nmPerawatan, String kdDokter, double tarifMurni) {
        return hitungJMOperasiPotongan(norawat, nmPerawatan, kdDokter, tarifMurni, null, null);
    }

    /**
     * Overloaded method untuk kompatibilitas cekPotongan.
     */
    private String cekPotongan(String norawat, String namatindakan) {
        return cekPotongan(norawat, namatindakan, null, null);
    }

    /**
     * Menghitung bruto, sharing RS, dan JM dokter untuk tindakan operasi.
     * Prioritas: diskon_ibs (exact match per tgl_operasi+kode_paket) →
     * pengurangan_biaya (LIKE, hanya baris pertama per kode_paket per no_rawat)
     */
    private double[] hitungJMOperasiPotongan(String norawat, String nmPerawatan, String kdDokter, double tarifMurni, String kodePaket, String tglOperasi) {
        double[] result = new double[]{tarifMurni, 0.0, tarifMurni};

        if (norawat == null || nmPerawatan == null) {
            return result;
        }

        // --- Step 1: Ambil persentase sharing dokter dan RS ---
        double persenDokter = 85.0;
        double persenRS = 15.0;
        boolean hasMapping = false;

        String kdDokterTrimmed = (kdDokter == null) ? "" : kdDokter.trim();
        if (!kdDokterTrimmed.isEmpty()) {
            if (jmMappingCache.containsKey(kdDokterTrimmed)) {
                Double pDok = jmMappingCache.get(kdDokterTrimmed);
                Double pRS = rsMappingCache.get(kdDokterTrimmed);
                if (pDok != null && pDok > 0) {
                    persenDokter = pDok;
                    persenRS = (pRS != null && pRS > 0) ? pRS : (100.0 - pDok);
                    hasMapping = true;
                }
            } else {
                try {
                    PreparedStatement psMap = koneksi.prepareStatement(
                            "SELECT bagiandokter, bagianrs FROM maping_jm_dokter WHERE kd_dokter = ?"
                    );
                    psMap.setString(1, kdDokterTrimmed);
                    ResultSet rsMap = psMap.executeQuery();
                    if (rsMap.next()) {
                        persenDokter = rsMap.getDouble("bagiandokter");
                        persenRS = rsMap.getDouble("bagianrs");
                        hasMapping = true;
                        jmMappingCache.put(kdDokterTrimmed, persenDokter);
                        rsMappingCache.put(kdDokterTrimmed, persenRS);
                    } else {
                        jmMappingCache.put(kdDokterTrimmed, null);
                        rsMappingCache.put(kdDokterTrimmed, null);
                    }
                    rsMap.close();
                    psMap.close();
                } catch (Exception ignored) {
                }
            }
        }

        String cleanTgl = (tglOperasi != null ? tglOperasi.trim() : "");
        if (cleanTgl.endsWith(".0")) {
            cleanTgl = cleanTgl.substring(0, cleanTgl.length() - 2);
        }

        // --- Step 2A: Prioritas 1 - diskon_ibs_detail (Potongan Komponen Dokter) ---
        if (kodePaket != null && !kodePaket.trim().isEmpty() && !kdDokterTrimmed.isEmpty()) {
            PreparedStatement psDetail = null;
            ResultSet rsDetail = null;
            try {
                boolean foundDetail = false;
                double besarDiskon = 0;
                double diskonPersen = 0;

                // 1. Coba exact match per tgl_operasi
                if (!cleanTgl.isEmpty()) {
                    psDetail = koneksi.prepareStatement(
                            "SELECT besar_diskon, diskon_persen FROM diskon_ibs_detail WHERE no_rawat = ? AND (tgl_operasi = ? OR tgl_operasi = ?) AND kode_paket = ? AND (kd_dokter = ? OR kd_dokter = ?)"
                    );
                    psDetail.setString(1, norawat.trim());
                    psDetail.setString(2, cleanTgl);
                    psDetail.setString(3, cleanTgl + ".0");
                    psDetail.setString(4, kodePaket.trim());
                    psDetail.setString(5, kdDokterTrimmed);
                    psDetail.setString(6, kdDokter != null ? kdDokter : kdDokterTrimmed);
                    rsDetail = psDetail.executeQuery();
                    if (rsDetail.next()) {
                        besarDiskon = rsDetail.getDouble("besar_diskon");
                        diskonPersen = rsDetail.getDouble("diskon_persen");
                        foundDetail = true;
                    }
                    rsDetail.close();
                    psDetail.close();
                }

                // 2. Fallback pencocokan tanpa tgl_operasi jika belum ketemu
                if (!foundDetail) {
                    psDetail = koneksi.prepareStatement(
                            "SELECT besar_diskon, diskon_persen FROM diskon_ibs_detail WHERE no_rawat = ? AND kode_paket = ? AND (kd_dokter = ? OR kd_dokter = ?) ORDER BY tgl_operasi ASC"
                    );
                    psDetail.setString(1, norawat.trim());
                    psDetail.setString(2, kodePaket.trim());
                    psDetail.setString(3, kdDokterTrimmed);
                    psDetail.setString(4, kdDokter != null ? kdDokter : kdDokterTrimmed);
                    rsDetail = psDetail.executeQuery();
                    if (rsDetail.next()) {
                        besarDiskon = rsDetail.getDouble("besar_diskon");
                        diskonPersen = rsDetail.getDouble("diskon_persen");
                        foundDetail = true;
                    }
                    rsDetail.close();
                    psDetail.close();
                }

                if (foundDetail) {
                    double bruto = tarifMurni - besarDiskon;
                    if (bruto < 0) {
                        bruto = 0;
                    }
                    double jm = roundTwoDecimalsUp(bruto * persenDokter / 100.0);
                    double sharing = roundTwoDecimalsUp(bruto - jm);
                    if (sharing < 0) {
                        sharing = 0;
                    }
                    return new double[]{bruto, sharing, jm};
                }
            } catch (Exception ignored) {
            } finally {
                if (rsDetail != null) try { rsDetail.close(); } catch (Exception ignored) {}
                if (psDetail != null) try { psDetail.close(); } catch (Exception ignored) {}
            }
        }

        // --- Step 2B: Fallback ke logika lama diskon_ibs ---
        // Kunci unik per no_rawat + kode_paket / nm_perawatan + tgl_operasi untuk membedakan multi-tindakan pada tanggal/jam berbeda
        String setKey = norawat.trim() + "|"
                + (kodePaket != null && !kodePaket.trim().isEmpty() ? kodePaket.trim() : nmPerawatan.trim())
                + (!cleanTgl.isEmpty() ? "|" + cleanTgl : "");

        if (sdahHitungDiskon.contains(setKey)) {
            // Baris peran dokter berikutnya dalam transaksi operasi ini tidak dapat diskon ganda
            if (hasMapping) {
                double bruto = tarifMurni;
                double jm = roundTwoDecimalsUp(tarifMurni * persenDokter / 100.0);
                double sharing = roundTwoDecimalsUp(bruto - jm);
                if (sharing < 0) {
                    sharing = 0;
                }
                return new double[]{bruto, sharing, jm};
            }
            return result;
        }

        boolean adaDiskonIbs = false;

        // --- Step 2: Prioritas diskon_ibs ---
        if (kodePaket != null && !kodePaket.trim().isEmpty()) {
            PreparedStatement psDiskonIbs = null;
            ResultSet rsDiskonIbs = null;
            try {
                boolean foundExact = false;
                double diskon = 0;

                // 1. Coba exact match per tgl_operasi + kode_paket
                if (!cleanTgl.isEmpty()) {
                    psDiskonIbs = koneksi.prepareStatement(
                            "SELECT besar_pengurangan FROM diskon_ibs WHERE no_rawat = ? AND (tgl_operasi = ? OR tgl_operasi = ?) AND kode_paket = ?"
                    );
                    psDiskonIbs.setString(1, norawat.trim());
                    psDiskonIbs.setString(2, cleanTgl);
                    psDiskonIbs.setString(3, cleanTgl + ".0");
                    psDiskonIbs.setString(4, kodePaket.trim());
                    rsDiskonIbs = psDiskonIbs.executeQuery();
                    if (rsDiskonIbs.next()) {
                        diskon = rsDiskonIbs.getDouble("besar_pengurangan");
                        foundExact = true;
                        sdahHitungDiskon.add(norawat.trim() + "|" + kodePaket.trim() + "|" + cleanTgl);
                    }
                    rsDiskonIbs.close();
                    psDiskonIbs.close();
                }

                // 2. Jika tanggal beda (belum ketemu exact), cari record diskon_ibs yang belum pernah terpakai
                if (!foundExact) {
                    psDiskonIbs = koneksi.prepareStatement(
                            "SELECT tgl_operasi, besar_pengurangan FROM diskon_ibs WHERE no_rawat = ? AND kode_paket = ? ORDER BY tgl_operasi ASC"
                    );
                    psDiskonIbs.setString(1, norawat.trim());
                    psDiskonIbs.setString(2, kodePaket.trim());
                    rsDiskonIbs = psDiskonIbs.executeQuery();
                    while (rsDiskonIbs.next()) {
                        String tglIbs = rsDiskonIbs.getString("tgl_operasi");
                        if (tglIbs != null && tglIbs.endsWith(".0")) {
                            tglIbs = tglIbs.substring(0, tglIbs.length() - 2);
                        }
                        String ibsKey = norawat.trim() + "|" + kodePaket.trim() + "|" + (tglIbs != null ? tglIbs.trim() : "");
                        if (!sdahHitungDiskon.contains(ibsKey)) {
                            diskon = rsDiskonIbs.getDouble("besar_pengurangan");
                            sdahHitungDiskon.add(ibsKey); // tandai record diskon_ibs ini sudah terpakai
                            foundExact = true;
                            break;
                        }
                    }
                    rsDiskonIbs.close();
                    psDiskonIbs.close();
                }

                if (diskon > 0) {
                    sdahHitungDiskon.add(setKey); // Tandai sudah dihitung agar peran dokter lain pada operasi ini tidak terpotong ganda
                    double totalPaket = getTotalPaketOperasi(norawat, tglOperasi, kodePaket);
                    double bruto = totalPaket - diskon;
                    if (bruto < 0) {
                        bruto = 0;
                    }
                    double jm = roundTwoDecimalsUp(bruto * persenDokter / 100.0);
                    double sharing = roundTwoDecimalsUp(bruto - jm);
                    if (sharing < 0) {
                        sharing = 0;
                    }

                    try {
                        java.io.FileWriter fw = new java.io.FileWriter("debug_bruto.txt", true);
                        fw.write("TglOp: " + tglOperasi + ", Paket: " + kodePaket + ", NoRawat: " + norawat + "\n");
                        fw.write(" => Tindakan (diskon_ibs): " + nmPerawatan + "\n");
                        fw.write(" => Total Paket Awal: " + totalPaket + "\n");
                        fw.write(" => Total Diskon (Besar Pengurangan): " + diskon + "\n");
                        fw.write(" => Bruto Final Ditampilkan: " + bruto + "\n");
                        fw.write("--------------------------------------------------\n");
                        fw.close();
                    } catch (Exception e) {
                    }

                    return new double[]{bruto, sharing, jm};
                }
            } catch (Exception ignored) {
            } finally {
                try {
                    if (rsDiskonIbs != null) {
                        rsDiskonIbs.close();
                    }
                } catch (Exception ignored) {
                }
                try {
                    if (psDiskonIbs != null) {
                        psDiskonIbs.close();
                    }
                } catch (Exception ignored) {
                }
            }

            // Cek apakah no_rawat dan kode_paket ini memiliki record di diskon_ibs
            try {
                if (Sequel.cariInteger("SELECT count(*) FROM diskon_ibs WHERE no_rawat = '" + norawat.trim() + "' AND kode_paket = '" + kodePaket.trim() + "'") > 0) {
                    adaDiskonIbs = true;
                }
            } catch (Exception ignored) {
            }
        }

        if (adaDiskonIbs) {
            // Operasi ini merupakan bagian dari transaksi diskon_ibs tetapi jamnya tidak berdiskon => gunakan tarif murni
            if (hasMapping) {
                double bruto = tarifMurni;
                double jm = roundTwoDecimalsUp(tarifMurni * persenDokter / 100.0);
                double sharing = roundTwoDecimalsUp(bruto - jm);
                if (sharing < 0) {
                    sharing = 0;
                }
                return new double[]{bruto, sharing, jm};
            }
            return result;
        }

        // --- Step 3: Fallback ke pengurangan_biaya (data lama tanpa diskon_ibs) ---
        PreparedStatement psPotongan = null;
        ResultSet rsPotongan = null;
        try {
            psPotongan = koneksi.prepareStatement(
                    "SELECT nama_pengurangan, besar_pengurangan FROM pengurangan_biaya WHERE no_rawat = ? AND nama_pengurangan LIKE ?"
            );
            psPotongan.setString(1, norawat.trim());
            psPotongan.setString(2, "%" + nmPerawatan.trim() + "%");
            rsPotongan = psPotongan.executeQuery();
            double totalPengurangan = 0;
            while (rsPotongan.next()) {
                String nm = rsPotongan.getString("nama_pengurangan");
                if (nm != null && !nm.trim().isEmpty()) {
                    String recordKey = norawat.trim() + "|" + nm.trim();
                    if (!sdahHitungDiskon.contains(recordKey)) {
                        double besarPengurangan = rsPotongan.getDouble("besar_pengurangan");
                        totalPengurangan += besarPengurangan;
                        sdahHitungDiskon.add(recordKey); // tandai record pengurangan_biaya ini sudah terpakai
                    }
                }
            }

            if (totalPengurangan > 0) {
                sdahHitungDiskon.add(setKey); // tandai sudah dihitung, duplikat tidak akan dapat diskon
                double totalPaket = getTotalPaketOperasi(norawat, tglOperasi, kodePaket);
                double bruto = totalPaket - totalPengurangan;
                if (bruto < 0) {
                    bruto = 0;
                }
                double jm = roundTwoDecimalsUp(bruto * persenDokter / 100.0);
                double sharing = roundTwoDecimalsUp(bruto - jm);
                if (sharing < 0) {
                    sharing = 0;
                }

                try {
                    java.io.FileWriter fw = new java.io.FileWriter("debug_bruto.txt", true);
                    fw.write("TglOp: " + tglOperasi + ", Paket: " + kodePaket + ", NoRawat: " + norawat + "\n");
                    fw.write(" => Tindakan (pengurangan_biaya): " + nmPerawatan + "\n");
                    fw.write(" => Total Paket Awal: " + totalPaket + "\n");
                    fw.write(" => Total Diskon (Besar Pengurangan): " + totalPengurangan + "\n");
                    fw.write(" => Bruto Final Ditampilkan: " + bruto + "\n");
                    fw.write("--------------------------------------------------\n");
                    fw.close();
                } catch (Exception e) {
                }

                return new double[]{bruto, sharing, jm};
            }
        } catch (SQLException e) {
            System.err.println("Error hitungJMOperasiPotongan (potongan): " + e.getMessage());
        } finally {
            try {
                if (rsPotongan != null) {
                    rsPotongan.close();
                }
                if (psPotongan != null) {
                    psPotongan.close();
                }
            } catch (SQLException ignored) {
            }
        }

        // --- Step 4: Tidak ada potongan — gunakan mapping jika tersedia ---
        if (hasMapping) {
            double bruto = tarifMurni;
            double jm = roundTwoDecimalsUp(tarifMurni * persenDokter / 100.0);
            double sharing = roundTwoDecimalsUp(bruto - jm);
            if (sharing < 0) {
                sharing = 0;
            }
            result = new double[]{bruto, sharing, jm};
        }
        return result;
    }

    private double[] hitungBrutoSharingNettoRalanWithPotongan(boolean isMapping, String kdDokter, double tarifAsli, String norawat, String nmPerawatan) {
        double bruto = tarifAsli;
        double sharing = 0.0;
        double jm = tarifAsli;

        String kdDokterTrimmed = (kdDokter == null) ? "" : kdDokter.trim();

        double persenDokter = 85.0;
        double persenRS = 15.0;
        boolean hasMapping = false;

        if (!kdDokterTrimmed.isEmpty()) {
            if (jmMappingCache.containsKey(kdDokterTrimmed)) {
                Double pDok = jmMappingCache.get(kdDokterTrimmed);
                Double pRS = rsMappingCache.get(kdDokterTrimmed);
                if (pDok != null && pDok > 0) {
                    persenDokter = pDok;
                    persenRS = (pRS != null && pRS > 0) ? pRS : (100.0 - pDok);
                    hasMapping = true;
                }
            } else {
                try {
                    PreparedStatement psMap = koneksi.prepareStatement(
                            "SELECT bagiandokter, bagianrs FROM maping_jm_dokter WHERE kd_dokter = ?"
                    );
                    psMap.setString(1, kdDokterTrimmed);
                    ResultSet rsMap = psMap.executeQuery();
                    if (rsMap.next()) {
                        persenDokter = rsMap.getDouble("bagiandokter");
                        persenRS = rsMap.getDouble("bagianrs");
                        hasMapping = true;
                        jmMappingCache.put(kdDokterTrimmed, persenDokter);
                        rsMappingCache.put(kdDokterTrimmed, persenRS);
                    } else {
                        jmMappingCache.put(kdDokterTrimmed, null);
                        rsMappingCache.put(kdDokterTrimmed, null);
                    }
                    rsMap.close();
                    psMap.close();
                } catch (Exception ignored) {
                }
            }
        }

        double besarPengurangan = 0.0;
        boolean hasPotongan = false;
        if (norawat != null && nmPerawatan != null && !norawat.trim().isEmpty() && !nmPerawatan.trim().isEmpty()) {
            try {
                PreparedStatement psPot = koneksi.prepareStatement(
                        "SELECT besar_pengurangan FROM pengurangan_biaya WHERE no_rawat = ? AND nama_pengurangan LIKE ?"
                );
                psPot.setString(1, norawat.trim());
                psPot.setString(2, "%" + nmPerawatan.trim() + "%");
                ResultSet rsPot = psPot.executeQuery();
                if (rsPot.next()) {
                    besarPengurangan = rsPot.getDouble("besar_pengurangan");
                    hasPotongan = true;
                }
                rsPot.close();
                psPot.close();
            } catch (Exception ignored) {
            }
        }

        if (hasPotongan && besarPengurangan > 0) {
            // masterTarifDr = tarifAsli (dari jns_perawatan.tarif_tindakandr)
            double jmAwalDokter = (tarifAsli * persenDokter) / 100.0;
            double sisaJMDokter = jmAwalDokter - besarPengurangan;
            if (sisaJMDokter < 0) {
                sisaJMDokter = 0;
            }

            double rasioSisa = (jmAwalDokter > 0) ? (sisaJMDokter / jmAwalDokter) : 0.0;

            bruto = roundTwoDecimalsUp(rasioSisa * tarifAsli);
            jm = roundTwoDecimalsUp(bruto * persenDokter / 100.0);
            sharing = roundTwoDecimalsUp(bruto - jm);
            if (sharing < 0) {
                sharing = 0;
            }

            System.out.println("[DEBUG MASTER TARIF FIX] NoRawat=" + norawat + ", Perawatan=" + nmPerawatan + ", masterTarifDr=" + tarifAsli + ", persenDokter=" + persenDokter + "%, jmAwalDokter=" + jmAwalDokter + ", besarPengurangan=" + besarPengurangan + ", sisaJMDokter=" + sisaJMDokter + " => RESULT: bruto=" + bruto + ", sharing=" + sharing + ", jm=" + jm);
        } else if (isMapping && hasMapping) {
            bruto = tarifAsli;
            jm = roundTwoDecimalsUp(tarifAsli * persenDokter / 100.0);
            sharing = roundTwoDecimalsUp(bruto - jm);
            if (sharing < 0) {
                sharing = 0;
            }
        }

        return new double[]{bruto, sharing, jm};
    }

    private javax.swing.JDialog dlgNotaAktif = null;

    public void tampilNota(String norawat) {
        if (norawat == null || norawat.trim().isEmpty()) {
            return;
        }
        if (dlgNotaAktif != null && dlgNotaAktif.isShowing()) {
            dlgNotaAktif.toFront();
            return;
        }
        try {
            java.io.FileInputStream is = new java.io.FileInputStream("setting/database.xml");
            java.util.Properties prop = new java.util.Properties();
            prop.loadFromXML(is);
            is.close();

            String host = koneksiDB.HOSTHYBRIDWEB();
            String port = prop.getProperty("PORTWEB");
            String hybrid = prop.getProperty("HYBRIDWEB");

            String url = "http://" + host + ":" + port + "/" + hybrid + "/berkasrawat/pages/billingtampil.php?norawat=" + norawat.trim();

            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    if (dlgNotaAktif != null && dlgNotaAktif.isShowing()) {
                        dlgNotaAktif.toFront();
                        return;
                    }
                    javax.swing.JDialog dlgNota = new javax.swing.JDialog(KeuanganBayarJMDokter.this, "Nota Billing - " + norawat.trim(), true);
                    dlgNotaAktif = dlgNota;
                    dlgNota.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosed(java.awt.event.WindowEvent e) {
                            dlgNotaAktif = null;
                        }
                    });
                    dlgNota.setSize(750, 850);
                    dlgNota.setLocationRelativeTo(KeuanganBayarJMDokter.this);

                    javafx.embed.swing.JFXPanel notaPanel = new javafx.embed.swing.JFXPanel();
                    dlgNota.getContentPane().add(notaPanel, java.awt.BorderLayout.CENTER);

                    javafx.application.Platform.runLater(() -> {
                        try {
                            javafx.scene.web.WebView viewNota = new javafx.scene.web.WebView();
                            javafx.scene.web.WebEngine engineNota = viewNota.getEngine();
                            engineNota.load(url);
                            notaPanel.setScene(new javafx.scene.Scene(viewNota));
                        } catch (Throwable exFX) {
                            exFX.printStackTrace();
                        }
                    });

                    dlgNota.setVisible(true);
                } catch (Throwable exSwing) {
                    exSwing.printStackTrace();
                }
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    class ButtonRenderNota extends javax.swing.JButton implements javax.swing.table.TableCellRenderer {

        public ButtonRenderNota() {
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD | java.awt.Font.ITALIC, 11));
            setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
            setText("<html><i><b>Nota</b></i></html>");
        }

        @Override
        public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("<html><i><b>Nota</b></i></html>");
            return this;
        }
    }

    class ButtonEditorNota extends javax.swing.DefaultCellEditor {

        private javax.swing.JButton button;
        private String label;
        private boolean isPushed;
        private int selectedRow = -1;

        public ButtonEditorNota(javax.swing.JTextField checkBox) {
            super(checkBox);
            setClickCountToStart(1);
            button = new javax.swing.JButton();
            button.setOpaque(false);
            button.setContentAreaFilled(false);
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD | java.awt.Font.ITALIC, 11));
            button.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png")));
            button.setText("<html><i><b>Nota</b></i></html>");
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = "Nota";
            button.setText("<html><i><b>Nota</b></i></html>");
            selectedRow = row;
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed && selectedRow >= 0 && selectedRow < tbBangsal.getRowCount()) {
                Object nrObj = tbBangsal.getValueAt(selectedRow, 3);
                if (nrObj != null) {
                    tampilNota(nrObj.toString());
                }
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }
    }

}
