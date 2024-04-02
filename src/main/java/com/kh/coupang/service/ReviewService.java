package com.kh.coupang.service;

import com.kh.coupang.domain.Review;
import com.kh.coupang.domain.ReviewImage;
import com.kh.coupang.repo.ReviewDAO;
import com.kh.coupang.repo.ReviewImageDAO;
import org.springframework.beans.factory.annotation.Autowired;
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

    public List<Review> viewAll(){
        return review.findAll();
    }

}
