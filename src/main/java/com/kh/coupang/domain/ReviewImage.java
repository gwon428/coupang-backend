package com.kh.coupang.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Table(name="review_image")  //테이블 컬럼에 "_"가 들어간 경우
public class ReviewImage {

    @Id @Column(name="revi_img_code")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int reviImgCode;

    @Column(name="revi_url")
    private String reviUrl;

    @ManyToOne
    @JoinColumn(name="revi_code")
    @JsonIgnore
    private Review review;
}
