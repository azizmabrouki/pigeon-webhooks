package pigeon;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class PigeonApplicationTests {

	@Test
	void contextLoads() {
	}

}
