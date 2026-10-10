package event_app.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import event_app.ApplyEventResult;
import event_app.repository.EventApplicationRepository;
import event_app.repository.EventRepository;
import java.time.Clock;
import java.sql.Timestamp;

@Service 
public class EventApplicationService  {

    private final EventRepository eventRepository;
    private  final EventApplicationRepository eventApplicationRepository;
    private final Clock clock;


    public EventApplicationService(EventRepository eventRepository,EventApplicationRepository eventApplicationRepository,Clock clock) {

        this.eventRepository = eventRepository;
        this.eventApplicationRepository = eventApplicationRepository;
        this.clock = clock;
    }

    @Transactional 
    public ApplyEventResult applyEvent(int eventId,int userId) { 

        boolean event_exists = eventRepository.existsByEventId(eventId);

        if (! event_exists) {
            return ApplyEventResult.EVENT_NOT_FOUND;
        }

        Integer capacity = eventRepository.getCapacityForUpdate(eventId);

        boolean alreadyApplied = eventApplicationRepository.existsByEventIdAndUserId(eventId,userId);

        if (alreadyApplied) {
            return ApplyEventResult.ALREADY_APPLIED;
        }

        Integer count = eventApplicationRepository.apply_count(eventId);

        if (count >= capacity) {
        return ApplyEventResult.FULL;
        }



        boolean checkApplicationDeadline = isRegistrationOpen(eventId);

        if (!checkApplicationDeadline) {
            return ApplyEventResult.CLOSED;
        }

        eventApplicationRepository.add_apply(eventId, userId);

        return ApplyEventResult.SUCCESS;
    }

    public boolean isRegistrationOpen(Integer eventId) {
        Timestamp eventDatetime = eventRepository.findEventDatetime(eventId);

        LocalDateTime deadline =
            eventDatetime.toLocalDateTime()
            .toLocalDate()
            .atStartOfDay();

        LocalDateTime now = LocalDateTime.now(clock);

        return now.isBefore(deadline);
    }

}
