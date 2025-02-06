package ru.yappy.docstorage;

import org.apache.catalina.LifecycleException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.junit.jupiter.api.Test;
import ru.yappy.docstorage.config.TestPersistenceConfig;

import java.io.IOException;

@SpringJUnitConfig(TestPersistenceConfig.class)
@ActiveProfiles("test")
class DocStorageServerTest {

    @Test
    void contextLoads() throws LifecycleException, IOException {
        DocStorageServer.main(new String[]{});
    }

}