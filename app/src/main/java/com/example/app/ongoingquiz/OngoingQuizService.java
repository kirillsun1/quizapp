package com.example.app.ongoingquiz;

public interface OngoingQuizService {

    void start(String requester, String code);

    void vote(String requester, String roomCode, int choice);

    void moveOn(String value, String code);
    
}
