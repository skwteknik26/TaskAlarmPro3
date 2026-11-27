# TaskAlarm Pro – Update: Alarm Layar Penuh + Catatan Suara

## Isi paket
- `www/index.html`            → UI baru (menggantikan `www/index.html` di project Capacitor Anda)
- `java/com/taskalarm/pro/*`  → salin ke `android/app/src/main/java/com/taskalarm/pro/` (MainActivity.java: timpa/gabungkan)
- `manifest/AndroidManifest-ADD.xml` → tambahkan ke `android/app/src/main/AndroidManifest.xml`

## Build (butuh project sumber + Android Studio)
1. Salin file seperti di atas.
2. Terminal di folder project: `npx cap sync android`
3. `cd android && ./gradlew assembleDebug`  (atau Build > Build APK di Android Studio)
4. Hasil: `android/app/build/outputs/apk/debug/app-debug.apk`

## Cara kerja
- **Alarm**: dijadwalkan dengan `AlarmManager.setAlarmClock` (tepat waktu, tembus Doze) → `AlarmService`
  memutar suara (nada pilihan/Default) + getar, memunculkan notifikasi full-screen → `AlarmActivity`
  tampil di tengah layar, di atas lock screen, dengan tombol **Matikan** / **Tunda 10 menit**.
  Alarm tetap hidup setelah restart HP (BootReceiver).
- **Izin sekali setup**: saat pertama dibuka, app mengarahkan ke "Tampil di atas aplikasi lain"
  (agar layar alarm muncul walau HP sedang dipakai). Bisa diulang lewat Pengaturan → "Atur Izin Alarm Layar Penuh".
  Android 14+ juga mungkin meminta izin "Notifikasi layar penuh".
- **Suara**: tombol 🎤 di Catatan Harian, Judul, Deskripsi, dan Catatan kegiatan → dialog suara Google
  (bahasa Indonesia), hasilnya ditambahkan ke teks. Tombol "🎤 Suara" di menu AI juga memakai ini.
  Perlu aplikasi Google terpasang.
- Jika dijalankan sebagai web biasa/tanpa plugin native, app otomatis kembali ke notifikasi lama.

## Catatan HP tertentu (Xiaomi/Oppo/Vivo/Samsung)
Aktifkan "Autostart" dan atur baterai aplikasi ke "Tanpa batasan" agar alarm tidak dimatikan sistem.
