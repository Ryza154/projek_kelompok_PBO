# Pemetaan Materi PBO pada Rail Ticket

1. **Class dan Object (Praktikum 1)**  
   Class adalah cetakan objek. Implementasi: `User`, `Admin`, `Customer`, `Train`, `Station`, `TrainSchedule`, dan `Booking`.

2. **Constructor, Visibility, this, Getter/Setter, Package (Praktikum 2)**  
   Constructor mengisi data awal objek. Atribut penting menggunakan `private`, atribut `role` menggunakan `protected`, dan data diakses melalui getter/setter. `this` digunakan untuk membedakan atribut dengan parameter. Class dikelompokkan ke package `model`, `dao`, `database`, dan `ui`.

3. **Inheritance (Praktikum 3)**  
   `Admin extends User` dan `Customer extends User`. `User` menjadi superclass, sedangkan Admin dan Customer menjadi subclass.

4. **Polimorfisme (Praktikum 4)**  
   Overriding diterapkan pada `dashboardInfo()` di Admin dan Customer serta `getTicketType()` di Booking. Overloading diterapkan pada method `search()` di ScheduleDAO dengan jumlah parameter berbeda.

5. **Asosiasi dan Agregasi (Praktikum 5)**  
   Booking menyimpan referensi ke Customer dan TrainSchedule (asosiasi). TrainCatalog menyimpan daftar TrainSchedule menggunakan `ArrayList` (agregasi). Hubungan satu pelanggan dapat mempunyai banyak booking (one-to-many).

6. **Abstract dan Interface (Praktikum 5 lanjutan)**  
   `Ticket` adalah abstract class dengan method abstract `getTicketType()`. `PaymentMethod` adalah interface yang diimplementasikan `SimulatedPayment`.

7. **Nested Class (Praktikum 6)**  
   `Booking.TicketCode` merupakan static nested class. `Booking.BoardingPass` merupakan inner class yang terikat pada objek Booking.

## Contoh alur penggunaan
Pelanggan login → cari jadwal → pilih kereta → isi penumpang dan kursi → buat pesanan → bayar simulasi → tampilkan e-ticket.  
Admin login → lihat dashboard → tambah/edit/hapus jadwal → lihat pesanan → konfirmasi atau batalkan transaksi.