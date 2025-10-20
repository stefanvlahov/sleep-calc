package org.svlahov.sleepcalc;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.svlahov.sleepcalc.support.TestJwtDynamicProps;

@SpringBootTest
@ActiveProfiles("test")
class SleepCalcApplicationTests extends TestJwtDynamicProps {

    @Test
    void contextLoads() {
    }

}
