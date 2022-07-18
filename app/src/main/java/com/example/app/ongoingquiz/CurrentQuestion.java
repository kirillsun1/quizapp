package com.example.app.ongoingquiz;

import java.util.List;

public record CurrentQuestion(String text, List<String> answers) {
}
