package sukhdmi.telegramBots.schoolEventBot.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sukhdmi.telegramBots.schoolEventBot.entity.Event;
import sukhdmi.telegramBots.schoolEventBot.entity.User1;
import sukhdmi.telegramBots.schoolEventBot.repo.EventRepository;
import sukhdmi.telegramBots.schoolEventBot.repo.UserRepository;

import java.util.List;
@Service
public class EventDAO {
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Autowired
    public EventDAO(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository =userRepository;
        this.eventRepository = eventRepository;
    }

    public List<Event> findByUserId(long userId) {
        User1 user1 = userRepository.findById(userId);
        return user1.getEvents();
    }
    public List<Event> findAllEvent() {
        return eventRepository.findAll();
    }

    public Event findByEventId(long eventId) {
        return eventRepository.findByEventId(eventId);
    }

    public void remove(Event event) {
        eventRepository.delete(event);
    }

    public void save(Event event) {
        eventRepository.save(event);
    }
}
