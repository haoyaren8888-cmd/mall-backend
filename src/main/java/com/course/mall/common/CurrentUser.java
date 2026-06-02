package com.course.mall.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser implements Serializable {
    private Long id;
    private String username;
    private String nickname;
    private String role;
}
