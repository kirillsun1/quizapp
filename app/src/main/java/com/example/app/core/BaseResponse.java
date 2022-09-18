package com.example.app.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BaseResponse {

    private final boolean error;

    public static BaseResponse ok() {
        return new BaseResponse(false);
    }

}
