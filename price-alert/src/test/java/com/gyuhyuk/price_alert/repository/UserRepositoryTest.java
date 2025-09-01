package com.gyuhyuk.price_alert.repository;

import com.gyuhyuk.price_alert.data.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2, replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource("classpath:application-test.properties") //test용 properties 파일 설정
@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Test
    public void findById() {
        // ✅ given
        String userId = UUID.randomUUID().toString();
        UserEntity user = UserEntity.builder()
                .id(userId)
                .email("kyuhyuk@example.com")
                .password("securePassword123")
                .name("이규혁")
                .createdAt(LocalDateTime.now())
                .build();

        // ✅ when
        userRepository.save(user);

        // ✅ then
        Optional<UserEntity> found = userRepository.findById(userId);
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("kyuhyuk@example.com");
        assertThat(found.get().getName()).isEqualTo("이규혁");
    }

}
