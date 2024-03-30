package bot.telegram.sahih_akamiz_uchun.controllers;

import bot.telegram.sahih_akamiz_uchun.entities.Service;
import bot.telegram.sahih_akamiz_uchun.entities.Utils;
import bot.telegram.sahih_akamiz_uchun.repositories.UtilsDao;
import bot.telegram.sahih_akamiz_uchun.services.ServiceBot;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
@Slf4j
public class TelegramBotController extends TelegramLongPollingBot{
    private String token;
    private ServiceBot serviceBot;

    private UtilsDao utilsDao;


    private String name;
    public TelegramBotController(String token, String name, ServiceBot serviceBot, UtilsDao utilsDao){
        super(token);
        this.name = name;
        this.serviceBot=serviceBot;
        this.utilsDao=utilsDao;
    }

    @SneakyThrows
    @Async
    public void start(Update update){
        SendMessage sendMessage=new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("""
            Assalomu alaykum
            Sahih print imkoniyat markaziga xush kelibsiz.
            Ishonavering. Bizda arzonroq.
            """);
        SendPhoto sendPhoto=new SendPhoto();
        sendPhoto.setChatId(update.getMessage().getChatId());
        ClassLoader classLoader=getClass().getClassLoader();
        InputStream resourceAsStream = classLoader.getResourceAsStream("static/home_page.jpg");
        sendPhoto.setPhoto(new InputFile(resourceAsStream,"home_page.jpg"));
        sendPhoto(sendPhoto);
        execute(sendMessage);
    }

    @Async
    public void selectService(String message, Long chatId) {
            Service service;
            try {
                try {
                    String id = message.substring(message.lastIndexOf("_") + 1);
                    service=serviceBot.get(Long.valueOf(id));
                }catch (Exception e){
                    /*SendMessage sendMessage=new SendMessage();
                    sendMessage.setChatId(update.getMessage().getChatId());
                    sendMessage.setText(e.getMessage());
                    execute(sendMessage);*/
                    e.printStackTrace();
                    return;
                }

                System.out.println("service.getName() = " + service.getName());
                InlineKeyboardButton button=new InlineKeyboardButton("Barcha xizmatlar");
            button.setCallbackData("/services");
            InlineKeyboardMarkup markup=new InlineKeyboardMarkup();
            markup.setKeyboard(List.of(List.of(button)));
            Utils utils = utilsDao.getValueByKey("phone");

                SendMessage sendMessage= new SendMessage();
                String message2="Xizmat turi : "+service.getName()+"\n"+"Xizmat narxi : 1 "+service.getType()+
                        " uchun "+service.getPrice()+" "+service.getCurrency() +"\nAgar taklifimiz sizga qiziq bo'lsa "+utils.value+" telefon raqamiga qo'ng'iroq qiling";
                sendMessage.setChatId(chatId);
                sendMessage.setText(message2);
                sendMessage(sendMessage);

                try {
                    SendPhoto sendPhoto=new SendPhoto();
                    sendPhoto.setChatId(chatId);
                    sendPhoto.setPhoto(new InputFile(serviceBot.getFile2(service.getImage())));
                    sendPhoto(sendPhoto);
                }catch (Exception e){
                    log.error(service.getName()+" : "+service.getImage()+" rasm jo'natib bo'lmadi");
                }

                try {
                    SendVideo sendVideo=new SendVideo();
                    sendVideo.setChatId(chatId);
                    File video = serviceBot.getFile2(service.getVideo());
                    sendVideo.setVideo(new InputFile(video));
                    sendVideo(sendVideo);
                }catch (Exception e){
                    log.error(service.getName()+" : "+service.getVideo()+" video jo'natib bo'lmadi");
                }

            } catch (Exception e) {
                e.printStackTrace();
                
            }
    }
    @Async
    @SneakyThrows
    public void sendMessage(SendMessage sendMessage){
        log.info("Sending message");
        execute(sendMessage);
        log.info("Send message");
    }
    @Async
    @SneakyThrows
    public void sendPhoto(SendPhoto sendPhoto){
        log.info("Sending photo");
        execute(sendPhoto);
        log.info("Send photo");
    }

    @Async
    @SneakyThrows
    public void sendVideo(SendVideo sendVideo){
        log.info("Sending video");
        execute(sendVideo);
        log.info("Send video");
    }

    @SneakyThrows
    @Async
public void services(Update update) {
    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
    List<InlineKeyboardButton> rows = new ArrayList<>();
    List<Service> services;
        System.out.println("update.getMessage().getChatId() = " + update.getMessage().getChatId());
        System.out.println("update.getMessage().getText() = " + update.getMessage().getText());
        try {
        services=serviceBot.services();
            if (services.isEmpty()) {
                throw new RuntimeException("Servislar mavjud emas");
            }
    } catch (Exception e) {
        SendMessage sendMessage= new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(e.getMessage());
        return;
    }
    
    services.forEach(s-> {
        InlineKeyboardButton button=new InlineKeyboardButton(s.getName());
        button.setCallbackData("service_"+s.getId().toString());
        rows.add(button);
    }
    );
    markup.setKeyboard(List.of(rows));
    // Set the keyboard to the markup
    // Add it to the message
     SendMessage sendMessage=new SendMessage();
     sendMessage.setChatId(update.getMessage().getChatId());
     sendMessage.setText("Servislar");
     sendMessage.setReplyMarkup(markup);
     execute(sendMessage);
}
@Override
@SneakyThrows
@Async
public void onUpdateReceived(Update update) {
    String message;
    try {
        message=update.getMessage().getText();
    }catch (Exception e){
        message=update.getCallbackQuery().getData();
    }
    if (message.startsWith("service_") && message.substring(message.lastIndexOf("_")+1).matches("[0-9]")) {
        selectService(message, update.getCallbackQuery().getMessage().getChatId());
        return;
    }
    switch (message) {
        case "/services"-> services(update);
        case "/start" -> start(update);
        default->{
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId());
            sendMessage.setText("Xatolik sodir bo'ldi");
            sendMessage(sendMessage);
        }
    }
}

    @Override
    public String getBotUsername() {
        return "sahih_print_bot";
    }
}
