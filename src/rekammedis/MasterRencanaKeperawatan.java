/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

 /*
 * DlgSpesialis.java
 *
 * Created on May 23, 2010, 1:25:13 AM
 */
package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
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
 *
 * @author dosen
 */
public class MasterRencanaKeperawatan extends javax.swing.JDialog {

    private final DefaultTableModel tabMode, tabModeSLKI, tabModeSIKI;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private MasterDiagnosaKeperawatan diagnosa = new MasterDiagnosaKeperawatan(null, false);

    /**
     * Creates new form DlgSpesialis
     *
     * @param parent
     * @param modal
     */
    public MasterRencanaKeperawatan(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        Object[] row = {
            "Kode Masalah",
            "Masalah Keperawatan",
            "Kode",
            "Entitas",
            "Sub-Entitas",
            "Diagnosa Keperawatan",
            "Kategori",
            "Definisi",
            "Kd Rencana"};
        tabMode = new DefaultTableModel(null, row) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };

        tbSDKI.setModel(tabMode);
        //tampil();
        //tbJabatan.setDefaultRenderer(Object.class, new WarnaTable(Scroll.getBackground(),Color.GREEN));
        tbSDKI.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbSDKI.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < 9; i++) {
            TableColumn column = tbSDKI.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 1) {
                column.setPreferredWidth(200);
            } else if (i == 2) {
                column.setPreferredWidth(80);
            } else if (i == 3) {
                column.setPreferredWidth(170);
            } else if (i == 4) {
                column.setPreferredWidth(170);
            } else if (i == 5) {
                column.setPreferredWidth(270);
            } else if (i == 6) {
                column.setPreferredWidth(80);
            } else if (i == 7) {
                column.setPreferredWidth(400);
            } else if (i == 8) {
                column.setPreferredWidth(80);
            }
        }

        tbSDKI.setDefaultRenderer(Object.class, new WarnaTable());

        Object[] rowSLKI = {"Kode Masalah",
            "Masalah Keperawatan",
            "Kode",
            "Entitas",
            "Sub-Entitas",
            "Diagnosa Keperawatan",
            "Kategori",
            "Definisi",
            "Kd Rencana"};
        tabModeSLKI = new DefaultTableModel(null, rowSLKI) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };

        tbSLKI.setModel(tabModeSLKI);
        //tampil();
        //tbJabatan.setDefaultRenderer(Object.class, new WarnaTable(Scroll.getBackground(),Color.GREEN));
        tbSLKI.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbSLKI.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < 9; i++) {
            TableColumn column = tbSLKI.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 1) {
                column.setPreferredWidth(200);
            } else if (i == 2) {
                column.setPreferredWidth(80);
            } else if (i == 3) {
                column.setPreferredWidth(170);
            } else if (i == 4) {
                column.setPreferredWidth(170);
            } else if (i == 5) {
                column.setPreferredWidth(270);
            } else if (i == 6) {
                column.setPreferredWidth(80);
            } else if (i == 7) {
                column.setPreferredWidth(400);
            } else if (i == 8) {
                column.setPreferredWidth(80);
            }
        }

        tbSLKI.setDefaultRenderer(Object.class, new WarnaTable());

        Object[] rowSIKI = {"Kode Masalah",
            "Masalah Keperawatan",
            "Kode",
            "Entitas",
            "Sub-Entitas",
            "Diagnosa Keperawatan",
            "Kategori",
            "Definisi",
            "Kd Rencana"};
        tabModeSIKI = new DefaultTableModel(null, rowSIKI) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };

        tbSIKI.setModel(tabModeSIKI);
        //tampil();
        //tbJabatan.setDefaultRenderer(Object.class, new WarnaTable(Scroll.getBackground(),Color.GREEN));
        tbSIKI.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbSIKI.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (int i = 0; i < 9; i++) {
            TableColumn column = tbSIKI.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 1) {
                column.setPreferredWidth(200);
            } else if (i == 2) {
                column.setPreferredWidth(80);
            } else if (i == 3) {
                column.setPreferredWidth(170);
            } else if (i == 4) {
                column.setPreferredWidth(170);
            } else if (i == 5) {
                column.setPreferredWidth(270);
            } else if (i == 6) {
                column.setPreferredWidth(80);
            } else if (i == 7) {
                column.setPreferredWidth(400);
            } else if (i == 8) {
                column.setPreferredWidth(80);
            }
        }

        tbSIKI.setDefaultRenderer(Object.class, new WarnaTable());

        TCari.setDocument(new batasInput((byte) 100).getKata(TCari));
        if (TabRawat.getSelectedIndex() == 0) {
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

        } else if (TabRawat.getSelectedIndex() == 1) {
            if (koneksiDB.CARICEPAT().equals("aktif")) {
                TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        if (TCari.getText().length() > 2) {
                            tampilSLKI();
                        }
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        if (TCari.getText().length() > 2) {
                            tampilSLKI();
                        }
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        if (TCari.getText().length() > 2) {
                            tampilSLKI();
                        }
                    }
                });
            }

        } else if (TabRawat.getSelectedIndex() == 2) {
            if (koneksiDB.CARICEPAT().equals("aktif")) {
                TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        if (TCari.getText().length() > 2) {
                            tampilSIKI();
                        }
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        if (TCari.getText().length() > 2) {
                            tampilSIKI();
                        }
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        if (TCari.getText().length() > 2) {
                            tampilSIKI();
                        }
                    }
                });
            }

        }

        diagnosa.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {
            }

            @Override
            public void windowClosing(WindowEvent e) {
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (TabRawat.getSelectedIndex() == 0) {
                    if (diagnosa.getTable().getSelectedRow() != -1) {
                        kdmasalah.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 1).toString());
                        nmmasalah.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 3).toString());
                        kode.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 2).toString());
                        entitas.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 6).toString());
                        sub_entitas.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 7).toString());
                        if (diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 8).toString().trim().equals("")) {
                            rencanaKep.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 3).toString() + " berhubungan dengan ");
                        } else {
                            rencanaKep.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 8).toString());
                        }
                        Definisi.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 9).toString());
                        kdrencana.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 4).toString());
                        if (!diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 5).toString().trim().equals("")) {
                            kategori.setSelectedItem(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 5).toString());
                        }
                    }
                    btnPemeriksaan.requestFocus();
                } else if (TabRawat.getSelectedIndex() == 1) {
                    if (diagnosa.getTable().getSelectedRow() != -1) {
                        kdmasalahSLKI.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 1).toString());
                        nmmasalahSLKI.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 3).toString());
                        kode1.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 2).toString());
                        entitas1.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 6).toString());
                        sub_entitas1.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 7).toString());
                        rencanaKepSLKI.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 8).toString());
                        Definisi1.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 9).toString());
                        kdrencanaSLKI.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 4).toString());
                        if (!diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 5).toString().trim().equals("")) {
                            kategori3.setSelectedItem(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 5).toString());
                        }
                    }
                    btnPemeriksaan1.requestFocus();
                } else if (TabRawat.getSelectedIndex() == 2) {
                    if (diagnosa.getTable().getSelectedRow() != -1) {
                        kdmasalahSIKI.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 1).toString());
                        nmmasalahSIKI.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 3).toString());
                        kode2.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 2).toString());
                        entitas2.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 6).toString());
                        sub_entitas2.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 7).toString());
                        rencanaKepSIKI.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 8).toString());
                        Definisi2.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 9).toString());
                        kdrencanaSIKI.setText(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 4).toString());
                        if (!diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 5).toString().trim().equals("")) {
                            kategori4.setSelectedItem(diagnosa.getTable().getValueAt(diagnosa.getTable().getSelectedRow(), 5).toString());
                        }
                    }
                    btnPemeriksaan2.requestFocus();
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

        diagnosa.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    diagnosa.dispose();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        TabRawat = new javax.swing.JTabbedPane();
        internalFrame2 = new widget.InternalFrame();
        panelGlass7 = new widget.panelisi();
        jLabel3 = new widget.Label();
        jLabel4 = new widget.Label();
        kdrencana = new widget.TextBox();
        jLabel5 = new widget.Label();
        kdmasalah = new widget.TextBox();
        nmmasalah = new widget.TextBox();
        btnPemeriksaan = new widget.Button();
        scrollPane1 = new widget.ScrollPane();
        rencanaKep = new widget.TextArea();
        Definisi = new widget.TextBox();
        jLabel14 = new widget.Label();
        kode = new widget.TextBox();
        jLabel17 = new widget.Label();
        jLabel18 = new widget.Label();
        entitas = new widget.TextBox();
        jLabel19 = new widget.Label();
        sub_entitas = new widget.TextBox();
        kategori = new widget.ComboBox();
        Scroll = new widget.ScrollPane();
        tbSDKI = new widget.Table();
        internalFrame3 = new widget.InternalFrame();
        panelGlass10 = new widget.panelisi();
        jLabel9 = new widget.Label();
        kdrencanaSLKI = new widget.TextBox();
        jLabel10 = new widget.Label();
        kdmasalahSLKI = new widget.TextBox();
        nmmasalahSLKI = new widget.TextBox();
        btnPemeriksaan1 = new widget.Button();
        scrollPane2 = new widget.ScrollPane();
        rencanaKepSLKI = new widget.TextArea();
        jLabel15 = new widget.Label();
        Definisi1 = new widget.TextBox();
        jLabel20 = new widget.Label();
        kode1 = new widget.TextBox();
        jLabel24 = new widget.Label();
        jLabel25 = new widget.Label();
        entitas1 = new widget.TextBox();
        jLabel26 = new widget.Label();
        sub_entitas1 = new widget.TextBox();
        kategori3 = new widget.ComboBox();
        Scroll1 = new widget.ScrollPane();
        tbSLKI = new widget.Table();
        internalFrame4 = new widget.InternalFrame();
        panelGlass11 = new widget.panelisi();
        jLabel11 = new widget.Label();
        jLabel12 = new widget.Label();
        kdrencanaSIKI = new widget.TextBox();
        jLabel13 = new widget.Label();
        kdmasalahSIKI = new widget.TextBox();
        nmmasalahSIKI = new widget.TextBox();
        btnPemeriksaan2 = new widget.Button();
        scrollPane3 = new widget.ScrollPane();
        rencanaKepSIKI = new widget.TextArea();
        jLabel16 = new widget.Label();
        Definisi2 = new widget.TextBox();
        kode2 = new widget.TextBox();
        jLabel28 = new widget.Label();
        jLabel29 = new widget.Label();
        entitas2 = new widget.TextBox();
        jLabel30 = new widget.Label();
        sub_entitas2 = new widget.TextBox();
        kategori4 = new widget.ComboBox();
        Scroll2 = new widget.ScrollPane();
        tbSIKI = new widget.Table();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowActivated(java.awt.event.WindowEvent evt) {
                formWindowActivated(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(51, 51, 51), 2, true), "::[ Data Master Rencana Keperawatan ]::", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

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
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 3, 9));

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(250, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('1');
        BtnCari.setToolTipText("Alt+1");
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
            public void keyReleased(java.awt.event.KeyEvent evt) {
                BtnCariKeyReleased(evt);
            }
        });
        panelGlass9.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('2');
        BtnAll.setToolTipText("Alt+2");
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

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(65, 23));
        panelGlass9.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(50, 23));
        panelGlass9.add(LCount);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        TabRawat.setBackground(new java.awt.Color(254, 255, 254));
        TabRawat.setForeground(new java.awt.Color(50, 50, 50));
        TabRawat.setFont(new java.awt.Font("Tahoma", 1, 10)); // NOI18N
        TabRawat.setName("TabRawat"); // NOI18N
        TabRawat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TabRawatMouseClicked(evt);
            }
        });

        internalFrame2.setBorder(null);
        internalFrame2.setName("internalFrame2"); // NOI18N
        internalFrame2.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass7.setName("panelGlass7"); // NOI18N
        panelGlass7.setPreferredSize(new java.awt.Dimension(44, 150));
        panelGlass7.setLayout(null);

        jLabel3.setText("Kode rencana :");
        jLabel3.setName("jLabel3"); // NOI18N
        panelGlass7.add(jLabel3);
        jLabel3.setBounds(0, 70, 90, 23);

        jLabel4.setText("Diagnosa  :");
        jLabel4.setName("jLabel4"); // NOI18N
        panelGlass7.add(jLabel4);
        jLabel4.setBounds(0, 100, 90, 23);

        kdrencana.setHighlighter(null);
        kdrencana.setName("kdrencana"); // NOI18N
        kdrencana.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdrencanaKeyPressed(evt);
            }
        });
        panelGlass7.add(kdrencana);
        kdrencana.setBounds(100, 70, 90, 23);

        jLabel5.setText("Definisi :");
        jLabel5.setName("jLabel5"); // NOI18N
        panelGlass7.add(jLabel5);
        jLabel5.setBounds(12, 40, 50, 23);

        kdmasalah.setEditable(false);
        kdmasalah.setHighlighter(null);
        kdmasalah.setName("kdmasalah"); // NOI18N
        kdmasalah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdmasalahKeyPressed(evt);
            }
        });
        panelGlass7.add(kdmasalah);
        kdmasalah.setBounds(70, 10, 70, 23);

        nmmasalah.setEditable(false);
        nmmasalah.setName("nmmasalah"); // NOI18N
        nmmasalah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                nmmasalahKeyPressed(evt);
            }
        });
        panelGlass7.add(nmmasalah);
        nmmasalah.setBounds(230, 10, 530, 23);

        btnPemeriksaan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPemeriksaan.setMnemonic('1');
        btnPemeriksaan.setToolTipText("Alt+1");
        btnPemeriksaan.setName("btnPemeriksaan"); // NOI18N
        btnPemeriksaan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPemeriksaanActionPerformed(evt);
            }
        });
        btnPemeriksaan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnPemeriksaanKeyPressed(evt);
            }
        });
        panelGlass7.add(btnPemeriksaan);
        btnPemeriksaan.setBounds(760, 10, 28, 23);

        scrollPane1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane1.setName("scrollPane1"); // NOI18N

        rencanaKep.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        rencanaKep.setColumns(20);
        rencanaKep.setRows(5);
        rencanaKep.setName("rencanaKep"); // NOI18N
        rencanaKep.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                rencanaKepKeyPressed(evt);
            }
        });
        scrollPane1.setViewportView(rencanaKep);

        panelGlass7.add(scrollPane1);
        scrollPane1.setBounds(100, 100, 680, 30);

        Definisi.setName("Definisi"); // NOI18N
        Definisi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DefinisiKeyPressed(evt);
            }
        });
        panelGlass7.add(Definisi);
        Definisi.setBounds(70, 40, 710, 23);

        jLabel14.setText("Masalah :");
        jLabel14.setName("jLabel14"); // NOI18N
        panelGlass7.add(jLabel14);
        jLabel14.setBounds(2, 10, 60, 23);

        kode.setHighlighter(null);
        kode.setName("kode"); // NOI18N
        kode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kodeKeyPressed(evt);
            }
        });
        panelGlass7.add(kode);
        kode.setBounds(140, 10, 90, 23);

        jLabel17.setText("Kategori :");
        jLabel17.setName("jLabel17"); // NOI18N
        panelGlass7.add(jLabel17);
        jLabel17.setBounds(190, 70, 60, 23);

        jLabel18.setText("Entitas :");
        jLabel18.setName("jLabel18"); // NOI18N
        panelGlass7.add(jLabel18);
        jLabel18.setBounds(360, 70, 50, 23);

        entitas.setHighlighter(null);
        entitas.setName("entitas"); // NOI18N
        entitas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                entitasKeyPressed(evt);
            }
        });
        panelGlass7.add(entitas);
        entitas.setBounds(410, 70, 140, 23);

        jLabel19.setText("Sub-Entitas :");
        jLabel19.setName("jLabel19"); // NOI18N
        panelGlass7.add(jLabel19);
        jLabel19.setBounds(550, 70, 80, 23);

        sub_entitas.setHighlighter(null);
        sub_entitas.setName("sub_entitas"); // NOI18N
        sub_entitas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sub_entitasKeyPressed(evt);
            }
        });
        panelGlass7.add(sub_entitas);
        sub_entitas.setBounds(630, 70, 150, 23);

        kategori.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SDKI", "SLKI", "SIKI" }));
        kategori.setName("kategori"); // NOI18N
        kategori.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kategoriKeyPressed(evt);
            }
        });
        panelGlass7.add(kategori);
        kategori.setBounds(250, 70, 95, 23);

        internalFrame2.add(panelGlass7, java.awt.BorderLayout.PAGE_START);

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbSDKI.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbSDKI.setName("tbSDKI"); // NOI18N
        tbSDKI.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbSDKIMouseClicked(evt);
            }
        });
        tbSDKI.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbSDKIKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbSDKIKeyReleased(evt);
            }
        });
        Scroll.setViewportView(tbSDKI);

        internalFrame2.add(Scroll, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("DIAGNOSA KEPERAWATAN (SDKI)", internalFrame2);

        internalFrame3.setBorder(null);
        internalFrame3.setName("internalFrame3"); // NOI18N
        internalFrame3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass10.setName("panelGlass10"); // NOI18N
        panelGlass10.setPreferredSize(new java.awt.Dimension(44, 150));
        panelGlass10.setLayout(null);

        jLabel9.setText("Tujuan Keperawatan :");
        jLabel9.setName("jLabel9"); // NOI18N
        panelGlass10.add(jLabel9);
        jLabel9.setBounds(-10, 100, 140, 23);

        kdrencanaSLKI.setHighlighter(null);
        kdrencanaSLKI.setName("kdrencanaSLKI"); // NOI18N
        kdrencanaSLKI.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdrencanaSLKIKeyPressed(evt);
            }
        });
        panelGlass10.add(kdrencanaSLKI);
        kdrencanaSLKI.setBounds(90, 70, 90, 23);

        jLabel10.setText("Masalah :");
        jLabel10.setName("jLabel10"); // NOI18N
        panelGlass10.add(jLabel10);
        jLabel10.setBounds(0, 10, 62, 23);

        kdmasalahSLKI.setEditable(false);
        kdmasalahSLKI.setHighlighter(null);
        kdmasalahSLKI.setName("kdmasalahSLKI"); // NOI18N
        kdmasalahSLKI.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdmasalahSLKIKeyPressed(evt);
            }
        });
        panelGlass10.add(kdmasalahSLKI);
        kdmasalahSLKI.setBounds(70, 10, 70, 23);

        nmmasalahSLKI.setEditable(false);
        nmmasalahSLKI.setName("nmmasalahSLKI"); // NOI18N
        nmmasalahSLKI.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                nmmasalahSLKIKeyPressed(evt);
            }
        });
        panelGlass10.add(nmmasalahSLKI);
        nmmasalahSLKI.setBounds(230, 10, 550, 23);

        btnPemeriksaan1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPemeriksaan1.setMnemonic('1');
        btnPemeriksaan1.setToolTipText("Alt+1");
        btnPemeriksaan1.setName("btnPemeriksaan1"); // NOI18N
        btnPemeriksaan1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPemeriksaan1ActionPerformed(evt);
            }
        });
        btnPemeriksaan1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnPemeriksaan1KeyPressed(evt);
            }
        });
        panelGlass10.add(btnPemeriksaan1);
        btnPemeriksaan1.setBounds(780, 10, 28, 23);

        scrollPane2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane2.setName("scrollPane2"); // NOI18N

        rencanaKepSLKI.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        rencanaKepSLKI.setColumns(20);
        rencanaKepSLKI.setRows(5);
        rencanaKepSLKI.setName("rencanaKepSLKI"); // NOI18N
        rencanaKepSLKI.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                rencanaKepSLKIKeyPressed(evt);
            }
        });
        scrollPane2.setViewportView(rencanaKepSLKI);

        panelGlass10.add(scrollPane2);
        scrollPane2.setBounds(140, 100, 670, 30);

        jLabel15.setText("Definisi :");
        jLabel15.setName("jLabel15"); // NOI18N
        panelGlass10.add(jLabel15);
        jLabel15.setBounds(12, 40, 50, 23);

        Definisi1.setName("Definisi1"); // NOI18N
        Definisi1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Definisi1KeyPressed(evt);
            }
        });
        panelGlass10.add(Definisi1);
        Definisi1.setBounds(70, 40, 740, 23);

        jLabel20.setText("Kode rencana :");
        jLabel20.setName("jLabel20"); // NOI18N
        panelGlass10.add(jLabel20);
        jLabel20.setBounds(0, 70, 90, 23);

        kode1.setHighlighter(null);
        kode1.setName("kode1"); // NOI18N
        kode1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kode1KeyPressed(evt);
            }
        });
        panelGlass10.add(kode1);
        kode1.setBounds(140, 10, 90, 23);

        jLabel24.setText("Kategori :");
        jLabel24.setName("jLabel24"); // NOI18N
        panelGlass10.add(jLabel24);
        jLabel24.setBounds(190, 70, 60, 23);

        jLabel25.setText("Entitas :");
        jLabel25.setName("jLabel25"); // NOI18N
        panelGlass10.add(jLabel25);
        jLabel25.setBounds(360, 70, 50, 23);

        entitas1.setHighlighter(null);
        entitas1.setName("entitas1"); // NOI18N
        entitas1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                entitas1KeyPressed(evt);
            }
        });
        panelGlass10.add(entitas1);
        entitas1.setBounds(410, 70, 170, 23);

        jLabel26.setText("Sub-Entitas :");
        jLabel26.setName("jLabel26"); // NOI18N
        panelGlass10.add(jLabel26);
        jLabel26.setBounds(580, 70, 80, 23);

        sub_entitas1.setHighlighter(null);
        sub_entitas1.setName("sub_entitas1"); // NOI18N
        sub_entitas1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sub_entitas1KeyPressed(evt);
            }
        });
        panelGlass10.add(sub_entitas1);
        sub_entitas1.setBounds(660, 70, 150, 23);

        kategori3.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SDKI", "SLKI", "SIKI" }));
        kategori3.setName("kategori3"); // NOI18N
        kategori3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kategori3KeyPressed(evt);
            }
        });
        panelGlass10.add(kategori3);
        kategori3.setBounds(250, 70, 95, 23);

        internalFrame3.add(panelGlass10, java.awt.BorderLayout.PAGE_START);

        Scroll1.setName("Scroll1"); // NOI18N
        Scroll1.setOpaque(true);

        tbSLKI.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbSLKI.setName("tbSLKI"); // NOI18N
        tbSLKI.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbSLKIMouseClicked(evt);
            }
        });
        tbSLKI.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbSLKIKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbSLKIKeyReleased(evt);
            }
        });
        Scroll1.setViewportView(tbSLKI);

        internalFrame3.add(Scroll1, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("TUJUAN KEPERAWATAN (SLKI)", internalFrame3);

        internalFrame4.setBorder(null);
        internalFrame4.setName("internalFrame4"); // NOI18N
        internalFrame4.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass11.setName("panelGlass11"); // NOI18N
        panelGlass11.setPreferredSize(new java.awt.Dimension(44, 150));
        panelGlass11.setLayout(null);

        jLabel11.setText("Kode  :");
        jLabel11.setName("jLabel11"); // NOI18N
        panelGlass11.add(jLabel11);
        jLabel11.setBounds(0, 70, 62, 23);

        jLabel12.setText("Rencana Tindakan : ");
        jLabel12.setName("jLabel12"); // NOI18N
        panelGlass11.add(jLabel12);
        jLabel12.setBounds(10, 100, 120, 23);

        kdrencanaSIKI.setHighlighter(null);
        kdrencanaSIKI.setName("kdrencanaSIKI"); // NOI18N
        kdrencanaSIKI.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdrencanaSIKIKeyPressed(evt);
            }
        });
        panelGlass11.add(kdrencanaSIKI);
        kdrencanaSIKI.setBounds(70, 70, 80, 23);

        jLabel13.setText("Masalah :");
        jLabel13.setName("jLabel13"); // NOI18N
        panelGlass11.add(jLabel13);
        jLabel13.setBounds(0, 10, 62, 23);

        kdmasalahSIKI.setEditable(false);
        kdmasalahSIKI.setHighlighter(null);
        kdmasalahSIKI.setName("kdmasalahSIKI"); // NOI18N
        kdmasalahSIKI.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kdmasalahSIKIKeyPressed(evt);
            }
        });
        panelGlass11.add(kdmasalahSIKI);
        kdmasalahSIKI.setBounds(66, 10, 70, 23);

        nmmasalahSIKI.setEditable(false);
        nmmasalahSIKI.setName("nmmasalahSIKI"); // NOI18N
        nmmasalahSIKI.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                nmmasalahSIKIKeyPressed(evt);
            }
        });
        panelGlass11.add(nmmasalahSIKI);
        nmmasalahSIKI.setBounds(235, 10, 540, 23);

        btnPemeriksaan2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnPemeriksaan2.setMnemonic('1');
        btnPemeriksaan2.setToolTipText("Alt+1");
        btnPemeriksaan2.setName("btnPemeriksaan2"); // NOI18N
        btnPemeriksaan2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPemeriksaan2ActionPerformed(evt);
            }
        });
        btnPemeriksaan2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnPemeriksaan2KeyPressed(evt);
            }
        });
        panelGlass11.add(btnPemeriksaan2);
        btnPemeriksaan2.setBounds(780, 10, 28, 23);

        scrollPane3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPane3.setName("scrollPane3"); // NOI18N

        rencanaKepSIKI.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        rencanaKepSIKI.setColumns(20);
        rencanaKepSIKI.setRows(5);
        rencanaKepSIKI.setName("rencanaKepSIKI"); // NOI18N
        rencanaKepSIKI.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                rencanaKepSIKIKeyPressed(evt);
            }
        });
        scrollPane3.setViewportView(rencanaKepSIKI);

        panelGlass11.add(scrollPane3);
        scrollPane3.setBounds(130, 100, 680, 30);

        jLabel16.setText("Definisi :");
        jLabel16.setName("jLabel16"); // NOI18N
        panelGlass11.add(jLabel16);
        jLabel16.setBounds(2, 40, 60, 23);

        Definisi2.setName("Definisi2"); // NOI18N
        Definisi2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Definisi2KeyPressed(evt);
            }
        });
        panelGlass11.add(Definisi2);
        Definisi2.setBounds(70, 40, 740, 23);

        kode2.setHighlighter(null);
        kode2.setName("kode2"); // NOI18N
        kode2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kode2KeyPressed(evt);
            }
        });
        panelGlass11.add(kode2);
        kode2.setBounds(140, 10, 90, 23);

        jLabel28.setText("Kategori :");
        jLabel28.setName("jLabel28"); // NOI18N
        panelGlass11.add(jLabel28);
        jLabel28.setBounds(150, 70, 60, 23);

        jLabel29.setText("Entitas :");
        jLabel29.setName("jLabel29"); // NOI18N
        panelGlass11.add(jLabel29);
        jLabel29.setBounds(320, 70, 50, 23);

        entitas2.setHighlighter(null);
        entitas2.setName("entitas2"); // NOI18N
        entitas2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                entitas2KeyPressed(evt);
            }
        });
        panelGlass11.add(entitas2);
        entitas2.setBounds(370, 70, 190, 23);

        jLabel30.setText("Sub-Entitas :");
        jLabel30.setName("jLabel30"); // NOI18N
        panelGlass11.add(jLabel30);
        jLabel30.setBounds(560, 70, 80, 23);

        sub_entitas2.setHighlighter(null);
        sub_entitas2.setName("sub_entitas2"); // NOI18N
        sub_entitas2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                sub_entitas2KeyPressed(evt);
            }
        });
        panelGlass11.add(sub_entitas2);
        sub_entitas2.setBounds(640, 70, 170, 23);

        kategori4.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SDKI", "SLKI", "SIKI" }));
        kategori4.setName("kategori4"); // NOI18N
        kategori4.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                kategori4KeyPressed(evt);
            }
        });
        panelGlass11.add(kategori4);
        kategori4.setBounds(220, 70, 95, 23);

        internalFrame4.add(panelGlass11, java.awt.BorderLayout.PAGE_START);

        Scroll2.setName("Scroll2"); // NOI18N
        Scroll2.setOpaque(true);

        tbSIKI.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbSIKI.setName("tbSIKI"); // NOI18N
        tbSIKI.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbSIKIMouseClicked(evt);
            }
        });
        tbSIKI.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbSIKIKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbSIKIKeyReleased(evt);
            }
        });
        Scroll2.setViewportView(tbSIKI);

        internalFrame4.add(Scroll2, java.awt.BorderLayout.CENTER);

        TabRawat.addTab("RENCANA TINDAKAN (SIKI)", internalFrame4);

        internalFrame1.add(TabRawat, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void kdrencanaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdrencanaKeyPressed
        Valid.pindah(evt, TCari, rencanaKep);
}//GEN-LAST:event_kdrencanaKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        try {
            if (TabRawat.getSelectedIndex() == 0) {
                // ===== TAB 0: SDKI =====
                if (kdrencana.getText().trim().equals("")) {
                    Valid.textKosong(kdrencana, "Kode Rencana");
                    return;
                } else if (rencanaKep.getText().trim().equals("")) {
                    Valid.textKosong(rencanaKep, "Rencana Keperawatan");
                    return;
                }

                System.out.println("=== DEBUG TAB 0 (SDKI) ===");
                System.out.println("Kode Masalah : " + kdmasalah.getText());
                System.out.println("Kode : " + kode.getText());
                System.out.println("Kode Rencana : " + kdrencana.getText());
                System.out.println("Kategori : " + kategori.getSelectedItem().toString());

                // 1. Cek duplikasi (7 kolom)
                int adaData = Sequel.cariInteger(
                        "SELECT COUNT(*) FROM master_diagnosa_keperawatan WHERE "
                        + "TRIM(kode_masalah)=TRIM('" + kdmasalah.getText() + "') AND "
                        + "TRIM(kode)=TRIM('" + kode.getText() + "') AND "
                        + "TRIM(kode_rencana)=TRIM('" + kdrencana.getText() + "') AND "
                        + "TRIM(kategori)=TRIM('" + kategori.getSelectedItem().toString() + "') AND "
                        + "TRIM(entitas)=TRIM('" + entitas.getText() + "') AND "
                        + "TRIM(sub_entitas)=TRIM('" + sub_entitas.getText() + "') AND "
                        + "TRIM(item)=TRIM('" + rencanaKep.getText() + "')"
                );

                if (adaData > 0) {
                    JOptionPane.showMessageDialog(rootPane, "Data sudah ada!");
                    return;
                }

                // 2. Cek master masalah
                int adaMasalah = Sequel.cariInteger(
                        "SELECT COUNT(*) FROM master_masalah_keperawatan WHERE "
                        + "TRIM(kode_masalah)=TRIM('" + kdmasalah.getText() + "')"
                );

                if (adaMasalah > 0) {
                    Sequel.mengedit("master_masalah_keperawatan",
                            "kode_masalah=?",
                            "nama_masalah=?, definisi=?",
                            3,
                            new String[]{
                                nmmasalah.getText(),
                                Definisi.getText(),
                                kdmasalah.getText()
                            }
                    );
                } else {
                    String insertMasalah = "'" + kdmasalah.getText() + "','"
                            + nmmasalah.getText() + "','"
                            + Definisi.getText() + "'";
                    Sequel.menyimpan("master_masalah_keperawatan", insertMasalah, "Master Masalah Keperawatan");
                }

                String insertSDKI = "'" + kdmasalah.getText() + "','"
                        + kode.getText() + "','"
                        + nmmasalah.getText() + "','"
                        + kdrencana.getText() + "','"
                        + kategori.getSelectedItem().toString() + "','"
                        + entitas.getText() + "','"
                        + sub_entitas.getText() + "','"
                        + rencanaKep.getText() + "'";
                Sequel.menyimpan("master_diagnosa_keperawatan", insertSDKI, "Diagnosa Keperawatan");

                tampil();
                emptTeks();
                JOptionPane.showMessageDialog(rootPane, "Berhasil Simpan Data");

            } else if (TabRawat.getSelectedIndex() == 1) {
                if (kdrencanaSLKI.getText().trim().equals("")) {
                    Valid.textKosong(kdrencanaSLKI, "Kode Rencana");
                    return;
                } else if (rencanaKepSLKI.getText().trim().equals("")) {
                    Valid.textKosong(rencanaKepSLKI, "Rencana Keperawatan");
                    return;
                }

                System.out.println("=== DEBUG TAB 1 (SLKI) ===");

                int adaData = Sequel.cariInteger(
                        "SELECT COUNT(*) FROM master_diagnosa_keperawatan WHERE "
                        + "TRIM(kode_masalah)=TRIM('" + kdmasalahSLKI.getText() + "') AND "
                        + "TRIM(kode)=TRIM('" + kode1.getText() + "') AND "
                        + "TRIM(kode_rencana)=TRIM('" + kdrencanaSLKI.getText() + "') AND "
                        + "TRIM(kategori)=TRIM('" + kategori3.getSelectedItem().toString() + "') AND "
                        + "TRIM(entitas)=TRIM('" + entitas1.getText() + "') AND "
                        + "TRIM(sub_entitas)=TRIM('" + sub_entitas1.getText() + "') AND "
                        + "TRIM(item)=TRIM('" + rencanaKepSLKI.getText() + "')"
                );

                if (adaData > 0) {
                    JOptionPane.showMessageDialog(rootPane, "Data sudah ada!");
                    return;
                }

                int adaMasalah = Sequel.cariInteger(
                        "SELECT COUNT(*) FROM master_masalah_keperawatan WHERE "
                        + "TRIM(kode_masalah)=TRIM('" + kdmasalahSLKI.getText() + "')"
                );

                if (adaMasalah > 0) {
                    Sequel.mengedit("master_masalah_keperawatan",
                            "kode_masalah=?",
                            "nama_masalah=?, definisi=?",
                            3,
                            new String[]{
                                nmmasalahSLKI.getText(),
                                Definisi1.getText(),
                                kdmasalahSLKI.getText()
                            }
                    );
                } else {
                    String insertMasalah = "'" + kdmasalahSLKI.getText() + "','"
                            + nmmasalahSLKI.getText() + "','"
                            + Definisi1.getText() + "'";
                    Sequel.menyimpan("master_masalah_keperawatan", insertMasalah, "Master Masalah Keperawatan");
                }

                String insertSLKI = "'" + kdmasalahSLKI.getText() + "','"
                        + kode1.getText() + "','"
                        + nmmasalahSLKI.getText() + "','"
                        + kdrencanaSLKI.getText() + "','"
                        + kategori3.getSelectedItem().toString() + "','"
                        + entitas1.getText() + "','"
                        + sub_entitas1.getText() + "','"
                        + rencanaKepSLKI.getText() + "'";
                Sequel.menyimpan("master_diagnosa_keperawatan", insertSLKI, "Diagnosa Keperawatan");

                tampilSLKI();
                emptTeks();
                JOptionPane.showMessageDialog(rootPane, "Berhasil Simpan Data");

            } else if (TabRawat.getSelectedIndex() == 2) {
                // ===== TAB 2: SIKI =====
                if (kdrencanaSIKI.getText().trim().equals("")) {
                    Valid.textKosong(kdrencanaSIKI, "Kode Rencana");
                    return;
                } else if (rencanaKepSIKI.getText().trim().equals("")) {
                    Valid.textKosong(rencanaKepSIKI, "Rencana Keperawatan");
                    return;
                }

                System.out.println("=== DEBUG TAB 2 (SIKI) ===");

                int adaData = Sequel.cariInteger(
                        "SELECT COUNT(*) FROM master_diagnosa_keperawatan WHERE "
                        + "TRIM(kode_masalah)=TRIM('" + kdmasalahSIKI.getText() + "') AND "
                        + "TRIM(kode)=TRIM('" + kode2.getText() + "') AND "
                        + "TRIM(kode_rencana)=TRIM('" + kdrencanaSIKI.getText() + "') AND "
                        + "TRIM(kategori)=TRIM('" + kategori4.getSelectedItem().toString() + "') AND "
                        + "TRIM(entitas)=TRIM('" + entitas2.getText() + "') AND "
                        + "TRIM(sub_entitas)=TRIM('" + sub_entitas2.getText() + "') AND "
                        + "TRIM(item)=TRIM('" + rencanaKepSIKI.getText() + "')"
                );

                if (adaData > 0) {
                    JOptionPane.showMessageDialog(rootPane, "Data sudah ada!");
                    return;
                }

                int adaMasalah = Sequel.cariInteger(
                        "SELECT COUNT(*) FROM master_masalah_keperawatan WHERE "
                        + "TRIM(kode_masalah)=TRIM('" + kdmasalahSIKI.getText() + "')"
                );

                if (adaMasalah > 0) {
                    Sequel.mengedit("master_masalah_keperawatan",
                            "kode_masalah=?",
                            "nama_masalah=?, definisi=?",
                            3,
                            new String[]{
                                nmmasalahSIKI.getText(),
                                Definisi2.getText(),
                                kdmasalahSIKI.getText()
                            }
                    );
                } else {
                    String insertMasalah = "'" + kdmasalahSIKI.getText() + "','"
                            + nmmasalahSIKI.getText() + "','"
                            + Definisi2.getText() + "'";
                    Sequel.menyimpan("master_masalah_keperawatan", insertMasalah, "Master Masalah Keperawatan");
                }

                String insertSIKI = "'" + kdmasalahSIKI.getText() + "','"
                        + kode2.getText() + "','"
                        + nmmasalahSIKI.getText() + "','"
                        + kdrencanaSIKI.getText() + "','"
                        + kategori4.getSelectedItem().toString() + "','"
                        + entitas2.getText() + "','"
                        + sub_entitas2.getText() + "','"
                        + rencanaKepSIKI.getText() + "'";
                Sequel.menyimpan("master_diagnosa_keperawatan", insertSIKI, "Diagnosa Keperawatan");

                tampilSIKI();
                emptTeks();
                JOptionPane.showMessageDialog(rootPane, "Berhasil Simpan Data");
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(rootPane, "Terjadi kesalahan saat menyimpan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnSimpanActionPerformed(null);
        } else {
            Valid.pindah(evt, rencanaKep, BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
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
        if (TabRawat.getSelectedIndex() == 0) {
            if (tbSDKI.getSelectedRow() > -1) {
                int jawab = JOptionPane.showConfirmDialog(null,
                        "Yakin data SDKI ini mau dihapus ?",
                        "Konfirmasi", JOptionPane.YES_NO_OPTION);

                if (jawab == JOptionPane.YES_OPTION) {
                    Sequel.queryu2tf(
                            "DELETE FROM master_diagnosa_keperawatan "
                            + "WHERE kode_masalah=? AND kode=? AND kode_rencana=? "
                            + "AND kategori=? AND entitas=? AND sub_entitas=? AND item=?",
                            7, new String[]{
                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 0).toString(), // kode_masalah
                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 1).toString(), // kode
                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 3).toString(), // kode_rencana
                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 4).toString(), // kategori
                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 5).toString(), // entitas
                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 6).toString(), // sub_entitas
                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 2).toString() // item
                            }
                    );
                    JOptionPane.showMessageDialog(null, "Data SDKI berhasil dihapus");
                    tampil();
                    emptTeks();
                }
            }
        } else if (TabRawat.getSelectedIndex() == 1) {
            if (tbSLKI.getSelectedRow() > -1) {
                int jawab = JOptionPane.showConfirmDialog(null,
                        "Yakin data SLKI ini mau dihapus ?",
                        "Konfirmasi", JOptionPane.YES_NO_OPTION);

                if (jawab == JOptionPane.YES_OPTION) {
                    Sequel.queryu2tf(
                            "DELETE FROM master_diagnosa_keperawatan "
                            + "WHERE kode_masalah=? AND kode=? AND kode_rencana=? "
                            + "AND kategori=? AND entitas=? AND sub_entitas=? AND item=?",
                            7, new String[]{
                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 0).toString(),
                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 1).toString(),
                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 3).toString(),
                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 4).toString(),
                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 5).toString(),
                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 6).toString(),
                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 2).toString()
                            }
                    );
                    JOptionPane.showMessageDialog(null, "Data SLKI berhasil dihapus");
                    tampilSLKI();
                    emptTeks();
                }
            }
        } else if (TabRawat.getSelectedIndex() == 2) {
            if (tbSIKI.getSelectedRow() > -1) {
                int jawab = JOptionPane.showConfirmDialog(null,
                        "Yakin data SIKI ini mau dihapus ?",
                        "Konfirmasi", JOptionPane.YES_NO_OPTION);

                if (jawab == JOptionPane.YES_OPTION) {
                    Sequel.queryu2tf(
                            "DELETE FROM master_diagnosa_keperawatan "
                            + "WHERE kode_masalah=? AND kode=? AND kode_rencana=? "
                            + "AND kategori=? AND entitas=? AND sub_entitas=? AND item=?",
                            7, new String[]{
                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 0).toString(),
                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 1).toString(),
                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 3).toString(),
                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 4).toString(),
                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 5).toString(),
                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 6).toString(),
                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 2).toString()
                            }
                    );
                    JOptionPane.showMessageDialog(null, "Data SIKI berhasil dihapus");
                    tampilSIKI();
                    emptTeks();
                }
            }
        }
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnHapusActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnBatal, BtnEdit);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
//        if (TabRawat.getSelectedIndex() == 0) {
//            if (tbSDKI.getSelectedRow() > -1) {
//                if (kdrencana.getText().trim().equals("")) {
//                    Valid.textKosong(kdrencana, "Kode");
//                } else if (rencanaKep.getText().trim().equals("")) {
//                    Valid.textKosong(rencanaKep, "Pengkajian");
//                } else {
//                    Sequel.mengedit("master_diagnosa_keperawatan",
//                            "kode_masalah=? and kode_rencana=? and kategori=?",
//                            "kode_masalah=?, kode=?, masalah_keperawatan=?, kode_rencana=?, kategori=?, entitas=?, sub_entitas=?, item=?",
//                            11, new String[]{
//                                kdmasalah.getText(),
//                                kode.getText(),
//                                nmmasalah.getText(),
//                                kdrencana.getText(),
//                                kategori.getSelectedItem().toString(),
//                                entitas.getText(),
//                                sub_entitas.getText(),
//                                rencanaKep.getText(),
//                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 0).toString(), // kode_masalah lama
//                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 3).toString(), // kode_rencana lama
//                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 6).toString() // kategori lama
//                            });
//                    JOptionPane.showMessageDialog(rootPane, "Data berhasil diedit");
//                    tampil();
//                    emptTeks();
//                }
//            }
//        } else if (TabRawat.getSelectedIndex() == 1) {
//            if (tbSLKI.getSelectedRow() > -1) {
//                if (kdrencanaSLKI.getText().trim().equals("")) {
//                    Valid.textKosong(kdrencanaSLKI, "Kode");
//                } else if (rencanaKepSLKI.getText().trim().equals("")) {
//                    Valid.textKosong(rencanaKepSLKI, "Pengkajian");
//                } else {
//                    Sequel.mengedit("master_diagnosa_keperawatan",
//                            "kode_masalah=? and kode_rencana=? and kategori=?",
//                            "kode_masalah=?, kode=?, masalah_keperawatan=?, kode_rencana=?, kategori=?, entitas=?, sub_entitas=?, item=?",
//                            11, new String[]{
//                                kdmasalah.getText(),
//                                kode.getText(),
//                                nmmasalah.getText(),
//                                kdrencanaSLKI.getText(),
//                                kategori.getSelectedItem().toString(),
//                                entitas.getText(),
//                                sub_entitas.getText(),
//                                rencanaKepSLKI.getText(),
//                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 0).toString(), // kode_masalah lama
//                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 3).toString(), // kode_rencana lama
//                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 6).toString() // kategori lama
//                            });
//                    JOptionPane.showMessageDialog(rootPane, "Data berhasil diedit");
//                    tampilSLKI();
//                    emptTeks();
//                }
//            }
//        } else if (TabRawat.getSelectedIndex() == 2) {
//            if (tbSIKI.getSelectedRow() > -1) {
//                if (kdrencanaSIKI.getText().trim().equals("")) {
//                    Valid.textKosong(kdrencanaSIKI, "Kode");
//                } else if (rencanaKepSIKI.getText().trim().equals("")) {
//                    Valid.textKosong(rencanaKepSIKI, "Pengkajian");
//                } else {
//                    Sequel.mengedit("master_diagnosa_keperawatan",
//                            "kode_masalah=? and kode_rencana=? and kategori=?",
//                            "kode_masalah=?, kode=?, masalah_keperawatan=?, kode_rencana=?, kategori=?, entitas=?, sub_entitas=?, item=?",
//                            11, new String[]{
//                                kdmasalah.getText(),
//                                kode.getText(),
//                                nmmasalah.getText(),
//                                kdrencanaSIKI.getText(),
//                                kategori.getSelectedItem().toString(),
//                                entitas.getText(),
//                                sub_entitas.getText(),
//                                rencanaKepSIKI.getText(),
//                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 0).toString(), // kode_masalah lama
//                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 3).toString(), // kode_rencana lama
//                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 6).toString() // kategori lama
//                            });
//                    JOptionPane.showMessageDialog(rootPane, "Data berhasil diedit");
//                    tampilSIKI();
//                    emptTeks();
//                }
//            }
//        }
        if (TabRawat.getSelectedIndex() == 0) {
            if (tbSDKI.getSelectedRow() > -1) {
                if (kdrencana.getText().trim().equals("")) {
                    Valid.textKosong(kdrencana, "Kode Rencana");
                } else if (rencanaKep.getText().trim().equals("")) {
                    Valid.textKosong(rencanaKep, "Diagnosa/Item");
                } else {
                    if (Sequel.cariInteger(
                            "SELECT COUNT(*) FROM master_diagnosa_keperawatan WHERE "
                            + "TRIM(kode_masalah)=TRIM('" + kdmasalah.getText() + "') AND "
                            + "TRIM(kode)=TRIM('" + kode.getText() + "') AND "
                            + "TRIM(kode_rencana)=TRIM('" + kdrencana.getText() + "') AND "
                            + "TRIM(kategori)=TRIM('" + kategori.getSelectedItem().toString() + "') AND "
                            + "TRIM(entitas)=TRIM('" + entitas.getText() + "') AND "
                            + "TRIM(sub_entitas)=TRIM('" + sub_entitas.getText() + "') AND "
                            + "TRIM(item)=TRIM('" + rencanaKep.getText() + "')"
                    ) > 0) {
                        JOptionPane.showMessageDialog(rootPane, "Data sudah ada, tidak dapat disimpan duplikat.");
                        return;
                    }

                    Sequel.mengedit(
                            "master_diagnosa_keperawatan",
                            "kode_masalah=? AND kode=? AND kode_rencana=? AND kategori=? "
                            + "AND entitas=? AND sub_entitas=? AND item=?",
                            "kode_masalah=?, kode=?, item=?, kode_rencana=?, kategori=?, entitas=?, sub_entitas=?",
                            14, new String[]{
                                // 7 baru (SET)
                                kdmasalah.getText(), kode.getText(), rencanaKep.getText(),
                                kdrencana.getText(), kategori.getSelectedItem().toString(),
                                entitas.getText(), sub_entitas.getText(),
                                // 7 lama (WHERE)
                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 0).toString(), // kode_masalah_lama
                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 1).toString(), // kode_lama
                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 3).toString(), // kode_rencana_lama
                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 6).toString(), // kategori_lama
                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 4).toString(), // entitas_lama
                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 5).toString(), // sub_entitas_lama
                                tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 2).toString() // item_lama
                            }
                    );

                    JOptionPane.showMessageDialog(rootPane, "Data berhasil diedit");
                    tampil();
                    emptTeks();
                }
            }
        } else if (TabRawat.getSelectedIndex() == 1) {
            if (tbSLKI.getSelectedRow() > -1) {
                if (kdrencanaSLKI.getText().trim().equals("")) {
                    Valid.textKosong(kdrencanaSLKI, "Kode Rencana");
                } else if (rencanaKepSLKI.getText().trim().equals("")) {
                    Valid.textKosong(rencanaKepSLKI, "Item");
                } else {

                    // CEK DUPLIKAT (7 kolom)
                    if (Sequel.cariInteger(
                            "SELECT COUNT(*) FROM master_diagnosa_keperawatan WHERE "
                            + "TRIM(kode_masalah)=TRIM('" + kdmasalahSLKI.getText() + "') AND "
                            + "TRIM(kode)=TRIM('" + kode1.getText() + "') AND "
                            + "TRIM(kode_rencana)=TRIM('" + kdrencanaSLKI.getText() + "') AND "
                            + "TRIM(kategori)=TRIM('" + kategori3.getSelectedItem().toString() + "') AND "
                            + "TRIM(entitas)=TRIM('" + entitas1.getText() + "') AND "
                            + "TRIM(sub_entitas)=TRIM('" + sub_entitas1.getText() + "') AND "
                            + "TRIM(item)=TRIM('" + rencanaKepSLKI.getText() + "')"
                    ) > 0) {
                        JOptionPane.showMessageDialog(rootPane, "Data sudah ada, tidak dapat disimpan duplikat.");
                        return;
                    }

                    // UPDATE (7 SET, 7 WHERE)
                    Sequel.mengedit(
                            "master_diagnosa_keperawatan",
                            "kode_masalah=? AND kode=? AND kode_rencana=? AND kategori=? "
                            + "AND entitas=? AND sub_entitas=? AND item=?",
                            "kode_masalah=?, kode=?, item=?, kode_rencana=?, kategori=?, entitas=?, sub_entitas=?",
                            14, new String[]{
                                // SET
                                kdmasalahSLKI.getText(), kode1.getText(), rencanaKepSLKI.getText(),
                                kdrencanaSLKI.getText(), kategori3.getSelectedItem().toString(),
                                entitas1.getText(), sub_entitas1.getText(),
                                // WHERE (lama)
                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 0).toString(),
                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 1).toString(),
                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 3).toString(),
                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 6).toString(),
                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 4).toString(),
                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 5).toString(),
                                tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 2).toString()
                            }
                    );

                    JOptionPane.showMessageDialog(rootPane, "Data berhasil diedit");
                    tampilSLKI();
                    emptTeks();
                }
            }
        } // ======================== TAB 2 : SIKI ========================
        else if (TabRawat.getSelectedIndex() == 2) {
            if (tbSIKI.getSelectedRow() > -1) {
                if (kdrencanaSIKI.getText().trim().equals("")) {
                    Valid.textKosong(kdrencanaSIKI, "Kode Rencana");
                } else if (rencanaKepSIKI.getText().trim().equals("")) {
                    Valid.textKosong(rencanaKepSIKI, "Item");
                } else {

                    // CEK DUPLIKAT (7 kolom)
                    if (Sequel.cariInteger(
                            "SELECT COUNT(*) FROM master_diagnosa_keperawatan WHERE "
                            + "TRIM(kode_masalah)=TRIM('" + kdmasalahSIKI.getText() + "') AND "
                            + "TRIM(kode)=TRIM('" + kode2.getText() + "') AND "
                            + "TRIM(kode_rencana)=TRIM('" + kdrencanaSIKI.getText() + "') AND "
                            + "TRIM(kategori)=TRIM('" + kategori4.getSelectedItem().toString() + "') AND "
                            + "TRIM(entitas)=TRIM('" + entitas2.getText() + "') AND "
                            + "TRIM(sub_entitas)=TRIM('" + sub_entitas2.getText() + "') AND "
                            + "TRIM(item)=TRIM('" + rencanaKepSIKI.getText() + "')"
                    ) > 0) {
                        JOptionPane.showMessageDialog(rootPane, "Data sudah ada, tidak dapat disimpan duplikat.");
                        return;
                    }

                    Sequel.mengedit(
                            "master_diagnosa_keperawatan",
                            "kode_masalah=? AND kode=? AND kode_rencana=? AND kategori=? "
                            + "AND entitas=? AND sub_entitas=? AND item=?",
                            "kode_masalah=?, kode=?, item=?, kode_rencana=?, kategori=?, entitas=?, sub_entitas=?",
                            14, new String[]{
                                kdmasalahSIKI.getText(), kode2.getText(), rencanaKepSIKI.getText(),
                                kdrencanaSIKI.getText(), kategori4.getSelectedItem().toString(),
                                entitas2.getText(), sub_entitas2.getText(),
                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 0).toString(),
                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 1).toString(),
                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 3).toString(),
                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 6).toString(),
                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 4).toString(),
                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 5).toString(),
                                tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 2).toString()
                            }
                    );

                    JOptionPane.showMessageDialog(rootPane, "Data berhasil diedit");
                    tampilSIKI();
                    emptTeks();
                }
            }
        }
}//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnEditActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnHapus, BtnKeluar);
        }
}//GEN-LAST:event_BtnEditKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            dispose();
        } else {
            Valid.pindah(evt, BtnEdit, TCari);
        }
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            BtnCariActionPerformed(null);
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            BtnCari.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            BtnKeluar.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            tbSDKI.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        tampil();
        tampilSIKI();
        tampilSLKI();
}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnCariActionPerformed(null);
        } else {
            Valid.pindah(evt, TCari, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void BtnCariKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyReleased
        // TODO add your handling code here:
}//GEN-LAST:event_BtnCariKeyReleased

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        emptTeks();
        tampil();
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnAllActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnCari, kdrencana);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbSDKIMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbSDKIMouseClicked
        if (tabMode.getRowCount() != 0) {
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbSDKIMouseClicked

    private void tbSDKIKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbSDKIKeyPressed
        if (tabMode.getRowCount() != 0) {
            if (evt.getKeyCode() == KeyEvent.VK_SHIFT) {
                TCari.setText("");
                TCari.requestFocus();
            }
        }
}//GEN-LAST:event_tbSDKIKeyPressed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        TCari.requestFocus();
    }//GEN-LAST:event_formWindowActivated

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        tampil();
        emptTeks();
    }//GEN-LAST:event_formWindowOpened

    private void tbSDKIKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbSDKIKeyReleased
        if (tabMode.getRowCount() != 0) {
            if ((evt.getKeyCode() == KeyEvent.VK_ENTER) || (evt.getKeyCode() == KeyEvent.VK_UP) || (evt.getKeyCode() == KeyEvent.VK_DOWN)) {
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbSDKIKeyReleased

    private void kdmasalahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdmasalahKeyPressed
        Valid.pindah(evt, TCari, kdrencana);
    }//GEN-LAST:event_kdmasalahKeyPressed

    private void nmmasalahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nmmasalahKeyPressed
        Valid.pindah(evt, kdrencana, BtnSimpan);
    }//GEN-LAST:event_nmmasalahKeyPressed

    private void btnPemeriksaanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPemeriksaanActionPerformed
        diagnosa.isCek();
        diagnosa.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        diagnosa.setLocationRelativeTo(internalFrame1);
        diagnosa.setVisible(true);
    }//GEN-LAST:event_btnPemeriksaanActionPerformed

    private void btnPemeriksaanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPemeriksaanKeyPressed
        Valid.pindah(evt, kdmasalah, BtnSimpan);
    }//GEN-LAST:event_btnPemeriksaanKeyPressed

    private void rencanaKepKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rencanaKepKeyPressed
        Valid.pindah2(evt, kdrencana, BtnSimpan);
    }//GEN-LAST:event_rencanaKepKeyPressed

    private void TabRawatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_TabRawatMouseClicked
        if (TabRawat.getSelectedIndex() == 0) {
            tampil();
        }
        if (TabRawat.getSelectedIndex() == 1) {
            tampilSLKI();
        }
        if (TabRawat.getSelectedIndex() == 2) {
            tampilSIKI();
        }
    }//GEN-LAST:event_TabRawatMouseClicked

    private void kdrencanaSLKIKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdrencanaSLKIKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kdrencanaSLKIKeyPressed

    private void kdmasalahSLKIKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdmasalahSLKIKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kdmasalahSLKIKeyPressed

    private void nmmasalahSLKIKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nmmasalahSLKIKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_nmmasalahSLKIKeyPressed

    private void btnPemeriksaan1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPemeriksaan1ActionPerformed
        diagnosa.isCek();
        diagnosa.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        diagnosa.setLocationRelativeTo(internalFrame1);
        diagnosa.setVisible(true);
    }//GEN-LAST:event_btnPemeriksaan1ActionPerformed

    private void btnPemeriksaan1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPemeriksaan1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnPemeriksaan1KeyPressed

    private void rencanaKepSLKIKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rencanaKepSLKIKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_rencanaKepSLKIKeyPressed

    private void kdrencanaSIKIKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdrencanaSIKIKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kdrencanaSIKIKeyPressed

    private void kdmasalahSIKIKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kdmasalahSIKIKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kdmasalahSIKIKeyPressed

    private void nmmasalahSIKIKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_nmmasalahSIKIKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_nmmasalahSIKIKeyPressed

    private void btnPemeriksaan2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPemeriksaan2ActionPerformed
        diagnosa.isCek();
        diagnosa.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        diagnosa.setLocationRelativeTo(internalFrame1);
        diagnosa.setVisible(true);
    }//GEN-LAST:event_btnPemeriksaan2ActionPerformed

    private void btnPemeriksaan2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPemeriksaan2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnPemeriksaan2KeyPressed

    private void rencanaKepSIKIKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_rencanaKepSIKIKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_rencanaKepSIKIKeyPressed

    private void tbSLKIMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbSLKIMouseClicked
        if (tabModeSLKI.getRowCount() != 0) {
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbSLKIMouseClicked

    private void tbSLKIKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbSLKIKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbSLKIKeyPressed

    private void tbSLKIKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbSLKIKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_tbSLKIKeyReleased

    private void tbSIKIMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbSIKIMouseClicked
        if (tabModeSIKI.getRowCount() != 0) {
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
    }//GEN-LAST:event_tbSIKIMouseClicked

    private void tbSIKIKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbSIKIKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_tbSIKIKeyPressed

    private void tbSIKIKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbSIKIKeyReleased
        // TODO add your handling code here:
    }//GEN-LAST:event_tbSIKIKeyReleased

    private void DefinisiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DefinisiKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_DefinisiKeyPressed

    private void Definisi1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Definisi1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Definisi1KeyPressed

    private void Definisi2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Definisi2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Definisi2KeyPressed

    private void kodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kodeKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kodeKeyPressed

    private void entitasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_entitasKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_entitasKeyPressed

    private void sub_entitasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sub_entitasKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_sub_entitasKeyPressed

    private void kode1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kode1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kode1KeyPressed

    private void entitas1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_entitas1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_entitas1KeyPressed

    private void sub_entitas1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sub_entitas1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_sub_entitas1KeyPressed

    private void kode2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kode2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kode2KeyPressed

    private void entitas2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_entitas2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_entitas2KeyPressed

    private void sub_entitas2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_sub_entitas2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_sub_entitas2KeyPressed

    private void kategoriKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kategoriKeyPressed
