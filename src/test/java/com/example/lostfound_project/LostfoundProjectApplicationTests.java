package com.example.lostfound_project;

import com.example.lostfound_project.repository.LostItemRepository;
import com.example.lostfound_project.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
class LostfoundProjectApplicationTests {

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private LostItemRepository lostItemRepository;

    @Test
    void contextLoads() {
    }

}
