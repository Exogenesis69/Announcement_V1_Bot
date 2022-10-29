package sukhdmi.telegramBots.schoolEventBot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import sukhdmi.telegramBots.schoolEventBot.dao.EventCashDAO;
import sukhdmi.telegramBots.schoolEventBot.dao.EventDAO;
import sukhdmi.telegramBots.schoolEventBot.entity.Event;
import sukhdmi.telegramBots.schoolEventBot.entity.EventCashEntity;
import sukhdmi.telegramBots.schoolEventBot.model.EventFreq;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

@EnableScheduling//включаем работу по расписанию в Spring
@Service

public class EventService {
    private final EventDAO eventDAO;
    private final EventCashDAO eventCashDAO;

    @Autowired
    public EventService(EventDAO eventDAO, EventCashDAO eventCashDAO) {
        this.eventDAO = eventDAO;
        this.eventCashDAO = eventCashDAO;
    }

    //настраиваем запуск метода в 0:00 каждый день
    @Scheduled(cron = "0 0 0 * * *") // @Scheduled(fixedRateString = "${eventservice.period}")
    private void eventService() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());//устанавливаем серверное время
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);


        List<Event> list = eventDAO.findAllEvent().stream().filter(event -> {//получаем список напоминаний на сегодня
            if (event.getUser1().isOn()) {
                EventFreq eventFreq = event.getFreq();

                Calendar calendarUserTime = getDateUserTimeZone(event);//назначаем пользовательское время события
                int day1 = calendarUserTime.get(Calendar.DAY_OF_MONTH);
                int month1 = calendarUserTime.get(Calendar.MONTH);
                int year1 = calendarUserTime.get(Calendar.YEAR);
                switch (eventFreq.name()) {
                    case "TIME"://если один раз-удалить событие
                        if (day == day1 && month == month1 && year == year1) {
                            eventDAO.remove(event);
                            return true;
                        } else
                            return false;
                    case "EVERYDAY":
                        return true;
                    case "MONTH":
                        return day == day1;
                    case "YEAR":
                        return day == day1 && month == month1;
                    default:
                        return false;
                }
            } else return false;
        }).collect(Collectors.toList());

        for (Event event : list) {//устанавливаем время пользовательского события
            Calendar calendarUserTime = getDateUserTimeZone(event);
            int hour1 = calendarUserTime.get(Calendar.HOUR_OF_DAY);
            calendarUserTime.set(year,month,day,hour1,0,0);

            String description = event.getDescription();
            String userId = String.valueOf(event.getUser1().getId());

            EventCashEntity eventCashEntity = EventCashEntity.eventTo(calendarUserTime.getTime(), event.getDescription(), event.getUser1().getId());
            eventCashDAO.save(eventCashEntity);

            SendEvent sendEvent = new SendEvent();
            sendEvent.setSendMessage(new SendMessage(userId,description));
            sendEvent.setEventCashId(eventCashEntity.getId());

            new Timer().schedule(new SimpleTask(sendEvent), calendarUserTime.getTime());
        }
    }


    private Calendar getDateUserTimeZone(Event event) {
        Calendar calendarUserTime = Calendar.getInstance();
        calendarUserTime.setTime(event.getDate());
        int timeZone = event.getUser1().getTimeZone();

        calendarUserTime.add(Calendar.HOUR_OF_DAY, -timeZone);//устанавливаем корректное время события с учетом часового пояса пользователя
        return calendarUserTime;
        }
    }
