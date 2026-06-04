package it.uniroma3.siw.authentication;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import static it.uniroma3.siw.model.Credentials.*;


@Configuration
@EnableWebSecurity
public class AuthConfiguration {

    @Autowired
    CustomOAuth2UserService customOAuth2UserService;

    @Autowired
    private DataSource dataSource;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
                .dataSource(dataSource)
                .authoritiesByUsernameQuery("SELECT username, role from credentials WHERE username=?")
                .usersByUsernameQuery("SELECT username, password, 1 as enabled FROM credentials WHERE username=?");
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    protected SecurityFilterChain configure(final HttpSecurity httpSecurity)
            throws Exception{
        httpSecurity
                // ❗ SOLUZIONE: Disabilita CSRF solo per l'endpoint API alert
                // Altrimenti, Spring Security reindirizza la richiesta POST anonima a /login.
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(new AntPathRequestMatcher("/api/alerts"))
                        .and()
                )
                .cors().disable()
                .authorizeHttpRequests()

                // pagine e risorse su cui tutti possono fare GET
                .requestMatchers(HttpMethod.GET,"/","/login","/register","/index","/css/**", "/images/**", "favicon.ico").permitAll()
                // pagine e risorse su cui tutti possono fare POST
                .requestMatchers(HttpMethod.POST,"/search","/register","/login","/api/alerts").permitAll() // ✅ /api/alerts è qui

                // pagine e risorse su cui solo gli ADMIN possono fare GET
                .requestMatchers(HttpMethod.GET,"/admin/**").hasAnyAuthority(ADMIN_ROLE)
                // pagine e risorse su cui solo gli ADMIN possono fare POST
                .requestMatchers(HttpMethod.POST,"/admin/**").hasAnyAuthority(ADMIN_ROLE)

                // pagine SUPERVISOR
                .requestMatchers(HttpMethod.GET,"/supervisor/**").hasAnyAuthority(SUPERVISOR_ROLE, ADMIN_ROLE)
                .requestMatchers(HttpMethod.POST,"/supervisor/**").hasAnyAuthority(SUPERVISOR_ROLE, ADMIN_ROLE)

                // pagine non elencate sopra richiedono auth (autenticato come ADMIN o come DEFAULT)
                .anyRequest().authenticated()

                // se utente tenta di accedere a contenuto per cui non ha permesso viene reindirizzato qui
                .and()
                .exceptionHandling()
                .accessDeniedPage("/accessDenied")

                // LOGIN:
                .and().formLogin()
                .loginPage("/login")
                .permitAll()
                .defaultSuccessUrl("/profile",true)
                .failureUrl("/login?error=true")

                // 👉 CREA NUOVA SESSIONE:
                .and().sessionManagement()
                .sessionFixation().newSession()

                // OAUTH:
                .and()
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/profile", true)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                )


                // LOGOUT:
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .clearAuthentication(true).permitAll();
        return httpSecurity.build();
    }
}