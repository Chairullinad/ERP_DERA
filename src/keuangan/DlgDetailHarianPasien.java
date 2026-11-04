package keuangan;

import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import simrskhanza.DlgCariCaraBayar;
import simrskhanza.DlgCariPoli;

/**
 * Dialog Rekapitulasi & Detail Harian Pasien (Berbasis Buku Bantu 2 + Parameter JM BPJS).
 * Menampilkan seluruh rincian tindakan per pasien beserta klasifikasi Operasi/Non-Operasi,
 * Non-ICU/Alih Rawat ICU/ICU Murni, Multi-DPJP (Tunggal/Tandem 2 Org/> 2 Org), Hemodialisa,
 * Terapi Rehab, Tindakan Khusus JM BPJS, COB, Ekses, Potongan, dan Status Klaim BPJS.
 * Dilengkapi pagination 3.000 baris per halaman, batch loading SwingWorker non-blocking,
 * pembukaan Nota Billing via double-click, serta export Excel seluruh halaman.
 */
public final class DlgDetailHarianPasien extends javax.swing.JDialog {

    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final DefaultTableModel tabMode;

    private final DlgCariDokter dokter = new DlgCariDokter(null, false);
    private final DlgCariPoli poli = new DlgCariPoli(null, false);
    private final DlgCariCaraBayar penjab = new DlgCariCaraBayar(null, false);

    // Konfigurasi Pagination & Batching
    private static final int PAGE_SIZE = 3000;
    private static final int BATCH_SIZE = 40;

    // Buffer Data & State Pagination
    private final List<EpisodeRecord> cachedEpisodes = new ArrayList<>();
    private final List<Object[]> filteredRows = new ArrayList<>();
    private int currentPage = 0;
    private int filteredEpisodeCount = 0;
    private int loadingTotalEpisodes = -1;
    private String dataPeriodKey = "";
    private String targetNoSep = "";
    private String targetNoRawat = "";
    private String targetNoRm = "";
    private volatile boolean isUpdatingFilterUI = false;
    private boolean includeObatRequested = false;

    // Workers, State & Dialogs
    private SwingWorker<Void, List<EpisodeRecord>> activeWorker = null;
    private SwingWorker<Map<EpisodeRecord, List<ActionItem>>, Void> obatWorker = null;
    private final Set<String> failedQueries = new HashSet<>();
    private javax.swing.JDialog dlgNotaAktif = null;

    /**
     * Struktur penyimpanan 1 episode perawatan pasien (no_rawat) beserta seluruh baris tindakannya
     * agar filter kategori ComboBox dapat menyaring data secara instan dari memori tanpa query ulang.
     */
    private static class EpisodeRecord {
        int episodeIndex;
        String noRawat = "";
        String noNota = "";
        String tglByr = "";
        String namaBulan = "";
        String tglRegistrasi = "";
        String noRM = "";
        String nmPasien = "";
        String noSep = "";
        String statusKlaim = ""; // Kolom Status Klaim BPJS (inacbg_klaim_umpanbalik.status)
        boolean isSepGanda = false;
        String statusLanjut = "";
        String kdPoli = "";
        String nmPoli = "";
        String kdDokterReg = "";
        String nmDokterReg = "";
        String dpjpSepVClaim = "";
        String dpjp1 = "";
        String dpjp2 = "";
        String dpjp3Plus = "";
        int jmlDpjp = 1;
        String kategoriTandem = "Tunggal (1 Org)";
        boolean hasOperasi = false;
        String kategoriOperasi = "Non Operasi";
        String kategoriICU = "Non ICU";
        String riwayatKamar = "-";
        int losHari = 0;
        boolean isHD = false;
        String statusHD = "Bukan HD";
        boolean isTerapi = false;
        String statusTerapi = "Tidak";
        String tindakanKhususEpisode = "Konsultasi/Visite";
        boolean isCOB = false;
        String statusCOB = "Non-COB";
        double plafonCOB = 0.0;
        String rincianCOB = "-";
        double totalPotongan = 0.0;
        double totalEkses = 0.0;
        String akunBayarEkses = "-";
        double totalBillingRS = 0.0;
        double byTarifGrouper = 0.0;
        double byDisetujui = 0.0;
        double byTopUp = 0.0;
        double selisihRS = 0.0;
        String noFpk = "";
        String kdPjUtama = "";
        String pngJawabUtama = "";
        boolean obatLoaded = false;

        // Daftar dokter yang melakukan operasi dan jumlah visite per dokter pada episode ini
        final Set<String> operatorDoctors = new LinkedHashSet<>();
        final Set<String> anestesiDoctors = new LinkedHashSet<>();
        final Set<String> dpjpUtamaDoctors = new LinkedHashSet<>();
        final Map<String, Integer> doctorVisitCounts = new HashMap<>();

        final List<ActionItem> actionItems = new ArrayList<>();
    }

    /**
     * Struktur penyimpanan 1 baris rincian tindakan/layanan di dalam episode pasien.
     */
    private static class ActionItem {
        boolean isObatOrBhp = false;
        String kdDokterOrNip = "";
        String nmPelaksana = "";
        boolean isDokter = false;
        boolean isDokterUmum = false;
        String peranDokter = "";
        String subKategoriOpDokter = "Non-OP";
        int jmlVisiteDokter = 0;
        String statusVisiteVsLos = "-";
        String kategoriLayanan = "";
        String kodeTindakan = "";
        String namaLayanan = "";
        String tagTindakanKhusus = "";
        double biayaSatuan = 0.0;
        double qty = 0.0;
        double totalBiaya = 0.0;
        double jmDokter = 0.0;
        String payorBaris = "";
    }

    /**
     * Akumulator data per episode selama proses pengayaan batch.
     */
    private static class BatchScratch {
        boolean hasIcu = false;
        boolean hasNonIcu = false;
        boolean cobSetFound = false;
        int countPayor = 0;
        double totalHours = 0.0;
        double sumNotaTunai = 0.0;
        final List<String> roomParts = new ArrayList<>();
        final List<String> cobParts = new ArrayList<>();
        final List<String> eksesParts = new ArrayList<>();
        final Map<String, String> specialists = new LinkedHashMap<>();
    }

    @FunctionalInterface
    private interface BatchRowHandler {
        void handle(ResultSet rs, List<EpisodeRecord> eps) throws Exception;
    }

    /**
     * Snapshot nilai filter saat pemrosesan agar konsisten di background thread dan EDT.
     */
    private static class FilterSnapshot {
        final String fRawat;
        final String fOp;
        final String fICU;
        final String fTandem;
        final String fHD;
        final String fKhusus;
        final String fBiaya;
        final String fKdDok;
        final String fKdPoli;
        final String fKdPj;
        final boolean includeObat;
        final String keyword;
        final String targetNoSep;
        final String targetNoRawat;
        final String targetNoRm;

        FilterSnapshot(String fRawat, String fOp, String fICU, String fTandem, String fHD,
                String fKhusus, String fBiaya, String fKdDok, String fKdPoli, String fKdPj,
                boolean includeObat, String keyword, String targetNoSep, String targetNoRawat, String targetNoRm) {
            this.fRawat = fRawat;
            this.fOp = fOp;
            this.fICU = fICU;
            this.fTandem = fTandem;
            this.fHD = fHD;
            this.fKhusus = fKhusus;
            this.fBiaya = fBiaya;
            this.fKdDok = fKdDok;
            this.fKdPoli = fKdPoli;
            this.fKdPj = fKdPj;
            this.includeObat = includeObat;
            this.keyword = keyword;
            this.targetNoSep = targetNoSep;
            this.targetNoRawat = targetNoRawat;
            this.targetNoRm = targetNoRm;
        }
    }

    public DlgDetailHarianPasien(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        Object[] headers = new Object[]{
            "Episode", "No. Rawat", "No. Nota", "Tgl. Billing", "Bulan", "Tgl. Registrasi",
            "No. RM", "Nama Pasien", "No. SEP", "Status Klaim", "SEP Ganda", "RJ/RI", "Unit / Poli Awal",
            "Dokter Pengirim / Reg", "DPJP SEP VClaim", "DPJP Utama (1)", "DPJP 2", "DPJP 3 / Lainnya",
            "Jml DPJP", "Kategori Tandem", "Kategori Operasi", "Sub-Kategori OP Dokter",
            "Kategori ICU", "Riwayat Kamar & LOS", "LOS (Hari)", "Jml Visite Dr", "Status Visite vs LOS",
            "Status HD", "Status Terapi (Rehab)", "Tindakan Khusus JM", "Status COB", "Plafon COB",
            "Rincian Multi-Payor (COB)", "Total Potongan", "Total Ekses / Uang Muka", "Akun Bayar Ekses",
            "Dokter / Petugas Pelaksana", "Peran & Kualifikasi Dr", "Kategori Layanan", "Kode Tindakan",
            "Nama Layanan / Tindakan", "Biaya Satuan", "Qty", "Total Biaya", "JM Dokter (Tarif RS)",
            "Total Billing RS", "Tarif INACBG (Grouper)", "Biaya Disetujui (Umbal)", "Biaya TopUp",
            "Selisih RS", "No. FPK", "Payor / Penjamin"
        };

        tabMode = new DefaultTableModel(null, headers) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }

