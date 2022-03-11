package com.msb.mall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import com.msb.common.exception.groups.AddGroupsInterface;
import com.msb.common.exception.groups.UpdateGroupsInterface;
import com.msb.common.valid.ListValue;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 *
 * @author Yiyi Zhu
 * @email zhuyiyi1990@outlook.com
 * @date 2022-02-24 11:16:48
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @NotNull(message = "更新数据品牌ID必须不为空", groups = {UpdateGroupsInterface.class})
    @Null(message = "添加品牌信息品牌ID必须为空", groups = {AddGroupsInterface.class})
    @TableId
    private Long brandId;

    /**
     * 品牌名
     */
//    @NotEmpty
    @NotNull(groups = {AddGroupsInterface.class})
    @NotBlank(message = "品牌的名称不能为空", groups = {AddGroupsInterface.class, UpdateGroupsInterface.class})
    private String name;

    /**
     * 品牌logo地址
     */
//    @NotBlank(message = "logo不能为空", groups = {AddGroupsInterface.class})
    @URL(message = "logo必须是一个合法URL地址", groups = {AddGroupsInterface.class, UpdateGroupsInterface.class})
    private String logo;

    /**
     * 介绍
     */
    private String descript;

    /**
     * 显示状态[0-不显示；1-显示]
     */
    @NotNull(message = "显示状态不能为空", groups = {AddGroupsInterface.class})
    @ListValue(value = {0, 1}, groups = {AddGroupsInterface.class, UpdateGroupsInterface.class})
    private Integer showStatus;

    /**
     * 检索首字母
     */
    @NotBlank(message = "检索首字母不能为空", groups = {AddGroupsInterface.class, UpdateGroupsInterface.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是单个的字母", groups = {AddGroupsInterface.class, UpdateGroupsInterface.class})
    private String firstLetter;

    /**
     * 排序
     */
    @NotNull(message = "排序不能为null", groups = {AddGroupsInterface.class})
    @Min(value = 0, message = "排序不能小于0", groups = {AddGroupsInterface.class, UpdateGroupsInterface.class})
    private Integer sort;

}