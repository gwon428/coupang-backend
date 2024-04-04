package com.kh.coupang.repo;

import com.kh.coupang.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;


// JpaRepository<VO, VO PRIMARY KEY의 type>
public interface UserDAO extends JpaRepository<Member, String> {

}
