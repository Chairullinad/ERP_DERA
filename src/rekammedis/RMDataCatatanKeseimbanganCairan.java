/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package rekammedis;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
public final class RMDataCatatanKeseimbanganCairan extends javax.swing.JDialog {

    private final DefaultTableModel tabMode;
    private Connection koneksi = koneksiDB.condb();
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0;
    private DlgCariPetugas petugas = new DlgCariPetugas(null, false);
    private String TANGGALMUNDUR = "yes";

    /**
     * Creates new form DlgRujuk
     *
     * @param parent
     * @param modal
     */
    public RMDataCatatanKeseimbanganCairan(java.awt.Frame parent, boolean modal) {
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
            "Tgl.Lahir",
            "Tanggal",
            "Jam",
            "Shift Ke",
            "Keterangan",
            "Nama Cairan",
            "Cairan Awal",
            "IV Line I",
            "Nama Cairan",
            "Cairan Awal",
            "IV Line II",
            "Nama Cairan",
            "Cairan Awal",
            "IV Line III",
            "Nama Cairan",
            "Minum",
            "Nama Cairan",
            "Tranfusi",
            "TOTAL INTAKKE",
            "Urine/Feses",
            "Muntah",
            "Feses",
            "NGT/Oral",
            "Darah",
            "Drain 1",
            "Drain 2",
            "Bilas",
            "IWL",
            "Keseimbangan Cairan",
            "TOTAL OUTPUT",
            "NIP",
            "Nama Petugas"

        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbObat.setModel(tabMode);

        //tbObat.setDefaultRenderer(Object.class, new WarnaTable(panelJudul.getBackground(),tbObat.getBackground()));
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 37; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(125);
            } else if (i == 1) {
                column.setPreferredWidth(65);
            } else if (i == 2) {
                column.setPreferredWidth(160); //nama
            } else if (i == 3) {
                column.setPreferredWidth(40);
            } else if (i == 4) {
                column.setPreferredWidth(30);
            } else if (i == 5) {
                column.setPreferredWidth(85);
            } else if (i == 6) {
                column.setPreferredWidth(85);
            } else if (i == 7) {
                column.setPreferredWidth(90);//nama iv L1
            } else if (i == 8) {
                column.setPreferredWidth(70);
            } else if (i == 9) {
                column.setPreferredWidth(90);
            } else if (i == 10) {
                column.setPreferredWidth(70);
            } else if (i == 11) {
                column.setPreferredWidth(90);
            } else if (i == 12) {
                column.setPreferredWidth(70);
            } else if (i == 13) {
                column.setPreferredWidth(90);
            } else if (i == 14) {
                column.setPreferredWidth(70);
            } else if (i == 15) {
                column.setPreferredWidth(90);
            } else if (i == 16) {
                column.setPreferredWidth(70);
            } else if (i == 17) {
                column.setPreferredWidth(90);
            } else if (i == 18) {
                column.setPreferredWidth(70);
            } else if (i == 19) {
                column.setPreferredWidth(90);
            } else if (i == 20) {
                column.setPreferredWidth(70);
            } else if (i == 21) {
                column.setPreferredWidth(90);
            } else if (i == 22) {
                column.setPreferredWidth(70);
            } else if (i == 23) {
                column.setPreferredWidth(90);
            } else if (i == 24) {
                column.setPreferredWidth(90);
            } else if (i == 25) {
                column.setPreferredWidth(90);
            } else if (i == 26) {
                column.setPreferredWidth(60);
            } else if (i == 27) {
                column.setPreferredWidth(90);
            } else if (i == 28) {
                column.setPreferredWidth(90);
            } else if (i == 29) {
                column.setPreferredWidth(90);
            } else if (i == 30) {
                column.setPreferredWidth(90);
            } else if (i == 31) {
                column.setPreferredWidth(90);
            } else if (i == 32) {
                column.setPreferredWidth(90);
            } else if (i == 33) {
                column.setPreferredWidth(90);
            } else if (i == 34) {
                column.setPreferredWidth(90);
            } else if (i == 35) {
                column.setPreferredWidth(90);
            } else if (i == 36) {
                column.setPreferredWidth(90);
            }
        }

        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        TNoRw.setDocument(new batasInput((byte) 17).getKata(TNoRw));
        NIP.setDocument(new batasInput((byte) 20).getKata(NIP));

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
                    NIP.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 0).toString());
                    NamaPetugas.setText(petugas.getTable().getValueAt(petugas.getTable().getSelectedRow(), 1).toString());
                }
                NIP.requestFocus();
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
        Infus.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                try {
                    int value1 = Infus.getText().trim().isEmpty() ? 0 : Integer.parseInt(Infus.getText().trim());
                    int value2 = Infus1.getText().trim().isEmpty() ? 0 : Integer.parseInt(Infus1.getText().trim());
                    int value3 = Infus2.getText().trim().isEmpty() ? 0 : Integer.parseInt(Infus2.getText().trim());
                    Tintake.setText(String.valueOf(value1 + value2 + value3));
                } catch (NumberFormatException ex) {
                    Tintake.setText("0");
                }
            }
        });

        Infus1.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                try {
                    int value1 = Infus.getText().trim().isEmpty() ? 0 : Integer.parseInt(Infus.getText().trim());
                    int value2 = Infus1.getText().trim().isEmpty() ? 0 : Integer.parseInt(Infus1.getText().trim());
                    int value3 = Infus2.getText().trim().isEmpty() ? 0 : Integer.parseInt(Infus2.getText().trim());
                    int value4 = minum.getText().trim().isEmpty() ? 0 : Integer.parseInt(minum.getText().trim());
                    int value5 = ngt.getText().trim().isEmpty() ? 0 : Integer.parseInt(ngt.getText().trim());
                    Tintake.setText(String.valueOf(value1 + value2 + value3 + value4 + value5));
                } catch (NumberFormatException ex) {
                    Tintake.setText("0");
                }
            }
        });

        Infus2.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                try {
                    int value1 = Infus.getText().trim().isEmpty() ? 0 : Integer.parseInt(Infus.getText().trim());
                    int value2 = Infus1.getText().trim().isEmpty() ? 0 : Integer.parseInt(Infus1.getText().trim());
                    int value3 = Infus2.getText().trim().isEmpty() ? 0 : Integer.parseInt(Infus2.getText().trim());
                    int value4 = minum.getText().trim().isEmpty() ? 0 : Integer.parseInt(minum.getText().trim());
                    int value5 = ngt.getText().trim().isEmpty() ? 0 : Integer.parseInt(ngt.getText().trim());
                    Tintake.setText(String.valueOf(value1 + value2 + value3 + value4 + value5));
                } catch (NumberFormatException ex) {
                    Tintake.setText("0");
                }
            }
        });
        minum.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                try {
                    int value1 = Infus.getText().trim().isEmpty() ? 0 : Integer.parseInt(Infus.getText().trim());
                    int value2 = Infus1.getText().trim().isEmpty() ? 0 : Integer.parseInt(Infus1.getText().trim());
                    int value3 = Infus2.getText().trim().isEmpty() ? 0 : Integer.parseInt(Infus2.getText().trim());
                    int value4 = minum.getText().trim().isEmpty() ? 0 : Integer.parseInt(minum.getText().trim());
                    int value5 = ngt.getText().trim().isEmpty() ? 0 : Integer.parseInt(ngt.getText().trim());
                    Tintake.setText(String.valueOf(value1 + value2 + value3 + value4 + value5));
                } catch (NumberFormatException ex) {
                    Tintake.setText("0");
                }
            }
        });
        ngt.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                try {
                    int value1 = Infus.getText().trim().isEmpty() ? 0 : Integer.parseInt(Infus.getText().trim());
                    int value2 = Infus1.getText().trim().isEmpty() ? 0 : Integer.parseInt(Infus1.getText().trim());
                    int value3 = Infus2.getText().trim().isEmpty() ? 0 : Integer.parseInt(Infus2.getText().trim());
                    int value4 = minum.getText().trim().isEmpty() ? 0 : Integer.parseInt(minum.getText().trim());
                    int value5 = ngt.getText().trim().isEmpty() ? 0 : Integer.parseInt(ngt.getText().trim());
                    Tintake.setText(String.valueOf(value1 + value2 + value3 + value4 + value5));
                } catch (NumberFormatException ex) {
                    Tintake.setText("0");
                }
            }
        });

        muntah.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                try {
                    int val1 = muntah.getText().trim().isEmpty() ? 0 : Integer.parseInt(muntah.getText().trim());
                    int val2 = feses.getText().trim().isEmpty() ? 0 : Integer.parseInt(feses.getText().trim());
                    int val3 = urine.getText().trim().isEmpty() ? 0 : Integer.parseInt(urine.getText().trim());
                    int val4 = ongt.getText().trim().isEmpty() ? 0 : Integer.parseInt(ongt.getText().trim());
                    int val5 = darah.getText().trim().isEmpty() ? 0 : Integer.parseInt(darah.getText().trim());
                    int val6 = drain.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain.getText().trim());
                    int val7 = drain1.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain1.getText().trim());
                    int val8 = bilas.getText().trim().isEmpty() ? 0 : Integer.parseInt(bilas.getText().trim());

//                    double val9 = IWL.getText().trim().isEmpty() ? 0 : Double.parseDouble(IWL.getText().trim());
                    double total = val1 + val2 + val3 + val4 + val5 + val6 + val7 + val8;

                    if (total % 1 == 0) {
                        Output.setText(String.valueOf((int) total));
                    } else {
                        Output.setText(String.format("%.2f", total));
                    }

                } catch (NumberFormatException ex) {
                    Output.setText("0");
                }
            }
        });
        feses.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                try {
                    int val1 = muntah.getText().trim().isEmpty() ? 0 : Integer.parseInt(muntah.getText().trim());
                    int val2 = feses.getText().trim().isEmpty() ? 0 : Integer.parseInt(feses.getText().trim());
                    int val3 = urine.getText().trim().isEmpty() ? 0 : Integer.parseInt(urine.getText().trim());
                    int val4 = ongt.getText().trim().isEmpty() ? 0 : Integer.parseInt(ongt.getText().trim());
                    int val5 = darah.getText().trim().isEmpty() ? 0 : Integer.parseInt(darah.getText().trim());
                    int val6 = drain.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain.getText().trim());
                    int val7 = drain1.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain1.getText().trim());
                    int val8 = bilas.getText().trim().isEmpty() ? 0 : Integer.parseInt(bilas.getText().trim());

