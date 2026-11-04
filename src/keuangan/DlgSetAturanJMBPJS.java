package keuangan;

import fungsi.KalkulatorJasaMedisDokter;
import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
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

/**
 * Pengaturan Ketentuan & Aturan Kalkulator JM BPJS
 * Memungkinkan aktivasi / deaktivasi dan modifikasi parameter perhitungan JM BPJS
 * secara dinamis tanpa kompilasi ulang kode program.
 * 
 * @author it-rsib & AI Engineering Team
 */
public class DlgSetAturanJMBPJS extends javax.swing.JDialog {

    private final DefaultTableModel tabMode;
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0;

    /**
     * Creates new form DlgSetAturanJMBPJS
     */
    public DlgSetAturanJMBPJS(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        this.setLocation(8, 1);
        setSize(980, 620);

        Object[] headers = {
            "P", "ID Aturan", "Kategori", "Nama Ketentuan / Aturan", "Kondisi Pemicu",
            "Metode Hitung", "Param 1 (Tarif/Batas)", "Param 2 (%/Plus)", "Param 3 (% Raber)", "Status Aktif", "Keterangan"
        };

        tabMode = new DefaultTableModel(null, headers) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return colIndex == 0; // Hanya kolom centang P yang editable langsung
            }

            Class[] types = new Class[]{
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Double.class, java.lang.Double.class, java.lang.Double.class,
                java.lang.Object.class, java.lang.Object.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        tbAturan.setModel(tabMode);
        tbAturan.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbAturan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] columnWidths = {25, 160, 90, 210, 220, 110, 115, 95, 95, 80, 260};
        for (i = 0; i < columnWidths.length; i++) {
            TableColumn column = tbAturan.getColumnModel().getColumn(i);
            column.setPreferredWidth(columnWidths[i]);
        }
        tbAturan.setDefaultRenderer(Object.class, new WarnaTable());

