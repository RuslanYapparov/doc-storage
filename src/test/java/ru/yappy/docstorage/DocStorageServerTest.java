package ru.yappy.docstorage;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.junit.jupiter.api.Test;
import ru.yappy.docstorage.config.TestPersistenceConfig;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig(TestPersistenceConfig.class)
@ActiveProfiles("test")
class DocStorageServerTest {

    @Test
    void contextLoads() {
        assertThrows(BeanCreationException.class, () -> DocStorageServer.main(new String[]{}));
    }

}