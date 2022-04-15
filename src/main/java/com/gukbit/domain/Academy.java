package com.gukbit.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table
public class Academy {
    @Id
    @Column
    private String code;

    @Column
    private String name;

    @Column
    private String home_url;

    @Column
    private String region;

    @Column
    private String addr;

    @Column
    private double eval;

    @Column
    private String tel;

    @Builder
    public Academy(String code, String name, String home_url, String region, String addr, double eval, String tel) {
        this.code = code;
        this.name = name;
        this.home_url = home_url;
        this.region = region;
        this.addr = addr;
        this.eval = eval;
        this.tel = tel;
    }
}