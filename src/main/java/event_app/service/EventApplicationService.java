package event_app.service;

import org.springframework.stereotype.Service;

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

    public String checkEvent(int eventId) {
        boolean event_exists = eventRepository.existsByEventId(eventId);

        if (! event_exists) {
            return "イベントが見つかりません";
        }

        return null;
    }

    public String alreadyApplied(int eventId,int userId) {
        boolean alreadyApplied = eventApplicationRepository.existsByEventIdAndUserId(eventId,userId);

        if (alreadyApplied) {
            return "既に申し込み済みです";
        }

        return null;
    }

    public String capaOver(int eventId) {
        boolean capaOver = eventApplicationRepository.isFull(eventId);

        if (capaOver) {
            return "定員オーバーです";
        }

        return null;
    }

    public String checkApplicationDeadline(int eventId) {
        boolean checkApplicationDeadline = eventRepository.isRegistrationOpen(eventId);

        if (!checkApplicationDeadline) {
            return "申し込み期限を過ぎています";
        }

        return null;
    }
    
    public void apply(int eventId, int userId) {
        eventApplicationRepository.add_apply(eventId, userId);
    }

}
