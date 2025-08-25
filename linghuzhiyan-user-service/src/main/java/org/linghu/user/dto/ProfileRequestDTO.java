package org.linghu.user.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户资料请求DTO
 * 用于接收用户个人资料更新请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequestDTO {
    
    // 个人信息
    @Size(max = 50)
    private String realName;    // 真实姓名
    
    @Size(max = 50)
    private String nickname;    // 昵称
    
    @Size(max = 10)
    private String gender;      // 性别
    
    @Size(max = 20)
    private String birthdate;   // 出生日期
    
    @Size(max = 500)
    private String bio;         // 个人简介
    
    @Size(max = 100)
    private String location;    // 所在地区

    // 联系方式
    @Size(max = 20)
    private String phone;       // 联系电话
    
    @Size(max = 50)
    private String wechat;      // 微信号

    // 教育/工作信息
    @Size(max = 100)
    private String education;   // 教育背景
    
    @Size(max = 100)
    private String school;      // 学校
    
    @Size(max = 100)
    private String major;       // 专业

    // 其他信息
    @Size(max = 200)
    private String interests;   // 兴趣爱好
    
    @Size(max = 200)
    private String skills;      // 技能特长
}
