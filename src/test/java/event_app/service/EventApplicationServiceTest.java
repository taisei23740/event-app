
package event_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import event_app.ApplyEventResult;
import event_app.Event;
import event_app.repository.EventApplicationRepository;
import event_app.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
class EventApplicationServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventApplicationRepository eventApplicationRepository;

    private EventApplicationService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
            Instant.parse("2026-10-12T03:00:00Z"),
            ZoneOffset.UTC
        );

        service = new EventApplicationService(
            eventRepository,
            eventApplicationRepository,
            clock
        );
    }

    @Test
    void 申し込み条件を満たす場合は正常に申し込める() {
        int eventId = 1;
        int userId = 100;

        Event event = new Event(
            eventId,
            10,
            Timestamp.valueOf(
                LocalDateTime.of(2026, 11, 1, 10, 0)
            )
        );

        when(eventRepository.findById(eventId))
            .thenReturn(Optional.of(event));

        when(eventApplicationRepository
            .existsByEventIdAndUserId(eventId, userId))
            .thenReturn(false);

        when(eventApplicationRepository.applyCount(eventId))
            .thenReturn(0);

        ApplyEventResult result = service.applyEvent(eventId, userId);

        assertEquals(ApplyEventResult.SUCCESS, result);

        verify(eventApplicationRepository).addApply(eventId, userId);
    }

    @Test
    void 存在しないイベントには申し込めない() {
        int eventId = 999;
        int userId = 100;

        when(eventRepository.findById(eventId))
            .thenReturn(Optional.empty());

        ApplyEventResult result = service.applyEvent(eventId, userId);

        assertEquals(ApplyEventResult.EVENT_NOT_FOUND, result);

        verify(eventApplicationRepository, never()).addApply(eventId, userId);
    }

    @Test 
    void すでに申し込んでいるイベントには再度申し込めない() {
        int eventId = 1;
        int userId = 100;

        Event event = new Event (
            eventId,
            10,
            Timestamp.valueOf(
            LocalDateTime.of(2026, 11, 1, 10, 0)
            )
        );

        when(eventRepository.findById(eventId))
            .thenReturn(Optional.of(event));

        when(eventApplicationRepository.existsByEventIdAndUserId(eventId,userId))
            .thenReturn(true);

        ApplyEventResult result = service.applyEvent(eventId, userId);

        assertEquals(ApplyEventResult.ALREADY_APPLIED, result);

        verify(eventApplicationRepository, never())
        .addApply(eventId, userId);
    }

    @Test 
    void 定員に達しているイベントには申し込めない() {
        int eventId = 1;
        int userId = 100;

        Event event = new Event (
            eventId,
            1,
            Timestamp.valueOf(
            LocalDateTime.of(2026, 11, 1, 10, 0)
            )
        );

        when(eventRepository.findById(eventId))
            .thenReturn(Optional.of(event));
        
        when(eventApplicationRepository.existsByEventIdAndUserId(eventId, userId))
            .thenReturn(false);

        when(eventApplicationRepository.applyCount(eventId))
            .thenReturn(1);

        ApplyEventResult result = service.applyEvent(eventId, userId);

        assertEquals(ApplyEventResult.FULL, result);

        verify(eventApplicationRepository, never())
        .addApply(eventId, userId);
    }

     @Test 
    void 定員に達していないイベントには申し込める() {
        int eventId = 1;
        int userId = 100;

        Event event = new Event (
            eventId,
            1,
            Timestamp.valueOf(
            LocalDateTime.of(2026, 11, 1, 10, 0)
            )
        );

        when(eventRepository.findById(eventId))
            .thenReturn(Optional.of(event));
        
        when(eventApplicationRepository.existsByEventIdAndUserId(eventId, userId))
            .thenReturn(false);

        when(eventApplicationRepository.applyCount(eventId))
            .thenReturn(0);

        ApplyEventResult result = service.applyEvent(eventId, userId);

        assertEquals(ApplyEventResult.SUCCESS, result);

        verify(eventApplicationRepository).addApply(eventId, userId);
    }

    @Test 
    void 開催日前日の23時59分なら申し込める() {
        int eventId = 1;
        int userId = 100;

        Event event = new Event (
            eventId,
            1,
            Timestamp.valueOf(
            LocalDateTime.of(2026, 11, 1, 10, 0)
            )
        );

        Clock fixedClock = Clock.fixed(
            Instant.parse("2026-10-31T14:59:00Z"),
            ZoneId.of("Asia/Tokyo")
        );

        service = new EventApplicationService(
            eventRepository, 
            eventApplicationRepository, 
            fixedClock);

        when(eventRepository.findById(eventId))
            .thenReturn(Optional.of(event));
        
        when(eventApplicationRepository.existsByEventIdAndUserId(eventId, userId))
            .thenReturn(false);

        when(eventApplicationRepository.applyCount(eventId))
            .thenReturn(0);

        ApplyEventResult result = service.applyEvent(eventId, userId);

        assertEquals(ApplyEventResult.SUCCESS, result);

        verify(eventApplicationRepository).addApply(eventId, userId);
    }

        @Test 
    void 開催日の0時0分は申し込めない() {
        int eventId = 1;
        int userId = 100;

        Event event = new Event (
            eventId,
            1,
            Timestamp.valueOf(
            LocalDateTime.of(2026, 11, 1, 10, 0)
            )
        );

        Clock fixedClock = Clock.fixed(
            Instant.parse("2026-10-31T15:00:00Z"),
            ZoneId.of("Asia/Tokyo")
        );

        service = new EventApplicationService(
            eventRepository, 
            eventApplicationRepository, 
            fixedClock);

        when(eventRepository.findById(eventId))
            .thenReturn(Optional.of(event));
        
        when(eventApplicationRepository.existsByEventIdAndUserId(eventId, userId))
            .thenReturn(false);

        when(eventApplicationRepository.applyCount(eventId))
            .thenReturn(0);

        ApplyEventResult result = service.applyEvent(eventId, userId);

        assertEquals(ApplyEventResult.CLOSED, result);

        verify(eventApplicationRepository, never())
            .addApply(eventId, userId);
    }

}