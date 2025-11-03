package ru.belisario.engine.client.resource;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.belisario.engine.client.ClientInstance;

import static org.junit.jupiter.api.Assertions.*;

public class ResourcesLoaderTest {

    private ResourcesLoader loader;

    @BeforeAll
    static void setUp(){
        ClientInstance.main(new String[]{""});
    }

    @Test
    @DisplayName("10+10")
    void should10Plus10(){
        double result = ResourcesLoader.calclulateExpression("10+10");

        assertEquals(20, result);
    }
}
