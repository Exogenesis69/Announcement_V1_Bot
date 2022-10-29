package sukhdmi.telegramBots.schoolEventBot.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sukhdmi.telegramBots.schoolEventBot.entity.User1;
import sukhdmi.telegramBots.schoolEventBot.repo.UserRepository;

import java.util.List;

@Service
public class UserDAO {
    private final UserRepository userRepository;

    @Autowired
    public UserDAO(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User1 findByUserId(long id) {
        return userRepository.findById(id);
    }

    public List<User1> findAllUsers() {
        return userRepository.findAll();
    }

    public void removeUser(User1 user1){
        userRepository.delete(user1);
    }

    public void save(User1 user1){
        userRepository.save(user1);
    }

    public boolean isExist(long id){
        User1 user1 = findByUserId(id);
        return user1 != null;
    }
}
