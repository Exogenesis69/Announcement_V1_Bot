package sukhdmi.telegramBots.schoolEventBot.cash;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import sukhdmi.telegramBots.schoolEventBot.entity.Event;

import java.util.HashMap;
import java.util.Map;

@Service
@Setter
@Getter
public class EventCash {//Используется для сохранения введенных данных о событии за сеанс

    private final Map<Long, Event> eventMap = new HashMap<>();

    public void saveEventCash(long userId, Event event) {
        eventMap.put(userId,event);
    }
}
