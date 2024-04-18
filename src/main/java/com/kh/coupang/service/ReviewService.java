package com.kh.coupang.service;

import com.kh.coupang.domain.Review;
import com.kh.coupang.domain.ReviewImage;
import com.kh.coupang.repo.ReviewDAO;
import com.kh.coupang.repo.ReviewImageDAO;
import com.querydsl.core.BooleanBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewDAO review;

    @Autowired
    private ReviewImageDAO image;

    public Review create(Review vo){
        return review.save(vo);
    }

    // 이미지가 몇 번 추가될 지 모르기 때문에 create()와 메서드 분리
    public ReviewImage createImg(ReviewImage vo){
        return image.save(vo);
    }

    public Page<Review> viewAll(Pageable pageable, BooleanBuilder builder){
        return review.findAll(builder, pageable);
    }

    public Review view(int code){
        return review.findById(code).orElse(null);
    }

    public Review update(Review vo){
        return review.save(vo);
    }

    public ReviewImage updateImg(ReviewImage img){
        return image.save(img);
    }

    // 이미지들 조회
    public List<ReviewImage> viewImges(int code){
        return image.findByreviCode(code);
    }

    public void deleteImg(int code){
        image.deleteById(code);
    }

    public void delete(int code){
        Review vo = review.findById(code).orElse(null);
        if(vo != null){
            review.deleteById(code);
        }
    }


}
