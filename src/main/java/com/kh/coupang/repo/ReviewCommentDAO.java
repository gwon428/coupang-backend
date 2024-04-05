package com.kh.coupang.repo;

import com.kh.coupang.domain.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewCommentDAO extends JpaRepository<ReviewComment, Integer> {
    // nativeQuery : JPA 방식 말고 내가 짠 쿼리문대로 가져오겠다
    @Query(value="SELECT * FROM review_comment WHERE revi_code = :code", nativeQuery = true)
    List<ReviewComment> findByReviCode(@Param("code") int code);
}
