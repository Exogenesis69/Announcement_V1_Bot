package sukhdmi.telegramBots.schoolEventBot.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User1 {

    @Id
    @Column(name = "id")
    private long id;
    @Column(name = "name")
    private String name;

    @Column(name = "time_zone", columnDefinition = "default 0")
    private int timeZone;

    @OneToMany(mappedBy = "user1")
    private List<Event> events;

    @Column(name = "on_off")
    private boolean on;

    public User1() {
    }
}