//        Valid.pindah(evt, caramasuk, KeluhanUtama);
    }//GEN-LAST:event_kategoriKeyPressed

    private void kategori3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kategori3KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kategori3KeyPressed

    private void kategori4KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_kategori4KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_kategori4KeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            MasterRencanaKeperawatan dialog = new MasterRencanaKeperawatan(new javax.swing.JFrame(), true);
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
    private widget.Button BtnSimpan;
    private widget.TextBox Definisi;
    private widget.TextBox Definisi1;
    private widget.TextBox Definisi2;
    private widget.Label LCount;
    private widget.ScrollPane Scroll;
    private widget.ScrollPane Scroll1;
    private widget.ScrollPane Scroll2;
    private widget.TextBox TCari;
    private javax.swing.JTabbedPane TabRawat;
    private widget.Button btnPemeriksaan;
    private widget.Button btnPemeriksaan1;
    private widget.Button btnPemeriksaan2;
    private widget.TextBox entitas;
    private widget.TextBox entitas1;
    private widget.TextBox entitas2;
    private widget.InternalFrame internalFrame1;
    private widget.InternalFrame internalFrame2;
    private widget.InternalFrame internalFrame3;
    private widget.InternalFrame internalFrame4;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel14;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel19;
    private widget.Label jLabel20;
    private widget.Label jLabel24;
    private widget.Label jLabel25;
    private widget.Label jLabel26;
    private widget.Label jLabel28;
    private widget.Label jLabel29;
    private widget.Label jLabel3;
    private widget.Label jLabel30;
    private widget.Label jLabel4;
    private widget.Label jLabel5;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel9;
    private javax.swing.JPanel jPanel3;
    private widget.ComboBox kategori;
    private widget.ComboBox kategori3;
    private widget.ComboBox kategori4;
    private widget.TextBox kdmasalah;
    private widget.TextBox kdmasalahSIKI;
    private widget.TextBox kdmasalahSLKI;
    private widget.TextBox kdrencana;
    private widget.TextBox kdrencanaSIKI;
    private widget.TextBox kdrencanaSLKI;
    private widget.TextBox kode;
    private widget.TextBox kode1;
    private widget.TextBox kode2;
    private widget.TextBox nmmasalah;
    private widget.TextBox nmmasalahSIKI;
    private widget.TextBox nmmasalahSLKI;
    private widget.panelisi panelGlass10;
    private widget.panelisi panelGlass11;
    private widget.panelisi panelGlass7;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.TextArea rencanaKep;
    private widget.TextArea rencanaKepSIKI;
    private widget.TextArea rencanaKepSLKI;
    private widget.ScrollPane scrollPane1;
    private widget.ScrollPane scrollPane2;
    private widget.ScrollPane scrollPane3;
    private widget.TextBox sub_entitas;
    private widget.TextBox sub_entitas1;
    private widget.TextBox sub_entitas2;
    private widget.Table tbSDKI;
    private widget.Table tbSIKI;
    private widget.Table tbSLKI;
    // End of variables declaration//GEN-END:variables

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            ps = koneksi.prepareStatement(
                    "SELECT\n"
                    + "	master_diagnosa_keperawatan.kode_masalah,\n"
                    + "	master_diagnosa_keperawatan.kode,\n"
                    + "	master_diagnosa_keperawatan.masalah_keperawatan,\n"
                    + "	master_diagnosa_keperawatan.kode_rencana,\n"
                    + "	master_diagnosa_keperawatan.kategori,\n"
                    + "	master_diagnosa_keperawatan.entitas,\n"
                    + "	master_diagnosa_keperawatan.sub_entitas,\n"
                    + "	master_diagnosa_keperawatan.item,\n"
                    + "	master_masalah_keperawatan.definisi \n"
                    + "FROM\n"
                    + "	master_masalah_keperawatan\n"
                    + "	INNER JOIN master_diagnosa_keperawatan ON master_masalah_keperawatan.kode_masalah = master_diagnosa_keperawatan.kode_masalah \n"
                    + "WHERE\n"
                    + "	kategori = 'SDKI' \n"
                    + "	AND (\n"
                    + "		master_diagnosa_keperawatan.kode_masalah LIKE ? \n"
                    + "		OR master_diagnosa_keperawatan.kode_rencana LIKE ? \n"
                    + "	OR master_diagnosa_keperawatan.masalah_keperawatan LIKE ?) \n"
                    + "ORDER BY\n"
                    + "	kode_masalah ASC,\n"
                    + "	kode_rencana ASC"
            );
            ps.setString(1, "%" + TCari.getText().trim() + "%");
            ps.setString(2, "%" + TCari.getText().trim() + "%");
            ps.setString(3, "%" + TCari.getText().trim() + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new String[]{
                    rs.getString("kode_masalah"),
                    rs.getString("masalah_keperawatan"),
                    rs.getString("kode"),
                    rs.getString("entitas"),
                    rs.getString("sub_entitas"),
                    rs.getString("item"),
                    rs.getString("kategori"),
                    rs.getString("definisi"),
                    rs.getString("kode_rencana")
                });
            }
            ps = koneksi.prepareStatement(
                    "SELECT m.kode_masalah, m.nama_masalah, m.definisi, r.kode_rencana, r.rencana_keperawatan "
                    + "FROM master_rencana_keperawatan_sdki r "
                    + "INNER JOIN master_masalah_keperawatan m ON m.kode_masalah = r.kode_masalah "
                    + "WHERE r.kode_rencana LIKE ? OR r.rencana_keperawatan LIKE ? OR m.nama_masalah LIKE ? "
                    + "ORDER BY r.kode_rencana"
            );
            ps.setString(1, "%" + TCari.getText().trim() + "%");
            ps.setString(2, "%" + TCari.getText().trim() + "%");
            ps.setString(3, "%" + TCari.getText().trim() + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new String[]{
                    rs.getString("kode_masalah"),
                    rs.getString("nama_masalah"),
                    "",
                    "",
                    "",
                    rs.getString("rencana_keperawatan"),
                    "",
                    rs.getString("definisi"),
                    rs.getString("kode_rencana")
                });
            }
            rs.close();
            ps.close();

        } catch (Exception e) {
            System.out.println("Notif tampil : " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
            }
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    private void tampilSLKI() {
        Valid.tabelKosong(tabModeSLKI);
        try {

            ps = koneksi.prepareStatement(
                    "SELECT\n"
                    + "	master_diagnosa_keperawatan.kode_masalah,\n"
                    + "	master_diagnosa_keperawatan.kode,\n"
                    + "	master_diagnosa_keperawatan.masalah_keperawatan,\n"
                    + "	master_diagnosa_keperawatan.kode_rencana,\n"
                    + "	master_diagnosa_keperawatan.kategori,\n"
                    + "	master_diagnosa_keperawatan.entitas,\n"
                    + "	master_diagnosa_keperawatan.sub_entitas,\n"
                    + "	master_diagnosa_keperawatan.item,\n"
                    + "	master_masalah_keperawatan.definisi \n"
                    + "FROM\n"
                    + "	master_masalah_keperawatan\n"
                    + "	INNER JOIN master_diagnosa_keperawatan ON master_masalah_keperawatan.kode_masalah = master_diagnosa_keperawatan.kode_masalah \n"
                    + "WHERE\n"
                    + "	kategori = 'SLKI' \n"
                    + "	AND (\n"
                    + "		master_diagnosa_keperawatan.kode_masalah LIKE ? \n"
                    + "		OR master_diagnosa_keperawatan.kode_rencana LIKE ? \n"
                    + "	OR master_diagnosa_keperawatan.masalah_keperawatan LIKE ?) \n"
                    + "ORDER BY\n"
                    + "	kode_masalah ASC,\n"
                    + "	kode_rencana ASC"
            );
            ps.setString(1, "%" + TCari.getText().trim() + "%");
            ps.setString(2, "%" + TCari.getText().trim() + "%");
            ps.setString(3, "%" + TCari.getText().trim() + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                tabModeSLKI.addRow(new String[]{
                    rs.getString("kode_masalah"),
                    rs.getString("masalah_keperawatan"),
                    rs.getString("kode"),
                    rs.getString("entitas"),
                    rs.getString("sub_entitas"),
                    rs.getString("item"),
                    rs.getString("kategori"),
                    rs.getString("definisi"),
                    rs.getString("kode_rencana")
                });
            }
            ps = koneksi.prepareStatement(
                    "SELECT m.kode_masalah, m.nama_masalah, m.definisi,  r.kode_rencana, r.rencana_keperawatan "
                    + "FROM master_rencana_keperawatan_slki r "
                    + "INNER JOIN master_masalah_keperawatan m ON m.kode_masalah = r.kode_masalah "
                    + "WHERE r.kode_rencana LIKE ? OR r.rencana_keperawatan LIKE ? OR m.nama_masalah LIKE ? "
                    + "ORDER BY r.kode_rencana"
            );
            ps.setString(1, "%" + TCari.getText().trim() + "%");
            ps.setString(2, "%" + TCari.getText().trim() + "%");
            ps.setString(3, "%" + TCari.getText().trim() + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                tabModeSLKI.addRow(new String[]{
                    rs.getString("kode_masalah"),
                    rs.getString("nama_masalah"),
                    "",
                    "",
                    "",
                    rs.getString("rencana_keperawatan"),
                    "",
                    rs.getString("definisi"),
                    rs.getString("kode_rencana")
                });
            }
            rs.close();
            ps.close();

        } catch (Exception e) {
            System.out.println("Notif tampilSLKI : " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
            }
        }
        LCount.setText("" + tabModeSLKI.getRowCount());
    }

    private void tampilSIKI() {
        Valid.tabelKosong(tabModeSIKI);
        try {

            ps = koneksi.prepareStatement(
                    "SELECT\n"
                    + "	master_diagnosa_keperawatan.kode_masalah,\n"
                    + "	master_diagnosa_keperawatan.kode,\n"
                    + "	master_diagnosa_keperawatan.masalah_keperawatan,\n"
                    + "	master_diagnosa_keperawatan.kode_rencana,\n"
                    + "	master_diagnosa_keperawatan.kategori,\n"
                    + "	master_diagnosa_keperawatan.entitas,\n"
                    + "	master_diagnosa_keperawatan.sub_entitas,\n"
                    + "	master_diagnosa_keperawatan.item,\n"
                    + "	master_masalah_keperawatan.definisi \n"
                    + "FROM\n"
                    + "	master_masalah_keperawatan\n"
                    + "	INNER JOIN master_diagnosa_keperawatan ON master_masalah_keperawatan.kode_masalah = master_diagnosa_keperawatan.kode_masalah \n"
                    + "WHERE\n"
                    + "	kategori = 'SIKI' \n"
                    + "	AND (\n"
                    + "		master_diagnosa_keperawatan.kode_masalah LIKE ? \n"
                    + "		OR master_diagnosa_keperawatan.kode_rencana LIKE ? \n"
                    + "	OR master_diagnosa_keperawatan.masalah_keperawatan LIKE ?) \n"
                    + "ORDER BY\n"
                    + "	kode_masalah ASC,\n"
                    + "	kode_rencana ASC"
            );
            ps.setString(1, "%" + TCari.getText().trim() + "%");
            ps.setString(2, "%" + TCari.getText().trim() + "%");
            ps.setString(3, "%" + TCari.getText().trim() + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                tabModeSIKI.addRow(new String[]{
                    rs.getString("kode_masalah"),
                    rs.getString("masalah_keperawatan"),
                    rs.getString("kode"),
                    rs.getString("entitas"),
                    rs.getString("sub_entitas"),
                    rs.getString("item"),
                    rs.getString("kategori"),
                    rs.getString("definisi"),
                    rs.getString("kode_rencana")
                });
            }
            ps = koneksi.prepareStatement(
                    "SELECT m.kode_masalah, m.nama_masalah, m.definisi, r.kode_rencana, r.rencana_keperawatan "
                    + "FROM master_rencana_keperawatan_siki r "
                    + "INNER JOIN master_masalah_keperawatan m ON m.kode_masalah = r.kode_masalah "
                    + "WHERE r.kode_rencana LIKE ? OR r.rencana_keperawatan LIKE ? OR m.nama_masalah LIKE ? "
                    + "ORDER BY r.kode_rencana"
            );
            ps.setString(1, "%" + TCari.getText().trim() + "%");
            ps.setString(2, "%" + TCari.getText().trim() + "%");
            ps.setString(3, "%" + TCari.getText().trim() + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                tabModeSIKI.addRow(new String[]{
                    rs.getString("kode_masalah"),
                    rs.getString("nama_masalah"),
                    "",
                    "",
                    "",
                    rs.getString("rencana_keperawatan"),
                    "",
                    rs.getString("definisi"),
                    rs.getString("kode_rencana")
                });
            }
            rs.close();
            ps.close();

        } catch (Exception e) {
            System.out.println("Notif tampilSIKI : " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (Exception e) {
            }
        }
        LCount.setText("" + tabModeSIKI.getRowCount());
    }

    private void emptTeks() {
        kdrencana.setText("");
        rencanaKep.setText("");
        kdrencanaSLKI.setText("");
        rencanaKepSLKI.setText("");
        kdrencanaSIKI.setText("");
        rencanaKepSIKI.setText("");
        TCari.setText("");
        if (TabRawat.getSelectedIndex() == 0) {
            kdrencana.requestFocus();
        } else if (TabRawat.getSelectedIndex() == 1) {
            kdrencanaSLKI.requestFocus();
        } else if (TabRawat.getSelectedIndex() == 2) {
            kdrencanaSIKI.requestFocus();
        }

        Valid.autoNomer(tabMode, "", 3, kdrencana);
        Valid.autoNomer(tabModeSLKI, "", 3, kdrencanaSLKI);
        Valid.autoNomer(tabModeSIKI, "", 3, kdrencanaSIKI);
        kdrencana.requestFocus();
    }

    private void getData() {
        if (TabRawat.getSelectedIndex() == 0) {
            if (tbSDKI.getSelectedRow() != -1) {
                kdmasalah.setText(tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 0).toString());
                nmmasalah.setText(tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 1).toString());
                kode.setText(tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 2).toString());
                entitas.setText(tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 3).toString());
                sub_entitas.setText(tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 4).toString());
                rencanaKep.setText(tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 5).toString());
                kategori.setSelectedItem(tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 6).toString());
                Definisi.setText(tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 7).toString());
                kdrencana.setText(tbSDKI.getValueAt(tbSDKI.getSelectedRow(), 8).toString());
            }
        } else if (TabRawat.getSelectedIndex() == 1) {
            if (tbSLKI.getSelectedRow() != -1) {
                kdmasalahSLKI.setText(tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 0).toString());
                nmmasalahSLKI.setText(tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 1).toString());
                kode1.setText(tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 2).toString());
                entitas1.setText(tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 3).toString());
                sub_entitas1.setText(tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 4).toString());
                rencanaKepSLKI.setText(tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 5).toString());
                kategori3.setSelectedItem(tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 6).toString());
                Definisi1.setText(tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 7).toString());
                kdrencanaSLKI.setText(tbSLKI.getValueAt(tbSLKI.getSelectedRow(), 8).toString());
            }
        } else if (TabRawat.getSelectedIndex() == 2) {
            if (tbSIKI.getSelectedRow() != -1) {
                kdmasalahSIKI.setText(tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 0).toString());
                nmmasalahSIKI.setText(tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 1).toString());
                kode2.setText(tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 2).toString());
                entitas2.setText(tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 3).toString());
                sub_entitas2.setText(tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 4).toString());
                rencanaKepSIKI.setText(tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 5).toString());
                kategori4.setSelectedItem(tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 6).toString());
                Definisi2.setText(tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 7).toString());
                kdrencanaSIKI.setText(tbSIKI.getValueAt(tbSIKI.getSelectedRow(), 8).toString());
            }
        }
    }

    public JTable getTable() {
        return tbSDKI;
    }

    public void isCek() {
        BtnSimpan.setEnabled(akses.getmaster_rencana_keperawatan());
        BtnHapus.setEnabled(akses.getmaster_rencana_keperawatan());
        BtnEdit.setEnabled(akses.getmaster_rencana_keperawatan());
    }
}
