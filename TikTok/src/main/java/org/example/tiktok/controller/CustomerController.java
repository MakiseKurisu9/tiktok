package org.example.tiktok.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.example.tiktok.dto.FavouriteDTO;
import org.example.tiktok.dto.UserModelDTO;
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

    /** 获取当前登录用户的所有收藏夹 */
    @GetMapping("/favourites")
    public Result getCustomerFavourite() {
        return customerService.getCustomerFavourite();
    }

    /** 根据收藏夹 ID 获取收藏夹信息（不含视频） */
    @GetMapping("/favourites/{favouriteId}")
    public Result getFavouriteById(@PathVariable Long favouriteId) {
        return customerService.getFavouriteById(favouriteId);
    }

    /** 新增或更新收藏夹 */
    @PostMapping("/favourites")
    public Result AddOrUpdateFavourite(FavouriteDTO favouriteDTO) {
        return customerService.addOrUpdateFavourite(favouriteDTO);
    }

    /** 删除指定 ID 的收藏夹 */
    @DeleteMapping("/favourites/{favouriteId}")
    public Result DelFavourite(@PathVariable Long favouriteId) {
        return customerService.delFavourite(favouriteId);
    }

    /** 用户订阅视频分类（传入分类字符串） */
    @PostMapping("/subscribe")
    public Result subscribeVideoTypes(@RequestParam String types){
        return customerService.subscribeVideoTypes(types);
    }

    /** 获取当前用户已订阅的视频分类 */
    @GetMapping("/subscribe")
    public Result getSubscribe() {
        return customerService.getSubscribeByUserId();
    }

    /** 上传用户头像 */
    @PostMapping("/upload/avatar")
    public Result uploadAvatar(MultipartFile file) throws IOException {
        return customerService.uploadAvatar(file);
    }
    /** 根据用户 ID 获取用户信息 */
    @GetMapping("/getInfo/{userId}")
    public Result getUserInfoByUserId(@PathVariable Long userId) throws JsonProcessingException {
        return customerService.getUserInfoByUserId(userId);
    }
    /** 更新当前用户信息（昵称、头像、性别、简介） */
    @PutMapping
    public Result updateUserInfo(String nickName,String avatarSource,
                                 String sex, String userDescription) throws JsonProcessingException {
        return customerService.updateUserInfo(nickName,avatarSource,sex,userDescription);
    }

    /** 获取当前用户关注的用户（分页） */
    @GetMapping("/follow")
    public Result getFollow(@RequestParam(defaultValue = "1") Integer page,
                            @RequestParam(defaultValue = "10") Integer limit){
        return customerService.getFollow(page,limit);
    }
    /** 获取当前用户的粉丝列表（分页） */
    @GetMapping("/followers")
    public Result getFollowers(@RequestParam(defaultValue = "1") Integer page,
                               @RequestParam(defaultValue = "10") Integer limit) {
        return customerService.getFollowers(page,limit);
    }
    /**
     * 关注或取消关注用户
     * @param followUserId 被关注用户 ID
     * @param isFollow true 表示关注，false 表示取消关注
     */
    @PostMapping("/{id}/{isFollow}")
    //前端判断isFollow
    public Result followUser(@PathVariable("id") Long followUserId,@PathVariable("isFollow") Boolean isFollow) {
        return customerService.followUser(followUserId,isFollow);
    }

    /*todo*/
    //可能做共同查看共同关注 用set求个交集

    /**
     * 更新当前用户的兴趣模型
     * 前端传入标签及评分（如停留时长评分），用于兴趣推荐
     */
    @PostMapping("/updateUserModel")
    public Result updateUserModel(@RequestBody UserModelDTO userModelDTO) throws JsonProcessingException {
        return customerService.updateUserModel(userModelDTO);
    }


}
