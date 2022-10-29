package sukhdmi.telegramBots.schoolEventBot.model.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import sukhdmi.telegramBots.schoolEventBot.cash.BotStateCash;
import sukhdmi.telegramBots.schoolEventBot.cash.EventCash;
import sukhdmi.telegramBots.schoolEventBot.entity.Event;
import sukhdmi.telegramBots.schoolEventBot.model.BotState;
import sukhdmi.telegramBots.schoolEventBot.model.EventFreq;
import sukhdmi.telegramBots.schoolEventBot.service.MenuService;


@Component
public class CallBackQueryHandler {
    private final BotStateCash botStateCash;
    private final EventCash eventCash;
    private final MenuService menuService;
    private final EventHandler eventHandler;

    @Autowired
    public CallBackQueryHandler(BotStateCash botStateCash, EventCash eventCash, MenuService menuService, EventHandler eventHandler) {
        this.botStateCash = botStateCash;
        this.eventCash = eventCash;
        this.menuService = menuService;
        this.eventHandler = eventHandler;
    }

    public BotApiMethod<?> processCallbackQuery(CallbackQuery buttonQuery) {
        final long chatId = buttonQuery.getMessage().getChatId();
        final long userId = buttonQuery.getFrom().getId();

        BotApiMethod<?> callBackAnswer = null;

        String data = buttonQuery.getData();

        switch (data) {
            case ("buttonDel"):
                callBackAnswer = new SendMessage(String.valueOf(chatId), "Введите номер напоминания из списка.");
                botStateCash.saveBotState(userId, BotState.ENTERNUMBEREVENT);
                break;
            case ("buttonDelUser"):
                callBackAnswer = new SendMessage(String.valueOf(chatId), "Enter ID User.");
                botStateCash.saveBotState(userId, BotState.ENTERNUMBERUSER);
                break;
            case ("buttonEdit"):
                callBackAnswer = new SendMessage(String.valueOf(chatId), "Введите номер напоминания из списка.");
                botStateCash.saveBotState(userId, BotState.ENTERNUMBERFOREDIT);
                break;
            case ("buttonHour"):
                callBackAnswer = new SendMessage(String.valueOf(chatId), "Необходимо ввести местное время в формате HH, например, если сейчас 21:45, то введите 21, это необходимо для корректного оповещения в соответствии с вашим часовым поясом.");
                botStateCash.saveBotState(userId, BotState.ENTERTIME);
                break;
            case ("buttonOneTime"):
                if (botStateCash.getBotStateMap().get(userId).name().equals("ENTERDATE")) {
                    callBackAnswer = eventHandler.saveEvent(EventFreq.TIME, userId, chatId);
                } else {
                    Event event = eventCash.getEventMap().get(userId);
                    event.setFreq(EventFreq.TIME);
                    eventCash.saveEventCash(userId, event);
                    callBackAnswer = eventHandler.editEvent(chatId, userId);
                }
                break;
            case ("buttonOneTimeMonth"):
                if (botStateCash.getBotStateMap().get(userId).name().equals("ENTERDATE")) {
                    callBackAnswer = eventHandler.saveEvent(EventFreq.MONTH, userId, chatId);
                } else {
                    Event event = eventCash.getEventMap().get(userId);
                    event.setFreq(EventFreq.MONTH);
                    eventCash.saveEventCash(userId, event);
                    callBackAnswer = eventHandler.editEvent(chatId, userId);
                }
                break;
            case ("buttonEveryDay"):
                if (botStateCash.getBotStateMap().get(userId).name().equals("ENTERDATE")) {
                    callBackAnswer = eventHandler.saveEvent(EventFreq.EVERYDAY, userId, chatId);
                } else {
                    Event event = eventCash.getEventMap().get(userId);
                    event.setFreq(EventFreq.EVERYDAY);
                    eventCash.saveEventCash(userId, event);
                    callBackAnswer = eventHandler.editEvent(chatId, userId);
                }
                break;
            case ("buttonOneTimeYear"):
                if (botStateCash.getBotStateMap().get(userId).name().equals("ENTERDATE")) {
                    callBackAnswer = eventHandler.saveEvent(EventFreq.YEAR, userId, chatId);
                } else {
                    Event event = eventCash.getEventMap().get(userId);
                    event.setFreq(EventFreq.YEAR);
                    eventCash.saveEventCash(userId, event);
                    callBackAnswer = eventHandler.editEvent(chatId, userId);
                }
                break;
            case ("buttonDate"):
                if (eventCash.getEventMap().get(userId).getEventId() != 0) {
                    callBackAnswer = new SendMessage(String.valueOf(chatId), "Введите дату предстоящего события в форме DD.MM.YYYY HH:MM, например - 27.09.2022 23:43, либо 27.09.2022");
                    botStateCash.saveBotState(userId, BotState.EDITDATE);
                } else {
                    callBackAnswer = new SendMessage(String.valueOf(chatId), "Нарушена последовательность действий");
                }
                break;
            case ("buttonDescription"):
                if (eventCash.getEventMap().get(userId).getEventId() != 0) {
                    callBackAnswer = new SendMessage(String.valueOf(chatId), "Введите описание события");
                    botStateCash.saveBotState(userId, BotState.EDITDESCRIPTION);
                } else {
                    callBackAnswer = new SendMessage(String.valueOf(chatId), "Нарушена последовательность действий");
                }
                break;
            case ("buttonFreq"):
                if (eventCash.getEventMap().get(userId).getEventId() != 0) {
                    SendMessage sendMessage = new SendMessage(String.valueOf(chatId), "Выберите период повторения: Единоразово(сработает один раз и удалится), " +
                            "Ежедневно в указанный час, " +
                            "1 раз в месяц в указанную дату, 1 раз в год в указанное число");
                    botStateCash.saveBotState(userId, BotState.EDITFREQ);
                    sendMessage.setReplyMarkup(menuService.getInlineMessageButtonsForEnterDate());
                    callBackAnswer = sendMessage;
                } else {
                    callBackAnswer = new SendMessage(String.valueOf(chatId), "Нарушена последовательность действий");
                }
        } return callBackAnswer;
    }
}

