package sukhdmi.telegramBots.schoolEventBot.service;

import lombok.Setter;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import sukhdmi.telegramBots.schoolEventBot.config.ApplicationContextProvider;
import sukhdmi.telegramBots.schoolEventBot.dao.EventCashDAO;
import sukhdmi.telegramBots.schoolEventBot.model.TelegramBot;

@Setter
public class SendEvent  extends Thread{//поток с событием

    private long eventCashId;
    private SendMessage sendMessage;

    public SendEvent() {
    }

    @SneakyThrows
    @Override
    public void run(){
        TelegramBot telegramBot = ApplicationContextProvider.getApplicationContext().getBean(TelegramBot.class);
        EventCashDAO eventCashDAO = ApplicationContextProvider.getApplicationContext().getBean(EventCashDAO.class);
        telegramBot.execute(sendMessage);
        eventCashDAO.delete(eventCashId);//если событие сработало, то удаляем его из базы неразрешенных событий
    }
}
