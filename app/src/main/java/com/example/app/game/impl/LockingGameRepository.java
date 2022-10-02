package com.example.app.game.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
@Repository
class LockingGameRepository {

    private final Map<String, Game> games = new ConcurrentHashMap<>();
    private final Map<String, Lock> locks = new ConcurrentHashMap<>();

    public void register(Game game) {
        if (games.containsKey(game.getRoomCode())) {
            throw new IllegalStateException("Room has already been registered");
        }
        games.put(game.getRoomCode(), createCopy(game));
        locks.put(game.getRoomCode(), new ReentrantLock());
    }

    public Optional<Game> findAndLock(String code) {
        Optional<Game> gameOptional = find(code);
        if (gameOptional.isPresent()) {
            Game game = gameOptional.get();
            Lock lock = locks.get(code);
            lock.lock();
            log.debug("Locked room {}", game.getRoomCode());
        }
        return gameOptional;
    }

    public Optional<Game> find(String code) {
        return Optional.ofNullable(createCopy(games.get(code)));
    }

    public void saveAndRelease(Game game) {
        if (!games.containsKey(game.getRoomCode())) {
            throw new IllegalArgumentException("Game is not registered");
        }
        games.put(game.getRoomCode(), game);
        release(game);
    }

    public void release(Game game) {
        if (!games.containsKey(game.getRoomCode())) {
            throw new IllegalArgumentException("Game is not registered");
        }
        Lock lock = locks.get(game.getRoomCode());
        lock.unlock();
        log.debug("Unlocked room {}", game.getRoomCode());
    }

    private Game createCopy(Game game) {
        if (game == null) {
            return null;
        }
        return Game.builder()
                .roomCode(game.getRoomCode())
                .quizId(game.getQuizId())
                .moderator(game.getModerator())
                .status(game.getStatus())
                .playersPoints(new HashMap<>(game.getPlayersPoints()))
                .currentQuestion(game.getCurrentQuestion())
                .votesByQuestions(game.getVotesByQuestions()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, v -> new HashMap<>(v.getValue()))))
                .build();
    }

}
