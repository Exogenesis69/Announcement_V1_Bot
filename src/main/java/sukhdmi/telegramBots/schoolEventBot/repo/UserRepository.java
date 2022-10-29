package sukhdmi.telegramBots.schoolEventBot.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import sukhdmi.telegramBots.schoolEventBot.entity.User1;

public interface UserRepository extends JpaRepository<User1, Long> {

    User1 findById(long id);
}
