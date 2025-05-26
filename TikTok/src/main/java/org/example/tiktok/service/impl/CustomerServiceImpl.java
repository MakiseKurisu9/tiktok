package org.example.tiktok.service.impl;

import jakarta.annotation.Resource;
import org.example.tiktok.dto.FavouriteDTO;
import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.User.Favourite;
import org.example.tiktok.mapper.CustomerMapper;
import org.example.tiktok.service.CustomerService;
import org.example.tiktok.utils.UserHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
}
