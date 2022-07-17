package com.example.app.room;

import com.example.app.room.events.internal.RoomChangedInternalEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final ApplicationEventPublisher eventPublisher;
    private final RoomRepository roomRepository;
    private final UniqueRoomCodeGenerator codeGenerator;

    public Room createRoom(String moderator) {
        var uniqueCode = codeGenerator.generate();
        var room = Room.builder().code(uniqueCode).moderator(moderator).players(Set.of(moderator)).build();
        roomRepository.save(room);
        return room;
    }

    public Optional<Room> joinRoom(String code, String player) {
        Optional<Room> roomOptional = roomRepository.findByCode(code);
        if (roomOptional.isEmpty()) {
            return Optional.empty();
        }
        var oldRoom = roomOptional.get();
        var players = new HashSet<>(oldRoom.players());
        players.add(player);
        var modifiedRoom = Room.builder()
                .code(code)
                .moderator(oldRoom.moderator())
                .ongoingQuiz(oldRoom.ongoingQuiz())
                .players(Set.copyOf(players))
                .build();
        roomRepository.save(modifiedRoom);
        eventPublisher.publishEvent(new RoomChangedInternalEvent(code));
        return Optional.of(modifiedRoom);
    }
}
