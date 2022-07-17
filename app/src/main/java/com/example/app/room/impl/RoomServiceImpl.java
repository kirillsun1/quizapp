package com.example.app.room.impl;

import com.example.app.quiz.QuizRepository;
import com.example.app.room.OngoingQuizService;
import com.example.app.room.OngoingQuizStatus;
import com.example.app.room.Room;
import com.example.app.room.RoomService;
import com.example.app.room.UniqueRoomCodeGenerator;
import com.example.app.room.events.RoomChangedInternalEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService, OngoingQuizService {

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
        Optional<MutableRoom> roomOptional = findRoom(requester, code);
        if (roomOptional.isEmpty() || quizRepository.findById(quizId).isEmpty()) {
            return false;
        }

        roomOptional.get().setQuizId(quizId);
        eventPublisher.publishEvent(new RoomChangedInternalEvent(code));
        return true;
    }

    @Override
    public boolean start(String requester, String code) {
        Optional<MutableRoom> roomOptional = findRoom(requester, code);
        if (roomOptional.isEmpty()) {
            return false;
        }

        MutableRoom room = roomOptional.get();
        room.setCurrentQuestion(0);
        room.setStatus(OngoingQuizStatus.QUESTION_IN_PROGRESS);
        room.getVotesByQuestions().put(0, new HashMap<>());

        eventPublisher.publishEvent(new RoomChangedInternalEvent(code));
        return true;
    }

    @Override
    public boolean vote(String requester, String roomCode, int choice) {
        Optional<MutableRoom> roomOptional = roomRepository.findByCode(roomCode);
        if (roomOptional.isEmpty()) {
            return false;
        }
        var room = roomOptional.get();
        if (room.getModerator().equals(requester)) {
            return false;
        }

        room.getVotesByQuestions().get(room.getCurrentQuestion()).put(requester, choice);
        return true;
    }

    @Override
    public boolean moveOn(String requester, String code) {
        Optional<MutableRoom> roomOptional = findRoom(requester, code);
        if (roomOptional.isEmpty()) {
            return false;
        }

        MutableRoom room = roomOptional.get();
        room.setStatus(OngoingQuizStatus.WAITING);
        room.getPlayersPoints().keySet()
                .forEach(player -> {
                    Integer playerAnswer = room.getVotesByQuestions().get(room.getCurrentQuestion()).get(player);
                    if (playerAnswer != null) {
                        var quiz = quizRepository.findById(room.getQuizId()).orElseThrow();
                        var question = quiz.questions().get(room.getCurrentQuestion());

                        if (question.correctAnswersIndexes().contains(playerAnswer)) {
                            room.getPlayersPoints()
                                    .compute(player, (name, oldPoints) -> Objects.requireNonNull(oldPoints) + question.correctPoints());
                        }
                    }
                });

        eventPublisher.publishEvent(new RoomChangedInternalEvent(code));
        return true;
    }

    private Optional<MutableRoom> findRoom(String requester, String code) {
        return roomRepository.findByCode(code)
                .filter(room -> requester.equals(room.getModerator()));
    }

}
