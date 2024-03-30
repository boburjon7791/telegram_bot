package bot.telegram.sahih_akamiz_uchun.services;

import bot.telegram.sahih_akamiz_uchun.entities.History;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface HistoryService {
    void save(History history);
    List<History> historiesByDate(LocalDate localDate);
}
