package com.cos.security1.config.oauth;

import com.cos.security1.config.auth.PrincipalDetails;
import com.cos.security1.config.oauth.provider.FacebookUserInfo;
import com.cos.security1.config.oauth.provider.GoogleUserInfo;
import com.cos.security1.config.oauth.provider.NaverUserInfo;
import com.cos.security1.config.oauth.provider.OAuth2Userinfo;
import com.cos.security1.model.User;
import com.cos.security1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    //구글로부터 받은 userRequest 데이터에 대한 후처리 되는 함수
    // 함수 종료시 @AuthenticationPrincipal 어노테이션이 만들어진다
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        System.out.println("getClientRegistration:" + userRequest.getClientRegistration()); //registration로 어떤 OAuth로 로그인 했는지 확인 가능
        System.out.println("getAccessToken:" + userRequest.getAccessToken().getTokenValue());

        OAuth2User oauth2User = super.loadUser(userRequest);
        // 구글 로그인 버튼 클릭 -> 구글 로그인창 -> 로그인 완료 -> code를 리턴(OAuth-Client라이브러리가 받아줌) -> AccessToken 요청
        // -> AccessToken를 받는데 userRequest정보도 같이 받음 -> loadUser함수 호출 -> 구글로부터 회원프로필을 받아준다
        System.out.println("getAttributes:" + oauth2User.getAttributes());

        OAuth2Userinfo oAuth2Userinfo = null;
        if(userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            System.out.println("구글 로그인 요청");
            oAuth2Userinfo = new GoogleUserInfo(oauth2User.getAttributes());
        } else if(userRequest.getClientRegistration().getRegistrationId().equals("facebook")) {
            System.out.println("페이스북 로그인 요청");
            oAuth2Userinfo = new FacebookUserInfo(oauth2User.getAttributes());
        } else if(userRequest.getClientRegistration().getRegistrationId().equals("naver")) {
            System.out.println("네이버 로그인 요청");
            oAuth2Userinfo = new NaverUserInfo((Map)oauth2User.getAttributes().get("response"));
        } else {
            System.out.println("우리는 구글, 페이스북, 네이버만 지원한다요");
        }

        String provider = oAuth2Userinfo.getProvider(); // 구글
        String providerId = oAuth2Userinfo.getProviderId();  // 구글에서 제공하는 primary key와 같음 (null)
        String username = provider + "-" + providerId;  // google -12213 (중복될일 없음) 안만들어줘도 됨
        String password = bCryptPasswordEncoder.encode("겟인데어");
        String email = oAuth2Userinfo.getEmail();
        String role = "ROLE_USER";

        //이미 회원가입 되어 있는지 확인
        User userEntity = userRepository.findByUsername(username);

        //디비에 정보가 없으면 회원가입을 강제로 진행
        if(userEntity == null) {
            System.out.println("Oauth 로그인이 최초입니다.");
            userEntity = User.builder()
                    .username(username)
                    .password(password)
                    .email(email)
                    .role(role)
                    .provider(provider)
                    .providerId(providerId)
                    .build();
            userRepository.save(userEntity);
        } else {
            System.out.println("로그인을 이미 한적이 있습니다. 당신은 자동회원가입이 되어 있습니다.");
        }

        //로그인이 완료되면 PrincipalDetails가 반환된다 (이 객체는 PrincipalDetails에서 생성자 만들떄 만든거임)
        //만들어진 PrincipalDetails는 Attentication에 저장됨
        return new PrincipalDetails(userEntity, oauth2User.getAttributes());
    }
}
