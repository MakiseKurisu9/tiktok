package org.example.tiktok.controller;

import jakarta.annotation.Resource;
import org.example.tiktok.dto.FavouriteDTO;
import org.example.tiktok.entity.Result;
import org.example.tiktok.service.CustomerService;
import org.springframework.web.bind.annotation.*;


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



}
