package org.example.tiktok.entity.User;

import lombok.Data;

import java.util.Map;

@Data
public class UserModel {
    //key:标签id value:概率
    Map<Long,Double> model;
}
