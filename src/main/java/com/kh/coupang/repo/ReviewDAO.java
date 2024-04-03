package com.kh.coupang.repo;

import com.kh.coupang.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

public interface ReviewDAO extends JpaRepository<Review, Integer>, QuerydslPredicateExecutor<Review> {

    @Query(value="SELECT * FROM review WHERE prod_code= :code", nativeQuery = true)
    Page<Review> findByprodCode(@Param("code") Integer code, Pageable pageable);
}