//                    double val9 = IWL.getText().trim().isEmpty() ? 0 : Double.parseDouble(IWL.getText().trim());
                    double total = val1 + val2 + val3 + val4 + val5 + val6 + val7 + val8;

                    if (total % 1 == 0) {
                        Output.setText(String.valueOf((int) total));
                    } else {
                        Output.setText(String.format("%.2f", total));
                    }

                } catch (NumberFormatException ex) {
                    Output.setText("0");
                }
            }
        });
        urine.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                try {
                    int val1 = muntah.getText().trim().isEmpty() ? 0 : Integer.parseInt(muntah.getText().trim());
                    int val2 = feses.getText().trim().isEmpty() ? 0 : Integer.parseInt(feses.getText().trim());
                    int val3 = urine.getText().trim().isEmpty() ? 0 : Integer.parseInt(urine.getText().trim());
                    int val4 = ongt.getText().trim().isEmpty() ? 0 : Integer.parseInt(ongt.getText().trim());
                    int val5 = darah.getText().trim().isEmpty() ? 0 : Integer.parseInt(darah.getText().trim());
                    int val6 = drain.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain.getText().trim());
                    int val7 = drain1.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain1.getText().trim());
                    int val8 = bilas.getText().trim().isEmpty() ? 0 : Integer.parseInt(bilas.getText().trim());

//                    double val9 = IWL.getText().trim().isEmpty() ? 0 : Double.parseDouble(IWL.getText().trim());
                    double total = val1 + val2 + val3 + val4 + val5 + val6 + val7 + val8;

                    if (total % 1 == 0) {
                        Output.setText(String.valueOf((int) total));
                    } else {
                        Output.setText(String.format("%.2f", total));
                    }

                } catch (NumberFormatException ex) {
                    Output.setText("0");
                }
            }
        });
        ongt.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                try {
                    int val1 = muntah.getText().trim().isEmpty() ? 0 : Integer.parseInt(muntah.getText().trim());
                    int val2 = feses.getText().trim().isEmpty() ? 0 : Integer.parseInt(feses.getText().trim());
                    int val3 = urine.getText().trim().isEmpty() ? 0 : Integer.parseInt(urine.getText().trim());
                    int val4 = ongt.getText().trim().isEmpty() ? 0 : Integer.parseInt(ongt.getText().trim());
                    int val5 = darah.getText().trim().isEmpty() ? 0 : Integer.parseInt(darah.getText().trim());
                    int val6 = drain.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain.getText().trim());
                    int val7 = drain1.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain1.getText().trim());
                    int val8 = bilas.getText().trim().isEmpty() ? 0 : Integer.parseInt(bilas.getText().trim());

//                    double val9 = IWL.getText().trim().isEmpty() ? 0 : Double.parseDouble(IWL.getText().trim());
                    double total = val1 + val2 + val3 + val4 + val5 + val6 + val7 + val8;

                    if (total % 1 == 0) {
                        Output.setText(String.valueOf((int) total));
                    } else {
                        Output.setText(String.format("%.2f", total));
                    }

                } catch (NumberFormatException ex) {
                    Output.setText("0");
                }
            }
        });
        darah.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                try {
                    int val1 = muntah.getText().trim().isEmpty() ? 0 : Integer.parseInt(muntah.getText().trim());
                    int val2 = feses.getText().trim().isEmpty() ? 0 : Integer.parseInt(feses.getText().trim());
                    int val3 = urine.getText().trim().isEmpty() ? 0 : Integer.parseInt(urine.getText().trim());
                    int val4 = ongt.getText().trim().isEmpty() ? 0 : Integer.parseInt(ongt.getText().trim());
                    int val5 = darah.getText().trim().isEmpty() ? 0 : Integer.parseInt(darah.getText().trim());
                    int val6 = drain.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain.getText().trim());
                    int val7 = drain1.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain1.getText().trim());
                    int val8 = bilas.getText().trim().isEmpty() ? 0 : Integer.parseInt(bilas.getText().trim());

//                    double val9 = IWL.getText().trim().isEmpty() ? 0 : Double.parseDouble(IWL.getText().trim());
                    double total = val1 + val2 + val3 + val4 + val5 + val6 + val7 + val8;

                    if (total % 1 == 0) {
                        Output.setText(String.valueOf((int) total));
                    } else {
                        Output.setText(String.format("%.2f", total));
                    }

                } catch (NumberFormatException ex) {
                    Output.setText("0");
                }
            }
        });
        drain.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                try {
                    int val1 = muntah.getText().trim().isEmpty() ? 0 : Integer.parseInt(muntah.getText().trim());
                    int val2 = feses.getText().trim().isEmpty() ? 0 : Integer.parseInt(feses.getText().trim());
                    int val3 = urine.getText().trim().isEmpty() ? 0 : Integer.parseInt(urine.getText().trim());
                    int val4 = ongt.getText().trim().isEmpty() ? 0 : Integer.parseInt(ongt.getText().trim());
                    int val5 = darah.getText().trim().isEmpty() ? 0 : Integer.parseInt(darah.getText().trim());
                    int val6 = drain.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain.getText().trim());
                    int val7 = drain1.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain1.getText().trim());
                    int val8 = bilas.getText().trim().isEmpty() ? 0 : Integer.parseInt(bilas.getText().trim());

//                    double val9 = IWL.getText().trim().isEmpty() ? 0 : Double.parseDouble(IWL.getText().trim());
                    double total = val1 + val2 + val3 + val4 + val5 + val6 + val7 + val8;

                    if (total % 1 == 0) {
                        Output.setText(String.valueOf((int) total));
                    } else {
                        Output.setText(String.format("%.2f", total));
                    }

                } catch (NumberFormatException ex) {
                    Output.setText("0");
                }
            }
        });
        drain1.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                try {
                    int val1 = muntah.getText().trim().isEmpty() ? 0 : Integer.parseInt(muntah.getText().trim());
                    int val2 = feses.getText().trim().isEmpty() ? 0 : Integer.parseInt(feses.getText().trim());
                    int val3 = urine.getText().trim().isEmpty() ? 0 : Integer.parseInt(urine.getText().trim());
                    int val4 = ongt.getText().trim().isEmpty() ? 0 : Integer.parseInt(ongt.getText().trim());
                    int val5 = darah.getText().trim().isEmpty() ? 0 : Integer.parseInt(darah.getText().trim());
                    int val6 = drain.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain.getText().trim());
                    int val7 = drain1.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain1.getText().trim());
                    int val8 = bilas.getText().trim().isEmpty() ? 0 : Integer.parseInt(bilas.getText().trim());

//                    double val9 = IWL.getText().trim().isEmpty() ? 0 : Double.parseDouble(IWL.getText().trim());
                    double total = val1 + val2 + val3 + val4 + val5 + val6 + val7 + val8;

                    if (total % 1 == 0) {
                        Output.setText(String.valueOf((int) total));
                    } else {
                        Output.setText(String.format("%.2f", total));
                    }

                } catch (NumberFormatException ex) {
                    Output.setText("0");
                }
            }
        });
        bilas.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                try {
                    int val1 = muntah.getText().trim().isEmpty() ? 0 : Integer.parseInt(muntah.getText().trim());
                    int val2 = feses.getText().trim().isEmpty() ? 0 : Integer.parseInt(feses.getText().trim());
                    int val3 = urine.getText().trim().isEmpty() ? 0 : Integer.parseInt(urine.getText().trim());
                    int val4 = ongt.getText().trim().isEmpty() ? 0 : Integer.parseInt(ongt.getText().trim());
                    int val5 = darah.getText().trim().isEmpty() ? 0 : Integer.parseInt(darah.getText().trim());
                    int val6 = drain.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain.getText().trim());
                    int val7 = drain1.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain1.getText().trim());
                    int val8 = bilas.getText().trim().isEmpty() ? 0 : Integer.parseInt(bilas.getText().trim());

//                    double val9 = IWL.getText().trim().isEmpty() ? 0 : Double.parseDouble(IWL.getText().trim());
                    double total = val1 + val2 + val3 + val4 + val5 + val6 + val7 + val8;

                    if (total % 1 == 0) {
                        Output.setText(String.valueOf((int) total));
                    } else {
                        Output.setText(String.format("%.2f", total));
                    }

                } catch (NumberFormatException ex) {
                    Output.setText("0");
                }
            }
        });

//        IWL.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
//            public void changedUpdate(DocumentEvent e) {
//                sync();
//            }
//
//            public void removeUpdate(DocumentEvent e) {
//                sync();
//            }
//
//            public void insertUpdate(DocumentEvent e) {
//                sync();
//            }
//
//            public void sync() {
//                try {
//                    int val1 = muntah.getText().trim().isEmpty() ? 0 : Integer.parseInt(muntah.getText().trim());
//                    int val2 = feses.getText().trim().isEmpty() ? 0 : Integer.parseInt(feses.getText().trim());
//                    int val3 = urine.getText().trim().isEmpty() ? 0 : Integer.parseInt(urine.getText().trim());
//                    int val4 = ongt.getText().trim().isEmpty() ? 0 : Integer.parseInt(ongt.getText().trim());
//                    int val5 = darah.getText().trim().isEmpty() ? 0 : Integer.parseInt(darah.getText().trim());
//                    int val6 = drain.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain.getText().trim());
//                    int val7 = drain1.getText().trim().isEmpty() ? 0 : Integer.parseInt(drain1.getText().trim());
//                    int val8 = bilas.getText().trim().isEmpty() ? 0 : Integer.parseInt(bilas.getText().trim());
//
//                    double val9 = IWL.getText().trim().isEmpty() ? 0 : Double.parseDouble(IWL.getText().trim());
//                    double total = val1 + val2 + val3 + val4 + val5 + val6 + val7 + val8 + val9;
//
//                    if (total % 1 == 0) {
//                        Output.setText(String.valueOf((int) total));
//                    } else {
//                        Output.setText(String.format("%.2f", total));
//                    }
//
//                } catch (NumberFormatException ex) {
//                    Output.setText("0");
//                }
//            }
//        });
        bb.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                try {
                    int v1 = bb.getText().trim().isEmpty() ? 0 : Integer.parseInt(bb.getText().trim());
                    int v2 = IWL1.getText().trim().isEmpty() ? 0 : Integer.parseInt(IWL1.getText().trim());

                    double iwl24Jam = v1 * v2;
//                    double iwlPerShift = (iwl24Jam / 24.0) * 8.0;

                    // Cek apakah angka bulat
                    if (iwl24Jam % 1 == 0) {
                        IWL.setText(String.valueOf((int) iwl24Jam));
                    } else {
                        IWL.setText(String.format("%.2f", iwl24Jam));
                    }
                } catch (NumberFormatException ex) {
                    IWL.setText("0");
                }
            }
        });

        IWL1.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                sync();
            }

            public void removeUpdate(DocumentEvent e) {
                sync();
            }

            public void insertUpdate(DocumentEvent e) {
                sync();
            }

            public void sync() {
                try {
                    int v1 = bb.getText().trim().isEmpty() ? 0 : Integer.parseInt(bb.getText().trim());
                    int v2 = IWL1.getText().trim().isEmpty() ? 0 : Integer.parseInt(IWL1.getText().trim());

                    double iwl24Jam = v1 * v2;
//                    double iwlPerShift = (iwl24Jam / 24.0) * 8.0;

                    // Cek apakah angka bulat
                    if (iwl24Jam % 1 == 0) {
                        IWL.setText(String.valueOf((int) iwl24Jam));
                    } else {
                        IWL.setText(String.format("%.2f", iwl24Jam));
                    }
                } catch (NumberFormatException ex) {
                    IWL.setText("0");
                }
            }
        });
