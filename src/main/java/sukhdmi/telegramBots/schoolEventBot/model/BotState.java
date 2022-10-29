package sukhdmi.telegramBots.schoolEventBot.model;

public enum BotState {
    ENTERDESCRIPTION,//бот будет ждать ввода описания.
    START,
    MYEVENTS,// бот показывает пользователю список событий.
    ENTERNUMBEREVENT,//бот будет ждать ввода номера события.
    ENTERDATE,//бот будет ждать ввода даты
    CREATE,//событие создания запуска бота
    ENTERNUMBERFOREDIT,//бот будет ждать ввода номера события
    EDITDATE, // бот будет ждать ввода даты
    EDITDESCRIPTION,//бот будет ждать ввода описания
    EDITFREQ,//бот будет ждать callbackquery
    ALLUSERS,// показать всех пользователей
    ALLEVENTS,//показать все события
    ENTERNUMBERUSER,//бот будет ждать ввода номера пользователя.
    ENTERTIME,//бот будет ждать ввода часа.
    ONEVENT//переключение состояния
}
