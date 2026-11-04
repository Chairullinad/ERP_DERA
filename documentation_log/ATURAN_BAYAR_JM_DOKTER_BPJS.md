# PANDUAN & ATURAN BAKU PEMBAGIAN JASA MEDIS (JM) DOKTER BPJS KESEHATAN
**Rumah Sakit Indriati Boyolali**

---

## 1. Prinsip Dasar & Fondasi Evaluasi Sistem

1. **Evaluasi Berbasis Nomor SEP (`no_sep`) sebagai Kunci Utama**:
   - Seluruh penilaian klaim BPJS dikelompokkan pertama kali berdasarkan **Nomor SEP**.
   - Satu Nomor SEP dapat memayungi lebih dari 1 nomor rawat (`no_rawat`), lebih dari 1 poliklinik, atau gabungan antara pemeriksaan dokter spesialis dan tindakan medis. Sistem mengevaluasi seluruh transaksi di bawah No SEP tersebut sebagai satu kesatuan paket klaim INA-CBG.
2. **Dasar Pengali Perhitungan**:
   - **Rawat Jalan (Ralan)**: Mengacu pada nilai klaim INA-CBG yang disetujui (Umbal) dengan batas dasar pemeriksaan spesialis sebesar **Rp 196.100**.
   - **Rawat Inap (Ranap)**: Mengacu pada total klaim paket yang disetujui, yaitu:
     $$\text{Total Klaim} = \text{INA-CBG} + \text{Top Up} \quad (\text{bila terdapat ekses/top up})$$
3. **Ketentuan Universal Dokter Anestesi**:
   - Dokter Anestesi selalu menerima proporsi **40% dari hak JM Dokter Operator**.

---

## 2. TABEL ATURAN BAYAR JM BPJS - RAWAT JALAN (RALAN)

| No | Kategori Kasus | Kondisi / Logika Pemicu dalam 1 No. SEP | Hak JM Dokter | Rumus / Nilai JM | Keterangan & Catatan Teknis SIMRS |
| :---: | :--- | :--- | :--- | :--- | :--- |
| **1** | **Hemodialisa (Single Use)** | • 1 SEP, 1 DPJP, ada tindakan `SISTEM SINGLE USE`<br>• 1 SEP memiliki >1 kunjungan (misal: kunjungan pertama Single Use, kunjungan berikutnya periksa dr. Sp, atau sebaliknya) | **Rp 75.000** | Flat **Rp 75.000** | Pemeriksaan dokter spesialis **diabaikan (Rp 0)**. Fokus penilaian hanya pada tindakan Single Use. |
| **2** | **Tindakan AV Shunt** | • Ada tindakan `AV SHUNT` dalam 1 SEP<br>• Terdapat kunjungan pemeriksaan dokter spesialis di SEP yang sama | • AV Shunt: **Rp 575.000**<br>• Periksa: **Rp 72.000** | **Rp 575.000** + **Rp 72.000** = **Rp 647.000** | Pemeriksaan dokter spesialis tetap dinilai penuh (Rp 72.000) terpisah dari tindakan AV Shunt. |
| **3** | **Tindakan ESWL** | • Ada tindakan `ESWL` dalam 1 SEP<br>• Terdapat kunjungan pemeriksaan dokter spesialis di SEP yang sama | • ESWL: **Rp 822.760**<br>• Periksa: **Rp 0** | Flat **Rp 822.760** | Pemeriksaan dokter spesialis **dinilai Rp 0** (diabaikan/sudah include di dalam paket ESWL). |
| **4** | **Tindakan Double Lumen** | • Ada tindakan `DOUBLE LUMEN` dalam 1 SEP<br>• Terdapat kunjungan pemeriksaan dokter spesialis di SEP yang sama | • Double Lumen: **Rp 500.000**<br>• Periksa: **Rp 72.000** | **Rp 500.000** + **Rp 72.000** = **Rp 572.000** | Pemeriksaan dokter spesialis tetap dinilai penuh (Rp 72.000) terpisah dari Double Lumen. |
| **5** | **Dokter Umum di IGD** | Pelayanan di IGD (`kd_poli = 'IGDK'` atau poli IGD) oleh dokter umum | **Rp 0** | **Rp 0** | Tidak ada pembagian jasa medis meskipun klaim INA-CBG terbit. |
| **6** | **Spesialis Tanpa Tindakan** | 1 DPJP Spesialis, murni konsultasi/pemeriksaan poliklinik tanpa tindakan medis | **Rp 72.000** | Flat **Rp 72.000** per periksa | Ambang tarif dasar pemeriksaan dokter spesialis standar. |
| **7** | **Spesialis Disertai Tindakan** | 1 DPJP Spesialis + ada tindakan medis yang menaikkan tarif klaim INA-CBG di atas ambang batas dasar | Formula Selisih INA-CBG | $\text{Rp 72.000} + 20\% \times (\text{INA-CBG} - \text{Rp 196.100})$ | Nilai Rp 196.100 adalah ambang batas minimal klaim pemeriksaan spesialis biasa. |
| **8** | **Klinik KFR (`U0036`)**<br>*(dr. Anwar & dr. Dian)* | **Poli `U0036` (Kedokteran Fisik & Rehabilitasi)**:<br>• Billing hanya Pemeriksaan Dokter Spesialis<br>• Billing hanya Tindakan KFR (tanpa periksa Sp)<br>• Semua dr. Sp KFR diposisikan sebagai Raber | Sesuai jenis pelayanan di Poli `U0036` | • Periksa Sp: **Rp 36.000**<br>• Tindakan KFR: **Rp 35.000**<br>• Raber KFR: **Rp 36.000** | Terdeteksi otomatis dari `kd_poli = 'U0036'`. Dokter KFR selalu berstatus raber sehingga tarif pemeriksaan adalah Rp 36.000. |
| **9** | **Rujukan Internal: 2 Dokter Tanpa Tindakan** | 1 SEP ada 2 dokter spesialis, keduanya murni periksa (tidak ada tindakan medis dari masing-masing dokter) | Dibagi 2 rata | Masing-masing: **Rp 36.000**<br>$\left(\frac{\text{Rp 72.000}}{2}\right)$ | Patokan dasar pemeriksaan Rp 72.000 dibagi rata untuk kedua dokter. |
| **10** | **Rujukan Internal: 1 Ber-Tindakan, 1 Tanpa Tindakan** | 1 SEP ada 2 dokter, 1 dokter melakukan tindakan medis (klaim naik), 1 dokter lainnya murni pemeriksaan | Proporsional | • Dr. Tanpa Tindakan = **Rp 36.000**<br>• Dr. Ber-Tindakan = $\text{Rp 36.000} + 20\% \times (\text{INA-CBG} - \text{Rp 196.100})$ | Dokter yang ber-tindakan mendapatkan hak kenaikan klaim INA-CBG di atas Rp 196.100. |
| **11** | **Rujukan Internal: 2 Dokter Sama-Sama Ber-Tindakan** | 1 SEP ada 2 dokter dan kedua dokter sama-sama memberikan tindakan medis | Dibagi 2 proporsional | Masing-masing: $\frac{\text{Rp 72.000} + 20\% \times (\text{INA-CBG} - \text{Rp 196.100})}{2}$ | Total formula selisih dibagi 2 secara merata. |

