package ru.yappy.docstorage;

import org.apache.catalina.LifecycleException;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.junit.jupiter.api.Test;
import ru.yappy.docstorage.config.TestPersistenceConfig;

@SpringJUnitConfig(TestPersistenceConfig.class)
class DocStorageServerTest {

    @Test
    void contextLoads() throws LifecycleException {
        DocStorageServer.main(new String[]{});
    }

}