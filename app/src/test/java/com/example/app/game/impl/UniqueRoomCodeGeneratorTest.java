package com.example.app.game.impl;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasLength;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;

class UniqueRoomCodeGeneratorTest {

    @Test
    void hasOnlyDigits() {
        var generator = new UniqueRoomCodeGenerator();

        String code = generator.generate();

        assertThat(code, matchesRegex("^\\d{6,}$"));
    }

    @Test
    void generatesFirstSixDigitsCode() {
        var generator = new UniqueRoomCodeGenerator();

        String code = generator.generate().trim();

        assertThat(code, hasLength(6));
    }

    @Test
    void codesAreUnique() {
        var generator = new UniqueRoomCodeGenerator();

        String code = generator.generate();
        String anotherCode = generator.generate();

        assertThat(code, is(not(anotherCode)));
    }

    @Test
    void generatesMoreDigitsCodeIfOverflow() {
        var generator = new UniqueRoomCodeGenerator();

        for (int i = 0; i < 1_000_000; i++) {
            generator.generate();
        }
        String code = generator.generate();

        assertThat(code, hasLength(7));
    }

}
