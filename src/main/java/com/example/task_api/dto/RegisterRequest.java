package com.example.task_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message="邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 320,message = "邮箱不能超过320个字符")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Size(
            min = 8,
            max = 72,
            message = "密码长度必须在8到72个字符之间"
    )
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
