package com.kh.coupang.controller;

import com.kh.coupang.domain.*;
import com.kh.coupang.service.ReviewCommentService;
import com.kh.coupang.service.ReviewService;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RequestMapping("/api/*")
@RestController
public class ReviewController {

    @Autowired
    private ReviewService service;

    @Autowired
    private ReviewCommentService comment;

    @Value("${spring.servlet.multipart.location}")
    private String uploadPath;

    @PostMapping("/review")
    public ResponseEntity<Review> create(ReviewDTO dto) throws IOException {

        // review부터 추가하여 revi_code가 담긴 review 생성
        Review vo = new Review();

        // 사용자에게 받는 정보 dto로 받아 review에 담음
        vo.setId(dto.getId());
        vo.setProdCode(dto.getProdCode());
        vo.setReviTitle(dto.getReviTitle());
        vo.setReviDesc(dto.getReviDesc());
        vo.setRating(dto.getRating());

        // review 객체 생성
        Review result = service.create(vo);

        // review_image에는 revi_code가 필요
        for(MultipartFile file : dto.getFiles()){
            ReviewImage img = new ReviewImage();

            String fileName = file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String saveName = uploadPath + File.separator + "review" + File.separator + uuid + "_" + fileName;

            Path savePath = Paths.get(saveName);
            file.transferTo(savePath);

            img.setReviUrl(saveName);
            img.setReview(result);
            service.createImg(img);
        }
        return result != null? ResponseEntity.status(HttpStatus.CREATED).body(result) :
                ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
    }

    @GetMapping("/review")
    public ResponseEntity<List<Review>> viewAll(@RequestParam(name="prodCode", required = false) Integer prodCode, @RequestParam(name="page", defaultValue = "1") int page){
        Sort sort = Sort.by("reviCode").descending();
        Pageable pageable = PageRequest.of(page-1, 10, sort);

        QReview qReview = QReview.review;
        BooleanBuilder builder = new BooleanBuilder();
        if(prodCode != null){
            BooleanExpression expression = qReview.prodCode.eq(prodCode);
            builder.and(expression);
        }

        Page<Review> list = service.viewAll(builder, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(list.getContent());
    }

    public Object authentication(){
        SecurityContext securityContext = SecurityContextHolder.getContext();
        Authentication authentication = securityContext.getAuthentication();
        return authentication.getPrincipal();


    }
    // 리뷰 댓글 추가
    @PostMapping("/review/comment")
    public ResponseEntity createComment(@RequestBody ReviewComment vo){

        Object principal = authentication();

        if(principal instanceof Member){
            Member user = (Member) principal;
            vo.setUser(user);
            return ResponseEntity.ok(comment.create(vo));
        }
        return ResponseEntity.badRequest().build();
    }

    // 상품 1개에 따른 댓글 조회
    @GetMapping("/public/review/{code}/comment")
    public ResponseEntity<List<ReviewCommentDTO>> viewComment(@PathVariable("code") int code){
        // 상위 댓글 리스트
        List<ReviewComment> topList = comment.getTopLevelComments(code);

        // 상위 댓글을 DTO를 통해 가공
        List<ReviewCommentDTO> response = new ArrayList<>();

        // ArrayList에 반복문을 통해 가공된 상위 댓글 넣기
        for(ReviewComment top : topList){

            // 각각 상위 댓글의 revi_code를 통해 하위 댓글 리스트 가져오기
            List<ReviewComment> replies = comment.getRepliesComments(top.getReviCode(), code);

            // 하위 댓글을 DTO를 통해 가공
            List<ReviewCommentDTO> repliesDTO = new ArrayList<>();

            // ArrayList에 반복문을 통해 가공된 하위 댓글 넣기
            for(ReviewComment reply : replies){
                ReviewCommentDTO dto = ReviewCommentDTO.builder()
                        .reviCode(reply.getReviComCode())
                        .reviComCode(reply.getReviComCode())
                        .reviComDesc(reply.getReviComDesc())
                        .reviComDate(reply.getReviComDate())
                        // 댓글에 저장된 user 정보 가져오려면 getUser를 통해 한 번 더 들어가서 정보를 가져와야 함
                        .user(UserDTO.builder()
                                .id(reply.getUser().getId())
                                .name(reply.getUser().getName())
                                .build())
                        .build();
                repliesDTO.add(dto);
            }
            
            // ArrayList에 반복문을 통해 가공된 상위 댓글 넣기 (이 때, (하위 댓글 리스트)repliesDTO list도 넣게 됨)
            ReviewCommentDTO dto = ReviewCommentDTO.builder()
                    .reviCode(top.getReviCode())
                    .reviComCode(top.getReviComCode())
                    .reviComDesc(top.getReviComDesc())
                    .reviComDate(top.getReviComDate())
                    .user(UserDTO.builder()
                            .id(top.getUser().getId())
                            .name(top.getUser().getName())
                            .build())
                    .replies(repliesDTO)
                    .build();
            response.add(dto);
        }
        return ResponseEntity.ok(response);
    }
}
