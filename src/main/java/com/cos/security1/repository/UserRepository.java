package com.cos.security1.repository;

import com.cos.security1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

//CRUD 함수를 JpaRepository가 들고 있음
//@Repostitory라는 어노테이션 없어도 IOC가 가능. 이유는 JpaRepository를 상속했기 때문(자동으로 빈 생성)
public interface UserRepository extends JpaRepository<User, Integer> {

    //findBy까지는 규칙 / Username은 문법
    //select * from user where username = ? 이게 호출이 됨
    // ?에는 파라미터로 들어온 username이 들어감
    public User findByUsername(String username); // Jpa query method
}
