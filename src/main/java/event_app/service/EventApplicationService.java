package event_app.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import event_app.ApplyEventResult;
import event_app.repository.EventApplicationRepository;
import event_app.repository.EventRepository;
import java.time.Clock;
import event_app.Event;

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

        Event event = eventRepository.findById(eventId).orElse(null);
        
        if (event == null) {
            return ApplyEventResult.EVENT_NOT_FOUND;
        }

        boolean alreadyApplied = eventApplicationRepository.existsByEventIdAndUserId(eventId,userId);

        if (alreadyApplied) {
            return ApplyEventResult.ALREADY_APPLIED;
        }

        Integer count = eventApplicationRepository.applyCount(eventId);

        if (count >= event.getCapacity()) {
            return ApplyEventResult.FULL;
        }

        boolean isRegistrationOpen = event.isRegistrationOpen(LocalDateTime.now(clock));

        if (!isRegistrationOpen) {
            return ApplyEventResult.CLOSED;
        }

        eventApplicationRepository.addApply(eventId, userId);

        return ApplyEventResult.SUCCESS;
    }
}
