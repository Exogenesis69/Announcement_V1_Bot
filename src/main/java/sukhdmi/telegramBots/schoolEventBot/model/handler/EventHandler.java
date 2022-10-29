package sukhdmi.telegramBots.schoolEventBot.model.handler;

import org.hibernate.action.internal.EntityActionVetoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import sukhdmi.telegramBots.schoolEventBot.cash.BotStateCash;
import sukhdmi.telegramBots.schoolEventBot.cash.EventCash;
import sukhdmi.telegramBots.schoolEventBot.dao.EventDAO;
import sukhdmi.telegramBots.schoolEventBot.dao.UserDAO;
import sukhdmi.telegramBots.schoolEventBot.entity.Event;
import sukhdmi.telegramBots.schoolEventBot.entity.User1;
import sukhdmi.telegramBots.schoolEventBot.model.BotState;
import sukhdmi.telegramBots.schoolEventBot.model.EventFreq;
import sukhdmi.telegramBots.schoolEventBot.service.MenuService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Component
public class EventHandler {//основная логика обработки событий

    private final BotStateCash botStateCash;//для сохранения состояния бота

    private final EventCash eventCash;//для сохранения этапов создания события

    private final UserDAO userDAO;
    private final EventDAO eventDAO;
    private final MenuService menuService;

    @Value("${telegrambot.adminId}")
    private int admin_id;

    @Autowired
    public EventHandler(BotStateCash botStateCash, EventCash eventCash, UserDAO userDAO, EventDAO eventDAO, MenuService menuService) {
        this.botStateCash = botStateCash;
        this.eventCash = eventCash;
        this.userDAO = userDAO;
        this.eventDAO = eventDAO;
        this.menuService = menuService;
    }

    public SendMessage saveNewUser(Message message, long userId, SendMessage sendMessage) {//Создаем нового пользователя, если впервые
        String userName = message.getFrom().getUserName();
        User1 user = new User1();
        user.setId(userId);
        user.setName(userName);
        user.setOn(true);
        userDAO.save(user);
        sendMessage.setText("В первый сеанс необходимо ввести местное время в формате HH, например, если сейчас 21:45, то введите 21, это необходимо для корректного оповещения в соответствии с вашим часовым поясом.");
        botStateCash.saveBotState(userId, BotState.ENTERTIME);
        return sendMessage;
    }

    public BotApiMethod<?> onEvent(Message message) {//Изменение состояния рассылки
        User1 user1 = userDAO.findByUserId(message.getFrom().getId());

        boolean on = user1.isOn();
        on = !on;
        user1.setOn(on);
        userDAO.save(user1);
        botStateCash.saveBotState(message.getFrom().getId(), BotState.START);

        return menuService.getMainMenuMessage(message.getChatId(),
                "Изменения сохранены", message.getFrom().getId());
    }

    public BotApiMethod<?> enterLocalTimeUser(Message message) {//устанавливаем часовой пояс
        long userId = message.getFrom().getId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH");
        Date nowHour = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nowHour);
        int num;
        try {
            num = Integer.parseInt(message.getText());
        } catch (NumberFormatException e) {
            sendMessage.setText("Введенные символы не являются числами, повторите ввод.");
            return sendMessage;
        }
        if (num < 0 || num > 24) {
            sendMessage.setText("Вы ввели неверное время, повторите.");
            return sendMessage;
        }
        Date userHour;
        try {
            userHour = simpleDateFormat.parse(message.getText());
        } catch (ParseException e) {
            sendMessage.setText("Вы ввели неверное время, повторите.");
            return sendMessage;
        }
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(userHour);

        int serverHour = calendar.get(Calendar.HOUR_OF_DAY);
        int clientHour = calendar1.get(Calendar.HOUR_OF_DAY);

        int timeZone = clientHour - serverHour;//рассчет часового пояса
        sendMessage.setText("Ваш часовой пояс: " + "+" + timeZone);
        User1 user = userDAO.findByUserId(userId);
        user.setTimeZone(timeZone);
        userDAO.save(user);

