package com.example.app.room;

public interface RoomService {

    Room createRoom(String moderator);

    boolean joinRoom(String code, String joiner);

    boolean assignQuiz(String requester, String code, int quizId);

}
