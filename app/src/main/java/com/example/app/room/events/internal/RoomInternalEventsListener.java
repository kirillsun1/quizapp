package com.example.app.room.events.internal;

import com.example.app.room.events.RoomEvent;
import com.example.app.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomInternalEventsListener {

    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onRoomChanged(RoomChangedInternalEvent event) {
        roomRepository.findByCode(event.code())
                .ifPresent(room -> {
                    messagingTemplate.convertAndSend("/topic/rooms." + event.code(), new RoomEvent(room));
                });
    }

}
