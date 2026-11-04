# ERP_DERA

Repositori kustomisasi modul ERP & SIMRS karya Dessy Rahmawati Chairullina (chairullinad).

## Daftar Modul

### 1. Modul Keuangan (Jasa Medis & Billing Pasien)
- **src/keuangan/DlgDetailHarianPasien.java & .form**: Dialog rincian harian pasien dan buku bantu JM BPJS.
- **src/keuangan/DlgRincianJMBPJS.java**: Dialog breakdown dan analisis klaim / tarif INA-CBG dan jasa medis.
- **src/keuangan/DlgSetAturanJMBPJS.java & .form**: Master pengaturan dinamis 39 aturan klaim/pembagian jasa medis BPJS.
- **src/keuangan/Aturan_Bayar_JM_BPJS.xlsx**: File referensi spreadsheet aturan kalkulasi JM BPJS.
- **src/keuangan/KeuanganBayarJMDokterBPJS.java & .form**: Pemrosesan dan pembayaran jasa medis dokter BPJS (rawat jalan, rawat inap, status klaim, COB).
- **src/keuangan/KeuanganBayarJMDokter.java & .form**: Pembayaran jasa medis dokter umum & sharing operasi.
- **src/keuangan/KeuanganCariBayarJMDokter.java & .form**: Riwayat dan pencarian pelunasan JM dokter serta cetak slip PDF.
- **src/keuangan/DlgBilingRalan.java**: Penyesuaian billing rawat jalan.
- **src/fungsi/KalkulatorJasaMedisDokter.java**: Kelas pembantu kalkulator logika perhitungan jasa medis dinamis.
- **documentation_log/ATURAN_BAYAR_JM_DOKTER_BPJS.md**: Dokumentasi aturan kalkulasi & bisnis jasa medis BPJS.

### 2. Modul Akuntansi & Jurnal Modern (Web Interface / JavaFX WebView)
- **src/keuangan/DlgJurnalTransaksiWeb.java** ➕ **src/resources/jurnal_transaksi.html**: Antarmuka modern web untuk pencatatan dan monitoring jurnal transaksi akuntansi.
- **src/keuangan/DlgPostingJurnalWeb.java** ➕ **src/resources/posting_jurnal.html**: Antarmuka modern web untuk posting jurnal harian terintegrasi.
- **src/keuangan/DlgBukuBesarWeb.java** ➕ **src/resources/buku_besar.html**: Modul Buku Besar SAK Baru (*buku_besar_new*) dengan kalkulasi *running balance*.
- **src/keuangan/DlgPenyusutanAsetWeb.java** ➕ **src/resources/penyusutan_aset.html**: Modul web terpadu depresiasi dan penyusutan aset (metode garis lurus, sinkronisasi data master, dan posting jurnal otomatis).
- **src/keuangan/DlgValidasiJurnalHPPJMDokterWeb.java** ➕ **src/resources/validasi_jurnal_hpp_jm.html**: Modul validasi dan rekonsiliasi jurnal HPP JM Dokter.
- **src/resources/mapping_coa.html**: Antarmuka mapping Chart of Accounts (COA) visual.

### 3. Modul Rekam Medis (Diagnosa & Rencana Asuhan Keperawatan SDKI, SLKI, SIKI)
- **src/rekammedis/MasterDiagnosaKeperawatan.java & .form**: Modul master diagnosa keperawatan terpadu (gabungan master diagnosa dan rencana, auto-number ID dan kode rencana, serta fitur pemilihan warna kustom entitas).
- **src/rekammedis/MasterRencanaKeperawatan.java & .form**: Modul master rencana keperawatan dengan pengalihan tombol master ke MasterDiagnosaKeperawatan.
- **src/rekammedis/DlgDiagnosaKeperawatan.java & .form**: Dialog transaksi asuhan keperawatan pasien rawat jalan/inap dengan fitur auto-commit cell editing dan penyimpanan multi-baris batch evaluasi masalah keperawatan.
- **src/fungsi/WarnaTableSDKI.java**: Kelas renderer tabel pewarnaan entitas keperawatan SDKI/SLKI/SIKI.
- **docs/PANDUAN_PENGISIAN_MASTER_DIAGNOSA_KEPERAWATAN.docx**: Buku panduan pengisian master diagnosa keperawatan untuk perawat.

## Dokumentasi & Logbook
- **docs/UAT_DlgJurnalTransaksiWeb_DlgPostingJurnalWeb.docx & .pdf**: Dokumen User Acceptance Testing (UAT) Jurnal Transaksi & Posting Jurnal.
- **docs/Logbook_Fitur_JurnalTransaksiWeb_PostingJurnalWeb.docx & .pdf**: Logbook pengembangan fitur Jurnal Web.
- **documentation_log/logs/**: Catatan riwayat rekayasa perangkat lunak dan arsitektur modul.
