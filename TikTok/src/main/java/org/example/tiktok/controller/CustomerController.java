package org.example.tiktok.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.example.tiktok.dto.FavouriteDTO;
import org.example.tiktok.entity.Result;
import org.example.tiktok.service.CustomerService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Resource
    CustomerService customerService;

    //获取用户收藏夹
    @GetMapping("/favourites")
    public Result getCustomerFavourite() {
        return customerService.getCustomerFavourite();
    }

    //获取收藏夹信息
    //获取收藏夹的视频的controller在videoController里
    @GetMapping("/favourites/{favouriteId}")
    public Result getFavouriteById(@PathVariable Long favouriteId) {
        return customerService.getFavouriteById(favouriteId);
    }
    //新增/修改收藏夹

    @PostMapping("/favourites")
    public Result AddOrUpdateFavourite(FavouriteDTO favouriteDTO) {
        return customerService.addOrUpdateFavourite(favouriteDTO);
    }

    //删除收藏夹
    @DeleteMapping("/favourites/{favouriteId}")
    public Result DelFavourite(@PathVariable Long favouriteId) {
        return customerService.delFavourite(favouriteId);
    }

    //订阅分类
    @PostMapping("/subscribe")
    public Result subscribeVideoTypes(@RequestParam String types){
        return customerService.subscribeVideoTypes(types);
    }

    //获取用户订阅的分类
    @GetMapping("/subscribe")
    public Result getSubscribe() {
        return customerService.getSubscribeByUserId();
    }

    @PostMapping("/upload/avatar")
    public Result uploadAvatar(MultipartFile file) throws IOException {
        return customerService.uploadAvatar(file);
    }

    @GetMapping("/getInfo/{userId}")
    public Result getUserInfoByUserId(@PathVariable Long userId) throws JsonProcessingException {
        return customerService.getUserInfoByUserId(userId);
    }


    @PutMapping
    public Result updateUserInfo(String nickName,String avatarSource,
                                 String sex, String userDescription) throws JsonProcessingException {
        return customerService.updateUserInfo(nickName,avatarSource,sex,userDescription);
    }

    //用户是哪个人的粉丝
    @GetMapping("/follow")
    public Result getFollow(@RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer limit){
        return customerService.getFollow(page,limit);
    }

    @GetMapping("/followers")
    public Result getFollowers(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer limit) {
        return customerService.getFollowers(page,limit);
    }

    @PostMapping("/{id}/{isFollow}")
    //前端判断isFollow
    public Result followUser(@PathVariable("id") Long followUserId,@PathVariable("isFollow") Boolean isFollow) {
        return customerService.followUser(followUserId,isFollow);
    }

    /*todo*/
    //可能做共同查看共同关注 用set求个交集



}
