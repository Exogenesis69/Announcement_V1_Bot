package sukhdmi.telegramBots.schoolEventBot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import sukhdmi.telegramBots.schoolEventBot.entity.EventCashEntity;

public interface EventCashRepository extends JpaRepository<EventCashEntity, Long> {
    EventCashEntity findById(long id);
}
