package sukhdmi.telegramBots.schoolEventBot.service;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import sukhdmi.telegramBots.schoolEventBot.dao.EventCashDAO;
import sukhdmi.telegramBots.schoolEventBot.entity.EventCashEntity;
import sukhdmi.telegramBots.schoolEventBot.model.TelegramBot;

import javax.annotation.PostConstruct;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;

@Component
public class SendEventFromCache {

    private final EventCashDAO eventCashDAO;
    private final TelegramBot telegramBot;

    @Value("${telegrambot.adminId}")
    private int admin_id;

    @Autowired

    public SendEventFromCache(EventCashDAO eventCashDAO, TelegramBot telegramBot) {
        this.eventCashDAO = eventCashDAO;
        this.telegramBot = telegramBot;
    }//все работает до сюда

    @PostConstruct
    @SneakyThrows
    private void afterStart() {
        List<EventCashEntity> list = eventCashDAO.findAllEventCash();

//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(String.valueOf(admin_id));
//        sendMessage.setText("Произошла перезагрузка!");
//        telegramBot.execute(sendMessage);
        //а вот в этом блоке проблема

        if (!list.isEmpty()) {
            for (EventCashEntity eventCashEntity : list) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(eventCashEntity.getDate());
                SendEvent sendEvent = new SendEvent();
                sendEvent.setSendMessage(new SendMessage(String.valueOf(eventCashEntity.getUserId()), eventCashEntity.getDescription()));
                sendEvent.setEventCashId(eventCashEntity.getId());
                new Timer().schedule(new SimpleTask(sendEvent), calendar.getTime());
            }
        }
    }
}
