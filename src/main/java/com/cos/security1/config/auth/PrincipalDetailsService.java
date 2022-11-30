package com.cos.security1.config.auth;

import com.cos.security1.model.User;
import com.cos.security1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


// Security 설정에서 loginProcessingUrl("/login");으로 설정했기 때문에
// /login 요청이 오면 자동으로 UserDetailsService 타입으로 Ioc되어 있는 LoadUserByUsername 함수가 실행
// 그때 username 값도 가져옴
// @Service를 쓰면 PrincipalDetailsService가 IOC에 등록
// username을 form태그에서도 이름을 맞춰줘야 함
@Service
public class PrincipalDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    // 시큐리티 session 내부 => authentication 내부 => UserDetails
    // UserDetails가 반환되면서 authentication 내부에 들어감 -> session 내부에 들어감
    // 로그인 과정
    // 함수 종료시 @AuthenticationPrincipal 어노테이션이 만들어진다
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //userRepository는 기본적인 CRUD만 가능하기 때문에 findByUsername을 만들어줘야 한다
        User userEntity = userRepository.findByUsername(username);
        if(userEntity != null) { // username이 디비에 있다는 얘기
            return new PrincipalDetails(userEntity);  // PrincipalDetails가 UserDetails이기 때문에 userEntity를 넣어서 반환
        }
        return null;
    }
}
