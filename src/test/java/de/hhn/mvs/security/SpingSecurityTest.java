package de.hhn.mvs.security;


import de.hhn.mvs.FeatureTests;
import de.hhn.mvs.database.UserCrudRepo;
import de.hhn.mvs.model.User;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureWebTestClient
@WithMockUser(username = "junit@hs-heilbronn.de", password = "testingRocks911!", roles = "USER")
@TestPropertySource(locations = "/springSecurityConfigTest.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
public class SpingSecurityTest {

    @Autowired
    private WebTestClient webClient;
    @Autowired
    private UserCrudRepo userRepo;

    private User testUser;

    @Test
    public void setServerSpringSecurity(){
        JUnitCore.runClasses(FeatureTests.class);
    }


}
