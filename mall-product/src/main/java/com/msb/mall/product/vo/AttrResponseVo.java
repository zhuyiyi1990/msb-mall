package com.msb.mall.product.vo;

import lombok.Data;

@Data
public class AttrResponseVo extends AttrVO {

    private String catalogName;

    private String groupName;

    private Long[] catalogPath;

}