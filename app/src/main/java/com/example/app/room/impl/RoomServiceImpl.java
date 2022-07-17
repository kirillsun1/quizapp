package com.example.app.room.impl;

import com.example.app.quiz.QuizRepository;
import com.example.app.room.Room;
import com.example.app.room.RoomService;
import com.example.app.room.UniqueRoomCodeGenerator;
import com.example.app.room.events.RoomChangedInternalEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final ApplicationEventPublisher eventPublisher;
    private final MutableRoomRepository roomRepository;
    private final UniqueRoomCodeGenerator codeGenerator;
    private final QuizRepository quizRepository;
    private final RoomMapper roomMapper;

    @Override
    public Room createRoom(String moderator) {
        var uniqueCode = codeGenerator.generate();
        var room = MutableRoom.builder()
                .code(uniqueCode)
                .moderator(moderator)
                .build();
        room.getPlayersPoints().put(moderator, 0);
        roomRepository.save(room);
        return roomMapper.toPublic(room);
    }

    @Override
    public boolean joinRoom(String code, String player) {
        Optional<MutableRoom> roomOptional = roomRepository.findByCode(code);
        if (roomOptional.isEmpty()) {
            return false;
        }
        roomOptional.get().getPlayersPoints().putIfAbsent(player, 0);
        eventPublisher.publishEvent(new RoomChangedInternalEvent(code));

        return true;
    }

    @Override
    public boolean assignQuiz(String requester, String code, int quizId) {
        Optional<MutableRoom> roomOptional = roomRepository.findByCode(code)
                .filter(room -> requester.equals(room.getModerator()));

        if (roomOptional.isEmpty() || quizRepository.findById(quizId).isEmpty()) {
            return false;
        }

        roomOptional.get().setQuizId(quizId);
        eventPublisher.publishEvent(new RoomChangedInternalEvent(code));

        return true;
    }

}
