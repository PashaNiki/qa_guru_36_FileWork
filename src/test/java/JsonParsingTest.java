import com.fasterxml.jackson.databind.ObjectMapper;
import model.FitnessClub;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

public class JsonParsingTest {

    ClassLoader cl = JsonParsingTest.class.getClassLoader();

    @Test
    @DisplayName("Чтение и парсинг JSON файла с тренерами")
    void parseJsonTest() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        try (InputStream is = cl.getResourceAsStream("trainers.json")) {
            assertNotNull(is, "JSON файл не найден в resources");
            FitnessClub club = objectMapper.readValue(is, FitnessClub.class);

            assertEquals("Intense Fitness", club.clubName);
            assertEquals("Ижевск", club.location);
            assertEquals(3, club.trainers.size());
            assertEquals("Бокс", club.trainers.get(0).specialization);
        }
    }
}