---

## 3. TABEL ATURAN BAYAR JM BPJS - RAWAT INAP (RANAP)
> **Catatan Pengali**: Seluruh persentase dihitung terhadap total **$(\text{INA-CBG} + \text{Top Up})$**.

### A. Ranap Non-Tindakan (Pelayanan Medis Tanpa Operasi)

| Komposisi Dokter | Frekuensi / Jenis Visite DPJP | Porsi DPJP | Porsi Dokter Tandem / Raber | Ketentuan Khusus & Batasan |
| :--- | :--- | :---: | :---: | :--- |
| **1 DPJP Tunggal**<br>*(Murni 1 Dokter)* | • Visite > 1 kali<br>• Visite = 1 kali<br>• Hanya Telekonsultasi | **9%**<br>**6%**<br>**4%** | - | Pasien dirawat penuh oleh 1 DPJP dari awal masuk hingga pulang. |
| **1 DPJP + 1 Dokter Tandem**<br>*(Total 2 Dokter)* | • DPJP Visite > 1 kali<br>• DPJP Visite = 1 kali<br>• DPJP Telekonsultasi | **8%**<br>**6%**<br>**4% (Maks. Rp 40.000)** | **2%**<br>**2%**<br>**2%** | • Tandem visite 1x mendapat 2% utuh.<br>• **Khusus DPJP Telekonsul**: DPJP mendapat 4% dengan **plafon maksimal Rp 40.000** (jika $4\% \times \text{klaim} > 40\text{rb}$ dipotong jadi Rp 40.000), sedangkan dokter tandem tetap menerima 2% utuh. |
| **1 DPJP + >1 Dokter Tandem**<br>*(Multi-Tandem $\ge 2$ Dokter)* | • DPJP Visite > 1 kali<br>• DPJP Visite = 1 kali<br>• DPJP Telekonsultasi | **6%**<br>**5%**<br>**4%** | **4% Prorata Visite**<br>**3% Prorata Visite**<br>**2% Prorata Visite** | • Dokter-dokter tandem berbagi porsi total secara **Prorata** sesuai jumlah kunjungan/visite masing-masing dokter tandem:<br>$$\text{JM Tandem}_i = \text{Porsi} \times (\text{INA-CBG} + \text{TopUp}) \times \frac{\text{Visite}_i}{\sum \text{Visite Tandem}}$$ |

