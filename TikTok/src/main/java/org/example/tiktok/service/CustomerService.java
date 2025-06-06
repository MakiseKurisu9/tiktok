package org.example.tiktok.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.example.tiktok.dto.FavouriteDTO;
import org.example.tiktok.dto.UserModelDTO;
import org.example.tiktok.entity.Result;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CustomerService {
    Result getCustomerFavourite();

    Result getFavouriteById(Long favouriteId);

    Result addOrUpdateFavourite(FavouriteDTO favouriteDTO);

    Result delFavourite(Long favouriteId);

    Result subscribeVideoTypes(String types);

    Result getSubscribeByUserId();

    Result uploadAvatar(MultipartFile file) throws IOException;

    Result getUserInfoByUserId(Long userId) throws JsonProcessingException;


    Result updateUserInfo(String nickName, String avatarSource, String sex, String userDescription) throws JsonProcessingException;

    Result getFollow(Integer page, Integer limit);

    Result getFollowers(Integer page, Integer limit);

    Result followUser(Long followUserId, Boolean isFollow);

    Result updateUserModel(UserModelDTO userModelDTO) throws JsonProcessingException;



}
