package id.rainticket.model;

import java.util.ArrayList;
import java.util.List;

public class TrainCatalog {
    // Aggregation: jadwal dibuat di luar katalog lalu disimpan sebagai referensi.
    private final ArrayList<TrainSchedule> schedules;

    public TrainCatalog() {
        this.schedules = new ArrayList<>();
    }

    public void addSchedule(TrainSchedule schedule) {
        schedules.add(schedule);
    }

    public List<TrainSchedule> getSchedules() {
        return new ArrayList<>(schedules);
    }
}
