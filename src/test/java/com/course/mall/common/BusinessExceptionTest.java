package com.course.mall.common;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {
    @Test
    void factoriesReturnExpectedStatusCode() {
        assertThat(BusinessException.badRequest("bad").getCode()).isEqualTo(400);
        assertThat(BusinessException.unauthorized("login").getCode()).isEqualTo(401);
        assertThat(BusinessException.forbidden("admin").getCode()).isEqualTo(403);
        assertThat(BusinessException.notFound("missing").getCode()).isEqualTo(404);
    }

    @Test
    void exceptionKeepsMessage() {
        BusinessException exception = BusinessException.badRequest("invalid quantity");

        assertThat(exception.getMessage()).isEqualTo("invalid quantity");
    }
}
