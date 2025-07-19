package bot.telegram.sahih_akamiz_uchun;

import bot.telegram.sahih_akamiz_uchun.controllers.TelegramBotController;
import bot.telegram.sahih_akamiz_uchun.repositories.HistoryDao;
import bot.telegram.sahih_akamiz_uchun.repositories.UtilsDao;
import bot.telegram.sahih_akamiz_uchun.services.ServiceBot;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class SahihAkamizUchunApplication {
	@Value(value = "${bot.token}")
	private String token;

	private final UtilsDao utilsDao;
	private final ServiceBot serviceBot;
	private final HistoryDao historyDao;
	@PostConstruct
	public void init() throws TelegramApiException {
		TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
		try {
			TelegramBotController sahihPrintBot = new TelegramBotController(token, "sahih_print_bot", serviceBot, utilsDao);
			telegramBotsApi.registerBot(sahihPrintBot);
		} catch (TelegramApiException e) {
			throw e;
		}
	}
	public static void main(String[] args) throws Exception {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tashkent"));
		SpringApplication.run(SahihAkamizUchunApplication.class, args);
	}
	// @Scheduled(cron = "0 0,10,20,30,40,50 * * * *")
	public void deleteCachedImages(){
		ServiceBot.cachedImages.entrySet()
				.removeIf(entry -> {
					log.info("{} file was removed from caches",entry.getKey());
				return entry.getValue().isBefore(LocalDateTime.now());
				});
	}
	@Scheduled(cron = "0 0 0 1 * *")
	public void deleteOldHistories(){
		historyDao.deleteOldHistories(LocalDate.now().minusMonths(6));
		log.info("deleted old histories");
	}
	@Bean
	public TaskExecutor taskExecutor(){
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(100);
		executor.setMaxPoolSize(150);
		executor.setQueueCapacity(120);
		executor.setThreadNamePrefix("Sahih Print : ");
		return executor;
	}
}
