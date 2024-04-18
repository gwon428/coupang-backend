package com.kh.coupang.repo;

import com.kh.coupang.domain.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewImageDAO extends JpaRepository<ReviewImage,  Integer>{

    @Query(value="SELECT * FROM review_image WHERE revi_code= :code", nativeQuery = true)
    List<ReviewImage> findByreviCode(@Param("code") Integer code);
}

