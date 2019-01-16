package de.hhn.mvs.security;


import de.hhn.mvs.FeatureTests;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
@WithMockUser(username = "junit@hs-heilbronn.de", password = "testingRocks911!", roles = "USER")
@TestPropertySource(locations = "/springSecurityConfigTest.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SrpingSecurityTest {

    @Test
    public void setServerSpringSecurity(){
        JUnitCore.runClasses(FeatureTests.class);
    }


}
