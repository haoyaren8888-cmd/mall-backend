package com.course.mall.vo;

import com.course.mall.entity.User;
import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String phone;
    private String studentNo;
    private String campus;
    private String college;
    private String dormitory;
    private String role;
    private String status;
    private Integer creditScore;
    private Integer dealCount;

    public static UserVO from(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setStudentNo(user.getStudentNo());
        vo.setCampus(user.getCampus());
        vo.setCollege(user.getCollege());
        vo.setDormitory(user.getDormitory());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreditScore(user.getCreditScore());
        vo.setDealCount(user.getDealCount());
        return vo;
    }
}
