# TaskAlarm Pro
1. Buat repo baru di GitHub, upload SEMUA isi folder ini (termasuk folder .github dan www).
2. Buka tab **Actions** > **Build APK** > **Run workflow** (otomatis jalan juga tiap push ke branch main).
3. Tunggu ±5-8 menit, buka run yang hijau, unduh **TaskAlarmPro-APK** di bagian Artifacts, unzip, install app-debug.apk di HP.
4. Saat pertama dibuka, izinkan Notifikasi & Alarm. Di HP tertentu (Xiaomi/Oppo/Vivo) matikan "hemat baterai" untuk app ini agar alarm tepat waktu.

## Nada alarm kustom
File nada ada di `www/sounds/` (tone_*.wav, WAV, nama huruf kecil tanpa spasi). Untuk menambah nada: taruh file WAV/MP3 baru di folder itu, lalu tambahkan satu baris di konstanta `TONES` di `www/index.html`.
Catatan Android: suara kanal notifikasi tidak bisa diubah setelah terpasang. Jika mengganti isi file nada, uninstall dulu aplikasinya lalu install ulang APK baru.
