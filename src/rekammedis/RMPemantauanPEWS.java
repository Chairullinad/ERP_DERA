/*
 * Kontribusi Mega RS Simpangan Depok
 */
package rekammedis;

import fungsi.WarnaTablePEWS;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariPetugas;

/**
 *
 * @author perpustakaan
 */
public final class RMPemantauanPEWS extends javax.swing.JDialog {

    private final DefaultTableModel tabMode;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private PreparedStatement ps, ps3;
    private ResultSet rs;
    private int i = 0, umur = 0, umurbulan = 0, umurhari = 0;
    private String sttsumur = "";
    private DlgCariPetugas petugas = new DlgCariPetugas(null, false);

    /**
     * Creates new form DlgRujuk
     *
     * @param parent
     * @param modal
     */
    public RMPemantauanPEWS(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.setLocation(8, 1);
        setSize(628, 674);

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.Rawat",
            "No.R.M.",
            "Nama Pasien",
            "Umur",
            "JK",
            "Tanggal",
            "Neurologi / Perilaku",
            "Skor 1",
            "Status Kardio Vaskuler",
            "Skor 2",
            "CRT",
            "Skor 3",
            "Warna",
            "Skor 4",
            "SStatus Pernafasan",
            "Skor 5",
            "Retraksi",
            "Skor 6",
            "Frekuensi Monitor",
            "Skala Eskalasi",
            "Ttl.Skor",
            "Kesimpulan",
            "NIP",
            "Petugas",
            "Tgl.Lahir"

        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable;
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 25; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(105);
            } else if (i == 1) {
                column.setPreferredWidth(65);
            } else if (i == 2) {
                column.setPreferredWidth(160);
            } else if (i == 3) {
                column.setPreferredWidth(35);
            } else if (i == 4) {
                column.setPreferredWidth(20);
            } else if (i == 5) {
                column.setPreferredWidth(120);
            } else if (i == 6) {
                column.setPreferredWidth(240);
            } else if (i == 7) {
                column.setPreferredWidth(39);
            } else if (i == 8) {
                column.setPreferredWidth(122);
            } else if (i == 9) {
                column.setPreferredWidth(39);
            } else if (i == 10) {
                column.setPreferredWidth(145);
            } else if (i == 11) {
                column.setPreferredWidth(39);
            } else if (i == 12) {
                column.setPreferredWidth(145);
            } else if (i == 13) {
                column.setPreferredWidth(39);
            } else if (i == 14) {
                column.setPreferredWidth(145);
            } else if (i == 15) {
                column.setPreferredWidth(39);
            } else if (i == 16) {
                column.setPreferredWidth(145);
            } else if (i == 17) {
                column.setPreferredWidth(39);
            } else if (i == 18) {
                column.setPreferredWidth(200);
            } else if (i == 19) {
                column.setPreferredWidth(45);
            } else if (i == 20) {
                column.setPreferredWidth(45);
            } else if (i == 21) {
                column.setPreferredWidth(300);
            } else if (i == 22) {
                column.setPreferredWidth(145);
            } else if (i == 23) {
                column.setPreferredWidth(30);
            } else if (i == 24) {
                column.setPreferredWidth(80);
            } else {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTablePEWS());

