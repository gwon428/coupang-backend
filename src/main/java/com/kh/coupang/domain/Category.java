package com.kh.coupang.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
public class Category {
    @Id @Column(name="cate_code")
    private int cateCode;

    @Column(name="cate_icon")
    private String cateIcon;

    @Column(name="cate_name")
    private String cateName;

    @Column(name="cate_url")
    private String cateUrl;
}
