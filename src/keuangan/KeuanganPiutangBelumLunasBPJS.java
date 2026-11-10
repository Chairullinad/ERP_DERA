package keuangan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import raven.toast.Notifications;

/**
 *
 * @author perpustakaan
 */
public final class KeuanganPiutangBelumLunasBPJS extends javax.swing.JDialog {

    private final DefaultTableModel tabMode;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private PreparedStatement ps, psstatusklaim;
    private ResultSet rs, rsstatusklaim;
    private int row = 0, jml = 0, i = 0, index = 0;
    private String koderekening = "", notagihan = "", nomorpemasukan = "", carabayar = "", koderekakunpiutang = "", namarekakunpiutang = "";
    private Jurnal jur = new Jurnal();
    private String status = "", Diskon_Piutang = "", Piutang_Tidak_Terbayar = "", Lebih_Bayar_Piutang = "";
    private double total = 0, lebihbayar = 0, totalumbal = 0, totaltopup = 0, totalrugi = 0, totallebih = 0;
    private boolean sukses = true;
    private File file;
    private FileWriter fileWriter;
    private String iyem;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode response;
    private FileReader myObj;

    // Properly define arrays matching the table model columns
    private boolean[] P;
    private String[] noRawat, tglPiutang, pasien, noSep, tglTempo, namaBayar, statusKlaim, kdPj, noVpk, poli, dokterRegis, dokterSep;
    private double[] totalPiutang, uangMuka, cicilanBayar, sisaPiutang, bayar, lebihBayar, tidakTerbayar, umbal, topup;

    /**
     * Creates new form DlgLhtBiaya
     *
     * @param parent
     * @param modal
     */
    public KeuanganPiutangBelumLunasBPJS(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8, 1);

        WindowHitungPaket.setSize(320, 200);

        tabMode = new DefaultTableModel(null, new Object[]{
            "P", "No.Rawat/No.Tagihan", "Tgl.Piutang", "Pasien", "No SEP", "Total Piutang",
            "Uang Muka", "Cicilan+Disk", "Sisa Piutang", "Jatuh Tempo", "Cara Bayar", "Bayar",
            "Lebih Bayar", "Tidak Terbayar", "Umbal", "Topup", "Status", "KdPJ", "No VPK", "Poli", "Dokter Regis", "Dokter SEP"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                boolean a = false;
                if ((colIndex == 11) || (colIndex == 12) || (colIndex == 13) || (colIndex == 0) || (colIndex == 14)) {
                    a = true;
                }
                return a;
            }
            Class[] types = new Class[]{
                java.lang.Boolean.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class, java.lang.Double.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbBangsal.setModel(tabMode);
        //tbBangsal.setDefaultRenderer(Object.class, new WarnaTable(jPanel2.getBackground(),tbBangsal.getBackground()));
        tbBangsal.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbBangsal.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 22; i++) {
            TableColumn column = tbBangsal.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(20);
            } else if (i == 1) {
                column.setPreferredWidth(120);
            } else if (i == 2) {
                column.setPreferredWidth(80);
            } else if (i == 3) {
                column.setPreferredWidth(170);
            } else if (i == 4) {
                column.setPreferredWidth(150);
            } else if (i == 5) {
                column.setPreferredWidth(95);
            } else if (i == 6) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 7) {
                column.setPreferredWidth(80);
            } else if (i == 8) {
                column.setPreferredWidth(95);
            } else if (i == 9) {
                column.setPreferredWidth(80);
            } else if (i == 10) {
                column.setPreferredWidth(150);
            } else if (i == 11) {
                column.setPreferredWidth(85);
            } else if (i == 12) {
                column.setPreferredWidth(85);
            } else if (i == 13) {
                column.setPreferredWidth(100);
            } else if (i == 14) {
                column.setPreferredWidth(85);
            } else if (i == 15) {
                column.setPreferredWidth(85);
            } else if (i == 16) {
                column.setPreferredWidth(85);
            } else if (i == 17) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 18) {
                column.setPreferredWidth(125);
            } else {
                column.setPreferredWidth(200);
            }
        }
        tbBangsal.setDefaultRenderer(Object.class, new WarnaTable());
        tbBangsal.setRowSorter(null);

        TKd.setDocument(new batasInput((byte) 20).getKata(TKd));
        NilaiKapitasiPaket.setDocument(new batasInput((byte) 20).getKata(NilaiKapitasiPaket));

        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
