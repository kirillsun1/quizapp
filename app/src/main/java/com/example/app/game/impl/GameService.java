package com.example.app.game.impl;

import com.example.app.game.impl.events.RoomChangedEvent;
import com.example.app.ongoingquiz.OngoingQuizService;
import com.example.app.ongoingquiz.OngoingQuizStatus;
import com.example.app.ongoingquiz.exceptions.ModeratorIsNotPlayerException;
import com.example.app.ongoingquiz.exceptions.PlayerIsNotModeratorException;
import com.example.app.ongoingquiz.exceptions.QuizNotAssignedException;
import com.example.app.quiz.QuizRepository;
import com.example.app.room.Room;
import com.example.app.room.RoomService;
import com.example.app.room.exceptions.QuizNotFoundException;
import com.example.app.room.exceptions.RoomNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Objects;

@Service
@RequiredArgsConstructor
class GameService implements RoomService, OngoingQuizService {

    private final ApplicationEventPublisher eventPublisher;
    private final LockingGameRepository gameRepository;
    private final UniqueRoomCodeGenerator codeGenerator;
    private final QuizRepository quizRepository;
    private final RoomMapper roomMapper;

    @Override
    public Room createRoom(String moderator) {
        Assert.notNull(moderator, "Moderator cannot be null.");

        var uniqueCode = codeGenerator.generate();
        var game = Game.builder()
                .roomCode(uniqueCode)
                .moderator(moderator)
                .build();
        gameRepository.register(game);
        return roomMapper.toPublic(game);
    }

    @Override
    public Room joinRoom(String code, String player) {
        Assert.notNull(code, "Room code cannot be null.");
        Assert.notNull(player, "Player cannot be null.");

        var game = gameRepository.findAndLock(code).orElseThrow(RoomNotFoundException::new);
        game.getPlayersPoints().putIfAbsent(player, 0);

        gameRepository.saveAndRelease(game);
        notifyAboutRoomChange(code);

        return roomMapper.toPublic(game);
    }

    @Override
    public void assignQuiz(String requester, String code, int quizId) {
        Assert.notNull(code, "Room code cannot be null.");
        Assert.notNull(requester, "Moderator cannot be null.");

        Game game = findModeratorsGame(requester, code);
        if (quizRepository.findById(quizId).isEmpty()) {
            gameRepository.release(game);
            throw new QuizNotFoundException();
        }
        game.setQuizId(quizId);

        gameRepository.saveAndRelease(game);
        notifyAboutRoomChange(code);
    }

    @Override
    public void vote(String requester, String roomCode, int choice) {
        Assert.notNull(roomCode, "Room code cannot be null.");
        Assert.notNull(requester, "Moderator cannot be null.");

        Game game = gameRepository.findAndLock(roomCode).orElseThrow(RoomNotFoundException::new);
        if (game.getModerator().equals(requester)) {
            gameRepository.release(game);
            throw new ModeratorIsNotPlayerException();
        }

        game.getVotesByQuestions().get(game.getCurrentQuestion()).put(requester, choice);

        gameRepository.saveAndRelease(game);
    }

    @Override
    public void moveOn(String requester, String code) {
        Assert.notNull(code, "Room code cannot be null.");
        Assert.notNull(requester, "Moderator cannot be null.");

        Game game = findModeratorsGame(requester, code);

        boolean sendEvent = false;
        switch (game.getStatus()) {
            case NOT_STARTED -> {
                if (game.getQuizId() == null) {
                    gameRepository.release(game);
                    throw new QuizNotAssignedException();
                }
                moveToQuestion(game, 0);
                sendEvent = true;
            }
            case WAITING -> {
                moveToQuestion(game, game.getCurrentQuestion() + 1);
                sendEvent = true;
            }
            case QUESTION_IN_PROGRESS -> {
                finishRound(game);
                sendEvent = true;
            }
        }

        gameRepository.saveAndRelease(game);
        if (sendEvent) {
            notifyAboutRoomChange(code);
        }
    }


    private void moveToQuestion(Game room, int question) {
        room.getVotesByQuestions().putIfAbsent(question, new HashMap<>());
        room.setCurrentQuestion(question);
        room.setStatus(OngoingQuizStatus.QUESTION_IN_PROGRESS);
    }

    private void finishRound(Game room) {
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

    private Game findModeratorsGame(String requester, String code) {
        Game game = gameRepository.findAndLock(code).orElseThrow(RoomNotFoundException::new);
        if (!requester.equals(game.getModerator())) {
            gameRepository.release(game);
            throw new PlayerIsNotModeratorException();
        }
        return game;
    }

    private void notifyAboutRoomChange(String code) {
        eventPublisher.publishEvent(new RoomChangedEvent(code));
    }

}
