package com.example.app.game.impl.events;

import com.example.app.room.RoomRepository;
import com.example.app.room.events.RoomEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomEventsListener {

    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @EventListener
    public void onRoomChanged(RoomChangedEvent event) {
        roomRepository.findByCode(event.code())
                .ifPresent(room -> messagingTemplate.convertAndSend("/topic/rooms." + event.code(), new RoomEvent(room)));
    }

}
