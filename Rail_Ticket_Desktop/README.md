# RAIL TICKET – UAS Praktikum PBO

Aplikasi desktop pemesanan tiket kereta api dengan Java 17, JavaFX, FXML, CSS, Maven, dan SQLite.

## 1. Persyaratan
- JDK 17 atau lebih baru
- Apache Maven 3.8+
- VS Code / IntelliJ IDEA / NetBeans
- Internet saat pertama kali Maven mengunduh dependency

## 2. Cara menjalankan
1. Ekstrak ZIP.
2. Buka folder `Rain_Ticket_PBO_Lengkap` di IDE.
3. Buka terminal pada folder yang memiliki `pom.xml`.
4. Jalankan:
   ```bash
   mvn clean
   mvn javafx:run
   ```
5. Database `db_rail_ticket_desktop.db` dibuat otomatis di folder proyek.

## 3. Akun demo
| Role | Email | Password |
|---|---|---|
| Admin | admin@rainticket.id | admin123 |
| Pelanggan | pelanggan@rainticket.id | user123 |

Pelanggan baru dapat membuat akun melalui tombol Daftar.

## 4. Konsep PBO yang digunakan
- Class dan Object: User, Admin, Customer, Train, TrainSchedule, Booking.
- Constructor, this, visibility private/protected, getter dan setter.
- Package dan import.
- Inheritance: Admin dan Customer extends User.
- Polymorphism: overriding dashboardInfo()/getTicketType(), overloading pencarian jadwal.
- Association: Booking memiliki referensi Customer dan TrainSchedule.
- Aggregation: TrainCatalog menyimpan ArrayList<TrainSchedule> yang dibuat terpisah.
- Abstract class: Ticket.
- Interface: PaymentMethod.
- Nested class: Booking.TicketCode (static nested) dan Booking.BoardingPass (inner class).

## 5. Fitur
### Admin
Dashboard statistik, tambah/edit/hapus jadwal, melihat pesanan, mengonfirmasi pembayaran, membatalkan pesanan, dan melihat data pelanggan.

### Pelanggan
Registrasi/login, pencarian tiket, filter asal/tujuan/tanggal/kelas, pemesanan, pilihan kursi, simulasi pembayaran, riwayat pesanan, dan tampilan e-ticket.

## Catatan
Pembayaran hanya simulasi untuk kebutuhan praktikum. Akun demo menggunakan password teks sederhana agar mudah dipahami dalam latihan; jangan gunakan pola ini untuk aplikasi produksi.


## Pembaruan fitur
- Pencarian jadwal menampilkan daftar perjalanan dengan detail rute, tanggal, jam, kelas, harga, dan kursi.
- Data contoh jadwal tambahan dibuat otomatis saat database memiliki kurang dari 10 jadwal.
- Pembayaran menyediakan Transfer Bank, Virtual Account, E-Wallet, dan QRIS beserta langkah pembayaran.
- QRIS pada project ini adalah QR demo untuk simulasi akademik, bukan kode pembayaran merchant sungguhan.
- Pelanggan dapat membatalkan tiket dari halaman Pesanan Saya. Status berubah menjadi DIBATALKAN; pembayaran yang sudah lunas diberi status refund simulasi REFUND_DIPROSES.

Catatan: aplikasi ini menggunakan pembayaran simulasi dan tidak terhubung ke bank, e-wallet, atau penyedia QRIS.