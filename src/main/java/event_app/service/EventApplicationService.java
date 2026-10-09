package event_app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import event_app.ApplyEventResult;
import event_app.repository.EventApplicationRepository;
import event_app.repository.EventRepository;

@Service 
public class EventApplicationService  {

    private final EventRepository eventRepository;
    private  final EventApplicationRepository eventApplicationRepository;

    public EventApplicationService(EventRepository eventRepository,EventApplicationRepository eventApplicationRepository) {

        this.eventRepository = eventRepository;
        this.eventApplicationRepository = eventApplicationRepository;
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

        boolean checkApplicationDeadline = eventRepository.isRegistrationOpen(eventId);

        if (!checkApplicationDeadline) {
            return ApplyEventResult.CLOSED;
        }
        
        eventApplicationRepository.add_apply(eventId, userId);

        return ApplyEventResult.SUCCESS;
    }

}
