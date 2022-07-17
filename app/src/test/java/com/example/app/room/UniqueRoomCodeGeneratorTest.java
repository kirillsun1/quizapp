package com.example.app.room;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class UniqueRoomCodeGeneratorTest {

    @Test
    void generatesFirstSixDigitsCode() {
        var generator = new UniqueRoomCodeGenerator();

        String code = generator.generate();

        assertThat(code, is("000001"));
    }

    @Test
    void generatesSecondSixDigitsCode() {
        var generator = new UniqueRoomCodeGenerator();

        generator.generate();
        String code = generator.generate();

        assertThat(code, is("000002"));
    }

    @Test
    void generatesMoreDigitsCodeIfOverflow() {
        var generator = new UniqueRoomCodeGenerator();

        for (int i = 0; i < 1_000_000; i++) {
            generator.generate();
        }
        String code = generator.generate();

        assertThat(code, is("1000001"));
    }
}