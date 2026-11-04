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