---

### B. Ranap Pasien Alih Rawat (Transfer DPJP)
*Pemicu: DPJP pada SEP VClaim berbeda dengan DPJP saat ini di SIMRS.*

| Komposisi Dokter | Porsi DPJP SEP VClaim | Porsi DPJP Aktif SIMRS | Porsi Dokter Tandem Tambahan | Catatan |
| :--- | :---: | :---: | :---: | :--- |
| **Tanpa Dokter Tandem** | **4%** | **4%** | - | Masing-masing DPJP (awal dan pengganti) mendapat 4%. |
| **Ada Dokter Tandem Tambahan** | **4%** | **4%** | **3% Prorata Visite** | Seluruh dokter tandem berbagi alokasi 3% secara prorata frekuensi visite. |

---

### C. Ranap Bayi Baru Lahir (Spesialis Anak Tanpa Tindakan)
*Pemicu: Dalam 1 No. SEP terdapat **2 Nomor Rekam Medis (No RM)**. No RM yang paling baru/terbaru adalah No RM milik bayi/anak.*

| Kasus Kelahiran Ibu | Hak JM Dokter Spesialis Anak | Nilai Pembagian JM | Cara Deteksi Sistem SIMRS |
| :--- | :---: | :--- | :--- |
| **Post SC (Sectio Caesarea)** | **4%** dari total klaim | $\mathbf{4\% \times (\text{INA-CBG} + \text{Top Up})}$ | Terdapat tindakan operasi Sectio Caesarea pada No RM ibu di SEP yang sama. |
| **Post Partum / Normal** | **Flat Rp 60.000** | **Rp 60.000** (flat) | Persalinan spontan/normal tanpa operasi bedah SC. |

---

### D. Tindakan Khusus di Ruang Perawatan Rawat Inap (Non-Bedah Sentral)

| Jenis Tindakan | Hak JM Dokter Pelaksana | Keterangan |
| :--- | :---: | :--- |
| **Double Lumen** (di Ranap) | **Rp 500.000** (flat) | Pemasangan kateter Double Lumen di ruang rawat inap. |
| **AV Shunt** (di Ranap) | **Rp 575.000** (flat) | Pembuatan akses vaskular AV Shunt di ruang rawat inap. |

---

### E. Ranap Tindakan Bedah / Operasi Sentral (IBS)
> **Ketentuan Universal Dokter Anestesi**: Dokter Anestesi selalu menerima **40% dari hak JM Dokter Operator**.

| Kasus Bedah / Operasi | Kondisi Visite / Komposisi Dokter | DPJP / Operator 1 | Operator 2 / Operator Lain | Dokter Anestesi | Dokter Tandem / Raber |
| :--- | :--- | :---: | :---: | :---: | :---: |
| **1 DPJP Tunggal**<br>*(Operator = DPJP SEP)* | • Visite post-op > 1 kali<br>• Visite post-op = 1 kali<br>• Telekonsultasi (tidak visite) | **20%**<br>**18%**<br>**16%** | - | **40% dari DPJP**<br>• Visite >1: $40\% \times 20\% = \mathbf{8\%}$<br>• Visite 1: $40\% \times 20\% = \mathbf{8\%}$<br>• Tele: $40\% \times 20\% = \mathbf{8\%}$ | - |
| **1 DPJP + Ada Tandem**<br>*(Operator = DPJP SEP)* | • DPJP Visite > 1 kali<br>• DPJP Visite = 1 kali<br>• DPJP Telekonsultasi | **17%**<br>**16%**<br>**15%** | - | **40% dari porsi DPJP**<br>*(yaitu $40\% \times 20\% = \mathbf{8\%}$)* | **3% Prorata**<br>**2% Prorata**<br>**2% Prorata** |
| **Operasi 2 Dokter / 2 Tindakan Berbeda**<br>*(1 Pasien, 1 SEP, salah satu = DPJP SEP)* | • Tanpa Raber dokter lain<br>• Ada Raber dokter lain | **10%**<br>**9%** | **10%**<br>**9%** | **40% dari Total Operator**<br>• Tanpa Raber: $40\% \times 20\% = \mathbf{8\%}$<br>• Ada Raber: $40\% \times 18\% = \mathbf{7.2\%}$ | -<br>**2% Prorata** |
| **Operator BUKAN DPJP SEP**<br>*(DPJP SEP tidak ikut mengoperasi)* | • Tanpa Raber dokter lain<br>• Ada Raber dokter lain | DPJP: **6%**<br>DPJP: **4%** | Operator: **14%**<br>Operator: **14%** | **40% dari Operator**<br>*(yaitu $40\% \times 14\% = \mathbf{5.6\%}$)* | -<br>**3% Prorata** |

