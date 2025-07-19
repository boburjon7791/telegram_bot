package bot.telegram.sahih_akamiz_uchun.controllers;

import bot.telegram.sahih_akamiz_uchun.entities.Service;
import bot.telegram.sahih_akamiz_uchun.entities.Utils;
import bot.telegram.sahih_akamiz_uchun.repositories.UtilsDao;
import bot.telegram.sahih_akamiz_uchun.services.ServiceBot;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TelegramBotController extends TelegramLongPollingBot{
    private final ServiceBot serviceBot;

    private final UtilsDao utilsDao;

    public TelegramBotController(String token, String name, ServiceBot serviceBot, UtilsDao utilsDao){
        super(token);
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
                    if (service.getImageId() != null && service.getImageChatId() != null) {
                        ForwardMessage forwardMessage = new ForwardMessage();
                        forwardMessage.setChatId(chatId);
                        forwardMessage.setFromChatId(service.getImageChatId());
                        forwardMessage.setMessageId(Integer.parseInt(service.getImageId()));
                        execute(forwardMessage);
                    }else {
                        SendPhoto sendPhoto=new SendPhoto();
                        sendPhoto.setChatId(chatId);
                        sendPhoto.setPhoto(new InputFile(serviceBot.getFile2(service.getImage())));
                        Message sentMessage = sendPhoto(sendPhoto);
                        service.setImageId(sentMessage.getMessageId().toString());
                        service.setImageChatId(chatId.toString());
                        serviceBot.update(service);

                    }
                }catch (Exception e){
                    log.error(service.getName()+" : "+service.getImage()+" rasm jo'natib bo'lmadi", e);
                }

                try {
                    if (service.getVideoId()!=null && service.getVideoChatId() != null) {
                        ForwardMessage forwardMessage = new ForwardMessage();
                        forwardMessage.setChatId(chatId);
                        forwardMessage.setFromChatId(service.getVideoChatId());
                        forwardMessage.setMessageId(Integer.parseInt(service.getVideoId()));
                        execute(forwardMessage);
                    }else {
                        SendVideo sendVideo=new SendVideo();
                        sendVideo.setChatId(chatId);
                        File video = serviceBot.getFile2(service.getVideo());
                        sendVideo.setVideo(new InputFile(video));
                        Message sentVideo = sendVideo(sendVideo);
                        service.setVideoId(sentVideo.getMessageId().toString());
                        service.setVideoChatId(chatId.toString());
                        serviceBot.update(service);
                    }
                }catch (Exception e){
                    log.error(service.getName()+" : "+service.getVideo()+" video jo'natib bo'lmadi", e);
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
    public Message sendPhoto(SendPhoto sendPhoto){
        log.info("Sending photo");
        Message message = execute(sendPhoto);
        log.info("Send photo");
        return message;
    }

    @Async
    @SneakyThrows
    public Message sendVideo(SendVideo sendVideo){
        log.info("Sending video");
        Message message = execute(sendVideo);
        log.info("Send video");
        return message;
    }

    @SneakyThrows
    @Async
    public void services(Update update) {
        List<InlineKeyboardMarkup> markups = new ArrayList<>();
        List<Service> services;
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

        AtomicInteger atomicInteger=new AtomicInteger(0);
        services.forEach(s-> {
            InlineKeyboardButton button=new InlineKeyboardButton(s.getName());
            button.setCallbackData("service_"+s.getId().toString());
            InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
            inlineKeyboardMarkup.setKeyboard(List.of(List.of(button)));
            markups.add(inlineKeyboardMarkup);
        });
        // Set the keyboard to the markup
        // Add it to the message
        SendMessage sendMessage=new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Servislar:");
        execute(sendMessage);

        markups.forEach(replyMarkup -> {
        SendMessage sendButton=new SendMessage();
        sendButton.setReplyMarkup(replyMarkup);
        sendButton.setText(String.valueOf(atomicInteger.incrementAndGet()));
        sendButton.setChatId(update.getMessage().getChatId());
        try {
            execute(sendButton);
        } catch (TelegramApiException e) {
            log.error("error : ",e);
        }
    });
}
@Override
@SneakyThrows
@Async
public void onUpdateReceived(Update update) {
    String message=update.getMessage()!=null?update.getMessage().getText():"";
    if (update.getCallbackQuery()!=null && update.getCallbackQuery().getData().startsWith("service_")) {
        message=update.getCallbackQuery().getData();
        selectService(message, update.getCallbackQuery().getMessage().getChatId());
        sendCommands(update);
        return;
    }
    switch (message) {
        case "/services"-> services(update);
        case "/start" -> start(update);
        default->{
            if (update.getMessage()==null) {
                sendCommands(update);
                return;
            }
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(update.getMessage().getChatId());
            sendMessage.setText("Xatolik sodir bo'ldi");
            sendMessage(sendMessage);
        }
    }
    sendCommands(update);
}

public void sendCommands(Update update){
    List<BotCommand> commandList = new ArrayList<>();
    commandList.add(new BotCommand("/start", "Botni ishga tushirish"));
    commandList.add(new BotCommand("/services", "Xizmatalar"));
//    commandList.add(new BotCommand("/info", "Bot haqida ma'lumot"));

    SetMyCommands setMyCommands = new SetMyCommands();
    setMyCommands.setCommands(commandList);
    setMyCommands.setScope(new BotCommandScopeDefault()); // umumiy holat uchun

    try {
        execute(setMyCommands);
    } catch (TelegramApiException e) {
        e.printStackTrace();
    }
}


    @Override
    public String getBotUsername() {
        return "sahih_print_bot";
    }
}