        txtIdAturan.setDocument(new batasInput((byte) 40).getKata(txtIdAturan));
        txtNamaAturan.setDocument(new batasInput((byte) 100).getKata(txtNamaAturan));
        txtKondisi.setDocument(new batasInput((byte) 150).getKata(txtKondisi));
        txtParam1.setDocument(new batasInput((byte) 15).getKata(txtParam1));
        txtParam2.setDocument(new batasInput((byte) 15).getKata(txtParam2));
        txtParam3.setDocument(new batasInput((byte) 15).getKata(txtParam3));
        txtKeterangan.setDocument(new batasInput((int) 200).getKata(txtKeterangan));
        TCari.setDocument(new batasInput((byte) 100).getKata(TCari));

        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2 || TCari.getText().trim().isEmpty()) {
                        tampil();
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2 || TCari.getText().trim().isEmpty()) {
                        tampil();
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2 || TCari.getText().trim().isEmpty()) {
                        tampil();
                    }
                }
            });
        }

        // Listener reaktif pada combo filter kategori & status
        cmbFilterKategori.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                    tampil();
                }
            }
        });

        cmbFilterStatus.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (evt.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                    tampil();
                }
            }
        });
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        PanelInput = new widget.panelisi();
        panelBarisInput1 = new widget.panelisi();
        lblIdAturan = new widget.Label();
        txtIdAturan = new widget.TextBox();
        lblKategori = new widget.Label();
        cmbKategori = new widget.ComboBox();
        lblNamaAturan = new widget.Label();
        txtNamaAturan = new widget.TextBox();
        lblStatus = new widget.Label();
        cmbStatus = new widget.ComboBox();
        panelBarisInput2 = new widget.panelisi();
        lblKondisi = new widget.Label();
        txtKondisi = new widget.TextBox();
        lblMetode = new widget.Label();
        cmbMetode = new widget.ComboBox();
        lblParam1 = new widget.Label();
        txtParam1 = new widget.TextBox();
        lblParam2 = new widget.Label();
        txtParam2 = new widget.TextBox();
        lblParam3 = new widget.Label();
        txtParam3 = new widget.TextBox();
        panelBarisInput3 = new widget.panelisi();
        lblKeterangan = new widget.Label();
        txtKeterangan = new widget.TextBox();
        Scroll = new widget.ScrollPane();
        tbAturan = new widget.Table();
        panelBawah = new widget.panelisi();
        panelGlass9 = new widget.panelisi();
        lblFilterKategori = new widget.Label();
        cmbFilterKategori = new widget.ComboBox();
        lblFilterStatus = new widget.Label();
        cmbFilterStatus = new widget.ComboBox();
        LCount = new widget.Label();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnGanti = new widget.Button();
        BtnToggle = new widget.Button();
        BtnKeluar = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Pengaturan Ketentuan & Aturan Kalkulator JM BPJS ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setPreferredSize(new java.awt.Dimension(100, 105));
        PanelInput.setLayout(new java.awt.GridLayout(3, 1));

        panelBarisInput1.setName("panelBarisInput1"); // NOI18N
        panelBarisInput1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 4));

        lblIdAturan.setText("ID Aturan :");
        lblIdAturan.setName("lblIdAturan"); // NOI18N
        lblIdAturan.setPreferredSize(new java.awt.Dimension(65, 23));
        panelBarisInput1.add(lblIdAturan);

        txtIdAturan.setName("txtIdAturan"); // NOI18N
        txtIdAturan.setPreferredSize(new java.awt.Dimension(180, 23));
        txtIdAturan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtIdAturanKeyPressed(evt);
            }
        });
        panelBarisInput1.add(txtIdAturan);

        lblKategori.setText("Kategori :");
        lblKategori.setName("lblKategori"); // NOI18N
        lblKategori.setPreferredSize(new java.awt.Dimension(60, 23));
        panelBarisInput1.add(lblKategori);

        cmbKategori.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Rawat Jalan", "Rawat Inap", "Tindakan Khusus" }));
        cmbKategori.setName("cmbKategori"); // NOI18N
        cmbKategori.setPreferredSize(new java.awt.Dimension(120, 23));
        cmbKategori.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbKategoriKeyPressed(evt);
            }
        });
        panelBarisInput1.add(cmbKategori);

        lblNamaAturan.setText("Nama Ketentuan :");
        lblNamaAturan.setName("lblNamaAturan"); // NOI18N
        lblNamaAturan.setPreferredSize(new java.awt.Dimension(100, 23));
        panelBarisInput1.add(lblNamaAturan);

        txtNamaAturan.setName("txtNamaAturan"); // NOI18N
        txtNamaAturan.setPreferredSize(new java.awt.Dimension(240, 23));
        txtNamaAturan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtNamaAturanKeyPressed(evt);
            }
        });
        panelBarisInput1.add(txtNamaAturan);

        lblStatus.setText("Status :");
        lblStatus.setName("lblStatus"); // NOI18N
        lblStatus.setPreferredSize(new java.awt.Dimension(50, 23));
        panelBarisInput1.add(lblStatus);

        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Aktif", "Tidak Aktif" }));
        cmbStatus.setName("cmbStatus"); // NOI18N
        cmbStatus.setPreferredSize(new java.awt.Dimension(95, 23));
        cmbStatus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbStatusKeyPressed(evt);
            }
        });
        panelBarisInput1.add(cmbStatus);

        PanelInput.add(panelBarisInput1);

        panelBarisInput2.setName("panelBarisInput2"); // NOI18N
        panelBarisInput2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 4));

        lblKondisi.setText("Pemicu :");
        lblKondisi.setName("lblKondisi"); // NOI18N
        lblKondisi.setPreferredSize(new java.awt.Dimension(65, 23));
        panelBarisInput2.add(lblKondisi);

        txtKondisi.setName("txtKondisi"); // NOI18N
        txtKondisi.setPreferredSize(new java.awt.Dimension(240, 23));
        txtKondisi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtKondisiKeyPressed(evt);
            }
        });
        panelBarisInput2.add(txtKondisi);

        lblMetode.setText("Metode :");
        lblMetode.setName("lblMetode"); // NOI18N
        lblMetode.setPreferredSize(new java.awt.Dimension(55, 23));
        panelBarisInput2.add(lblMetode);

        cmbMetode.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Flat", "Persentase", "Formula Selisih", "Proporsional DPJP", "Prorata Persentase" }));
        cmbMetode.setName("cmbMetode"); // NOI18N
        cmbMetode.setPreferredSize(new java.awt.Dimension(130, 23));
        cmbMetode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbMetodeKeyPressed(evt);
            }
        });
        panelBarisInput2.add(cmbMetode);

        lblParam1.setText("Param 1 (Tarif) :");
        lblParam1.setName("lblParam1"); // NOI18N
        lblParam1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelBarisInput2.add(lblParam1);

        txtParam1.setName("txtParam1"); // NOI18N
        txtParam1.setPreferredSize(new java.awt.Dimension(80, 23));
        txtParam1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtParam1KeyPressed(evt);
            }
        });
        panelBarisInput2.add(txtParam1);

        lblParam2.setText("Param 2 (%) :");
        lblParam2.setName("lblParam2"); // NOI18N
        lblParam2.setPreferredSize(new java.awt.Dimension(80, 23));
        panelBarisInput2.add(lblParam2);

        txtParam2.setName("txtParam2"); // NOI18N
        txtParam2.setPreferredSize(new java.awt.Dimension(60, 23));
        txtParam2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtParam2KeyPressed(evt);
            }
        });
        panelBarisInput2.add(txtParam2);

        lblParam3.setText("Param 3 (% Raber) :");
        lblParam3.setName("lblParam3"); // NOI18N
        lblParam3.setPreferredSize(new java.awt.Dimension(110, 23));
        panelBarisInput2.add(lblParam3);

        txtParam3.setName("txtParam3"); // NOI18N
        txtParam3.setPreferredSize(new java.awt.Dimension(60, 23));
        txtParam3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtParam3KeyPressed(evt);
            }
        });
        panelBarisInput2.add(txtParam3);

        PanelInput.add(panelBarisInput2);

        panelBarisInput3.setName("panelBarisInput3"); // NOI18N
        panelBarisInput3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 4));

        lblKeterangan.setText("Keterangan :");
        lblKeterangan.setName("lblKeterangan"); // NOI18N
        lblKeterangan.setPreferredSize(new java.awt.Dimension(65, 23));
        panelBarisInput3.add(lblKeterangan);

        txtKeterangan.setName("txtKeterangan"); // NOI18N
        txtKeterangan.setPreferredSize(new java.awt.Dimension(860, 23));
        txtKeterangan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtKeteranganKeyPressed(evt);
            }
        });
        panelBarisInput3.add(txtKeterangan);

        PanelInput.add(panelBarisInput3);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbAturan.setToolTipText("Klik baris untuk memilih atau klik ganda untuk toggle On/Off status");
        tbAturan.setName("tbAturan"); // NOI18N
        tbAturan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbAturanMouseClicked(evt);
            }
        });
        tbAturan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbAturanKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbAturan);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        panelBawah.setName("panelBawah"); // NOI18N
        panelBawah.setPreferredSize(new java.awt.Dimension(100, 95));
        panelBawah.setLayout(new java.awt.BorderLayout());

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(100, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 8));

        lblFilterKategori.setText("Filter Kategori :");
        lblFilterKategori.setName("lblFilterKategori"); // NOI18N
        lblFilterKategori.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(lblFilterKategori);

        cmbFilterKategori.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Rawat Jalan", "Rawat Inap", "Tindakan Khusus" }));
        cmbFilterKategori.setName("cmbFilterKategori"); // NOI18N
        cmbFilterKategori.setPreferredSize(new java.awt.Dimension(120, 23));
        cmbFilterKategori.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFilterKategoriActionPerformed(evt);
            }
        });
        panelGlass9.add(cmbFilterKategori);

        lblFilterStatus.setText("Status :");
        lblFilterStatus.setName("lblFilterStatus"); // NOI18N
        lblFilterStatus.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass9.add(lblFilterStatus);

        cmbFilterStatus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Aktif", "Tidak Aktif" }));
        cmbFilterStatus.setName("cmbFilterStatus"); // NOI18N
        cmbFilterStatus.setPreferredSize(new java.awt.Dimension(95, 23));
        cmbFilterStatus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFilterStatusActionPerformed(evt);
            }
        });
        panelGlass9.add(cmbFilterStatus);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("Record : 0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(85, 23));
        panelGlass9.add(LCount);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(65, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(160, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnCari.setMnemonic('5');
        BtnCari.setToolTipText("F5");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(30, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        panelGlass9.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(85, 23));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        panelGlass9.add(BtnAll);

        panelBawah.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(100, 48));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 8));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("F1 / Alt+S");
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
        BtnBatal.setToolTipText("F2 / Alt+B");
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

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("F3 / Alt+H");
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

        BtnGanti.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnGanti.setMnemonic('G');
        BtnGanti.setText("Ganti");
        BtnGanti.setToolTipText("Alt+G");
        BtnGanti.setName("BtnGanti"); // NOI18N
        BtnGanti.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnGanti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnGantiActionPerformed(evt);
            }
        });
        BtnGanti.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnGantiKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnGanti);

        BtnToggle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnToggle.setMnemonic('T');
        BtnToggle.setText("On / Off Status");
        BtnToggle.setToolTipText("Alt+T (Aktifkan / Nonaktifkan Ketentuan Terpilih)");
        BtnToggle.setName("BtnToggle"); // NOI18N
        BtnToggle.setPreferredSize(new java.awt.Dimension(140, 30));
        BtnToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnToggleActionPerformed(evt);
            }
        });
        BtnToggle.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnToggleKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnToggle);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("F4 / Alt+K");
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

        panelBawah.add(panelGlass8, java.awt.BorderLayout.CENTER);

        internalFrame1.add(panelBawah, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        KalkulatorJasaMedisDokter.setupDatabaseTable(koneksi);
        tampil();
    }//GEN-LAST:event_formWindowOpened

    private void txtIdAturanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtIdAturanKeyPressed
        Valid.pindah(evt, TCari, cmbKategori);
    }//GEN-LAST:event_txtIdAturanKeyPressed

    private void cmbKategoriKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbKategoriKeyPressed
        Valid.pindah(evt, txtIdAturan, txtNamaAturan);
    }//GEN-LAST:event_cmbKategoriKeyPressed

    private void txtNamaAturanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNamaAturanKeyPressed
        Valid.pindah(evt, cmbKategori, cmbStatus);
    }//GEN-LAST:event_txtNamaAturanKeyPressed

    private void cmbStatusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbStatusKeyPressed
        Valid.pindah(evt, txtNamaAturan, txtKondisi);
    }//GEN-LAST:event_cmbStatusKeyPressed

    private void txtKondisiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtKondisiKeyPressed
        Valid.pindah(evt, cmbStatus, cmbMetode);
    }//GEN-LAST:event_txtKondisiKeyPressed

    private void cmbMetodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbMetodeKeyPressed
        Valid.pindah(evt, txtKondisi, txtParam1);
    }//GEN-LAST:event_cmbMetodeKeyPressed

    private void txtParam1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtParam1KeyPressed
        Valid.pindah(evt, cmbMetode, txtParam2);
    }//GEN-LAST:event_txtParam1KeyPressed

    private void txtParam2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtParam2KeyPressed
        Valid.pindah(evt, txtParam1, txtParam3);
    }//GEN-LAST:event_txtParam2KeyPressed

    private void txtParam3KeyPressed(java.awt.event.KeyEvent evt) {
        Valid.pindah(evt, txtParam2, txtKeterangan);
    }

    private void txtKeteranganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtKeteranganKeyPressed
        Valid.pindah(evt, txtParam3, BtnSimpan);
    }//GEN-LAST:event_txtKeteranganKeyPressed

    private void tbAturanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbAturanMouseClicked
        if (tabMode.getRowCount() != 0) {
            try {
                getData();
                if (evt.getClickCount() == 2) {
                    // Double click memicu toggle status cepat On / Off
                    BtnToggleActionPerformed(null);
                }
            } catch (java.lang.Exception e) {
            }
        }
    }//GEN-LAST:event_tbAturanMouseClicked

    private void tbAturanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbAturanKeyPressed
        if (tabMode.getRowCount() != 0) {
            if ((evt.getKeyCode() == KeyEvent.VK_ENTER) || (evt.getKeyCode() == KeyEvent.VK_UP) || (evt.getKeyCode() == KeyEvent.VK_DOWN)) {
                try {
                    getData();
                } catch (java.lang.Exception e) {
                }
            } else if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
                BtnToggleActionPerformed(null);
            }
        }
    }//GEN-LAST:event_tbAturanKeyPressed

    private void cmbFilterKategoriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFilterKategoriActionPerformed
        tampil();
    }//GEN-LAST:event_cmbFilterKategoriActionPerformed

    private void cmbFilterStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFilterStatusActionPerformed
        tampil();
    }//GEN-LAST:event_cmbFilterStatusActionPerformed

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
        tampil();
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        cmbFilterKategori.setSelectedIndex(0);
        cmbFilterStatus.setSelectedIndex(0);
        tampil();
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if (txtIdAturan.getText().trim().equals("")) {
            Valid.textKosong(txtIdAturan, "ID Aturan");
        } else if (txtNamaAturan.getText().trim().equals("")) {
            Valid.textKosong(txtNamaAturan, "Nama Ketentuan / Aturan");
        } else {
            // Cek apakah ID Aturan sudah ada
            int ada = Sequel.cariInteger("select count(id_aturan) from set_aturan_jm_bpjs where id_aturan='" + txtIdAturan.getText().trim() + "'");
            if (ada > 0) {
                int reply = JOptionPane.showConfirmDialog(rootPane,
                        "ID Aturan '" + txtIdAturan.getText().trim() + "' sudah terdaftar.\nApakah Anda ingin memperbarui datanya?",
                        "Konfirmasi Perbaruan", JOptionPane.YES_NO_OPTION);
                if (reply == JOptionPane.YES_OPTION) {
                    BtnGantiActionPerformed(evt);
                }
            } else {
                try {
                    ps = koneksi.prepareStatement(
                        "insert into set_aturan_jm_bpjs "
                        + "(id_aturan, kategori, nama_aturan, kondisi_pemicu, metode_hitung, nilai_parameter_1, nilai_parameter_2, nilai_parameter_3, status_aktif, keterangan) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                    );
                    try {
                        ps.setString(1, txtIdAturan.getText().trim());
                        ps.setString(2, cmbKategori.getSelectedItem().toString());
                        ps.setString(3, txtNamaAturan.getText().trim());
                        ps.setString(4, txtKondisi.getText().trim());
                        ps.setString(5, cmbMetode.getSelectedItem().toString());
                        ps.setDouble(6, Double.parseDouble(txtParam1.getText().trim().isEmpty() ? "0" : txtParam1.getText().trim().replace(",", "")));
                        ps.setDouble(7, Double.parseDouble(txtParam2.getText().trim().isEmpty() ? "0" : txtParam2.getText().trim().replace(",", "")));
                        ps.setDouble(8, Double.parseDouble(txtParam3.getText().trim().isEmpty() ? "0" : txtParam3.getText().trim().replace(",", "")));
                        ps.setString(9, cmbStatus.getSelectedItem().toString());
                        ps.setString(10, txtKeterangan.getText().trim());
                        ps.executeUpdate();

                        KalkulatorJasaMedisDokter.reloadCache(koneksi);
                        tampil();
                        emptTeks();
                    } catch (Exception e) {
                        System.out.println("Notifikasi simpan set_aturan_jm_bpjs: " + e);
                        JOptionPane.showMessageDialog(rootPane, "Gagal menyimpan data aturan: " + e.getMessage());
                    } finally {
                        if (ps != null) {
                            ps.close();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Koneksi simpan: " + e);
                }
            }
        }
    }//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnSimpanActionPerformed(null);
        } else {
            Valid.pindah(evt, txtKeterangan, BtnBatal);
        }
    }//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
    }//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnBatalActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnSimpan, BtnHapus);
        }
    }//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        int row = tbAturan.getSelectedRow();
        if (row != -1) {
            String idAturan = tabMode.getValueAt(row, 1).toString();
            String namaAturan = tabMode.getValueAt(row, 3).toString();
            int reply = JOptionPane.showConfirmDialog(rootPane,
                    "Apakah yakin ingin menghapus aturan '" + namaAturan + "' (" + idAturan + ")?",
                    "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION) {
                Sequel.meghapus("set_aturan_jm_bpjs", "id_aturan", idAturan);
                KalkulatorJasaMedisDokter.reloadCache(koneksi);
                tampil();
                emptTeks();
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "Silahkan pilih baris aturan yang ingin dihapus pada tabel!");
        }
    }//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnHapusActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnBatal, BtnGanti);
        }
    }//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnGantiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGantiActionPerformed
        if (txtIdAturan.getText().trim().equals("")) {
            Valid.textKosong(txtIdAturan, "ID Aturan");
        } else if (txtNamaAturan.getText().trim().equals("")) {
            Valid.textKosong(txtNamaAturan, "Nama Ketentuan / Aturan");
        } else {
            try {
                ps = koneksi.prepareStatement(
                    "update set_aturan_jm_bpjs set "
                    + "kategori=?, nama_aturan=?, kondisi_pemicu=?, metode_hitung=?, "
                    + "nilai_parameter_1=?, nilai_parameter_2=?, nilai_parameter_3=?, status_aktif=?, keterangan=? "
                    + "where id_aturan=?"
                );
                try {
                    ps.setString(1, cmbKategori.getSelectedItem().toString());
                    ps.setString(2, txtNamaAturan.getText().trim());
                    ps.setString(3, txtKondisi.getText().trim());
                    ps.setString(4, cmbMetode.getSelectedItem().toString());
                    ps.setDouble(5, Double.parseDouble(txtParam1.getText().trim().isEmpty() ? "0" : txtParam1.getText().trim().replace(",", "")));
                    ps.setDouble(6, Double.parseDouble(txtParam2.getText().trim().isEmpty() ? "0" : txtParam2.getText().trim().replace(",", "")));
                    ps.setDouble(7, Double.parseDouble(txtParam3.getText().trim().isEmpty() ? "0" : txtParam3.getText().trim().replace(",", "")));
                    ps.setString(8, cmbStatus.getSelectedItem().toString());
                    ps.setString(9, txtKeterangan.getText().trim());
                    ps.setString(10, txtIdAturan.getText().trim());
                    ps.executeUpdate();

                    KalkulatorJasaMedisDokter.reloadCache(koneksi);
                    tampil();
                    emptTeks();
                } catch (Exception e) {
                    System.out.println("Notifikasi ganti set_aturan_jm_bpjs: " + e);
                    JOptionPane.showMessageDialog(rootPane, "Gagal memperbarui aturan: " + e.getMessage());
                } finally {
                    if (ps != null) {
                        ps.close();
                    }
                }
            } catch (Exception e) {
                System.out.println("Koneksi ganti: " + e);
            }
        }
    }//GEN-LAST:event_BtnGantiActionPerformed

    private void BtnGantiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnGantiKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnGantiActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnHapus, BtnToggle);
        }
    }//GEN-LAST:event_BtnGantiKeyPressed

    private void BtnToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnToggleActionPerformed
        // Toggle status Aktif <-> Tidak Aktif untuk baris terpilih atau seluruh baris yang dicentang
        int row = tbAturan.getSelectedRow();
        boolean adaPilihan = false;

        // Cek apakah ada yang dicentang di kolom 0
        for (int r = 0; r < tabMode.getRowCount(); r++) {
            if (Boolean.TRUE.equals(tabMode.getValueAt(r, 0))) {
                adaPilihan = true;
                String idAturan = tabMode.getValueAt(r, 1).toString();
                Sequel.mengedit("set_aturan_jm_bpjs", "id_aturan='" + idAturan + "'",
                        "status_aktif = IF(status_aktif='Aktif', 'Tidak Aktif', 'Aktif')");
            }
        }

        // Jika tidak ada yang dicentang namun ada baris yang dipilih kursor
        if (!adaPilihan && row != -1) {
            String idAturan = tabMode.getValueAt(row, 1).toString();
            Sequel.mengedit("set_aturan_jm_bpjs", "id_aturan='" + idAturan + "'",
                    "status_aktif = IF(status_aktif='Aktif', 'Tidak Aktif', 'Aktif')");
            adaPilihan = true;
        }

        if (adaPilihan) {
            KalkulatorJasaMedisDokter.reloadCache(koneksi);
            tampil();
            if (row != -1 && row < tbAturan.getRowCount()) {
                tbAturan.setRowSelectionInterval(row, row);
                getData();
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "Pilih baris pada tabel atau centang kolom 'P' untuk mengubah status On/Off!");
        }
    }//GEN-LAST:event_BtnToggleActionPerformed

    private void BtnToggleKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnToggleKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnToggleActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnGanti, BtnKeluar);
        }
    }//GEN-LAST:event_BtnToggleKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            dispose();
        } else {
            Valid.pindah(evt, BtnToggle, TCari);
        }
    }//GEN-LAST:event_BtnKeluarKeyPressed

    public void emptTeks() {
        txtIdAturan.setText("");
        txtNamaAturan.setText("");
        txtKondisi.setText("");
        txtParam1.setText("0");
        txtParam2.setText("0");
        txtParam3.setText("0");
        txtKeterangan.setText("-");
        cmbKategori.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
        cmbMetode.setSelectedIndex(0);
        txtIdAturan.requestFocus();
    }

    private void getData() {
        int row = tbAturan.getSelectedRow();
        if (row != -1) {
            txtIdAturan.setText(tabMode.getValueAt(row, 1).toString());
            cmbKategori.setSelectedItem(tabMode.getValueAt(row, 2).toString());
            txtNamaAturan.setText(tabMode.getValueAt(row, 3).toString());
            txtKondisi.setText(tabMode.getValueAt(row, 4).toString());
            cmbMetode.setSelectedItem(tabMode.getValueAt(row, 5).toString());
            txtParam1.setText(tabMode.getValueAt(row, 6).toString());
            txtParam2.setText(tabMode.getValueAt(row, 7).toString());
            txtParam3.setText(tabMode.getValueAt(row, 8).toString());
            cmbStatus.setSelectedItem(tabMode.getValueAt(row, 9).toString());
            txtKeterangan.setText(tabMode.getValueAt(row, 10).toString());
        }
    }

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            String sqlKategori = "";
            if (cmbFilterKategori != null && cmbFilterKategori.getSelectedItem() != null) {
                String kat = cmbFilterKategori.getSelectedItem().toString().trim();
                if (!kat.equalsIgnoreCase("Semua") && !kat.isEmpty()) {
                    sqlKategori = " and kategori='" + kat + "' ";
                }
            }

            String sqlStatus = "";
            if (cmbFilterStatus != null && cmbFilterStatus.getSelectedItem() != null) {
                String st = cmbFilterStatus.getSelectedItem().toString().trim();
                if (!st.equalsIgnoreCase("Semua") && !st.isEmpty()) {
                    sqlStatus = " and status_aktif='" + st + "' ";
                }
            }

            String sqlCari = "";
            if (!TCari.getText().trim().isEmpty()) {
                sqlCari = " and (id_aturan like '%" + TCari.getText().trim() + "%' or "
                        + "nama_aturan like '%" + TCari.getText().trim() + "%' or "
                        + "kondisi_pemicu like '%" + TCari.getText().trim() + "%' or "
                        + "keterangan like '%" + TCari.getText().trim() + "%') ";
            }

            String sql = "SELECT id_aturan, kategori, nama_aturan, kondisi_pemicu, metode_hitung, "
                    + "nilai_parameter_1, nilai_parameter_2, nilai_parameter_3, status_aktif, keterangan "
                    + "FROM set_aturan_jm_bpjs WHERE 1=1 "
                    + sqlKategori + sqlStatus + sqlCari
                    + " ORDER BY FIELD(kategori, 'Rawat Jalan', 'Rawat Inap', 'Tindakan Khusus'), id_aturan ASC";

            ps = koneksi.prepareStatement(sql);
            try {
                rs = ps.executeQuery();
                int count = 0;
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        false,
                        rs.getString("id_aturan"),
                        rs.getString("kategori"),
                        rs.getString("nama_aturan"),
                        rs.getString("kondisi_pemicu"),
                        rs.getString("metode_hitung"),
                        rs.getDouble("nilai_parameter_1"),
                        rs.getDouble("nilai_parameter_2"),
                        rs.getDouble("nilai_parameter_3"),
                        rs.getString("status_aktif"),
                        rs.getString("keterangan")
                    });
                    count++;
                }
                LCount.setText("Record : " + count);
            } catch (Exception e) {
                System.out.println("Notifikasi tampil set_aturan_jm_bpjs: " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi SQL: " + e);
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnGanti;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnSimpan;
    private widget.Button BtnToggle;
    private widget.Label LCount;
    private widget.panelisi PanelInput;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.ComboBox cmbFilterKategori;
    private widget.ComboBox cmbFilterStatus;
    private widget.ComboBox cmbKategori;
    private widget.ComboBox cmbMetode;
    private widget.ComboBox cmbStatus;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel6;
    private widget.Label lblFilterKategori;
    private widget.Label lblFilterStatus;
    private widget.Label lblIdAturan;
    private widget.Label lblKategori;
    private widget.Label lblKeterangan;
    private widget.Label lblKondisi;
    private widget.Label lblMetode;
    private widget.Label lblNamaAturan;
    private widget.Label lblParam1;
    private widget.Label lblParam2;
    private widget.Label lblParam3;
    private widget.Label lblStatus;
    private widget.panelisi panelBarisInput1;
    private widget.panelisi panelBarisInput2;
    private widget.panelisi panelBarisInput3;
    private widget.panelisi panelBawah;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.Table tbAturan;
    private widget.TextBox txtIdAturan;
    private widget.TextBox txtKeterangan;
    private widget.TextBox txtKondisi;
    private widget.TextBox txtNamaAturan;
    private widget.TextBox txtParam1;
    private widget.TextBox txtParam2;
    private widget.TextBox txtParam3;
    // End of variables declaration//GEN-END:variables
}

