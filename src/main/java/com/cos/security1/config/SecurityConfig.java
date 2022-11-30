package com.cos.security1.config;

import com.cos.security1.config.oauth.PrincipalOauth2UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Configuration
@EnableWebSecurity  // 스프링 시큐리티 필터(SecurityConfig)가 스프링 필터체인에 등록이 된다
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true) //secured 어노테이션 활성화, preAuthorized, PostAuthorized 어노테이션 활성화
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private PrincipalOauth2UserService principalOauth2UserService;

    @Bean // 해당메서드의 리턴되는 오브젝트를 IOC로 등록해준다. 어디서든 사용할 수 있다
    public BCryptPasswordEncoder encodePwd() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
        http.authorizeRequests()
        .antMatchers("/user/**").authenticated()    // authenticated의 의미는 인증(로그인)만 되면 들어갈수 있는 주소
                                                              // admin이나 manager도 로그인만 하면 들어갈 수 있다
        .antMatchers("/manager/**").access("hasRole('ROLE_ADMIN') or hasRole('ROLE_MANAGER')")
        .antMatchers("/admin/**").access("hasRole('ROLE_ADMIN')")
        .anyRequest().permitAll()
        .and()
        .formLogin()
        .loginPage("/loginForm")
        .loginProcessingUrl("/login") // /login 주소가 호출이 되면 시큐리티가 낚아채서 대신 로그인을 진행해 준다 (컨트롤러에 /login 불필요)
        .defaultSuccessUrl("/") // 로그인이 성공하면 main페이지로 가게 만든다..
        .and()
        // 구글 로그인이 완료된 후의 후처리가 필요함 Tip. 구글 로그인 성공 -> 코드 X, 액세스 토큰 + 사용자 프로필 정보 O 를 바로 받아볼 수 있음
        // 1) 코드 받기(로그인 인증 - 구글의 정상적인 사용자구나) 2) 액세스 토큰 받기(구글 정보에 접근할 수 있는 권한)
        // 3) 사용자 프로필 정보를 가져와서 4-1) 그 정보를 토대로 회원가입을 자동으로 진행시키기도 함
        // 4-2) (이메일, 전화번호, 이름, 아이디) -> 추가적으로 집주소, 고객 등급 등 추가 정보가 필요함
        .oauth2Login()
        .loginPage("/loginForm")
        .userInfoEndpoint()
        .userService(principalOauth2UserService);
    }
}



