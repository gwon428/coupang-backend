package com.kh.coupang.service;

import com.kh.coupang.domain.Member;
import com.kh.coupang.repo.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserDAO userDao;

    // 회원가입
    public Member create(Member vo) {
        return userDao.save(vo);
    }

    // 로그인 - 사용자 확인
    public Member login(String id, String password, PasswordEncoder encoder){
        // 아이디 체크
        Member user = userDao.findById(id).orElse(null);
        // 암호화된 비밀번호와 입력된 비밀번호 일치 확인
        if(user!=null && encoder.matches(password, user.getPassword())){
            return user;
        }
        return null;
    }

}