//        Tintake.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
//            public void changedUpdate(DocumentEvent e) {
//                sync();
//            }
//
//            public void removeUpdate(DocumentEvent e) {
//                sync();
//            }
//
//            public void insertUpdate(DocumentEvent e) {
//                sync();
//            }
//
//            public void sync() {
//                try {
//                    double intake = Tintake.getText().trim().isEmpty() ? 0 : Double.parseDouble(Tintake.getText().trim());
//                    double output = Output.getText().trim().isEmpty() ? 0 : Double.parseDouble(Output.getText().trim());
//
//                    double balances = intake - output;
//                    String result;
//                    if (balances > 0) {
//                        // Cek apakah angka bulat
//                        if (balances % 1 == 0) {
//                            result = "+" + (int) balances;
//                        } else {
//                            result = "+" + String.format("%.2f", balances);
//                        }
//                    } else if (balances < 0) {
//                        // Cek apakah angka bulat
//                        if (balances % 1 == 0) {
//                            result = String.valueOf((int) balances); // Sudah ada tanda minus
//                        } else {
//                            result = String.format("%.2f", balances); // Sudah ada tanda minus
//                        }
//                    } else {
//                        result = "0";
//                    }
//
//                    balance.setText(result);
//
//                } catch (NumberFormatException ex) {
//                    balance.setText("0");
//                }
//            }
//        });
//        Output.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
//            public void changedUpdate(DocumentEvent e) {
//                sync();
//            }
//
//            public void removeUpdate(DocumentEvent e) {
//                sync();
//            }
//
//            public void insertUpdate(DocumentEvent e) {
//                sync();
//            }
//
//            public void sync() {
//                try {
//                    double intake = Tintake.getText().trim().isEmpty() ? 0 : Double.parseDouble(Tintake.getText().trim());
//                    double outtake = Output.getText().trim().isEmpty() ? 0 : Double.parseDouble(Output.getText().trim());
//
//                    double balances = intake - outtake;
//                    String result;
//                    if (balances > 0) {
//                        // Cek apakah angka bulat
//                        if (balances % 1 == 0) {
//                            result = "+" + (int) balances;
//                        } else {
//                            result = "+" + String.format("%.2f", balances);
//                        }
//                    } else if (balances < 0) {
//                        // Cek apakah angka bulat
//                        if (balances % 1 == 0) {
//                            result = String.valueOf((int) balances); // Sudah ada tanda minus
//                        } else {
//                            result = String.format("%.2f", balances); // Sudah ada tanda minus
//                        }
//                    } else {
//                        result = "0";
//                    }
//
//                    balance.setText(result);
//
//                } catch (NumberFormatException ex) {
//                    balance.setText("0");
//                }
//            }
//        });

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
        MnCatatanKeseimbanganCairan = new javax.swing.JMenuItem();
        JK = new widget.TextBox();
        Umur = new widget.TextBox();
        TanggalRegistrasi = new widget.TextBox();
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
        NIP = new widget.TextBox();
        NamaPetugas = new widget.TextBox();
        btnPetugas = new widget.Button();
        jLabel8 = new widget.Label();
        TglLahir = new widget.TextBox();
        jLabel12 = new widget.Label();
        Infus = new widget.TextBox();
        ngt = new widget.TextBox();
        jLabel20 = new widget.Label();
        jLabel23 = new widget.Label();
        minum = new widget.TextBox();
        jLabel17 = new widget.Label();
        jLabel22 = new widget.Label();
        ongt = new widget.TextBox();
        drain = new widget.TextBox();
        urine = new widget.TextBox();
        jLabel13 = new widget.Label();
        jLabel25 = new widget.Label();
        jSeparator2 = new javax.swing.JSeparator();
        jLabel26 = new widget.Label();
        IWL = new widget.TextBox();
        jLabel27 = new widget.Label();
        jLabel29 = new widget.Label();
        Keterangan = new widget.TextBox();
        Nngt = new widget.TextBox();
        Line1 = new widget.TextBox();
        Nminum = new widget.TextBox();
        jLabel30 = new widget.Label();
        jLabel31 = new widget.Label();
        Output = new widget.TextBox();
        Tintake = new widget.TextBox();
        umur = new widget.Label();
        jLabel32 = new widget.Label();
        jLabel33 = new widget.Label();
        jLabel14 = new widget.Label();
        Infus1 = new widget.TextBox();
        Line2 = new widget.TextBox();
        jLabel15 = new widget.Label();
        Infus2 = new widget.TextBox();
        Line3 = new widget.TextBox();
        jLabel35 = new widget.Label();
        muntah = new widget.TextBox();
        awal1 = new widget.TextBox();
        awal2 = new widget.TextBox();
        awal3 = new widget.TextBox();
        jLabel38 = new widget.Label();
        feses = new widget.TextBox();
        jLabel28 = new widget.Label();
        darah = new widget.TextBox();
        jLabel39 = new widget.Label();
        jLabel40 = new widget.Label();
        drain1 = new widget.TextBox();
        jLabel41 = new widget.Label();
        IWL1 = new widget.TextBox();
        bilas = new widget.TextBox();
        jLabel42 = new widget.Label();
        jLabel43 = new widget.Label();
        bb = new widget.TextBox();
        jSeparator4 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        balance = new widget.Label();
        jLabel34 = new widget.Label();
        shift = new widget.ComboBox();
        jLabel37 = new widget.Label();
        balance1 = new widget.TextBox();
        ChkInput = new widget.CekBox();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

        MnCatatanKeseimbanganCairan.setBackground(new java.awt.Color(255, 255, 254));
        MnCatatanKeseimbanganCairan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        MnCatatanKeseimbanganCairan.setForeground(new java.awt.Color(50, 50, 50));
        MnCatatanKeseimbanganCairan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        MnCatatanKeseimbanganCairan.setText("Formulir Catatan Keseimbangan Cairan");
        MnCatatanKeseimbanganCairan.setName("MnCatatanKeseimbanganCairan"); // NOI18N
        MnCatatanKeseimbanganCairan.setPreferredSize(new java.awt.Dimension(230, 26));
        MnCatatanKeseimbanganCairan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MnCatatanKeseimbanganCairanActionPerformed(evt);
            }
        });
        jPopupMenu1.add(MnCatatanKeseimbanganCairan);

        JK.setHighlighter(null);
        JK.setName("JK"); // NOI18N

        Umur.setHighlighter(null);
        Umur.setName("Umur"); // NOI18N

        TanggalRegistrasi.setHighlighter(null);
        TanggalRegistrasi.setName("TanggalRegistrasi"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Monitoring Cairan ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
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
        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "27-02-2026" }));
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
        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "27-02-2026" }));
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
        TCari.setPreferredSize(new java.awt.Dimension(310, 23));
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
        PanelInput.setPreferredSize(new java.awt.Dimension(192, 174));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        FormInput.setBackground(new java.awt.Color(250, 255, 245));
        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 274));
        FormInput.setLayout(null);

        jLabel4.setText("No.Rawat :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(0, 10, 70, 23);

        TNoRw.setHighlighter(null);
        TNoRw.setName("TNoRw"); // NOI18N
        TNoRw.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TNoRwKeyPressed(evt);
            }
        });
        FormInput.add(TNoRw);
        TNoRw.setBounds(74, 10, 136, 23);

        TPasien.setEditable(false);
        TPasien.setHighlighter(null);
        TPasien.setName("TPasien"); // NOI18N
        TPasien.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TPasienKeyPressed(evt);
            }
        });
        FormInput.add(TPasien);
        TPasien.setBounds(326, 10, 295, 23);

        Tanggal.setForeground(new java.awt.Color(50, 70, 50));
        Tanggal.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "27-02-2026" }));
        Tanggal.setDisplayFormat("dd-MM-yyyy");
        Tanggal.setName("Tanggal"); // NOI18N
        Tanggal.setOpaque(false);
        Tanggal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TanggalKeyPressed(evt);
            }
        });
        FormInput.add(Tanggal);
        Tanggal.setBounds(74, 40, 90, 23);

        TNoRM.setEditable(false);
        TNoRM.setHighlighter(null);
        TNoRM.setName("TNoRM"); // NOI18N
        FormInput.add(TNoRM);
        TNoRM.setBounds(212, 10, 112, 23);

        jLabel16.setText("Tanggal :");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel16);
        jLabel16.setBounds(0, 40, 70, 23);

        Jam.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23" }));
        Jam.setName("Jam"); // NOI18N
        Jam.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                JamKeyPressed(evt);
            }
        });
        FormInput.add(Jam);
        Jam.setBounds(168, 40, 62, 23);

        Menit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Menit.setName("Menit"); // NOI18N
        Menit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                MenitKeyPressed(evt);
            }
        });
        FormInput.add(Menit);
        Menit.setBounds(233, 40, 62, 23);

        Detik.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59" }));
        Detik.setName("Detik"); // NOI18N
        Detik.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DetikKeyPressed(evt);
            }
        });
        FormInput.add(Detik);
        Detik.setBounds(300, 40, 62, 23);

        ChkKejadian.setBorder(null);
        ChkKejadian.setSelected(true);
        ChkKejadian.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        ChkKejadian.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        ChkKejadian.setName("ChkKejadian"); // NOI18N
        FormInput.add(ChkKejadian);
        ChkKejadian.setBounds(360, 40, 23, 23);

        jLabel18.setText("Petugas :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(400, 40, 70, 23);

        NIP.setEditable(false);
        NIP.setHighlighter(null);
        NIP.setName("NIP"); // NOI18N
        NIP.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NIPKeyPressed(evt);
            }
        });
        FormInput.add(NIP);
        NIP.setBounds(474, 40, 94, 23);

        NamaPetugas.setEditable(false);
        NamaPetugas.setName("NamaPetugas"); // NOI18N
        FormInput.add(NamaPetugas);
        NamaPetugas.setBounds(570, 40, 187, 23);

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
        btnPetugas.setBounds(761, 40, 28, 23);

        jLabel8.setText("Tgl.Lahir :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(625, 10, 60, 23);

        TglLahir.setHighlighter(null);
        TglLahir.setName("TglLahir"); // NOI18N
        FormInput.add(TglLahir);
        TglLahir.setBounds(689, 10, 100, 23);

        jLabel12.setText("IV LINE 1 :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(20, 140, 62, 23);

        Infus.setFocusTraversalPolicyProvider(true);
        Infus.setName("Infus"); // NOI18N
        Infus.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                InfusCaretPositionChanged(evt);
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                InfusInputMethodTextChanged(evt);
            }
        });
        Infus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                InfusKeyPressed(evt);
            }
        });
        FormInput.add(Infus);
        Infus.setBounds(360, 140, 50, 23);

        ngt.setFocusTraversalPolicyProvider(true);
        ngt.setName("ngt"); // NOI18N
        ngt.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                ngtInputMethodTextChanged(evt);
            }
        });
        ngt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ngtKeyPressed(evt);
            }
        });
        FormInput.add(ngt);
        ngt.setBounds(360, 260, 50, 23);

        jLabel20.setText("NGT :");
        jLabel20.setName("jLabel20"); // NOI18N
        FormInput.add(jLabel20);
        jLabel20.setBounds(0, 260, 80, 23);

        jLabel23.setText("Oral/minum :");
        jLabel23.setName("jLabel23"); // NOI18N
        FormInput.add(jLabel23);
        jLabel23.setBounds(-10, 230, 90, 23);

        minum.setFocusTraversalPolicyProvider(true);
        minum.setName("minum"); // NOI18N
        minum.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                minumInputMethodTextChanged(evt);
            }
        });
        minum.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                minumKeyPressed(evt);
            }
        });
        FormInput.add(minum);
        minum.setBounds(360, 230, 50, 23);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel17.setText("INPUT");
        jLabel17.setFont(new java.awt.Font("Roboto", 1, 12)); // NOI18N
        jLabel17.setName("jLabel17"); // NOI18N
        jLabel17.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel17);
        jLabel17.setBounds(30, 110, 70, 23);

        jLabel22.setText("NGT :");
        jLabel22.setName("jLabel22"); // NOI18N
        FormInput.add(jLabel22);
        jLabel22.setBounds(450, 230, 60, 23);

        ongt.setFocusTraversalPolicyProvider(true);
        ongt.setName("ongt"); // NOI18N
        ongt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                ongtKeyPressed(evt);
            }
        });
        FormInput.add(ongt);
        ongt.setBounds(520, 230, 50, 23);

        drain.setFocusTraversalPolicyProvider(true);
        drain.setName("drain"); // NOI18N
        drain.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                drainKeyPressed(evt);
            }
        });
        FormInput.add(drain);
        drain.setBounds(650, 140, 50, 23);

        urine.setFocusTraversalPolicyProvider(true);
        urine.setName("urine"); // NOI18N
        urine.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                urineKeyPressed(evt);
            }
        });
        FormInput.add(urine);
        urine.setBounds(520, 200, 50, 23);

        jLabel13.setText("Urine :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(410, 200, 90, 23);

        jLabel25.setText("OUTPUT");
        jLabel25.setFont(new java.awt.Font("Roboto", 1, 12)); // NOI18N
        jLabel25.setName("jLabel25"); // NOI18N
        jLabel25.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel25);
        jLabel25.setBounds(460, 120, 50, 23);

        jSeparator2.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator2.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator2.setName("jSeparator2"); // NOI18N
        FormInput.add(jSeparator2);
        jSeparator2.setBounds(0, 70, 800, 1);

        jLabel26.setText("IWL :");
        jLabel26.setName("jLabel26"); // NOI18N
        FormInput.add(jLabel26);
        jLabel26.setBounds(550, 80, 40, 23);

        IWL.setFocusTraversalPolicyProvider(true);
        IWL.setName("IWL"); // NOI18N
        IWL.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                IWLKeyPressed(evt);
            }
        });
        FormInput.add(IWL);
        IWL.setBounds(710, 80, 50, 23);

        jLabel27.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel27.setText("Balance Cairan : ");
        jLabel27.setFont(new java.awt.Font("Roboto", 1, 12)); // NOI18N
        jLabel27.setName("jLabel27"); // NOI18N
        jLabel27.setVerifyInputWhenFocusTarget(false);
        FormInput.add(jLabel27);
        jLabel27.setBounds(710, 200, 100, 23);

        jLabel29.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel29.setText("Masuk");
        jLabel29.setName("jLabel29"); // NOI18N
        FormInput.add(jLabel29);
        jLabel29.setBounds(350, 120, 70, 16);

        Keterangan.setFocusTraversalPolicyProvider(true);
        Keterangan.setName("Keterangan"); // NOI18N
        Keterangan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KeteranganKeyPressed(evt);
            }
        });
        FormInput.add(Keterangan);
        Keterangan.setBounds(240, 80, 180, 23);

        Nngt.setText("-");
        Nngt.setFocusTraversalPolicyProvider(true);
        Nngt.setName("Nngt"); // NOI18N
        Nngt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NngtKeyPressed(evt);
            }
        });
        FormInput.add(Nngt);
        Nngt.setBounds(90, 260, 200, 23);

        Line1.setFocusTraversalPolicyProvider(true);
        Line1.setName("Line1"); // NOI18N
        Line1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Line1KeyPressed(evt);
            }
        });
        FormInput.add(Line1);
        Line1.setBounds(90, 140, 200, 23);

        Nminum.setText("-");
        Nminum.setFocusTraversalPolicyProvider(true);
        Nminum.setName("Nminum"); // NOI18N
        Nminum.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NminumKeyPressed(evt);
            }
        });
        FormInput.add(Nminum);
        Nminum.setBounds(90, 230, 200, 23);

        jLabel30.setText("Skema Infus :");
        jLabel30.setName("jLabel30"); // NOI18N
        FormInput.add(jLabel30);
        jLabel30.setBounds(160, 80, 80, 23);

        jLabel31.setText("Nama Cairan");
        jLabel31.setName("jLabel31"); // NOI18N
        FormInput.add(jLabel31);
        jLabel31.setBounds(130, 120, 100, 23);

        Output.setFocusTraversalPolicyProvider(true);
        Output.setName("Output"); // NOI18N
        Output.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                OutputKeyPressed(evt);
            }
        });
        FormInput.add(Output);
        Output.setBounds(810, 170, 50, 23);

        Tintake.setFocusTraversalPolicyProvider(true);
        Tintake.setName("Tintake"); // NOI18N
        Tintake.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                TintakeActionPerformed(evt);
            }
        });
        Tintake.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TintakeKeyPressed(evt);
            }
        });
        FormInput.add(Tintake);
        Tintake.setBounds(810, 140, 50, 23);

        umur.setForeground(new java.awt.Color(0, 0, 0));
        umur.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        umur.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        umur.setName("umur"); // NOI18N
        FormInput.add(umur);
        umur.setBounds(800, 10, 100, 20);

        jLabel32.setText("Drain I :");
        jLabel32.setName("jLabel32"); // NOI18N
        FormInput.add(jLabel32);
        jLabel32.setBounds(590, 140, 50, 23);

        jLabel33.setText("Total Intake : ");
        jLabel33.setName("jLabel33"); // NOI18N
        FormInput.add(jLabel33);
        jLabel33.setBounds(720, 140, 80, 23);

        jLabel14.setText("IV LINE 2 :");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(20, 170, 62, 23);

        Infus1.setFocusTraversalPolicyProvider(true);
        Infus1.setName("Infus1"); // NOI18N
        Infus1.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                Infus1InputMethodTextChanged(evt);
            }
        });
        Infus1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Infus1KeyPressed(evt);
            }
        });
        FormInput.add(Infus1);
        Infus1.setBounds(360, 170, 50, 23);

        Line2.setText("-");
        Line2.setFocusTraversalPolicyProvider(true);
        Line2.setName("Line2"); // NOI18N
        Line2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Line2KeyPressed(evt);
            }
        });
        FormInput.add(Line2);
        Line2.setBounds(90, 170, 200, 23);

        jLabel15.setText("IV LINE 3 :");
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(20, 200, 62, 23);

        Infus2.setFocusTraversalPolicyProvider(true);
        Infus2.setName("Infus2"); // NOI18N
        Infus2.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                Infus2InputMethodTextChanged(evt);
            }
        });
        Infus2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Infus2KeyPressed(evt);
            }
        });
        FormInput.add(Infus2);
        Infus2.setBounds(360, 200, 50, 23);

        Line3.setText("-");
        Line3.setFocusTraversalPolicyProvider(true);
        Line3.setName("Line3"); // NOI18N
        Line3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Line3KeyPressed(evt);
            }
        });
        FormInput.add(Line3);
        Line3.setBounds(90, 200, 200, 23);

        jLabel35.setText("Muntah :");
        jLabel35.setName("jLabel35"); // NOI18N
        FormInput.add(jLabel35);
        jLabel35.setBounds(450, 140, 60, 23);

        muntah.setFocusTraversalPolicyProvider(true);
        muntah.setName("muntah"); // NOI18N
        muntah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                muntahKeyPressed(evt);
            }
        });
        FormInput.add(muntah);
        muntah.setBounds(520, 140, 50, 23);

        awal1.setFocusTraversalPolicyProvider(true);
        awal1.setName("awal1"); // NOI18N
        awal1.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                awal1CaretPositionChanged(evt);
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                awal1InputMethodTextChanged(evt);
            }
        });
        awal1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                awal1KeyPressed(evt);
            }
        });
        FormInput.add(awal1);
        awal1.setBounds(300, 140, 50, 23);

        awal2.setFocusTraversalPolicyProvider(true);
        awal2.setName("awal2"); // NOI18N
        awal2.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                awal2InputMethodTextChanged(evt);
            }
        });
        awal2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                awal2KeyPressed(evt);
            }
        });
        FormInput.add(awal2);
        awal2.setBounds(300, 170, 50, 23);

        awal3.setFocusTraversalPolicyProvider(true);
        awal3.setName("awal3"); // NOI18N
        awal3.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
                awal3InputMethodTextChanged(evt);
            }
        });
        awal3.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                awal3KeyPressed(evt);
            }
        });
        FormInput.add(awal3);
        awal3.setBounds(300, 200, 50, 23);

        jLabel38.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel38.setText("Awal");
        jLabel38.setName("jLabel38"); // NOI18N
        FormInput.add(jLabel38);
        jLabel38.setBounds(290, 120, 70, 16);

        feses.setFocusTraversalPolicyProvider(true);
        feses.setName("feses"); // NOI18N
        feses.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                fesesKeyPressed(evt);
            }
        });
        FormInput.add(feses);
        feses.setBounds(520, 170, 50, 23);

        jLabel28.setText("Feses :");
        jLabel28.setName("jLabel28"); // NOI18N
        FormInput.add(jLabel28);
        jLabel28.setBounds(410, 170, 90, 23);

        darah.setFocusTraversalPolicyProvider(true);
        darah.setName("darah"); // NOI18N
        darah.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                darahKeyPressed(evt);
            }
        });
        FormInput.add(darah);
        darah.setBounds(650, 230, 50, 23);

        jLabel39.setText("Perdarahan :");
        jLabel39.setName("jLabel39"); // NOI18N
        FormInput.add(jLabel39);
        jLabel39.setBounds(560, 230, 80, 23);

        jLabel40.setText("Drain II :");
        jLabel40.setName("jLabel40"); // NOI18N
        FormInput.add(jLabel40);
        jLabel40.setBounds(590, 170, 50, 23);

        drain1.setFocusTraversalPolicyProvider(true);
        drain1.setName("drain1"); // NOI18N
        drain1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                drain1KeyPressed(evt);
            }
        });
        FormInput.add(drain1);
        drain1.setBounds(650, 170, 50, 23);

        jLabel41.setText("T.IWL : ");
        jLabel41.setFont(new java.awt.Font("Roboto", 1, 12)); // NOI18N
        jLabel41.setName("jLabel41"); // NOI18N
        FormInput.add(jLabel41);
        jLabel41.setBounds(650, 80, 60, 23);

        IWL1.setFocusTraversalPolicyProvider(true);
        IWL1.setName("IWL1"); // NOI18N
        IWL1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                IWL1KeyPressed(evt);
            }
        });
        FormInput.add(IWL1);
        IWL1.setBounds(590, 80, 50, 23);

        bilas.setFocusTraversalPolicyProvider(true);
        bilas.setName("bilas"); // NOI18N
        bilas.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                bilasKeyPressed(evt);
            }
        });
        FormInput.add(bilas);
        bilas.setBounds(650, 200, 50, 23);

        jLabel42.setText("Bilas :");
        jLabel42.setName("jLabel42"); // NOI18N
        FormInput.add(jLabel42);
        jLabel42.setBounds(560, 200, 80, 23);

        jLabel43.setText("BB :");
        jLabel43.setName("jLabel43"); // NOI18N
        FormInput.add(jLabel43);
        jLabel43.setBounds(440, 80, 40, 23);

        bb.setFocusTraversalPolicyProvider(true);
        bb.setName("bb"); // NOI18N
        bb.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                bbKeyPressed(evt);
            }
        });
        FormInput.add(bb);
        bb.setBounds(490, 80, 50, 23);

        jSeparator4.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator4.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator4.setName("jSeparator4"); // NOI18N
        FormInput.add(jSeparator4);
        jSeparator4.setBounds(420, 100, 1, 200);

        jSeparator5.setBackground(new java.awt.Color(239, 244, 234));
        jSeparator5.setForeground(new java.awt.Color(239, 244, 234));
        jSeparator5.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(239, 244, 234)));
        jSeparator5.setName("jSeparator5"); // NOI18N
        FormInput.add(jSeparator5);
        jSeparator5.setBounds(710, 100, 1, 200);

        balance.setForeground(new java.awt.Color(0, 0, 0));
        balance.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        balance.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        balance.setName("balance"); // NOI18N
        FormInput.add(balance);
        balance.setBounds(810, 230, 60, 20);

        jLabel34.setText("Shift ke : ");
        jLabel34.setName("jLabel34"); // NOI18N
        FormInput.add(jLabel34);
        jLabel34.setBounds(30, 80, 50, 23);

        shift.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Pagi", "Siang", "Malam" }));
        shift.setName("shift"); // NOI18N
        shift.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                shiftKeyPressed(evt);
            }
        });
        FormInput.add(shift);
        shift.setBounds(80, 80, 80, 23);

        jLabel37.setText("Total Output : ");
        jLabel37.setName("jLabel37"); // NOI18N
        FormInput.add(jLabel37);
        jLabel37.setBounds(720, 170, 80, 23);

        balance1.setFocusTraversalPolicyProvider(true);
        balance1.setName("balance1"); // NOI18N
        balance1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                balance1KeyPressed(evt);
            }
        });
        FormInput.add(balance1);
        balance1.setBounds(810, 200, 50, 23);

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
        } else if (NIP.getText().trim().equals("") || NamaPetugas.getText().trim().equals("")) {
            Valid.textKosong(NIP, "Petugas");
        } else if (Infus.getText().trim().equals("")) {
            Valid.textKosong(Infus, "Infus");
        } else if (minum.getText().trim().equals("")) {
            Valid.textKosong(minum, "Tranfusi");
        } else {
            simpan();
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnSimpanActionPerformed(null);
        } else {
            Valid.pindah(evt, Keterangan, BtnBatal);
        }
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
        ChkInput.setSelected(true);
        isForm();
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
                if (NIP.getText().equals(tbObat.getValueAt(tbObat.getSelectedRow(), 35).toString())) {
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

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if (TNoRw.getText().trim().equals("") || TPasien.getText().trim().equals("")) {
            Valid.textKosong(TNoRw, "pasien");
        } else if (NIP.getText().trim().equals("") || NamaPetugas.getText().trim().equals("")) {
            Valid.textKosong(NIP, "Petugas");
        } else if (Infus.getText().trim().equals("")) {
            Valid.textKosong(Infus, "Infus");
        } else if (minum.getText().trim().equals("")) {
            Valid.textKosong(minum, "Tranfusi");
        } else if (ngt.getText().trim().equals("")) {
            Valid.textKosong(ngt, "Minum");
        } else if (urine.getText().trim().equals("")) {
            Valid.textKosong(urine, "Urine/Feses");
        } else if (drain.getText().trim().equals("")) {
            Valid.textKosong(drain, "Drain");
        } else if (ongt.getText().trim().equals("")) {
            Valid.textKosong(ongt, "NGT/Oral");
        } else if (IWL.getText().trim().equals("")) {
            Valid.textKosong(IWL, "IWL");

        } else {
            if (tbObat.getSelectedRow() > -1) {
                ganti();
            } else {
                JOptionPane.showMessageDialog(rootPane, "Silahkan anda pilih data terlebih dahulu..!!");
            }
        }
}//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnEditActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnHapus, BtnPrint);
        }
}//GEN-LAST:event_BtnEditKeyPressed

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
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (tabMode.getRowCount() == 0) {
            JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnBatal.requestFocus();
        } else if (tabMode.getRowCount() != 0) {
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));

            if (TCari.getText().trim().equals("")) {
                Valid.MyReportqry("rptDataCatatanKeseimbanganCairan.jasper", "report", "::[ Data Catatan Keseimbangan Cairan ]::",
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,"
                        + "pasien.jk,pasien.tgl_lahir,catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat,catatan_keseimbangan_cairan.infus,"
                        + "catatan_keseimbangan_cairan.tranfusi,catatan_keseimbangan_cairan.minum,catatan_keseimbangan_cairan.urine,catatan_keseimbangan_cairan.drain,"
                        + "catatan_keseimbangan_cairan.ngt,catatan_keseimbangan_cairan.iwl,catatan_keseimbangan_cairan.keseimbangan,catatan_keseimbangan_cairan.keterangan,"
                        + "catatan_keseimbangan_cairan.nip,petugas.nama "
                        + "from catatan_keseimbangan_cairan inner join reg_periksa on catatan_keseimbangan_cairan.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join petugas on catatan_keseimbangan_cairan.nip=petugas.nip where "
                        + "catatan_keseimbangan_cairan.tgl_perawatan between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' order by catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat", param);
            } else {
                Valid.MyReportqry("rptDataCatatanKeseimbanganCairan.jasper", "report", "::[ Data Catatan Keseimbangan Cairan ]::",
                        "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,"
                        + "pasien.jk,pasien.tgl_lahir,catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat,catatan_keseimbangan_cairan.infus,"
                        + "catatan_keseimbangan_cairan.tranfusi,catatan_keseimbangan_cairan.minum,catatan_keseimbangan_cairan.urine,catatan_keseimbangan_cairan.drain,"
                        + "catatan_keseimbangan_cairan.ngt,catatan_keseimbangan_cairan.iwl,catatan_keseimbangan_cairan.keseimbangan,catatan_keseimbangan_cairan.keterangan,"
                        + "catatan_keseimbangan_cairan.nip,petugas.nama "
                        + "from catatan_keseimbangan_cairan inner join reg_periksa on catatan_keseimbangan_cairan.no_rawat=reg_periksa.no_rawat "
                        + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                        + "inner join petugas on catatan_keseimbangan_cairan.nip=petugas.nip where "
                        + "catatan_keseimbangan_cairan.tgl_perawatan between '" + Valid.SetTgl(DTPCari1.getSelectedItem() + "") + "' and '" + Valid.SetTgl(DTPCari2.getSelectedItem() + "") + "' and "
                        + "(reg_periksa.no_rawat like '%" + TCari.getText().trim() + "%' or pasien.no_rkm_medis like '%" + TCari.getText().trim() + "%' or "
                        + "pasien.nm_pasien like '%" + TCari.getText().trim() + "%' or catatan_keseimbangan_cairan.nip like '%" + TCari.getText().trim() + "%' or petugas.nama like '%" + TCari.getText().trim() + "%') "
                        + "order by catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat ", param);
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnPrintActionPerformed(null);
        } else {
            Valid.pindah(evt, BtnEdit, BtnKeluar);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

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

    private void NIPKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NIPKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            NamaPetugas.setText(petugas.tampil3(NIP.getText()));
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            Detik.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            Infus.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            btnPetugasActionPerformed(null);
        }
    }//GEN-LAST:event_NIPKeyPressed

    private void btnPetugasActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPetugasActionPerformed
        petugas.emptTeks();
        petugas.isCek();
        petugas.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        petugas.setLocationRelativeTo(internalFrame1);
        petugas.setVisible(true);
    }//GEN-LAST:event_btnPetugasActionPerformed

    private void btnPetugasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnPetugasKeyPressed
        Valid.pindah(evt, Detik, Infus);
    }//GEN-LAST:event_btnPetugasKeyPressed

    private void MnCatatanKeseimbanganCairanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MnCatatanKeseimbanganCairanActionPerformed
        if (tbObat.getSelectedRow() > -1) {
            Map<String, Object> param = new HashMap<>();
            param.put("namars", akses.getnamars());
            param.put("alamatrs", akses.getalamatrs());
            param.put("kotars", akses.getkabupatenrs());
            param.put("propinsirs", akses.getpropinsirs());
            param.put("kontakrs", akses.getkontakrs());
            param.put("emailrs", akses.getemailrs());
            param.put("logo", Sequel.cariGambar("select setting.logo from setting"));
            Valid.MyReportqry("rptFormulirCatatanKeseimbanganCairan.jasper", "report", "::[ Formulir Catatan Keseimbangan Cairan ]::",
                    "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,reg_periksa.umurdaftar,reg_periksa.sttsumur,reg_periksa.tgl_registrasi,reg_periksa.jam_reg,"
                    + "pasien.jk,pasien.tgl_lahir,catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat,catatan_keseimbangan_cairan.infus,"
                    + "catatan_keseimbangan_cairan.tranfusi,catatan_keseimbangan_cairan.minum,catatan_keseimbangan_cairan.urine,catatan_keseimbangan_cairan.drain,"
                    + "catatan_keseimbangan_cairan.ngt,catatan_keseimbangan_cairan.iwl,catatan_keseimbangan_cairan.keseimbangan,catatan_keseimbangan_cairan.keterangan,"
                    + "catatan_keseimbangan_cairan.nip,petugas.nama "
                    + "from catatan_keseimbangan_cairan inner join reg_periksa on catatan_keseimbangan_cairan.no_rawat=reg_periksa.no_rawat "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "inner join petugas on catatan_keseimbangan_cairan.nip=petugas.nip where reg_periksa.no_rawat='" + tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString() + "' order by catatan_keseimbangan_cairan.tgl_perawatan,catatan_keseimbangan_cairan.jam_rawat", param);
        }
    }//GEN-LAST:event_MnCatatanKeseimbanganCairanActionPerformed

    private void InfusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_InfusKeyPressed
        Valid.pindah(evt, Infus, Infus1);

    }//GEN-LAST:event_InfusKeyPressed

    private void ngtKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ngtKeyPressed
        Valid.pindah(evt, ngt, Tintake);

    }//GEN-LAST:event_ngtKeyPressed

    private void minumKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minumKeyPressed
        Valid.pindah(evt, minum, ngt);

    }//GEN-LAST:event_minumKeyPressed

    private void ongtKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_ongtKeyPressed
        Valid.pindah(evt, ongt, muntah);

    }//GEN-LAST:event_ongtKeyPressed

    private void drainKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_drainKeyPressed
        Valid.pindah(evt, drain, ongt);

    }//GEN-LAST:event_drainKeyPressed

    private void urineKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_urineKeyPressed
        Valid.pindah(evt, urine, drain);

    }//GEN-LAST:event_urineKeyPressed

    private void IWLKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_IWLKeyPressed
        Valid.pindah(evt, IWL, Output);
    }//GEN-LAST:event_IWLKeyPressed

    private void KeteranganKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KeteranganKeyPressed

    }//GEN-LAST:event_KeteranganKeyPressed

    private void NngtKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NngtKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NngtKeyPressed

    private void Line1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Line1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Line1KeyPressed

    private void NminumKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NminumKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_NminumKeyPressed

    private void OutputKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_OutputKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_OutputKeyPressed

    private void TintakeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TintakeKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_TintakeKeyPressed

    private void Infus1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Infus1KeyPressed
        Valid.pindah(evt, Infus1, Infus2);

    }//GEN-LAST:event_Infus1KeyPressed

    private void Line2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Line2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Line2KeyPressed

    private void Infus2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Infus2KeyPressed
        Valid.pindah(evt, Infus2, minum);

    }//GEN-LAST:event_Infus2KeyPressed

    private void Line3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Line3KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_Line3KeyPressed

    private void muntahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_muntahKeyPressed
        Valid.pindah(evt, muntah, IWL);

    }//GEN-LAST:event_muntahKeyPressed

    private void InfusInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_InfusInputMethodTextChanged

    }//GEN-LAST:event_InfusInputMethodTextChanged

    private void Infus1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_Infus1InputMethodTextChanged

    }//GEN-LAST:event_Infus1InputMethodTextChanged

    private void Infus2InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_Infus2InputMethodTextChanged

    }//GEN-LAST:event_Infus2InputMethodTextChanged

    private void minumInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_minumInputMethodTextChanged

    }//GEN-LAST:event_minumInputMethodTextChanged

    private void ngtInputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_ngtInputMethodTextChanged

    }//GEN-LAST:event_ngtInputMethodTextChanged

    private void InfusCaretPositionChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_InfusCaretPositionChanged

    }//GEN-LAST:event_InfusCaretPositionChanged

    private void TintakeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_TintakeActionPerformed

    }//GEN-LAST:event_TintakeActionPerformed

    private void awal1CaretPositionChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_awal1CaretPositionChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_awal1CaretPositionChanged

    private void awal1InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_awal1InputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_awal1InputMethodTextChanged

    private void awal1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_awal1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_awal1KeyPressed

    private void awal2InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_awal2InputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_awal2InputMethodTextChanged

    private void awal2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_awal2KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_awal2KeyPressed

    private void awal3InputMethodTextChanged(java.awt.event.InputMethodEvent evt) {//GEN-FIRST:event_awal3InputMethodTextChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_awal3InputMethodTextChanged

    private void awal3KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_awal3KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_awal3KeyPressed

    private void fesesKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fesesKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_fesesKeyPressed

    private void darahKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_darahKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_darahKeyPressed

    private void drain1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_drain1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_drain1KeyPressed

    private void IWL1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_IWL1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_IWL1KeyPressed

    private void bilasKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_bilasKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_bilasKeyPressed

    private void bbKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_bbKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_bbKeyPressed

    private void shiftKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_shiftKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_shiftKeyPressed

    private void balance1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_balance1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_balance1KeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            RMDataCatatanKeseimbanganCairan dialog = new RMDataCatatanKeseimbanganCairan(new javax.swing.JFrame(), true);
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
    private widget.TextBox IWL;
    private widget.TextBox IWL1;
    private widget.TextBox Infus;
    private widget.TextBox Infus1;
    private widget.TextBox Infus2;
    private widget.TextBox JK;
    private widget.ComboBox Jam;
    private widget.TextBox Keterangan;
    private widget.Label LCount;
    private widget.TextBox Line1;
    private widget.TextBox Line2;
    private widget.TextBox Line3;
    private widget.ComboBox Menit;
    private javax.swing.JMenuItem MnCatatanKeseimbanganCairan;
    private widget.TextBox NIP;
    private widget.TextBox NamaPetugas;
    private widget.TextBox Nminum;
    private widget.TextBox Nngt;
    private widget.TextBox Output;
    private javax.swing.JPanel PanelInput;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.TextBox TNoRM;
    private widget.TextBox TNoRw;
    private widget.TextBox TPasien;
    private widget.Tanggal Tanggal;
    private widget.TextBox TanggalRegistrasi;
    private widget.TextBox TglLahir;
    private widget.TextBox Tintake;
    private widget.TextBox Umur;
    private widget.TextBox awal1;
    private widget.TextBox awal2;
    private widget.TextBox awal3;
    private widget.Label balance;
    private widget.TextBox balance1;
    private widget.TextBox bb;
    private widget.TextBox bilas;
    private widget.Button btnPetugas;
    private widget.TextBox darah;
    private widget.TextBox drain;
    private widget.TextBox drain1;
    private widget.TextBox feses;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel14;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel19;
    private widget.Label jLabel20;
    private widget.Label jLabel21;
    private widget.Label jLabel22;
    private widget.Label jLabel23;
    private widget.Label jLabel25;
    private widget.Label jLabel26;
    private widget.Label jLabel27;
    private widget.Label jLabel28;
    private widget.Label jLabel29;
    private widget.Label jLabel30;
    private widget.Label jLabel31;
    private widget.Label jLabel32;
    private widget.Label jLabel33;
    private widget.Label jLabel34;
    private widget.Label jLabel35;
    private widget.Label jLabel37;
    private widget.Label jLabel38;
    private widget.Label jLabel39;
    private widget.Label jLabel4;
    private widget.Label jLabel40;
    private widget.Label jLabel41;
    private widget.Label jLabel42;
    private widget.Label jLabel43;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private widget.TextBox minum;
    private widget.TextBox muntah;
    private widget.TextBox ngt;
    private widget.TextBox ongt;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.ComboBox shift;
    private widget.Table tbObat;
    private widget.Label umur;
    private widget.TextBox urine;
    // End of variables declaration//GEN-END:variables

    public void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            if (TCari.getText().trim().equals("")) {
                ps = koneksi.prepareStatement(
                        "SELECT\n"
                        + " pasien.no_rkm_medis,\n"
                        + " pasien.nm_pasien,\n"
                        + " reg_periksa.umurdaftar,\n"
                        + " reg_periksa.sttsumur,\n"
                        + " pasien.jk,\n"
                        + " pasien.tgl_lahir,\n"
                        + " catatan_keseimbangan_cairan.tgl_perawatan,\n"
                        + " catatan_keseimbangan_cairan.jam_rawat,\n"
                        + " catatan_keseimbangan_cairan.infus,\n"
                        + " catatan_keseimbangan_cairan.tranfusi,\n"
                        + " catatan_keseimbangan_cairan.minum,\n"
                        + " catatan_keseimbangan_cairan.urine,\n"
                        + " catatan_keseimbangan_cairan.drain,\n"
                        + " catatan_keseimbangan_cairan.ngt,\n"
                        + " catatan_keseimbangan_cairan.iwl,\n"
                        + " catatan_keseimbangan_cairan.keseimbangan,\n"
                        + " catatan_keseimbangan_cairan.keterangan,\n"
                        + " catatan_keseimbangan_cairan.nip,\n"
                        + " petugas.nama,\n"
                        + " catatan_keseimbangan_cairan.line1,\n"
                        + " catatan_keseimbangan_cairan.line2,\n"
                        + " catatan_keseimbangan_cairan.line3,\n"
                        + " catatan_keseimbangan_cairan.ntransfusi,\n"
                        + " catatan_keseimbangan_cairan.nminum,\n"
                        + " catatan_keseimbangan_cairan.infus1,\n"
                        + " catatan_keseimbangan_cairan.infus2,\n"
                        + " catatan_keseimbangan_cairan.intake,\n"
                        + " catatan_keseimbangan_cairan.output,\n"
                        + " catatan_keseimbangan_cairan.muntah,\n"
                        + " catatan_keseimbangan_cairan.feses,\n"
                        + " catatan_keseimbangan_cairan.drain1,\n"
                        + " catatan_keseimbangan_cairan.darah,\n"
                        + " catatan_keseimbangan_cairan.bilas,\n"
                        + " catatan_keseimbangan_cairan.shift,\n"
                        + " catatan_keseimbangan_cairan.awal1,\n"
                        + " catatan_keseimbangan_cairan.awal2,\n"
                        + " catatan_keseimbangan_cairan.awal3,\n"
                        + " catatan_keseimbangan_cairan.no_rawat \n"
                        + "FROM\n"
                        + " catatan_keseimbangan_cairan\n"
                        + " INNER JOIN reg_periksa ON catatan_keseimbangan_cairan.no_rawat = reg_periksa.no_rawat\n"
                        + " INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis\n"
                        + " INNER JOIN petugas ON catatan_keseimbangan_cairan.nip = petugas.nip where "
                        + "catatan_keseimbangan_cairan.tgl_perawatan between ? and ? "
                        + "order by catatan_keseimbangan_cairan.no_rawat, catatan_keseimbangan_cairan.tgl_perawatan, catatan_keseimbangan_cairan.jam_rawat");
            } else {
                ps = koneksi.prepareStatement(
                        "SELECT\n"
                        + " pasien.no_rkm_medis,\n"
                        + " pasien.nm_pasien,\n"
                        + " reg_periksa.umurdaftar,\n"
                        + " reg_periksa.sttsumur,\n"
                        + " pasien.jk,\n"
                        + " pasien.tgl_lahir,\n"
                        + " catatan_keseimbangan_cairan.tgl_perawatan,\n"
                        + " catatan_keseimbangan_cairan.jam_rawat,\n"
                        + " catatan_keseimbangan_cairan.infus,\n"
                        + " catatan_keseimbangan_cairan.tranfusi,\n"
                        + " catatan_keseimbangan_cairan.minum,\n"
                        + " catatan_keseimbangan_cairan.urine,\n"
                        + " catatan_keseimbangan_cairan.drain,\n"
                        + " catatan_keseimbangan_cairan.ngt,\n"
                        + " catatan_keseimbangan_cairan.iwl,\n"
                        + " catatan_keseimbangan_cairan.keseimbangan,\n"
                        + " catatan_keseimbangan_cairan.keterangan,\n"
                        + " catatan_keseimbangan_cairan.nip,\n"
                        + " petugas.nama,\n"
                        + " catatan_keseimbangan_cairan.line1,\n"
                        + " catatan_keseimbangan_cairan.line2,\n"
                        + " catatan_keseimbangan_cairan.line3,\n"
                        + " catatan_keseimbangan_cairan.ntransfusi,\n"
                        + " catatan_keseimbangan_cairan.nminum,\n"
                        + " catatan_keseimbangan_cairan.infus1,\n"
                        + " catatan_keseimbangan_cairan.infus2,\n"
                        + " catatan_keseimbangan_cairan.intake,\n"
                        + " catatan_keseimbangan_cairan.output,\n"
                        + " catatan_keseimbangan_cairan.muntah,\n"
                        + " catatan_keseimbangan_cairan.feses,\n"
                        + " catatan_keseimbangan_cairan.drain1,\n"
                        + " catatan_keseimbangan_cairan.darah,\n"
                        + " catatan_keseimbangan_cairan.bilas,\n"
                        + " catatan_keseimbangan_cairan.shift,\n"
                        + " catatan_keseimbangan_cairan.awal1,\n"
                        + " catatan_keseimbangan_cairan.awal2,\n"
                        + " catatan_keseimbangan_cairan.awal3,\n"
                        + " catatan_keseimbangan_cairan.no_rawat \n"
                        + "FROM\n"
                        + " catatan_keseimbangan_cairan\n"
                        + " INNER JOIN reg_periksa ON catatan_keseimbangan_cairan.no_rawat = reg_periksa.no_rawat\n"
                        + " INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis\n"
                        + " INNER JOIN petugas ON catatan_keseimbangan_cairan.nip = petugas.nip where "
                        + "catatan_keseimbangan_cairan.tgl_perawatan between ? and ? and "
                        + "(reg_periksa.no_rawat like ? or pasien.no_rkm_medis like ? or pasien.nm_pasien like ?) "
                        + "order by catatan_keseimbangan_cairan.no_rawat, catatan_keseimbangan_cairan.tgl_perawatan, catatan_keseimbangan_cairan.jam_rawat ");
            }

            try {
                if (TCari.getText().trim().equals("")) {
                    ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                    ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                } else {
                    ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + "") + " 00:00:00");
                    ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + "") + " 23:59:59");
                    ps.setString(3, "%" + TCari.getText() + "%");
                    ps.setString(4, "%" + TCari.getText() + "%");
                    ps.setString(5, "%" + TCari.getText() + "%");
                }

                rs = ps.executeQuery();
                while (rs.next()) {
                    String currentNoRawat = rs.getString("no_rawat");
                    String currentDate = rs.getString("tgl_perawatan");

                    java.math.BigDecimal iwlValue = rs.getBigDecimal("iwl");
                    java.math.BigDecimal balanceValue = rs.getBigDecimal("keseimbangan");
                    java.math.BigDecimal intakeValue = rs.getBigDecimal("intake");
                    java.math.BigDecimal outputValue = rs.getBigDecimal("output");

//                    String iwlFormatted = iwlValue != null ? String.format("%.2f", iwlValue) : "0.00";
//                    String balanceFormatted = balanceValue != null ? String.format("%.2f", balanceValue) : "0.00";
//                    String outputFormatted = outputValue != null ? String.format("%.2f", outputValue) : "0.00";
                    tabMode.addRow(new Object[]{
                        currentNoRawat, // No.Rawat
                        rs.getString("no_rkm_medis"), // No.R.M.
                        rs.getString("nm_pasien"), // Nama Pasien
                        rs.getString("umurdaftar") + " " + rs.getString("sttsumur"), // Umur
                        rs.getString("jk"), // JK
                        rs.getString("tgl_lahir"), // Tgl.Lahir
                        currentDate, // Tanggal
                        rs.getString("jam_rawat"), // Jam
                        rs.getString("shift"), // Shift Ke
                        rs.getString("keterangan"), // Keterangan
                        rs.getString("line1"), // Nama Cairan 1
                        rs.getString("awal1"), // Cairan Awal 1
                        rs.getString("infus"), // IV Line I
                        rs.getString("line2"), // Nama Cairan 2
                        rs.getString("awal2"), // Cairan Awal 2
                        rs.getString("infus1"), // IV Line II
                        rs.getString("line3"), // Nama Cairan 3
                        rs.getString("awal3"), // Cairan Awal 3
                        rs.getString("infus2"), // IV Line III

                        rs.getString("nminum"), // Nama Cairan 5
                        rs.getString("minum"), // Minum
                        rs.getString("ntransfusi"), // Nama Cairan 4
                        rs.getString("tranfusi"), // Tranfusi
                        rs.getString("intake"), // TOTAL INTAKE
                        rs.getString("urine"), // Urine/Feses
                        rs.getString("muntah"), // Muntah
                        rs.getString("feses"), // Feses
                        rs.getString("ngt"), // NGT/Oral
                        rs.getString("darah"), // Darah
                        rs.getString("drain"), // Drain 1
                        rs.getString("drain1"), // Drain 2
                        rs.getString("bilas"), // Bilas
                        //                        iwlFormatted, // IWL
                        //                        balanceFormatted, // Keseimbangan Cairan
                        //                        outputFormatted, // TOTAL OUTPUT (
                        rs.getString("iwl"), // Drain 1
                        rs.getString("keseimbangan"), // Drain 2
                        rs.getString("output"), // Bilas
                        rs.getString("nip"), // NIP
                        rs.getString("nama") // Nama Petugas
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
                e.printStackTrace();
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (SQLException e) {
            System.out.println("Notifikasi : " + e);
        }

        LCount.setText("" + tabMode.getRowCount());
    }

    public void emptTeks() {
        Infus.setText("0");
        minum.setText("0");
        ngt.setText("0");
        Infus1.setText("0");
        Infus2.setText("0");
        urine.setText("0");
        drain.setText("0");
        drain1.setText("0");
        IWL1.setText("0");
        ongt.setText("0");
        feses.setText("0");
        darah.setText("0");
        bilas.setText("0");
        Tintake.setText("0");
        Output.setText("0");
        Line1.setText("");
        Line2.setText("");
        Line3.setText("");
        Nminum.setText("");
        Nngt.setText("");
        muntah.setText("0");
        Keterangan.setText("");
        awal1.setText(Sequel.cariIsi(""));
        Tanggal.setDate(new Date());
        Infus.requestFocus();
    }

    private void getData() {
        if (tbObat.getSelectedRow() != -1) {
            // Hindari mengambil data dari row "TOTAL 24 JAM"
            String noRawat = tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString();
            if (noRawat.equals("→→→→→") || noRawat.equals("──────")
                    || noRawat.equals("════════") || noRawat.contains("TOTAL")) {
                JOptionPane.showMessageDialog(null, "Tidak dapat mengedit data total!");
                return;
            }

            TNoRw.setText(noRawat);
            TNoRM.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 1).toString());
            TPasien.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 2).toString());
            Umur.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 3).toString());
            JK.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 4).toString());
            TglLahir.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 5).toString());
            Valid.SetTgl(Tanggal, tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString());
            String jamValue = tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString();
            if (jamValue.contains(":")) {
                Jam.setSelectedItem(jamValue.substring(0, 2));
                Menit.setSelectedItem(jamValue.substring(3, 5));
                Detik.setSelectedItem(jamValue.substring(6, 8));
            }

            if (shift != null) {
                shift.setSelectedItem(tbObat.getValueAt(tbObat.getSelectedRow(), 8).toString());
            }
            Keterangan.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 9).toString());
            Line1.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 10).toString());  // Nama Cairan 1
            awal1.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 11).toString());  // Cairan Awal 1
            Infus.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 12).toString());  // IV Line I
            Line2.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 13).toString());  // Nama Cairan 2
            awal2.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 14).toString());  // Cairan Awal 2
            Infus1.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 15).toString()); // IV Line II
            Line3.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 16).toString());  // Nama Cairan 3
            awal3.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 17).toString());  // Cairan Awal 3
            Infus2.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 18).toString()); // IV Line III
            Nminum.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 19).toString()); // Nama Cairan 4
            minum.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 20).toString());  // Minum
            Nngt.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 21).toString());   // Nama Cairan 5
            ngt.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 22).toString());    // Tranfusi
            Tintake.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 23).toString()); // TOTAL INTAKKE
            urine.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 24).toString());   // Urine/Feses
            muntah.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 25).toString());  // Muntah
            feses.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 26).toString());   // Feses
            ongt.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 27).toString());    // NGT/Oral
            darah.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 28).toString());   // Darah
            drain.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 29).toString());   // Drain 1
            drain1.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 30).toString());  // Drain 2
            bilas.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 31).toString());   // Bilas
            IWL1.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 32).toString());     // IWL
            balance1.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 33).toString()); // Keseimbangan Cairan
            Output.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 34).toString()); // TOTAL OUTPUT
            NIP.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 35).toString());     // NIP
            NamaPetugas.setText(tbObat.getValueAt(tbObat.getSelectedRow(), 36).toString()); // Nama Petugas
        }
    }

    private void isRawat() {
        try {
            ps = koneksi.prepareStatement(
                    "select reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.jk,pasien.tgl_lahir,reg_periksa.tgl_registrasi,reg_periksa.umurdaftar,"
                    + "reg_periksa.sttsumur,reg_periksa.jam_reg,  CONCAT(SUBSTRING_INDEX(pasien.umur, ' ', 1), ' Th') AS umur_tahun from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat=?");
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
                    TanggalRegistrasi.setText(rs.getString("tgl_registrasi") + " " + rs.getString("jam_reg"));
                    umur.setText(rs.getString("umur_tahun"));
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
        setAutoIWL();
        bb.setText(Sequel.cariIsi("SELECT pemeriksaan_ranap.berat FROM pemeriksaan_ranap WHERE pemeriksaan_ranap.no_rawat = '" + norwt + "' ORDER BY pemeriksaan_ranap.no_rawat DESC LIMIT 1"));

    }

    private void isForm() {
        if (ChkInput.isSelected() == true) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 304));
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
        BtnSimpan.setEnabled(akses.getcatatan_observasi_ranap());
        BtnHapus.setEnabled(akses.getcatatan_observasi_ranap());
        BtnEdit.setEnabled(akses.getcatatan_observasi_ranap());
        BtnPrint.setEnabled(akses.getcatatan_observasi_ranap());
        if (akses.getjml2() >= 1) {
            NIP.setEditable(false);
            btnPetugas.setEnabled(false);
            NIP.setText(akses.getkode());
            NamaPetugas.setText(petugas.tampil3(NIP.getText()));
            if (NamaPetugas.getText().equals("")) {
                NIP.setText("");
                JOptionPane.showMessageDialog(null, "User login bukan petugas...!!");
            }
        }

        if (TANGGALMUNDUR.equals("no")) {
            if (!akses.getkode().equals("Admin Utama")) {
                Tanggal.setEditable(false);
                Tanggal.setEnabled(false);
                ChkKejadian.setEnabled(false);
                Jam.setEnabled(false);
                Menit.setEnabled(false);
                Detik.setEnabled(false);
            }
        }
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
        String balanceValue = balance1.getText().trim();
        if (balanceValue.startsWith("+")) {
            balanceValue = balanceValue.substring(1);
        }
        if (balanceValue.isEmpty()) {
            balanceValue = "0";
        }

        String iwlValue = IWL1.getText().trim();
        if (iwlValue.isEmpty()) {
            iwlValue = "0";
        }

        String intakeValue = Tintake.getText().trim();
        if (intakeValue.isEmpty()) {
            intakeValue = "0";
        }

        String outputValue = Output.getText().trim();
        if (outputValue.isEmpty()) {
            outputValue = "0";
        }

        String ngtValue = ongt.getText().trim();
        if (ngtValue.isEmpty()) {
            ngtValue = "0";
        }

        String keteranganValue = Keterangan.getText().trim();
        if (keteranganValue.isEmpty()) {
            keteranganValue = "-";
        }

        String tranfusiValue = ngt.getText().trim();
        if (keteranganValue.isEmpty()) {
            keteranganValue = "-";
        }

        if (Sequel.mengedittf("catatan_keseimbangan_cairan",
                "tgl_perawatan=? and jam_rawat=? and no_rawat=?",
                "no_rawat=?,tgl_perawatan=?,jam_rawat=?,nip=?, shift=?, keterangan=?, " +
                "iwl=?, line1=?, line2=?, line3=?, nminum=?, ntransfusi=?, " +
                "awal1=?, awal2=?, awal3=?, infus=?, infus1=?, infus2=?, minum=?, " +
                "tranfusi=?, muntah=?, feses=?, urine=?, ngt=?, drain=?, drain1=?, " +
                "bilas=?, darah=?, intake=?, output=?, keseimbangan=?",
                34, new String[]{
                    TNoRw.getText(), // 1. no_rawat
                    Valid.SetTgl(Tanggal.getSelectedItem() + ""), // 2. tgl_perawatan
                    Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(), // 3. jam_rawat
                    NIP.getText(), // 4. nip
                    shift.getSelectedItem().toString(), // 5. shift
                    keteranganValue, // 6. keterangan
                    iwlValue, // 7. iwl
                    Line1.getText(), // 8. line1
                    Line2.getText(), // 9. line2
                    Line3.getText(), // 10. line3
                    Nminum.getText(), // 11. nminum
                    Nngt.getText(), // 12. ntransfusi 
                    awal1.getText(), // 13. awal1
                    awal2.getText(), // 14. awal2
                    awal3.getText(), // 15. awal3
                    Infus.getText(), // 16. infus
                    Infus1.getText(), // 17. infus1
                    Infus2.getText(), // 18. infus2
                    minum.getText(), // 19. minum
                    tranfusiValue, // 20. tranfusi (Textbox ngt = Tranfusi di UI)
                    muntah.getText(), // 21. muntah
                    feses.getText(), // 22. feses
                    urine.getText(), // 23. urine
                    ngtValue, // 24. ngt (Textbox ongt = NGT/Oral di UI)
                    drain.getText(), // 25. drain
                    drain1.getText(), // 26. drain1
                    bilas.getText(), // 27. bilas
                    darah.getText(), // 28. darah
                    intakeValue, // 29. intake
                    outputValue, // 30. output
                    balanceValue, // 31. keseimbangan
                    
                    // Key pencarian WHERE
                    tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString(), // tgl_perawatan lama
                    tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString(), // jam_rawat lama
                    tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()  // no_rawat lama
                }
        ) == true) {
            tampil();
            emptTeks();
        }
    }

    private void hapus() {
        if (Sequel.queryu2tf("delete from catatan_keseimbangan_cairan where tgl_perawatan=? and jam_rawat=? and no_rawat=?", 3, new String[]{
            tbObat.getValueAt(tbObat.getSelectedRow(), 6).toString(), tbObat.getValueAt(tbObat.getSelectedRow(), 7).toString(), tbObat.getValueAt(tbObat.getSelectedRow(), 0).toString()
        }) == true) {
            tabMode.removeRow(tbObat.getSelectedRow());
            LCount.setText("" + tabMode.getRowCount());
            emptTeks();
        } else {
            JOptionPane.showMessageDialog(null, "Gagal menghapus..!!");
        }
    }

    private void simpan() {
        try {
            String balanceValue = balance1.getText().trim();
            if (balanceValue.startsWith("+")) {
                balanceValue = balanceValue.substring(1);
            }
            if (balanceValue.isEmpty()) {
                balanceValue = "0";
            }

            String iwlValue = IWL1.getText().trim();
            if (iwlValue.isEmpty()) {
                iwlValue = "0";
            }

            String intakeValue = Tintake.getText().trim();
            if (intakeValue.isEmpty()) {
                intakeValue = "0";
            }

            String outputValue = Output.getText().trim();
            if (outputValue.isEmpty()) {
                outputValue = "0";
            }

            String ngtValue = ongt.getText().trim();
            if (ngtValue.isEmpty()) {
                ngtValue = "0";
            }

            String keteranganValue = Keterangan.getText().trim();
            if (keteranganValue.isEmpty()) {
                keteranganValue = "-";
            }

            String tranfusiValue = ngt.getText().trim();
            if (keteranganValue.isEmpty()) {
                keteranganValue = "-";
            }

            // Susun parameter WAJIB URUT sesuai dengan struktur fisik Database yang baru (31 Kolom)
            String[] parameters = new String[]{
                TNoRw.getText(), // 1. no_rawat
                Valid.SetTgl(Tanggal.getSelectedItem() + ""), // 2. tgl_perawatan
                Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(), // 3. jam_rawat
                NIP.getText(), // 4. nip
                shift.getSelectedItem().toString(), // 5. shift
                keteranganValue, // 6. keterangan
                iwlValue, // 7. iwl
                Line1.getText(), // 8. line1
                Line2.getText(), // 9. line2
                Line3.getText(), // 10. line3
                Nminum.getText(), // 11. nminum
                Nngt.getText(), // 12. ntransfusi 
                awal1.getText(), // 13. awal1
                awal2.getText(), // 14. awal2
                awal3.getText(), // 15. awal3
                Infus.getText(), // 16. infus
                Infus1.getText(), // 17. infus1
                Infus2.getText(), // 18. infus2
                minum.getText(), // 19. minum
                tranfusiValue, // 20. tranfusi (Textbox ngt = Tranfusi di UI)
                muntah.getText(), // 21. muntah
                feses.getText(), // 22. feses
                urine.getText(), // 23. urine
                ngtValue, // 24. ngt (Textbox ongt = NGT/Oral di UI)
                drain.getText(), // 25. drain
                drain1.getText(), // 26. drain1
                bilas.getText(), // 27. bilas
                darah.getText(), // 28. darah
                intakeValue, // 29. intake
                outputValue, // 30. output
                balanceValue // 31. keseimbangan
            };

            if (TNoRw.getText().isEmpty() || NIP.getText().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Error: No. Rawat atau NIP tidak boleh kosong!");
                return;
            }

            if (Sequel.menyimpantf("catatan_keseimbangan_cairan",
                    "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?",
                    "Data", 31, parameters) == true) {

                tabMode.addRow(new String[]{
                    TNoRw.getText(),
                    TNoRM.getText(),
                    TPasien.getText(),
                    Umur.getText(),
                    JK.getText(),
                    TglLahir.getText(),
                    Valid.SetTgl(Tanggal.getSelectedItem() + ""),
                    Jam.getSelectedItem() + ":" + Menit.getSelectedItem() + ":" + Detik.getSelectedItem(),
                    shift.getSelectedItem().toString(),
                    keteranganValue,
                    Line1.getText(),
                    awal1.getText(),
                    Infus.getText(),
                    Line2.getText(),
                    awal2.getText(),
                    Infus1.getText(),
                    Line3.getText(),
                    awal3.getText(),
                    Infus2.getText(),
                    Nminum.getText(), // nminum ✅
                    minum.getText(),
                    Nngt.getText(), // ntransfusi ✅
                    ngtValue,
                    intakeValue,
                    urine.getText(),
                    muntah.getText(),
                    feses.getText(),
                    ongt.getText(),
                    darah.getText(),
                    drain.getText(),
                    drain1.getText(),
                    bilas.getText(),
                    iwlValue,
                    balanceValue,
                    outputValue,
                    NIP.getText(),
                    NamaPetugas.getText()
                });

                LCount.setText("" + tabMode.getRowCount());
                emptTeks();
                tampil();

                JOptionPane.showMessageDialog(null, "Data berhasil disimpan!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setAutoIWL() {
        try {
            String umur = Sequel.cariIsi("SELECT CONCAT(SUBSTRING_INDEX(pasien.umur, ' ', 1)) AS umur_tahun "
                    + "FROM pasien "
                    + "INNER JOIN reg_periksa ON pasien.no_rkm_medis = reg_periksa.no_rkm_medis "
                    + "WHERE reg_periksa.no_rawat = '" + TNoRw.getText() + "'");

            if (!umur.isEmpty()) {
                int umurInt = Integer.parseInt(umur);

                if (umurInt <= 18) {
                    IWL1.setText("25");
                } else if (umurInt >= 19) {
                    IWL1.setText("10");
                }

            }
        } catch (Exception e) {
            System.out.println("Error setting auto IWL: " + e);
        }
    }
}
