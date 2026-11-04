package keuangan;

import fungsi.KalkulatorJasaMedisDokter;
import fungsi.WarnaTable;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 * Dialog Popup Rincian Breakdown Jasa Medis Dokter BPJS.
 * Menampilkan audit transparansi dan pencocokan asal-usul nominal JM BPJS
 * dalam 1 SEP utuh (seluruh dokter yang bertugas) secara rinci dan terstruktur.
 * 
 * @author it-rsib & AI Engineering Team
 */
public class DlgRincianJMBPJS extends javax.swing.JDialog {

    private final DefaultTableModel tabMode;
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();

    private widget.InternalFrame internalFrame1;
    private widget.panelisi panelHeader;
    private widget.panelisi panelBawah;
    private widget.ScrollPane scrollPane;
    private widget.Table tbRincian;
    private widget.Label lblInfoPasien1;
    private widget.Label lblInfoPasien2;
    private widget.Label lblInfoPasien3;
    private widget.Label lblTotalRS;
    private widget.Label lblTotalJM;
    private widget.Button BtnCopy;
    private widget.Button BtnKeluar;

    // Cache teks penjelasan untuk fitur Salin ke Clipboard
    private String teksSalinanClipboard = "";

    /**
     * Data item penampung dokter dalam 1 SEP
     */
    public static class DokterSepItem {
        public String kdDokter = "";
        public String nmDokter = "";
        public double totalPiutang = 0.0;
        public double totalTarifJm = 0.0;
        public double umbal = 0.0;
        public double jmBpjs = 0.0;
        public String keterangan = "";
        public String statusDokter = "";
        public String statusOperasi = "";
        public String jenisPct = "";
        public String visitLos = "";

        public DokterSepItem(String kdDokter, String nmDokter, double totalPiutang, double totalTarifJm,
                             double umbal, double jmBpjs, String keterangan, String statusDokter,
                             String statusOperasi, String jenisPct, String visitLos) {
            this.kdDokter = kdDokter;
            this.nmDokter = nmDokter;
            this.totalPiutang = totalPiutang;
            this.totalTarifJm = totalTarifJm;
            this.umbal = umbal;
            this.jmBpjs = jmBpjs;
            this.keterangan = keterangan;
            this.statusDokter = statusDokter;
            this.statusOperasi = statusOperasi;
            this.jenisPct = jenisPct;
            this.visitLos = visitLos;
        }
    }

    public DlgRincianJMBPJS(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponentsCustom();

        tabMode = new DefaultTableModel(null, new Object[]{
            "No.", "Nama Dokter", "Peran / Status", "Komponen Layanan / Tindakan", "Acuan Aturan Master",
            "Ketentuan / Formula Porsi", "Tarif RS Normal", "JM BPJS Diterima", "Keterangan Klinis"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }

            Class[] types = new Class[]{
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.Double.class, java.lang.Double.class, java.lang.String.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        tbRincian.setModel(tabMode);
        tbRincian.setPreferredScrollableViewportSize(new Dimension(1050, 300));
        tbRincian.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = {35, 170, 110, 190, 160, 140, 110, 110, 230};
        for (int i = 0; i < widths.length; i++) {
            TableColumn col = tbRincian.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
        }
        tbRincian.setDefaultRenderer(Object.class, new WarnaTable());
    }

    private void initComponentsCustom() {
        setUndecorated(true);
        setResizable(false);
        setSize(1080, 520);

        internalFrame1 = new widget.InternalFrame();
        internalFrame1.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(240, 245, 235)),
                "::[ Rincian Perhitungan Jasa Medis Dokter BPJS (1 SEP Utuh) ]::",
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                new Font("Tahoma", Font.BOLD, 12), new Color(50, 50, 50)
        ));
        internalFrame1.setLayout(new BorderLayout(1, 1));

        // 1. PANEL HEADER INFORMASI
        panelHeader = new widget.panelisi();
        panelHeader.setPreferredSize(new Dimension(1070, 75));
        panelHeader.setLayout(new GridLayout(3, 1, 2, 2));

