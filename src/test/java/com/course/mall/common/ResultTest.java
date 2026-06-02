package com.course.mall.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {
    @Test
    void okReturnsSuccessBody() {
        Result<String> result = Result.ok("done");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("success");
        assertThat(result.getData()).isEqualTo("done");
    }

    @Test
    void failKeepsCodeAndMessage() {
        Result<Void> result = Result.fail(404, "not found");

        assertThat(result.getCode()).isEqualTo(404);
        assertThat(result.getMessage()).isEqualTo("not found");
        assertThat(result.getData()).isNull();
    }
}
