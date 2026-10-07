package event_app.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public String applyEvent(int eventId,int userId) { 

        boolean event_exists = eventRepository.existsByEventId(eventId);

        if (! event_exists) {
            return "イベントが見つかりません";
        }

        Integer capacity = eventRepository.getCapacityForUpdate(eventId);

        boolean alreadyApplied = eventApplicationRepository.existsByEventIdAndUserId(eventId,userId);

        if (alreadyApplied) {
            return "既に申し込み済みです";
        }

        Integer count = eventApplicationRepository.apply_count(eventId);

        if (count >= capacity) {
        return "定員オーバーです";
        }

        boolean checkApplicationDeadline = eventRepository.isRegistrationOpen(eventId);

        if (!checkApplicationDeadline) {
            return "申し込み期限を過ぎています";
        }
        
        eventApplicationRepository.add_apply(eventId, userId);

        return null;
    }

}