        botStateCash.saveBotState(userId, BotState.START);
        return sendMessage;
    }

    public BotApiMethod<?> removeUserHandler(Message message, long userId) {//Удаление пользователя(только админ)
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));

        User1 user;
        try {
            long i = Long.parseLong(message.getText());
            user = userDAO.findByUserId(i);
        } catch (NumberFormatException e) {
            sendMessage.setText("Введенная строка не является числом, попробуйте снова!");
            return sendMessage;
        }
        if (user == null) {
            sendMessage.setText("Введенное число отсутствует в списке, попробуйте снова!");
            return sendMessage;
        }

        userDAO.removeUser(user);
        botStateCash.saveBotState(userId, BotState.START);
        sendMessage.setText("Удаление прошло успешно");
        return sendMessage;
    }

    public BotApiMethod<?> allEvents(long userId) {//получаем список всех событий(только админ)
        List<Event> list = eventDAO.findAllEvent();
        botStateCash.saveBotState(userId, BotState.START);
        return eventListBuilder(userId, list);
    }

    public BotApiMethod<?> myEventHandler(long userId) {//получаем список всех событий(Пользователь)
        List<Event> list = eventDAO.findByUserId(userId);
        return eventListBuilder(userId, list);
    }


    public BotApiMethod<?> eventListBuilder(long userId, List<Event> list) {//вохвращает скомпилированный список событий
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(String.valueOf(userId));
        StringBuilder builder = new StringBuilder();
        if (list.isEmpty()) {
            replyMessage.setText("Уведомления отсутствуют!");
            return replyMessage;
        }
        for (Event event : list) {
            builder.append(buildEvent(event));
        }
        replyMessage.setText(builder.toString());
        replyMessage.setReplyMarkup(menuService.getInlineMessageButtons());
        return replyMessage;
    }

    public BotApiMethod<?> allUsers(long userId) {
        SendMessage replyMessage = new SendMessage();
        replyMessage.setChatId(String.valueOf(userId));
        StringBuilder builder = new StringBuilder();
        List<User1> list = userDAO.findAllUsers();
        for (User1 user : list) {
            builder.append(buildUser(user));
        }
        replyMessage.setText(builder.toString());
        replyMessage.setReplyMarkup(menuService.getInlineMessageButtonsAllUser());
        botStateCash.saveBotState(userId, BotState.START);
        return replyMessage;
    }

    private StringBuilder buildUser(User1 user) {//скомпилированный список пользователей
        StringBuilder builder = new StringBuilder();
        long userId = user.getId();
        String name = user.getName();
        builder.append(userId).append(". ").append(name).append("\n");
        return builder;
    }

    public BotApiMethod<?> editDate(Message message) {//Обрабатывает введенную дату
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        long userId = message.getFrom().getId();
        Date date;
        try {
            date = parseDate(message.getText());
        } catch (ParseException e) {
            sendMessage.setText("Не удается распознать указанную дату и время, попробуйте еще раз");
            return sendMessage;
        }

        Event event = eventCash.getEventMap().get(userId);//получаем данные предыдущегго набора
        event.setDate(date);
        eventCash.saveEventCash(userId, event);
        return editEvent(message.getChatId(), userId);//ожидается завершение ввода события, изменения должны быть сохранены
    }

    public BotApiMethod<?> editDescription(Message message) {//обработка введенного события
        String description = message.getText();
        long userId = message.getFrom().getId();

        if (description.isEmpty() || description.length() < 4 || description.length() > 200) {//не должно быть пустым, меньше 4 и больше 200 символов
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            sendMessage.setText("Описание должно быть минимум 4 символа, но не более 200");
            return sendMessage;
        }

        Event event = eventCash.getEventMap().get(userId);//получаем данные предыдущего набора
        event.setDescription(description);
        eventCash.saveEventCash(userId, event);//сохраняем в кэш
        return editEvent(message.getChatId(), userId);//ожидается завершение ввода события, изменения должны быть сохранены
    }

    public BotApiMethod<?> editHandler(Message message, long userId) {//реакция на callbackquerry buttonEdit
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));

        Event eventRes;
        try {
            eventRes = enterNumberEvent(message.getText(), userId);//ожидаем введенного eventId и получаем событие из базы
        } catch (NumberFormatException e) {
            sendMessage.setText("Введенная строка не является числом, попробуйте снова!");
            return sendMessage;
        }
        if (eventRes == null) {
            sendMessage.setText("Введенное число отсутствует в списке, попробуйте снова");
            return sendMessage;
        }
        eventCash.saveEventCash(userId, eventRes);//полученное событие сохраняется в кеше
        StringBuilder builder = buildEvent(eventRes);//полученное событие выводим пользователю
        sendMessage.setText(builder.toString());
        sendMessage.setReplyMarkup(menuService.getInlineMessageForEdit());//показываем пользователю меню для редактирования события
        return sendMessage;
    }

    public BotApiMethod<?> enterDateHandler(Message message, long userId) {//Обрабатываем ввод даты
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        Date date;
        try {
            date = parseDate(message.getText());
        } catch (ParseException e) {
            sendMessage.setText("Не удается распознать указанную дату и время, попробуйте еще раз");
            return sendMessage;
        }
        Event event = eventCash.getEventMap().get(userId);//Получаем данные предыдущего набора
        event.setDate(date);
        eventCash.saveEventCash(userId, event);//Сохраняем данные в кеше
        sendMessage.setText("Выберите период повторения: Единоразово(сработает один раз и удалится), " +
                "Ежедневно в указанный час, " +
                "1 раз в месяц в указанную дату, 1 раз в год в указанное число");
        sendMessage.setReplyMarkup(menuService.getInlineMessageButtonsForEnterDate());//показываем меню пользователю для выбора частоты отправки напоминания
        return sendMessage;
    }

    public BotApiMethod<?> enterDescriptionHandler(Message message, long userId) {//Обрабатываем ввод описания
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));
        String description = message.getText();
        if (description.isEmpty() || description.length() < 4 || description.length() > 200) {
            sendMessage.setChatId(String.valueOf(message.getChatId()));
            sendMessage.setText("Описание должно быть минимум 4 символа, но не более 200");
            return sendMessage;
        }
        botStateCash.saveBotState(userId, BotState.ENTERDATE);//Переключаем состояния для ввода даты
        Event event = eventCash.getEventMap().get(userId);//Получаем предыдущий набор событий из кеша
        event.setDescription(description);
        eventCash.saveEventCash(userId, event);//Сохраняем в кеш
        sendMessage.setText("Введите дату предстоящего события в форме DD.MM.YYYY HH:MM, например - 27.09.2022 23:43, либо 27.09.2022");
        return sendMessage;
    }

    private Event enterNumberEvent(String message, long userId) throws NumberFormatException, NullPointerException, EntityActionVetoException {
        List<Event> list;//возвращаем событие из базы данных
        if (userId == admin_id) {
            list = eventDAO.findAllEvent();
        } else {
            list = eventDAO.findByUserId(userId);
        }

        int i = Integer.parseInt(message);

        return list.stream().filter(event -> event.getEventId() == i).findFirst().orElseThrow(null);
    }

    public BotApiMethod<?> removeEventHandler(Message message, long userId) {//удаляем событие из базы данных
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(message.getChatId()));

        Event eventRes;
        try {
            eventRes = enterNumberEvent(message.getText(), userId);
        } catch (NumberFormatException e) {
            sendMessage.setText("Введенная строка не является числом, попробуйте снова!");
            return sendMessage;
        }
        if (eventRes == null) {
            sendMessage.setText("Введенное число отсутствует в списке, попробуйте снова!");
            return sendMessage;
        }

        eventDAO.remove(eventRes);
        botStateCash.saveBotState(userId, BotState.START);//переключаем состояние
        sendMessage.setText("Удаление прошло успешно.");
        return sendMessage;
    }

    private StringBuilder buildEvent(Event event) {//создаем события для показа пользователю
        StringBuilder builder = new StringBuilder();
        long eventId = event.getEventId();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        Date date = event.getDate();
        String dateFormat = simpleDateFormat.format(date);

        String description = event.getDescription();
        EventFreq freq = event.getFreq();
        String freqEvent;
        switch (freq.name()) {
            case ("TIME"):
                freqEvent = "Единоразово";
                break;
            case ("EVERYDAY"):
                freqEvent = "Ежедневно";
                break;
            case ("MONTH"):
                freqEvent = "Один раз в месяц";
                break;
            case ("YEAR"):
                freqEvent = "Один раз в год";
            default:
                throw new IllegalStateException("Unexpected value: " + freq.name());
        }
        builder.append(eventId).append(". ").append(dateFormat).append(": ")
                .append(description).append(": ").append(freqEvent).append("\n");
        return builder;
    }

    public SendMessage editEvent(long chatId, long userId) {//сохраняем событие из кеша(для редактирования)
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        Event event = eventCash.getEventMap().get(userId);//Если что-то пошло не так
        if (event.getEventId() == 0) {
            sendMessage.setText("Не удалось сохранить пользователя, нарушена последовательность действий");
            return sendMessage;
        }
        eventDAO.save(event);
        sendMessage.setText("Изменение сохранено");
        eventCash.saveEventCash(userId, new Event());//Сбросить кеш
        return sendMessage;
    }

    public SendMessage saveEvent(EventFreq freq, long userId, long chatId) {//сохраняем событие из кеша(для операции создания)
        Event event = eventCash.getEventMap().get(userId);
        event.setFreq(freq);
        event.setUser1(userDAO.findByUserId(userId));
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        eventDAO.save(event);
        eventCash.saveEventCash(userId, new Event());
        sendMessage.setText("напоминание успешно сохранено");
        botStateCash.saveBotState(userId, BotState.START);
        return sendMessage;
    }

    private Date parseDate(String s) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        return simpleDateFormat.parse(s);
    }
}
