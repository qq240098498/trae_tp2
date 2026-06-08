package com.express.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MemberRequest {

    private String name;

    private String phone;

    private String address;

    private BigDecimal discount;
}
