package com.example.lostfound_project.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    @Test
    void createTokenContainsUserId() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider("test-secret-key", 3600000L);

        String token = jwtTokenProvider.createToken("user1");

        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo("user1");
    }

    @Test
    void getUserIdReturnsNullWhenTokenIsTampered() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider("test-secret-key", 3600000L);
        String token = jwtTokenProvider.createToken("user1") + "tampered";

        String userId = jwtTokenProvider.getUserId(token);

        assertThat(userId).isNull();
    }
}
