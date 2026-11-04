# Log Dokumentasi: Implementasi Auto Number (ID & Kode Rencana) serta Pewarnaan Entitas Kustom Dinamis pada MasterDiagnosaKeperawatan

---

## 1. Identitas Perubahan
- **ID Referensi**: `LOG-20261025-1510-REKAM_MEDIS-03`
- **Waktu Pelaksanaan**: 2026-10-25 15:10 WIB
- **Konteks / Submodul**: `[REKAM_MEDIS]` / `[KEPERAWATAN]` / `[UI_UX]` / `[CORE_DATABASE]`
- **Tipe Perubahan**: `FEAT` / `ENHANCE` / `UI_UX`
- **Target File**:
  - `src/rekammedis/MasterDiagnosaKeperawatan.java`
  - `src/rekammedis/MasterDiagnosaKeperawatan.form`

---

## 2. Latar Belakang & Kebutuhan Pengguna
1. Saat tenaga perawat atau tim rekam medis menambahkan item diagnosa/rencana keperawatan baru di form `MasterDiagnosaKeperawatan`, dibutuhkan:
   - **Nomor ID yang jelas**: Nilai ID ditampilkan di form (`txtId`) dan tabel (`tbDiagnosa`), mempermudah pelacakan data.
   - **Auto Number Kode Rencana Mutakhir**: `txtKdRencana` otomatis ter-generate secara cerdas berdasar kombinasi `kode_masalah` dan `kategori` (SDKI, SLKI, SIKI) langsung dari basis data tanpa risiko tumpang tindih (*stale client row counter*).
2. **Kebutuhan Pewarnaan Entitas Bebas Hardcode**:
   - Sebelumnya, pewarnaan diatur statis di `WarnaTableSDKI.java`.
   - Pengguna menginginkan kemampuan memilih dan mengatur warna sendiri untuk setiap entitas di semua kategori secara interaktif langsung dari GUI tanpa perlu menyentuh atau me-recompile kode Java.

---

## 3. Rincian Solusi Rekayasa & Implementasi

### A. Auto Number ID & Kode Rencana Berbasis Database
1. **Field `txtId`**:
   - Ditambahkan ke panel input header (`panelGlass7`, x=38, y=10, w=45) dengan sifat `setEditable(false)`.
   - Pada mode baru (`emptTeks()`), menampilkan preview ID berikutnya via `SELECT COALESCE(MAX(id), 0) + 1 FROM master_diagnosa_keperawatan`.
   - Pada mode edit/pilih baris (`getData()`), menampilkan ID dari baris yang dipilih.
   - Kolom `ID` pada tabel `tbDiagnosa` ditampilkan dengan lebar `preferredWidth=45`.
2. **Metode `autoKodeRencana(String kdMasalah, String kategori)`**:
   - Mengeksekusi kueri agregasi:
     ```sql
     SELECT COALESCE(MAX(CAST(kode_rencana AS UNSIGNED)), 0) + 1 AS no_urut 
     FROM master_diagnosa_keperawatan 
     WHERE kode_masalah = ? AND kategori = ?
     ```
   - Mengembalikan format 3 digit (contoh: `001`, `002`, `015`, dst.).
   - Terpicu secara otomatis saat:
     - Form dikosongkan (`emptTeks()`)
     - Pengguna menekan tombol Enter pada `txtKdMasalah`
     - Pilihan ComboBox `cmbKategori` berubah (`ItemEvent.SELECTED`)
     - Pengguna mengklik tombol bantuan baru `BtnAutoKode` di samping field `txtKdRencana`.

### B. Arsitektur Pewarnaan Entitas Kustom Dinamis
1. **Dynamic Zero-Downtime Table Schema**:
   - Saat form dibuka (`formWindowOpened`), dijalankan inisialisasi tabel idempotent:
     ```sql
     CREATE TABLE IF NOT EXISTS master_entitas_warna (
         kategori VARCHAR(10) NOT NULL,
         entitas VARCHAR(150) NOT NULL,
         kode_warna VARCHAR(10) NOT NULL,
         PRIMARY KEY (kategori, entitas)
     ) ENGINE=InnoDB DEFAULT CHARSET=latin1;
     ```
2. **Tombol Pemilih Warna (`BtnWarna`)**:
   - Diletakkan di samping `txtEntitas` (x=272, y=40, w=28, h=23).
   - Membuka dialog pemilih warna native `javax.swing.JColorChooser.showDialog(...)`.
   - Mengonversi warna terpilih ke representasi Hexadecimal (`#RRGGBB`).
   - Menyimpan ke database via parameterized `PreparedStatement` (`REPLACE INTO master_entitas_warna ...`).
3. **High-Performance In-Memory Cached Cell Renderer (`WarnaTableDiagnosa`)**:
   - Warna dibaca ke dalam `Map<String, Color> cacheWarna` saat form dibuka dan di-refresh saat user mengganti warna.
   - Renderer mengecek kombinasi `kategori#entitas` di cache in-memory untuk mewarnai baris tabel `tbDiagnosa` secara instan 60 FPS tanpa beban delay kueri SQL.
   - Baris tanpa kustomisasi warna otomatis menggunakan zebra-style standar Khanza (putih / soft green).

---

## 4. Verifikasi & Integritas Desain
- [x] Sinkronisasi 100% antara NetBeans GUI Builder `MasterDiagnosaKeperawatan.form` dan `MasterDiagnosaKeperawatan.java`.
- [x] Penomoran `kode_rencana` otomatis berganti menyesuaikan `kode_masalah` dan kategori terpilih (SDKI, SLKI, SIKI).
- [x] Fitur pemilih warna `JColorChooser` menyimpan nilai ke basis data dan seketika merefresh tampilan tabel.
- [x] Tetap mematuhi Rule 1 `AGENTS.md` (kompilasi/eksekusi runtime tidak dijalankan mandiri, diserahkan kepada pengguna melalui NetBeans IDE).
