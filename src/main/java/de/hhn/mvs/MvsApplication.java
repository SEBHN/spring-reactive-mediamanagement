package de.hhn.mvs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@EnableOAuth2Sso
@SpringBootApplication
@RestController
public class MvsApplication extends WebSecurityConfigurerAdapter {
//public class MvsApplication {

//later: check https
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http.requiresChannel().anyRequest().requiresSecure();
//    }

    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login").permitAll();
    }


    public static void main(String[] args) {
        SpringApplication.run(MvsApplication.class, args);
    }

    @GetMapping("/")
    String home(Principal user) {
        return "Hello " + user.getName();
    }

    @GetMapping("/token")
    String token(OAuth2Authentication user) {
        OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) user.getDetails();
        return details.getTokenValue();
    }

}
