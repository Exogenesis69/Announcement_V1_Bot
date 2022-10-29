package sukhdmi.telegramBots.schoolEventBot.service;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import sukhdmi.telegramBots.schoolEventBot.dao.UserDAO;
import sukhdmi.telegramBots.schoolEventBot.entity.User1;

import java.util.ArrayList;
import java.util.List;

@Service
@Getter
@Setter
public class MenuService {

    private UserDAO userDAO;
    @Value("${telegrambot.adminId}")
    private int admin_id;

    public MenuService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public SendMessage getMainMenuMessage(final long chatId, final String textMessage, final long userId) {
        final ReplyKeyboardMarkup replyKeyboardMarkup = getMainMenuKeyboard(userId);

        return  createMessageWithKeyboard(chatId, textMessage, replyKeyboardMarkup);
    }

    //Main menu
    private ReplyKeyboardMarkup getMainMenuKeyboard(long userId) {

        final ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(false);
        User1 user1 = userDAO.findByUserId(userId);
        String text = user1.isOn() ? "Отключить напоминания" : "Включить напоминания";

        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();

        row1.add(new KeyboardButton("Создать напоминание"));
        row2.add(new KeyboardButton("Мои напоминания"));
        row3.add(new KeyboardButton(text));
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        if (userId == admin_id) {

            row4.add(new KeyboardButton("All users"));
            row4.add(new KeyboardButton("All events"));
            keyboard.add(row4);
        }
        replyKeyboardMarkup.setKeyboard(keyboard);
        return replyKeyboardMarkup;
    }

    private SendMessage createMessageWithKeyboard(final long chatId, String textMessage,
                                                 final ReplyKeyboardMarkup replyKeyboardMarkup) {
        final SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textMessage);
        if (replyKeyboardMarkup != null) {
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        }
        return sendMessage;
    }

    //set callBackQuery keyboard for list event
    public InlineKeyboardMarkup getInlineMessageButtons(){

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton buttonDel = new InlineKeyboardButton();
        buttonDel.setText("Удалить");

        InlineKeyboardButton buttonEdit = new InlineKeyboardButton();
        buttonEdit.setText("Редактировать");

        InlineKeyboardButton buttonHour = new InlineKeyboardButton();
        buttonHour.setText("Изменить часовой пояс");

        buttonDel.setCallbackData("buttonDel");
        buttonEdit.setCallbackData("buttonEdit");
        buttonHour.setCallbackData("buttonHour");

        List<InlineKeyboardButton>keyboardButtonsRow1 = new ArrayList<>();
        List<InlineKeyboardButton>keyboardButtonsRow2 = new ArrayList<>();

        keyboardButtonsRow1.add(buttonDel);
        keyboardButtonsRow1.add(buttonEdit);
        keyboardButtonsRow2.add(buttonHour);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);
        rowList.add(keyboardButtonsRow2);

        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    //set callBackQuery keyboard for chang freq
    public InlineKeyboardMarkup getInlineMessageButtonsForEnterDate() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton buttonOneTime = new InlineKeyboardButton();
        buttonOneTime.setText("Единоразово");
        InlineKeyboardButton buttonEveryDay = new InlineKeyboardButton();
        buttonEveryDay.setText("Ежедневно");
        InlineKeyboardButton buttonOneTimeMonth = new InlineKeyboardButton();
        buttonOneTimeMonth.setText("Раз в месяц");
        InlineKeyboardButton buttonOneTimeYear = new InlineKeyboardButton();
        buttonOneTimeYear.setText("раз в год");

        List<InlineKeyboardButton> keyboardButtonsRow1 = new ArrayList<>();
        keyboardButtonsRow1.add(buttonOneTime);
        keyboardButtonsRow1.add(buttonEveryDay);
        keyboardButtonsRow1.add(buttonOneTimeMonth);
        keyboardButtonsRow1.add(buttonOneTimeYear);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonsRow1);

        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    //set callBackQuery keyboard for push edit
    public ReplyKeyboard getInlineMessageForEdit() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton buttonDate = new InlineKeyboardButton();
        buttonDate.setText("Изменить дату");
        InlineKeyboardButton buttonDescription = new InlineKeyboardButton();
        buttonDescription.setText("Изменить описание");
        InlineKeyboardButton buttonFreq = new InlineKeyboardButton();
        buttonFreq.setText("Изменить интервал");

        buttonDate.setCallbackData("buttonDate");
        buttonDescription.setCallbackData("buttonDescription");
        buttonFreq.setCallbackData("buttonFreq");

        List<InlineKeyboardButton> keyboardButtonRow1 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonRow2 = new ArrayList<>();
        List<InlineKeyboardButton> keyboardButtonRow3 = new ArrayList<>();

        keyboardButtonRow1.add(buttonDate);
        keyboardButtonRow2.add(buttonDescription);
        keyboardButtonRow3.add(buttonFreq);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonRow1);
        rowList.add(keyboardButtonRow2);
        rowList.add(keyboardButtonRow3);

        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }

    //set callBackQuery keyboard for users list edit
    public ReplyKeyboard getInlineMessageButtonsAllUser() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        InlineKeyboardButton buttonDelUser = new InlineKeyboardButton();
        buttonDelUser.setText("Del user");

        buttonDelUser.setCallbackData("buttonDelUser");

        List<InlineKeyboardButton> keyboardButtonRow1 = new ArrayList<>();
        keyboardButtonRow1.add(buttonDelUser);

        List<List<InlineKeyboardButton>> rowList = new ArrayList<>();
        rowList.add(keyboardButtonRow1);

        inlineKeyboardMarkup.setKeyboard(rowList);

        return inlineKeyboardMarkup;
    }
}
