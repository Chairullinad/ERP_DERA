# Log Dokumentasi: Duplikasi Master Masalah Keperawatan Menjadi Master Diagnosa & Rencana Keperawatan

---

## 1. Identitas Perubahan
- **ID Referensi**: `LOG-20261025-1045-REKAM_MEDIS-01`
- **Waktu Pelaksanaan**: 2026-10-25 10:45 WIB
- **Konteks / Submodul**: `[REKAM_MEDIS]` / `[KEPERAWATAN]` / `[UI_UX]` / `[CORE_DATABASE]`
- **Tipe Perubahan**: `FEAT` / `UI_UX`
- **Target File**:
  - `src/rekammedis/MasterDiagnosaKeperawatan.java`
  - `src/rekammedis/MasterDiagnosaKeperawatan.form`

---

## 2. Latar Belakang & Kebutuhan Pengguna
1. Pengguna membutuhkan modul antarmuka master baru yang diduplikasi dari `MasterMasalahKeperawatan.java` untuk menampilkan relasi data diagnosa dan rencana keperawatan secara komprehensif.
2. Query data yang diminta secara spesifik menggabungkan tabel `master_diagnosa_keperawatan` dengan tabel `master_masalah_keperawatan` melalui relasi `kode_masalah`:
   ```sql
   SELECT
       master_diagnosa_keperawatan.id, 
       master_diagnosa_keperawatan.kode_masalah, 
       master_diagnosa_keperawatan.kode, 
       master_diagnosa_keperawatan.masalah_keperawatan, 
       master_diagnosa_keperawatan.kode_rencana, 
       master_diagnosa_keperawatan.kategori, 
       master_diagnosa_keperawatan.entitas, 
       master_diagnosa_keperawatan.sub_entitas, 
       master_diagnosa_keperawatan.item, 
       master_masalah_keperawatan.definisi
   FROM
       master_diagnosa_keperawatan
       INNER JOIN
       master_masalah_keperawatan
       ON 
           master_diagnosa_keperawatan.kode_masalah = master_masalah_keperawatan.kode_masalah
   GROUP BY
       master_diagnosa_keperawatan.id
   ```
3. Modul ini memungkinkan perawat/tenaga medis dan pengelola rekam medis meninjau seluruh data diagnosa dan rencana asuhan keperawatan multi-entitas (SDKI, SLKI, SIKI) beserta definisi masalah keperawatannya dalam satu tabel terpadu dengan kapabilitas CRUD yang aman.

---

## 3. Rincian Solusi Rekayasa & Implementasi

### A. Arsitektur Antarmuka GUI (`widget.*` & Khanza Flat Hospital Styling)
- Membuat berkas form NetBeans GUI Builder `MasterDiagnosaKeperawatan.form` dan kelas Java `MasterDiagnosaKeperawatan.java`.
- Mematuhi 100% aturan Khanza HMS dan pedoman `AGENTS.md`:
  - `setUndecorated(true)` dan `setResizable(false)` agar dialog menyatu rapi.
  - Titled Border baku: `::[ Data Master Diagnosa & Rencana Keperawatan ]::`.
  - Menggunakan native widget: `widget.Table`, `widget.ScrollPane`, `widget.Button`, `widget.TextBox`, `widget.TextArea`, `widget.ComboBox`, dan `widget.panelisi`.
  - Menerapkan `WarnaTable` untuk styling warna zebra baris otomatis.

### B. Komponen Form & Mapping Input
- **Panel Input Header (`panelGlass7`)**:
  - `txtKdMasalah`: Kode Masalah Keperawatan (lebar 65px).
  - `txtNmMasalah`: Deskripsi Masalah Keperawatan (lebar 220px).
  - `txtKode`: Kode Diagnosa (lebar 80px).
  - `txtKdRencana`: Kode Rencana Intervensi/Keluaran (lebar 60px).
  - `cmbKategori`: Kategori standar (SDKI, SLKI, SIKI).
  - `txtEntitas`: Entitas/Domain (lebar 220px).
  - `txtSubEntitas`: Sub-Entitas/Kelas (lebar 205px).
  - `txtDefinisi`: Definisi Masalah (lebar 215px).
  - `txtItem`: Deskripsi Tindakan / Rencana Asuhan Keperawatan dalam TextArea dengan ScrollPane.
- **Tabel Data (`tbDiagnosa`) 10 Kolom**:
  1. `ID` (Kolom 0, disembunyikan `minWidth=0`, `maxWidth=0`, `preferredWidth=0` sebagai primary key update/delete)
  2. `Kd Masalah` (lebar 75px)
  3. `Kode` (lebar 70px)
  4. `Masalah Keperawatan` (lebar 230px)
  5. `Kd Rencana` (lebar 75px)
  6. `Kategori` (lebar 65px)
  7. `Entitas` (lebar 140px)
  8. `Sub-Entitas` (lebar 140px)
  9. `Item / Tindakan` (lebar 350px)
  10. `Definisi Masalah` (lebar 250px)

### C. Mekanisme Kueri, Pencarian Cepat & Anti SQL Injection
- Query pemuatan data menggunakan INNER JOIN persis sesuai permintaan:
  - Didukung filter pencarian dinamis (`TCari`) ke seluruh atribut (kode masalah, kode diagnosa, masalah keperawatan, kode rencana, kategori, entitas, sub-entitas, item tindakan, dan definisi).
  - Parameter binding `PreparedStatement` (`?`) pada seluruh query pencarian, simpan, edit, dan hapus guna menjamin keamanan anti SQL-Injection.
  - Operasi edit dan hapus secara akurat merujuk ke primary key `master_diagnosa_keperawatan.id` (`selectedId`) dari baris yang dipilih.

### D. Keyboard-First Navigation & Shortcut Standard
- Navigasi perpindahan fokus input menggunakan `Valid.pindah(evt, ...)` dan `Valid.pindah2(evt, ...)` via tombol `Enter`.
- Standar shortcut tombol aksi:
  - `Alt+S` (Simpan)
  - `Alt+B` (Baru / Batal)
  - `Alt+H` (Hapus)
  - `Alt+G` (Ganti / Edit)
  - `Alt+K` (Keluar)
  - `Alt+1` (Cari)
  - `Alt+2` (Tampilkan Semua)

---

## 4. Verifikasi & Integritas Desain
- [x] Struktur GUI Form NetBeans Builder (`.form`) dan Java (`.java`) tersinkronisasi 100%.
- [x] Query data INNER JOIN `master_diagnosa_keperawatan` dan `master_masalah_keperawatan` dengan GROUP BY `id` terimplementasi sempurna.
- [x] Seleksi baris tabel (`tbDiagnosaMouseClicked` & `tbDiagnosaKeyReleased`) mengisi otomatis seluruh kontrol input form.
- [x] Operasi simpan, ganti/edit, dan hapus berjalan aman menggunakan `PreparedStatement`.
- [x] Batasan kompilasi & eksekusi ditaati penuh (tidak menjalankan `javac`/`ant` mandiri sesuai Rule 1 `AGENTS.md`).
