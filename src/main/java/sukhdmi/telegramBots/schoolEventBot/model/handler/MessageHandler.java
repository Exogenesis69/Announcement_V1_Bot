package sukhdmi.telegramBots.schoolEventBot.model.handler;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import sukhdmi.telegramBots.schoolEventBot.cash.BotStateCash;
import sukhdmi.telegramBots.schoolEventBot.cash.EventCash;
import sukhdmi.telegramBots.schoolEventBot.dao.UserDAO;
import sukhdmi.telegramBots.schoolEventBot.entity.Event;
import sukhdmi.telegramBots.schoolEventBot.model.BotState;
import sukhdmi.telegramBots.schoolEventBot.service.MenuService;

@Component
public class MessageHandler { //Обработка входящих текстовых сообщений

    private final UserDAO userDAO;
    private final MenuService menuService;
    private final EventHandler eventHandler;
    private final BotStateCash botStateCash;
    private final EventCash eventCash;

    public MessageHandler(UserDAO userDAO, MenuService menuService, EventHandler eventHandler, BotStateCash botStateCash, EventCash eventCash) {
        this.userDAO = userDAO;
        this.menuService = menuService;
        this.eventHandler = eventHandler;
        this.botStateCash = botStateCash;
        this.eventCash = eventCash;
    }

    public BotApiMethod<?> handle(Message message, BotState botState) {
        long userId = message.getFrom().getId();
        long chatId = message.getChatId();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));

        if (!userDAO.isExist(userId)){//Если пользователь новый
            return eventHandler.saveNewUser(message, userId, sendMessage);
        }

        botStateCash.saveBotState(userId, botState);//Сохраняем состояние бота в кеше
        switch (botState.name()) {//Если состояние...
            case ("START"):
                return menuService.getMainMenuMessage(message.getChatId(),"Воспользуйтесь главным меню", userId);

            case ("ENTERDESCRIPTION")://вводим описание для создания события
                return eventHandler.enterDescriptionHandler(message, userId);


            case ("MYEVENTS")://выводим список событий пользователя
                return eventHandler.myEventHandler(userId);

            case ("ENTERNUMBEREVENT")://удаляем событие
                return eventHandler.removeEventHandler(message, userId);

            case ("ENTERDATE")://Выводим дату создания события
                return eventHandler.enterDateHandler(message, userId);

            case ("CREATE")://начинаем создавать событие,устанавливаем состояние на следующий шаг
                botStateCash.saveBotState(userId,botState.ENTERDESCRIPTION);
                eventCash.saveEventCash(userId,new Event());//добавляем новое событие в кеш
                return sendMessage;

            case ("ENTERNUMBERFOREDIT")://показываем пользователю выбранные события
                return eventHandler.editHandler(message, userId);

            case ("EDITDATE")://сохраняем новую дату в базе данных
                return eventHandler.editDate(message);

            case ("EDITDESCRIPTION")://сохраняем новое описание в базу данных
                return eventHandler.editDescription(message);

                case ("ALLUSERS")://только админ
                return eventHandler.allUsers(userId);

            case ("ALLEVENTS")://только админ
                return eventHandler.allEvents(userId);

            case ("ENTERNUMBERUSER")://только админ
                return eventHandler.removeUserHandler(message,userId);

            case ("ONEVENT")://вкл/выкл уведомления
                return eventHandler.onEvent(message);

            case ("ENTERTIME"):
                return eventHandler.enterLocalTimeUser(message);//установка пользователем часового пояса для корректной отправки события
            default:
        throw new IllegalStateException("Unexpected value: " + botState);
        }
    }
}
