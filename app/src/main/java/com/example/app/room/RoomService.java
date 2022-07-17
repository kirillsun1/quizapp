package com.example.app.room;

public interface RoomService {

    Room createRoom(String moderator);

    void joinRoom(String code, String joiner);

    void assignQuiz(String requester, String code, int quizId);

}
