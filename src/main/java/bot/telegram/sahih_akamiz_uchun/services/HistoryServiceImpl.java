package bot.telegram.sahih_akamiz_uchun.services;

import bot.telegram.sahih_akamiz_uchun.entities.History;
import bot.telegram.sahih_akamiz_uchun.exceptions.NotFoundException;
import bot.telegram.sahih_akamiz_uchun.repositories.HistoryDao;
import bot.telegram.sahih_akamiz_uchun.repositories.ServiceDao;
import bot.telegram.sahih_akamiz_uchun.repositories.UserDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HistoryServiceImpl implements HistoryService {
    private final HistoryDao historyDao;
    private final UserDao userDao;
    private final ServiceDao serviceDao;
    @Override
    @Async
    public void save(History history) {
        historyDao.save(history);
    }

    @Override
    public List<History> historiesByDate(LocalDate localDate) {
        List<History> histories;
        if (localDate==null) {
            histories = historyDao.findAll(PageRequest.of
                (0, 100, Sort.by("dateTime").descending())).getContent();
        }else {
            histories = historyDao.findAllByDate(localDate);
        }
        if (histories.isEmpty()) {
            throw new NotFoundException("Savdolar tarixi mavjud emas");
        }
        return histories;
    }
}