//                        if (nmpenjab.getText().equals("")) {
//                            tampil();
//                        } else {
//                            tampilperakun();
//                        }
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
//                        if (nmpenjab.getText().equals("")) {
//                            tampil();
//                        } else {
//                            tampilperakun();
//                        }
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
//                        if (nmpenjab.getText().equals("")) {
//                            tampil();
//                        } else {
//                            tampilperakun();
//                        }
                    }
                }
            });
        }

        try {
            ps = koneksi.prepareStatement(
                    "select set_akun.Diskon_Piutang,set_akun.Kerugian_Klaim_BPJS_RVP,set_akun.Lebih_Bayar_Klaim_BPJS_RVP from set_akun");
            try {
                rs = ps.executeQuery();
                if (rs.next()) {
                    Diskon_Piutang = rs.getString("Diskon_Piutang");
                    Piutang_Tidak_Terbayar = rs.getString("Kerugian_Klaim_BPJS_RVP");
                    Lebih_Bayar_Piutang = rs.getString("Lebih_Bayar_Klaim_BPJS_RVP");
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

    double sisapiutang = 0, cicilan = 0;

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        TKd = new widget.TextBox();
        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnDetailPiutang = new javax.swing.JMenuItem();
        ppPilihSemua = new javax.swing.JMenuItem();
        ppBersihkan = new javax.swing.JMenuItem();
        ppKapitasiPaket = new javax.swing.JMenuItem();
        WindowHitungPaket = new javax.swing.JDialog();
        internalFrame8 = new widget.InternalFrame();
        jLabel42 = new widget.Label();
        panelisi5 = new widget.panelisi();
        BtnCloseHitungKapitasi = new widget.Button();
        BtnHitungKapitasi = new widget.Button();
        jLabel43 = new widget.Label();
        LebihBayarPiutang = new widget.TextBox();
        PersentaseBayarPaket = new widget.TextBox();
        jLabel41 = new widget.Label();
        NilaiKapitasiPaket = new widget.TextBox();
        jLabel40 = new widget.Label();
        jLabel39 = new widget.Label();
        PiutangBelumDibayar = new widget.TextBox();
        jLabel44 = new widget.Label();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbBangsal = new widget.Table();
        panelisi4 = new widget.panelisi();
        label32 = new widget.Label();
        Tanggal = new widget.Tanggal();
        jLabel11 = new widget.Label();
        AkunBayar = new widget.ComboBox();
        BtnAll1 = new widget.Button();
        jLabel8 = new widget.Label();
        CmbStts = new widget.ComboBox();
        jLabel9 = new widget.Label();
        CmbStatusKlaim = new widget.ComboBox();
        jLabelStatusPiutang = new widget.Label();
        CmbStatusPiutang = new widget.ComboBox();
        label19 = new widget.Label();
        keterangan = new widget.TextBox();
        jPanel1 = new javax.swing.JPanel();
        panelisi3 = new widget.panelisi();
        ChkTanggal = new widget.CekBox();
        Tgl1 = new widget.Tanggal();
        label18 = new widget.Label();
        Tgl2 = new widget.Tanggal();
        label17 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        jLabel18 = new widget.Label();
        CmbSortBy = new widget.ComboBox();
        jLabel17 = new javax.swing.JLabel();
        LCountData = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        LCount = new javax.swing.JLabel();
        panelisi1 = new widget.panelisi();
        jLabel12 = new javax.swing.JLabel();
        LCount1 = new javax.swing.JLabel();
        jLabelBiayaRiil = new javax.swing.JLabel();
        LCountBiayaRiil = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        LCountUmbal = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        LCountTopup = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        LCountTotalRugi = new javax.swing.JLabel();
        jLabel16 = new javax.swing.JLabel();
        LCountTotalLebih = new javax.swing.JLabel();
        BtnBayar = new widget.Button();
        BtnKeluar = new widget.Button();

        TKd.setForeground(new java.awt.Color(255, 255, 255));
        TKd.setName("TKd"); // NOI18N

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnDetailPiutang.setBackground(new java.awt.Color(255, 255, 254));
        MnDetailPiutang.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnDetailPiutang.setForeground(new java.awt.Color(50, 50, 50));
        MnDetailPiutang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnDetailPiutang.setText("Detail Piutang");
        MnDetailPiutang.setName("MnDetailPiutang"); // NOI18N
        MnDetailPiutang.setPreferredSize(new java.awt.Dimension(170, 26));
        MnDetailPiutang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnDetailPiutangActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnDetailPiutang);

        ppPilihSemua.setBackground(new java.awt.Color(255, 255, 254));
        ppPilihSemua.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppPilihSemua.setForeground(new java.awt.Color(50, 50, 50));
        ppPilihSemua.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppPilihSemua.setText("Pilih Semua");
        ppPilihSemua.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppPilihSemua.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppPilihSemua.setName("ppPilihSemua"); // NOI18N
        ppPilihSemua.setPreferredSize(new java.awt.Dimension(170, 26));
        ppPilihSemua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppPilihSemuaActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppPilihSemua);

        ppBersihkan.setBackground(new java.awt.Color(255, 255, 254));
        ppBersihkan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppBersihkan.setForeground(new java.awt.Color(50, 50, 50));
        ppBersihkan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppBersihkan.setText("Hilangkan Pilihan");
        ppBersihkan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppBersihkan.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppBersihkan.setName("ppBersihkan"); // NOI18N
        ppBersihkan.setPreferredSize(new java.awt.Dimension(170, 26));
        ppBersihkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppBersihkanActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppBersihkan);

        ppKapitasiPaket.setBackground(new java.awt.Color(255, 255, 254));
        ppKapitasiPaket.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppKapitasiPaket.setForeground(new java.awt.Color(50, 50, 50));
        ppKapitasiPaket.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppKapitasiPaket.setText("Hitung Kapitasi/Paket");
        ppKapitasiPaket.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppKapitasiPaket.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppKapitasiPaket.setName("ppKapitasiPaket"); // NOI18N
        ppKapitasiPaket.setPreferredSize(new java.awt.Dimension(170, 26));
        ppKapitasiPaket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppKapitasiPaketActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppKapitasiPaket);

        WindowHitungPaket.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        WindowHitungPaket.setName("WindowHitungPaket"); // NOI18N
        WindowHitungPaket.setUndecorated(true);
        WindowHitungPaket.setResizable(false);

        internalFrame8.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Hitung Kapitasi/Paket Pembayaran ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame8.setName("internalFrame8"); // NOI18N
        internalFrame8.setLayout(new java.awt.BorderLayout());

        jLabel42.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel42.setText("%");
        jLabel42.setName("jLabel42"); // NOI18N
        internalFrame8.add(jLabel42, java.awt.BorderLayout.CENTER);

        panelisi5.setName("panelisi5"); // NOI18N
        panelisi5.setPreferredSize(new java.awt.Dimension(100, 44));
        panelisi5.setLayout(null);

        BtnCloseHitungKapitasi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/cross.png"))); // NOI18N
        BtnCloseHitungKapitasi.setMnemonic('U');
        BtnCloseHitungKapitasi.setText("Tutup");
        BtnCloseHitungKapitasi.setToolTipText("Alt+U");
        BtnCloseHitungKapitasi.setName("BtnCloseHitungKapitasi"); // NOI18N
        BtnCloseHitungKapitasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCloseHitungKapitasiActionPerformed(evt);
            }
        });
        panelisi5.add(BtnCloseHitungKapitasi);
        BtnCloseHitungKapitasi.setBounds(200, 135, 100, 30);

        BtnHitungKapitasi.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/gaji.png"))); // NOI18N
        BtnHitungKapitasi.setMnemonic('S');
        BtnHitungKapitasi.setText("Hitung");
        BtnHitungKapitasi.setToolTipText("Alt+S");
        BtnHitungKapitasi.setName("BtnHitungKapitasi"); // NOI18N
        BtnHitungKapitasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHitungKapitasiActionPerformed(evt);
            }
        });
        panelisi5.add(BtnHitungKapitasi);
        BtnHitungKapitasi.setBounds(10, 135, 100, 30);

        jLabel43.setText("Lebih Bayar Piutang :");
        jLabel43.setName("jLabel43"); // NOI18N
        panelisi5.add(jLabel43);
        jLabel43.setBounds(0, 100, 140, 23);

        LebihBayarPiutang.setText("0");
        LebihBayarPiutang.setHighlighter(null);
        LebihBayarPiutang.setName("LebihBayarPiutang"); // NOI18N
        panelisi5.add(LebihBayarPiutang);
        LebihBayarPiutang.setBounds(144, 100, 150, 23);

        PersentaseBayarPaket.setEditable(false);
        PersentaseBayarPaket.setText("0");
        PersentaseBayarPaket.setHighlighter(null);
        PersentaseBayarPaket.setName("PersentaseBayarPaket"); // NOI18N
        panelisi5.add(PersentaseBayarPaket);
        PersentaseBayarPaket.setBounds(144, 70, 136, 23);

        jLabel41.setText("Persentase Pembayaran :");
        jLabel41.setName("jLabel41"); // NOI18N
        panelisi5.add(jLabel41);
        jLabel41.setBounds(0, 70, 140, 23);

        NilaiKapitasiPaket.setText("0");
        NilaiKapitasiPaket.setHighlighter(null);
        NilaiKapitasiPaket.setName("NilaiKapitasiPaket"); // NOI18N
        NilaiKapitasiPaket.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NilaiKapitasiPaketKeyPressed(evt);
            }
        });
        panelisi5.add(NilaiKapitasiPaket);
        NilaiKapitasiPaket.setBounds(144, 40, 150, 23);

        jLabel40.setText("Nilai Kapitasi/Paket :");
        jLabel40.setName("jLabel40"); // NOI18N
        panelisi5.add(jLabel40);
        jLabel40.setBounds(0, 40, 140, 23);

        jLabel39.setText("Piutang Belum Dibayar :");
        jLabel39.setName("jLabel39"); // NOI18N
        panelisi5.add(jLabel39);
        jLabel39.setBounds(0, 10, 140, 23);

        PiutangBelumDibayar.setEditable(false);
        PiutangBelumDibayar.setText("0");
        PiutangBelumDibayar.setHighlighter(null);
        PiutangBelumDibayar.setName("PiutangBelumDibayar"); // NOI18N
        panelisi5.add(PiutangBelumDibayar);
        PiutangBelumDibayar.setBounds(144, 10, 150, 23);

        jLabel44.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel44.setText("%");
        jLabel44.setName("jLabel44"); // NOI18N
        panelisi5.add(jLabel44);
        jLabel44.setBounds(280, 70, 50, 23);

        internalFrame8.add(panelisi5, java.awt.BorderLayout.CENTER);

        WindowHitungPaket.getContentPane().add(internalFrame8, java.awt.BorderLayout.CENTER);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Piutang Belum Lunas Payor BPJS ]::", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setComponentPopupMenu(jPopupMenu1);
        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbBangsal.setComponentPopupMenu(jPopupMenu1);
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

        panelisi4.setName("panelisi4"); // NOI18N
        panelisi4.setPreferredSize(new java.awt.Dimension(100, 50));
        panelisi4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        label32.setText("Tanggal Bayar :");
        label32.setName("label32"); // NOI18N
        label32.setPreferredSize(new java.awt.Dimension(90, 23));
        panelisi4.add(label32);

        Tanggal.setDisplayFormat("dd-MM-yyyy HH:mm:ss");
        Tanggal.setFont(Tanggal.getFont());
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setPreferredSize(new java.awt.Dimension(150, 23));
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        panelisi4.add(Tanggal);

        jLabel11.setText("Akun Bayar :");
        jLabel11.setName("jLabel11"); // NOI18N
        jLabel11.setPreferredSize(new java.awt.Dimension(75, 23));
        panelisi4.add(jLabel11);

        AkunBayar.setMinimumSize(new java.awt.Dimension(50, 23));
        AkunBayar.setName("AkunBayar"); // NOI18N
        AkunBayar.setPreferredSize(new java.awt.Dimension(175, 23));
        AkunBayar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                AkunBayarKeyPressed(evt);
            }
        });
        panelisi4.add(AkunBayar);

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

        jLabel8.setText("Status Rawat :");
        jLabel8.setName("jLabel8"); // NOI18N
        jLabel8.setPreferredSize(new java.awt.Dimension(100, 23));
        panelisi4.add(jLabel8);

        CmbStts.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Ranap", "Ralan" }));
        CmbStts.setLightWeightPopupEnabled(false);
        CmbStts.setMinimumSize(new java.awt.Dimension(75, 21));
        CmbStts.setName("CmbStts"); // NOI18N
        CmbStts.setPreferredSize(new java.awt.Dimension(80, 23));
        CmbStts.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CmbSttsItemStateChanged(evt);
            }
        });
        CmbStts.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                CmbSttsKeyPressed(evt);
            }
        });
        panelisi4.add(CmbStts);

        jLabel9.setText("Status Klaim :");
        jLabel9.setName("jLabel9"); // NOI18N
        jLabel9.setPreferredSize(new java.awt.Dimension(100, 23));
        panelisi4.add(jLabel9);

        CmbStatusKlaim.setLightWeightPopupEnabled(false);
        CmbStatusKlaim.setMinimumSize(new java.awt.Dimension(75, 21));
        CmbStatusKlaim.setName("CmbStatusKlaim"); // NOI18N
        CmbStatusKlaim.setPreferredSize(new java.awt.Dimension(120, 23));
        CmbStatusKlaim.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CmbStatusKlaimItemStateChanged(evt);
            }
        });
        CmbStatusKlaim.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                CmbStatusKlaimKeyPressed(evt);
            }
        });
        panelisi4.add(CmbStatusKlaim);

        jLabelStatusPiutang.setText("Status Piutang :");
        jLabelStatusPiutang.setName("jLabelStatusPiutang"); // NOI18N
        jLabelStatusPiutang.setPreferredSize(new java.awt.Dimension(85, 23));
        panelisi4.add(jLabelStatusPiutang);

        CmbStatusPiutang.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Belum Lunas", "Lunas", "Semua" }));
        CmbStatusPiutang.setLightWeightPopupEnabled(false);
        CmbStatusPiutang.setName("CmbStatusPiutang"); // NOI18N
        CmbStatusPiutang.setPreferredSize(new java.awt.Dimension(95, 23));
        CmbStatusPiutang.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CmbStatusPiutangItemStateChanged(evt);
            }
        });
        CmbStatusPiutang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                CmbStatusPiutangKeyPressed(evt);
            }
        });
        panelisi4.add(CmbStatusPiutang);

        label19.setText("Keterangan :");
        label19.setName("label19"); // NOI18N
        label19.setPreferredSize(new java.awt.Dimension(100, 23));
        panelisi4.add(label19);

        keterangan.setName("keterangan"); // NOI18N
        keterangan.setPreferredSize(new java.awt.Dimension(200, 23));
        keterangan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                keteranganKeyPressed(evt);
            }
        });
        panelisi4.add(keterangan);

        internalFrame1.add(panelisi4, java.awt.BorderLayout.PAGE_START);

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setOpaque(false);
        jPanel1.setPreferredSize(new java.awt.Dimension(100, 120));
        jPanel1.setLayout(new java.awt.BorderLayout(1, 1));

        panelisi3.setName("panelisi3"); // NOI18N
        panelisi3.setPreferredSize(new java.awt.Dimension(100, 20));
        panelisi3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        ChkTanggal.setSelected(true);
        ChkTanggal.setText("Tgl. Piutang :");
        ChkTanggal.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        ChkTanggal.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkTanggal.setName("ChkTanggal"); // NOI18N
        ChkTanggal.setPreferredSize(new java.awt.Dimension(105, 23));
        ChkTanggal.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                ChkTanggalItemStateChanged(evt);
            }
        });
        panelisi3.add(ChkTanggal);

        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setName("Tgl1"); // NOI18N
        Tgl1.setPreferredSize(new java.awt.Dimension(95, 23));
        Tgl1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Tgl1KeyPressed(evt);
            }
        });
        panelisi3.add(Tgl1);

        label18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label18.setText("s.d.");
        label18.setName("label18"); // NOI18N
        label18.setPreferredSize(new java.awt.Dimension(30, 23));
        panelisi3.add(label18);

        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2"); // NOI18N
        Tgl2.setPreferredSize(new java.awt.Dimension(95, 23));
        Tgl2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Tgl2KeyPressed(evt);
            }
        });
        panelisi3.add(Tgl2);

        label17.setText("Key Word :");
        label17.setName("label17"); // NOI18N
        label17.setPreferredSize(new java.awt.Dimension(75, 23));
        panelisi3.add(label17);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(220, 23));
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

        jLabel18.setText("Sort  by : ");
        jLabel18.setName("jLabel18"); // NOI18N
        jLabel18.setPreferredSize(new java.awt.Dimension(50, 23));
        panelisi3.add(jLabel18);
        jLabel18.getAccessibleContext().setAccessibleName("Sort by");

        CmbSortBy.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No SEP Asc", "No SEP Desc", "No Rawat Asc", "No Rawat Desc", "Tgl Piutang Asc", "Tgl Piutang Desc" }));
        CmbSortBy.setLightWeightPopupEnabled(false);
        CmbSortBy.setMinimumSize(new java.awt.Dimension(75, 21));
        CmbSortBy.setName("CmbSortBy"); // NOI18N
        CmbSortBy.setPreferredSize(new java.awt.Dimension(120, 23));
        CmbSortBy.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                CmbSortByItemStateChanged(evt);
            }
        });
        CmbSortBy.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                CmbSortByKeyPressed(evt);
            }
        });
        panelisi3.add(CmbSortBy);

        jLabel17.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jLabel17.setForeground(new java.awt.Color(50, 50, 50));
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel17.setText("Count : ");
        jLabel17.setName("jLabel17"); // NOI18N
        jLabel17.setPreferredSize(new java.awt.Dimension(50, 23));
        panelisi3.add(jLabel17);

        LCountData.setFont(LCountData.getFont().deriveFont(LCountData.getFont().getStyle() | java.awt.Font.BOLD, LCountData.getFont().getSize()-2));
        LCountData.setForeground(new java.awt.Color(50, 50, 50));
        LCountData.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCountData.setText("0");
        LCountData.setName("LCountData"); // NOI18N
        LCountData.setPreferredSize(new java.awt.Dimension(100, 23));
        panelisi3.add(LCountData);

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(50, 50, 50));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Belum Dibayar :");
        jLabel10.setName("jLabel10"); // NOI18N
        jLabel10.setPreferredSize(new java.awt.Dimension(87, 23));
        panelisi3.add(jLabel10);

        LCount.setFont(LCount.getFont().deriveFont(LCount.getFont().getStyle() | java.awt.Font.BOLD, LCount.getFont().getSize()-2));
        LCount.setForeground(new java.awt.Color(50, 50, 50));
        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(100, 23));
        panelisi3.add(LCount);

        jPanel1.add(panelisi3, java.awt.BorderLayout.CENTER);

        panelisi1.setMinimumSize(new java.awt.Dimension(681, 45));
        panelisi1.setName("panelisi1"); // NOI18N
        panelisi1.setPreferredSize(new java.awt.Dimension(55, 80));
        panelisi1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel12.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(50, 50, 50));
        jLabel12.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel12.setText("Dipilih :");
        jLabel12.setName("jLabel12"); // NOI18N
        jLabel12.setPreferredSize(new java.awt.Dimension(45, 23));
        panelisi1.add(jLabel12);

        LCount1.setFont(LCount1.getFont().deriveFont(LCount1.getFont().getStyle() | java.awt.Font.BOLD, LCount1.getFont().getSize()-2));
        LCount1.setForeground(new java.awt.Color(50, 50, 50));
        LCount1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount1.setText("0");
        LCount1.setName("LCount1"); // NOI18N
        LCount1.setPreferredSize(new java.awt.Dimension(65, 23));
        panelisi1.add(LCount1);

        jLabelBiayaRiil.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jLabelBiayaRiil.setForeground(new java.awt.Color(50, 50, 50));
        jLabelBiayaRiil.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabelBiayaRiil.setText("Biaya Riil RS :");
        jLabelBiayaRiil.setName("jLabelBiayaRiil"); // NOI18N
        jLabelBiayaRiil.setPreferredSize(new java.awt.Dimension(85, 23));
        panelisi1.add(jLabelBiayaRiil);

        LCountBiayaRiil.setFont(LCountBiayaRiil.getFont().deriveFont(LCountBiayaRiil.getFont().getStyle() | java.awt.Font.BOLD, LCountBiayaRiil.getFont().getSize()-2));
        LCountBiayaRiil.setForeground(new java.awt.Color(50, 50, 50));
        LCountBiayaRiil.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCountBiayaRiil.setText("0");
        LCountBiayaRiil.setName("LCountBiayaRiil"); // NOI18N
        LCountBiayaRiil.setPreferredSize(new java.awt.Dimension(100, 23));
        panelisi1.add(LCountBiayaRiil);

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(50, 50, 50));
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel13.setText("Umbal dipilih :");
        jLabel13.setName("jLabel13"); // NOI18N
        jLabel13.setPreferredSize(new java.awt.Dimension(65, 23));
        panelisi1.add(jLabel13);

        LCountUmbal.setFont(LCountUmbal.getFont().deriveFont(LCountUmbal.getFont().getStyle() | java.awt.Font.BOLD, LCountUmbal.getFont().getSize()-2));
        LCountUmbal.setForeground(new java.awt.Color(50, 50, 50));
        LCountUmbal.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCountUmbal.setText("0");
        LCountUmbal.setName("LCountUmbal"); // NOI18N
        LCountUmbal.setPreferredSize(new java.awt.Dimension(100, 23));
        panelisi1.add(LCountUmbal);

        jLabel14.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jLabel14.setForeground(new java.awt.Color(50, 50, 50));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel14.setText("Topup dipilih :");
        jLabel14.setName("jLabel14"); // NOI18N
        jLabel14.setPreferredSize(new java.awt.Dimension(65, 23));
        panelisi1.add(jLabel14);

        LCountTopup.setFont(LCountTopup.getFont().deriveFont(LCountTopup.getFont().getStyle() | java.awt.Font.BOLD, LCountTopup.getFont().getSize()-2));
        LCountTopup.setForeground(new java.awt.Color(50, 50, 50));
        LCountTopup.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCountTopup.setText("0");
        LCountTopup.setName("LCountTopup"); // NOI18N
        LCountTopup.setPreferredSize(new java.awt.Dimension(100, 23));
        panelisi1.add(LCountTopup);

        jLabel15.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(50, 50, 50));
        jLabel15.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel15.setText("Tot Rugi");
        jLabel15.setName("jLabel15"); // NOI18N
        jLabel15.setPreferredSize(new java.awt.Dimension(65, 23));
        panelisi1.add(jLabel15);

        LCountTotalRugi.setFont(LCountTotalRugi.getFont().deriveFont(LCountTotalRugi.getFont().getStyle() | java.awt.Font.BOLD, LCountTotalRugi.getFont().getSize()-2));
        LCountTotalRugi.setForeground(new java.awt.Color(50, 50, 50));
        LCountTotalRugi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCountTotalRugi.setText("0");
        LCountTotalRugi.setName("LCountTotalRugi"); // NOI18N
        LCountTotalRugi.setPreferredSize(new java.awt.Dimension(100, 23));
        panelisi1.add(LCountTotalRugi);

        jLabel16.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        jLabel16.setForeground(new java.awt.Color(50, 50, 50));
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel16.setText("Tot Lebih");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setPreferredSize(new java.awt.Dimension(65, 23));
        panelisi1.add(jLabel16);

        LCountTotalLebih.setFont(LCountTotalLebih.getFont().deriveFont(LCountTotalLebih.getFont().getStyle() | java.awt.Font.BOLD, LCountTotalLebih.getFont().getSize()-2));
        LCountTotalLebih.setForeground(new java.awt.Color(50, 50, 50));
        LCountTotalLebih.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCountTotalLebih.setText("0");
        LCountTotalLebih.setName("LCountTotalLebih"); // NOI18N
        LCountTotalLebih.setPreferredSize(new java.awt.Dimension(100, 23));
        panelisi1.add(LCountTotalLebih);

        BtnBayar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnBayar.setMnemonic('B');
        BtnBayar.setText("Bayar");
        BtnBayar.setToolTipText("Alt+B");
        BtnBayar.setName("BtnBayar"); // NOI18N
        BtnBayar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBayarActionPerformed(evt);
            }
        });
        BtnBayar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnBayarKeyPressed(evt);
            }
        });
        panelisi1.add(BtnBayar);

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
        carabayar = "";
        tampil();

}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnAllActionPerformed(null);
        } else {

        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbBangsalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbBangsalKeyPressed
        if (tabMode.getRowCount() != 0) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                getdata();
            }
        }
}//GEN-LAST:event_tbBangsalKeyPressed

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
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        tampil();
        this.setCursor(Cursor.getDefaultCursor());

}//GEN-LAST:event_BtnCariActionPerformed

