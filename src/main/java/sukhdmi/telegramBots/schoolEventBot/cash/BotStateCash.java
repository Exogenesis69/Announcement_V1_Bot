package sukhdmi.telegramBots.schoolEventBot.cash;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import sukhdmi.telegramBots.schoolEventBot.model.BotState;

import java.util.HashMap;
import java.util.Map;

@Service
@Setter
@Getter

public class BotStateCash {//Used to save state bot
    private final Map<Long, BotState> botStateMap = new HashMap<>();

    public void saveBotState(long userId, BotState botState){
        botStateMap.put(userId, botState);
    }
}
