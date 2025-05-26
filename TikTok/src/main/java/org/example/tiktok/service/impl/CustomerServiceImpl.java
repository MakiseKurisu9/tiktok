package org.example.tiktok.service.impl;

import jakarta.annotation.Resource;
import org.example.tiktok.dto.FavouriteDTO;
import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.User.Favourite;
import org.example.tiktok.entity.Video.VideoType;
import org.example.tiktok.mapper.CustomerMapper;
import org.example.tiktok.service.CustomerService;
import org.example.tiktok.utils.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Resource
    CustomerMapper customerMapper;

    @Override
    public Result getCustomerFavourite() {
        Long userId = UserHolder.getUser().getId();
        List<Favourite> customerFavourite = customerMapper.getCustomerFavourite(userId);
        if(customerFavourite.isEmpty()) {
            return Result.ok("this user do not have any favourite", Collections.emptyList());
        } else {
            return Result.ok("successfully get customer favourite",customerFavourite);
        }
    }

    @Override
    public Result getFavouriteById(Long favouriteId) {
        Favourite favouriteById = customerMapper.getFavouriteById(favouriteId);
        if(favouriteById == null) {
            return Result.fail("cannot get this favouriteById");
        } else {
            return Result.ok("successfully get favouriteById",favouriteById);
        }
    }

    @Override
    public Result addOrUpdateFavourite(FavouriteDTO favouriteDTO) {
        Long userId = UserHolder.getUser().getId();

        Favourite favourite = new Favourite();
        favourite.setId(favourite.getId());
        favourite.setName(favouriteDTO.getName());
        favourite.setDescription(favouriteDTO.getDescription());
        favourite.setCreateUserId(userId);


        if(favouriteDTO.getId() == null) {
            favourite.setCreateTime(LocalDateTime.now());
            favourite.setVideoCount(0);
            favourite.setUpdateTime(LocalDateTime.now());
            customerMapper.addFavourite(favourite);
            return Result.ok("successfully add favourite",favourite);
        } else {
            customerMapper.updateFavourite(favourite);
            return Result.ok("successfully update favourite",favourite);
        }
    }

    @Override
    @Transactional
    public Result delFavourite(Long favouriteId) {
        customerMapper.delFavouriteById(favouriteId);
        customerMapper.delFavouriteUserById(favouriteId);
        customerMapper.delFavouriteVideoById(favouriteId);
        return Result.ok("successfully delete favourite");
    }

    @Override
    @Transactional
    public Result subscribeVideoTypes(String types) {
        //实际情况应该是前端判非空输入 保险起见 后端也加上
        if(types == null || types.isEmpty()) {
            return Result.fail("subscribe cannot not be null");
        }
        List<Long> typeIds = Arrays.stream(types.split(","))
                .map(String::trim)
                .filter(id -> !id.isEmpty())
                .map(Long::valueOf)
                .toList();
        Long userId = UserHolder.getUser().getId();
        customerMapper.subscribeVideoTypes(userId,typeIds);

        return Result.ok("successfully subscribe");
    }

    @Override
    public Result getSubscribeByUserId() {
        Long userId = UserHolder.getUser().getId();
        List<Long> subscribedVideoTypes = customerMapper.getSubscribeByUserId(userId);
        if(subscribedVideoTypes == null || subscribedVideoTypes.isEmpty()) {
            return Result.ok("no subscribedVideoTypes",Collections.emptyList());
        }
        List<VideoType> videoTypes = customerMapper.getVideoTypesByIds(subscribedVideoTypes);
        return Result.ok("successfully get subscribe videoType",videoTypes);
    }


}