private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            tampil();

        } else {
            Valid.pindah(evt, TKd, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

private void MnDetailPiutangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnDetailPiutangActionPerformed
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        } else {
            if (tbBangsal.getSelectedRow() != -1) {
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                status = Sequel.cariIsi("select reg_periksa.status_lanjut from reg_periksa where reg_periksa.no_rawat=?", tbBangsal.getValueAt(tbBangsal.getSelectedRow(), 1).toString());
                if (status.equals("Ralan")) {
                    DlgBilingRalan billing = new DlgBilingRalan(null, false);
                    billing.TNoRw.setText(tbBangsal.getValueAt(tbBangsal.getSelectedRow(), 1).toString());
                    billing.isCek();
                    billing.isRawat();
                    if (Sequel.cariInteger("select count(piutang_pasien.no_rawat) from piutang_pasien where piutang_pasien.no_rawat=?", billing.TNoRw.getText()) > 0) {
                        billing.setPiutang();
                    }
                    billing.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
                    billing.setLocationRelativeTo(internalFrame1);
                    billing.setVisible(true);
                } else if (status.equals("Ranap")) {
                    DlgBilingRanap billing = new DlgBilingRanap(null, false);
                    billing.TNoRw.setText(tbBangsal.getValueAt(tbBangsal.getSelectedRow(), 1).toString());
                    billing.isCek();
                    billing.isRawat();
                    billing.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
                    billing.setLocationRelativeTo(internalFrame1);
                    billing.setVisible(true);
                }
                this.setCursor(Cursor.getDefaultCursor());
            } else {
                JOptionPane.showMessageDialog(null, "Silahkan pilih data terlebih dahulu...!!");
            }
        }
}//GEN-LAST:event_MnDetailPiutangActionPerformed

    private void BtnBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBayarActionPerformed

        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda proses...!!!!");
        } else if (total <= 0) {
            JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih piutang yang mau dibayar..!!!!");
            TCari.requestFocus();
        } else {
            i = JOptionPane.showConfirmDialog(rootPane, "Yakin semua data pembayaran piutang sudah benar?\n"
                    + "Total Data : " + LCountData.getText() + " \n Jumlah pembayaran sebesar " + Valid.SetAngka(total) + "", "Konfirmasi", JOptionPane.YES_NO_OPTION);
            if (i == JOptionPane.YES_OPTION) {

//                BtnCariActionPerformed(null);
                this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                Sequel.AutoComitFalse();
                sukses = true;
                koderekening = "";

                try {
                    myObj = new FileReader("./cache/akunbayar.iyem");
                    root = mapper.readTree(myObj);
                    response = root.path("akunbayar");
                    if (response.isArray()) {
                        for (JsonNode list : response) {
                            if (list.path("NamaAkun").asText().equals(AkunBayar.getSelectedItem().toString())) {
                                koderekening = list.path("KodeRek").asText();
                            }
                        }
                    }
                    myObj.close();
                } catch (Exception e) {
                    sukses = false;
                }

                for (i = 0; i < tbBangsal.getRowCount(); i++) {
                    if (tbBangsal.getValueAt(i, 0).toString().equals("true")) {
                        String norawatambil = tbBangsal.getValueAt(i, 1).toString();
                        String nosepasien = tbBangsal.getValueAt(i, 4).toString();
                        double lebihBayarAmbil = Double.parseDouble(String.valueOf(tbBangsal.getValueAt(i, 12)));
                        double nilaiBayarAmbil = Double.parseDouble(String.valueOf(tbBangsal.getValueAt(i, 11)));
//                        double diskonAmbil = Double.parseDouble(String.valueOf(tbBangsal.getValueAt(i, 12)));
                        double tidakTerbayarAmb = Double.parseDouble(String.valueOf(tbBangsal.getValueAt(i, 13)));
                        double umbal = Double.parseDouble(String.valueOf(tbBangsal.getValueAt(i, 14)));
                        double sisaPiutangambil = Double.parseDouble(String.valueOf(tbBangsal.getValueAt(i, 8)));
                        String noRMPasienAmbil = Sequel.cariIsi("select reg_periksa.no_rkm_medis from reg_periksa where reg_periksa.no_rawat=?", tbBangsal.getValueAt(i, 1).toString());
                        if (sisaPiutangambil <= 0) {
                            JOptionPane.showMessageDialog(null, "Piutang pasien ini sudah lunas, tidak dapat dilakukan pembayaran lagi...!!");
                            tbBangsal.setValueAt(false, i, 0);
                            sukses = false;
                        } else {
                            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                            ambilkoderekpiutang(norawatambil);
                            if (Sequel.menyimpantf("bayar_piutang", "?,?,?,?,?,?,?,?,?,?,?,?", "Data", 12, new String[]{
                                Valid.SetTgl(Tanggal.getSelectedItem() + ""),
                                noRMPasienAmbil,
                                "" + nilaiBayarAmbil,
                                "pelunasan oleh " + akses.getkode(),
                                norawatambil,
                                koderekening,
                                koderekakunpiutang,
                                "0",
                                Diskon_Piutang,
                                "" + tidakTerbayarAmb,
                                Piutang_Tidak_Terbayar,
                                keterangan.getText()
                            })) {
                                double sisapiutangsisa = Sequel.cariIsiAngka("SELECT ifnull(SUM(piutang_pasien.sisapiutang),0) FROM piutang_pasien where no_rawat=?", norawatambil)
                                        - Sequel.cariIsiAngka("SELECT ifnull(SUM(bayar_piutang.besar_cicilan)+SUM(bayar_piutang.diskon_piutang),0) FROM bayar_piutang where no_rawat=?", norawatambil);

                                if (sisapiutangsisa <= 1) {
                                    Sequel.mengedit("piutang_pasien", "no_rawat='" + norawatambil + "'", "status='Lunas'");
                                }

                                Sequel.mengedit("detail_piutang_pasien", "no_rawat='" + norawatambil + "' and nama_bayar='" + namarekakunpiutang + "'",
                                        "sisapiutang=sisapiutang-" + (nilaiBayarAmbil));

                                Sequel.queryu("delete from tampjurnal");

                                if (!Sequel.menyimpantf2("tampjurnal", "'" + koderekakunpiutang + "','PENERIMAAN PIUTANG BPJS PASIEN " + tbBangsal.getValueAt(i, 3) + " NO SEP " + nosepasien + "','0','" + (nilaiBayarAmbil) + "'", "Rekening")) {
                                    sukses = false;
                                }

                                if (nilaiBayarAmbil > 0 && !Sequel.menyimpantf2("tampjurnal", "'" + koderekening + "','" + AkunBayar.getSelectedItem() + "','" + umbal + "','0'", "Rekening")) {
                                    sukses = false;
                                }

                                if (lebihBayarAmbil > 0 && !Sequel.menyimpantf2("tampjurnal", "'" + Lebih_Bayar_Piutang + "','LEBIH BAYAR " + nosepasien + "','0','" + lebihBayarAmbil + "'", "Rekening")) {
                                    sukses = false;
                                }

                                if (tidakTerbayarAmb > 0 && !Sequel.menyimpantf2("tampjurnal", "'" + Piutang_Tidak_Terbayar + "','PIUTANG TIDAK TERBAYAR','" + tidakTerbayarAmb + "','0'", "Rekening")) {
                                    sukses = false;
                                }

                                if (!notagihan.equals("")) {
                                    if (!jur.simpanJurnal(norawatambil, "U", "PENERIMAAN PIUTANG BPJS KET : " + keterangan.getText() + " " + notagihan + " PASIEN " + tbBangsal.getValueAt(i, 3) + " NO SEP " + nosepasien + " NO FPK " + tbBangsal.getValueAt(i, 18) + " , OLEH " + akses.getkode())) {
                                        sukses = false;
                                    }
                                } else {
                                    if (!jur.simpanJurnal(norawatambil, "U", "PENERIMAAN PIUTANG BPJS PASIEN " + tbBangsal.getValueAt(i, 3) + " NO SEP " + nosepasien + " NO FPK " + tbBangsal.getValueAt(i, 18) + " , OLEH " + akses.getkode())) {
                                        sukses = false;
                                    }
                                }

                            } else {
                                sukses = false;
                            }

                            this.setCursor(Cursor.getDefaultCursor());
                        }

                    }
                }

                if (sukses) {
                    Sequel.Commit();
                    JOptionPane.showMessageDialog(null, "Proses Selesai");
                } else {
                    JOptionPane.showMessageDialog(null, "Terjadi kesalahan saat pemrosesan data, transaksi dibatalkan.\nPeriksa kembali data sebelum melanjutkan menyimpan..!!");
                    Sequel.RollBack();
                }

                Sequel.AutoComitTrue();

                if (sukses) {
                    for (int rowClear = 0; rowClear < tbBangsal.getRowCount(); rowClear++) {
                        tbBangsal.setValueAt(false, rowClear, 0);
                    }

                    if (!notagihan.equals("")) {
                        Sequel.queryu("update penagihan_piutang set status='Sudah Dibayar' where no_tagihan=?", notagihan);
                        notagihan = "";
                    }

                    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    DlgJurnalHarian form = new DlgJurnalHarian(null, false);
                    form.emptTeks();
                    form.lihatJurnal("PENERIMAAN PIUTANG BPJS PASIEN");
                    form.setSize(internalFrame1.getWidth(), internalFrame1.getHeight());
                    form.setLocationRelativeTo(internalFrame1);
                    form.setAlwaysOnTop(false);
                    form.setVisible(true);
                    this.setCursor(Cursor.getDefaultCursor());
                }
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnBayarActionPerformed

    private void BtnBayarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBayarKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnBayarKeyPressed

    private void AkunBayarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_AkunBayarKeyPressed
        Valid.pindah(evt, Tanggal, TCari);
    }//GEN-LAST:event_AkunBayarKeyPressed

    private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed

    }//GEN-LAST:event_TanggalKeyPressed

    private void tbBangsalMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbBangsalMouseClicked
        if (tabMode.getRowCount() != 0) {
//            if (tbBangsal.getSelectedColumn() == 0 && tbBangsal.getValueAt(tbBangsal.getSelectedRow(), 16).toString().equals("3#Klaim")) {
//                if (tbBangsal.getSelectedColumn() == 0) {
//                    getdata();
//                }
//            } else {
//                Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT, "Klaim belum terbayar/Status : " + tbBangsal.getValueAt(tbBangsal.getSelectedRow(), 16).toString() + "");
//                tbBangsal.setValueAt(false, tbBangsal.getSelectedRow(), 0);
//            }

            if (tbBangsal.getSelectedColumn() == 0) {
                if (tbBangsal.getValueAt(tbBangsal.getSelectedRow(), 16).toString().equals("3#Klaim")) {
                    getdata();
                } else {
                    Notifications.getInstance().show(Notifications.Type.WARNING, Notifications.Location.TOP_RIGHT,
                            "Klaim belum terbayar/Status : " + tbBangsal.getValueAt(tbBangsal.getSelectedRow(), 16).toString());
                    tbBangsal.setValueAt(false, tbBangsal.getSelectedRow(), 0);
                }
            }

        }
    }//GEN-LAST:event_tbBangsalMouseClicked

    private void tbBangsalPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_tbBangsalPropertyChange
        if (this.isVisible() == true) {
            total = 0;
            totallebih = 0;
            totalrugi = 0;
            totaltopup = 0;
            totalumbal = 0;
            try {
                if (tbBangsal.getSelectedRow() != -1) {
                    if (tbBangsal.getValueAt(tbBangsal.getSelectedRow(), 0).toString().equals("true")) {
                        HitungSelisihLebihKurang(tbBangsal.getSelectedRow());
                    }
                }
            } catch (Exception e) {
                // Consider adding proper error handling here
                e.printStackTrace(); // At minimum, print the stack trace for debugging
            }

            row = tbBangsal.getRowCount();
            Set<String> setSep = new HashSet<>();
            int countTanpaSep = 0;
            double totalBiayaRiil = 0;
            for (i = 0; i < row; i++) {
                if (tbBangsal.getValueAt(i, 0).toString().equals("true")) {
                    String sep = tbBangsal.getValueAt(i, 4) != null ? tbBangsal.getValueAt(i, 4).toString().trim() : "";
                    if (!sep.isEmpty() && !sep.equals("-")) {
                        setSep.add(sep);
                    } else {
                        countTanpaSep++;
                    }
                    totalBiayaRiil = totalBiayaRiil + Double.parseDouble(tbBangsal.getValueAt(i, 11).toString());
                    total = total + Double.parseDouble(tbBangsal.getValueAt(i, 8).toString());
                    totallebih = totallebih + Double.parseDouble(tbBangsal.getValueAt(i, 12).toString());
                    totalrugi = totalrugi + Double.parseDouble(tbBangsal.getValueAt(i, 13).toString());
                    totalumbal = totalumbal + Double.parseDouble(tbBangsal.getValueAt(i, 14).toString());
                    totaltopup = totaltopup + Double.parseDouble(tbBangsal.getValueAt(i, 15).toString());
                }
            }
            LCount1.setText((setSep.size() + countTanpaSep) + "");
            LCountBiayaRiil.setText(Valid.SetAngka(totalBiayaRiil));
            LCountTopup.setText(Valid.SetAngka(totaltopup));
            LCountTotalLebih.setText(Valid.SetAngka(totallebih));
            LCountTotalRugi.setText(Valid.SetAngka(totalrugi));
            LCountUmbal.setText(Valid.SetAngka(totalumbal));
        }
    }//GEN-LAST:event_tbBangsalPropertyChange

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        try {
            if (Valid.daysOld("./cache/akunbayar.iyem") < 8) {
                tampilAkunBayar2();
            } else {
                tampilAkunBayar();
            }
            tampilStatusKlaim();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_formWindowOpened

    private void BtnAll1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAll1ActionPerformed
        tampilAkunBayar();
        tampilStatusKlaim();
    }//GEN-LAST:event_BtnAll1ActionPerformed

    private void ppPilihSemuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppPilihSemuaActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        total = 0;
        totallebih = 0;
        totalrugi = 0;
        totaltopup = 0;
        totalumbal = 0;
        for (i = 0; i < tbBangsal.getRowCount(); i++) {
            tbBangsal.setValueAt(true, i, 0);
            getdata(i);
        }
        row = tbBangsal.getRowCount();
        Set<String> setSep = new HashSet<>();
        int countTanpaSep = 0;
        double totalBiayaRiil = 0;
        for (i = 0; i < row; i++) {
            if (tbBangsal.getValueAt(i, 0).toString().equals("true")) {
                String sep = tbBangsal.getValueAt(i, 4) != null ? tbBangsal.getValueAt(i, 4).toString().trim() : "";
                if (!sep.isEmpty() && !sep.equals("-")) {
                    setSep.add(sep);
                } else {
                    countTanpaSep++;
                }
                totalBiayaRiil = totalBiayaRiil + Double.parseDouble(tbBangsal.getValueAt(i, 11).toString());
                total = total + Double.parseDouble(tbBangsal.getValueAt(i, 5).toString())
                        - (Double.parseDouble(tbBangsal.getValueAt(i, 6).toString())
                        + Double.parseDouble(tbBangsal.getValueAt(i, 7).toString()));
                totallebih = totallebih + Double.parseDouble(tbBangsal.getValueAt(i, 12).toString());
                totalrugi = totalrugi + Double.parseDouble(tbBangsal.getValueAt(i, 13).toString());
                totalumbal = totalumbal + Double.parseDouble(tbBangsal.getValueAt(i, 14).toString());
                totaltopup = totaltopup + Double.parseDouble(tbBangsal.getValueAt(i, 15).toString());

                HitungSelisihLebihKurang(i);
            }

        }
        LCount1.setText((setSep.size() + countTanpaSep) + "");
        LCountBiayaRiil.setText(Valid.SetAngka(totalBiayaRiil));
        LCountTopup.setText(Valid.SetAngka(totaltopup));
        LCountTotalLebih.setText(Valid.SetAngka(totallebih));
        LCountTotalRugi.setText(Valid.SetAngka(totalrugi));
        LCountUmbal.setText(Valid.SetAngka(totalumbal));

        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_ppPilihSemuaActionPerformed

    private void ppBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBersihkanActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        total = 0;
        totallebih = 0;
        totalrugi = 0;
        totaltopup = 0;
        totalumbal = 0;
        for (i = 0; i < tbBangsal.getRowCount(); i++) {
            tbBangsal.setValueAt(false, i, 0);
            getdata(i);
        }
        row = tbBangsal.getRowCount();
        Set<String> setSep = new HashSet<>();
        int countTanpaSep = 0;
        double totalBiayaRiil = 0;
        for (i = 0; i < row; i++) {
            if (tbBangsal.getValueAt(i, 0).toString().equals("true")) {
                String sep = tbBangsal.getValueAt(i, 4) != null ? tbBangsal.getValueAt(i, 4).toString().trim() : "";
                if (!sep.isEmpty() && !sep.equals("-")) {
                    setSep.add(sep);
                } else {
                    countTanpaSep++;
                }
                totalBiayaRiil = totalBiayaRiil + Double.parseDouble(tbBangsal.getValueAt(i, 11).toString());
                total = total + Double.parseDouble(tbBangsal.getValueAt(i, 5).toString())
                        - (Double.parseDouble(tbBangsal.getValueAt(i, 6).toString())
                        + Double.parseDouble(tbBangsal.getValueAt(i, 7).toString()));
                totallebih = totallebih + Double.parseDouble(tbBangsal.getValueAt(i, 12).toString());
                totalrugi = totalrugi + Double.parseDouble(tbBangsal.getValueAt(i, 13).toString());
                totalumbal = totalumbal + Double.parseDouble(tbBangsal.getValueAt(i, 14).toString());
                totaltopup = totaltopup + Double.parseDouble(tbBangsal.getValueAt(i, 15).toString());
            }
        }
        LCount1.setText((setSep.size() + countTanpaSep) + "");
        LCountBiayaRiil.setText(Valid.SetAngka(totalBiayaRiil));
        LCountTopup.setText(Valid.SetAngka(totaltopup));
        LCountTotalLebih.setText(Valid.SetAngka(totallebih));
        LCountTotalRugi.setText(Valid.SetAngka(totalrugi));
        LCountUmbal.setText(Valid.SetAngka(totalumbal));

        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_ppBersihkanActionPerformed

    private void ppKapitasiPaketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppKapitasiPaketActionPerformed
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, table masih kosong...!!!!");
            TCari.requestFocus();
        } else {
            LCount1.setText(Valid.SetAngka(total));
            PiutangBelumDibayar.setText(Valid.SetAngka(total));
            NilaiKapitasiPaket.setText("");
            PersentaseBayarPaket.setText("0");
            LebihBayarPiutang.setText("0");
            lebihbayar = 0;
            WindowHitungPaket.setLocationRelativeTo(internalFrame1);
            WindowHitungPaket.setVisible(true);
            NilaiKapitasiPaket.requestFocus();
        }
    }//GEN-LAST:event_ppKapitasiPaketActionPerformed

    private void BtnCloseHitungKapitasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCloseHitungKapitasiActionPerformed
        WindowHitungPaket.dispose();
    }//GEN-LAST:event_BtnCloseHitungKapitasiActionPerformed

    private void BtnHitungKapitasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHitungKapitasiActionPerformed

        double totalPiutang = Double.parseDouble(tbBangsal.getValueAt(i, 5).toString());
        double umbal = Double.parseDouble(tbBangsal.getValueAt(i, 14).toString());
        double topup = Double.parseDouble(tbBangsal.getValueAt(i, 15).toString());