---

## 4. CONTOH SIMULASI PERHITUNGAN

### Contoh 1: Rawat Jalan - 1 SEP dengan AV Shunt + Pemeriksaan Spesialis
- **Kondisi**: Pasien memiliki 1 SEP, berkunjung ke Poli Spesialis Bedah (periksa) dan menjalani tindakan AV Shunt.
- **Klaim INA-CBG**: Rp 3.500.000
- **Perhitungan**:
  - Tindakan AV Shunt = Rp 575.000
  - Pemeriksaan dr. Spesialis = Rp 72.000
  - **Total JM Dokter** = $\text{Rp 575.000} + \text{Rp 72.000} = \mathbf{\text{Rp 647.000}}$

### Contoh 2: Rawat Jalan - Rujukan Internal (1 Dokter Ber-Tindakan, 1 Tanpa Tindakan)
- **Kondisi**: Pasien berobat ke DPJP Poli Penyakit Dalam (murni periksa), lalu dirujuk internal ke Poli Jantung dan dilakukan tindakan Echocardiography.
- **Klaim INA-CBG**: Rp 600.000
- **Perhitungan**:
  - Dokter Penyakit Dalam (tanpa tindakan) = **Rp 36.000**
  - Dokter Jantung (ber-tindakan) = $\text{Rp 36.000} + 20\% \times (\text{Rp 600.000} - \text{Rp 196.100}) = \text{Rp 36.000} + 20\% \times \text{Rp 403.900} = \text{Rp 36.000} + \text{Rp 80.780} = \mathbf{\text{Rp 116.780}}$

### Contoh 3: Rawat Inap - 1 DPJP + 3 Dokter Tandem (Prorata Visite)
- **Kondisi**: Pasien Non-Bedah, DPJP Utama visite 4 kali (>1 kali). Ada 3 dokter konsul/tandem:
  - Dokter A: 3 kali visite
  - Dokter B: 2 kali visite
  - Dokter C: 3 kali visite
  - *Total visite tandem* = $3 + 2 + 3 = 8$ kali visite.
- **Klaim INA-CBG + Top Up**: Rp 10.000.000
- **Perhitungan**:
  - DPJP Utama (>1x visite) = $6\% \times \text{Rp 10.000.000} = \mathbf{\text{Rp 600.000}}$
  - Total porsi seluruh tandem = $4\% \times \text{Rp 10.000.000} = \text{Rp 400.000}$
  - Dokter A = $\frac{3}{8} \times \text{Rp 400.000} = \mathbf{\text{Rp 150.000}}$
  - Dokter B = $\frac{2}{8} \times \text{Rp 400.000} = \mathbf{\text{Rp 100.000}}$
  - Dokter C = $\frac{3}{8} \times \text{Rp 400.000} = \mathbf{\text{Rp 150.000}}$
  - *Kontrol Jumlah*: $\text{Rp 150.000} + \text{Rp 100.000} + \text{Rp 150.000} = \text{Rp 400.000}$ (Pas & Akurat).

### Contoh 4: Rawat Inap - Operasi Bedah dengan Operator Bukan DPJP SEP
- **Kondisi**: DPJP SEP adalah dr. Sp.PD (visite medis), operasi dilakukan oleh dr. Sp.B (Operator). Pasien didampingi 1 dokter konsul Paru (1x visite).
- **Klaim INA-CBG + Top Up**: Rp 15.000.000
- **Perhitungan**:
  - DPJP SEP = $4\% \times \text{Rp 15.000.000} = \mathbf{\text{Rp 600.000}}$
  - Dokter Operator (dr. Sp.B) = $14\% \times \text{Rp 15.000.000} = \mathbf{\text{Rp 2.100.000}}$
  - Dokter Anestesi = $40\% \times \text{Rp 2.100.000} = \mathbf{\text{Rp 840.000}}$
  - Dokter Tandem (dr. Paru) = $3\% \times \text{Rp 15.000.000} = \mathbf{\text{Rp 450.000}}$
