package ru.msu.cmc.webprak;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

// It is not in a separate directory, 'cause that is a tradition.
// This is the main file for all tests.
// All the tests are postfixed with "Test.java".

@SpringBootTest
@Sql(
        scripts = {
                "file:sql/01_create_db.sql",
                "file:sql/02_init_db.sql"
        },
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
public abstract class BaseIntegrationTest {
}

