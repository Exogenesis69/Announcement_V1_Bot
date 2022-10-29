package sukhdmi.telegramBots.schoolEventBot.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sukhdmi.telegramBots.schoolEventBot.entity.EventCashEntity;
import sukhdmi.telegramBots.schoolEventBot.repo.EventCashRepository;

import java.util.List;

@Service
public class EventCashDAO {
    private EventCashRepository eventCashRepository;

    @Autowired
    public void setEventCashRepository(EventCashRepository eventCashRepository) {
        this.eventCashRepository = eventCashRepository;
    }

    public List<EventCashEntity> findAllEventCash() {
        return eventCashRepository.findAll();
    }

    public void save(EventCashEntity eventCashEntity) {
        eventCashRepository.save(eventCashEntity);
    }

    public void delete(long id) {
        eventCashRepository.deleteById(id);
    }
}