        TNoRw.setDocument(new batasInput((byte) 17).getKata(TNoRw));
        KdPetugas.setDocument(new batasInput((byte) 20).getKata(KdPetugas));
        TCari.setDocument(new batasInput((int) 100).getKata(TCari));

        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        tampil();
                    }
                }
            });
        }

        petugas.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (petugas.getTable().getSelectedRow() != -1) {
                    KdPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 0).toString());
                    NmPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 1).toString());
                }
                KdPetugas.requestFocus();
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

        ChkInput.setSelected(false);
        isForm();
        jam();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        MnPemantauanPEWS = new javax.swing.JMenuItem();
        JK = new widget.TextBox();
        Umur = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel19 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel21 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        PanelInput = new javax.swing.JPanel();
        FormInput = new widget.PanelBiasa();
        jLabel4 = new widget.Label();
        TNoRw = new widget.TextBox();
        TPasien = new widget.TextBox();
        Tanggal = new widget.Tanggal();
        TNoRM = new widget.TextBox();
        jLabel16 = new widget.Label();
        Jam = new widget.ComboBox();
        Menit = new widget.ComboBox();
        Detik = new widget.ComboBox();
        ChkKejadian = new widget.CekBox();
        jLabel18 = new widget.Label();
        NmPetugas = new widget.TextBox();
        KdPetugas = new widget.TextBox();
        btnPetugas = new widget.Button();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        jLabel14 = new widget.Label();
        cmbSkor1 = new widget.ComboBox();
        jLabel17 = new widget.Label();
        cmbSkor3 = new widget.ComboBox();
        Skor1 = new widget.TextBox();
        jLabel20 = new widget.Label();
        jLabel22 = new widget.Label();
        Skor3 = new widget.TextBox();
        jLabel23 = new widget.Label();
        cmbSkor5 = new widget.ComboBox();
        jLabel26 = new widget.Label();
        Skor5 = new widget.TextBox();
        jLabel27 = new widget.Label();
        TotalSkor = new widget.TextBox();
        jLabel28 = new widget.Label();
        scrollPane3 = new widget.ScrollPane();
        ParameterSkor = new widget.TextArea();
        jLabel24 = new widget.Label();
        jLabel25 = new widget.Label();
        Skor4 = new widget.TextBox();
        jLabel29 = new widget.Label();
        cmbSkor2 = new widget.ComboBox();
        jLabel30 = new widget.Label();
        Skor2 = new widget.TextBox();
        jLabel31 = new widget.Label();
        cmbSkor6 = new widget.ComboBox();
        jLabel32 = new widget.Label();
        Skor6 = new widget.TextBox();
        jLabel43 = new widget.Label();
        jLabel44 = new widget.Label();
        frekuensi = new widget.TextBox();
        cmbSkor4 = new widget.ComboBox();
        jLabel53 = new widget.Label();
        labelUmur = new widget.Label();
        labelUmur2 = new widget.Label();
        eskalasi = new widget.Label();
        ChkInput = new widget.CekBox();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnPemantauanPEWS.setBackground(new java.awt.Color(255, 255, 254));
        MnPemantauanPEWS.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnPemantauanPEWS.setForeground(new java.awt.Color(50, 50, 50));
        MnPemantauanPEWS.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnPemantauanPEWS.setText("Lembar Pemantauan PEWS");
        MnPemantauanPEWS.setName("MnPemantauanPEWS"); // NOI18N
        MnPemantauanPEWS.setPreferredSize(new java.awt.Dimension(200, 26));
        MnPemantauanPEWS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnPemantauanPEWSActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnPemantauanPEWS);

        JK.setHighlighter(null);
        JK.setName("JK"); // NOI18N

        Umur.setHighlighter(null);
        Umur.setName("Umur"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Pemantauan PEWS Pasien Anak ]::", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 2, 12)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);
        Scroll.setPreferredSize(new java.awt.Dimension(452, 200));

        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
        });
        Scroll.setViewportView(tbObat);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 44));
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
        panelGlass8.add(BtnPrint);

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass8.add(LCount);

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

        jPanel3.add(panelGlass8, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel19.setText("Tanggal :");
        jLabel19.setName("jLabel19"); // NOI18N
        jLabel19.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(jLabel19);

        DTPCari1.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "26-11-2024" }));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari1);

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel21.setText("s.d.");
        jLabel21.setName("jLabel21"); // NOI18N
        jLabel21.setPreferredSize(new java.awt.Dimension(23, 23));
        panelGlass9.add(jLabel21);

        DTPCari2.setForeground(new java.awt.Color(50, 70, 50));
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "26-11-2024" }));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari2);

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(90, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(315, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('3');
        BtnCari.setToolTipText("Alt+3");
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
        panelGlass9.add(BtnCari);

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
        panelGlass9.add(BtnAll);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 360));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setBackground(new java.awt.Color(250, 255, 245));
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 225));
        FormInput.setLayout(null);

        jLabel4.setText("No.Rawat :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 75, 23);

        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(79, 10, 141, 23);

        TPasien.setEditable(false);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TPasienKeyPressed(evt);
            }
        });
        FormInput.add(TPasien);
        TPasien.setBounds(336, 10, 285, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "26-11-2024" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setOpaque(false);
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        FormInput.add(Tanggal);
        Tanggal.setBounds(79, 40, 90, 23);

        TNoRM.setEditable(false);
        TNoRM.setName("TNoRM"); // NOI18N
        TNoRM.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRMKeyPressed(evt);
            }
        });
        FormInput.add(TNoRM);
        TNoRM.setBounds(222, 10, 112, 23);

        jLabel16.setText("Tanggal :");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel16);
        jLabel16.setBounds(0, 40, 75, 23);

        Jam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        Jam.setName("Jam"); // NOI18N
        Jam.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JamKeyPressed(evt);
            }
        });
        FormInput.add(Jam);
        Jam.setBounds(173, 40, 62, 23);

        Menit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Menit.setName("Menit"); // NOI18N
        Menit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MenitKeyPressed(evt);
            }
        });
        FormInput.add(Menit);
        Menit.setBounds(238, 40, 62, 23);

        Detik.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Detik.setName("Detik"); // NOI18N
        Detik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DetikKeyPressed(evt);
            }
        });
        FormInput.add(Detik);
        Detik.setBounds(303, 40, 62, 23);

        ChkKejadian.setSelected(true);
        ChkKejadian.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        ChkKejadian.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setName("ChkKejadian"); // NOI18N
        FormInput.add(ChkKejadian);
        ChkKejadian.setBounds(368, 40, 23, 23);

        jLabel18.setText("Petugas :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(400, 40, 70, 23);

        NmPetugas.setEditable(false);
        NmPetugas.setName("NmPetugas"); // NOI18N
        FormInput.add(NmPetugas);
        NmPetugas.setBounds(571, 40, 420, 23);

        KdPetugas.setEditable(false);
        KdPetugas.setName("KdPetugas"); // NOI18N
        KdPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KdPetugasKeyPressed(evt);
            }
        });
        FormInput.add(KdPetugas);
        KdPetugas.setBounds(474, 40, 94, 23);

        btnPetugas.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPetugas.setMnemonic('2');
        btnPetugas.setToolTipText("ALt+2");
        btnPetugas.setName("btnPetugas"); // NOI18N
        btnPetugas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPetugasActionPerformed(evt);
            }
        });
        btnPetugas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnPetugasKeyPressed(evt);
            }
        });
        FormInput.add(btnPetugas);
        btnPetugas.setBounds(1000, 40, 28, 23);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(625, 10, 60, 23);

        TglLahir.setName("TglLahir"); // NOI18N
        FormInput.add(TglLahir);
        TglLahir.setBounds(689, 10, 100, 23);

        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText("1.  Neurologi / Perilaku");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(80, 80, 160, 23);

        cmbSkor1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Bermain / Bisa dibujuk", "Tidur / Tidak nyaman", "Bingung", "Letargik" }));
        cmbSkor1.setName("cmbSkor1"); // NOI18N
        cmbSkor1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbSkor1ItemStateChanged(evt);
            }
        });
        cmbSkor1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSkor1ActionPerformed(evt);
            }
        });
        cmbSkor1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbSkor1KeyPressed(evt);
            }
        });
        FormInput.add(cmbSkor1);
        cmbSkor1.setBounds(230, 80, 420, 23);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel17.setText(" Skor CRT ");
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(120, 140, 110, 23);

        cmbSkor3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0", "1", "2", "3", "4", "5", ">= 6" }));
        cmbSkor3.setName("cmbSkor3"); // NOI18N
        cmbSkor3.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbSkor3ItemStateChanged(evt);
            }
        });
        cmbSkor3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbSkor3KeyPressed(evt);
            }
        });
        FormInput.add(cmbSkor3);
        cmbSkor3.setBounds(230, 140, 420, 23);

        Skor1.setEditable(false);
        Skor1.setBackground(new java.awt.Color(0, 153, 0));
        Skor1.setForeground(new java.awt.Color(255, 255, 255));
        Skor1.setText("0");
        Skor1.setFocusTraversalPolicyProvider(true);
        Skor1.setName("Skor1"); // NOI18N
        Skor1.setOpaque(true);
        FormInput.add(Skor1);
        Skor1.setBounds(750, 80, 44, 23);

        jLabel20.setText("Skor :");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(681, 80, 60, 23);

        jLabel22.setText("Skor :");
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(680, 140, 60, 23);

        Skor3.setEditable(false);
        Skor3.setBackground(new java.awt.Color(0, 153, 0));
        Skor3.setForeground(new java.awt.Color(255, 255, 255));
        Skor3.setText("0");
        Skor3.setFocusTraversalPolicyProvider(true);
        Skor3.setName("Skor3"); // NOI18N
        Skor3.setOpaque(true);
        FormInput.add(Skor3);
        Skor3.setBounds(750, 140, 44, 23);

        jLabel23.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel23.setText("Status Pernafasan");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(80, 200, 140, 23);

        cmbSkor5.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "64", "62", "60", "58", "56", "54", "52", "50", "48", "46", "44", "42", "40", "38", "36", "34", "32", "30", "28", "26", "24", "22", "20", "18", "16", "14", "12", "10", "8", "6", "4" }));
        cmbSkor5.setSelectedIndex(21);
        cmbSkor5.setName("cmbSkor5"); // NOI18N
        cmbSkor5.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbSkor5ItemStateChanged(evt);
            }
        });
        cmbSkor5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbSkor5KeyPressed(evt);
            }
        });
        FormInput.add(cmbSkor5);
        cmbSkor5.setBounds(230, 200, 420, 23);

        jLabel26.setText("Skor :");
        jLabel26.setName("jLabel26"); // NOI18N
        FormInput.add(jLabel26);
        jLabel26.setBounds(680, 200, 60, 23);

        Skor5.setEditable(false);
        Skor5.setBackground(new java.awt.Color(0, 153, 0));
        Skor5.setForeground(new java.awt.Color(255, 255, 255));
        Skor5.setText("0");
        Skor5.setFocusTraversalPolicyProvider(true);
        Skor5.setName("Skor5"); // NOI18N
        Skor5.setOpaque(true);
        FormInput.add(Skor5);
        Skor5.setBounds(750, 200, 44, 23);

        jLabel27.setText("Total Skor :");
        jLabel27.setName("jLabel27"); // NOI18N
        FormInput.add(jLabel27);
        jLabel27.setBounds(0, 270, 75, 23);

        TotalSkor.setEditable(false);
        TotalSkor.setText("0");
        TotalSkor.setFocusTraversalPolicyProvider(true);
        TotalSkor.setName("TotalSkor"); // NOI18N
        FormInput.add(TotalSkor);
        TotalSkor.setBounds(80, 270, 40, 23);

        jLabel28.setText("Parameter :");
        jLabel28.setName("jLabel28"); // NOI18N
        FormInput.add(jLabel28);
        jLabel28.setBounds(0, 70, 75, 23);

        scrollPane3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane3.setName("scrollPane3"); // NOI18N

        ParameterSkor.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ParameterSkor.setColumns(20);
        ParameterSkor.setRows(5);
        ParameterSkor.setName("ParameterSkor"); // NOI18N
        ParameterSkor.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ParameterSkorKeyPressed(evt);
            }
        });
        scrollPane3.setViewportView(ParameterSkor);

        FormInput.add(scrollPane3);
        scrollPane3.setBounds(130, 270, 660, 60);

        jLabel24.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel24.setText(" Warna Kulit");
        jLabel24.setName("jLabel24"); // NOI18N
        FormInput.add(jLabel24);
        jLabel24.setBounds(120, 170, 110, 23);

        jLabel25.setText("Skor :");
        jLabel25.setName("jLabel25"); // NOI18N
        FormInput.add(jLabel25);
        jLabel25.setBounds(680, 170, 60, 23);

        Skor4.setEditable(false);
        Skor4.setBackground(new java.awt.Color(0, 153, 0));
        Skor4.setForeground(new java.awt.Color(255, 255, 255));
        Skor4.setText("0");
        Skor4.setFocusTraversalPolicyProvider(true);
        Skor4.setName("Skor4"); // NOI18N
        Skor4.setOpaque(true);
        FormInput.add(Skor4);
        Skor4.setBounds(750, 170, 44, 23);

        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel29.setText("Status kardio vaskuler");
        jLabel29.setName("jLabel29"); // NOI18N
        FormInput.add(jLabel29);
        jLabel29.setBounds(80, 110, 140, 23);

        cmbSkor2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "190", "185", "180", "175", "170", "165", "160", "155", "150", "145", "140", "135", "130", "125", "120", "115", "110", "105", "100", "95", "90", "85", "80", "75", "70", "65", "60", "55", "50", "45", "40", "35", "30" }));
        cmbSkor2.setSelectedIndex(16);
        cmbSkor2.setName("cmbSkor2"); // NOI18N
        cmbSkor2.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbSkor2ItemStateChanged(evt);
            }
        });
        cmbSkor2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbSkor2KeyPressed(evt);
            }
        });
        FormInput.add(cmbSkor2);
        cmbSkor2.setBounds(230, 110, 420, 23);

        jLabel30.setText("Skor :");
        jLabel30.setName("jLabel30"); // NOI18N
        FormInput.add(jLabel30);
        jLabel30.setBounds(680, 110, 60, 23);

        Skor2.setEditable(false);
        Skor2.setBackground(new java.awt.Color(0, 153, 0));
        Skor2.setForeground(new java.awt.Color(255, 255, 255));
        Skor2.setText("0");
        Skor2.setFocusTraversalPolicyProvider(true);
        Skor2.setName("Skor2"); // NOI18N
        Skor2.setOpaque(true);
        FormInput.add(Skor2);
        Skor2.setBounds(750, 110, 44, 23);

        jLabel31.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel31.setText("Retraksi");
        jLabel31.setName("jLabel31"); // NOI18N
        FormInput.add(jLabel31);
        jLabel31.setBounds(120, 230, 110, 23);

        cmbSkor6.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Tidak Ada Retraksi", "Pakai otot aksesoris", "Retraksi terlihat", "Mendengkur" }));
        cmbSkor6.setName("cmbSkor6"); // NOI18N
        cmbSkor6.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbSkor6ItemStateChanged(evt);
            }
        });
        cmbSkor6.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbSkor6KeyPressed(evt);
            }
        });
        FormInput.add(cmbSkor6);
        cmbSkor6.setBounds(230, 230, 420, 23);

        jLabel32.setText("Skor :");
        jLabel32.setName("jLabel32"); // NOI18N
        FormInput.add(jLabel32);
        jLabel32.setBounds(680, 230, 60, 23);

        Skor6.setEditable(false);
        Skor6.setBackground(new java.awt.Color(0, 153, 0));
        Skor6.setForeground(new java.awt.Color(255, 255, 255));
        Skor6.setText("0");
        Skor6.setFocusTraversalPolicyProvider(true);
        Skor6.setName("Skor6"); // NOI18N
        Skor6.setOpaque(true);
        FormInput.add(Skor6);
        Skor6.setBounds(750, 230, 44, 23);

        jLabel43.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel43.setText("Frekuensi Monitor");
        jLabel43.setName("jLabel43"); // NOI18N
        FormInput.add(jLabel43);
        jLabel43.setBounds(820, 80, 130, 23);

        jLabel44.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel44.setText("Skala Eskalasi");
        jLabel44.setName("jLabel44"); // NOI18N
        FormInput.add(jLabel44);
        jLabel44.setBounds(820, 110, 130, 23);

        frekuensi.setFocusTraversalPolicyProvider(true);
        frekuensi.setName("frekuensi"); // NOI18N
        frekuensi.setOpaque(true);
        FormInput.add(frekuensi);
        frekuensi.setBounds(940, 80, 210, 23);

        cmbSkor4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Pink", "Pucat", "Kelabu", "Kelabu dengan motled" }));
        cmbSkor4.setName("cmbSkor4"); // NOI18N
        cmbSkor4.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbSkor4ItemStateChanged(evt);
            }
        });
        cmbSkor4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbSkor4ActionPerformed(evt);
            }
        });
        cmbSkor4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbSkor4KeyPressed(evt);
            }
        });
        FormInput.add(cmbSkor4);
        cmbSkor4.setBounds(230, 170, 420, 23);

        jLabel53.setText("Umur : ");
        jLabel53.setName("jLabel53"); // NOI18N
        FormInput.add(jLabel53);
        jLabel53.setBounds(790, 10, 60, 23);

        labelUmur.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelUmur.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        labelUmur.setName("labelUmur"); // NOI18N
        FormInput.add(labelUmur);
        labelUmur.setBounds(850, 10, 160, 23);

        labelUmur2.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        labelUmur2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        labelUmur2.setName("labelUmur2"); // NOI18N
        FormInput.add(labelUmur2);
        labelUmur2.setBounds(1040, 10, 70, 23);

        eskalasi.setForeground(new java.awt.Color(255, 0, 0));
        eskalasi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        eskalasi.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        eskalasi.setName("eskalasi"); // NOI18N
        FormInput.add(eskalasi);
        eskalasi.setBounds(940, 110, 160, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('I');
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
        PanelInput.add(ChkInput, java.awt.BorderLayout.PAGE_END);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TNoRwKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRwKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            isRawat();
        } else {
            Valid.pindah(evt, TCari, Tanggal);
        }
}//GEN-LAST:event_TNoRwKeyPressed

    private void TPasienKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TPasienKeyPressed
        Valid.pindah(evt, TCari, BtnSimpan);
}//GEN-LAST:event_TPasienKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
            Valid.textKosong(TNoRw, "pasien");
        } else if (KdPetugas.getText().trim().equals("") || NmPetugas.getText().trim().equals("")) {
            Valid.textKosong(KdPetugas, "Petugas");
        } else if (Skor1.getText().trim().equals("")) {
            Valid.textKosong(Skor1, "Skor 1");
        } else if (Skor2.getText().trim().equals("")) {
            Valid.textKosong(Skor2, "Skor 2");
        } else if (Skor3.getText().trim().equals("")) {
            Valid.textKosong(Skor3, "Skor 3");
        } else if (Skor4.getText().trim().equals("")) {
            Valid.textKosong(Skor4, "Skor 4");
        } else if (Skor5.getText().trim().equals("")) {
            Valid.textKosong(Skor5, "Skor 5");
        } else if (Skor6.getText().trim().equals("")) {
            Valid.textKosong(Skor6, "Skor 6");
        } else {
            isCombo1();
            isCombo2();
            isCombo3();
            isCombo4();
            isCombo5();
            isCombo6();
            isjml();
            isHitung();
            if (Sequel.menyimpantf("pemantauan_pews_anak", "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?", "Data", 19, new String[]{
                TNoRw.getText(),
                Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " + Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(),
                cmbSkor1.getSelectedItem().toString(),
                Skor1.getText(),
                cmbSkor2.getSelectedItem().toString(),
                Skor2.getText(),
                cmbSkor3.getSelectedItem().toString(),
                Skor3.getText(),
                cmbSkor4.getSelectedItem().toString(),
                Skor4.getText(),
                cmbSkor5.getSelectedItem().toString(),
                Skor5.getText(),
                cmbSkor6.getSelectedItem().toString(),
                Skor6.getText(),
                frekuensi.getText(),
                eskalasi.getText().toString(),
                TotalSkor.getText(),
                ParameterSkor.getText(),
                KdPetugas.getText()
            }) == true) {
                tabMode.addRow(new String[]{
                    TNoRw.getText(), TNoRM.getText(), TPasien.getText(), Umur.getText(), JK.getText(), Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " + Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(),
                    cmbSkor1.getSelectedItem().toString(), Skor1.getText(),
                    cmbSkor2.getSelectedItem().toString(), Skor2.getText(),
                    cmbSkor3.getSelectedItem().toString(), Skor3.getText(),
                    cmbSkor4.getSelectedItem().toString(), Skor4.getText(),
                    cmbSkor5.getSelectedItem().toString(), Skor5.getText(),
                    cmbSkor6.getSelectedItem().toString(), Skor6.getText(),
                    frekuensi.getText(),
                    eskalasi.getText().toString(),
                    TotalSkor.getText(),
                    ParameterSkor.getText(),
                    KdPetugas.getText(),
                    NmPetugas.getText(),
                    TglLahir.getText()
                });
                LCount.setText("" + tabMode.getRowCount());

                emptTeks();
            }
        }
        JOptionPane.showMessageDialog(rootPane, "Berhasil Simpan..!!");
        emptTeks();
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnSimpanActionPerformed(null);
        } else {
            Valid.pindah(evt, cmbSkor5, BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        ChkInput.setSelected(true);
        isForm();
        emptTeks();
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            emptTeks();
        } else {
            Valid.pindah(evt, BtnSimpan, BtnHapus);
        }
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if (tbObat.getSelectedRow() > -1) {
            if (akses.getkode().equals("Admin Utama")) {
                hapus();
            } else {
                if (KdPetugas.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(), 22).toString())) {
                    hapus();
                } else {
                    JOptionPane.showMessageDialog(null, "Hanya bisa dihapus oleh petugas yang bersangkutan..!!");
                }
            }
        } else {
            JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
        }
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnHapusActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnBatal, BtnEdit);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        petugas.dispose();
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnKeluarActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnEdit, TCari);
        }
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
//        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//        if (tabMode.getRowCount() == 0) {
//            JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
//            BtnBatal.requestFocus();
//        } else if (tabMode.getRowCount() != 0) {
//            Map<String, Object> param = new HashMap<>();
//            param.put("namars", akses.getnamars());
//            param.put("alamatrs", akses.getalamatrs());
//            param.put("kotars", akses.getkabupatenrs());
//            param.put("propinsirs", akses.getpropinsirs());
//            param.put("kontakrs", akses.getkontakrs());
//            param.put("emailrs", akses.getemailrs());
//            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
//            if (TCari.getText().trim().equals("")) {
//                Valid.MyReportqry("rptDataPemantauanPEWS.jasper", "report", "::[ Data Pemantauan PEWS ]::",
//                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,"
//                        + "pasien.jk,pemantauan_pews_anak.tanggal,pemantauan_pews_anak.parameter_perilaku,pemantauan_pews_anak.skor_perilaku,pemantauan_pews_anak.parameter_crt_atau_warna_kulit,pemantauan_pews_anak.skor_crt_atau_warna_kulit,pemantauan_pews_anak.parameter_perespirasi,pemantauan_pews_anak.skor_perespirasi,"
//                        + "pemantauan_pews_anak.skor_total,pemantauan_pews_anak.parameter_total,pemantauan_pews_anak.nip,petugas.nama,date_format(pasien.tgl_lahir,'%d-%m-%Y') as lahir "
//                        + "from pemantauan_pews_anak inner join reg_periksa on pemantauan_pews_anak.no_rawat=reg_periksa.no_rawat "
//                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
//                        + "inner join petugas on pemantauan_pews_anak.nip=petugas.nip where "
//                        + "pemantauan_pews_anak.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59' order by pemantauan_pews_anak.tanggal ", param);
//            } else {
//                Valid.MyReportqry("rptDataPemantauanPEWS.jasper", "report", "::[ Data Pemantauan PEWS ]::",
//                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,"
//                        + "pasien.jk,pemantauan_pews_anak.tanggal,pemantauan_pews_anak.parameter_perilaku,pemantauan_pews_anak.skor_perilaku,pemantauan_pews_anak.parameter_crt_atau_warna_kulit,pemantauan_pews_anak.skor_crt_atau_warna_kulit,pemantauan_pews_anak.parameter_perespirasi,pemantauan_pews_anak.skor_perespirasi,"
//                        + "pemantauan_pews_anak.skor_total,pemantauan_pews_anak.parameter_total,pemantauan_pews_anak.nip,petugas.nama,date_format(pasien.tgl_lahir,'%d-%m-%Y') as lahir "
//                        + "from pemantauan_pews_anak inner join reg_periksa on pemantauan_pews_anak.no_rawat=reg_periksa.no_rawat "
//                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
//                        + "inner join petugas on pemantauan_pews_anak.nip=petugas.nip "
//                        + "where pemantauan_pews_anak.tanggal between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59' and "
//                        + "(reg_periksa.no_rawat like '%" + TCari.getText().trim() + "%'  or pasien.no_rkm_medis like '%" + TCari.getText().trim() + "%' "
//                        + "or pasien.nm_pasien like '%" + TCari.getText().trim() + "%' "
//                        + "or pemantauan_pews_anak.parameter_total like '%" + TCari.getText().trim() + "%' or pemantauan_pews_anak.nip like '%" + TCari.getText().trim() + "%' or petugas.nama like ?) "
//                        + "order by pemantauan_pews_anak.tanggal ", param);
//            }
//        }
//        this.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnPrintActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnEdit, BtnKeluar);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        tampil();
}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnCariActionPerformed(null);
        } else {
            Valid.pindah(evt, TCari, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        tampil();
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            TCari.setText("");
            tampil();
        } else {
            Valid.pindah(evt, BtnCari, TPasien);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void TanggalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TanggalKeyPressed
        Valid.pindah(evt, TCari, Jam);
}//GEN-LAST:event_TanggalKeyPressed

    private void TNoRMKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TNoRMKeyPressed
        // Valid.pindah(evt, TNm, BtnSimpan);
}//GEN-LAST:event_TNoRMKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked
        if (tabMode.getRowCount() != 0) {
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if (tabMode.getRowCount() != 0) {
            if ((evt.getKeyCode() == KeyEvent.VK_ENTER) || (evt.getKeyCode() == KeyEvent.VK_UP) || (evt.getKeyCode() == KeyEvent.VK_DOWN)) {
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
}//GEN-LAST:event_tbObatKeyPressed

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void JamKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_JamKeyPressed
        Valid.pindah(evt, Tanggal, Menit);
    }//GEN-LAST:event_JamKeyPressed

    private void MenitKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_MenitKeyPressed
        Valid.pindah(evt, Jam, Detik);
    }//GEN-LAST:event_MenitKeyPressed

    private void DetikKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DetikKeyPressed
        Valid.pindah(evt, Menit, btnPetugas);
    }//GEN-LAST:event_DetikKeyPressed

    private void KdPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KdPetugasKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            NmPetugas.setText(petugas.tampil3(KdPetugas.getText()));
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            Detik.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            btnPetugasActionPerformed(null);
        }
    }//GEN-LAST:event_KdPetugasKeyPressed

    private void btnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPetugasActionPerformed
        petugas.emptTeks();
        petugas.isCek();
        petugas.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        petugas.setLocationRelativeTo(internalFrame1);
        petugas.setVisible(true);
    }//GEN-LAST:event_btnPetugasActionPerformed

    private void btnPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPetugasKeyPressed
        Valid.pindah(evt, Detik, cmbSkor1);
    }//GEN-LAST:event_btnPetugasKeyPressed

    private void MnPemantauanPEWSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnPemantauanPEWSActionPerformed
