package com.example.app.room;

import com.example.app.room.impl.UniqueRoomCodeGenerator;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

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