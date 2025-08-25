package org.linghu.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/**
 * 用户资料更新数据传输对象
 * 只包含允许用户更新的字段
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdateDTO {
    
    @Size(max = 255, message = "头像URL不能超过255个字符")
    private String avatar;
    
    /**
     * 用户个人资料信息
     * 提供结构化的个人信息字段，如姓名、性别、联系方式等
     */
    @Valid
    private ProfileRequestDTO profile;
    
    // 敏感字段如username、email、password和roles不应包含在此
}
