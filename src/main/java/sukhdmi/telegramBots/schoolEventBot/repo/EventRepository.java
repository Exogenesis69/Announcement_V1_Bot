package sukhdmi.telegramBots.schoolEventBot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import sukhdmi.telegramBots.schoolEventBot.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long> {
    Event findByEventId(long id);
}