            final Class<?>[] types = new Class<?>[]{
                java.lang.Integer.class, // 0 Episode
                java.lang.String.class,  // 1 No. Rawat
                java.lang.String.class,  // 2 No. Nota
                java.lang.String.class,  // 3 Tgl. Billing
                java.lang.String.class,  // 4 Bulan
                java.lang.String.class,  // 5 Tgl. Registrasi
                java.lang.String.class,  // 6 No. RM
                java.lang.String.class,  // 7 Nama Pasien
                java.lang.String.class,  // 8 No. SEP
                java.lang.String.class,  // 9 Status Klaim
                java.lang.String.class,  // 10 SEP Ganda
                java.lang.String.class,  // 11 RJ/RI
                java.lang.String.class,  // 12 Unit / Poli Awal
                java.lang.String.class,  // 13 Dokter Pengirim / Reg
                java.lang.String.class,  // 14 DPJP SEP VClaim
                java.lang.String.class,  // 15 DPJP Utama (1)
                java.lang.String.class,  // 16 DPJP 2
                java.lang.String.class,  // 17 DPJP 3 / Lainnya
                java.lang.Integer.class, // 18 Jml DPJP
                java.lang.String.class,  // 19 Kategori Tandem
                java.lang.String.class,  // 20 Kategori Operasi
                java.lang.String.class,  // 21 Sub-Kategori OP Dokter
                java.lang.String.class,  // 22 Kategori ICU
                java.lang.String.class,  // 23 Riwayat Kamar & LOS
                java.lang.Integer.class, // 24 LOS (Hari)
                java.lang.Integer.class, // 25 Jml Visite Dr
                java.lang.String.class,  // 26 Status Visite vs LOS
                java.lang.String.class,  // 27 Status HD
                java.lang.String.class,  // 28 Status Terapi (Rehab)
                java.lang.String.class,  // 29 Tindakan Khusus JM
                java.lang.String.class,  // 30 Status COB
                java.lang.Double.class,  // 31 Plafon COB
                java.lang.String.class,  // 32 Rincian Multi-Payor (COB)
                java.lang.Double.class,  // 33 Total Potongan
                java.lang.Double.class,  // 34 Total Ekses / Uang Muka
                java.lang.String.class,  // 35 Akun Bayar Ekses
                java.lang.String.class,  // 36 Dokter / Petugas Pelaksana
                java.lang.String.class,  // 37 Peran & Kualifikasi Dr
                java.lang.String.class,  // 38 Kategori Layanan
                java.lang.String.class,  // 39 Kode Tindakan
                java.lang.String.class,  // 40 Nama Layanan / Tindakan
                java.lang.Double.class,  // 41 Biaya Satuan
                java.lang.Double.class,  // 42 Qty
                java.lang.Double.class,  // 43 Total Biaya
                java.lang.Double.class,  // 44 JM Dokter (Tarif RS)
                java.lang.Double.class,  // 45 Total Billing RS
                java.lang.Double.class,  // 46 Tarif INACBG (Grouper)
                java.lang.Double.class,  // 47 Biaya Disetujui (Umbal)
                java.lang.Double.class,  // 48 Biaya TopUp
                java.lang.Double.class,  // 49 Selisih RS
                java.lang.String.class,  // 50 No. FPK
                java.lang.String.class   // 51 Payor / Penjamin
            };

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };

        tbDetailHarian.setModel(tabMode);
        tbDetailHarian.setPreferredScrollableViewportSize(new Dimension(800, 500));
        tbDetailHarian.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] widths = new int[]{
            55, 105, 105, 75, 75, 75, 65, 160, 125, 95, 70, 55, 120,
            150, 150, 150, 150, 150, 60, 110, 95, 135,
            105, 185, 65, 75, 115, 85, 95, 125, 75, 95,
            195, 95, 95, 145, 155, 125, 115, 85, 210,
            95, 50, 105, 105, 110, 110, 110, 95, 105, 100, 135
        };
        for (int i = 0; i < widths.length; i++) {
            TableColumn col = tbDetailHarian.getColumnModel().getColumn(i);
            col.setPreferredWidth(widths[i]);
        }
        tbDetailHarian.setDefaultRenderer(Object.class, new WarnaTable());

        TCari.setDocument(new batasInput((int) 100).getKata(TCari));

        // Mencegah JavaFX runtime shutdown saat modal nota ditutup
        try {
            javafx.application.Platform.setImplicitExit(false);
        } catch (Throwable ignore) {
        }

        setupLookupDialogs();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        PanelFilterAtas = new widget.panelisi();
        panelBarisFilter1 = new widget.panelisi();
        lblRawat = new widget.Label();
        cmbStatusRawat = new widget.ComboBox();
        lblOp = new widget.Label();
        cmbKategoriOperasi = new widget.ComboBox();
        lblICU = new widget.Label();
        cmbKategoriICU = new widget.ComboBox();
        lblTandem = new widget.Label();
        cmbKategoriTandem = new widget.ComboBox();
        lblHD = new widget.Label();
        cmbKategoriHD = new widget.ComboBox();
        lblKhusus = new widget.Label();
        cmbTindakanKhusus = new widget.ComboBox();
        panelBarisFilter2 = new widget.panelisi();
        lblBiaya = new widget.Label();
        cmbFilterBiaya = new widget.ComboBox();
        lblDok = new widget.Label();
        KdDokter = new widget.TextBox();
        NmDokter = new widget.TextBox();
        BtnDokter = new widget.Button();
        lblPoli = new widget.Label();
        KdPoli = new widget.TextBox();
        NmPoli = new widget.TextBox();
        BtnPoli = new widget.Button();
        lblPenjab = new widget.Label();
        KdCaraBayar = new widget.TextBox();
        NmCaraBayar = new widget.TextBox();
        BtnCaraBayar = new widget.Button();
        ChkSertakanObat = new widget.CekBox();
        Scroll = new widget.ScrollPane();
        tbDetailHarian = new widget.Table();
        panelGlassBawah = new widget.panelisi();
        lblTgl = new widget.Label();
        Tgl1 = new widget.Tanggal();
        lblSd = new widget.Label();
        Tgl2 = new widget.Tanggal();
        lblKey = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnAll = new widget.Button();
        BtnPrevPage = new widget.Button();
        lblPageInfo = new widget.Label();
        BtnNextPage = new widget.Button();
        BtnExportExcel = new widget.Button();
        lblStatusLoading = new widget.Label();
        BtnKeluar = new widget.Button();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Detail Harian Pasien (Rekap Tindakan & Parameter JM BPJS) ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        PanelFilterAtas.setName("PanelFilterAtas"); // NOI18N
        PanelFilterAtas.setPreferredSize(new java.awt.Dimension(100, 78));
        PanelFilterAtas.setLayout(new java.awt.GridLayout(2, 1));

        panelBarisFilter1.setName("panelBarisFilter1"); // NOI18N
        panelBarisFilter1.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 6));

        lblRawat.setText("Status Rawat :");
        lblRawat.setName("lblRawat"); // NOI18N
        lblRawat.setPreferredSize(new java.awt.Dimension(75, 23));
        panelBarisFilter1.add(lblRawat);

        cmbStatusRawat.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Ralan", "Ranap" }));
        cmbStatusRawat.setName("cmbStatusRawat"); // NOI18N
        cmbStatusRawat.setPreferredSize(new java.awt.Dimension(85, 23));
        cmbStatusRawat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbStatusRawatActionPerformed(evt);
            }
        });
        panelBarisFilter1.add(cmbStatusRawat);

        lblOp.setText("Kategori Bedah :");
        lblOp.setName("lblOp"); // NOI18N
        lblOp.setPreferredSize(new java.awt.Dimension(88, 23));
        panelBarisFilter1.add(lblOp);

        cmbKategoriOperasi.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Operasi", "Non Operasi", "Bedah + Tindakan", "Visite Pasien OP", "Anestesi OP" }));
        cmbKategoriOperasi.setName("cmbKategoriOperasi"); // NOI18N
        cmbKategoriOperasi.setPreferredSize(new java.awt.Dimension(130, 23));
        cmbKategoriOperasi.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbKategoriOperasiActionPerformed(evt);
            }
        });
        panelBarisFilter1.add(cmbKategoriOperasi);

        lblICU.setText("Kategori ICU :");
        lblICU.setName("lblICU"); // NOI18N
        lblICU.setPreferredSize(new java.awt.Dimension(75, 23));
        panelBarisFilter1.add(lblICU);

        cmbKategoriICU.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Semua ICU", "Non ICU", "Alih Rawat ICU", "ICU Murni" }));
        cmbKategoriICU.setName("cmbKategoriICU"); // NOI18N
        cmbKategoriICU.setPreferredSize(new java.awt.Dimension(110, 23));
        cmbKategoriICU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbKategoriICUActionPerformed(evt);
            }
        });
        panelBarisFilter1.add(cmbKategoriICU);

        lblTandem.setText("DPJP / Tandem :");
        lblTandem.setName("lblTandem"); // NOI18N
        lblTandem.setPreferredSize(new java.awt.Dimension(88, 23));
        panelBarisFilter1.add(lblTandem);

        cmbKategoriTandem.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Tunggal (1 Org)", "Tandem 2 Org", "Lebih Dari 2 Org", "SEP Ganda (Raber RJ)" }));
        cmbKategoriTandem.setName("cmbKategoriTandem"); // NOI18N
        cmbKategoriTandem.setPreferredSize(new java.awt.Dimension(140, 23));
        cmbKategoriTandem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbKategoriTandemActionPerformed(evt);
            }
        });
        panelBarisFilter1.add(cmbKategoriTandem);

        lblHD.setText("Hemodialisa :");
        lblHD.setName("lblHD"); // NOI18N
        lblHD.setPreferredSize(new java.awt.Dimension(72, 23));
        panelBarisFilter1.add(lblHD);

        cmbKategoriHD.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Pasien HD", "Bukan HD" }));
        cmbKategoriHD.setName("cmbKategoriHD"); // NOI18N
        cmbKategoriHD.setPreferredSize(new java.awt.Dimension(95, 23));
        cmbKategoriHD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbKategoriHDActionPerformed(evt);
            }
        });
        panelBarisFilter1.add(cmbKategoriHD);

        lblKhusus.setText("Tindakan Khusus :");
        lblKhusus.setName("lblKhusus"); // NOI18N
        lblKhusus.setPreferredSize(new java.awt.Dimension(95, 23));
        panelBarisFilter1.add(lblKhusus);

        cmbTindakanKhusus.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "CDL / Double Lumen", "AV Shunt", "ESWL", "EEG", "Audiometri", "Terapi (KRM/OT/Okupasi)", "Tindakan Poli", "Konsultasi Saja" }));
        cmbTindakanKhusus.setName("cmbTindakanKhusus"); // NOI18N
        cmbTindakanKhusus.setPreferredSize(new java.awt.Dimension(160, 23));
        cmbTindakanKhusus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbTindakanKhususActionPerformed(evt);
            }
        });
        panelBarisFilter1.add(cmbTindakanKhusus);

        PanelFilterAtas.add(panelBarisFilter1);

        panelBarisFilter2.setName("panelBarisFilter2"); // NOI18N
        panelBarisFilter2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 6));

        lblBiaya.setText("Klaim/Biaya :");
        lblBiaya.setName("lblBiaya"); // NOI18N
        lblBiaya.setPreferredSize(new java.awt.Dimension(70, 23));
        panelBarisFilter2.add(lblBiaya);

        cmbFilterBiaya.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Semua", "Pasien COB", "Ada Ekses", "Ada Potongan", "Dokter Spesialis", "Dokter Umum", "Visite Tidak Normal" }));
        cmbFilterBiaya.setName("cmbFilterBiaya"); // NOI18N
        cmbFilterBiaya.setPreferredSize(new java.awt.Dimension(135, 23));
        cmbFilterBiaya.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbFilterBiayaActionPerformed(evt);
            }
        });
        panelBarisFilter2.add(cmbFilterBiaya);

        lblDok.setText("Dokter :");
        lblDok.setName("lblDok"); // NOI18N
        lblDok.setPreferredSize(new java.awt.Dimension(48, 23));
        panelBarisFilter2.add(lblDok);

        KdDokter.setEditable(false);
        KdDokter.setName("KdDokter"); // NOI18N
        KdDokter.setPreferredSize(new java.awt.Dimension(65, 23));
        panelBarisFilter2.add(KdDokter);

        NmDokter.setEditable(false);
        NmDokter.setName("NmDokter"); // NOI18N
        NmDokter.setPreferredSize(new java.awt.Dimension(135, 23));
        panelBarisFilter2.add(NmDokter);

        BtnDokter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnDokter.setToolTipText("Pilih Dokter");
        BtnDokter.setName("BtnDokter"); // NOI18N
        BtnDokter.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnDokter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnDokterActionPerformed(evt);
            }
        });
        panelBarisFilter2.add(BtnDokter);

        lblPoli.setText("Unit/Poli :");
        lblPoli.setName("lblPoli"); // NOI18N
        lblPoli.setPreferredSize(new java.awt.Dimension(55, 23));
        panelBarisFilter2.add(lblPoli);

        KdPoli.setEditable(false);
        KdPoli.setName("KdPoli"); // NOI18N
        KdPoli.setPreferredSize(new java.awt.Dimension(50, 23));
        panelBarisFilter2.add(KdPoli);

        NmPoli.setEditable(false);
        NmPoli.setName("NmPoli"); // NOI18N
        NmPoli.setPreferredSize(new java.awt.Dimension(115, 23));
        panelBarisFilter2.add(NmPoli);

        BtnPoli.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnPoli.setToolTipText("Pilih Unit/Poli");
        BtnPoli.setName("BtnPoli"); // NOI18N
        BtnPoli.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnPoli.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPoliActionPerformed(evt);
            }
        });
        panelBarisFilter2.add(BtnPoli);

        lblPenjab.setText("Penjamin :");
        lblPenjab.setName("lblPenjab"); // NOI18N
        lblPenjab.setPreferredSize(new java.awt.Dimension(58, 23));
        panelBarisFilter2.add(lblPenjab);

        KdCaraBayar.setEditable(false);
        KdCaraBayar.setName("KdCaraBayar"); // NOI18N
        KdCaraBayar.setPreferredSize(new java.awt.Dimension(45, 23));
        panelBarisFilter2.add(KdCaraBayar);

        NmCaraBayar.setEditable(false);
        NmCaraBayar.setName("NmCaraBayar"); // NOI18N
        NmCaraBayar.setPreferredSize(new java.awt.Dimension(115, 23));
        panelBarisFilter2.add(NmCaraBayar);

        BtnCaraBayar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        BtnCaraBayar.setToolTipText("Pilih Cara Bayar / Penjamin");
        BtnCaraBayar.setName("BtnCaraBayar"); // NOI18N
        BtnCaraBayar.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCaraBayar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCaraBayarActionPerformed(evt);
            }
        });
        panelBarisFilter2.add(BtnCaraBayar);

        ChkSertakanObat.setText("Sertakan Obat & BHP");
        ChkSertakanObat.setName("ChkSertakanObat"); // NOI18N
        ChkSertakanObat.setPreferredSize(new java.awt.Dimension(135, 23));
        ChkSertakanObat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkSertakanObatActionPerformed(evt);
            }
        });
        panelBarisFilter2.add(ChkSertakanObat);

        PanelFilterAtas.add(panelBarisFilter2);

        internalFrame1.add(PanelFilterAtas, java.awt.BorderLayout.PAGE_START);

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbDetailHarian.setToolTipText("Double-klik pada baris pasien untuk menampilkan Nota Billing Pasien");
        tbDetailHarian.setName("tbDetailHarian"); // NOI18N
        tbDetailHarian.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbDetailHarianMouseClicked(evt);
            }
        });
        Scroll.setViewportView(tbDetailHarian);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        panelGlassBawah.setName("panelGlassBawah"); // NOI18N
        panelGlassBawah.setPreferredSize(new java.awt.Dimension(100, 48));
        panelGlassBawah.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        lblTgl.setText("Tgl. Billing :");
        lblTgl.setName("lblTgl"); // NOI18N
        lblTgl.setPreferredSize(new java.awt.Dimension(65, 23));
        panelGlassBawah.add(lblTgl);

        Tgl1.setDisplayFormat("dd-MM-yyyy");
        Tgl1.setName("Tgl1"); // NOI18N
        Tgl1.setPreferredSize(new java.awt.Dimension(90, 23));
        Tgl1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Tgl1KeyPressed(evt);
            }
        });
        panelGlassBawah.add(Tgl1);

        lblSd.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSd.setText("s.d.");
        lblSd.setName("lblSd"); // NOI18N
        lblSd.setPreferredSize(new java.awt.Dimension(24, 23));
        panelGlassBawah.add(lblSd);

        Tgl2.setDisplayFormat("dd-MM-yyyy");
        Tgl2.setName("Tgl2"); // NOI18N
        Tgl2.setPreferredSize(new java.awt.Dimension(90, 23));
        Tgl2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                Tgl2KeyPressed(evt);
            }
        });
        panelGlassBawah.add(Tgl2);

        lblKey.setText("Key Word :");
        lblKey.setName("lblKey"); // NOI18N
        lblKey.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlassBawah.add(lblKey);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(175, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlassBawah.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('5');
        BtnCari.setToolTipText("Tampilkan / Saring Data (Enter / F5)");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        panelGlassBawah.add(BtnCari);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Reset Filter & Muat Ulang Semua Data (Alt+M)");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(80, 23));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        panelGlassBawah.add(BtnAll);

        BtnPrevPage.setText("< Prev");
        BtnPrevPage.setToolTipText("Halaman Sebelumnya (Maks. 3.000 baris/halaman)");
        BtnPrevPage.setEnabled(false);
        BtnPrevPage.setName("BtnPrevPage"); // NOI18N
        BtnPrevPage.setPreferredSize(new java.awt.Dimension(62, 23));
        BtnPrevPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrevPageActionPerformed(evt);
            }
        });
        panelGlassBawah.add(BtnPrevPage);

        lblPageInfo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblPageInfo.setText("Hal: 1 / 1");
        lblPageInfo.setName("lblPageInfo"); // NOI18N
        lblPageInfo.setPreferredSize(new java.awt.Dimension(75, 23));
        panelGlassBawah.add(lblPageInfo);

        BtnNextPage.setText("Next >");
        BtnNextPage.setToolTipText("Halaman Berikutnya (Maks. 3.000 baris/halaman)");
        BtnNextPage.setEnabled(false);
        BtnNextPage.setName("BtnNextPage"); // NOI18N
        BtnNextPage.setPreferredSize(new java.awt.Dimension(62, 23));
        BtnNextPage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnNextPageActionPerformed(evt);
            }
        });
        panelGlassBawah.add(BtnNextPage);

        BtnExportExcel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnExportExcel.setMnemonic('E');
        BtnExportExcel.setText("Export Excel");
        BtnExportExcel.setToolTipText("Export Seluruh Data (Semua Halaman) ke Microsoft Excel .xlsx (Alt+E)");
        BtnExportExcel.setName("BtnExportExcel"); // NOI18N
        BtnExportExcel.setPreferredSize(new java.awt.Dimension(105, 23));
        BtnExportExcel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnExportExcelActionPerformed(evt);
            }
        });
        panelGlassBawah.add(BtnExportExcel);

        lblStatusLoading.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblStatusLoading.setText("Siap.");
        lblStatusLoading.setName("lblStatusLoading"); // NOI18N
        lblStatusLoading.setPreferredSize(new java.awt.Dimension(240, 23));
        panelGlassBawah.add(lblStatusLoading);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Tutup Form (Alt+K / F4)");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(80, 23));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        panelGlassBawah.add(BtnKeluar);

        internalFrame1.add(panelGlassBawah, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // Kebijakan Zero Auto-Load: Tidak memicu query saat form pertama kali dibuka
        lblStatusLoading.setText("Silakan pilih filter/tanggal lalu tekan Enter atau klik Cari");
        TCari.requestFocus();
    }//GEN-LAST:event_formWindowOpened

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        cancelActiveWorker();
    }//GEN-LAST:event_formWindowClosing

    private void cmbStatusRawatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbStatusRawatActionPerformed
        onCategoryFilterChanged();
    }//GEN-LAST:event_cmbStatusRawatActionPerformed

    private void cmbKategoriOperasiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbKategoriOperasiActionPerformed
        onCategoryFilterChanged();
    }//GEN-LAST:event_cmbKategoriOperasiActionPerformed

    private void cmbKategoriICUActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbKategoriICUActionPerformed
        onCategoryFilterChanged();
    }//GEN-LAST:event_cmbKategoriICUActionPerformed

    private void cmbKategoriTandemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbKategoriTandemActionPerformed
        onCategoryFilterChanged();
    }//GEN-LAST:event_cmbKategoriTandemActionPerformed

    private void cmbKategoriHDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbKategoriHDActionPerformed
        onCategoryFilterChanged();
    }//GEN-LAST:event_cmbKategoriHDActionPerformed

    private void cmbTindakanKhususActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbTindakanKhususActionPerformed
        onCategoryFilterChanged();
    }//GEN-LAST:event_cmbTindakanKhususActionPerformed

    private void cmbFilterBiayaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbFilterBiayaActionPerformed
        onCategoryFilterChanged();
    }//GEN-LAST:event_cmbFilterBiayaActionPerformed

    private void BtnDokterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnDokterActionPerformed
        dokter.isCek();
        dokter.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        dokter.setLocationRelativeTo(internalFrame1);
        dokter.setAlwaysOnTop(false);
        dokter.setVisible(true);
    }//GEN-LAST:event_BtnDokterActionPerformed

    private void BtnPoliActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPoliActionPerformed
        poli.isCek();
        poli.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        poli.setLocationRelativeTo(internalFrame1);
        poli.setAlwaysOnTop(false);
        poli.setVisible(true);
    }//GEN-LAST:event_BtnPoliActionPerformed

    private void BtnCaraBayarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCaraBayarActionPerformed
        penjab.isCek();
        penjab.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
        penjab.setLocationRelativeTo(internalFrame1);
        penjab.setAlwaysOnTop(false);
        penjab.setVisible(true);
    }//GEN-LAST:event_BtnCaraBayarActionPerformed

    private void ChkSertakanObatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkSertakanObatActionPerformed
        if (ChkSertakanObat.isSelected()) {
            includeObatRequested = true;
            startObatCatchUp();
        } else {
            onCategoryFilterChanged();
        }
    }//GEN-LAST:event_ChkSertakanObatActionPerformed

    private void Tgl1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tgl1KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            Tgl2.requestFocus();
        }
    }//GEN-LAST:event_Tgl1KeyPressed

    private void Tgl2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_Tgl2KeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            loadDataAsync(false);
        }
    }//GEN-LAST:event_Tgl2KeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            loadDataAsync(false);
        } else if (evt.getKeyCode() == KeyEvent.VK_F5) {
            loadDataAsync(true);
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        loadDataAsync(false);
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        resetFiltersAndLoad();
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnPrevPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrevPageActionPerformed
        if (currentPage > 0) {
            renderPage(currentPage - 1);
        }
    }//GEN-LAST:event_BtnPrevPageActionPerformed

    private void BtnNextPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnNextPageActionPerformed
        int totalRows = filteredRows.size();
        int totalPages = (int) Math.ceil((double) totalRows / PAGE_SIZE);
        if (currentPage < totalPages - 1) {
            renderPage(currentPage + 1);
        }
    }//GEN-LAST:event_BtnNextPageActionPerformed

    private void BtnExportExcelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnExportExcelActionPerformed
        exportToExcelActionPerformed(evt);
    }//GEN-LAST:event_BtnExportExcelActionPerformed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        cancelActiveWorker();
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void tbDetailHarianMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbDetailHarianMouseClicked
        if (evt.getClickCount() == 2 && tbDetailHarian.getSelectedRow() != -1) {
            int row = tbDetailHarian.getSelectedRow();
            Object nrObj = tbDetailHarian.getValueAt(row, 1); // Kolom 1 adalah "No. Rawat"
            if (nrObj != null) {
                String noRawat = nrObj.toString().trim();
                if (!noRawat.isEmpty()) {
                    tampilNota(noRawat);
                }
            }
        }
    }//GEN-LAST:event_tbDetailHarianMouseClicked

    private void setupLookupDialogs() {
        dokter.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dokter.getTable().getSelectedRow() != -1) {
                    KdDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 0).toString());
                    NmDokter.setText(dokter.getTable().getValueAt(dokter.getTable().getSelectedRow(), 1).toString());
                    onCategoryFilterChanged();
                }
            }
        });

        poli.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (poli.getTable().getSelectedRow() != -1) {
                    KdPoli.setText(poli.getTable().getValueAt(poli.getTable().getSelectedRow(), 0).toString());
                    NmPoli.setText(poli.getTable().getValueAt(poli.getTable().getSelectedRow(), 1).toString());
                    onCategoryFilterChanged();
                }
            }
        });

        penjab.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (penjab.getTable().getSelectedRow() != -1) {
                    KdCaraBayar.setText(penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(), 1).toString());
                    NmCaraBayar.setText(penjab.getTable().getValueAt(penjab.getTable().getSelectedRow(), 2).toString());
                    onCategoryFilterChanged();
                }
            }
        });
        penjab.getTable().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    penjab.dispose();
                }
            }
        });
    }

    private String getCurrentPeriodKey() {
        return Valid.SetTgl(Tgl1.getSelectedItem() + "") + "_" + Valid.SetTgl(Tgl2.getSelectedItem() + "");
    }

    /**
     * Dipanggil ketika pengguna memilih item pada ComboBox filter atau lookup Dokter/Poli/Penjamin.
     * Jika data pada periode tanggal terpilih sudah ada di memori, langsung disaring secara instan (<10ms).
     */
    private void onCategoryFilterChanged() {
        if (isUpdatingFilterUI || !isShowing()) {
            return;
        }
        final String periodKey = getCurrentPeriodKey();
        if (!cachedEpisodes.isEmpty() && periodKey.equals(dataPeriodKey)) {
            applyFilterFromCache();
        } else {
            loadDataAsync(false);
        }
    }

    private void resetFiltersAndLoad() {
        isUpdatingFilterUI = true;
        try {
            targetNoSep = "";
            targetNoRawat = "";
            targetNoRm = "";
            cmbStatusRawat.setSelectedIndex(0);
            cmbKategoriOperasi.setSelectedIndex(0);
            cmbKategoriICU.setSelectedIndex(0);
            cmbKategoriTandem.setSelectedIndex(0);
            cmbKategoriHD.setSelectedIndex(0);
            cmbTindakanKhusus.setSelectedIndex(0);
            cmbFilterBiaya.setSelectedIndex(0);
            KdDokter.setText("");
            NmDokter.setText("");
            KdPoli.setText("");
            NmPoli.setText("");
            KdCaraBayar.setText("");
            NmCaraBayar.setText("");
            TCari.setText("");
        } finally {
            isUpdatingFilterUI = false;
        }
        loadDataAsync(true);
    }

    private void cancelActiveWorker() {
        if (activeWorker != null && !activeWorker.isDone()) {
            activeWorker.cancel(true);
        }
        if (obatWorker != null && !obatWorker.isDone()) {
            obatWorker.cancel(true);
        }
    }

    /**
     * Membuka dan memuat data Detail Harian Pasien secara terarah dan matched
     * berdasarkan parameter pasien dari form pemanggil (misal: KeuanganBayarJMDokterBPJS).
     */
    public void setPasien(String tglAwal, String tglAkhir, String noSep, String noRawat, String noRm, String statusRawat) {
        this.targetNoSep = (noSep != null ? noSep.trim() : "");
        this.targetNoRawat = (noRawat != null ? noRawat.trim() : "");
        this.targetNoRm = (noRm != null ? noRm.trim() : "");

        isUpdatingFilterUI = true;
        try {
            if (tglAwal != null && !tglAwal.trim().isEmpty()) {
                Valid.SetTgl(Tgl1, tglAwal.trim());
            }
            if (tglAkhir != null && !tglAkhir.trim().isEmpty()) {
                Valid.SetTgl(Tgl2, tglAkhir.trim());
            }

            if (statusRawat != null) {
                if ("Ralan".equalsIgnoreCase(statusRawat)) {
                    cmbStatusRawat.setSelectedItem("Ralan");
                } else if ("Ranap".equalsIgnoreCase(statusRawat)) {
                    cmbStatusRawat.setSelectedItem("Ranap");
                } else {
                    cmbStatusRawat.setSelectedItem("Semua");
                }
            } else {
                cmbStatusRawat.setSelectedItem("Semua");
            }

            // Reset filter kategori lainnya agar tidak membatasi data pasien yang dicari
            cmbKategoriOperasi.setSelectedIndex(0);
            cmbKategoriICU.setSelectedIndex(0);
            cmbKategoriTandem.setSelectedIndex(0);
            cmbKategoriHD.setSelectedIndex(0);
            cmbTindakanKhusus.setSelectedIndex(0);
            cmbFilterBiaya.setSelectedIndex(0);
            KdDokter.setText("");
            NmDokter.setText("");
            KdPoli.setText("");
            NmPoli.setText("");
            KdCaraBayar.setText("");
            NmCaraBayar.setText("");

            // Kata kunci pencarian di TCari
            String keyword = "";
            if (!targetNoSep.isEmpty() && !"-".equals(targetNoSep)) {
                keyword = targetNoSep;
            } else if (!targetNoRawat.isEmpty()) {
                keyword = targetNoRawat;
            } else if (!targetNoRm.isEmpty()) {
                keyword = targetNoRm;
            }
            TCari.setText(keyword);
        } finally {
            isUpdatingFilterUI = false;
        }

        loadDataAsync(true);
    }

    private FilterSnapshot readFilterSnapshot() {
        return new FilterSnapshot(
                cmbStatusRawat.getSelectedItem() != null ? cmbStatusRawat.getSelectedItem().toString() : "Semua",
                cmbKategoriOperasi.getSelectedItem() != null ? cmbKategoriOperasi.getSelectedItem().toString() : "Semua",
                cmbKategoriICU.getSelectedItem() != null ? cmbKategoriICU.getSelectedItem().toString() : "Semua",
                cmbKategoriTandem.getSelectedItem() != null ? cmbKategoriTandem.getSelectedItem().toString() : "Semua",
                cmbKategoriHD.getSelectedItem() != null ? cmbKategoriHD.getSelectedItem().toString() : "Semua",
                cmbTindakanKhusus.getSelectedItem() != null ? cmbTindakanKhusus.getSelectedItem().toString() : "Semua",
                cmbFilterBiaya.getSelectedItem() != null ? cmbFilterBiaya.getSelectedItem().toString() : "Semua",
                KdDokter.getText().trim(),
                KdPoli.getText().trim(),
                KdCaraBayar.getText().trim(),
                ChkSertakanObat.isSelected(),
                TCari.getText().trim().toLowerCase(Locale.ROOT),
                targetNoSep,
                targetNoRawat,
                targetNoRm
        );
    }

    /**
     * Memuat episode pada rentang Tgl1..Tgl2 di background thread (SwingWorker).
     * Pengayaan data dilakukan per batch BATCH_SIZE episode dengan WHERE no_rawat IN (...),
     * dan tiap batch langsung di-publish agar baris tampil bertahap selama loading berjalan.
     */
    private void loadDataAsync(boolean forceReloadFromDb) {
        final String periodKey = getCurrentPeriodKey();
        // Periode sama (sudah dimuat / sedang dimuat): cukup saring ulang dari memori tanpa query ulang
        if (!forceReloadFromDb && periodKey.equals(dataPeriodKey) && !cachedEpisodes.isEmpty()) {
            applyFilterFromCache();
            return;
        }

        cancelActiveWorker();
        cachedEpisodes.clear();
        filteredRows.clear();
        filteredEpisodeCount = 0;
        currentPage = 0;
        Valid.tabelKosong(tabMode);
        dataPeriodKey = periodKey;
        loadingTotalEpisodes = -1;
        failedQueries.clear();
        includeObatRequested = ChkSertakanObat.isSelected();
        updatePageControls();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblStatusLoading.setText("Memuat daftar episode pasien...");

        final String tglAwal = Valid.SetTgl(Tgl1.getSelectedItem() + "");
        final String tglAkhir = Valid.SetTgl(Tgl2.getSelectedItem() + "");

        activeWorker = new SwingWorker<Void, List<EpisodeRecord>>() {
            @Override
            protected Void doInBackground() throws Exception {
                // 1. Cache referensi agar tidak ada query N+1 nama pegawai/dokter di dalam loop
                Map<String, String> mapPegawai = new HashMap<>();
                Map<String, Boolean> mapDokterUmum = new HashMap<>();
                Map<String, String> mapDpjpVClaimToRs = new HashMap<>();
                Map<String, String> mapDpjpVClaimToNama = new HashMap<>();
                loadReferenceCaches(mapPegawai, mapDokterUmum, mapDpjpVClaimToRs, mapDpjpVClaimToNama);
                if (isCancelled()) {
                    return null;
                }

                // 2. Daftar episode (basis Buku Bantu 2: billing.no = 'No.Nota')
                List<EpisodeRecord> episodes = loadEpisodeList(tglAwal, tglAkhir, mapDpjpVClaimToRs, mapDpjpVClaimToNama);
                loadingTotalEpisodes = episodes.size();
                publish(new ArrayList<EpisodeRecord>());

                // 3. Pengayaan per batch, lalu publish agar langsung tampil di tabel
                for (int start = 0; start < episodes.size(); start += BATCH_SIZE) {
                    if (isCancelled()) {
                        return null;
                    }
                    List<EpisodeRecord> batch = new ArrayList<>(
                            episodes.subList(start, Math.min(start + BATCH_SIZE, episodes.size())));
                    enrichBatch(batch, mapPegawai, mapDokterUmum, includeObatRequested);
                    publish(batch);
                }
                return null;
            }

            @Override
            protected void process(List<List<EpisodeRecord>> chunks) {
                if (isCancelled() || activeWorker != this) {
                    return;
                }
                FilterSnapshot f = readFilterSnapshot();
                for (List<EpisodeRecord> batch : chunks) {
                    for (EpisodeRecord ep : batch) {
                        cachedEpisodes.add(ep);
                        if (buildRowsForEpisode(ep, f, filteredRows) > 0) {
                            filteredEpisodeCount++;
                        }
                    }
                }
                // Isi halaman aktif (halaman 1) selama belum penuh 3.000 baris
                if (currentPage == 0) {
                    int pageStart = 0;
                    int pageEnd = Math.min(PAGE_SIZE, filteredRows.size());
                    int alreadyShown = tabMode.getRowCount();
                    if (alreadyShown < pageEnd) {
                        appendRowsToModel(alreadyShown, pageEnd);
                    }
                }
                updatePageControls();
                updateStatusLabel();
            }

            @Override
            protected void done() {
                if (activeWorker != this) {
                    return;
                }
                setCursor(Cursor.getDefaultCursor());
                if (isCancelled()) {
                    lblStatusLoading.setText("Pemuatan dibatalkan.");
                    return;
                }
                try {
                    get();
                } catch (Exception ex) {
                    System.out.println("Notifikasi SwingWorker Detail Harian Pasien : " + ex);
                    dataPeriodKey = "";
                    lblStatusLoading.setText("Gagal memuat data: " + ex.getMessage());
                    return;
                }
                updatePageControls();
                updateStatusLabel();
                if (includeObatRequested) {
                    startObatCatchUp();
                }
            }
        };
        activeWorker.execute();
    }

    private boolean isLoading() {
        return activeWorker != null && !activeWorker.isDone();
    }

    private void loadReferenceCaches(Map<String, String> mapPegawai, Map<String, Boolean> mapDokterUmum,
            Map<String, String> mapDpjpVClaimToRs, Map<String, String> mapDpjpVClaimToNama) {
        try (PreparedStatement psPeg = koneksi.prepareStatement("SELECT nik, nama FROM pegawai");
             ResultSet rsPeg = psPeg.executeQuery()) {
            while (rsPeg.next()) {
                mapPegawai.put(rsPeg.getString("nik"), rsPeg.getString("nama"));
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Cache Pegawai : " + e);
        }

        try (PreparedStatement psDok = koneksi.prepareStatement(
                "SELECT dokter.kd_dokter, dokter.nm_dokter, spesialis.nm_sps "
                + "FROM dokter LEFT JOIN spesialis ON dokter.kd_sps = spesialis.kd_sps");
             ResultSet rsDok = psDok.executeQuery()) {
            while (rsDok.next()) {
                String kd = rsDok.getString("kd_dokter");
                String sps = rsDok.getString("nm_sps");
                mapPegawai.put(kd, rsDok.getString("nm_dokter"));
                mapDokterUmum.put(kd, sps != null && sps.toLowerCase(Locale.ROOT).contains("umum"));
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Cache Dokter : " + e);
        }

        try (PreparedStatement psMap = koneksi.prepareStatement(
                "SELECT kd_dokter, kd_dokter_bpjs FROM maping_dokter_dpjpvclaim");
             ResultSet rsMap = psMap.executeQuery()) {
            while (rsMap.next()) {
                String kdBpjs = nvl(rsMap.getString("kd_dokter_bpjs"));
                String kdRs = nvl(rsMap.getString("kd_dokter"));
                mapDpjpVClaimToRs.put(kdBpjs, kdRs);
                mapDpjpVClaimToNama.put(kdBpjs, mapPegawai.getOrDefault(kdRs, kdBpjs));
            }
        } catch (Exception e) {
            System.out.println("Notifikasi Cache Mapping DPJP : " + e);
        }
    }

    /**
     * Mengambil daftar episode pasien berdasarkan billing.no = 'No.Nota' (Basis Buku Bantu 2).
     * Kolom status_klaim_bpjs diambil dari IFNULL(inacbg_klaim_umpanbalik.status, '').
     */
    private List<EpisodeRecord> loadEpisodeList(String tglAwal, String tglAkhir,
            Map<String, String> mapDpjpVClaimToRs, Map<String, String> mapDpjpVClaimToNama) throws Exception {
        List<EpisodeRecord> resultList = new ArrayList<>();
        String sqlEpisode = "SELECT "
                + "REPLACE(billing.nm_perawatan, ':', '') AS no_nota, "
                + "billing.tgl_byr, "
                + "MONTHNAME(billing.tgl_byr) AS namabulan, "
                + "reg_periksa.tgl_registrasi, "
                + "reg_periksa.no_rkm_medis, "
                + "pasien.nm_pasien, "
                + "reg_periksa.kd_poli, "
                + "poliklinik.nm_poli, "
                + "reg_periksa.kd_dokter, "
                + "dokter.nm_dokter, "
                + "reg_periksa.status_lanjut, "
                + "reg_periksa.kd_pj, "
                + "penjab.png_jawab, "
                + "bridging_sep.no_sep, "
                + "bridging_sep.kddpjp, "
                + "bridging_sep.klsnaik, "
                + "IFNULL(inacbg_klaim_umpanbalik.status, '') AS status_klaim_bpjs, "
                + "inacbg_klaim_umpanbalik.bydisetujui, "
                + "inacbg_klaim_umpanbalik.bypengajuan, "
                + "inacbg_klaim_umpanbalik.bytarifgrouper, "
                + "inacbg_klaim_umpanbalik.bytarifrs, "
                + "inacbg_klaim_umpanbalik.bytopup, "
                + "inacbg_klaim_umpanbalik.nofpk, "
                + "inacbg_grouping_stage12.tarif AS tarif_stage12, "
                + "piutang_pasien.uangmuka AS ekses_piutang, "
                + "billing.no_rawat "
                + "FROM billing "
                + "INNER JOIN reg_periksa ON billing.no_rawat = reg_periksa.no_rawat "
                + "INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis "
                + "INNER JOIN poliklinik ON reg_periksa.kd_poli = poliklinik.kd_poli "
                + "INNER JOIN dokter ON reg_periksa.kd_dokter = dokter.kd_dokter "
                + "INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj "
                + "LEFT JOIN bridging_sep ON reg_periksa.no_rawat = bridging_sep.no_rawat "
                + "LEFT JOIN inacbg_klaim_umpanbalik ON bridging_sep.no_sep = inacbg_klaim_umpanbalik.no_sep "
                + "LEFT JOIN inacbg_grouping_stage12 ON bridging_sep.no_sep = inacbg_grouping_stage12.no_sep "
                + "LEFT JOIN piutang_pasien ON reg_periksa.no_rawat = piutang_pasien.no_rawat "
                + "WHERE billing.no = 'No.Nota' AND billing.tgl_byr BETWEEN ? AND ? "
                + "ORDER BY billing.tgl_byr ASC, billing.nm_perawatan ASC";

        Map<String, Integer> sepCountMap = new HashMap<>();
        try (PreparedStatement psEp = koneksi.prepareStatement(sqlEpisode)) {
            psEp.setString(1, tglAwal);
            psEp.setString(2, tglAkhir);
            try (ResultSet rsEp = psEp.executeQuery()) {
                int idx = 1;
                while (rsEp.next()) {
                    EpisodeRecord ep = new EpisodeRecord();
                    ep.episodeIndex = idx++;
                    ep.noRawat = nvl(rsEp.getString("no_rawat"));
                    ep.noNota = nvl(rsEp.getString("no_nota")).trim();
                    ep.tglByr = nvl(rsEp.getString("tgl_byr"));
                    ep.namaBulan = nvl(rsEp.getString("namabulan"));
                    ep.tglRegistrasi = nvl(rsEp.getString("tgl_registrasi"));
                    ep.noRM = nvl(rsEp.getString("no_rkm_medis"));
                    ep.nmPasien = nvl(rsEp.getString("nm_pasien"));
                    ep.kdPoli = nvl(rsEp.getString("kd_poli"));
                    ep.nmPoli = nvl(rsEp.getString("nm_poli"));
                    ep.kdDokterReg = nvl(rsEp.getString("kd_dokter"));
                    ep.nmDokterReg = nvl(rsEp.getString("nm_dokter"));
                    ep.statusLanjut = nvl(rsEp.getString("status_lanjut"));
                    ep.kdPjUtama = nvl(rsEp.getString("kd_pj"));
                    ep.pngJawabUtama = nvl(rsEp.getString("png_jawab"));
                    ep.noSep = nvl(rsEp.getString("no_sep"));

                    // Ambil status klaim terutama untuk pasien BPJS, selain BPJS diberi string kosong ""
                    String statusKlaimDb = nvl(rsEp.getString("status_klaim_bpjs"));
                    boolean isBpjs = (!ep.noSep.isEmpty())
                            || (ep.pngJawabUtama != null && ep.pngJawabUtama.toUpperCase(Locale.ROOT).contains("BPJS"))
                            || (ep.kdPjUtama != null && ep.kdPjUtama.toUpperCase(Locale.ROOT).contains("BPJ"));
                    ep.statusKlaim = isBpjs ? statusKlaimDb : "";

                    // DPJP SEP: bridging_sep.kddpjp -> maping_dokter_dpjpvclaim -> kd_dokter RS
                    String kdDpjpBpjs = nvl(rsEp.getString("kddpjp"));
                    String mappedKdRs = mapDpjpVClaimToRs.getOrDefault(kdDpjpBpjs, "");
                    String nmDpjpBpjs = mapDpjpVClaimToNama.getOrDefault(kdDpjpBpjs, kdDpjpBpjs);
                    if (!kdDpjpBpjs.isEmpty()) {
                        boolean matchReg = !mappedKdRs.isEmpty() && mappedKdRs.equals(ep.kdDokterReg);
                        ep.dpjpSepVClaim = nmDpjpBpjs + (matchReg ? " (Sesuai)" : "");
                    }

                    String klsNaik = nvl(rsEp.getString("klsnaik"));
                    if ("6".equals(klsNaik) || "7".equals(klsNaik)) {
                        ep.kategoriICU = "ICU Murni";
                    }

                    double grouper = rsEp.getDouble("bytarifgrouper");
                    if (grouper <= 0) {
                        grouper = rsEp.getDouble("tarif_stage12");
                    }
                    ep.byTarifGrouper = grouper;
                    ep.byDisetujui = rsEp.getDouble("bydisetujui");
                    ep.byTopUp = rsEp.getDouble("bytopup");
                    double byTarifRs = rsEp.getDouble("bytarifrs");
                    ep.selisihRS = (byTarifRs > 0 || ep.byDisetujui > 0) ? (byTarifRs - ep.byDisetujui) : 0.0;
                    ep.noFpk = nvl(rsEp.getString("nofpk"));
                    ep.totalEkses = rsEp.getDouble("ekses_piutang");

                    // Deteksi awal HD dari poliklinik
                    if (ep.nmPoli.toUpperCase(Locale.ROOT).contains("HEMODIALISA")
                            || ep.kdPoli.equalsIgnoreCase("HD")
                            || ep.kdPoli.equalsIgnoreCase("HDL")) {
                        ep.isHD = true;
                        ep.statusHD = "Hemodialisa";
                    }

                    if (!ep.noSep.isEmpty()) {
                        sepCountMap.put(ep.noSep, sepCountMap.getOrDefault(ep.noSep, 0) + 1);
                    }
                    resultList.add(ep);
                }
            }
        }

        // Tandai SEP Ganda: 1 SEP dipakai > 1 episode (Raber RJ)
        for (EpisodeRecord ep : resultList) {
            if (!ep.noSep.isEmpty() && sepCountMap.getOrDefault(ep.noSep, 0) > 1) {
                ep.isSepGanda = true;
            }
        }
        return resultList;
    }

    /**
     * Mengeksekusi satu query batch dengan placeholder {IN} diganti (?,?,...).
     */
    private void runBatchQuery(String label, String sqlTemplate,
            Map<String, List<EpisodeRecord>> byRawat, BatchRowHandler handler) {
        if (byRawat.isEmpty() || failedQueries.contains(label)) {
            return;
        }
        List<String> keys = new ArrayList<>(byRawat.keySet());
        StringBuilder ph = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            ph.append(i == 0 ? "?" : ",?");
        }
        int occurrences = 0;
        int pos = sqlTemplate.indexOf("{IN}");
        while (pos >= 0) {
            occurrences++;
            pos = sqlTemplate.indexOf("{IN}", pos + 4);
        }
        String sql = sqlTemplate.replace("{IN}", ph.toString());

        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            int idx = 1;
            for (int o = 0; o < occurrences; o++) {
                for (String key : keys) {
                    ps.setString(idx++, key);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    List<EpisodeRecord> eps = byRawat.get(nvl(rs.getString("no_rawat")));
                    if (eps != null) {
                        handler.handle(rs, eps);
                    }
                }
            }
        } catch (Exception e) {
            failedQueries.add(label);
            System.out.println("Notifikasi Detail Harian Pasien [" + label + "] : " + e);
        }
    }

    /**
     * Memperkaya 1 batch episode (maks. 40 episode) secara serempak.
     */
    private void enrichBatch(List<EpisodeRecord> batch, final Map<String, String> mapPegawai,
            final Map<String, Boolean> mapDokterUmum, boolean includeObat) {
        final Map<String, List<EpisodeRecord>> byRawat = new LinkedHashMap<>();
        final Map<String, List<EpisodeRecord>> byRawatRanap = new LinkedHashMap<>();
        final Map<EpisodeRecord, BatchScratch> scratch = new IdentityHashMap<>();
        for (EpisodeRecord ep : batch) {
            byRawat.computeIfAbsent(ep.noRawat, k -> new ArrayList<>()).add(ep);
            if ("Ranap".equalsIgnoreCase(ep.statusLanjut)) {
                byRawatRanap.computeIfAbsent(ep.noRawat, k -> new ArrayList<>()).add(ep);
            }
            scratch.put(ep, new BatchScratch());
        }
        final DateTimeFormatter dtfKamar = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // 1. Total billing RS per no_rawat
        runBatchQuery("billing_total",
                "SELECT no_rawat, SUM(totalbiaya) AS total FROM billing WHERE no_rawat IN ({IN}) GROUP BY no_rawat",
                byRawat, (rs, eps) -> {
                    double total = rs.getDouble("total");
                    for (EpisodeRecord ep : eps) {
                        ep.totalBillingRS = total;
                    }
                });

        // 2. Multi-DPJP dari dpjp_ranap (Utama didahulukan)
        runBatchQuery("dpjp_ranap",
                "SELECT dpjp_ranap.no_rawat, dpjp_ranap.kd_dokter, dokter.nm_dokter, dpjp_ranap.statusdpjp "
                + "FROM dpjp_ranap INNER JOIN dokter ON dpjp_ranap.kd_dokter = dokter.kd_dokter "
                + "WHERE dpjp_ranap.no_rawat IN ({IN}) "
                + "ORDER BY dpjp_ranap.no_rawat, CASE WHEN dpjp_ranap.statusdpjp = 'Utama' THEN 0 ELSE 1 END, dpjp_ranap.kd_dokter",
                byRawatRanap, (rs, eps) -> {
                    String kdD = nvl(rs.getString("kd_dokter"));
                    String nmD = nvl(rs.getString("nm_dokter"));
                    boolean utama = "Utama".equalsIgnoreCase(nvl(rs.getString("statusdpjp")));
                    for (EpisodeRecord ep : eps) {
                        if (utama) {
                            ep.dpjpUtamaDoctors.add(kdD);
                        }
                        scratch.get(ep).specialists.put(kdD, nmD + (utama ? " (Utama)" : ""));
                    }
                });

        // 3. Riwayat kamar, LOS, dan deteksi ICU / Alih Rawat ICU
        runBatchQuery("kamar_inap",
                "SELECT kamar_inap.no_rawat, bangsal.nm_bangsal, kamar_inap.tgl_masuk, kamar_inap.jam_masuk, "
                + "kamar_inap.tgl_keluar, kamar_inap.jam_keluar, kamar_inap.lama "
                + "FROM kamar_inap INNER JOIN kamar ON kamar_inap.kd_kamar = kamar.kd_kamar "
                + "INNER JOIN bangsal ON kamar.kd_bangsal = bangsal.kd_bangsal "
                + "WHERE kamar_inap.no_rawat IN ({IN}) "
                + "ORDER BY kamar_inap.no_rawat, kamar_inap.tgl_masuk ASC, kamar_inap.jam_masuk ASC",
                byRawatRanap, (rs, eps) -> {
                    String nmBangsal = nvl(rs.getString("nm_bangsal"));
                    int lamaKamar = rs.getInt("lama");
                    String upperB = nmBangsal.toUpperCase(Locale.ROOT);
                    boolean isIntensive = upperB.contains("ICU") || upperB.contains("ICCU")
                            || upperB.contains("NICU") || upperB.contains("PICU")
                            || upperB.contains("HCU") || upperB.contains("INTENSIVE");
                    double hours;
                    try {
                        String tglMasuk = rs.getString("tgl_masuk") + " " + rs.getString("jam_masuk");
                        String tglKeluar = rs.getString("tgl_keluar") + " " + rs.getString("jam_keluar");
                        LocalDateTime m = LocalDateTime.parse(tglMasuk, dtfKamar);
                        LocalDateTime k = (tglKeluar == null || tglKeluar.startsWith("0000-00-00"))
                                ? LocalDateTime.now()
                                : LocalDateTime.parse(tglKeluar, dtfKamar);
                        hours = Math.max(0, Duration.between(m, k).toHours());
                    } catch (Exception ignore) {
                        hours = lamaKamar * 24.0;
                    }
                    for (EpisodeRecord ep : eps) {
                        BatchScratch s = scratch.get(ep);
                        if (isIntensive) {
                            s.hasIcu = true;
                        } else {
                            s.hasNonIcu = true;
                        }
                        s.roomParts.add(nmBangsal + " (" + lamaKamar + " hr)");
                        s.totalHours += hours;
                    }
                });

        // 4. COB: plafon set_cob & multi-payor detail_piutang_pasien
        runBatchQuery("set_cob",
                "SELECT set_cob.no_rawat, set_cob.nominal, penjab.png_jawab "
                + "FROM set_cob INNER JOIN penjab ON set_cob.kd_pj = penjab.kd_pj "
                + "WHERE set_cob.no_rawat IN ({IN})",
                byRawat, (rs, eps) -> {
                    for (EpisodeRecord ep : eps) {
                        BatchScratch s = scratch.get(ep);
                        if (s.cobSetFound) {
                            continue;
                        }
                        s.cobSetFound = true;
                        ep.isCOB = true;
                        ep.plafonCOB = rs.getDouble("nominal");
                        s.cobParts.add("Plafon " + rs.getString("png_jawab") + ": " + Valid.SetAngka(ep.plafonCOB));
                    }
                });

        runBatchQuery("detail_piutang_pasien",
                "SELECT detail_piutang_pasien.no_rawat, detail_piutang_pasien.nama_bayar, detail_piutang_pasien.totalpiutang "
                + "FROM detail_piutang_pasien INNER JOIN penjab ON detail_piutang_pasien.kd_pj = penjab.kd_pj "
                + "WHERE detail_piutang_pasien.no_rawat IN ({IN})",
                byRawat, (rs, eps) -> {
                    String part = rs.getString("nama_bayar") + ": " + Valid.SetAngka(rs.getDouble("totalpiutang"));
                    for (EpisodeRecord ep : eps) {
                        BatchScratch s = scratch.get(ep);
                        s.countPayor++;
                        s.cobParts.add(part);
                    }
                });

        // 5. Akun bayar ekses (nota inap & nota jalan)
        runBatchQuery("detail_nota",
                "SELECT no_rawat, nama_bayar, besar_bayar FROM detail_nota_inap WHERE no_rawat IN ({IN}) "
                + "UNION ALL SELECT no_rawat, nama_bayar, besar_bayar FROM detail_nota_jalan WHERE no_rawat IN ({IN})",
                byRawat, (rs, eps) -> {
                    double byr = rs.getDouble("besar_bayar");
                    if (byr <= 0) {
                        return;
                    }
                    String part = rs.getString("nama_bayar") + " (" + Valid.SetAngka(byr) + ")";
                    for (EpisodeRecord ep : eps) {
                        BatchScratch s = scratch.get(ep);
                        s.sumNotaTunai += byr;
                        s.eksesParts.add(part);
                    }
                });

        // 6. Potongan klaim piutang & diskon IBS
        runBatchQuery("bayar_piutang",
                "SELECT no_rawat, IFNULL(SUM(diskon_piutang + tidak_terbayar), 0) AS diskon_klaim "
                + "FROM bayar_piutang WHERE no_rawat IN ({IN}) GROUP BY no_rawat",
                byRawat, (rs, eps) -> {
                    double diskon = rs.getDouble("diskon_klaim");
                    for (EpisodeRecord ep : eps) {
                        ep.totalPotongan += diskon;
                    }
                });

        runBatchQuery("diskon_ibs",
                "SELECT no_rawat, kode_paket, besar_pengurangan FROM diskon_ibs WHERE no_rawat IN ({IN})",
                byRawat, (rs, eps) -> {
                    double potIbs = rs.getDouble("besar_pengurangan");
                    if (potIbs <= 0) {
                        return;
                    }
                    for (EpisodeRecord ep : eps) {
                        ep.totalPotongan += potIbs;
                    }
                });

        // Finalisasi kamar/ICU, COB, dan ekses sebelum tindakan
        for (EpisodeRecord ep : batch) {
            BatchScratch s = scratch.get(ep);
            if (!s.roomParts.isEmpty()) {
                ep.riwayatKamar = String.join(" -> ", s.roomParts);
                ep.losHari = (int) Math.round(s.totalHours / 24.0);
            }
            if (s.hasIcu && s.hasNonIcu) {
                ep.kategoriICU = "Alih Rawat ICU";
            } else if (s.hasIcu) {
                ep.kategoriICU = "ICU Murni";
            }
            if (s.countPayor > 1) {
                ep.isCOB = true;
            }
            ep.statusCOB = ep.isCOB ? "COB" : "Non-COB";
            if (!s.cobParts.isEmpty()) {
                ep.rincianCOB = String.join(" | ", s.cobParts);
            }
            if (s.countPayor > 0 && ep.totalEkses <= 0 && s.sumNotaTunai > 0) {
                ep.totalEkses = s.sumNotaTunai;
            }
            if (!s.eksesParts.isEmpty()) {
                ep.akunBayarEkses = String.join(", ", s.eksesParts);
            }
        }

        // 7. Operasi lebih dulu agar dokter Operator & Anestesi diketahui sebelum tindakan lain
        runBatchQuery("operasi",
                "SELECT operasi.no_rawat, operasi.kode_paket, paket_operasi.nm_perawatan, operasi.status, "
                + "operasi.operator1, operasi.operator2, operasi.operator3, "
                + "operasi.asisten_operator1, operasi.asisten_operator2, operasi.asisten_operator3, "
                + "operasi.instrumen, operasi.dokter_anak, operasi.perawaat_resusitas, "
                + "operasi.dokter_anestesi, operasi.asisten_anestesi, operasi.asisten_anestesi2, "
                + "operasi.bidan, operasi.bidan2, operasi.bidan3, operasi.perawat_luar, "
                + "operasi.omloop, operasi.omloop2, operasi.omloop3, operasi.omloop4, operasi.omloop5, "
                + "operasi.dokter_pjanak, operasi.dokter_umum, "
                + "operasi.biayaoperator1, operasi.biayaoperator2, operasi.biayaoperator3, "
                + "operasi.biayaasisten_operator1, operasi.biayaasisten_operator2, operasi.biayaasisten_operator3, "
                + "operasi.biayainstrumen, operasi.biayadokter_anak, operasi.biayaperawaat_resusitas, "
                + "operasi.biayadokter_anestesi, operasi.biayaasisten_anestesi, operasi.biayaasisten_anestesi2, "
                + "operasi.biayabidan, operasi.biayabidan2, operasi.biayabidan3, operasi.biayaperawat_luar, "
                + "operasi.biayaalat, operasi.biayasewaok, operasi.akomodasi, operasi.bagian_rs, "
                + "operasi.biaya_omloop, operasi.biaya_omloop2, operasi.biaya_omloop3, operasi.biaya_omloop4, operasi.biaya_omloop5, "
                + "operasi.biayasarpras, operasi.biaya_dokter_pjanak, operasi.biaya_dokter_umum, "
                + "penjab.png_jawab "
                + "FROM operasi "
                + "INNER JOIN paket_operasi ON operasi.kode_paket = paket_operasi.kode_paket "
                + "INNER JOIN penjab ON paket_operasi.kd_pj = penjab.kd_pj "
                + "WHERE operasi.no_rawat IN ({IN})",
                byRawat, (rs, eps) -> {
                    for (EpisodeRecord ep : eps) {
                        applyOperasiRow(ep, rs, mapPegawai, mapDokterUmum, scratch.get(ep).specialists);
                    }
                });

        // 8. Tindakan Ralan (dokter, dokter & perawat, paramedis)
        runBatchQuery("rawat_jl_dr",
                "SELECT rawat_jl_dr.no_rawat, rawat_jl_dr.kd_jenis_prw, jns_perawatan.nm_perawatan, "
                + "kategori_perawatan.nm_kategori, rawat_jl_dr.kd_dokter, dokter.nm_dokter, "
                + "rawat_jl_dr.biaya_rawat, COUNT(rawat_jl_dr.kd_jenis_prw) AS jml, "
                + "SUM(rawat_jl_dr.biaya_rawat) AS total_biaya, SUM(rawat_jl_dr.tarif_tindakandr) AS total_jmdr, "
                + "penjab.png_jawab "
                + "FROM rawat_jl_dr "
                + "INNER JOIN jns_perawatan ON rawat_jl_dr.kd_jenis_prw = jns_perawatan.kd_jenis_prw "
                + "INNER JOIN kategori_perawatan ON jns_perawatan.kd_kategori = kategori_perawatan.kd_kategori "
                + "INNER JOIN dokter ON rawat_jl_dr.kd_dokter = dokter.kd_dokter "
                + "INNER JOIN penjab ON jns_perawatan.kd_pj = penjab.kd_pj "
                + "WHERE rawat_jl_dr.no_rawat IN ({IN}) "
                + "GROUP BY rawat_jl_dr.no_rawat, rawat_jl_dr.kd_jenis_prw, rawat_jl_dr.kd_dokter",
                byRawat, (rs, eps) -> {
                    for (EpisodeRecord ep : eps) {
                        processMedicalActionRow(ep, rs, true, "Ralan Dokter", mapDokterUmum, scratch.get(ep).specialists);
                    }
                });

        runBatchQuery("rawat_jl_drpr",
                "SELECT rawat_jl_drpr.no_rawat, rawat_jl_drpr.kd_jenis_prw, jns_perawatan.nm_perawatan, "
                + "kategori_perawatan.nm_kategori, rawat_jl_drpr.kd_dokter, dokter.nm_dokter, "
                + "rawat_jl_drpr.biaya_rawat, COUNT(rawat_jl_drpr.kd_jenis_prw) AS jml, "
                + "SUM(rawat_jl_drpr.biaya_rawat) AS total_biaya, SUM(rawat_jl_drpr.tarif_tindakandr) AS total_jmdr, "
                + "penjab.png_jawab "
                + "FROM rawat_jl_drpr "
                + "INNER JOIN jns_perawatan ON rawat_jl_drpr.kd_jenis_prw = jns_perawatan.kd_jenis_prw "
                + "INNER JOIN kategori_perawatan ON jns_perawatan.kd_kategori = kategori_perawatan.kd_kategori "
                + "INNER JOIN dokter ON rawat_jl_drpr.kd_dokter = dokter.kd_dokter "
                + "INNER JOIN penjab ON jns_perawatan.kd_pj = penjab.kd_pj "
                + "WHERE rawat_jl_drpr.no_rawat IN ({IN}) "
                + "GROUP BY rawat_jl_drpr.no_rawat, rawat_jl_drpr.kd_jenis_prw, rawat_jl_drpr.kd_dokter",
                byRawat, (rs, eps) -> {
                    for (EpisodeRecord ep : eps) {
                        processMedicalActionRow(ep, rs, true, "Ralan Dr & Pr", mapDokterUmum, scratch.get(ep).specialists);
                    }
                });

        runBatchQuery("rawat_jl_pr",
                "SELECT rawat_jl_pr.no_rawat, rawat_jl_pr.kd_jenis_prw, jns_perawatan.nm_perawatan, "
                + "kategori_perawatan.nm_kategori, rawat_jl_pr.nip, pegawai.nama, "
                + "rawat_jl_pr.biaya_rawat, COUNT(rawat_jl_pr.kd_jenis_prw) AS jml, "
                + "SUM(rawat_jl_pr.biaya_rawat) AS total_biaya, penjab.png_jawab "
                + "FROM rawat_jl_pr "
                + "INNER JOIN jns_perawatan ON rawat_jl_pr.kd_jenis_prw = jns_perawatan.kd_jenis_prw "
                + "INNER JOIN kategori_perawatan ON jns_perawatan.kd_kategori = kategori_perawatan.kd_kategori "
                + "INNER JOIN pegawai ON rawat_jl_pr.nip = pegawai.nik "
                + "INNER JOIN penjab ON jns_perawatan.kd_pj = penjab.kd_pj "
                + "WHERE rawat_jl_pr.no_rawat IN ({IN}) "
                + "GROUP BY rawat_jl_pr.no_rawat, rawat_jl_pr.kd_jenis_prw, rawat_jl_pr.nip",
                byRawat, (rs, eps) -> {
                    for (EpisodeRecord ep : eps) {
                        addParamedisRow(ep, rs);
                    }
                });

        // 9. Tindakan Ranap (hanya episode Ranap)
        runBatchQuery("rawat_inap_dr",
                "SELECT rawat_inap_dr.no_rawat, rawat_inap_dr.kd_jenis_prw, jns_perawatan_inap.nm_perawatan, "
                + "kategori_perawatan.nm_kategori, rawat_inap_dr.kd_dokter, dokter.nm_dokter, "
                + "rawat_inap_dr.biaya_rawat, COUNT(rawat_inap_dr.kd_jenis_prw) AS jml, "
                + "SUM(rawat_inap_dr.biaya_rawat) AS total_biaya, SUM(rawat_inap_dr.tarif_tindakandr) AS total_jmdr, "
                + "penjab.png_jawab "
                + "FROM rawat_inap_dr "
                + "INNER JOIN jns_perawatan_inap ON rawat_inap_dr.kd_jenis_prw = jns_perawatan_inap.kd_jenis_prw "
                + "INNER JOIN kategori_perawatan ON jns_perawatan_inap.kd_kategori = kategori_perawatan.kd_kategori "
                + "INNER JOIN dokter ON rawat_inap_dr.kd_dokter = dokter.kd_dokter "
                + "INNER JOIN penjab ON jns_perawatan_inap.kd_pj = penjab.kd_pj "
                + "WHERE rawat_inap_dr.no_rawat IN ({IN}) "
                + "GROUP BY rawat_inap_dr.no_rawat, rawat_inap_dr.kd_jenis_prw, rawat_inap_dr.kd_dokter",
                byRawatRanap, (rs, eps) -> {
                    for (EpisodeRecord ep : eps) {
                        processMedicalActionRow(ep, rs, false, "Ranap Dokter", mapDokterUmum, scratch.get(ep).specialists);
                    }
                });

        runBatchQuery("rawat_inap_drpr",
                "SELECT rawat_inap_drpr.no_rawat, rawat_inap_drpr.kd_jenis_prw, jns_perawatan_inap.nm_perawatan, "
                + "kategori_perawatan.nm_kategori, rawat_inap_drpr.kd_dokter, dokter.nm_dokter, "
                + "rawat_inap_drpr.biaya_rawat, COUNT(rawat_inap_drpr.kd_jenis_prw) AS jml, "
                + "SUM(rawat_inap_drpr.biaya_rawat) AS total_biaya, SUM(rawat_inap_drpr.tarif_tindakandr) AS total_jmdr, "
                + "penjab.png_jawab "
                + "FROM rawat_inap_drpr "
                + "INNER JOIN jns_perawatan_inap ON rawat_inap_drpr.kd_jenis_prw = jns_perawatan_inap.kd_jenis_prw "
                + "INNER JOIN kategori_perawatan ON jns_perawatan_inap.kd_kategori = kategori_perawatan.kd_kategori "
                + "INNER JOIN dokter ON rawat_inap_drpr.kd_dokter = dokter.kd_dokter "
                + "INNER JOIN penjab ON jns_perawatan_inap.kd_pj = penjab.kd_pj "
                + "WHERE rawat_inap_drpr.no_rawat IN ({IN}) "
                + "GROUP BY rawat_inap_drpr.no_rawat, rawat_inap_drpr.kd_jenis_prw, rawat_inap_drpr.kd_dokter",
                byRawatRanap, (rs, eps) -> {
                    for (EpisodeRecord ep : eps) {
                        processMedicalActionRow(ep, rs, false, "Ranap Dr & Pr", mapDokterUmum, scratch.get(ep).specialists);
                    }
                });

        runBatchQuery("rawat_inap_pr",
                "SELECT rawat_inap_pr.no_rawat, rawat_inap_pr.kd_jenis_prw, jns_perawatan_inap.nm_perawatan, "
                + "kategori_perawatan.nm_kategori, rawat_inap_pr.nip, pegawai.nama, "
                + "rawat_inap_pr.biaya_rawat, COUNT(rawat_inap_pr.kd_jenis_prw) AS jml, "
                + "SUM(rawat_inap_pr.biaya_rawat) AS total_biaya, penjab.png_jawab "
                + "FROM rawat_inap_pr "
                + "INNER JOIN jns_perawatan_inap ON rawat_inap_pr.kd_jenis_prw = jns_perawatan_inap.kd_jenis_prw "
                + "INNER JOIN kategori_perawatan ON jns_perawatan_inap.kd_kategori = kategori_perawatan.kd_kategori "
                + "INNER JOIN pegawai ON rawat_inap_pr.nip = pegawai.nik "
                + "INNER JOIN penjab ON jns_perawatan_inap.kd_pj = penjab.kd_pj "
                + "WHERE rawat_inap_pr.no_rawat IN ({IN}) "
                + "GROUP BY rawat_inap_pr.no_rawat, rawat_inap_pr.kd_jenis_prw, rawat_inap_pr.nip",
                byRawatRanap, (rs, eps) -> {
                    for (EpisodeRecord ep : eps) {
                        addParamedisRow(ep, rs);
                    }
                });

        // 10. Laboratorium & Radiologi
        runBatchQuery("periksa_lab",
                "SELECT periksa_lab.no_rawat, periksa_lab.kd_jenis_prw, jns_perawatan_lab.nm_perawatan, "
                + "periksa_lab.kd_dokter, dokter.nm_dokter, periksa_lab.biaya, "
                + "COUNT(periksa_lab.kd_jenis_prw) AS jml, SUM(periksa_lab.biaya) AS total_biaya, "
                + "SUM(periksa_lab.tarif_tindakan_dokter) AS total_jmdr, penjab.png_jawab "
                + "FROM periksa_lab "
                + "INNER JOIN jns_perawatan_lab ON periksa_lab.kd_jenis_prw = jns_perawatan_lab.kd_jenis_prw "
                + "INNER JOIN dokter ON periksa_lab.kd_dokter = dokter.kd_dokter "
                + "INNER JOIN penjab ON jns_perawatan_lab.kd_pj = penjab.kd_pj "
                + "WHERE periksa_lab.no_rawat IN ({IN}) "
                + "GROUP BY periksa_lab.no_rawat, periksa_lab.kd_jenis_prw, periksa_lab.kd_dokter",
                byRawat, (rs, eps) -> {
                    for (EpisodeRecord ep : eps) {
                        addPenunjangRow(ep, rs, "Dokter PK (Lab)", "Laboratorium", mapDokterUmum);
                    }
                });

        runBatchQuery("periksa_radiologi",
                "SELECT periksa_radiologi.no_rawat, periksa_radiologi.kd_jenis_prw, jns_perawatan_radiologi.nm_perawatan, "
                + "periksa_radiologi.kd_dokter, dokter.nm_dokter, periksa_radiologi.biaya, "
                + "COUNT(periksa_radiologi.kd_jenis_prw) AS jml, SUM(periksa_radiologi.biaya) AS total_biaya, "
                + "SUM(periksa_radiologi.tarif_tindakan_dokter) AS total_jmdr, penjab.png_jawab "
                + "FROM periksa_radiologi "
                + "INNER JOIN jns_perawatan_radiologi ON periksa_radiologi.kd_jenis_prw = jns_perawatan_radiologi.kd_jenis_prw "
                + "INNER JOIN dokter ON periksa_radiologi.kd_dokter = dokter.kd_dokter "
                + "INNER JOIN penjab ON jns_perawatan_radiologi.kd_pj = penjab.kd_pj "
                + "WHERE periksa_radiologi.no_rawat IN ({IN}) "
                + "GROUP BY periksa_radiologi.no_rawat, periksa_radiologi.kd_jenis_prw, periksa_radiologi.kd_dokter",
                byRawat, (rs, eps) -> {
                    for (EpisodeRecord ep : eps) {
                        addPenunjangRow(ep, rs, "Dokter Radiologi", "Radiologi", mapDokterUmum);
                    }
                });

        // 11. Tambahan & Potongan Biaya
        runBatchQuery("tambahan_biaya",
                "SELECT no_rawat, nama_biaya, besar_biaya FROM tambahan_biaya WHERE no_rawat IN ({IN})",
                byRawat, (rs, eps) -> {
                    for (EpisodeRecord ep : eps) {
                        ActionItem item = new ActionItem();
                        item.nmPelaksana = ep.nmDokterReg;
                        item.peranDokter = "-";
                        item.kategoriLayanan = "Tambahan Biaya";
                        item.kodeTindakan = "TAMBAHAN";
                        item.namaLayanan = nvl(rs.getString("nama_biaya"));
                        item.biayaSatuan = rs.getDouble("besar_biaya");
                        item.qty = 1.0;
                        item.totalBiaya = rs.getDouble("besar_biaya");
                        item.jmDokter = 0.0;
                        item.payorBaris = ep.pngJawabUtama;
                        ep.actionItems.add(item);
                    }
                });

        runBatchQuery("pengurangan_biaya",
                "SELECT no_rawat, nama_pengurangan, besar_pengurangan FROM pengurangan_biaya WHERE no_rawat IN ({IN})",
                byRawat, (rs, eps) -> {
                    double besarPot = rs.getDouble("besar_pengurangan");
                    for (EpisodeRecord ep : eps) {
                        ep.totalPotongan += besarPot;
                        ActionItem item = new ActionItem();
                        item.nmPelaksana = ep.nmDokterReg;
                        item.peranDokter = "-";
                        item.kategoriLayanan = "Potongan Biaya";
                        item.kodeTindakan = "POTONGAN";
                        item.namaLayanan = nvl(rs.getString("nama_pengurangan"));
                        item.biayaSatuan = besarPot;
                        item.qty = 1.0;
                        item.totalBiaya = -1.0 * besarPot;
                        item.jmDokter = 0.0;
                        item.payorBaris = ep.pngJawabUtama;
                        ep.actionItems.add(item);
                    }
                });

        // 12. Obat & BHP hanya bila dicentang
        if (includeObat) {
            Map<EpisodeRecord, List<ActionItem>> obatSink = new IdentityHashMap<>();
            queryObatBatch(byRawat, obatSink);
            for (EpisodeRecord ep : batch) {
                List<ActionItem> items = obatSink.get(ep);
                if (items != null) {
                    ep.actionItems.addAll(items);
                }
                ep.obatLoaded = true;
            }
        }

        // 13. DPJP 1/2/3+, Kategori Tandem, peran dokter, dan normalitas visite per baris
        for (EpisodeRecord ep : batch) {
            finalizeEpisodeDoctorClassifications(ep, scratch.get(ep).specialists);
        }
    }

    private void queryObatBatch(Map<String, List<EpisodeRecord>> byRawat,
            final Map<EpisodeRecord, List<ActionItem>> sink) {
        runBatchQuery("beri_obat_operasi",
                "SELECT beri_obat_operasi.no_rawat, obatbhp_ok.kd_obat, obatbhp_ok.nm_obat, beri_obat_operasi.hargasatuan, "
                + "SUM(beri_obat_operasi.jumlah) AS jml, "
                + "SUM(beri_obat_operasi.hargasatuan * beri_obat_operasi.jumlah) AS total "
                + "FROM beri_obat_operasi INNER JOIN obatbhp_ok ON beri_obat_operasi.kd_obat = obatbhp_ok.kd_obat "
                + "WHERE beri_obat_operasi.no_rawat IN ({IN}) "
                + "GROUP BY beri_obat_operasi.no_rawat, beri_obat_operasi.kd_obat",
                byRawat, (rs, eps) -> {
                    for (EpisodeRecord ep : eps) {
                        ActionItem item = new ActionItem();
                        item.isObatOrBhp = true;
                        item.nmPelaksana = "Farmasi OK";
                        item.peranDokter = "Farmasi";
                        item.kategoriLayanan = "Obat/BHP Operasi";
                        item.kodeTindakan = nvl(rs.getString("kd_obat"));
                        item.namaLayanan = nvl(rs.getString("nm_obat"));
                        item.biayaSatuan = rs.getDouble("hargasatuan");
                        item.qty = rs.getDouble("jml");
                        item.totalBiaya = rs.getDouble("total");
                        item.jmDokter = 0.0;
                        item.payorBaris = ep.pngJawabUtama;
                        sink.computeIfAbsent(ep, k -> new ArrayList<>()).add(item);
                    }
                });

        runBatchQuery("detail_pemberian_obat",
                "SELECT detail_pemberian_obat.no_rawat, databarang.kode_brng, databarang.nama_brng, jenis.nama AS nm_jenis, "
                + "detail_pemberian_obat.biaya_obat, SUM(detail_pemberian_obat.jml) AS jml, "
                + "SUM(detail_pemberian_obat.total) AS total "
                + "FROM detail_pemberian_obat "
                + "INNER JOIN databarang ON detail_pemberian_obat.kode_brng = databarang.kode_brng "
                + "INNER JOIN jenis ON databarang.kdjns = jenis.kdjns "
                + "WHERE detail_pemberian_obat.no_rawat IN ({IN}) "
                + "GROUP BY detail_pemberian_obat.no_rawat, databarang.kode_brng, detail_pemberian_obat.biaya_obat",
                byRawat, (rs, eps) -> {
                    for (EpisodeRecord ep : eps) {
                        ActionItem item = new ActionItem();
                        item.isObatOrBhp = true;
                        item.nmPelaksana = "Instalasi Farmasi";
                        item.peranDokter = "Farmasi";
                        item.kategoriLayanan = "Obat & BHP";
                        item.kodeTindakan = nvl(rs.getString("kode_brng"));
                        item.namaLayanan = nvl(rs.getString("nama_brng")) + " (" + nvl(rs.getString("nm_jenis")) + ")";
                        item.biayaSatuan = rs.getDouble("biaya_obat");
                        item.qty = rs.getDouble("jml");
                        item.totalBiaya = rs.getDouble("total");
                        item.jmDokter = 0.0;
                        item.payorBaris = ep.pngJawabUtama;
                        sink.computeIfAbsent(ep, k -> new ArrayList<>()).add(item);
                    }
                });
    }

    private void startObatCatchUp() {
        List<EpisodeRecord> pending = new ArrayList<>();
        for (EpisodeRecord ep : cachedEpisodes) {
            if (!ep.obatLoaded) {
                pending.add(ep);
            }
        }
        if (pending.isEmpty()) {
            applyFilterFromCache();
            return;
        }
        loadObatAsync(pending);
    }

    private void loadObatAsync(final List<EpisodeRecord> pending) {
        if (obatWorker != null && !obatWorker.isDone()) {
            obatWorker.cancel(true);
        }
        final String periodAtStart = dataPeriodKey;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblStatusLoading.setText("Memuat data Obat & BHP untuk " + formatAngka(pending.size()) + " episode...");

        obatWorker = new SwingWorker<Map<EpisodeRecord, List<ActionItem>>, Void>() {
            @Override
            protected Map<EpisodeRecord, List<ActionItem>> doInBackground() throws Exception {
                Map<EpisodeRecord, List<ActionItem>> sink = new IdentityHashMap<>();
                for (int start = 0; start < pending.size(); start += BATCH_SIZE) {
                    if (isCancelled()) {
                        return sink;
                    }
                    Map<String, List<EpisodeRecord>> byRawat = new LinkedHashMap<>();
                    for (EpisodeRecord ep : pending.subList(start, Math.min(start + BATCH_SIZE, pending.size()))) {
                        byRawat.computeIfAbsent(ep.noRawat, k -> new ArrayList<>()).add(ep);
                    }
                    queryObatBatch(byRawat, sink);
                }
                return sink;
            }

            @Override
            protected void done() {
                if (obatWorker != this) {
                    return;
                }
                setCursor(Cursor.getDefaultCursor());
                if (isCancelled() || !periodAtStart.equals(dataPeriodKey)) {
                    return;
                }
                try {
                    Map<EpisodeRecord, List<ActionItem>> sink = get();
                    for (EpisodeRecord ep : pending) {
                        List<ActionItem> items = sink.get(ep);
                        if (items != null) {
                            ep.actionItems.addAll(items);
                        }
                        ep.obatLoaded = true;
                    }
                } catch (Exception ex) {
                    System.out.println("Notifikasi SwingWorker Obat Detail Harian Pasien : " + ex);
                }
                applyFilterFromCache();
            }
        };
        obatWorker.execute();
    }

    private void applyOperasiRow(EpisodeRecord ep, ResultSet rsO, Map<String, String> mapPegawai,
            Map<String, Boolean> mapDokterUmum, Map<String, String> uniqueSpecialistDoctors) throws Exception {
        ep.hasOperasi = true;
        ep.kategoriOperasi = "Operasi";
        String kdPaket = nvl(rsO.getString("kode_paket"));
        String nmOp = nvl(rsO.getString("nm_perawatan"));
        String payorOp = nvl(rsO.getString("png_jawab"));
        String lowerOp = nmOp.toLowerCase(Locale.ROOT);

        String tagOpKhusus = "";
        if (lowerOp.contains("av shunt")) {
            tagOpKhusus = "AV Shunt";
            ep.tindakanKhususEpisode = "AV Shunt";
        } else if (lowerOp.contains("eswl")) {
            tagOpKhusus = "ESWL";
            ep.tindakanKhususEpisode = "ESWL";
        } else if (lowerOp.contains("double lumen") || lowerOp.contains("cdl")) {
            tagOpKhusus = "CDL / Double Lumen";
            ep.tindakanKhususEpisode = "CDL / Double Lumen";
        }

        // Komponen dokter
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("operator1"), rsO.getDouble("biayaoperator1"), "Operator 1", true, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("operator2"), rsO.getDouble("biayaoperator2"), "Operator 2", true, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("operator3"), rsO.getDouble("biayaoperator3"), "Operator 3", true, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("dokter_anestesi"), rsO.getDouble("biayadokter_anestesi"), "Anestesi", true, true, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("dokter_anak"), rsO.getDouble("biayadokter_anak"), "Dokter Anak", true, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("dokter_pjanak"), rsO.getDouble("biaya_dokter_pjanak"), "Dokter PJ Anak", true, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("dokter_umum"), rsO.getDouble("biaya_dokter_umum"), "Dokter Umum OP", true, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);

        // Komponen asisten / paramedis OK
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("asisten_operator1"), rsO.getDouble("biayaasisten_operator1"), "Asisten Op 1", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("asisten_operator2"), rsO.getDouble("biayaasisten_operator2"), "Asisten Op 2", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("asisten_operator3"), rsO.getDouble("biayaasisten_operator3"), "Asisten Op 3", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("asisten_anestesi"), rsO.getDouble("biayaasisten_anestesi"), "Asisten Anestesi 1", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("asisten_anestesi2"), rsO.getDouble("biayaasisten_anestesi2"), "Asisten Anestesi 2", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("instrumen"), rsO.getDouble("biayainstrumen"), "Instrumen OK", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("perawaat_resusitas"), rsO.getDouble("biayaperawaat_resusitas"), "Perawat Resusitas", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("bidan"), rsO.getDouble("biayabidan"), "Bidan 1", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("bidan2"), rsO.getDouble("biayabidan2"), "Bidan 2", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("bidan3"), rsO.getDouble("biayabidan3"), "Bidan 3", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("perawat_luar"), rsO.getDouble("biayaperawat_luar"), "Perawat Luar", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("omloop"), rsO.getDouble("biaya_omloop"), "Omloop 1", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("omloop2"), rsO.getDouble("biaya_omloop2"), "Omloop 2", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("omloop3"), rsO.getDouble("biaya_omloop3"), "Omloop 3", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("omloop4"), rsO.getDouble("biaya_omloop4"), "Omloop 4", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);
        addOperasiComponentRow(ep, kdPaket, nmOp, payorOp, tagOpKhusus,
                rsO.getString("omloop5"), rsO.getDouble("biaya_omloop5"), "Omloop 5", false, false, mapPegawai, mapDokterUmum, uniqueSpecialistDoctors);

        double sarprasOk = rsO.getDouble("biayaalat") + rsO.getDouble("biayasewaok")
                + rsO.getDouble("akomodasi") + rsO.getDouble("bagian_rs") + rsO.getDouble("biayasarpras");
        if (sarprasOk > 0) {
            ActionItem itemSarpras = new ActionItem();
            itemSarpras.nmPelaksana = "Instalasi Kamar Operasi";
            itemSarpras.peranDokter = "Sarpras OK";
            itemSarpras.kategoriLayanan = "Operasi (Sarpras/OK)";
            itemSarpras.kodeTindakan = kdPaket;
            itemSarpras.namaLayanan = nmOp + " (Sewa OK/Alat/Sarpras)";
            itemSarpras.tagTindakanKhusus = tagOpKhusus;
            itemSarpras.biayaSatuan = sarprasOk;
            itemSarpras.qty = 1.0;
            itemSarpras.totalBiaya = sarprasOk;
            itemSarpras.jmDokter = 0.0;
            itemSarpras.payorBaris = payorOp;
            ep.actionItems.add(itemSarpras);
        }
    }

    private void addOperasiComponentRow(
            EpisodeRecord ep, String kdPaket, String nmOp, String payorOp, String tagOpKhusus,
            String kdPetugas, double biayaKomponen, String labelPeran,
            boolean isDoctorRole, boolean isAnestesiRole,
            Map<String, String> mapPegawai, Map<String, Boolean> mapDokterUmum,
            Map<String, String> uniqueSpecialistDoctors) {

        if (biayaKomponen <= 0) {
            return;
        }
        String kd = nvl(kdPetugas).trim();
        if (kd.isEmpty() || "-".equals(kd)) {
            return;
        }
        String nama = mapPegawai.getOrDefault(kd, kd);

        ActionItem item = new ActionItem();
        item.kdDokterOrNip = kd;
        item.nmPelaksana = nama;
        item.isDokter = isDoctorRole;
        item.isDokterUmum = isDoctorRole && mapDokterUmum.getOrDefault(kd, false);
        item.peranDokter = labelPeran;
        item.subKategoriOpDokter = isAnestesiRole ? "Anestesi OP" : (isDoctorRole ? "Bedah + Tindakan" : "Tim OK");
        item.kategoriLayanan = "Operasi (" + labelPeran + ")";
        item.kodeTindakan = kdPaket;
        item.namaLayanan = nmOp;
        item.tagTindakanKhusus = tagOpKhusus;
        item.biayaSatuan = biayaKomponen;
        item.qty = 1.0;
        item.totalBiaya = biayaKomponen;
        item.jmDokter = isDoctorRole ? biayaKomponen : 0.0;
        item.payorBaris = payorOp;

        if (isDoctorRole) {
            if (isAnestesiRole) {
                ep.anestesiDoctors.add(kd);
            } else {
                ep.operatorDoctors.add(kd);
                if (!item.isDokterUmum && !uniqueSpecialistDoctors.containsKey(kd)) {
                    uniqueSpecialistDoctors.put(kd, nama + " (Op)");
                }
            }
        }
        ep.actionItems.add(item);
    }

    private void processMedicalActionRow(
            EpisodeRecord ep, ResultSet rs, boolean isRalan, String defaultKat,
            Map<String, Boolean> mapDokterUmum, Map<String, String> uniqueSpecialistDoctors) throws Exception {

        ActionItem item = new ActionItem();
        item.kdDokterOrNip = nvl(rs.getString("kd_dokter"));
        item.nmPelaksana = nvl(rs.getString("nm_dokter"));
        item.isDokter = true;
        item.isDokterUmum = mapDokterUmum.getOrDefault(item.kdDokterOrNip, false);
        item.kategoriLayanan = nvl(rs.getString("nm_kategori"));
        if (item.kategoriLayanan.isEmpty()) {
            item.kategoriLayanan = defaultKat;
        }
        item.kodeTindakan = nvl(rs.getString("kd_jenis_prw"));
        item.namaLayanan = nvl(rs.getString("nm_perawatan"));
        item.biayaSatuan = rs.getDouble("biaya_rawat");
        item.qty = rs.getDouble("jml");
        item.totalBiaya = rs.getDouble("total_biaya");
        item.jmDokter = rs.getDouble("total_jmdr");
        item.payorBaris = nvl(rs.getString("png_jawab"));

        inspectSpecialFlags(ep, item);

        // Deteksi Visite Spesialis Ranap sesuai KeuanganBayarJMDokterBPJS
        String upperNm = item.namaLayanan.toUpperCase(Locale.ROOT);
        boolean isVisiteAction = upperNm.contains("VISITE DOKTER SPESIALIS")
                || upperNm.contains("JASA TELEKONSULTASI DR SPESIALIS")
                || upperNm.contains("JASA KONSULTASI DR SPESIALIS")
                || upperNm.contains("VISITE ICU")
                || upperNm.contains("VISITE DOKTER R.ISOLASI");

        if (!isRalan && isVisiteAction && item.jmDokter > 0) {
            int addCount = (int) Math.max(1, Math.round(item.qty));
            ep.doctorVisitCounts.put(item.kdDokterOrNip,
                    ep.doctorVisitCounts.getOrDefault(item.kdDokterOrNip, 0) + addCount);
            if (!item.isDokterUmum && !uniqueSpecialistDoctors.containsKey(item.kdDokterOrNip)) {
                uniqueSpecialistDoctors.put(item.kdDokterOrNip, item.nmPelaksana);
            }
        } else if (isRalan && !item.isDokterUmum && !uniqueSpecialistDoctors.containsKey(item.kdDokterOrNip)) {
            uniqueSpecialistDoctors.put(item.kdDokterOrNip, item.nmPelaksana);
        }

        ep.actionItems.add(item);
    }

    private void inspectSpecialFlags(EpisodeRecord ep, ActionItem item) {
        String nmUpper = item.namaLayanan.toUpperCase(Locale.ROOT);
        String katUpper = item.kategoriLayanan.toUpperCase(Locale.ROOT);

        if (nmUpper.contains("VISITE ICU") || nmUpper.contains("SPESIALIS - INTENSIVE")) {
            if ("Non ICU".equals(ep.kategoriICU)) {
                ep.kategoriICU = "Alih Rawat ICU";
            }
        }

        if (nmUpper.contains("SISTEM SINGLE USE") || nmUpper.contains("HEMODIALISA")) {
            ep.isHD = true;
            ep.statusHD = "Hemodialisa";
            item.tagTindakanKhusus = "Hemodialisa";
        }

        if (item.namaLayanan.contains("(KRM)") || item.namaLayanan.contains("(OT)")
                || nmUpper.contains("(OKUPASI)") || nmUpper.contains("REHAB MEDIK")) {
            ep.isTerapi = true;
            ep.statusTerapi = "Ya (KRM/OT)";
            item.tagTindakanKhusus = "Terapi (KRM/OT/Okupasi)";
            if ("Konsultasi/Visite".equals(ep.tindakanKhususEpisode)) {
                ep.tindakanKhususEpisode = "Terapi (KRM/OT/Okupasi)";
            }
        }

        if (nmUpper.contains("DOUBLE LUMEN") || nmUpper.contains("CDL")) {
            item.tagTindakanKhusus = "CDL / Double Lumen";
            ep.tindakanKhususEpisode = "CDL / Double Lumen";
        } else if (nmUpper.contains("AV SHUNT")) {
            item.tagTindakanKhusus = "AV Shunt";
            ep.tindakanKhususEpisode = "AV Shunt";
        } else if (nmUpper.contains("ESWL")) {
            item.tagTindakanKhusus = "ESWL";
            ep.tindakanKhususEpisode = "ESWL";
        } else if (nmUpper.contains("EEG")) {
            item.tagTindakanKhusus = "EEG";
            ep.tindakanKhususEpisode = "EEG";
        } else if (nmUpper.contains("AUDIOMETRI")) {
            item.tagTindakanKhusus = "Audiometri";
            ep.tindakanKhususEpisode = "Audiometri";
        } else if (item.isDokter && !katUpper.contains("VISIT & KONSULTASI") && !nmUpper.contains("SISTEM SINGLE USE")) {
            if (item.tagTindakanKhusus.isEmpty()) {
                item.tagTindakanKhusus = "Tindakan Poli";
            }
            if ("Konsultasi/Visite".equals(ep.tindakanKhususEpisode)) {
                ep.tindakanKhususEpisode = "Tindakan Poli";
            }
        } else if (item.tagTindakanKhusus.isEmpty()) {
            item.tagTindakanKhusus = "Konsultasi Saja";
        }
    }

    private void addParamedisRow(EpisodeRecord ep, ResultSet rs) throws Exception {
        ActionItem item = new ActionItem();
        item.kdDokterOrNip = nvl(rs.getString("nip"));
        item.nmPelaksana = nvl(rs.getString("nama"));
        item.peranDokter = "Paramedis";
        item.kategoriLayanan = nvl(rs.getString("nm_kategori"));
        item.kodeTindakan = nvl(rs.getString("kd_jenis_prw"));
        item.namaLayanan = nvl(rs.getString("nm_perawatan"));
        item.biayaSatuan = rs.getDouble("biaya_rawat");
        item.qty = rs.getDouble("jml");
        item.totalBiaya = rs.getDouble("total_biaya");
        item.jmDokter = 0.0;
        item.payorBaris = nvl(rs.getString("png_jawab"));
        inspectSpecialFlags(ep, item);
        ep.actionItems.add(item);
    }

    private void addPenunjangRow(EpisodeRecord ep, ResultSet rs, String peran, String kategori,
            Map<String, Boolean> mapDokterUmum) throws Exception {
        ActionItem item = new ActionItem();
        item.kdDokterOrNip = nvl(rs.getString("kd_dokter"));
        item.nmPelaksana = nvl(rs.getString("nm_dokter"));
        item.isDokter = true;
        item.isDokterUmum = mapDokterUmum.getOrDefault(item.kdDokterOrNip, false);
        item.peranDokter = peran;
        item.kategoriLayanan = kategori;
        item.kodeTindakan = nvl(rs.getString("kd_jenis_prw"));
        item.namaLayanan = nvl(rs.getString("nm_perawatan"));
        item.biayaSatuan = rs.getDouble("biaya");
        item.qty = rs.getDouble("jml");
        item.totalBiaya = rs.getDouble("total_biaya");
        item.jmDokter = rs.getDouble("total_jmdr");
        item.payorBaris = nvl(rs.getString("png_jawab"));
        ep.actionItems.add(item);
    }

    private void finalizeEpisodeDoctorClassifications(EpisodeRecord ep, Map<String, String> uniqueSpecialistDoctors) {
        if (uniqueSpecialistDoctors.isEmpty()) {
            uniqueSpecialistDoctors.put(ep.kdDokterReg, ep.nmDokterReg);
        }
        if (ep.dpjpUtamaDoctors.isEmpty()) {
            String firstKd = uniqueSpecialistDoctors.keySet().iterator().next();
            ep.dpjpUtamaDoctors.add(firstKd);
        }

        List<String> dpjpNames = new ArrayList<>(uniqueSpecialistDoctors.values());
        ep.jmlDpjp = dpjpNames.size();
        ep.dpjp1 = dpjpNames.get(0);
        ep.dpjp2 = dpjpNames.size() >= 2 ? dpjpNames.get(1) : "-";
        if (dpjpNames.size() >= 3) {
            ep.dpjp3Plus = String.join(" | ", dpjpNames.subList(2, dpjpNames.size()));
        } else {
            ep.dpjp3Plus = "-";
        }

        if (ep.jmlDpjp == 1) {
            ep.kategoriTandem = "Tunggal (1 Org)";
        } else if (ep.jmlDpjp == 2) {
            ep.kategoriTandem = "Tandem 2 Org";
        } else {
            ep.kategoriTandem = "Lebih Dari 2 Org";
        }

        for (ActionItem item : ep.actionItems) {
            if (!item.isDokter) {
                continue;
            }
            String kd = item.kdDokterOrNip;
            int vCount = ep.doctorVisitCounts.getOrDefault(kd, 0);
            item.jmlVisiteDokter = vCount;

            // Tentukan Peran & Kualifikasi Dokter
            if (item.isDokterUmum) {
                item.peranDokter = "Dokter Umum";
            } else if (ep.anestesiDoctors.contains(kd)) {
                item.peranDokter = "Anestesi";
            } else if (ep.dpjpUtamaDoctors.contains(kd)) {
                item.peranDokter = "DPJP Utama";
            } else if (ep.operatorDoctors.contains(kd)) {
                item.peranDokter = "DP (Operator)";
            } else if (item.peranDokter.isEmpty()) {
                item.peranDokter = "DP (Pendamping)";
            }

            // Tentukan Sub-Kategori Operasi per Dokter
            if (ep.hasOperasi) {
                if (ep.anestesiDoctors.contains(kd)) {
                    item.subKategoriOpDokter = "Anestesi OP";
                } else if (ep.operatorDoctors.contains(kd)) {
                    item.subKategoriOpDokter = "Bedah + Tindakan";
                } else {
                    item.subKategoriOpDokter = "Visite Pasien OP";
                }
            } else {
                item.subKategoriOpDokter = "Non-OP";
            }

            // Tentukan Normalitas Visite vs LOS untuk Rawat Inap
            if ("Ranap".equalsIgnoreCase(ep.statusLanjut)) {
                if (vCount == 0) {
                    item.statusVisiteVsLos = "Telekonsultasi (0)";
                } else if (ep.losHari >= 5 && vCount < 2) {
                    item.statusVisiteVsLos = "Tidak Normal (<2x / 5hr)";
                } else if (ep.losHari >= 2 && vCount < 1) {
                    item.statusVisiteVsLos = "Tidak Normal (0x / >=2hr)";
                } else {
                    item.statusVisiteVsLos = "Normal (" + vCount + "x / " + ep.losHari + "hr)";
                }
            } else {
                item.statusVisiteVsLos = "Ralan";
            }
        }
    }

    /**
     * Menyusun baris tabel untuk 1 episode berdasarkan snapshot filter.
     * Mengembalikan jumlah baris tindakan yang cocok dan ditambahkan ke daftar output.
     */
    private int buildRowsForEpisode(EpisodeRecord ep, FilterSnapshot f, List<Object[]> out) {
        // 1. Filter level Episode
        if (!"Semua".equals(f.fRawat) && !ep.statusLanjut.equalsIgnoreCase(f.fRawat)) {
            return 0;
        }
        if ("Non Operasi".equals(f.fOp) && ep.hasOperasi) {
            return 0;
        }
        if ("Operasi".equals(f.fOp) && !ep.hasOperasi) {
            return 0;
        }
        if (!"Semua".equals(f.fICU)) {
            if ("Semua ICU".equals(f.fICU)) {
                if ("Non ICU".equals(ep.kategoriICU)) {
                    return 0;
                }
            } else if (!ep.kategoriICU.equalsIgnoreCase(f.fICU)) {
                return 0;
            }
        }
        if (!"Semua".equals(f.fTandem)) {
            if ("SEP Ganda (Raber RJ)".equals(f.fTandem)) {
                if (!ep.isSepGanda) {
                    return 0;
                }
            } else if (!ep.kategoriTandem.equalsIgnoreCase(f.fTandem)) {
                return 0;
            }
        }
        if ("Pasien HD".equals(f.fHD) && !ep.isHD) {
            return 0;
        }
        if ("Bukan HD".equals(f.fHD) && ep.isHD) {
            return 0;
        }
        if ("Pasien COB".equals(f.fBiaya) && !ep.isCOB) {
            return 0;
        }
        if ("Ada Ekses".equals(f.fBiaya) && ep.totalEkses <= 0) {
            return 0;
        }
        if ("Ada Potongan".equals(f.fBiaya) && ep.totalPotongan <= 0) {
            return 0;
        }
        if (!f.fKdPoli.isEmpty() && !ep.kdPoli.equalsIgnoreCase(f.fKdPoli)) {
            return 0;
        }
        if (!f.fKdPj.isEmpty() && !ep.kdPjUtama.equalsIgnoreCase(f.fKdPj)) {
            return 0;
        }

        int added = 0;
        // 2. Filter level Baris Tindakan
        for (ActionItem item : ep.actionItems) {
            if (!f.includeObat && item.isObatOrBhp) {
                continue;
            }
            if ("Bedah + Tindakan".equals(f.fOp) && !"Bedah + Tindakan".equals(item.subKategoriOpDokter)) {
                continue;
            }
            if ("Visite Pasien OP".equals(f.fOp) && !"Visite Pasien OP".equals(item.subKategoriOpDokter)) {
                continue;
            }
            if ("Anestesi OP".equals(f.fOp) && !"Anestesi OP".equals(item.subKategoriOpDokter)) {
                continue;
            }
            if (!"Semua".equals(f.fKhusus)) {
                boolean matchItemTag = f.fKhusus.equalsIgnoreCase(item.tagTindakanKhusus)
                        || f.fKhusus.equalsIgnoreCase(ep.tindakanKhususEpisode);
                if (!matchItemTag) {
                    continue;
                }
            }
            if ("Dokter Spesialis".equals(f.fBiaya) && (!item.isDokter || item.isDokterUmum)) {
                continue;
            }
            if ("Dokter Umum".equals(f.fBiaya) && (!item.isDokter || !item.isDokterUmum)) {
                continue;
            }
            if ("Visite Tidak Normal".equals(f.fBiaya) && !item.statusVisiteVsLos.startsWith("Tidak Normal")) {
                continue;
            }
            if (!f.fKdDok.isEmpty()) {
                boolean matchDok = f.fKdDok.equalsIgnoreCase(item.kdDokterOrNip)
                        || f.fKdDok.equalsIgnoreCase(ep.kdDokterReg)
                        || ep.dpjpUtamaDoctors.contains(f.fKdDok);
                if (!matchDok) {
                    continue;
                }
            }
            if (!f.keyword.isEmpty()) {
                boolean matchKw = ep.noRawat.toLowerCase(Locale.ROOT).contains(f.keyword)
                        || ep.noNota.toLowerCase(Locale.ROOT).contains(f.keyword)
                        || ep.noRM.toLowerCase(Locale.ROOT).contains(f.keyword)
                        || ep.nmPasien.toLowerCase(Locale.ROOT).contains(f.keyword)
                        || ep.noSep.toLowerCase(Locale.ROOT).contains(f.keyword)
                        || ep.statusKlaim.toLowerCase(Locale.ROOT).contains(f.keyword)
                        || ep.dpjp1.toLowerCase(Locale.ROOT).contains(f.keyword)
                        || ep.dpjp2.toLowerCase(Locale.ROOT).contains(f.keyword)
                        || item.nmPelaksana.toLowerCase(Locale.ROOT).contains(f.keyword)
                        || item.namaLayanan.toLowerCase(Locale.ROOT).contains(f.keyword)
                        || item.kodeTindakan.toLowerCase(Locale.ROOT).contains(f.keyword);
                if (!matchKw) {
                    if (!f.targetNoRawat.isEmpty() && (ep.noRawat.equalsIgnoreCase(f.targetNoRawat) || (!f.targetNoRm.isEmpty() && ep.noRM.equalsIgnoreCase(f.targetNoRm)))) {
                        matchKw = true;
                    }
                }
                if (!matchKw) {
                    continue;
                }
            }

            out.add(new Object[]{
                ep.episodeIndex,
                ep.noRawat,
                ep.noNota,
                ep.tglByr,
                ep.namaBulan,
                ep.tglRegistrasi,
                ep.noRM,
                ep.nmPasien,
                ep.noSep,
                ep.statusKlaim, // Kolom 9: Status Klaim
                ep.isSepGanda ? "Ya" : "Tidak",
                ep.statusLanjut,
                ep.nmPoli,
                ep.nmDokterReg,
                ep.dpjpSepVClaim,
                ep.dpjp1,
                ep.dpjp2,
                ep.dpjp3Plus,
                ep.jmlDpjp,
                ep.kategoriTandem,
                ep.kategoriOperasi,
                item.subKategoriOpDokter,
                ep.kategoriICU,
                ep.riwayatKamar,
                ep.losHari,
                item.jmlVisiteDokter,
                item.statusVisiteVsLos,
                ep.statusHD,
                ep.statusTerapi,
                item.tagTindakanKhusus.isEmpty() ? ep.tindakanKhususEpisode : item.tagTindakanKhusus,
                ep.statusCOB,
                ep.plafonCOB,
                ep.rincianCOB,
                ep.totalPotongan,
                ep.totalEkses,
                ep.akunBayarEkses,
                item.nmPelaksana,
                item.peranDokter,
                item.kategoriLayanan,
                item.kodeTindakan,
                item.namaLayanan,
                item.biayaSatuan,
                item.qty,
                item.totalBiaya,
                item.jmDokter,
                ep.totalBillingRS,
                ep.byTarifGrouper,
                ep.byDisetujui,
                ep.byTopUp,
                ep.selisihRS,
                ep.noFpk,
                item.payorBaris.isEmpty() ? ep.pngJawabUtama : item.payorBaris
            });
            added++;
        }
        return added;
    }

    /**
     * Menyaring cachedEpisodes di memori berdasarkan seluruh ComboBox filter, lookup Dokter/Poli/Penjamin,
     * Checkbox Obat, dan Keyword TCari secara instan tanpa query ulang ke database.
     */
    private void applyFilterFromCache() {
        cancelActiveWorker();
        filteredRows.clear();
        filteredEpisodeCount = 0;
        currentPage = 0;
        Valid.tabelKosong(tabMode);

        if (cachedEpisodes.isEmpty()) {
            updatePageControls();
            lblStatusLoading.setText("0 baris ditemukan.");
            return;
        }

        FilterSnapshot f = readFilterSnapshot();
        for (EpisodeRecord ep : cachedEpisodes) {
            if (buildRowsForEpisode(ep, f, filteredRows) > 0) {
                filteredEpisodeCount++;
            }
        }

        renderPage(0);
        updateStatusLabel();
    }

    /**
     * Merender halaman tabel tertentu (maks. 3.000 baris per halaman) dari filteredRows.
     */
    private void renderPage(int page) {
        int totalRows = filteredRows.size();
        int totalPages = (int) Math.ceil((double) totalRows / PAGE_SIZE);
        if (totalPages <= 0) {
            totalPages = 1;
        }
        currentPage = Math.max(0, Math.min(page, totalPages - 1));

        Valid.tabelKosong(tabMode);
        int pageStart = currentPage * PAGE_SIZE;
        int pageEnd = Math.min(pageStart + PAGE_SIZE, totalRows);
        appendRowsToModel(pageStart, pageEnd);

        updatePageControls();
        if (tbDetailHarian.getRowCount() > 0) {
            tbDetailHarian.scrollRectToVisible(tbDetailHarian.getCellRect(0, 0, true));
        }
    }

    private void appendRowsToModel(int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex && i < filteredRows.size(); i++) {
            tabMode.addRow(filteredRows.get(i));
        }
    }

    private void updatePageControls() {
        int totalRows = filteredRows.size();
        int totalPages = (int) Math.ceil((double) totalRows / PAGE_SIZE);
        if (totalPages <= 0) {
            totalPages = 1;
        }
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }
        lblPageInfo.setText("Hal: " + (currentPage + 1) + " / " + totalPages);
        BtnPrevPage.setEnabled(currentPage > 0);
        BtnNextPage.setEnabled(currentPage < totalPages - 1);
    }

    private void updateStatusLabel() {
        if (isLoading()) {
            int loaded = cachedEpisodes.size();
            String totalStr = (loadingTotalEpisodes >= 0 ? String.valueOf(loadingTotalEpisodes) : "?");
            lblStatusLoading.setText("Memuat " + loaded + "/" + totalStr + " episode | Baris: " + formatAngka(filteredRows.size()));
        } else {
            lblStatusLoading.setText("Episode: " + formatAngka(filteredEpisodeCount) + " | Baris Tindakan: " + formatAngka(filteredRows.size()));
        }
    }

    private String formatAngka(int n) {
        return String.format(Locale.US, "%,d", n).replace(',', '.');
    }

    /**
     * Membuka popup rincian Nota Billing Pasien menggunakan JavaFX WebView (billingtampil.php).
     */
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
                    javax.swing.JDialog dlgNota = new javax.swing.JDialog(DlgDetailHarianPasien.this, "Nota Billing - " + norawat.trim(), true);
                    dlgNotaAktif = dlgNota;
                    dlgNota.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosed(java.awt.event.WindowEvent e) {
                            dlgNotaAktif = null;
                        }
                    });
                    dlgNota.setSize(750, 850);
                    dlgNota.setLocationRelativeTo(DlgDetailHarianPasien.this);

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

    /**
     * Mengekspor seluruh baris hasil filter (semua halaman) ke berkas Microsoft Excel (.xlsx)
     * menggunakan OpenXML ZipOutputStream di background thread tanpa memblokir GUI.
     */
    private void exportToExcelActionPerformed(ActionEvent evt) {
        if (filteredRows.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Maaf, data pada tabel masih kosong. Silakan tampilkan data terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan Export Excel Detail Harian Pasien");
        File defaultDir = new File(System.getProperty("user.home"), "Documents");
        if (!defaultDir.exists()) {
            defaultDir = new File(System.getProperty("user.home"), "Desktop");
        }
        if (defaultDir.exists()) {
            fileChooser.setCurrentDirectory(defaultDir);
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String defaultName = "Detail_Harian_Pasien_" + Valid.SetTgl(Tgl1.getSelectedItem() + "")
                + "_sd_" + Valid.SetTgl(Tgl2.getSelectedItem() + "") + "_" + timestamp + ".xlsx";
        fileChooser.setSelectedFile(new File(defaultDir.exists() ? defaultDir : new File("."), defaultName));
        fileChooser.setFileFilter(new FileNameExtensionFilter("Excel Files (*.xlsx)", "xlsx"));

        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File file = fileChooser.getSelectedFile();
        if (file == null) {
            return;
        }
        if (!file.getAbsolutePath().toLowerCase(Locale.ROOT).endsWith(".xlsx")) {
            file = new File(file.getAbsolutePath() + ".xlsx");
        }

        if (file.exists()) {
            try (FileOutputStream testFos = new FileOutputStream(file, true)) {
                // file dapat ditulis
            } catch (Exception exLock) {
                JOptionPane.showMessageDialog(this,
                        "Berkas '" + file.getName() + "' sedang dibuka di Microsoft Excel!\nSilakan tutup terlebih dahulu atau simpan dengan nama berbeda.",
                        "Berkas Terkunci", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        final File targetFile = file;
        final List<Object[]> exportData = new ArrayList<>(filteredRows);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblStatusLoading.setText("Mengekspor " + formatAngka(exportData.size()) + " baris ke Excel...");

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                writeExcelWorkbook(targetFile, exportData);
                return null;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                updateStatusLabel();
                try {
                    get();
                    int pilihan = JOptionPane.showConfirmDialog(DlgDetailHarianPasien.this,
                            "Export selesai (" + formatAngka(exportData.size()) + " baris).\nApakah berkas ingin langsung dibuka di Microsoft Excel?",
                            "Konfirmasi", JOptionPane.YES_NO_OPTION);
                    if (pilihan == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(targetFile);
                    }
                } catch (Exception ex) {
                    System.out.println("Notifikasi Export Excel : " + ex);
                    JOptionPane.showMessageDialog(DlgDetailHarianPasien.this,
                            "Gagal menulis file Excel: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void writeExcelWorkbook(File file, List<Object[]> exportData) throws Exception {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
            Charset utf8 = StandardCharsets.UTF_8;

            zos.putNextEntry(new ZipEntry("[Content_Types].xml"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">\n"
                    + "  <Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>\n"
                    + "  <Default Extension=\"xml\" ContentType=\"application/xml\"/>\n"
                    + "  <Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>\n"
                    + "  <Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>\n"
                    + "  <Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>\n"
                    + "</Types>").getBytes(utf8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("_rels/.rels"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n"
                    + "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>\n"
                    + "</Relationships>").getBytes(utf8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("xl/_rels/workbook.xml.rels"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">\n"
                    + "  <Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>\n"
                    + "  <Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>\n"
                    + "</Relationships>").getBytes(utf8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("xl/workbook.xml"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">\n"
                    + "  <sheets>\n"
                    + "    <sheet name=\"Detail Harian Pasien\" sheetId=\"1\" r:id=\"rId1\"/>\n"
                    + "  </sheets>\n"
                    + "</workbook>").getBytes(utf8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("xl/styles.xml"));
            zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                    + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n"
                    + "  <numFmts count=\"1\">\n"
                    + "    <numFmt numFmtId=\"164\" formatCode=\"#,##0\"/>\n"
                    + "  </numFmts>\n"
                    + "  <fonts count=\"3\">\n"
                    + "    <font><name val=\"Tahoma\"/><sz val=\"10\"/></font>\n"
                    + "    <font><b/><color rgb=\"FFFFFFFF\"/><name val=\"Tahoma\"/><sz val=\"10\"/></font>\n"
                    + "    <font><b/><name val=\"Tahoma\"/><sz val=\"10\"/></font>\n"
                    + "  </fonts>\n"
                    + "  <fills count=\"3\">\n"
                    + "    <fill><patternFill patternType=\"none\"/></fill>\n"
                    + "    <fill><patternFill patternType=\"gray125\"/></fill>\n"
                    + "    <fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF2E7D32\"/></patternFill></fill>\n"
                    + "  </fills>\n"
                    + "  <borders count=\"2\">\n"
                    + "    <border><left/><right/><top/><bottom/></border>\n"
                    + "    <border>\n"
                    + "      <left style=\"thin\"><color rgb=\"FFCCCCCC\"/></left>\n"
                    + "      <right style=\"thin\"><color rgb=\"FFCCCCCC\"/></right>\n"
                    + "      <top style=\"thin\"><color rgb=\"FFCCCCCC\"/></top>\n"
                    + "      <bottom style=\"thin\"><color rgb=\"FFCCCCCC\"/></bottom>\n"
                    + "    </border>\n"
                    + "  </borders>\n"
                    + "  <cellXfs count=\"5\">\n"
                    + "    <xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" applyBorder=\"1\"/>\n"
                    + "    <xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\"/>\n"
                    + "    <xf numFmtId=\"164\" fontId=\"0\" fillId=\"0\" borderId=\"1\" applyNumberFormat=\"1\" applyBorder=\"1\"/>\n"
                    + "    <xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"1\" applyFont=\"1\" applyBorder=\"1\"/>\n"
                    + "    <xf numFmtId=\"164\" fontId=\"2\" fillId=\"0\" borderId=\"1\" applyNumberFormat=\"1\" applyFont=\"1\" applyBorder=\"1\"/>\n"
                    + "  </cellXfs>\n"
                    + "</styleSheet>").getBytes(utf8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("xl/worksheets/sheet1.xml"));
            writeSheetData(zos, exportData, utf8);
            zos.closeEntry();
        }
    }

    private void writeSheetData(ZipOutputStream zos, List<Object[]> exportData, Charset utf8) throws Exception {
        int colCount = tabMode.getColumnCount();
        int rowCount = exportData.size();

        zos.write(("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n"
                + "  <sheetData>\n").getBytes(utf8));

        // Header Row (Row 1)
        StringBuilder sbRow = new StringBuilder();
        sbRow.append("    <row r=\"1\">\n");
        for (int c = 0; c < colCount; c++) {
            String colRef = getExcelColumnName(c) + "1";
            sbRow.append("      <c r=\"").append(colRef).append("\" t=\"inlineStr\" s=\"1\"><is><t>")
                    .append(escapeXml(tabMode.getColumnName(c)))
                    .append("</t></is></c>\n");
        }
        sbRow.append("    </row>\n");
        zos.write(sbRow.toString().getBytes(utf8));

        double sumQty = 0.0;
        double sumTotalBiaya = 0.0;
        double sumJmDokter = 0.0;

        // Data Rows (Row 2 .. rowCount + 1)
        for (int r = 0; r < rowCount; r++) {
            sbRow.setLength(0);
            int rowIdx = r + 2;
            sbRow.append("    <row r=\"").append(rowIdx).append("\">\n");
            Object[] row = exportData.get(r);
            for (int c = 0; c < colCount; c++) {
                String colRef = getExcelColumnName(c) + rowIdx;
                Object val = (c < row.length ? row[c] : null);
                if (val instanceof Number) {
                    double numVal = ((Number) val).doubleValue();
                    sbRow.append("      <c r=\"").append(colRef).append("\" s=\"2\"><v>")
                            .append(val)
                            .append("</v></c>\n");
                    if (c == 42) { // Qty
                        sumQty += numVal;
                    } else if (c == 43) { // Total Biaya
                        sumTotalBiaya += numVal;
                    } else if (c == 44) { // JM Dokter (Tarif RS)
                        sumJmDokter += numVal;
                    }
                } else if (val != null) {
                    sbRow.append("      <c r=\"").append(colRef).append("\" t=\"inlineStr\" s=\"0\"><is><t>")
                            .append(escapeXml(val.toString()))
                            .append("</t></is></c>\n");
                } else {
                    sbRow.append("      <c r=\"").append(colRef).append("\" s=\"0\"/>\n");
                }
            }
            sbRow.append("    </row>\n");
            zos.write(sbRow.toString().getBytes(utf8));
        }

        // Summary Row (Row rowCount + 2)
        int totalRowIdx = rowCount + 2;
        sbRow.setLength(0);
        sbRow.append("    <row r=\"").append(totalRowIdx).append("\">\n");
        for (int c = 0; c < colCount; c++) {
            String colRef = getExcelColumnName(c) + totalRowIdx;
            if (c == 0) {
                sbRow.append("      <c r=\"").append(colRef).append("\" t=\"inlineStr\" s=\"3\"><is><t>TOTAL</t></is></c>\n");
            } else if (c == 40) { // Nama Layanan / Tindakan
                sbRow.append("      <c r=\"").append(colRef).append("\" t=\"inlineStr\" s=\"3\"><is><t>TOTAL KESELURUHAN (")
                        .append(formatAngka(rowCount)).append(" Baris)</t></is></c>\n");
            } else if (c == 42) { // Qty
                sbRow.append("      <c r=\"").append(colRef).append("\" s=\"4\"><v>").append(sumQty).append("</v></c>\n");
            } else if (c == 43) { // Total Biaya
                sbRow.append("      <c r=\"").append(colRef).append("\" s=\"4\"><v>").append(sumTotalBiaya).append("</v></c>\n");
            } else if (c == 44) { // JM Dokter
                sbRow.append("      <c r=\"").append(colRef).append("\" s=\"4\"><v>").append(sumJmDokter).append("</v></c>\n");
            } else {
                sbRow.append("      <c r=\"").append(colRef).append("\" s=\"3\"/>\n");
            }
        }
        sbRow.append("    </row>\n");
        zos.write(sbRow.toString().getBytes(utf8));

        zos.write(("  </sheetData>\n"
                + "</worksheet>").getBytes(utf8));
    }

    private static String getExcelColumnName(int colIndex) {
        StringBuilder sb = new StringBuilder();
        colIndex++;
        while (colIndex > 0) {
            int rem = (colIndex - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            colIndex = (colIndex - 1) / 26;
        }
        return sb.toString();
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static String escapeXml(String input) {
        if (input == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(input.length() + 16);
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
            switch (ch) {
                case '&':
                    out.append("&amp;");
                    break;
                case '<':
                    out.append("&lt;");
                    break;
                case '>':
                    out.append("&gt;");
                    break;
                case '"':
                    out.append("&quot;");
                    break;
                case '\'':
                    out.append("&apos;");
                    break;
                default:
                    if (ch >= 0x20 || ch == '\n' || ch == '\r' || ch == '\t') {
                        out.append(ch);
                    }
                    break;
            }
        }
        return out.toString();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnAll;
    private widget.Button BtnCaraBayar;
    private widget.Button BtnCari;
    private widget.Button BtnDokter;
    private widget.Button BtnExportExcel;
    private widget.Button BtnKeluar;
    private widget.Button BtnNextPage;
    private widget.Button BtnPoli;
    private widget.Button BtnPrevPage;
    private widget.CekBox ChkSertakanObat;
    private widget.TextBox KdCaraBayar;
    private widget.TextBox KdDokter;
    private widget.TextBox KdPoli;
    private widget.TextBox NmCaraBayar;
    private widget.TextBox NmDokter;
    private widget.TextBox NmPoli;
    private widget.panelisi PanelFilterAtas;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.Tanggal Tgl1;
    private widget.Tanggal Tgl2;
    private widget.ComboBox cmbFilterBiaya;
    private widget.ComboBox cmbKategoriHD;
    private widget.ComboBox cmbKategoriICU;
    private widget.ComboBox cmbKategoriOperasi;
    private widget.ComboBox cmbKategoriTandem;
    private widget.ComboBox cmbStatusRawat;
    private widget.ComboBox cmbTindakanKhusus;
    private widget.InternalFrame internalFrame1;
    private widget.Label lblBiaya;
    private widget.Label lblDok;
    private widget.Label lblHD;
    private widget.Label lblICU;
    private widget.Label lblKey;
    private widget.Label lblKhusus;
    private widget.Label lblOp;
    private widget.Label lblPageInfo;
    private widget.Label lblPenjab;
    private widget.Label lblPoli;
    private widget.Label lblRawat;
    private widget.Label lblSd;
    private widget.Label lblStatusLoading;
    private widget.Label lblTandem;
    private widget.Label lblTgl;
    private widget.panelisi panelBarisFilter1;
    private widget.panelisi panelBarisFilter2;
    private widget.panelisi panelGlassBawah;
    private widget.Table tbDetailHarian;
    // End of variables declaration//GEN-END:variables
}
