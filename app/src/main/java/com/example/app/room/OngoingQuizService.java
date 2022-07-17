package com.example.app.room;

public interface OngoingQuizService {

    boolean start(String requester, String code);

    boolean vote(String requester, String roomCode, int choice);

    boolean moveOn(String value, String code);
    
}
