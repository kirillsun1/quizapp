package com.example.app.room.impl;

import com.example.app.quiz.QuizRepository;
import com.example.app.room.OngoingQuizService;
import com.example.app.room.OngoingQuizStatus;
import com.example.app.room.Room;
import com.example.app.room.RoomService;
import com.example.app.room.events.internal.RoomChangedInternalEvent;
import com.example.app.room.exceptions.ModeratorIsNotPlayerException;
import com.example.app.room.exceptions.PlayerIsNotModeratorException;
import com.example.app.room.exceptions.QuizNotFoundException;
import com.example.app.room.exceptions.RoomNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GameService implements RoomService, OngoingQuizService {

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
    public void joinRoom(String code, String player) {
        var room = roomRepository.findByCode(code).orElseThrow(RoomNotFoundException::new);
        room.getPlayersPoints().putIfAbsent(player, 0);
        eventPublisher.publishEvent(new RoomChangedInternalEvent(code));
    }

    @Override
    public void assignQuiz(String requester, String code, int quizId) {
        MutableRoom room = findRoomForModerator(requester, code);
        if (quizRepository.findById(quizId).isEmpty()) {
            throw new QuizNotFoundException();
        }
        room.setQuizId(quizId);
        eventPublisher.publishEvent(new RoomChangedInternalEvent(code));
    }

    @Override
    public void start(String requester, String code) {
        MutableRoom room = findRoomForModerator(requester, code);
        moveToQuestion(room, 0);
        eventPublisher.publishEvent(new RoomChangedInternalEvent(code));
    }

    @Override
    public void vote(String requester, String roomCode, int choice) {
        MutableRoom room = roomRepository.findByCode(roomCode).orElseThrow(RoomNotFoundException::new);
        if (room.getModerator().equals(requester)) {
            throw new ModeratorIsNotPlayerException();
        }
        room.getVotesByQuestions().get(room.getCurrentQuestion()).put(requester, choice);
    }

    @Override
    public void moveOn(String requester, String code) {
        MutableRoom room = findRoomForModerator(requester, code);

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
    }


    private void moveToQuestion(MutableRoom room, int question) {
        room.getVotesByQuestions().putIfAbsent(question, new HashMap<>());
        room.setCurrentQuestion(question);
        room.setStatus(OngoingQuizStatus.QUESTION_IN_PROGRESS);
    }

    private void finishRound(MutableRoom room) {
        var quiz = quizRepository.findById(room.getQuizId()).orElseThrow(QuizNotFoundException::new);

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

    private MutableRoom findRoomForModerator(String requester, String code) {
        MutableRoom mutableRoom = roomRepository.findByCode(code).orElseThrow(RoomNotFoundException::new);
        if (!requester.equals(mutableRoom.getModerator())) {
            throw new PlayerIsNotModeratorException();
        }
        return mutableRoom;
    }

}