//        if (tbObat.getSelectedRow() > -1) {
//            Map<String, Object> param = new HashMap<>();
//            param.put("namars", akses.getnamars());
//            param.put("alamatrs", akses.getalamatrs());
//            param.put("kotars", akses.getkabupatenrs());
//            param.put("propinsirs", akses.getpropinsirs());
//            param.put("kontakrs", akses.getkontakrs());
//            param.put("emailrs", akses.getemailrs());
//            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
//            param.put("diagnosa", Sequel.cariIsi("select diagnosa_awal from kamar_inap where diagnosa_awal<>'' and no_rawat=? ", TNoRw.getText()));
//            Valid.MyReportqry("rptFormulirPemantauanPEWS.jasper", "report", "::[ Pemantauan PEWS ]::",
//                    "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur, "
//                    + "pasien.jk,pasien.tgl_lahir,pemantauan_pews_anak.tanggal,pemantauan_pews_anak.parameter_perilaku,pemantauan_pews_anak.skor_perilaku,pemantauan_pews_anak.parameter_crt_atau_warna_kulit,pemantauan_pews_anak.skor_crt_atau_warna_kulit, "
//                    + "pemantauan_pews_anak.parameter_perespirasi,pemantauan_pews_anak.skor_perespirasi,pemantauan_pews_anak.skor_total,pemantauan_pews_anak.parameter_total, "
//                    + "pemantauan_pews_anak.nip,petugas.nama "
//                    + "from pemantauan_pews_anak inner join reg_periksa on pemantauan_pews_anak.no_rawat=reg_periksa.no_rawat inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
//                    + "inner join petugas on pemantauan_pews_anak.nip=petugas.nip where reg_periksa.no_rawat='" + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString() + "'", param);
//        }
    }//GEN-LAST:event_MnPemantauanPEWSActionPerformed

    private void cmbSkor1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbSkor1KeyPressed
        Valid.pindah(evt, btnPetugas, cmbSkor3);
    }//GEN-LAST:event_cmbSkor1KeyPressed

    private void cmbSkor3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbSkor3KeyPressed
        Valid.pindah(evt, cmbSkor1, cmbSkor5);
    }//GEN-LAST:event_cmbSkor3KeyPressed

    private void cmbSkor5KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbSkor5KeyPressed
        Valid.pindah(evt, cmbSkor3, BtnSimpan);
    }//GEN-LAST:event_cmbSkor5KeyPressed

    private void cmbSkor1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbSkor1ItemStateChanged
        isCombo1();
        isjml();
        isHitung();
    }//GEN-LAST:event_cmbSkor1ItemStateChanged

    private void cmbSkor3ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbSkor3ItemStateChanged
        isCombo3();
        isjml();
        isHitung();

    }//GEN-LAST:event_cmbSkor3ItemStateChanged

    private void cmbSkor5ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbSkor5ItemStateChanged
        isCombo5();
        isjml();
        isHitung();
    }//GEN-LAST:event_cmbSkor5ItemStateChanged

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnEditActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnHapus, BtnPrint);
        }
    }//GEN-LAST:event_BtnEditKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
            Valid.textKosong(TNoRw, "pasien");
        } else if (KdPetugas.getText().trim().equals("") || NmPetugas.getText().trim().equals("")) {
            Valid.textKosong(KdPetugas, "Petugas");
        } else if (Skor1.getText().trim().equals("")) {
            Valid.textKosong(Skor1, "Skor 1");
        } else if (Skor2.getText().trim().equals("")) {
            Valid.textKosong(Skor2, "Skor 2");
        } else if (Skor3.getText().trim().equals("")) {
            Valid.textKosong(Skor3, "Skor 3");
        } else if (Skor4.getText().trim().equals("")) {
            Valid.textKosong(Skor4, "Skor 4");
        } else if (Skor5.getText().trim().equals("")) {
            Valid.textKosong(Skor5, "Skor 5");
        } else if (Skor6.getText().trim().equals("")) {
            Valid.textKosong(Skor6, "Skor 6");
        } else {
            if (tbObat.getSelectedRow() > -1) {
                if (akses.getkode().equals("Admin Utama")) {
                    ganti();
                } else {
                    if (KdPetugas.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(), 22).toString())) {
                        ganti();
                    } else {
                        JOptionPane.showMessageDialog(null, "Hanya bisa diganti oleh petugas yang bersangkutan..!!");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
            }
        }
        JOptionPane.showMessageDialog(rootPane, "Berhasil Update..!!");
        emptTeks();
    }//GEN-LAST:event_BtnEditActionPerformed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            BtnCariActionPerformed(null);
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            BtnCari.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            BtnKeluar.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            tbObat.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void ParameterSkorKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ParameterSkorKeyPressed
        Valid.pindah2(evt, ParameterSkor, BtnSimpan);
    }//GEN-LAST:event_ParameterSkorKeyPressed

    private void cmbSkor2ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbSkor2ItemStateChanged
        isCombo2();
        isjml();
        isHitung();
    }//GEN-LAST:event_cmbSkor2ItemStateChanged

    private void cmbSkor2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbSkor2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbSkor2KeyPressed

    private void cmbSkor6ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbSkor6ItemStateChanged
        isCombo6();
        isjml();
        isHitung();
    }//GEN-LAST:event_cmbSkor6ItemStateChanged

    private void cmbSkor6KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbSkor6KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbSkor6KeyPressed

    private void cmbSkor4ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbSkor4ItemStateChanged
        isCombo4();
        isjml();
        isHitung();
    }//GEN-LAST:event_cmbSkor4ItemStateChanged

    private void cmbSkor4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSkor4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbSkor4ActionPerformed

    private void cmbSkor4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbSkor4KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbSkor4KeyPressed

    private void cmbSkor1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSkor1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbSkor1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMPemantauanPEWS dialog = new RMPemantauanPEWS(new javax.swing.JFrame(), true);
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
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkInput;
    private widget.CekBox ChkKejadian;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.ComboBox Detik;
    private widget.PanelBiasa FormInput;
    private widget.TextBox JK;
    private widget.ComboBox Jam;
    private widget.TextBox KdPetugas;
    private widget.Label LCount;
    private widget.ComboBox Menit;
    private javax.swing.JMenuItem MnPemantauanPEWS;
    private widget.TextBox NmPetugas;
    private javax.swing.JPanel PanelInput;
    private widget.TextArea ParameterSkor;
    private widget.ScrollPane Scroll;
    private widget.TextBox Skor1;
    private widget.TextBox Skor2;
    private widget.TextBox Skor3;
    private widget.TextBox Skor4;
    private widget.TextBox Skor5;
    private widget.TextBox Skor6;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.Tanggal Tanggal;
    private widget.TextBox TglLahir;
    private widget.TextBox TotalSkor;
    private widget.TextBox Umur;
    private widget.Button btnPetugas;
    private widget.ComboBox cmbSkor1;
    private widget.ComboBox cmbSkor2;
    private widget.ComboBox cmbSkor3;
    private widget.ComboBox cmbSkor4;
    private widget.ComboBox cmbSkor5;
    private widget.ComboBox cmbSkor6;
    private widget.Label eskalasi;
    private widget.TextBox frekuensi;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel14;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel19;
    private widget.Label jLabel20;
    private widget.Label jLabel21;
    private widget.Label jLabel22;
    private widget.Label jLabel23;
    private widget.Label jLabel24;
    private widget.Label jLabel25;
    private widget.Label jLabel26;
    private widget.Label jLabel27;
    private widget.Label jLabel28;
    private widget.Label jLabel29;
    private widget.Label jLabel30;
    private widget.Label jLabel31;
    private widget.Label jLabel32;
    private widget.Label jLabel4;
    private widget.Label jLabel43;
    private widget.Label jLabel44;
    private widget.Label jLabel53;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.Label labelUmur;
    private widget.Label labelUmur2;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollPane3;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            if (TCari.getText().toString().trim().equals("")) {
                ps = koneksi.prepareStatement(
                        "SELECT\n"
                        + "	reg_periksa.no_rawat,\n"
                        + "	pasien.no_rkm_medis,\n"
                        + "	pasien.nm_pasien,\n"
                        + "	reg_periksa.umurdaftar,\n"
                        + "	reg_periksa.sttsumur,\n"
                        + "	pasien.jk,\n"
                        + "	petugas.nama,\n"
                        + "	date_format( pasien.tgl_lahir, '%d-%m-%Y' ) AS lahir,\n"
                        + "	pemantauan_pews_anak.skor_total,\n"
                        + "	pemantauan_pews_anak.tanggal,\n"
                        + "	pemantauan_pews_anak.parameter_perilaku,\n"
                        + "	pemantauan_pews_anak.skor_perilaku,\n"
                        + "	pemantauan_pews_anak.status_kardio_vaskuler,\n"
                        + "	pemantauan_pews_anak.skor_kardio_vaskuler,\n"
                        + "	pemantauan_pews_anak.parameter_crt,\n"
                        + "	pemantauan_pews_anak.skor_crt,\n"
                        + "	pemantauan_pews_anak.parameter_warna_kulit,\n"
                        + "	pemantauan_pews_anak.skor_warna_kulit,\n"
                        + "	pemantauan_pews_anak.status_pernafasan,\n"
                        + "	pemantauan_pews_anak.skor_pernafasan,\n"
                        + "	pemantauan_pews_anak.parameter_perespirasi,\n"
                        + "	pemantauan_pews_anak.skor_perespirasi,\n"
                        + "	pemantauan_pews_anak.frekuensi_monitor,\n"
                        + "	pemantauan_pews_anak.skala_eskalasi,\n"
                        + "	pemantauan_pews_anak.parameter_total,\n"
                        + "	pemantauan_pews_anak.nip "
                        + "from pemantauan_pews_anak inner join reg_periksa on pemantauan_pews_anak.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join petugas on pemantauan_pews_anak.nip=petugas.nip where "
                        + "pemantauan_pews_anak.tanggal between ? and ? order by pemantauan_pews_anak.tanggal ");
            } else {
                ps = koneksi.prepareStatement(
                        "SELECT\n"
                        + "	reg_periksa.no_rawat,\n"
                        + "	pasien.no_rkm_medis,\n"
                        + "	pasien.nm_pasien,\n"
                        + "	reg_periksa.umurdaftar,\n"
                        + "	reg_periksa.sttsumur,\n"
                        + "	pasien.jk,\n"
                        + "	petugas.nama,\n"
                        + "	date_format( pasien.tgl_lahir, '%d-%m-%Y' ) AS lahir,\n"
                        + "	pemantauan_pews_anak.skor_total,\n"
                        + "	pemantauan_pews_anak.tanggal,\n"
                        + "	pemantauan_pews_anak.parameter_perilaku,\n"
                        + "	pemantauan_pews_anak.skor_perilaku,\n"
                        + "	pemantauan_pews_anak.status_kardio_vaskuler,\n"
                        + "	pemantauan_pews_anak.skor_kardio_vaskuler,\n"
                        + "	pemantauan_pews_anak.parameter_crt,\n"
                        + "	pemantauan_pews_anak.skor_crt,\n"
                        + "	pemantauan_pews_anak.parameter_warna_kulit,\n"
                        + "	pemantauan_pews_anak.skor_warna_kulit,\n"
                        + "	pemantauan_pews_anak.status_pernafasan,\n"
                        + "	pemantauan_pews_anak.skor_pernafasan,\n"
                        + "	pemantauan_pews_anak.parameter_perespirasi,\n"
                        + "	pemantauan_pews_anak.skor_perespirasi,\n"
                        + "	pemantauan_pews_anak.frekuensi_monitor,\n"
                        + "	pemantauan_pews_anak.skala_eskalasi,\n"
                        + "	pemantauan_pews_anak.parameter_total,\n"
                        + "	pemantauan_pews_anak.nip "
                        + "from pemantauan_pews_anak inner join reg_periksa on pemantauan_pews_anak.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join petugas on pemantauan_pews_anak.nip=petugas.nip "
                        + "where pemantauan_pews_anak.tanggal between ? and ? and "
                        + "(reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ? or pemantauan_pews_anak.parameter_total like ? or pemantauan_pews_anak.nip like ? or petugas.nama like ?) "
                        + "order by pemantauan_pews_anak.tanggal ");
            }

            try {
                if (TCari.getText().toString().trim().equals("")) {
                    ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                    ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                } else {
                    ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                    ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                    ps.setString(3, "%" + TCari.getText() + "%");
                    ps.setString(4, "%" + TCari.getText() + "%");
                    ps.setString(5, "%" + TCari.getText() + "%");
                    ps.setString(6, "%" + TCari.getText() + "%");
                    ps.setString(7, "%" + TCari.getText() + "%");
                    ps.setString(8, "%" + TCari.getText() + "%");
                }

                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new String[]{
                        rs.getString("no_rawat"),
                        rs.getString("no_rkm_medis"),
                        rs.getString("nm_pasien"),
                        rs.getString("umurdaftar") + " " + rs.getString("sttsumur"),
                        rs.getString("jk"),
                        rs.getString("tanggal"),
                        rs.getString("parameter_perilaku"),
                        rs.getString("skor_perilaku"),
                        rs.getString("status_kardio_vaskuler"),
                        rs.getString("skor_kardio_vaskuler"),
                        rs.getString("parameter_crt"),
                        rs.getString("skor_crt"),
                        rs.getString("parameter_warna_kulit"),
                        rs.getString("skor_warna_kulit"),
                        rs.getString("status_pernafasan"),
                        rs.getString("skor_pernafasan"),
                        rs.getString("parameter_perespirasi"),
                        rs.getString("skor_perespirasi"),
                        rs.getString("frekuensi_monitor"),
                        rs.getString("skala_eskalasi"),
                        rs.getString("skor_total"),
                        rs.getString("parameter_total"),
                        rs.getString("nip"),
                        rs.getString("nama"),
                        rs.getString("lahir")
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
        LCount.setText("" + tabMode.getRowCount());
    }

    private void isCombo1() {
        if (cmbSkor1.getSelectedItem().equals("Tidur / Tidak nyaman")) {
            Skor1.setBackground(Color.YELLOW);
            Skor1.setForeground(new Color(54, 117, 55));
            Skor1.setText("1");
        } else if (cmbSkor1.getSelectedItem().equals("Bingung")) {
            Skor1.setBackground(new Color(245, 110, 32));
            Skor1.setForeground(Color.BLACK);
            Skor1.setText("2");
        } else if (cmbSkor1.getSelectedItem().equals("Letargik")) {
            Skor1.setBackground(new Color(143, 13, 1)); //red
            Skor1.setForeground(Color.WHITE);
            Skor1.setText("3");
        } else {
            Skor1.setBackground(new Color(54, 117, 55));
            Skor1.setForeground(Color.WHITE);
            Skor1.setText("0");

        }
    }

    private void isCombo2() {
        try {
            String umurStr = labelUmur2.getText();
            int umurhr = (umurStr != null && !umurStr.isEmpty()) ? Integer.parseInt(umurStr) : 0;

            String skorStr = cmbSkor2.getSelectedItem() != null ? cmbSkor2.getSelectedItem().toString() : "0";
            int skor = Integer.parseInt(skorStr);

            int kategori = 0;

            if (umurhr < 365) {
                if (skor >= 30 && skor <= 80) {
                    kategori = 1;
                } else if (skor >= 85 && skor <= 90) {
                    kategori = 2;
                } else if (skor >= 95 && skor <= 105) {
                    kategori = 3;
                } else if (skor >= 110 && skor <= 115) {
                    kategori = 4;
                } else if (skor >= 160 && skor <= 165) {
                    kategori = 3;
                } else if (skor >= 170 && skor <= 175) {
                    kategori = 2;
                } else if (skor >= 180 && skor <= 190) {
                    kategori = 1;
                }
            } else if (umurhr >= 365 && umurhr <= 1825) {
                if (skor >= 30 && skor <= 60) {
                    kategori = 1;
                } else if (skor >= 65 && skor <= 80) {
                    kategori = 2;
                } else if (skor >= 85 && skor <= 100) {
                    kategori = 3;
                } else if (skor >= 105 && skor <= 140) {
                    kategori = 4;
                } else if (skor >= 145 && skor <= 155) {
                    kategori = 3;
                } else if (skor >= 160 && skor <= 165) {
                    kategori = 2;
                } else if (skor >= 170 && skor <= 190) {
                    kategori = 1;
                }

            } else if (umurhr >= 1826 && umurhr <= 4380) {
                if (skor >= 30 && skor <= 55) {
                    kategori = 1;
                } else if (skor >= 60 && skor <= 70) {
                    kategori = 2;
                } else if (skor == 75) {
                    kategori = 3;
                } else if (skor >= 80 && skor <= 125) {
                    kategori = 4;
                } else if (skor >= 130 && skor <= 135) {
                    kategori = 3;
                } else if (skor >= 140 && skor <= 145) {
                    kategori = 2;
                } else if (skor >= 150 && skor <= 170) {
                    kategori = 1;
                }

            } else if (umurhr >= 4381) {
                if (skor >= 30 && skor <= 55) {
                    kategori = 1;
                } else if (skor == 60) {
                    kategori = 2;
                } else if (skor >= 65 && skor <= 70) {
                    kategori = 3;
                } else if (skor >= 75 && skor <= 120) {
                    kategori = 4;
                } else if (skor >= 125 && skor <= 135) {
                    kategori = 3;
                } else if (skor >= 140 && skor <= 145) {
                    kategori = 2;
                } else if (skor >= 150 && skor <= 190) {
                    kategori = 1;
                }
            }

            switch (kategori) {
                case 1:
                    Skor2.setBackground(Color.RED);
                    Skor2.setForeground(Color.BLACK);
                    Skor2.setText("3");
                    break;
                case 2:
                    Skor2.setBackground(new Color(245, 110, 32));
                    Skor2.setForeground(Color.BLACK);
                    Skor2.setText("2");
                    break;
                case 3:
                    Skor2.setBackground(Color.YELLOW);
                    Skor2.setForeground(Color.BLACK);
                    Skor2.setText("1");
                    break;
                case 4:
                    Skor2.setBackground(new Color(54, 117, 55));
                    Skor2.setForeground(Color.BLACK);
                    Skor2.setText("0");
                    break;
//                default:
//                    Skor2.setBackground(Color.WHITE);
//                    Skor2.setForeground(Color.BLACK);
//                    Skor2.setText("0");
//                    break;
            }

        } catch (NumberFormatException e) {
            // Tangani exception jika ada input yang tidak valid
            System.out.println("Error parsing number: " + e.getMessage());
        }
    }

    private void isCombo3() {
        if (cmbSkor3.getSelectedItem().equals("0")) {
            Skor3.setBackground(new Color(54, 117, 55));
            Skor3.setForeground(Color.WHITE);
            Skor3.setText("0");
        } else if (cmbSkor3.getSelectedItem().equals("1")) {
            Skor3.setBackground(new Color(54, 117, 55));
            Skor3.setForeground(Color.WHITE);
            Skor3.setText("0");
        } else if (cmbSkor3.getSelectedItem().equals("2")) {
            Skor3.setBackground(new Color(54, 117, 55));
            Skor3.setForeground(Color.WHITE);
            Skor3.setText("0");
        } else if (cmbSkor3.getSelectedItem().equals("3")) {
            Skor3.setBackground(Color.YELLOW);
            Skor3.setForeground(new Color(54, 117, 55));
            Skor3.setText("1");
        } else if (cmbSkor3.getSelectedItem().equals("4")) {
            Skor3.setBackground(new Color(245, 110, 32));
            Skor3.setForeground(Color.BLACK);
            Skor3.setText("2");
        } else if (cmbSkor3.getSelectedItem().equals("5")) {
            Skor3.setBackground(new Color(143, 13, 1));
            Skor3.setForeground(Color.WHITE);
            Skor3.setText("3");
        } else if (cmbSkor3.getSelectedItem().equals(">= 6")) {
            Skor3.setBackground(new Color(143, 13, 1));
            Skor3.setForeground(Color.WHITE);
            Skor3.setText("3");
        }

    }

    private void isCombo4() {
        if (cmbSkor4.getSelectedItem().equals("Pink")) {
            Skor4.setBackground(new Color(54, 117, 55));
            Skor4.setForeground(Color.BLACK);
            Skor4.setText("0");
        } else if (cmbSkor4.getSelectedItem().equals("Pucat")) {
            Skor4.setBackground(Color.YELLOW);
            Skor4.setForeground(Color.BLACK);
            Skor4.setText("1");
        } else if (cmbSkor4.getSelectedItem().equals("Kelabu")) {
            Skor4.setBackground(new Color(245, 110, 32));
            Skor4.setForeground(Color.BLACK);
            Skor4.setText("2");
        } else if (cmbSkor4.getSelectedItem().equals("Kelabu dengan motled")) {
            Skor4.setBackground(Color.RED);
            Skor4.setForeground(Color.BLACK);
            Skor4.setText("3");

        }
    }

    private void isCombo5() {
        String umurStr = labelUmur2.getText();
        int umurhr = (umurStr != null && !umurStr.isEmpty()) ? Integer.parseInt(umurStr) : 0;

        String skorStrt = cmbSkor5.getSelectedItem() != null ? cmbSkor5.getSelectedItem().toString() : "0";
        int skort = Integer.parseInt(skorStrt);
        int kategorit = 0;

        if (umurhr <= 364) {
            if (skort >= 4 && skort <= 14) {
                kategorit = 1;
            } else if (skort >= 16 && skort <= 20) {
                kategorit = 2;
            } else if (skort >= 22 && skort <= 26) {
                kategorit = 3;
            } else if (skort >= 28 && skort <= 40) {
                kategorit = 4;
            } else if (skort >= 42 && skort <= 48) {
                kategorit = 3;
            } else if (skort >= 50 && skort <= 54) {
                kategorit = 2;
            } else if (skort >= 56 && skort <= 64) {
                kategorit = 1;
            }
        } else if (umurhr >= 365 && umurhr <= 1825) {
            if (skort >= 4 && skort <= 10) {
                kategorit = 1;
            } else if (skort >= 12 && skort <= 16) {
                kategorit = 2;
            } else if (skort >= 18 && skort <= 20) {
                kategorit = 3;
            } else if (skort >= 22 && skort <= 32) {
                kategorit = 4;
            } else if (skort >= 34 && skort <= 36) {
                kategorit = 3;
            } else if (skort >= 38 && skort <= 42) {
                kategorit = 2;
            } else if (skort >= 44 && skort <= 64) {
                kategorit = 1;
            }

        } else if (umurhr > 1825 && umurhr <= 4380) {
            if (skort >= 4 && skort <= 10) {
                kategorit = 1;
            } else if (skort == 12) {
                kategorit = 2;
            } else if (skort >= 14 && skort <= 16) {
                kategorit = 3;
            } else if (skort >= 18 && skort <= 28) {
                kategorit = 4;
            } else if (skort >= 30 && skort <= 32) {
                kategorit = 3;
            } else if (skort >= 34 && skort <= 38) {
                kategorit = 2;
            } else if (skort >= 40 && skort <= 64) {
                kategorit = 1;
            }

        } else if (umurhr > 4380) {
            if (skort >= 4 && skort <= 8) {
                kategorit = 1;
            } else if (skort == 10) {
                kategorit = 2;
            } else if (skort == 12) {
                kategorit = 3;
            } else if (skort >= 14 && skort <= 22) {
                kategorit = 4;
            } else if (skort >= 24 && skort <= 26) {
                kategorit = 3;
            } else if (skort >= 28 && skort <= 32) {
                kategorit = 2;
            } else if (skort >= 34 && skort <= 64) {
                kategorit = 1;
            }
        }

        switch (kategorit) {
            case 1:
                Skor5.setBackground(Color.RED);
                Skor5.setForeground(Color.BLACK);
                Skor5.setText("3");
                break;
            case 2:
                Skor5.setBackground(new Color(245, 110, 32)); //orange
                Skor5.setForeground(Color.BLACK);
                Skor5.setText("2");
                break;
            case 3:
                Skor5.setBackground(Color.YELLOW);
                Skor5.setForeground(Color.BLACK);
                Skor5.setText("1");
                break;
            case 4:
                Skor5.setBackground(new Color(54, 117, 55));
                Skor5.setForeground(Color.BLACK);
                Skor5.setText("0");
                break;
//            default:
//                Skor5.setBackground(Color.WHITE);
//                Skor5.setForeground(Color.BLACK);
//                Skor5.setText("0");
//                break;
        }
    }

    private void isCombo6() {
        if (cmbSkor6.getSelectedItem().equals("Pakai otot aksesoris")) {
            Skor6.setBackground(Color.YELLOW);
            Skor6.setForeground(Color.BLACK);
            Skor6.setText("1");
        } else if (cmbSkor6.getSelectedItem().equals("Retraksi terlihat")) {
            Skor6.setBackground(new Color(245, 110, 32));
            Skor6.setForeground(Color.WHITE);
            Skor6.setText("2");
        } else if (cmbSkor6.getSelectedItem().equals("Mendengkur")) {
            Skor6.setBackground(new Color(143, 13, 1));
            Skor6.setForeground(Color.WHITE);
            Skor6.setText("3");
        } else {
            Skor6.setBackground(new Color(54, 117, 55));
            Skor6.setForeground(Color.WHITE);
            Skor6.setText("0");
        }
    }

    private void isjml() {
        if ((!Skor1.getText().equals("")) && (!Skor2.getText().equals("")) && (!Skor3.getText().equals("")) && (!Skor4.getText().equals("")) && (!Skor5.getText().equals("")) && (!Skor6.getText().equals(""))) {
            TotalSkor.setText(Valid.SetAngka2(Double.parseDouble(Skor1.getText().trim())
                    + Double.parseDouble(Skor2.getText().trim())
                    + Double.parseDouble(Skor3.getText().trim())
                    + Double.parseDouble(Skor4.getText().trim())
                    + Double.parseDouble(Skor5.getText().trim())
                    + Double.parseDouble(Skor6.getText().trim())
            ));
        }
    }

    private void isHitung() {
        try {
            int totalSkor = Integer.parseInt(TotalSkor.getText()); // Parsing nilai dari input

            if (totalSkor >= 0 && totalSkor <= 2) {
                frekuensi.setText("Monitoring Rutin setiap 4 jam.");
                eskalasi.setText("Tidak");
                eskalasi.setForeground(Color.BLACK);
                ParameterSkor.setText("Dokumentasikan ke dalam form monitoring PEWS, CPPT, dan Catatan Keperawatan");
            } else if (totalSkor >= 3 && totalSkor <= 4) {
                frekuensi.setText("Monitoring setiap 2 jam.");
                eskalasi.setText("Ya");
                eskalasi.setForeground(Color.RED);
                ParameterSkor.setText("Laporkan ke dokter jaga ruangan");
            } else if (totalSkor >= 5) {
                frekuensi.setText("Monitoring setiap 1 jam.");
                eskalasi.setText("Ya");
                eskalasi.setForeground(Color.RED);
                ParameterSkor.setText("1. Dokter jaga menghubungi DPJP\n"
                        + "2. DPJP akan menentukan tindakan selanjutnya (ke ICU, DNR atau tindakan lain)");
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Masukkan nilai Total Skor yang valid!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void emptTeks() {
        Tanggal.setDate(new Date());
        cmbSkor1.setSelectedIndex(0);
        Skor1.setText("0");
        Skor1.setBackground(new Color(54, 117, 55));
        Skor1.setForeground(Color.WHITE);
        cmbSkor2.setSelectedIndex(16);
        Skor2.setText("0");
        Skor2.setBackground(new Color(54, 117, 55));
        Skor2.setForeground(Color.WHITE);
        cmbSkor3.setSelectedIndex(2);
        Skor3.setText("0");
        Skor3.setBackground(new Color(54, 117, 55));
        Skor3.setForeground(Color.WHITE);
        cmbSkor4.setSelectedIndex(0);
        Skor4.setText("0");
        Skor4.setBackground(new Color(54, 117, 55));
        Skor4.setForeground(Color.WHITE);
        cmbSkor5.setSelectedIndex(21);
        Skor5.setText("0");
        Skor5.setBackground(new Color(54, 117, 55));
        Skor5.setForeground(Color.WHITE);
        cmbSkor6.setSelectedIndex(0);
        Skor6.setText("0");
        Skor6.setBackground(new Color(54, 117, 55));
        Skor6.setForeground(Color.WHITE);
        ParameterSkor.setText("Dokumentasikan ke dalam form monitoring PEWS, CPPT, dan Catatan Keperawatan");
        cmbSkor1.requestFocus();
        frekuensi.setText("Monitoring Rutin setiap 4 jam.");
        eskalasi.setText("Tidak");
    }

    private void getData() {
        if (tbObat.getSelectedRow() != -1) {
            TNoRw.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString());
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 2).toString());
            cmbSkor1.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString());
            Skor1.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString());
            cmbSkor2.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 8).toString());
            Skor2.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 9).toString());
            cmbSkor3.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 10).toString());
            Skor3.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 11).toString());
            cmbSkor4.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 12).toString());
            Skor4.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 13).toString());
            cmbSkor5.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 14).toString());
            Skor5.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 15).toString());
            cmbSkor6.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 16).toString());
            Skor6.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 17).toString());
            frekuensi.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 18).toString());
            eskalasi.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 19).toString());
            TotalSkor.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 20).toString());
            ParameterSkor.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 21).toString());
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 24).toString());
            Valid.SetTgl(Tanggal, tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString());
            Jam.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString().substring(11, 13));
            Menit.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString().substring(14, 16));
            Detik.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString().substring(17, 19));
        }
    }

    private void isRawat() {
        try {
            ps = koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.tgl_lahir,reg_periksa.tgl_registrasi,reg_periksa.umurdaftar,"
                    + "reg_periksa.sttsumur from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat=?");
            try {
                ps.setString(1, TNoRw.getText());
                rs = ps.executeQuery();
                if (rs.next()) {
                    TNoRM.setText(rs.getString("no_rkm_medis"));
                    DTPCari1.setDate(rs.getDate("tgl_registrasi"));
                    TPasien.setText(rs.getString("nm_pasien"));
                    JK.setText(rs.getString("jk"));
                    Umur.setText(rs.getString("umurdaftar") + " " + rs.getString("sttsumur"));
                    TglLahir.setText(rs.getString("tgl_lahir"));
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

    public void setNoRm(String norwt, Date tgl2) {
        TNoRw.setText(norwt);
        TCari.setText(norwt);
        DTPCari2.setDate(tgl2);
        isRawat();
        ChkInput.setSelected(true);
        isForm();
        isCekPasien();
    }

    private void isForm() {
        if (ChkInput.isSelected() == true) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 360));
            FormInput.setVisible(true);
            ChkInput.setVisible(true);
        } else if (ChkInput.isSelected() == false) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 20));
            FormInput.setVisible(false);
            ChkInput.setVisible(true);
        }
    }

    public void isCek() {
        BtnSimpan.setEnabled(akses.getpemantauan_pews_anak());
        BtnHapus.setEnabled(akses.getpemantauan_pews_anak());
        BtnEdit.setEnabled(akses.getpemantauan_pews_anak());
        BtnPrint.setEnabled(akses.getpemantauan_pews_anak());
        if (akses.getjml2() >= 1) {
            KdPetugas.setEditable(false);
            btnPetugas.setEnabled(false);
            KdPetugas.setText(akses.getkode());
            NmPetugas.setText(petugas.tampil3(KdPetugas.getText()));
            if (NmPetugas.getText().equals("")) {
                KdPetugas.setText("");
                JOptionPane.showMessageDialog(null, "User login bukan petugas...!!");
            }
        }
        labelUmur2.setVisible(false);
    }

    private void jam() {
        ActionListener taskPerformer = new ActionListener() {
            private int nilai_jam;
            private int nilai_menit;
            private int nilai_detik;

            public void actionPerformed(ActionEvent e) {
                String nol_jam = "";
                String nol_menit = "";
                String nol_detik = "";

                Date now = Calendar.getInstance().getTime();

                // Mengambil nilaj JAM, MENIT, dan DETIK Sekarang
                if (ChkKejadian.isSelected() == true) {
                    nilai_jam = now.getHours();
                    nilai_menit = now.getMinutes();
                    nilai_detik = now.getSeconds();
                } else if (ChkKejadian.isSelected() == false) {
                    nilai_jam = Jam.getSelectedIndex();
                    nilai_menit = Menit.getSelectedIndex();
                    nilai_detik = Detik.getSelectedIndex();
                }

                // Jika nilai JAM lebih kecil dari 10 (hanya 1 digit)
                if (nilai_jam <= 9) {
                    // Tambahkan "0" didepannya
                    nol_jam = "0";
                }
                // Jika nilai MENIT lebih kecil dari 10 (hanya 1 digit)
                if (nilai_menit <= 9) {
                    // Tambahkan "0" didepannya
                    nol_menit = "0";
                }
                // Jika nilai DETIK lebih kecil dari 10 (hanya 1 digit)
                if (nilai_detik <= 9) {
                    // Tambahkan "0" didepannya
                    nol_detik = "0";
                }
                // Membuat String JAM, MENIT, DETIK
                String jam = nol_jam + Integer.toString(nilai_jam);
                String menit = nol_menit + Integer.toString(nilai_menit);
                String detik = nol_detik + Integer.toString(nilai_detik);
                // Menampilkan pada Layar
                //tampil_jam.setText("  " + jam + " : " + menit + " : " + detik + "  ");
                Jam.setSelectedItem(jam);
                Menit.setSelectedItem(menit);
                Detik.setSelectedItem(detik);
            }
        };
        // Timer
        new Timer(1000, taskPerformer).start();
    }

    private void ganti() {
        isCombo1();
        isCombo2();
        isCombo3();
        isCombo4();
        isCombo5();
        isCombo6();
        isjml();
        isHitung();
        if (Sequel.mengedittf("pemantauan_pews_anak", "tanggal=? and no_rawat=?",
                "no_rawat=?,tanggal=?,parameter_perilaku=?,skor_perilaku=?,status_kardio_vaskuler=?,\n"
                + "skor_kardio_vaskuler=?,parameter_crt=?,skor_crt=?,parameter_warna_kulit=?,\n"
                + "skor_warna_kulit=?,status_pernafasan=?,skor_pernafasan=?,"
                + "parameter_perespirasi=?,skor_perespirasi=?,\n"
                + "frekuensi_monitor=?,skala_eskalasi=?,skor_total=?,parameter_total=?,nip=?", 21, new String[]{
                    TNoRw.getText(), Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " + Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(),
                    cmbSkor1.getSelectedItem().toString(), Skor1.getText(),
                    cmbSkor2.getSelectedItem().toString(), Skor2.getText(),
                    cmbSkor3.getSelectedItem().toString(), Skor3.getText(),
                    cmbSkor4.getSelectedItem().toString(), Skor4.getText(),
                    cmbSkor5.getSelectedItem().toString(), Skor5.getText(),
                    cmbSkor6.getSelectedItem().toString(), Skor6.getText(),
                    frekuensi.getText(),
                    eskalasi.getText().toString(),
                    TotalSkor.getText(),
                    ParameterSkor.getText(),
                    KdPetugas.getText(),
                    tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString(),
                    tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()
                }) == true) {
            tbObat.setValueAt(TNoRw.getText(), tbObat.getSelectedRow(), 0);
            tbObat.setValueAt(TNoRM.getText(), tbObat.getSelectedRow(), 1);
            tbObat.setValueAt(TPasien.getText(), tbObat.getSelectedRow(), 2);
            tbObat.setValueAt(Umur.getText(), tbObat.getSelectedRow(), 3);
            tbObat.setValueAt(JK.getText(), tbObat.getSelectedRow(), 4);
            tbObat.setValueAt(Valid.SetTgl(Tanggal.getSelectedItem() + "") + " " + Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(), tbObat.getSelectedRow(), 5);
            tbObat.setValueAt(cmbSkor1.getSelectedItem().toString(), tbObat.getSelectedRow(), 6);
            tbObat.setValueAt(Skor1.getText(), tbObat.getSelectedRow(), 7);
            tbObat.setValueAt(cmbSkor2.getSelectedItem().toString(), tbObat.getSelectedRow(), 8);
            tbObat.setValueAt(Skor2.getText(), tbObat.getSelectedRow(), 9);
            tbObat.setValueAt(cmbSkor3.getSelectedItem().toString(), tbObat.getSelectedRow(), 10);
            tbObat.setValueAt(Skor3.getText(), tbObat.getSelectedRow(), 11);
            tbObat.setValueAt(cmbSkor4.getSelectedItem().toString(), tbObat.getSelectedRow(), 12);
            tbObat.setValueAt(Skor4.getText(), tbObat.getSelectedRow(), 13);
            tbObat.setValueAt(cmbSkor5.getSelectedItem().toString(), tbObat.getSelectedRow(), 14);
            tbObat.setValueAt(Skor5.getText(), tbObat.getSelectedRow(), 15);
            tbObat.setValueAt(cmbSkor6.getSelectedItem().toString(), tbObat.getSelectedRow(), 16);
            tbObat.setValueAt(Skor6.getText(), tbObat.getSelectedRow(), 17);
            tbObat.setValueAt(frekuensi.getText(), tbObat.getSelectedRow(), 18);
            tbObat.setValueAt(eskalasi.getText().toString(), tbObat.getSelectedRow(), 19);
            tbObat.setValueAt(TotalSkor.getText(), tbObat.getSelectedRow(), 20);
            tbObat.setValueAt(ParameterSkor.getText(), tbObat.getSelectedRow(), 21);
            tbObat.setValueAt(KdPetugas.getText(), tbObat.getSelectedRow(), 22);
            tbObat.setValueAt(NmPetugas.getText(), tbObat.getSelectedRow(), 23);
            tbObat.setValueAt(TglLahir.getText(), tbObat.getSelectedRow(), 24);
            emptTeks();
        }
    }

    private void hapus() {
        if (Sequel.queryu2tf("delete from pemantauan_pews_anak where tanggal=? and no_rawat=?", 2, new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString(), tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()
        }) == true) {
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText("" + tabMode.getRowCount());
            JOptionPane.showMessageDialog(rootPane, "Berhasil Hapus..!!");
            emptTeks();
        } else {
            JOptionPane.showMessageDialog(null, "Gagal menghapus..!!");
        }
    }

    private void isCekPasien() {
        try {
            ps3 = koneksi.prepareStatement(
                    "SELECT "
                    + "    nm_pasien, "
                    + "    TIMESTAMPDIFF(YEAR, tgl_lahir, ?) AS tahun, "
                    + "    (TIMESTAMPDIFF(MONTH, tgl_lahir, ?) % 12) AS bulan, "
                    + "    DATEDIFF(?, DATE_ADD(DATE_ADD(tgl_lahir, INTERVAL TIMESTAMPDIFF(YEAR, tgl_lahir, ?) YEAR), INTERVAL TIMESTAMPDIFF(MONTH, tgl_lahir, ?) % 12 MONTH)) AS hari, "
                    + "    DATEDIFF(?, tgl_lahir) AS total_hari "
                    + "FROM pasien "
                    + "WHERE pasien.no_rkm_medis = ?"
            );

            try {
                ps3.setString(1, Valid.SetTgl(Tanggal.getSelectedItem() + ""));
                ps3.setString(2, Valid.SetTgl(Tanggal.getSelectedItem() + ""));
                ps3.setString(3, Valid.SetTgl(Tanggal.getSelectedItem() + ""));
                ps3.setString(4, Valid.SetTgl(Tanggal.getSelectedItem() + ""));
                ps3.setString(5, Valid.SetTgl(Tanggal.getSelectedItem() + ""));
                ps3.setString(6, Valid.SetTgl(Tanggal.getSelectedItem() + ""));
                ps3.setString(7, TNoRM.getText());

                rs = ps3.executeQuery();
                if (rs.next()) {
                    String tahunStr = rs.getString("tahun");
                    String bulanStr = rs.getString("bulan");
                    String hariStr = rs.getString("hari");
                    String totalHariStr = rs.getString("total_hari");

                    int tahun = tahunStr != null && !tahunStr.isEmpty() ? Integer.parseInt(tahunStr) : 0;
                    int bulan = bulanStr != null && !bulanStr.isEmpty() ? Integer.parseInt(bulanStr) : 0;
                    int hari = hariStr != null && !hariStr.isEmpty() ? Integer.parseInt(hariStr) : 0;
                    int totalHari = totalHariStr != null && !totalHariStr.isEmpty() ? Integer.parseInt(totalHariStr) : 0;

                    if (tahun > 0) {
                        labelUmur.setText(tahun + " Th " + bulan + " Bln " + hari + " Hr");
                    } else if (bulan > 0) {
                        labelUmur.setText(bulan + " Bln " + hari + " Hr");
                    } else {
                        labelUmur.setText(hari + " Hr");
                    }

                    labelUmur2.setText(String.valueOf(totalHari));
                }

            } catch (Exception ex) {
                System.out.println(ex);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps3 != null) {
                    ps3.close();
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
