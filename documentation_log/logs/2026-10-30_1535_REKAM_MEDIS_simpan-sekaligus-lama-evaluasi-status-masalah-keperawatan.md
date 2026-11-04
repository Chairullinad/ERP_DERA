# Dokumentasi Perubahan: Simpan Sekaligus Lama Evaluasi & Status Masalah Keperawatan (Tanpa Wajib Tekan Enter)

- **Tanggal & Waktu**: 2026-10-30 15:35 WIB
- **ID Referensi**: `LOG-20261030-1535-REKAM_MEDIS-08`
- **Konteks**: `[REKAM_MEDIS]` / `[KEPERAWATAN]` / `[UI_UX]`
- **Tipe Perubahan**: `ENHANCE` / `FIX` / `UI_UX`
- **Berkas Terdampak**:
  - `src/rekammedis/DlgDiagnosaKeperawatan.java`

---

## 1. Latar Belakang & Permasalahan
1. Pada tabel kiri **Daftar Masalah Keperawatan** (`tbMasalahDetailMasalah`), kolom **Lama Evaluasi** (kolom 2) dan **Status** (kolom 3) sebelumnya mengharuskan pengguna menekan tombol `Enter` terlebih dahulu sebelum menekan tombol **Simpan** atau **Ganti**. Jika pengguna langsung mengklik tombol **Simpan**/**Ganti**, nilai yang sedang diketik masih tertahan di *cell editor* dan belum masuk ke `TableModel`.
2. Saat pasien memiliki beberapa masalah keperawatan dan petugas menginput **Lama Evaluasi** serta **Status** pada banyak baris sekaligus lalu menekan **Simpan** atau **Ganti**, sistem sebelumnya hanya menyimpan 1 baris terakhir yang sedang terpilih (`selectedRow`), sehingga isian pada baris-baris lainnya ter-reset saat `getMasalah()` dipanggil.

---

## 2. Rincian Solusi & Perubahan Teknis
1. **Komit Otomatis Tanpa Wajib Tekan `Enter` pada `tbMasalahDetailMasalah`**:
   - Menambahkan properti `tbMasalahDetailMasalah.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);` saat inisialisasi tabel.
   - Menambahkan penghentian editor sel aktif (`tbMasalahDetailMasalah.getCellEditor().stopCellEditing()`) di awal `BtnSimpanActionPerformed` dan `BtnEditActionPerformed`.
2. **Penyimpanan Batch (Banyak Baris Sekaligus) untuk Kolom `Lama Evaluasi` & `Status` (`simpanBatchDetailMasalah`)**:
   - Menambahkan metode `simpanBatchDetailMasalah(String rw, String nipUser, String now)` yang melakukan iterasi pada seluruh baris di `tabModeDetailMasalah` dan menyimpan `waktu_eval` (Lama Evaluasi) serta `keterangan` (Status) ke tabel target masing-masing baris (`penilaian_awal_keperawatan_ranap_masalah` atau `penilaian_awal_keperawatan_ralan_masalah` sesuai penanda `(Ranap)` / `(Ralan)`).
   - **Proteksi Riwayat Evaluator (`nipeval` & `tanggal_eval`)**: Jika suatu baris masalah sudah teratasi sebelumnya (kolom `Nrs. Evaluasi` dan `Tgl. Teratasi` sudah terisi), nilai `nipeval` dan `tanggal_eval` lama dipertahankan agar tidak tertimpa oleh petugas yang sedang mengedit baris masalah lainnya.
3. **Tetap Menjaga Algoritma Centangan SDKI, SLKI, dan SIKI**:
   - Penyimpanan centangan detail **SDKI** (`tbDiagKep`), **SLKI** (`tbIntervensi`), dan **SIKI** (`tbEvaluasi`) di tabel kanan tetap dijalankan per satu masalah yang sedang dipilih (`selectedRow`) melalui `syncChecklistPerMasalah`.

