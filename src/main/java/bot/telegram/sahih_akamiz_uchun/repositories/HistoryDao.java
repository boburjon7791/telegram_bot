package bot.telegram.sahih_akamiz_uchun.repositories;

import bot.telegram.sahih_akamiz_uchun.entities.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface HistoryDao extends JpaRepository<History, UUID> {
    @Query(nativeQuery = true,value = "select * from history where date(date_time)=?1")
    List<History> findAllByDate(LocalDate date);

    @Modifying
    @Query(nativeQuery = true,value = "delete from history h where date(h.date_time)<?1")
    void deleteOldHistories(LocalDate date);
}