        lblInfoPasien1 = new widget.Label();
        lblInfoPasien1.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblInfoPasien1.setText(" Pasien : - | No. Rawat : - | No. SEP : - | No. RM : -");

        lblInfoPasien2 = new widget.Label();
        lblInfoPasien2.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblInfoPasien2.setText(" Status Rawat : - | Total Klaim INA-CBG : Rp 0 | Total Piutang RS : Rp 0 | Dokter Terlibat : 0 Dokter");

        lblInfoPasien3 = new widget.Label();
        lblInfoPasien3.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblInfoPasien3.setForeground(new Color(40, 120, 40));
        lblInfoPasien3.setText(" Audit Transparansi & Pencocokan Pembagian JM 1 SEP");

        panelHeader.add(lblInfoPasien1);
        panelHeader.add(lblInfoPasien2);
        panelHeader.add(lblInfoPasien3);
        internalFrame1.add(panelHeader, BorderLayout.PAGE_START);

        // 2. TABEL RINCIAN KOMPONEN
        tbRincian = new widget.Table();
        scrollPane = new widget.ScrollPane();
        scrollPane.setViewportView(tbRincian);
        internalFrame1.add(scrollPane, BorderLayout.CENTER);

        // 3. PANEL BAWAH (RINGKASAN & TOMBOL)
        panelBawah = new widget.panelisi();
        panelBawah.setPreferredSize(new Dimension(1070, 45));
        panelBawah.setLayout(new FlowLayout(FlowLayout.RIGHT, 6, 6));

        lblTotalRS = new widget.Label();
        lblTotalRS.setFont(new Font("Tahoma", Font.BOLD, 11));
        lblTotalRS.setText("Total Tarif RS: Rp 0   |   ");

        lblTotalJM = new widget.Label();
        lblTotalJM.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblTotalJM.setForeground(new Color(180, 40, 40));
        lblTotalJM.setText("TOTAL JM BPJS (1 SEP): Rp 0     ");

        BtnCopy = new widget.Button();
        BtnCopy.setIcon(new ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCopy.setMnemonic('C');
        BtnCopy.setText("Salin Teks");
        BtnCopy.setToolTipText("Alt+C (Salin Rincian Perhitungan 1 SEP ke Clipboard)");
        BtnCopy.setPreferredSize(new Dimension(110, 30));
        BtnCopy.addActionListener((ActionEvent evt) -> {
            salinKeClipboard();
        });

        BtnKeluar = new widget.Button();
        BtnKeluar.setIcon(new ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Tutup");
        BtnKeluar.setToolTipText("Alt+K / Esc (Tutup Jendela)");
        BtnKeluar.setPreferredSize(new Dimension(90, 30));
        BtnKeluar.addActionListener((ActionEvent evt) -> {
            dispose();
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                }
            }
        });

