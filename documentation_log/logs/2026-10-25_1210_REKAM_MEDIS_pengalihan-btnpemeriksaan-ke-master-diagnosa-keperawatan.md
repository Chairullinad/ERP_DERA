# Log Dokumentasi: Pengalihan Tombol Pencarian Master (btnPemeriksaan, btnPemeriksaan1, btnPemeriksaan2) ke MasterDiagnosaKeperawatan

---

## 1. Identitas Perubahan
- **ID Referensi**: `LOG-20261025-1210-REKAM_MEDIS-02`
- **Waktu Pelaksanaan**: 2026-10-25 12:10 WIB
- **Konteks / Submodul**: `[REKAM_MEDIS]` / `[KEPERAWATAN]` / `[UI_UX]`
- **Tipe Perubahan**: `ENHANCE` / `REFACTOR`
- **Target File**:
  - `src/rekammedis/MasterRencanaKeperawatan.java`

---

## 2. Latar Belakang & Kebutuhan Pengguna
1. Sebelumnya, tombol pencarian master pada masing-masing tab formulir `MasterRencanaKeperawatan`:
   - `btnPemeriksaan` pada Tab 0 (SDKI)
   - `btnPemeriksaan1` pada Tab 1 (SLKI)
   - `btnPemeriksaan2` pada Tab 2 (SIKI)
   terhubung ke modul lama `MasterMasalahKeperawatan` (`masalah`), yang hanya menyediakan 3 kolom sederhana (`kode_masalah`, `nama_masalah`, dan `definisi`).
2. Setelah dibuatnya modul terpadu `MasterDiagnosaKeperawatan` yang menyajikan data gabungan `master_diagnosa_keperawatan` dan `master_masalah_keperawatan` (10 kolom data meliputi kode masalah, kode diagnosa, masalah keperawatan, kode rencana, kategori, entitas, sub-entitas, item tindakan, dan definisi masalah), pengguna meminta agar ketiga tombol pencarian tersebut dialihkan untuk membuka modul `MasterDiagnosaKeperawatan`.

---

## 3. Rincian Solusi Rekayasa & Implementasi

### A. Penggantian Instans Objek Dialog
Mengubah instans dialog pencarian pada `MasterRencanaKeperawatan.java`:
```java
// Sebelum:
private MasterMasalahKeperawatan masalah = new MasterMasalahKeperawatan(null, false);

// Sesudah:
private MasterDiagnosaKeperawatan diagnosa = new MasterDiagnosaKeperawatan(null, false);
```

### B. Pengalihan Aksi Tombol (`ActionPerformed`)
Ketiga tombol aksi kini memanggil form `MasterDiagnosaKeperawatan`:
- `btnPemeriksaanActionPerformed`:
  ```java
  diagnosa.isCek();
  diagnosa.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
  diagnosa.setLocationRelativeTo(internalFrame1);
  diagnosa.setVisible(true);
  ```
- `btnPemeriksaan1ActionPerformed`:
  ```java
  diagnosa.isCek();
  diagnosa.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
  diagnosa.setLocationRelativeTo(internalFrame1);
  diagnosa.setVisible(true);
  ```
- `btnPemeriksaan2ActionPerformed`:
  ```java
  diagnosa.isCek();
  diagnosa.setSize(internalFrame1.getWidth() - 20, internalFrame1.getHeight() - 20);
  diagnosa.setLocationRelativeTo(internalFrame1);
  diagnosa.setVisible(true);
  ```

### C. Pemetaan Komprehensif Data Kolom saat Dialog Ditutup (`windowClosed`)
Saat perawat memilih baris pada tabel `tbDiagnosa` di `MasterDiagnosaKeperawatan` lalu menutup dialog (via klik dua kali atau spasi), seluruh atribut klinis langsung terisi otomatis:
1. **Tab 0 (SDKI)**:
   - `kdmasalah` <- Index 1 (`kode_masalah`)
   - `nmmasalah` <- Index 3 (`masalah_keperawatan`)
   - `kode` <- Index 2 (`kode`)
   - `entitas` <- Index 6 (`entitas`)
   - `sub_entitas` <- Index 7 (`sub_entitas`)
   - `rencanaKep` <- Index 8 (`item`), atau fallback ke format baku jika kosong
   - `Definisi` <- Index 9 (`definisi`)
   - `kdrencana` <- Index 4 (`kode_rencana`)
   - `kategori` <- Index 5 (`kategori`)
2. **Tab 1 (SLKI)**:
   - `kdmasalahSLKI` <- Index 1 (`kode_masalah`)
   - `nmmasalahSLKI` <- Index 3 (`masalah_keperawatan`)
   - `kode1` <- Index 2 (`kode`)
   - `entitas1` <- Index 6 (`entitas`)
   - `sub_entitas1` <- Index 7 (`sub_entitas`)
   - `rencanaKepSLKI` <- Index 8 (`item`)
   - `Definisi1` <- Index 9 (`definisi`)
   - `kdrencanaSLKI` <- Index 4 (`kode_rencana`)
   - `kategori3` <- Index 5 (`kategori`)
3. **Tab 2 (SIKI)**:
   - `kdmasalahSIKI` <- Index 1 (`kode_masalah`)
   - `nmmasalahSIKI` <- Index 3 (`masalah_keperawatan`)
   - `kode2` <- Index 2 (`kode`)
   - `entitas2` <- Index 6 (`entitas`)
   - `sub_entitas2` <- Index 7 (`sub_entitas`)
   - `rencanaKepSIKI` <- Index 8 (`item`)
   - `Definisi2` <- Index 9 (`definisi`)
   - `kdrencanaSIKI` <- Index 4 (`kode_rencana`)
   - `kategori4` <- Index 5 (`kategori`)

### D. Keyboard Accessibility
- Listener tombol spasi (`KeyEvent.VK_SPACE`) dipertahankan pada tabel `tbDiagnosa` milik `MasterDiagnosaKeperawatan` sehingga dialog dapat ditutup dan data dipilih menggunakan navigasi keyboard cepat.

---

## 4. Verifikasi & Integritas Desain
- [x] Kode Java bebas dari sisa referensi objek lama `masalah`.
- [x] Penyesuaian `windowClosed` memetakan kolom secara presisi sesuai urutan 10 kolom `MasterDiagnosaKeperawatan`.
- [x] Sesuai Rule 1 `AGENTS.md`, kompilasi/eksekusi runtime tidak dijalankan mandiri dan diserahkan kepada pengguna melalui NetBeans IDE.
