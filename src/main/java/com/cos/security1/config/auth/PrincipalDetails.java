package com.cos.security1.config.auth;

//시큐리티가 /login 주소 요청이 오면 낚아채서 로그인을 진행시킨다
//로그인을 진행이 완료가 되면 시큐리티가 가지고 있는 session을 만들어준다 (Security ContextHolder라는 키값에 session정보를 저장)
//session에 들어갈 수 있는 오브젝트 타입 => Authentication 타입 객체
//Authentication 안에는 user정보가 있어야 됨
//User오브젝트 타입 => UserDetails 타입 객체

//Security Session => Authentication => UserDetails(PrincipalDetails)

import com.cos.security1.model.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

//나중에 알아서 빈 생성해줌
@Data
public class PrincipalDetails implements UserDetails, OAuth2User {

    private User user; //컴포지션
    private Map<String, Object> attributes;

    //일반 로그인할때 사용하는 객체
    public PrincipalDetails(User user) {
        this.user = user;
    }

    //구글 로그인할때 사용하는 객체
    public PrincipalDetails(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    //PrincipalDetails override
    //해당 User의 권한을 리턴하는 곳
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collect = new ArrayList<>();  // ArrayList는 Collection의 자손 -> ArrayList로 바꿔줘
        collect.add(new GrantedAuthority() {    // ArrayList에 GrantedAuthority 객체를 넣어준다
            @Override
            public String getAuthority() {  // 객체에 있는 getAuthority 메서드를 오버라이드를 해서 필요한 정보인 user.GetRole(String)를 반환시킨다
                return user.getRole();
            }
        });
        return collect;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {

        // 우리 사이트에서 1년동안 회원이 로그인을 안하면 휴면 계정으로 하기로 함
        // user.getLoginDate를 가져와서 현재시간 - 로그인 시간을 계산해서 1년을 초과하면 return을 false하면 휴면계정으로 됨
        return true;
    }

    //OAuth2User override
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return null;
    }
}