        panelBawah.add(lblTotalRS);
        panelBawah.add(lblTotalJM);
        panelBawah.add(BtnCopy);
        panelBawah.add(BtnKeluar);
        internalFrame1.add(panelBawah, BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1);
        pack();
    }

    /**
     * Menampilkan rincian pembagian Jasa Medis untuk SELURUH dokter dalam 1 SEP yang sama.
     */
    public void tampilkanRincianSep(
            String noRawat, String noSep, String noRm, String nmPasien, String statusRawat,
            double totalPiutang, double umbal, List<DokterSepItem> listDokter
    ) {
        Valid.tabelKosong(tabMode);

        lblInfoPasien1.setText(" Pasien : " + nmPasien + " (RM: " + noRm + ")  |  No. Rawat : " + noRawat + "  |  No. SEP : " + (noSep.isEmpty() ? "-" : noSep));
        lblInfoPasien2.setText(" Status : " + statusRawat + "  |  Total Klaim INA-CBG : Rp " + Valid.SetAngka(umbal) + "  |  Piutang RS : Rp " + Valid.SetAngka(totalPiutang) + "  |  Dokter Terlibat : " + listDokter.size() + " Dokter");
        lblInfoPasien3.setText(" Audit Transparansi & Pencocokan Pembagian JM 1 SEP");

        double sumRS = 0;
        double sumJM = 0;
        int no = 1;

        StringBuilder sbClip = new StringBuilder();
        sbClip.append("========================================================================\n");
        sbClip.append("RINCIAN PERHITUNGAN JASA MEDIS DOKTER BPJS (1 SEP LENGKAP)\n");
        sbClip.append("========================================================================\n");
        sbClip.append("No. Rawat    : ").append(noRawat).append("\n");
        sbClip.append("No. SEP      : ").append(noSep.isEmpty() ? "-" : noSep).append("\n");
        sbClip.append("No. RM       : ").append(noRm).append("\n");
        sbClip.append("Nama Pasien  : ").append(nmPasien).append("\n");
        sbClip.append("Status Rawat : ").append(statusRawat).append("\n");
        sbClip.append("Total Klaim  : Rp ").append(Valid.SetAngka(umbal)).append(" (INA-CBG + TopUp)\n");
        sbClip.append("Total Piutang: Rp ").append(Valid.SetAngka(totalPiutang)).append("\n");
        sbClip.append("Jml Dokter   : ").append(listDokter.size()).append(" Dokter\n");
        sbClip.append("------------------------------------------------------------------------\n");
        sbClip.append("PEMBAGIAN JASA MEDIS PER DOKTER:\n");

        for (DokterSepItem doc : listDokter) {
            List<RincianItem> items;
            if ("Ralan".equalsIgnoreCase(statusRawat)) {
                items = pecahRincianRalan(noRawat, noSep, doc.kdDokter, doc.nmDokter, doc.totalTarifJm, umbal, doc.jmBpjs, doc.keterangan, doc.statusDokter);
            } else {
                items = pecahRincianRanap(noRawat, noSep, doc.kdDokter, doc.nmDokter, doc.totalTarifJm, umbal, doc.jmBpjs, doc.keterangan, doc.statusDokter, doc.statusOperasi, doc.jenisPct, doc.visitLos);
            }

            sbClip.append("[").append(doc.nmDokter).append(" - ").append(doc.statusDokter.isEmpty() ? "Dokter" : doc.statusDokter).append("]\n");

            for (RincianItem it : items) {
                tabMode.addRow(new Object[]{
                    no, it.namaDokter, it.peran, it.namaKomponen, it.acuanAturan, it.formula, it.tarifRS, it.nominalJM, it.rationale
                });

                sbClip.append("  ").append(no).append(". ").append(it.namaKomponen).append("\n");
                sbClip.append("     - Aturan  : ").append(it.acuanAturan).append("\n");
                sbClip.append("     - Porsi   : ").append(it.formula).append("\n");
                sbClip.append("     - Tarif RS: Rp ").append(Valid.SetAngka(it.tarifRS)).append("\n");
                sbClip.append("     - JM BPJS : Rp ").append(Valid.SetAngka(it.nominalJM)).append("\n");
                sbClip.append("     - Ket     : ").append(it.rationale).append("\n");

                sumRS += it.tarifRS;
                sumJM += it.nominalJM;
                no++;
            }
            sbClip.append("\n");
        }

        double sisaRS = umbal - sumJM;

        // Tampilkan total
        lblTotalRS.setText("Total Tarif RS: Rp " + Valid.SetAngka(sumRS) + "   |   ");
        lblTotalJM.setText("TOTAL JM BPJS (1 SEP): Rp " + Valid.SetAngka(sumJM) + "   |   Sisa RS: Rp " + Valid.SetAngka(sisaRS > 0 ? sisaRS : 0) + "     ");

        sbClip.append("------------------------------------------------------------------------\n");
        sbClip.append("TOTAL TARIF RS NORMAL : Rp ").append(Valid.SetAngka(sumRS)).append("\n");
        sbClip.append("TOTAL JM BPJS (1 SEP) : Rp ").append(Valid.SetAngka(sumJM)).append("\n");
        sbClip.append("SISA ALOKASI HAK RS   : Rp ").append(Valid.SetAngka(sisaRS > 0 ? sisaRS : 0)).append("\n");
        sbClip.append("========================================================================\n");
        sbClip.append("Dasar Aturan: Master Ketentuan Bayar JM BPJS RS Indriati Boyolali");

        this.teksSalinanClipboard = sbClip.toString();
        BtnKeluar.requestFocus();
    }

    /**
     * Metode overload backward compatibility untuk single doctor
     */
    public void tampilkanRincian(
            String noRawat, String noSep, String noRm, String nmPasien,
            String kdDokter, String nmDokter, String statusRawat,
            double totalPiutang, double totalTarifJm, double umbal, double jmBpjs,
            String keterangan, String statusDokter, String statusOperasi, String jenisPct, String visitLos
    ) {
        List<DokterSepItem> list = new ArrayList<>();
        list.add(new DokterSepItem(kdDokter, nmDokter, totalPiutang, totalTarifJm, umbal, jmBpjs, keterangan, statusDokter, statusOperasi, jenisPct, visitLos));
        tampilkanRincianSep(noRawat, noSep, noRm, nmPasien, statusRawat, totalPiutang, umbal, list);
    }

    private List<RincianItem> pecahRincianRalan(
            String noRawat, String noSep, String kdDokter, String nmDokter,
            double totalTarifJm, double umbal, double jmBpjs, String keterangan, String statusDokter
    ) {
        List<RincianItem> list = new ArrayList<>();
        String peran = (statusDokter != null && !statusDokter.trim().isEmpty()) ? statusDokter : "Dokter Ralan";

        // 1. Pengecualian Dokter Radiologi / Gigi Bedah Mulut
        if (keterangan.contains("Bebas Aturan") || nmDokter.toLowerCase().contains("sp.rad") || nmDokter.toLowerCase().contains("radiologi") || nmDokter.toLowerCase().contains("sp.bm") || nmDokter.toLowerCase().contains("sp.bmm") || nmDokter.toLowerCase().contains("bedah mulut")) {
            RincianItem it = new RincianItem();
            it.namaDokter = nmDokter;
            it.peran = "Radiologi / Sp.BM";
            it.namaKomponen = "Pelayanan Medis Spesialis (Bebas Aturan BPJS)";
            it.acuanAturan = "PENGECUALIAN_KHUSUS";
            it.formula = "100% Tarif Normal RS";
            it.tarifRS = totalTarifJm;
            it.nominalJM = jmBpjs;
            it.rationale = "Dokter Radiologi & Gigi Bedah Mulut memperoleh tarif normal RS penuh tanpa dikenakan pemotongan BPJS.";
            list.add(it);
            return list;
        }

        // 2. Kasus Hemodialisa Single Use
        if (keterangan.contains("Hemodialisa") || keterangan.contains("Single Use")) {
            RincianItem it = new RincianItem();
            it.namaDokter = nmDokter;
            it.peran = peran;
            it.namaKomponen = "Jasa Medis Paket Hemodialisa (Single Use)";
            it.acuanAturan = "RALAN_HD_SINGLE_USE";
            it.formula = "Paket Tetap Rp " + Valid.SetAngka(KalkulatorJasaMedisDokter.getParameter1("RALAN_HD_SINGLE_USE", 75000.0));
            it.tarifRS = totalTarifJm;
            it.nominalJM = jmBpjs;
            it.rationale = "Jasa tindakan hemodialisa paket single use. Pemeriksaan dokter diabaikan (Rp 0) sesuai regulasi SEP HD.";
            list.add(it);
            return list;
        }

        // 3. Kasus AV Shunt
        if (keterangan.contains("AV Shunt") || keterangan.contains("AV-Shunt")) {
            double p1 = KalkulatorJasaMedisDokter.getParameter1("RALAN_AV_SHUNT", 575000.0);
            double p2 = KalkulatorJasaMedisDokter.getParameter2("RALAN_AV_SHUNT", 72000.0);

            if (keterangan.contains("Periksa Sp") || Math.abs(jmBpjs - (p1 + p2)) < 500) {
                RincianItem it1 = new RincianItem();
                it1.namaDokter = nmDokter;
                it1.peran = "Operator AV-Shunt";
                it1.namaKomponen = "Tindakan Operasi AV-Shunt (Cimino)";
                it1.acuanAturan = "RALAN_AV_SHUNT";
                it1.formula = "Param 1 (Rp " + Valid.SetAngka(p1) + ")";
                it1.tarifRS = totalTarifJm > p1 ? (totalTarifJm - p2) : p1;
                it1.nominalJM = p1;
                it1.rationale = "Jasa pembuatan akses vaskular AV-Shunt pada pasien rawat jalan.";
                list.add(it1);

                RincianItem it2 = new RincianItem();
                it2.namaDokter = nmDokter;
                it2.peran = "Pemeriksaan Sp";
                it2.namaKomponen = "Pemeriksaan dr. Spesialis (Kunjungan SEP AV Shunt)";
                it2.acuanAturan = "RALAN_AV_SHUNT";
                it2.formula = "Param 2 (Rp " + Valid.SetAngka(p2) + ")";
                it2.tarifRS = p2;
                it2.nominalJM = p2;
                it2.rationale = "Jasa pemeriksaan dr. spesialis pada SEP tindakan AV Shunt.";
                list.add(it2);
            } else if (Math.abs(jmBpjs - p2) < 500) {
                RincianItem it = new RincianItem();
                it.namaDokter = nmDokter;
                it.peran = "Pemeriksaan Sp";
                it.namaKomponen = "Pemeriksaan dr. Spesialis (SEP AV Shunt)";
                it.acuanAturan = "RALAN_AV_SHUNT";
                it.formula = "Param 2 (Rp " + Valid.SetAngka(p2) + ")";
                it.tarifRS = totalTarifJm;
                it.nominalJM = jmBpjs;
                it.rationale = "Pemeriksaan dr. spesialis kedua di SEP yang sama (tindakan AV Shunt dikerjakan dokter lain).";
                list.add(it);
            } else {
                RincianItem it = new RincianItem();
                it.namaDokter = nmDokter;
                it.peran = peran;
                it.namaKomponen = "Tindakan Operasi AV-Shunt (Cimino)";
                it.acuanAturan = "RALAN_AV_SHUNT";
                it.formula = "Param 1 (Rp " + Valid.SetAngka(p1) + ")";
                it.tarifRS = totalTarifJm;
                it.nominalJM = jmBpjs;
                it.rationale = "Tindakan pembuatan akses AV-Shunt rawat jalan.";
                list.add(it);
            }
            return list;
        }

        // 4. Kasus Double Lumen (CDL)
        if (keterangan.contains("Double Lumen") || keterangan.contains("CDL")) {
            double p1 = KalkulatorJasaMedisDokter.getParameter1("RALAN_DOUBLE_LUMEN", 300000.0);
            double p2 = KalkulatorJasaMedisDokter.getParameter2("RALAN_DOUBLE_LUMEN", 72000.0);

            if (keterangan.contains("Periksa Sp") || Math.abs(jmBpjs - (p1 + p2)) < 500) {
                RincianItem it1 = new RincianItem();
                it1.namaDokter = nmDokter;
                it1.peran = "Operator CDL";
                it1.namaKomponen = "Tindakan Pemasangan Double Lumen (CDL)";
                it1.acuanAturan = "RALAN_DOUBLE_LUMEN";
                it1.formula = "Param 1 (Rp " + Valid.SetAngka(p1) + ")";
                it1.tarifRS = totalTarifJm > p1 ? (totalTarifJm - p2) : p1;
                it1.nominalJM = p1;
                it1.rationale = "Jasa tindakan pemasangan kateter Double Lumen rawat jalan.";
                list.add(it1);

                RincianItem it2 = new RincianItem();
                it2.namaDokter = nmDokter;
                it2.peran = "Pemeriksaan Sp";
                it2.namaKomponen = "Pemeriksaan dr. Spesialis (SEP Double Lumen)";
                it2.acuanAturan = "RALAN_DOUBLE_LUMEN";
                it2.formula = "Param 2 (Rp " + Valid.SetAngka(p2) + ")";
                it2.tarifRS = p2;
                it2.nominalJM = p2;
                it2.rationale = "Jasa pemeriksaan dr. spesialis pada SEP pemasangan Double Lumen.";
                list.add(it2);
            } else {
                RincianItem it = new RincianItem();
                it.namaDokter = nmDokter;
                it.peran = peran;
                it.namaKomponen = "Tindakan Pemasangan Double Lumen (CDL)";
                it.acuanAturan = "RALAN_DOUBLE_LUMEN";
                it.formula = "Param 1 (Rp " + Valid.SetAngka(p1) + ")";
                it.tarifRS = totalTarifJm;
                it.nominalJM = jmBpjs;
                it.rationale = "Jasa tindakan pemasangan kateter Double Lumen rawat jalan.";
                list.add(it);
            }
            return list;
        }

        // 5. Kasus Dokter Spesialis KFR (Rehab Medik)
        if (keterangan.contains("KFR")) {
            RincianItem it = new RincianItem();
            it.namaDokter = nmDokter;
            it.peran = peran;
            it.namaKomponen = "Pelayanan Dokter Spesialis KFR";
            it.acuanAturan = "RALAN_KFR";
            it.formula = "Tarif Paket KFR (Rp " + Valid.SetAngka(jmBpjs) + ")";
            it.tarifRS = totalTarifJm;
            it.nominalJM = jmBpjs;
            it.rationale = "Jasa pelayanan pemeriksaan / tindakan rehabilitasi medik spesialis KFR.";
            list.add(it);
            return list;
        }

        // 6. Kasus Rujukan Internal (Raber 2 Spesialis dalam 1 SEP)
        if (keterangan.contains("Rujukan Internal") || keterangan.contains("Raber")) {
            RincianItem it = new RincianItem();
            it.namaDokter = nmDokter;
            it.peran = "Raber Ralan";
            it.namaKomponen = "Pemeriksaan Rawat Bersama (Raber 2 Spesialis)";
            it.acuanAturan = "RALAN_RUJUKAN_INTERNAL";
            it.formula = "Porsi Raber (Rp " + Valid.SetAngka(jmBpjs) + ")";
            it.tarifRS = totalTarifJm;
            it.nominalJM = jmBpjs;
            it.rationale = "Pasien berkunjung ke 2 dokter spesialis pada SEP yang sama. Klaim dibagi sesuai ketentuan rujukan internal.";
            list.add(it);
            return list;
        }

        // 7. Kasus Dokter Umum IGD
        if (keterangan.contains("Dokter Umum") || keterangan.contains("IGD")) {
            RincianItem it = new RincianItem();
            it.namaDokter = nmDokter;
            it.peran = "Dokter Umum IGD";
            it.namaKomponen = "Pemeriksaan Dokter Umum IGD";
            it.acuanAturan = "RALAN_DOKTER_UMUM_IGD";
            it.formula = "Rp 0 (Non-JM)";
            it.tarifRS = totalTarifJm;
            it.nominalJM = 0.0;
            it.rationale = "Pelayanan triase / gawat darurat oleh dokter umum non-spesialis.";
            list.add(it);
            return list;
        }

        // 8. Default: Dokter Spesialis Standar (1 DPJP Tunggal)
        double plafonPeriksa = KalkulatorJasaMedisDokter.getParameter1("RALAN_PERIKSA_SP", 40000.0);
        if (keterangan.contains("Tindakan") && jmBpjs > plafonPeriksa) {
            RincianItem it1 = new RincianItem();
            it1.namaDokter = nmDokter;
            it1.peran = peran;
            it1.namaKomponen = "Pemeriksaan Poliklinik Dokter Spesialis";
            it1.acuanAturan = "RALAN_PERIKSA_SP";
            it1.formula = "Plafon Rp " + Valid.SetAngka(plafonPeriksa);
            it1.tarifRS = 80000.0;
            it1.nominalJM = plafonPeriksa;
            it1.rationale = "Konsultasi dan pemeriksaan dokter spesialis rawat jalan.";
            list.add(it1);

            RincianItem it2 = new RincianItem();
            it2.namaDokter = nmDokter;
            it2.peran = peran;
            it2.namaKomponen = "Tindakan Medis Poliklinik";
            it2.acuanAturan = "RALAN_TINDAKAN_SP";
            it2.formula = "Sisa Alokasi Tindakan (Rp " + Valid.SetAngka(jmBpjs - plafonPeriksa) + ")";
            it2.tarifRS = totalTarifJm > 80000 ? (totalTarifJm - 80000) : (jmBpjs - plafonPeriksa);
            it2.nominalJM = jmBpjs - plafonPeriksa;
            it2.rationale = "Tindakan medis rawat jalan yang dilakukan di poliklinik.";
            list.add(it2);
        } else {
            RincianItem it = new RincianItem();
            it.namaDokter = nmDokter;
            it.peran = peran;
            it.namaKomponen = "Pemeriksaan Poliklinik Dokter Spesialis";
            it.acuanAturan = "RALAN_PERIKSA_SP";
            it.formula = "Plafon Rp " + Valid.SetAngka(jmBpjs);
            it.tarifRS = totalTarifJm;
            it.nominalJM = jmBpjs;
            it.rationale = "Konsultasi dan pemeriksaan dokter spesialis rawat jalan reguler.";
            list.add(it);
        }

        return list;
    }

    private List<RincianItem> pecahRincianRanap(
            String noRawat, String noSep, String kdDokter, String nmDokter,
            double totalTarifJm, double umbal, double jmBpjs, String keterangan,
            String statusDokter, String statusOperasi, String jenisPct, String visitLos
    ) {
        List<RincianItem> list = new ArrayList<>();
        String peran = (statusDokter != null && !statusDokter.trim().isEmpty()) ? statusDokter : "Dokter Ranap";

        // 0. Pengecualian Dokter Radiologi / Gigi Bedah Mulut
        if (keterangan.contains("Bebas Aturan") || nmDokter.toLowerCase().contains("sp.rad") || nmDokter.toLowerCase().contains("radiologi") || nmDokter.toLowerCase().contains("sp.bm") || nmDokter.toLowerCase().contains("sp.bmm") || nmDokter.toLowerCase().contains("bedah mulut")) {
            RincianItem it = new RincianItem();
            it.namaDokter = nmDokter;
            it.peran = "Radiologi/Sp.BM";
            it.namaKomponen = "Pelayanan Medis Spesialis (Bebas Aturan BPJS)";
            it.acuanAturan = "PENGECUALIAN_KHUSUS";
            it.formula = "100% Tarif Normal RS";
            it.tarifRS = totalTarifJm;
            it.nominalJM = jmBpjs;
            it.rationale = "Dokter Radiologi & Gigi Bedah Mulut memperoleh tarif normal RS penuh tanpa pemotongan BPJS.";
            list.add(it);
            return list;
        }

        // 0.1 Dokter Umum di Ranap (Non-JM, payroll terpisah)
        if ("Dokter Umum".equalsIgnoreCase(statusDokter) || keterangan.contains("Dokter Umum") || nmDokter.toLowerCase().contains("umum")) {
            RincianItem it = new RincianItem();
            it.namaDokter = nmDokter;
            it.peran = "Dokter Umum";
            it.namaKomponen = "Visite / Jaga Ruangan Ranap (Dokter Umum)";
            it.acuanAturan = "NON_JM_PAYROLL_TERPISAH";
            it.formula = "Rp 0 (Non-JM)";
            it.tarifRS = totalTarifJm;
            it.nominalJM = 0.0;
            it.rationale = "Dokter umum tidak mendapatkan pembagian klaim INA-CBG DPJP Ranap (telah diakomodasi modul payroll tersendiri).";
            list.add(it);
            return list;
        }

        // 1. Kasus Dokter Anestesi
        if ("Anestesi".equalsIgnoreCase(statusDokter)) {
            RincianItem it = new RincianItem();
            it.namaDokter = nmDokter;
            it.peran = "Anestesi";
            it.namaKomponen = "Jasa Medis Pembiusan / Anestesi Operasi";
            it.acuanAturan = "RANAP_ANESTESI";
            it.formula = "40% dari Porsi Operator Bedah (" + jenisPct + "%)";
            it.tarifRS = totalTarifJm;
            it.nominalJM = jmBpjs;
            it.rationale = "Jasa pembiusan operasi rawat inap (standar akumulasi universal 40% dari porsi operator).";
            list.add(it);
            return list;
        }

        // 2. Kasus Dokter Anak (Pendamping SC / Post Partum)
        if ("Dokter Anak".equalsIgnoreCase(statusDokter) || keterangan.contains("dr. Anak")) {
            RincianItem it = new RincianItem();
            it.namaDokter = nmDokter;
            it.peran = "Dokter Anak";
            it.namaKomponen = "Jasa Pendampingan Bayi Baru Lahir (SC / Partus)";
            it.acuanAturan = "RANAP_DOKTER_ANAK";
            it.formula = jenisPct + "% dari Umbal Klaim";
            it.tarifRS = totalTarifJm;
            it.nominalJM = jmBpjs;
            it.rationale = "Penanganan dan resusitasi neonatus pada operasi persalinan Sectio Caesarea / Post Partum.";
            list.add(it);
            return list;
        }

        // 3. Kasus Operator Bedah
        if ("Operator".equalsIgnoreCase(statusDokter) || "Operasi".equalsIgnoreCase(statusOperasi) || keterangan.contains("Operator")) {
            RincianItem it = new RincianItem();
            it.namaDokter = nmDokter;
            it.peran = "Operator Bedah";
            it.namaKomponen = "Jasa Operator Bedah Utama";
            it.acuanAturan = "RANAP_OPERASI";
            it.formula = jenisPct + "% dari Umbal (Rp " + Valid.SetAngka(umbal) + ")";
            it.tarifRS = totalTarifJm;
            it.nominalJM = jmBpjs;
            it.rationale = "Jasa dokter operator pelaksana tindakan bedah rawat inap.";
            list.add(it);
            return list;
        }

        // 4. Kasus Visite Ranap (DPJP Utama / Pendamping)
        if (visitLos != null && !visitLos.trim().isEmpty() && !"-".equals(visitLos)) {
            RincianItem it = new RincianItem();
            it.namaDokter = nmDokter;
            it.peran = peran;
            it.namaKomponen = "Jasa Visite Perawatan Rawat Inap";
            it.acuanAturan = "RANAP_NON_BEDAH";
            it.formula = "Rasio Visite (" + visitLos + ") -> Porsi " + jenisPct + "%";
            it.tarifRS = totalTarifJm;
            it.nominalJM = jmBpjs;
            it.rationale = "Jasa kunjungan / visite dan evaluasi medis dokter spesialis selama pasien dirawat di bangsal.";
            list.add(it);
            return list;
        }

        // 5. Default Ranap
        RincianItem it = new RincianItem();
        it.namaDokter = nmDokter;
        it.peran = peran;
        it.namaKomponen = "Jasa Medis Rawat Inap (" + statusDokter + ")";
        it.acuanAturan = "RANAP_DPJP";
        it.formula = jenisPct + "% dari Umbal";
        it.tarifRS = totalTarifJm;
        it.nominalJM = jmBpjs;
        it.rationale = "Jasa pelayanan medis rawat inap sesuai peran klinis dokter.";
        list.add(it);

        return list;
    }

    private void salinKeClipboard() {
        if (teksSalinanClipboard.isEmpty()) {
            return;
        }
        try {
            StringSelection sel = new StringSelection(teksSalinanClipboard);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
            JOptionPane.showMessageDialog(this,
                    "Rincian perhitungan 1 SEP berhasil disalin ke clipboard!\nAnda dapat langsung mem-paste (Ctrl+V) ke WhatsApp atau Excel.",
                    "Berhasil Disalin", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Gagal menyalin: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class RincianItem {
        String namaDokter = "";
        String peran = "";
        String namaKomponen = "";
        String acuanAturan = "";
        String formula = "";
        double tarifRS = 0;
        double nominalJM = 0;
        String rationale = "";
    }
}