// Avoid division by zero
        double persentaseBayarPaket = (umbal + topup == 0) ? 0 : (totalPiutang / (umbal + topup)) * 100;

// Store the result in PersentaseBayarPaket field
        PersentaseBayarPaket.setText(Valid.SetAngka3(persentaseBayarPaket));

        if (Double.parseDouble(PersentaseBayarPaket.getText()) == 0) {
            JOptionPane.showMessageDialog(null, "Persentase pembayaran masih 0..!!");
            NilaiKapitasiPaket.requestFocus();
        } else if (Double.parseDouble(PersentaseBayarPaket.getText()) == 100) {
            JOptionPane.showMessageDialog(null, "Persentase pembayaran 100% tidak perlu menggunakan kapitasi/paket..!!");
            NilaiKapitasiPaket.requestFocus();
        } else if (Double.parseDouble(PersentaseBayarPaket.getText()) > 100) {
            LebihBayarPiutang.setText(Valid.SetAngka3(Double.parseDouble(NilaiKapitasiPaket.getText()) - total));
            lebihbayar = Double.parseDouble(NilaiKapitasiPaket.getText()) - total;
            for (i = 0; i < tbBangsal.getRowCount(); i++) {
                try {
                    tbBangsal.setValueAt(
                            Double.parseDouble(tbBangsal.getValueAt(i, 5).toString())
                            - (Double.parseDouble(tbBangsal.getValueAt(i, 6).toString())
                            + Double.parseDouble(tbBangsal.getValueAt(i, 7).toString())),
                            i, 8);
                    tbBangsal.setValueAt(
                            Double.parseDouble(tbBangsal.getValueAt(i, 5).toString())
                            - (Double.parseDouble(tbBangsal.getValueAt(i, 6).toString())
                            + Double.parseDouble(tbBangsal.getValueAt(i, 7).toString())),
                            i, 11);
                    tbBangsal.setValueAt(0, i, 12);
                    tbBangsal.setValueAt(0, i, 13);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Terjadi kesalahan penghitungan, silahkan ulangi perhitungan..!!");
                    System.out.println("Notif : " + e);
                }
            }
        } else if (Double.parseDouble(PersentaseBayarPaket.getText()) < 100) {
            lebihbayar = 0;
            LebihBayarPiutang.setText("0");
            for (i = 0; i < tbBangsal.getRowCount(); i++) {
                try {
                    tbBangsal.setValueAt(
                            Math.round((Double.parseDouble(tbBangsal.getValueAt(i, 5).toString()) - (Double.parseDouble(tbBangsal.getValueAt(i, 6).toString()) + Double.parseDouble(tbBangsal.getValueAt(i, 7).toString())))
                                    - ((Double.parseDouble(tbBangsal.getValueAt(i, 5).toString()) - (Double.parseDouble(tbBangsal.getValueAt(i, 6).toString()) + Double.parseDouble(tbBangsal.getValueAt(i, 7).toString()))) * (Double.parseDouble(PersentaseBayarPaket.getText()) / 100))),
                            i, 13);
                    tbBangsal.setValueAt(
                            Math.round((Double.parseDouble(tbBangsal.getValueAt(i, 5).toString())
                                    - (Double.parseDouble(tbBangsal.getValueAt(i, 6).toString())
                                    + Double.parseDouble(tbBangsal.getValueAt(i, 7).toString())))
                                    * (Double.parseDouble(PersentaseBayarPaket.getText()) / 100)),
                            i, 11);
                    tbBangsal.setValueAt(
                            Math.round(Double.parseDouble(tbBangsal.getValueAt(i, 5).toString())
                                    - (Double.parseDouble(tbBangsal.getValueAt(i, 6).toString())
                                    + Double.parseDouble(tbBangsal.getValueAt(i, 7).toString())
                                    + Double.parseDouble(tbBangsal.getValueAt(i, 11).toString())
                                    + Double.parseDouble(tbBangsal.getValueAt(i, 12).toString())
                                    + Double.parseDouble(tbBangsal.getValueAt(i, 13).toString()))),
                            i, 8);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Terjadi kesalahan penghitungan, silahkan ulangi perhitungan..!!");
                    System.out.println("Notif : " + e);
                }
            }
        }
    }//GEN-LAST:event_BtnHitungKapitasiActionPerformed

    private void NilaiKapitasiPaketKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NilaiKapitasiPaketKeyPressed
        Valid.pindah(evt, BtnCloseHitungKapitasi, BtnHitungKapitasi);
    }//GEN-LAST:event_NilaiKapitasiPaketKeyPressed

    private void Tgl1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tgl1KeyPressed
        Valid.pindah(evt, BtnKeluar, Tgl2);
    }//GEN-LAST:event_Tgl1KeyPressed

    private void Tgl2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tgl2KeyPressed

    }//GEN-LAST:event_Tgl2KeyPressed

    private void CmbSttsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_CmbSttsKeyPressed

    }//GEN-LAST:event_CmbSttsKeyPressed

    private void CmbStatusKlaimKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_CmbStatusKlaimKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_CmbStatusKlaimKeyPressed

    private void CmbStatusKlaimItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CmbStatusKlaimItemStateChanged
        BtnCariActionPerformed(null);
    }//GEN-LAST:event_CmbStatusKlaimItemStateChanged

    private void CmbStatusPiutangItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CmbStatusPiutangItemStateChanged
        BtnCariActionPerformed(null);
    }//GEN-LAST:event_CmbStatusPiutangItemStateChanged

    private void CmbStatusPiutangKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_CmbStatusPiutangKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnCariActionPerformed(null);
        } else {
            Valid.pindah(evt, CmbStatusKlaim, keterangan);
        }
    }//GEN-LAST:event_CmbStatusPiutangKeyPressed

    private void CmbSttsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CmbSttsItemStateChanged
        BtnCariActionPerformed(null);
    }//GEN-LAST:event_CmbSttsItemStateChanged

    private void ChkTanggalItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_ChkTanggalItemStateChanged
        Tgl1.setEnabled(ChkTanggal.isSelected());
        Tgl2.setEnabled(ChkTanggal.isSelected());
    }//GEN-LAST:event_ChkTanggalItemStateChanged

    private void keteranganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_keteranganKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_keteranganKeyPressed

    private void CmbSortByItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_CmbSortByItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_CmbSortByItemStateChanged

    private void CmbSortByKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_CmbSortByKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_CmbSortByKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            KeuanganPiutangBelumLunasBPJS dialog = new KeuanganPiutangBelumLunasBPJS(new javax.swing.JFrame(), true);
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
    private widget.Button BtnBayar;
    private widget.Button BtnCari;
    private widget.Button BtnCloseHitungKapitasi;
    private widget.Button BtnHitungKapitasi;
    private widget.Button BtnKeluar;
    private widget.ComboBox CmbSortBy;
    private widget.ComboBox CmbStatusKlaim;
    private widget.ComboBox CmbStatusPiutang;
    private widget.ComboBox CmbStts;
    private widget.CekBox ChkTanggal;
    private javax.swing.JLabel LCount;
    private javax.swing.JLabel LCount1;
    private javax.swing.JLabel LCountBiayaRiil;
    private javax.swing.JLabel LCountData;
    private javax.swing.JLabel LCountTopup;
    private javax.swing.JLabel LCountTotalLebih;
    private javax.swing.JLabel LCountTotalRugi;
    private javax.swing.JLabel LCountUmbal;
    private widget.TextBox LebihBayarPiutang;
    private javax.swing.JMenuItem MnDetailPiutang;
    private widget.TextBox NilaiKapitasiPaket;
    private widget.TextBox PersentaseBayarPaket;
    private widget.TextBox PiutangBelumDibayar;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.TextBox TKd;
    private widget.Tanggal Tanggal;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private javax.swing.JDialog WindowHitungPaket;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame8;
    private javax.swing.JLabel jLabel10;
    private widget.Label jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabelBiayaRiil;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel39;
    private widget.Label jLabel40;
    private widget.Label jLabel41;
    private widget.Label jLabel42;
    private widget.Label jLabel43;
    private widget.Label jLabel44;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private widget.Label jLabelStatusPiutang;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.TextBox keterangan;
    private widget.Label label17;
    private widget.Label label18;
    private widget.Label label19;
    private widget.Label label32;
    private widget.panelisi panelisi1;
    private widget.panelisi panelisi3;
    private widget.panelisi panelisi4;
    private widget.panelisi panelisi5;
    private javax.swing.JMenuItem ppBersihkan;
    private javax.swing.JMenuItem ppKapitasiPaket;
    private javax.swing.JMenuItem ppPilihSemua;
    private widget.Table tbBangsal;
    // End of variables declaration//GEN-END:variables

    private void tampil() {

        jml = 0;
        for (i = 0; i < tabMode.getRowCount(); i++) {
            if (tabMode.getValueAt(i, 0) != null && Boolean.parseBoolean(tabMode.getValueAt(i, 0).toString())) {
                jml++;
            }
        }
        // Initialize each array separately with null then new array
        P = null;
        P = new boolean[jml];

        noRawat = null;
        noRawat = new String[jml];

        tglPiutang = null;
        tglPiutang = new String[jml];

        pasien = null;
        pasien = new String[jml];

        noSep = null;
        noSep = new String[jml];

        totalPiutang = null;
        totalPiutang = new double[jml];

        uangMuka = null;
        uangMuka = new double[jml];

        cicilanBayar = null;
        cicilanBayar = new double[jml];

        sisaPiutang = null;
        sisaPiutang = new double[jml];

        tglTempo = null;
        tglTempo = new String[jml];

        namaBayar = null;
        namaBayar = new String[jml];

        bayar = null;
        bayar = new double[jml];

        lebihBayar = null;
        lebihBayar = new double[jml];

        tidakTerbayar = null;
        tidakTerbayar = new double[jml];

        umbal = null;
        umbal = new double[jml];

        topup = null;
        topup = new double[jml];

        statusKlaim = null;
        statusKlaim = new String[jml];

        kdPj = null;
        kdPj = new String[jml];

        noVpk = null;
        noVpk = new String[jml];

        poli = null;
        poli = new String[jml];

        dokterRegis = null;
        dokterRegis = new String[jml];

        dokterSep = null;
        dokterSep = new String[jml];

        index = 0;
        for (i = 0; i < tabMode.getRowCount(); i++) {
            if (tabMode.getValueAt(i, 0) != null && Boolean.parseBoolean(tabMode.getValueAt(i, 0).toString())) {
                P[index] = true;
                noRawat[index] = tabMode.getValueAt(i, 1).toString();
                tglPiutang[index] = tabMode.getValueAt(i, 2).toString();
                pasien[index] = tabMode.getValueAt(i, 3).toString();
                noSep[index] = tabMode.getValueAt(i, 4).toString();
                totalPiutang[index] = Double.parseDouble(tabMode.getValueAt(i, 5).toString());
                uangMuka[index] = Double.parseDouble(tabMode.getValueAt(i, 6).toString());
                cicilanBayar[index] = Double.parseDouble(tabMode.getValueAt(i, 7).toString());
                sisaPiutang[index] = Double.parseDouble(tabMode.getValueAt(i, 8).toString());
                tglTempo[index] = tabMode.getValueAt(i, 9).toString();
                namaBayar[index] = tabMode.getValueAt(i, 10).toString();
                bayar[index] = Double.parseDouble(tabMode.getValueAt(i, 11).toString());
                lebihBayar[index] = Double.parseDouble(tabMode.getValueAt(i, 12).toString());
                tidakTerbayar[index] = Double.parseDouble(tabMode.getValueAt(i, 13).toString());
                umbal[index] = Double.parseDouble(tabMode.getValueAt(i, 14).toString());
                topup[index] = Double.parseDouble(tabMode.getValueAt(i, 15).toString());
                statusKlaim[index] = tabMode.getValueAt(i, 16).toString();
                kdPj[index] = tabMode.getValueAt(i, 17).toString();
                noVpk[index] = tabMode.getValueAt(i, 18).toString();
                poli[index] = tabMode.getValueAt(i, 19).toString();
                dokterRegis[index] = tabMode.getValueAt(i, 20).toString();
                dokterSep[index] = tabMode.getValueAt(i, 21).toString();
                index++;
            }
        }

        Valid.tabelKosong(tabMode);
        for (i = 0; i < jml; i++) {
            tabMode.addRow(new Object[]{
                P[i], noRawat[i], tglPiutang[i], pasien[i], noSep[i],
                totalPiutang[i], uangMuka[i], cicilanBayar[i], sisaPiutang[i],
                tglTempo[i], namaBayar[i], bayar[i], lebihBayar[i],
                tidakTerbayar[i], umbal[i], topup[i], statusKlaim[i],
                kdPj[i], noVpk[i], poli[i], dokterRegis[i], dokterSep[i]
            });
        }
//        Valid.tabelKosong(tabMode);

        String filterstatusrawat = "";
        if (!CmbStts.getSelectedItem().toString().equals("Semua")) {
            filterstatusrawat = " and reg_periksa.status_lanjut='" + CmbStts.getSelectedItem().toString() + "'";
        }

        String filterstatusklaim = "";

        if (!CmbStatusKlaim.getSelectedItem().toString().equals("Semua")) {
            filterstatusklaim = " and inacbg_klaim_umpanbalik.status='" + CmbStatusKlaim.getSelectedItem().toString() + "'";
        }

        String filterstatuspiutang = "";
        String sttsPiutang = (CmbStatusPiutang != null && CmbStatusPiutang.getSelectedItem() != null) ? CmbStatusPiutang.getSelectedItem().toString() : "Belum Lunas";
        if (sttsPiutang.equals("Belum Lunas")) {
            filterstatuspiutang = " and detail_piutang_pasien.sisapiutang >= 1 ";
        } else if (sttsPiutang.equals("Lunas")) {
            filterstatuspiutang = " and (detail_piutang_pasien.sisapiutang <= 0 or piutang_pasien.status = 'Lunas') ";
        } else {
            filterstatuspiutang = "";
        }

        String filtertanggal = "";
        if (ChkTanggal.isSelected()) {
            filtertanggal = " and piutang_pasien.tgl_piutang between '" + Valid.SetTgl(Tgl1.getSelectedItem().toString()) + "' and '" + Valid.SetTgl(Tgl2.getSelectedItem().toString()) + "' ";
        }

        String filterby = "";
        String selectedSort = CmbSortBy.getSelectedItem().toString();

        switch (selectedSort) {
            case "No SEP Asc":
                filterby = " order by nosep asc, piutang_pasien.tgl_piutang asc, piutang_pasien.no_rawat asc";
                break;
            case "No SEP Desc":
                filterby = " order by nosep desc, piutang_pasien.tgl_piutang desc, piutang_pasien.no_rawat desc";
                break;
            case "No Rawat Asc":
                filterby = " order by piutang_pasien.no_rawat asc, piutang_pasien.tgl_piutang asc, nosep asc";
                break;
            case "No Rawat Desc":
                filterby = " order by piutang_pasien.no_rawat desc, piutang_pasien.tgl_piutang desc, nosep desc";
                break;
            case "Tgl Piutang Asc":
                filterby = " order by piutang_pasien.tgl_piutang asc, nosep asc, piutang_pasien.no_rawat asc";
                break;
            case "Tgl Piutang Desc":
                filterby = " order by piutang_pasien.tgl_piutang desc, nosep desc, piutang_pasien.no_rawat desc";
                break;
            default:
                filterby = " order by piutang_pasien.tgl_piutang asc, nosep asc, piutang_pasien.no_rawat asc";
                break;
        }

        try {
            sisapiutang = 0;
            ps = koneksi.prepareStatement("select piutang_pasien.no_rawat,"
                    + " piutang_pasien.tgl_piutang, "
                    + "concat(piutang_pasien.no_rkm_medis,' ',pasien.nm_pasien) as nm_pasien, "
                    + "piutang_pasien.status,"
                    + "detail_piutang_pasien.totalpiutang, "
                    + "piutang_pasien.uangmuka, "
                    + "detail_piutang_pasien.sisapiutang, "
                    + "piutang_pasien.tgltempo,"
                    + "penjab.png_jawab,"
                    + "detail_piutang_pasien.kd_pj,"
                    + "detail_piutang_pasien.nama_bayar,"
                    + "dokter.nm_dokter, "
                    + "poliklinik.nm_poli,"
                    + "IFNULL(inacbg_klaim_umpanbalik.bypengajuan, 0) AS bypengajuan, \n"
                    + "    IFNULL(inacbg_klaim_umpanbalik.bydisetujui, 0) AS bydisetujui, \n"
                    + "    IFNULL(inacbg_klaim_umpanbalik.bytarifgrouper, 0) AS bytarifgrouper, \n"
                    + "    IFNULL(inacbg_klaim_umpanbalik.bytarifrs, 0) AS bytarifrs, \n"
                    + "    IFNULL(inacbg_klaim_umpanbalik.bytopup, 0) AS bytopup, \n"
                    + "    IFNULL(tambahan_biaya_inacbg.besar_biaya_dibayar, 0) AS iurbayar, \n"
                    + "    IFNULL(inacbg_klaim_umpanbalik.status,'') as statusklaim,"
                    + "    IFNULL(inacbg_klaim_umpanbalik.nofpk,'') as nofpk,"
                    + "    IFNULL(bridging_sep.no_sep,'') as nosep, "
                    + "    IFNULL(bridging_sep.nmdpdjp,'') as doktersep "
                    + "from piutang_pasien inner join pasien inner join reg_periksa inner join penjab inner join detail_piutang_pasien inner join poliklinik inner join dokter on  "
                    + "piutang_pasien.no_rkm_medis=pasien.no_rkm_medis and "
                    + "piutang_pasien.no_rawat=reg_periksa.no_rawat and "
                    + "reg_periksa.kd_pj=penjab.kd_pj and piutang_pasien.no_rawat=detail_piutang_pasien.no_rawat and reg_periksa.kd_poli=poliklinik.kd_poli and reg_periksa.kd_dokter=dokter.kd_dokter "
                    + " LEFT join bridging_sep on reg_periksa.no_rawat=bridging_sep.no_rawat "
                    + " LEFT JOIN\n"
                    + "    inacbg_klaim_umpanbalik\n"
                    + "    ON \n"
                    + "        bridging_sep.no_sep = inacbg_klaim_umpanbalik.no_sep \n"
                    + "    LEFT JOIN\n"
                    + "    tambahan_biaya_inacbg\n"
                    + "    ON \n"
                    + "        bridging_sep.no_rawat = tambahan_biaya_inacbg.no_rawat "
                    + " where detail_piutang_pasien.nama_bayar='01. Piutang BPJS Kesehatan' and piutang_pasien.no_rawat IN (select bridging_sep.no_rawat from bridging_sep) "
                    + filterstatuspiutang
                    + filtertanggal
                    + (TCari.getText().trim().equals("") ? "" : " and (piutang_pasien.no_rawat like ? or piutang_pasien.no_rkm_medis like ? or "
                    + "pasien.nm_pasien like ? or piutang_pasien.status like ? or inacbg_klaim_umpanbalik.nofpk like ? or bridging_sep.no_sep like ?)") + filterstatusrawat + filterstatusklaim + filterby);
            // Add these before your try block
            Set<String> processedNoSep = new HashSet<>();

            try {
                if (!TCari.getText().trim().equals("")) {
                    ps.setString(1, "%" + TCari.getText() + "%");
                    ps.setString(2, "%" + TCari.getText() + "%");
                    ps.setString(3, "%" + TCari.getText() + "%");
                    ps.setString(4, "%" + TCari.getText() + "%");
                    ps.setString(5, "%" + TCari.getText() + "%");
                    ps.setString(6, "%" + TCari.getText() + "%");
                }

                rs = ps.executeQuery();

                while (rs.next()) {
                    double cicilanDB = Sequel.cariIsiAngka("SELECT ifnull(SUM(bayar_piutang.besar_cicilan) + SUM(bayar_piutang.diskon_piutang) + SUM(bayar_piutang.tidak_terbayar), 0) FROM bayar_piutang WHERE bayar_piutang.no_rawat=?", rs.getString(1));
                    double totalPiutangDB = rs.getDouble("totalpiutang"); // "Total Piutang"
                    double uangMukaDB = 0.0; // "Uang Muka"
                    double sisaPiutangDB = rs.getDouble(7); // "Sisa Piutang" calculation
                    double umbalDB = rs.getDouble("bydisetujui"); // "Umbal"
                    double topupDB = rs.getDouble("iurbayar"); // "Topup"
                    double byTarifRsDB = rs.getDouble("bytarifrs"); // "Biaya Riil RS / Tarif RS"
                    String statusKlaimDB = rs.getString("statusklaim");

                    String noSep = rs.getString("nosep");
                    double tarifUmbal = umbalDB;
                    double tarifRiil = byTarifRsDB;

                    // If this no_sep has been processed before, set tarif to 0
                    if (processedNoSep.contains(noSep)) {
                        tarifUmbal = 0;
                        tarifRiil = 0;
                    } else {
                        // First time seeing this no_sep, keep original value and mark as processed
                        processedNoSep.add(noSep);
                    }

                    double totalAvailableFunds = tarifUmbal; // Total available funds
                    double totalDanaKlaim = tarifUmbal + topupDB;
                    double bayarDB = 0;
                    double lebihBayar = 0;
                    double tidakTerbayarDB = 0;

                    if ("3#Klaim".equals(statusKlaimDB)) {
                        if (totalPiutangDB <= totalDanaKlaim) {
                            lebihBayar = totalDanaKlaim - totalPiutangDB;
                            bayarDB = totalPiutangDB; // Fully covered
                        } else {
                            tidakTerbayarDB = totalPiutangDB - totalDanaKlaim;
                            bayarDB = totalPiutangDB; // Only partially covered
                        }
                    } else {
                        bayarDB = totalPiutangDB;
                        lebihBayar = 0;
                        tidakTerbayarDB = 0;
                    }

                    tabMode.addRow(new Object[]{
                        false, // "P" (Checkbox for selection)
                        rs.getString("no_rawat"), // "No.Rawat/No.Tagihan"
                        rs.getString("tgl_piutang"), // "Tgl.Piutang"
                        rs.getString(3), // "Pasien"
                        rs.getString("nosep"), // "Status"
                        totalPiutangDB, // "Total Piutang"
                        uangMukaDB, // "Uang Muka"
                        cicilanDB, // "Cicilan+Disk+T.Terbayar"
                        sisaPiutangDB, // "Sisa Piutang"
                        rs.getString("tgltempo"), // "Jatuh Tempo"
                        rs.getString("nama_bayar"), // "Cara Bayar"
                        tarifRiil, // "Bayar"
                        lebihBayar, // "Diskon Bayar"
                        tidakTerbayarDB, // "Tidak Terbayar"
                        totalAvailableFunds, // "Umbal"
                        topupDB, // "Topup"
                        statusKlaimDB, // "Status Klaim"
                        rs.getString("kd_pj"), // "Kode Penjamin"
                        rs.getString("nofpk"),
                        rs.getString("nm_poli"),
                        rs.getString("nm_dokter"),
                        rs.getString("doktersep")
                    });

                    sisapiutang += sisaPiutangDB;
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
            LCount.setText(Valid.SetAngka(sisapiutang));
            LCountData.setText("" + tabMode.getRowCount());
            LCount1.setText("0");
            LCountBiayaRiil.setText("0");
            LCountTopup.setText("0");
            LCountTotalLebih.setText("0");
            LCountTotalRugi.setText("0");
            LCountUmbal.setText("0");
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void tampilperakun() {
        Valid.tabelKosong(tabMode);
        try {
            sisapiutang = 0;
            ps = koneksi.prepareStatement("select piutang_pasien.no_rawat, piutang_pasien.tgl_piutang, concat(piutang_pasien.no_rkm_medis,' ',pasien.nm_pasien), "
                    + "piutang_pasien.status,detail_piutang_pasien.totalpiutang,0, detail_piutang_pasien.sisapiutang, piutang_pasien.tgltempo,detail_piutang_pasien.nama_bayar,"
                    + "IFNULL(inacbg_klaim_umpanbalik.bypengajuan, 0) AS bypengajuan, \n"
                    + "    IFNULL(inacbg_klaim_umpanbalik.bydisetujui, 0) AS bydisetujui, \n"
                    + "    IFNULL(inacbg_klaim_umpanbalik.bytarifgrouper, 0) AS bytarifgrouper, \n"
                    + "    IFNULL(inacbg_klaim_umpanbalik.bytarifrs, 0) AS bytarifrs, \n"
                    + "    IFNULL(inacbg_klaim_umpanbalik.bytopup, 0) AS bytopup, \n"
                    + "    IFNULL(tambahan_biaya_inacbg.besar_biaya_dibayar, 0) AS iurbayar, \n"
                    + "    IFNULL(inacbg_klaim_umpanbalik.status,'') as statusklaim,"
                    + "    IFNULL(bridging_sep.no_sep,'') as nosep "
                    + "from piutang_pasien inner join pasien inner join reg_periksa inner join penjab inner join detail_piutang_pasien on  "
                    + "piutang_pasien.no_rkm_medis=pasien.no_rkm_medis and piutang_pasien.no_rawat=reg_periksa.no_rawat and "
                    + "reg_periksa.kd_pj=penjab.kd_pj and piutang_pasien.no_rawat=detail_piutang_pasien.no_rawat"
                    + " LEFT join bridging_sep on reg_periksa.no_rawat=bridging_sep.no_rawat "
                    + " LEFT JOIN\n"
                    + "    inacbg_klaim_umpanbalik\n"
                    + "    ON \n"
                    + "        bridging_sep.no_sep = inacbg_klaim_umpanbalik.no_sep \n"
                    + "    LEFT JOIN\n"
                    + "    tambahan_biaya_inacbg\n"
                    + "    ON \n"
                    + "        bridging_sep.no_rawat = tambahan_biaya_inacbg.no_rawat "
                    + " where "
                    + "detail_piutang_pasien.sisapiutang>=1 and detail_piutang_pasien.nama_bayar like ? and "
                    + "(piutang_pasien.no_rawat like ? or piutang_pasien.no_rkm_medis like ? or "
                    + "pasien.nm_pasien like ? or piutang_pasien.status like ?) order by piutang_pasien.tgl_piutang");
            try {
                ps.setString(1, "%" + TCari.getText() + "%");
                ps.setString(2, "%" + TCari.getText() + "%");
                ps.setString(3, "%" + TCari.getText() + "%");
                ps.setString(4, "%" + TCari.getText() + "%");
                ps.setString(5, "%" + TCari.getText() + "%");
                rs = ps.executeQuery();
                while (rs.next()) {
                    String koderekkontra = "";
                    cicilan = Sequel.cariIsiAngka("SELECT ifnull(SUM(bayar_piutang.besar_cicilan)+SUM(bayar_piutang.diskon_piutang)+SUM(bayar_piutang.tidak_terbayar),0) FROM bayar_piutang where bayar_piutang.no_rawat='" + rs.getString(1) + "' and bayar_piutang.kd_rek_kontra='" + koderekkontra + "'");
                    tabMode.addRow(new Object[]{
                        false, // "P" (Checkbox for selection, likely used for marking rows)
                        rs.getString(1), // "No.Rawat/No.Tagihan" (Invoice or patient visit number)
                        rs.getString(2), // "Tgl.Piutang" (Date of receivable/debt)
                        rs.getString(3), // "Pasien" (Patient name)
                        rs.getString(4), // "Status" (Status of the debt, e.g., paid, unpaid, partially paid)
                        rs.getDouble(5), // "Total Piutang" (Total outstanding receivable amount)
                        rs.getDouble(6), // "Uang Muka" (Down payment made by the patient)
                        cicilan, // "Cicilan+Disk+T.Terbayar" (Installments + Discounts + Total Paid)
                        (rs.getDouble(7) - cicilan), // "Sisa Piutang" (Remaining receivable after payments)
                        rs.getString(8), // "Jatuh Tempo" (Due date of the receivable)
                        rs.getString(9), // "Cara Bayar" (Payment method used)
                        (rs.getDouble(7) - cicilan), // "Bayar" (Amount to be paid, possibly the same as remaining receivable)
                        0, // "Diskon Bayar" (Discount on the payment, set to zero by default)
                        0, // "Tidak Terbayar" (Unpaid amount, set to zero initially)
                        rs.getDouble("bydisetujui"), // "Umbal" (Additional claim adjustment)
                        rs.getDouble("iurbayar"), // "Topup" (Extra charges added to the bill)
                        rs.getString("statusklaim") // "Status" (Final claim status, e.g., approved, rejected)
                    });
                    sisapiutang = sisapiutang + rs.getDouble(7) - cicilan;
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

            LCount.setText(Valid.SetAngka(sisapiutang));
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    public void tampiltagihan(String notagihan) {
        this.notagihan = notagihan;
        keterangan.setText(notagihan);
        Valid.tabelKosong(tabMode);
//        System.out.println("masuk fungsi tampil");
        try {
            ps = koneksi.prepareStatement("select akun_piutang.kd_rek,akun_piutang.nama_bayar,akun_bayar.nama_bayar,"
                    + "penagihan_piutang.kd_pj from penagihan_piutang inner join akun_piutang on penagihan_piutang.kd_pj=akun_piutang.kd_pj "
                    + "inner join akun_bayar on penagihan_piutang.kd_rek=akun_bayar.kd_rek where penagihan_piutang.no_tagihan=?");
            try {
                ps.setString(1, notagihan);
                rs = ps.executeQuery();
                if (rs.next()) {
                    AkunBayar.setSelectedItem(rs.getString(3));
                    carabayar = rs.getString("kd_pj");
//                    System.out.println("masuk fungsi loop");
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

            sisapiutang = 0;
            ps = koneksi.prepareStatement("select piutang_pasien.no_rawat,"
                    + " piutang_pasien.tgl_piutang, "
                    + "concat(piutang_pasien.no_rkm_medis,' ',pasien.nm_pasien) as nm_pasien, "
                    + "piutang_pasien.status,"
                    + "detail_piutang_pasien.totalpiutang, "
                    + "piutang_pasien.uangmuka, "
                    + "detail_piutang_pasien.sisapiutang, "
                    + "piutang_pasien.tgltempo,"
                    + "penjab.png_jawab,"
                    + "detail_piutang_pasien.kd_pj,"
                    + "detail_piutang_pasien.nama_bayar,"
                    + "dokter.nm_dokter, "
                    + "poliklinik.nm_poli,"
                    + "IFNULL(inacbg_klaim_umpanbalik.bypengajuan, 0) AS bypengajuan, \n"
                    + "    IFNULL(inacbg_klaim_umpanbalik.bydisetujui, 0) AS bydisetujui, \n"
                    + "    IFNULL(inacbg_klaim_umpanbalik.bytarifgrouper, 0) AS bytarifgrouper, \n"
                    + "    IFNULL(inacbg_klaim_umpanbalik.bytarifrs, 0) AS bytarifrs, \n"
                    + "    IFNULL(inacbg_klaim_umpanbalik.bytopup, 0) AS bytopup, \n"
                    + "    IFNULL(tambahan_biaya_inacbg.besar_biaya_dibayar, 0) AS iurbayar, \n"
                    + "    IFNULL(inacbg_klaim_umpanbalik.status,'') as statusklaim,"
                    + "    IFNULL(inacbg_klaim_umpanbalik.nofpk,'') as nofpk,"
                    + "    IFNULL(bridging_sep.no_sep,'') as nosep, "
                    + "    IFNULL(bridging_sep.nmdpdjp,'') as doktersep "
                    + "from piutang_pasien inner join pasien inner join reg_periksa inner join penjab inner join detail_piutang_pasien inner join poliklinik inner join dokter on  "
                    + "piutang_pasien.no_rkm_medis=pasien.no_rkm_medis and "
                    + "piutang_pasien.no_rawat=reg_periksa.no_rawat and "
                    + "reg_periksa.kd_pj=penjab.kd_pj and piutang_pasien.no_rawat=detail_piutang_pasien.no_rawat and reg_periksa.kd_poli=poliklinik.kd_poli and reg_periksa.kd_dokter=dokter.kd_dokter "
                    + " LEFT join bridging_sep on reg_periksa.no_rawat=bridging_sep.no_rawat "
                    + " LEFT JOIN\n"
                    + "    inacbg_klaim_umpanbalik\n"
                    + "    ON \n"
                    + "        bridging_sep.no_sep = inacbg_klaim_umpanbalik.no_sep \n"
                    + "    LEFT JOIN\n"
                    + "    tambahan_biaya_inacbg\n"
                    + "    ON \n"
                    + "        bridging_sep.no_rawat = tambahan_biaya_inacbg.no_rawat"
                    + " where "
                    + "detail_piutang_pasien.sisapiutang>=1 and detail_piutang_pasien.kd_pj='" + carabayar + "' and piutang_pasien.no_rawat in "
                    + "(select detail_penagihan_piutang.no_rawat from detail_penagihan_piutang where detail_penagihan_piutang.no_tagihan= '" + notagihan + "' ) order by nosep asc,nofpk asc");

// Add these before your try block
//            System.out.println(ps.toString());
            Set<String> processedNoSep = new HashSet<>();
            try {
//                ps.setString(1, notagihan);
                rs = ps.executeQuery();
                while (rs.next()) {
                    double cicilanDB = Sequel.cariIsiAngka("SELECT ifnull(SUM(bayar_piutang.besar_cicilan) + SUM(bayar_piutang.diskon_piutang) + SUM(bayar_piutang.tidak_terbayar), 0) FROM bayar_piutang WHERE bayar_piutang.no_rawat=?", rs.getString(1));
                    double totalPiutangDB = rs.getDouble("totalpiutang"); // "Total Piutang"
                    double uangMukaDB = 0.0; // "Uang Muka"
                    double sisaPiutangDB = rs.getDouble(7); // "Sisa Piutang" calculation
                    double umbalDB = rs.getDouble("bydisetujui"); // "Umbal"
                    double topupDB = rs.getDouble("iurbayar"); // "Topup"
                    double byTarifRsDB = rs.getDouble("bytarifrs"); // "Biaya Riil RS / Tarif RS"
                    String statusKlaimDB = rs.getString("statusklaim");
                    String noSep = rs.getString("nosep");
                    double tarifUmbal = umbalDB;
                    double tarifRiil = byTarifRsDB;

                    // If this no_sep has been processed before, set tarif to 0
                    if (processedNoSep.contains(noSep)) {
                        tarifUmbal = 0;
                        tarifRiil = 0;
                    } else {
                        // First time seeing this no_sep, keep original value and mark as processed
                        processedNoSep.add(noSep);
                    }
                    double totalAvailableFunds = tarifUmbal; // Total available funds
                    double totalDanaKlaim = tarifUmbal + topupDB;
                    double bayarDB = 0;
                    double lebihBayar = 0;
                    double tidakTerbayarDB = 0;

                    if ("3#Klaim".equals(statusKlaimDB)) {
                        if (totalPiutangDB <= totalDanaKlaim) {
                            lebihBayar = totalDanaKlaim - totalPiutangDB;
                            bayarDB = totalPiutangDB; // Fully covered
                        } else {
                            tidakTerbayarDB = totalPiutangDB - totalDanaKlaim;
                            bayarDB = totalPiutangDB; // Only partially covered
                        }
                    } else {
                        bayarDB = totalPiutangDB;
                        lebihBayar = 0;
                        tidakTerbayarDB = 0;
                    }
//                    System.out.println(rs.getString("no_rawat"));
                    if (statusKlaimDB.equals("3#Klaim")) {
                        tabMode.addRow(new Object[]{
                            true, // "P" (Checkbox for selection)
                            rs.getString("no_rawat"), // "No.Rawat/No.Tagihan"
                            rs.getString("tgl_piutang"), // "Tgl.Piutang"
                            rs.getString(3), // "Pasien"
                            noSep, // "Status"
                            totalPiutangDB, // "Total Piutang"
                            uangMukaDB, // "Uang Muka"
                            cicilanDB, // "Cicilan+Disk+T.Terbayar"
                            sisaPiutangDB, // "Sisa Piutang"
                            rs.getString("tgltempo"), // "Jatuh Tempo"
                            rs.getString("nama_bayar"), // "Cara Bayar"
                            tarifRiil, // "Bayar"
                            lebihBayar, // "Diskon Bayar"
                            tidakTerbayarDB, // "Tidak Terbayar"
                            totalAvailableFunds, // "Umbal"
                            topupDB, // "Topup"
                            statusKlaimDB, // "Status Klaim"
                            rs.getString("kd_pj"), // "Kode Penjamin"
                            rs.getString("nofpk"),
                            rs.getString("nm_poli"),
                            rs.getString("nm_dokter"),
                            rs.getString("doktersep")
                        });
                    } else {
                        tabMode.addRow(new Object[]{
                            false, // "P" (Checkbox for selection)
                            rs.getString("no_rawat"), // "No.Rawat/No.Tagihan"
                            rs.getString("tgl_piutang"), // "Tgl.Piutang"
                            rs.getString(3), // "Pasien"
                            noSep, // "Status"
                            totalPiutangDB, // "Total Piutang"
                            uangMukaDB, // "Uang Muka"
                            cicilanDB, // "Cicilan+Disk+T.Terbayar"
                            sisaPiutangDB, // "Sisa Piutang"
                            rs.getString("tgltempo"), // "Jatuh Tempo"
                            rs.getString("nama_bayar"), // "Cara Bayar"
                            tarifRiil, // "Bayar"
                            lebihBayar, // "Diskon Bayar"
                            tidakTerbayarDB, // "Tidak Terbayar"
                            totalAvailableFunds, // "Umbal"
                            topupDB, // "Topup"
                            statusKlaimDB, // "Status Klaim"
                            rs.getString("kd_pj"), // "Kode Penjamin"
                            rs.getString("nofpk"),
                            rs.getString("nm_poli"),
                            rs.getString("nm_dokter"),
                            rs.getString("doktersep")
                        });
                    }

                    sisapiutang += sisaPiutangDB;

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

            LCount.setText(Valid.SetAngka(sisapiutang));
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void ambilkoderekpiutang(String notagihan) {
        this.notagihan = notagihan;
        try {
            ps = koneksi.prepareStatement("SELECT\n"
                    + "	detail_piutang_pasien.nama_bayar, \n"
                    + "	akun_piutang.kd_rek, \n"
                    + "	akun_piutang.kd_pj\n"
                    + "FROM\n"
                    + "	detail_piutang_pasien\n"
                    + "	INNER JOIN\n"
                    + "	akun_piutang\n"
                    + "	ON \n"
                    + "		detail_piutang_pasien.nama_bayar = akun_piutang.nama_bayar AND detail_piutang_pasien.kd_pj=akun_piutang.kd_pj where detail_piutang_pasien.no_rawat=?");
            try {
                ps.setString(1, notagihan);
                rs = ps.executeQuery();
                if (rs.next()) {
                    koderekakunpiutang = rs.getString(2);
                    namarekakunpiutang = rs.getString(1);
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

    private void getdata() {
        total = 0;
        totallebih = 0;
        totalrugi = 0;
        totaltopup = 0;
        totalumbal = 0;
        row = tbBangsal.getRowCount();
        Set<String> setSep = new HashSet<>();
        int countTanpaSep = 0;
        double totalBiayaRiil = 0;
        for (i = 0; i < row; i++) {
            if (tbBangsal.getValueAt(i, 0).toString().equals("true")) {
                String sep = tbBangsal.getValueAt(i, 4) != null ? tbBangsal.getValueAt(i, 4).toString().trim() : "";
                if (!sep.isEmpty() && !sep.equals("-")) {
                    setSep.add(sep);
                } else {
                    countTanpaSep++;
                }
                totalBiayaRiil = totalBiayaRiil + Double.parseDouble(tbBangsal.getValueAt(i, 11).toString());
                total = total + Double.parseDouble(tbBangsal.getValueAt(i, 8).toString());
                totallebih = totallebih + Double.parseDouble(tbBangsal.getValueAt(i, 12).toString());
                totalrugi = totalrugi + Double.parseDouble(tbBangsal.getValueAt(i, 13).toString());
                totalumbal = totalumbal + Double.parseDouble(tbBangsal.getValueAt(i, 14).toString());
                totaltopup = totaltopup + Double.parseDouble(tbBangsal.getValueAt(i, 15).toString());
            }
        }
        LCount1.setText((setSep.size() + countTanpaSep) + "");
        LCountBiayaRiil.setText(Valid.SetAngka(totalBiayaRiil));
        LCountTopup.setText(Valid.SetAngka(totaltopup));
        LCountTotalLebih.setText(Valid.SetAngka(totallebih));
        LCountTotalRugi.setText(Valid.SetAngka(totalrugi));
        LCountUmbal.setText(Valid.SetAngka(totalumbal));

    }

    private void getdata(int pilih) {
        try {
            if (pilih != -1) {
                if (tbBangsal.getValueAt(pilih, 0).toString().equals("true")) {
                    // Check if "Status" is "3#Klaim" before applying additional calculations
                    String status = tbBangsal.getValueAt(pilih, 16).toString();

                    double totalPiutang = Double.parseDouble(tbBangsal.getValueAt(pilih, 5).toString()); // "Total Piutang"
                    double umbal = Double.parseDouble(tbBangsal.getValueAt(pilih, 14).toString()); // "Umbal"
                    double topup = Double.parseDouble(tbBangsal.getValueAt(pilih, 15).toString()); // "Topup"
                    double totalAvailable = umbal + topup;

                    double diskonBayar = 0;
                    double tidakTerbayar = 0;
                    double bayar = 0;

                    if ("3#Klaim".equals(status)) {
                        if (totalPiutang <= totalAvailable) {
                            diskonBayar = totalAvailable - totalPiutang;
                            bayar = totalPiutang; // Fully covered
                        } else {
                            tidakTerbayar = totalPiutang - totalAvailable;
                            bayar = totalPiutang; // Fully covered
//                            bayar = totalAvailable; // Only partially covered
                        }
                    }

                    // Update "Diskon Bayar" and "Tidak Terbayar" (kolom 11 "Bayar" dipertahankan sebagai bytarifrs)
                    tbBangsal.setValueAt(diskonBayar, pilih, 12);
                    tbBangsal.setValueAt(tidakTerbayar, pilih, 13);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan dalam perhitungan data!");
            System.out.println("Error: " + e);
        }
    }

    public void isCek() {
        TCari.requestFocus();
        BtnBayar.setEnabled(akses.getbayar_piutang());
    }

    private void tampilAkunBayar() {
        try {
            file = new File("./cache/akunbayar.iyem");
            file.createNewFile();
            fileWriter = new FileWriter(file);
            iyem = "";
            ps = koneksi.prepareStatement("select * from akun_bayar order by akun_bayar.nama_bayar");
            try {
                rs = ps.executeQuery();
                AkunBayar.removeAllItems();
                while (rs.next()) {
                    AkunBayar.addItem(rs.getString(1).replaceAll("\"", ""));
                    iyem = iyem + "{\"NamaAkun\":\"" + rs.getString(1).replaceAll("\"", "") + "\",\"KodeRek\":\"" + rs.getString(2) + "\",\"PPN\":\"" + rs.getDouble(3) + "\"},";
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

            fileWriter.write("{\"akunbayar\":[" + iyem.substring(0, iyem.length() - 1) + "]}");
            fileWriter.flush();
            fileWriter.close();
            iyem = null;
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void tampilStatusKlaim() {
        try {
            psstatusklaim = koneksi.prepareStatement("select inacbg_klaim_umpanbalik.status from inacbg_klaim_umpanbalik group by 1");
            try {
                rsstatusklaim = psstatusklaim.executeQuery();
                CmbStatusKlaim.removeAllItems();
                CmbStatusKlaim.addItem("Semua");
                while (rsstatusklaim.next()) {
                    CmbStatusKlaim.addItem(rsstatusklaim.getString("status"));

                }
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            } finally {
                if (rsstatusklaim != null) {
                    rsstatusklaim.close();
                }
                if (psstatusklaim != null) {
                    psstatusklaim.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    private void tampilAkunBayar2() {
        try {
            myObj = new FileReader("./cache/akunbayar.iyem");
            root = mapper.readTree(myObj);
            response = root.path("akunbayar");
            if (response.isArray()) {
                for (JsonNode list : response) {
                    AkunBayar.addItem(list.path("NamaAkun").asText().replaceAll("\"", ""));
                }
            }
            myObj.close();
        } catch (Exception ex) {
            if (ex.toString().contains("java.io.FileNotFoundException")) {
                tampilAkunBayar();
            } else {
                System.out.println("Notifikasi : " + ex);
            }
        }
    }

    private void HitungSelisihLebihKurang(int x) {
        try {
            // Get the "Status" column value
            String status = tbBangsal.getValueAt(x, 16).toString();

            // Only proceed if Status == "3#Klaim"
            if ("3#Klaim".equals(status)) {
                // Get required column values
                double totalPiutang = Double.parseDouble(tbBangsal.getValueAt(x, 5).toString()); // "Total Piutang"
                double umbal = Double.parseDouble(tbBangsal.getValueAt(x, 14).toString()); // "Umbal"
                double topup = Double.parseDouble(tbBangsal.getValueAt(x, 15).toString()); // "Topup"

                // Calculate available funds
                double totalAvailable = umbal + topup;

                // Determine values for "Diskon Bayar" and "Tidak Terbayar"
                if (totalPiutang <= totalAvailable) {
                    // If the available amount covers the total piutang
                    tbBangsal.setValueAt(totalAvailable - totalPiutang, x, 12); // Set "Diskon Bayar"
                    tbBangsal.setValueAt(0, x, 13); // Set "Tidak Terbayar" to 0
                } else {
                    // If total piutang is greater than the available amount
                    tbBangsal.setValueAt(0, x, 12); // Set "Diskon Bayar" to 0
                    tbBangsal.setValueAt(totalPiutang - totalAvailable, x, 13); // Set "Tidak Terbayar"
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Terjadi kesalahan penghitungan, silahkan ulangi perhitungan!");
            System.out.println("Notif : " + e);
        }
    }

}
