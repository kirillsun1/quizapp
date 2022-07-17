package com.example.app.room.impl;

import com.example.app.quiz.QuizRepository;
import com.example.app.room.OngoingQuizService;
import com.example.app.room.OngoingQuizStatus;
import com.example.app.room.Room;
import com.example.app.room.RoomService;
import com.example.app.room.UniqueRoomCodeGenerator;
import com.example.app.room.events.internal.RoomChangedInternalEvent;
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
        moveToQuestion(room, 0);

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

        boolean sendEvent = false;
        switch (room.getStatus()) {
            case WAITING -> {
                moveToQuestion(room, room.getCurrentQuestion() + 1);
                sendEvent = true;
            }
            case QUESTION_IN_PROGRESS -> {
                finishRound(room);
                sendEvent = true;
            }
        }

        if (sendEvent) {
            eventPublisher.publishEvent(new RoomChangedInternalEvent(code));
        }
        return true;
    }


    private void moveToQuestion(MutableRoom room, int question) {
        room.getVotesByQuestions().putIfAbsent(question, new HashMap<>());
        room.setCurrentQuestion(question);
        room.setStatus(OngoingQuizStatus.QUESTION_IN_PROGRESS);
    }

    private void finishRound(MutableRoom room) {
        var quiz = quizRepository.findById(room.getQuizId()).orElseThrow();

        if (room.getCurrentQuestion() == quiz.questions().size() - 1) {
            // last question
            room.setStatus(OngoingQuizStatus.DONE);
        } else {
            room.setStatus(OngoingQuizStatus.WAITING);
        }

        room.getPlayersPoints().keySet()
                .forEach(player -> {
                    Integer playerAnswer = room.getVotesByQuestions().get(room.getCurrentQuestion()).get(player);
                    if (playerAnswer != null) {

                        var question = quiz.questions().get(room.getCurrentQuestion());
                        var answers = question.answers();

                        if (playerAnswer >= 0 && playerAnswer < answers.size()) {
                            var answer = answers.get(playerAnswer);
                            room.getPlayersPoints()
                                    .compute(player, (name, oldPoints) -> Objects.requireNonNull(oldPoints) + answer.points());
                        }
                    }
                });
    }

    private Optional<MutableRoom> findRoom(String requester, String code) {
        return roomRepository.findByCode(code)
                .filter(room -> requester.equals(room.getModerator()));
    }

}
