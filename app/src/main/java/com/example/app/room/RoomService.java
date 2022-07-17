package com.example.app.room;

public interface RoomService {

    Room createRoom(String moderator);

    boolean joinRoom(String code, String joiner);

    boolean assignQuiz(String requester, String code, int quizId);

    boolean startQuiz(String requester, String code);

    boolean vote(String requester, String roomCode, int choice);
}
