package top.fblue.watermelon.infrastructure.repository;

import org.junit.jupiter.api.Test;
import top.fblue.watermelon.domain.user.entity.UserToken;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTokenRepositoryImplTest {

    @Test
    void shouldCreateTokenUsingApplicationClock() {
        Clock clock = Clock.fixed(
                Instant.parse("2026-08-07T08:00:00Z"), ZoneId.of("Asia/Shanghai"));
        UserTokenRepositoryImpl repository = new UserTokenRepositoryImpl(clock);

        String token = repository.create(7L);

        UserToken userToken = repository.findByToken(token);
        LocalDateTime createdTime = LocalDateTime.now(clock);
        assertEquals(createdTime, userToken.getCreatedTime());
        assertEquals(createdTime.plusDays(7), userToken.getExpireTime());
    }
}
