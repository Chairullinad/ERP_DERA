/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rekammedis;

import fungsi.WarnaTable;
import fungsi.WarnaTableSDKI;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Modul Master Diagnosa dan Rencana Keperawatan
 * Menampilkan data gabungan master_diagnosa_keperawatan dan master_masalah_keperawatan
 * Dilengkapi dengan Auto Number ID, Auto Kode Rencana per kategori & masalah, 
 * serta pewarnaan entitas dinamis (user-configurable).
 * 
 * @author SIMRS Indriati Boyolali
 */
public class MasterDiagnosaKeperawatan extends javax.swing.JDialog {

    private final DefaultTableModel tabMode;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private String selectedId = "";
    private final Map<String, Color> cacheWarna = new HashMap<>();
    private MasterMasalahKeperawatan masalah = new MasterMasalahKeperawatan(null, false);

    /**
     * Creates new form MasterDiagnosaKeperawatan
     *
     * @param parent
     * @param modal
     */
    public MasterDiagnosaKeperawatan(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        masalah.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (masalah.getTable().getSelectedRow() != -1) {
                    txtKdMasalah.setText(masalah.getTable().getValueAt(masalah.getTable().getSelectedRow(), 0).toString());
                    txtNmMasalah.setText(masalah.getTable().getValueAt(masalah.getTable().getSelectedRow(), 1).toString());
                    if (masalah.getTable().getColumnCount() > 2 && masalah.getTable().getValueAt(masalah.getTable().getSelectedRow(), 2) != null) {
                        txtDefinisi.setText(masalah.getTable().getValueAt(masalah.getTable().getSelectedRow(), 2).toString());
                    }
                    txtKdRencana.setText(autoKodeRencana(txtKdMasalah.getText(), cmbKategori.getSelectedItem().toString()));
                    txtId.setText(autoIdPreview());
                }
                btnMasalah.requestFocus();
            }
        });

        Object[] row = {
            "ID", "Kd Masalah", "Kode", "Masalah Keperawatan", "Kd Rencana", 
            "Kategori", "Entitas", "Sub-Entitas", "Item / Tindakan", "Definisi Masalah"
        };
        tabMode = new DefaultTableModel(null, row) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };

        tbDiagnosa.setModel(tabMode);
        tbDiagnosa.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbDiagnosa.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Atur lebar kolom tabel
        for (int i = 0; i < 10; i++) {
            TableColumn column = tbDiagnosa.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(45);
            } else if (i == 1) {
                column.setPreferredWidth(75);
            } else if (i == 2) {
                column.setPreferredWidth(65);
            } else if (i == 3) {
                column.setPreferredWidth(220);
            } else if (i == 4) {
                column.setPreferredWidth(75);
            } else if (i == 5) {
                column.setPreferredWidth(60);
            } else if (i == 6) {
                column.setPreferredWidth(130);
            } else if (i == 7) {
                column.setPreferredWidth(130);
            } else if (i == 8) {
                column.setPreferredWidth(320);
            } else if (i == 9) {
                column.setPreferredWidth(220);
            }
        }

        // Terapkan renderer kustom pewarnaan entitas dinamis
        tbDiagnosa.setDefaultRenderer(Object.class, new WarnaTableDiagnosa());

        // Batasan panjang input text
        txtKdMasalah.setDocument(new batasInput((byte) 10).getKata(txtKdMasalah));
        txtNmMasalah.setDocument(new batasInput((int) 150).getKata(txtNmMasalah));
        txtKode.setDocument(new batasInput((byte) 20).getKata(txtKode));
        txtKdRencana.setDocument(new batasInput((byte) 15).getKata(txtKdRencana));
        txtEntitas.setDocument(new batasInput((int) 100).getKata(txtEntitas));
        txtSubEntitas.setDocument(new batasInput((int) 100).getKata(txtSubEntitas));
        txtDefinisi.setDocument(new batasInput((int) 255).getKata(txtDefinisi));
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
        Scroll = new widget.ScrollPane();
        tbDiagnosa = new widget.Table();
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
        panelGlass7 = new widget.panelisi();
        jLabelId = new widget.Label();
        txtId = new widget.TextBox();
        jLabel3 = new widget.Label();
        txtKdMasalah = new widget.TextBox();
        btnMasalah = new widget.Button();
        jLabel5 = new widget.Label();
        txtNmMasalah = new widget.TextBox();
        jLabelKode = new widget.Label();
        txtKode = new widget.TextBox();
        jLabelKdRencana = new widget.Label();
        txtKdRencana = new widget.TextBox();
        BtnAutoKode = new widget.Button();
        jLabelKategori = new widget.Label();
        cmbKategori = new widget.ComboBox();
        jLabelEntitas = new widget.Label();
        txtEntitas = new widget.TextBox();
        BtnWarna = new widget.Button();
        jLabelSubEntitas = new widget.Label();
        txtSubEntitas = new widget.TextBox();
        jLabelDefinisi = new widget.Label();
        txtDefinisi = new widget.TextBox();
        jLabelItem = new widget.Label();
        scrollPaneItem = new widget.ScrollPane();
        txtItem = new widget.TextArea();

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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Master Diagnosa & Rencana Keperawatan ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbDiagnosa.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbDiagnosa.setName("tbDiagnosa"); // NOI18N
        tbDiagnosa.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDiagnosaMouseClicked(evt);
            }
        });
        tbDiagnosa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbDiagnosaKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbDiagnosaKeyReleased(evt);
            }
        });
        Scroll.setViewportView(tbDiagnosa);

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
        TCari.setPreferredSize(new java.awt.Dimension(280, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('1');
        BtnCari.setToolTipText("Alt+1");
        BtnCari.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
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
        BtnAll.setMnemonic('2');
        BtnAll.setToolTipText("Alt+2");
        BtnAll.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
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
        LCount.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass9.add(LCount);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        panelGlass7.setName("panelGlass7"); // NOI18N
        panelGlass7.setPreferredSize(new java.awt.Dimension(44, 130));
        panelGlass7.setLayout(null);

        jLabelId.setText("ID :");
        jLabelId.setName("jLabelId"); // NOI18N
        panelGlass7.add(jLabelId);
        jLabelId.setBounds(0, 10, 35, 23);

        txtId.setEditable(false);
        txtId.setName("txtId"); // NOI18N
        panelGlass7.add(txtId);
        txtId.setBounds(38, 10, 45, 23);

        jLabel3.setText("Kd Masalah :");
        jLabel3.setName("jLabel3"); // NOI18N
        panelGlass7.add(jLabel3);
        jLabel3.setBounds(85, 10, 70, 23);

        txtKdMasalah.setName("txtKdMasalah"); // NOI18N
        txtKdMasalah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtKdMasalahKeyPressed(evt);
            }
        });
        panelGlass7.add(txtKdMasalah);
        txtKdMasalah.setBounds(158, 10, 55, 23);

        btnMasalah.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnMasalah.setMnemonic('1');
        btnMasalah.setToolTipText("Alt+1");
        btnMasalah.setName("btnMasalah"); // NOI18N
        btnMasalah.setPreferredSize(new java.awt.Dimension(28, 23));
        btnMasalah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnMasalahActionPerformed(evt);
            }
        });
        btnMasalah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnMasalahKeyPressed(evt);
            }
        });
        panelGlass7.add(btnMasalah);
        btnMasalah.setBounds(215, 10, 28, 23);

        jLabel5.setText("Masalah :");
        jLabel5.setName("jLabel5"); // NOI18N
        panelGlass7.add(jLabel5);
        jLabel5.setBounds(245, 10, 55, 23);

        txtNmMasalah.setName("txtNmMasalah"); // NOI18N
        txtNmMasalah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtNmMasalahKeyPressed(evt);
            }
        });
        panelGlass7.add(txtNmMasalah);
        txtNmMasalah.setBounds(303, 10, 155, 23);

        jLabelKode.setText("Kode :");
        jLabelKode.setName("jLabelKode"); // NOI18N
        panelGlass7.add(jLabelKode);
        jLabelKode.setBounds(460, 10, 42, 23);

        txtKode.setName("txtKode"); // NOI18N
        txtKode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtKodeKeyPressed(evt);
            }
        });
        panelGlass7.add(txtKode);
        txtKode.setBounds(505, 10, 75, 23);

        jLabelKdRencana.setText("Kd Rencana :");
        jLabelKdRencana.setName("jLabelKdRencana"); // NOI18N
        panelGlass7.add(jLabelKdRencana);
        jLabelKdRencana.setBounds(582, 10, 75, 23);

        txtKdRencana.setName("txtKdRencana"); // NOI18N
        txtKdRencana.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtKdRencanaKeyPressed(evt);
            }
        });
        panelGlass7.add(txtKdRencana);
        txtKdRencana.setBounds(660, 10, 50, 23);

        BtnAutoKode.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnAutoKode.setToolTipText("Auto Generate Kode Rencana");
        BtnAutoKode.setName("BtnAutoKode"); // NOI18N
        BtnAutoKode.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnAutoKode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAutoKodeActionPerformed(evt);
            }
        });
        panelGlass7.add(BtnAutoKode);
        BtnAutoKode.setBounds(712, 10, 28, 23);

        jLabelKategori.setText("Kategori :");
        jLabelKategori.setName("jLabelKategori"); // NOI18N
        panelGlass7.add(jLabelKategori);
        jLabelKategori.setBounds(742, 10, 55, 23);

        cmbKategori.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "SDKI", "SLKI", "SIKI" }));
        cmbKategori.setName("cmbKategori"); // NOI18N
        cmbKategori.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                cmbKategoriItemStateChanged(evt);
            }
        });
        cmbKategori.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                cmbKategoriKeyPressed(evt);
            }
        });
        panelGlass7.add(cmbKategori);
        cmbKategori.setBounds(800, 10, 85, 23);

        jLabelEntitas.setText("Entitas :");
        jLabelEntitas.setName("jLabelEntitas"); // NOI18N
        panelGlass7.add(jLabelEntitas);
        jLabelEntitas.setBounds(0, 40, 80, 23);

        txtEntitas.setName("txtEntitas"); // NOI18N
        txtEntitas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtEntitasKeyPressed(evt);
            }
        });
        panelGlass7.add(txtEntitas);
        txtEntitas.setBounds(85, 40, 185, 23);

        BtnWarna.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        BtnWarna.setToolTipText("Pilih Warna Entitas");
        BtnWarna.setName("BtnWarna"); // NOI18N
        BtnWarna.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnWarna.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnWarnaActionPerformed(evt);
            }
        });
        panelGlass7.add(BtnWarna);
        BtnWarna.setBounds(272, 40, 28, 23);

        jLabelSubEntitas.setText("Sub-Entitas :");
        jLabelSubEntitas.setName("jLabelSubEntitas"); // NOI18N
        panelGlass7.add(jLabelSubEntitas);
        jLabelSubEntitas.setBounds(305, 40, 75, 23);

        txtSubEntitas.setName("txtSubEntitas"); // NOI18N
        txtSubEntitas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtSubEntitasKeyPressed(evt);
            }
        });
        panelGlass7.add(txtSubEntitas);
        txtSubEntitas.setBounds(385, 40, 205, 23);

        jLabelDefinisi.setText("Definisi :");
        jLabelDefinisi.setName("jLabelDefinisi"); // NOI18N
        panelGlass7.add(jLabelDefinisi);
        jLabelDefinisi.setBounds(595, 40, 65, 23);

        txtDefinisi.setName("txtDefinisi"); // NOI18N
        txtDefinisi.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtDefinisiKeyPressed(evt);
            }
        });
        panelGlass7.add(txtDefinisi);
        txtDefinisi.setBounds(665, 40, 220, 23);

        jLabelItem.setText("Item / Tindakan :");
        jLabelItem.setName("jLabelItem"); // NOI18N
        panelGlass7.add(jLabelItem);
        jLabelItem.setBounds(0, 70, 80, 23);

        scrollPaneItem.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        scrollPaneItem.setName("scrollPaneItem"); // NOI18N

        txtItem.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        txtItem.setColumns(20);
        txtItem.setRows(5);
        txtItem.setName("txtItem"); // NOI18N
        txtItem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtItemKeyPressed(evt);
            }
        });
        scrollPaneItem.setViewportView(txtItem);

        panelGlass7.add(scrollPaneItem);
        scrollPaneItem.setBounds(85, 70, 800, 50);

        internalFrame1.add(panelGlass7, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txtKdMasalahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtKdMasalahKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            txtKdRencana.setText(autoKodeRencana(txtKdMasalah.getText(), cmbKategori.getSelectedItem().toString()));
            txtId.setText(autoIdPreview());
        }
        Valid.pindah(evt, TCari, btnMasalah);
    }//GEN-LAST:event_txtKdMasalahKeyPressed

    private void btnMasalahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMasalahActionPerformed
        masalah.isCek();
        masalah.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        masalah.setLocationRelativeTo(internalFrame1);
        masalah.setVisible(true);
    }//GEN-LAST:event_btnMasalahActionPerformed

    private void btnMasalahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnMasalahKeyPressed
        Valid.pindah(evt, txtKdMasalah, txtNmMasalah);
    }//GEN-LAST:event_btnMasalahKeyPressed

    private void txtNmMasalahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtNmMasalahKeyPressed
        Valid.pindah(evt, btnMasalah, txtKode);
    }//GEN-LAST:event_txtNmMasalahKeyPressed

    private void txtKodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtKodeKeyPressed
        Valid.pindah(evt, txtNmMasalah, txtKdRencana);
    }//GEN-LAST:event_txtKodeKeyPressed

    private void txtKdRencanaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtKdRencanaKeyPressed
        Valid.pindah(evt, txtKode, cmbKategori);
    }//GEN-LAST:event_txtKdRencanaKeyPressed

    private void cmbKategoriKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_cmbKategoriKeyPressed
        Valid.pindah(evt, txtKdRencana, txtEntitas);
    }//GEN-LAST:event_cmbKategoriKeyPressed

    private void cmbKategoriItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbKategoriItemStateChanged
        if (evt.getStateChange() == ItemEvent.SELECTED) {
            txtKdRencana.setText(autoKodeRencana(txtKdMasalah.getText(), cmbKategori.getSelectedItem().toString()));
        }
    }//GEN-LAST:event_cmbKategoriItemStateChanged

    private void BtnAutoKodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAutoKodeActionPerformed
        txtKdRencana.setText(autoKodeRencana(txtKdMasalah.getText(), cmbKategori.getSelectedItem().toString()));
    }//GEN-LAST:event_BtnAutoKodeActionPerformed

    private void BtnWarnaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnWarnaActionPerformed
        String entitas = txtEntitas.getText().trim();
        String kategori = cmbKategori.getSelectedItem().toString().trim().toUpperCase();
        if (entitas.equals("")) {
            JOptionPane.showMessageDialog(null, "Silahkan isi nama entitas terlebih dahulu sebelum memilih warna!");
            txtEntitas.requestFocus();
            return;
        }

        // Ambil warna saat ini (baik dari DB custom atau warna bawaan WarnaTableSDKI) sebagai initial color
        Color initial = WarnaTableSDKI.getWarna(kategori, entitas);
        if (initial == null) {
            initial = Color.WHITE;
        }

        Color pilihan = JColorChooser.showDialog(this, "Pilih Warna untuk Entitas: " + entitas + " (" + kategori + ")", initial);
        if (pilihan != null) {
            String hexColor = String.format("#%02X%02X%02X", pilihan.getRed(), pilihan.getGreen(), pilihan.getBlue());
            try {
                PreparedStatement psWarna = koneksi.prepareStatement(
                    "replace into master_entitas_warna (kategori, entitas, kode_warna) values (?, ?, ?)"
                );
                try {
                    psWarna.setString(1, kategori);
                    psWarna.setString(2, entitas);
                    psWarna.setString(3, hexColor);
                    psWarna.executeUpdate();
                } finally {
                    psWarna.close();
                }
                // Reload cache sentral di WarnaTableSDKI agar langsung tersinkron ke DlgDiagnosaKeperawatan dan Master
                WarnaTableSDKI.loadCustomWarna();
                loadWarnaDariDB();
                tbDiagnosa.repaint();
                JOptionPane.showMessageDialog(null, "Warna entitas '" + entitas + "' (" + kategori + ") berhasil disimpan dan disinkronkan!");
            } catch (Exception ex) {
                System.out.println("Notifikasi Simpan Warna : " + ex);
                JOptionPane.showMessageDialog(null, "Gagal menyimpan warna entitas: " + ex.getMessage());
            }
        }
    }//GEN-LAST:event_BtnWarnaActionPerformed

    private void txtEntitasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtEntitasKeyPressed
        Valid.pindah(evt, cmbKategori, txtSubEntitas);
    }//GEN-LAST:event_txtEntitasKeyPressed

    private void txtSubEntitasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtSubEntitasKeyPressed
        Valid.pindah(evt, txtEntitas, txtDefinisi);
    }//GEN-LAST:event_txtSubEntitasKeyPressed

    private void txtDefinisiKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtDefinisiKeyPressed
        Valid.pindah(evt, txtSubEntitas, txtItem);
    }//GEN-LAST:event_txtDefinisiKeyPressed

    private void txtItemKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtItemKeyPressed
        Valid.pindah2(evt, txtDefinisi, BtnSimpan);
    }//GEN-LAST:event_txtItemKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if (txtKdMasalah.getText().trim().equals("")) {
            Valid.textKosong(txtKdMasalah, "Kode Masalah");
        } else if (txtNmMasalah.getText().trim().equals("")) {
            Valid.textKosong(txtNmMasalah, "Masalah Keperawatan");
        } else if (txtKdRencana.getText().trim().equals("")) {
            Valid.textKosong(txtKdRencana, "Kode Rencana");
        } else if (txtItem.getText().trim().equals("")) {
            Valid.textKosong(txtItem, "Item / Tindakan");
        } else {
            try {
                // Parameterized insert untuk keamanan Anti SQL-Injection
                ps = koneksi.prepareStatement(
                    "insert into master_diagnosa_keperawatan " +
                    "(kode_masalah, kode, masalah_keperawatan, kode_rencana, kategori, entitas, sub_entitas, item) " +
                    "values (?, ?, ?, ?, ?, ?, ?, ?)"
                );
                try {
                    ps.setString(1, txtKdMasalah.getText().trim());
                    ps.setString(2, txtKode.getText().trim());
                    ps.setString(3, txtNmMasalah.getText().trim());
                    ps.setString(4, txtKdRencana.getText().trim());
                    ps.setString(5, cmbKategori.getSelectedItem().toString());
                    ps.setString(6, txtEntitas.getText().trim());
                    ps.setString(7, txtSubEntitas.getText().trim());
                    ps.setString(8, txtItem.getText().trim());
                    ps.executeUpdate();
                } finally {
                    if (ps != null) {
                        ps.close();
                    }
                }
                tampil();
                emptTeks();
            } catch (Exception ex) {
                System.out.println("Notifikasi Simpan : " + ex);
                JOptionPane.showMessageDialog(null, "Gagal menyimpan data diagnosa/rencana keperawatan: " + ex.getMessage());
            }
        }
    }//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnSimpanActionPerformed(null);
        } else {
            Valid.pindah(evt, txtItem, BtnBatal);
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
        if (selectedId.trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih data pada tabel yang mau dihapus terlebih dahulu...!");
            tbDiagnosa.requestFocus();
        } else {
            if (JOptionPane.showConfirmDialog(null, "Apakah yakin data rencana keperawatan ini mau dihapus?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                try {
                    ps = koneksi.prepareStatement("delete from master_diagnosa_keperawatan where id=?");
                    try {
                        ps.setString(1, selectedId);
                        ps.executeUpdate();
                    } finally {
                        if (ps != null) {
                            ps.close();
                        }
                    }
                    tampil();
                    emptTeks();
                } catch (Exception ex) {
                    System.out.println("Notifikasi Hapus : " + ex);
                    JOptionPane.showMessageDialog(null, "Gagal menghapus data: " + ex.getMessage());
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
        if (selectedId.trim().equals("")) {
            JOptionPane.showMessageDialog(null, "Maaf, silahkan pilih data pada tabel yang mau diganti/diedit terlebih dahulu...!");
            tbDiagnosa.requestFocus();
        } else if (txtKdMasalah.getText().trim().equals("")) {
            Valid.textKosong(txtKdMasalah, "Kode Masalah");
        } else if (txtNmMasalah.getText().trim().equals("")) {
            Valid.textKosong(txtNmMasalah, "Masalah Keperawatan");
        } else if (txtKdRencana.getText().trim().equals("")) {
            Valid.textKosong(txtKdRencana, "Kode Rencana");
        } else if (txtItem.getText().trim().equals("")) {
            Valid.textKosong(txtItem, "Item / Tindakan");
        } else {
            try {
                // Update parameterized menggunakan primary key id
                ps = koneksi.prepareStatement(
                    "update master_diagnosa_keperawatan set " +
                    "kode_masalah=?, kode=?, masalah_keperawatan=?, kode_rencana=?, kategori=?, entitas=?, sub_entitas=?, item=? " +
                    "where id=?"
                );
                try {
                    ps.setString(1, txtKdMasalah.getText().trim());
                    ps.setString(2, txtKode.getText().trim());
                    ps.setString(3, txtNmMasalah.getText().trim());
                    ps.setString(4, txtKdRencana.getText().trim());
                    ps.setString(5, cmbKategori.getSelectedItem().toString());
                    ps.setString(6, txtEntitas.getText().trim());
                    ps.setString(7, txtSubEntitas.getText().trim());
                    ps.setString(8, txtItem.getText().trim());
                    ps.setString(9, selectedId);
                    ps.executeUpdate();
                } finally {
                    if (ps != null) {
                        ps.close();
                    }
                }
                tampil();
                emptTeks();
            } catch (Exception ex) {
                System.out.println("Notifikasi Edit : " + ex);
                JOptionPane.showMessageDialog(null, "Gagal mengubah data: " + ex.getMessage());
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
            tbDiagnosa.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

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
        emptTeks();
        tampil();
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnAllActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnCari, txtKdMasalah);
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void tbDiagnosaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDiagnosaMouseClicked
        if (tabMode.getRowCount() != 0) {
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
            if (evt.getClickCount() == 2) {
                dispose();
            }
        }
    }//GEN-LAST:event_tbDiagnosaMouseClicked

    private void tbDiagnosaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDiagnosaKeyPressed
        if (tabMode.getRowCount() != 0) {
            if (evt.getKeyCode() == KeyEvent.VK_SHIFT) {
                TCari.setText("");
                TCari.requestFocus();
            }
        }
    }//GEN-LAST:event_tbDiagnosaKeyPressed

    private void formWindowActivated(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowActivated
        TCari.requestFocus();
    }//GEN-LAST:event_formWindowActivated

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        initTabelWarna();
        tampil();
        emptTeks();
    }//GEN-LAST:event_formWindowOpened

    private void tbDiagnosaKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbDiagnosaKeyReleased
        if (tabMode.getRowCount() != 0) {
            if ((evt.getKeyCode() == KeyEvent.VK_ENTER) || (evt.getKeyCode() == KeyEvent.VK_UP) || (evt.getKeyCode() == KeyEvent.VK_DOWN)) {
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbDiagnosaKeyReleased

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            MasterDiagnosaKeperawatan dialog = new MasterDiagnosaKeperawatan(new javax.swing.JFrame(), true);
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
    private widget.Button BtnAutoKode;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnSimpan;
    private widget.Button BtnWarna;
    private widget.Button btnMasalah;
    private widget.Label LCount;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.ComboBox cmbKategori;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel3;
    private widget.Label jLabel5;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabelDefinisi;
    private widget.Label jLabelEntitas;
    private widget.Label jLabelId;
    private widget.Label jLabelItem;
    private widget.Label jLabelKategori;
    private widget.Label jLabelKdRencana;
    private widget.Label jLabelKode;
    private widget.Label jLabelSubEntitas;
    private javax.swing.JPanel jPanel3;
    private widget.panelisi panelGlass7;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ScrollPane scrollPaneItem;
    private widget.Table tbDiagnosa;
    private widget.TextBox txtDefinisi;
    private widget.TextBox txtEntitas;
    private widget.TextBox txtId;
    private widget.TextArea txtItem;
    private widget.TextBox txtKdMasalah;
    private widget.TextBox txtKdRencana;
    private widget.TextBox txtKode;
    private widget.TextBox txtNmMasalah;
    private widget.TextBox txtSubEntitas;
    // End of variables declaration//GEN-END:variables

    /**
     * Inisialisasi skema tabel database master_entitas_warna (idempotent)
     */
    private void initTabelWarna() {
        try {
            Statement stmt = koneksi.createStatement();
            try {
                stmt.execute(
                    "create table if not exists master_entitas_warna (" +
                    "  kategori varchar(10) not null," +
                    "  entitas varchar(150) not null," +
                    "  kode_warna varchar(10) not null," +
                    "  primary key (kategori, entitas)" +
                    ") engine=innodb default charset=latin1"
                );
            } finally {
                stmt.close();
            }
        } catch (Exception ex) {
            System.out.println("Notifikasi inisialisasi tabel warna : " + ex);
        }
        loadWarnaDariDB();
    }

    /**
     * Memuat konfigurasi warna entitas dari database ke in-memory cache
     */
    private void loadWarnaDariDB() {
        WarnaTableSDKI.loadCustomWarna();
        cacheWarna.clear();
        try {
            Statement stmt = koneksi.createStatement();
            try {
                ResultSet rsWarna = stmt.executeQuery("select kategori, entitas, kode_warna from master_entitas_warna");
                while (rsWarna.next()) {
                    String kat = rsWarna.getString("kategori").trim().toUpperCase();
                    String ent = rsWarna.getString("entitas").trim().toLowerCase();
                    String hex = rsWarna.getString("kode_warna").trim();
                    try {
                        Color col = Color.decode(hex);
                        cacheWarna.put(kat + "#" + ent, col);
                        cacheWarna.put(ent, col); // fallback tanpa prefix kategori
                    } catch (Exception ex) {
                        System.out.println("Gagal decode warna: " + hex + " -> " + ex);
                    }
                }
            } finally {
                stmt.close();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi muat warna entitas : " + e);
        }
    }

    /**
     * Menghasilkan nomor urut kode_rencana otomatis (3 digit) berdasarkan kode_masalah dan kategori
     */
    private String autoKodeRencana(String kdMasalah, String kategori) {
        String kodeBaru = "001";
        if (kdMasalah.trim().equals("")) {
            return kodeBaru;
        }
        try {
            PreparedStatement psKd = koneksi.prepareStatement(
                "select coalesce(max(cast(kode_rencana as unsigned)), 0) + 1 as no_urut " +
                "from master_diagnosa_keperawatan " +
                "where kode_masalah = ? and kategori = ?"
            );
            try {
                psKd.setString(1, kdMasalah.trim());
                psKd.setString(2, kategori.trim());
                ResultSet rsKd = psKd.executeQuery();
                if (rsKd.next()) {
                    int urut = rsKd.getInt("no_urut");
                    kodeBaru = String.format("%03d", urut);
                }
            } finally {
                psKd.close();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi autoKodeRencana : " + e);
        }
        return kodeBaru;
    }

    /**
     * Menghasilkan preview nomor ID berikutnya dari basis data
     */
    private String autoIdPreview() {
        String idBaru = "1";
        try {
            Statement stId = koneksi.createStatement();
            try {
                ResultSet rsId = stId.executeQuery("select coalesce(max(id), 0) + 1 as id_next from master_diagnosa_keperawatan");
                if (rsId.next()) {
                    idBaru = rsId.getString("id_next");
                }
            } finally {
                stId.close();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi autoIdPreview : " + e);
        }
        return idBaru;
    }

    /**
     * Memuat dan menampilkan data ke tabel tbDiagnosa sesuai query gabungan
     * master_diagnosa_keperawatan dan master_masalah_keperawatan
     */
    private void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            String sql = "SELECT " +
                "master_diagnosa_keperawatan.id, " +
                "master_diagnosa_keperawatan.kode_masalah, " +
                "master_diagnosa_keperawatan.kode, " +
                "master_diagnosa_keperawatan.masalah_keperawatan, " +
                "master_diagnosa_keperawatan.kode_rencana, " +
                "master_diagnosa_keperawatan.kategori, " +
                "master_diagnosa_keperawatan.entitas, " +
                "master_diagnosa_keperawatan.sub_entitas, " +
                "master_diagnosa_keperawatan.item, " +
                "master_masalah_keperawatan.definisi " +
                "FROM " +
                "master_diagnosa_keperawatan " +
                "INNER JOIN " +
                "master_masalah_keperawatan " +
                "ON " +
                "master_diagnosa_keperawatan.kode_masalah = master_masalah_keperawatan.kode_masalah ";

            String keyword = TCari.getText().trim();
            if (!keyword.equals("")) {
                sql += "WHERE (" +
                    "master_diagnosa_keperawatan.kode_masalah LIKE ? OR " +
                    "master_diagnosa_keperawatan.kode LIKE ? OR " +
                    "master_diagnosa_keperawatan.masalah_keperawatan LIKE ? OR " +
                    "master_diagnosa_keperawatan.kode_rencana LIKE ? OR " +
                    "master_diagnosa_keperawatan.kategori LIKE ? OR " +
                    "master_diagnosa_keperawatan.entitas LIKE ? OR " +
                    "master_diagnosa_keperawatan.sub_entitas LIKE ? OR " +
                    "master_diagnosa_keperawatan.item LIKE ? OR " +
                    "master_masalah_keperawatan.definisi LIKE ?) ";
            }

            sql += "GROUP BY master_diagnosa_keperawatan.id " +
                   "ORDER BY master_diagnosa_keperawatan.kode_masalah, master_diagnosa_keperawatan.kategori, master_diagnosa_keperawatan.kode_rencana";

            ps = koneksi.prepareStatement(sql);
            try {
                if (!keyword.equals("")) {
                    String param = "%" + keyword + "%";
                    for (int i = 1; i <= 9; i++) {
                        ps.setString(i, param);
                    }
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new String[]{
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6),
                        rs.getString(7),
                        rs.getString(8),
                        rs.getString(9),
                        rs.getString(10)
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif Tampil Query : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Tampil : " + e);
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    /**
     * Mengosongkan form input dan mengembalikan fokus kursor ke mode penambahan item baru
     */
    private void emptTeks() {
        selectedId = "";
        txtId.setText(autoIdPreview());
        txtKdMasalah.setText("");
        txtNmMasalah.setText("");
        txtKode.setText("");
        txtKdRencana.setText("001");
        cmbKategori.setSelectedIndex(0);
        txtEntitas.setText("");
        txtSubEntitas.setText("");
        txtDefinisi.setText("");
        txtItem.setText("");
        TCari.setText("");
        txtKdMasalah.requestFocus();
    }

    /**
     * Membaca baris yang dipilih dari tabel tbDiagnosa ke field input
     */
    private void getData() {
        if (tbDiagnosa.getSelectedRow() != -1) {
            selectedId = tabMode.getValueAt(tbDiagnosa.getSelectedRow(), 0).toString();
            txtId.setText(selectedId);
            txtKdMasalah.setText(tabMode.getValueAt(tbDiagnosa.getSelectedRow(), 1).toString());
            txtKode.setText(tabMode.getValueAt(tbDiagnosa.getSelectedRow(), 2).toString());
            txtNmMasalah.setText(tabMode.getValueAt(tbDiagnosa.getSelectedRow(), 3).toString());
            txtKdRencana.setText(tabMode.getValueAt(tbDiagnosa.getSelectedRow(), 4).toString());
            cmbKategori.setSelectedItem(tabMode.getValueAt(tbDiagnosa.getSelectedRow(), 5).toString());
            txtEntitas.setText(tabMode.getValueAt(tbDiagnosa.getSelectedRow(), 6).toString());
            txtSubEntitas.setText(tabMode.getValueAt(tbDiagnosa.getSelectedRow(), 7).toString());
            txtItem.setText(tabMode.getValueAt(tbDiagnosa.getSelectedRow(), 8).toString());
            txtDefinisi.setText(tabMode.getValueAt(tbDiagnosa.getSelectedRow(), 9).toString());
        }
    }

    public JTable getTable() {
        return tbDiagnosa;
    }

    public void isCek() {
        BtnSimpan.setEnabled(akses.getmaster_masalah_keperawatan());
        BtnHapus.setEnabled(akses.getmaster_masalah_keperawatan());
        BtnEdit.setEnabled(akses.getmaster_masalah_keperawatan());
    }

    /**
     * TableCellRenderer kustom untuk pewarnaan dinamis baris tabel berdasarkan entitas & kategori
     */
    private class WarnaTableDiagnosa extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Warna default zebra standar Khanza
            if (row % 2 == 1) {
                component.setBackground(new Color(248, 253, 243));
            } else {
                component.setBackground(Color.WHITE);
            }

            // Periksa warna entitas dari WarnaTableSDKI (prioritas DB custom, fallback ke bawaan SDKI/SLKI/SIKI)
            try {
                String kat = table.getValueAt(row, 5).toString().trim().toUpperCase();
                String ent = table.getValueAt(row, 6).toString().trim().toLowerCase();
                Color warna = WarnaTableSDKI.getWarna(kat, ent);
                if (warna != null) {
                    component.setBackground(warna);
                }
            } catch (Exception ex) {
            }

            component.setForeground(Color.BLACK);
            if (isSelected) {
                component.setBackground(new Color(220, 235, 255));
                component.setForeground(Color.BLUE);
            }
            return component;
        }
    }
}
