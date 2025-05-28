package org.example.tiktok.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.example.tiktok.dto.FavouriteDTO;
import org.example.tiktok.dto.FollowersDTO;
import org.example.tiktok.dto.PageBean;
import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.User.Favourite;
import org.example.tiktok.entity.User.User;
import org.example.tiktok.entity.Video.VideoType;
import org.example.tiktok.mapper.CustomerMapper;
import org.example.tiktok.service.CustomerService;
import org.example.tiktok.utils.AliOSSUtil;
import org.example.tiktok.utils.CacheClient;
import org.example.tiktok.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class CustomerServiceImpl implements CustomerService {

    @Resource
    CustomerMapper customerMapper;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    AliOSSUtil aliOSSUtil;

    @Resource
    CacheClient cacheClient;

    private final static ObjectMapper objectMapper = new ObjectMapper();

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

    @Override
    public Result uploadAvatar(MultipartFile file) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String filename = UUID.randomUUID() + originalFileName.substring(originalFileName.lastIndexOf("."));
        String url = aliOSSUtil.uploadFile(filename,file.getInputStream());
        return Result.ok("successfully upload file",url);
    }

    @Override
    public Result getUserInfoByUserId(Long userId) throws JsonProcessingException {
        String keyPrefix = "customer:user:";
        User user = cacheClient.queryWithPassThrough(
                keyPrefix,
                userId,
                User.class,
                id1 -> customerMapper.getUserByUserId(id1),
                2L,
                TimeUnit.HOURS);
        if(user == null) {
            return Result.fail("cannot find this user");
        }
        return Result.ok("successfully get user",user);
    }

    @Override
    public Result updateUserInfo(String nickName, String avatarSource, String sex, String userDescription) throws JsonProcessingException {
        Long userId = UserHolder.getUser().getId();
        String key = "customer:user:" + userId;
        User user = new User();
        user.setId(userId);
        user.setNickname(nickName);
        user.setUserDescription(userDescription);
        user.setSex(sex);
        user.setAvatarSource(avatarSource);
        customerMapper.updateUserInfo(user);
        stringRedisTemplate.delete(key);
        cacheClient.set(key,user,2L,TimeUnit.HOURS);
        return Result.ok("successfully update user info",user);
    }

    @Override
    public Result getFollow(Integer page, Integer limit) {
        Long userId = UserHolder.getUser().getId();
        //获取了这个人关注的所有用户id
        List<Long> followIds = customerMapper.getFollowIds(userId);
        if(followIds == null || followIds.isEmpty()) {
            return Result.ok("this user do not follow anyone",Collections.emptyList());
        }
        PageHelper.startPage(page,limit);
        List<FollowersDTO> follows = customerMapper.getFollowInfo(followIds);

        List<Long> mutualIds = customerMapper.getFollowersInList(userId,followIds);
        Set<Long> mutualSet = new HashSet<>(mutualIds);
        for (FollowersDTO dto : follows) {
            dto.setEachLike(mutualSet.contains(dto.getId()));
        }

        PageInfo<FollowersDTO> pageInfo = new PageInfo<>(follows);

        PageBean<FollowersDTO> pageBean = new PageBean<>();
        pageBean.setItems(pageInfo.getList());
        pageBean.setTotal(pageInfo.getTotal());
        return Result.ok("successfully get follow",pageBean);
    }

    @Override
    public Result getFollowers(Integer page, Integer limit) {
        Long userId = UserHolder.getUser().getId();
        List<Long> followersId = customerMapper.getFollowers(userId);
        if(followersId == null || followersId.isEmpty()) {
            return Result.ok("this user do not have any followers",Collections.emptyList());
        }
        PageHelper.startPage(page,limit);
        List<FollowersDTO> followers = customerMapper.getFollowersInfo(followersId);

        //查看是否互关 userId is follow followersDTO.getId()
        List<Long> mutualIds = customerMapper.getFollowingIds(userId,followersId);
        Set<Long> mutualSet = new HashSet<>(mutualIds);
        for (FollowersDTO dto : followers) {
            dto.setEachLike(mutualSet.contains(dto.getId()));
        }

        PageInfo<FollowersDTO> pageInfo = new PageInfo<>(followers);
        PageBean<FollowersDTO> pageBean = new PageBean<>();
        pageBean.setTotal(pageInfo.getTotal());
        pageBean.setItems(pageInfo.getList());
        return Result.ok("successfully get followers",pageBean);
    }

    @Override
    public Result followUser(Long followUserId, Boolean isFollow) {
        Long userId = UserHolder.getUser().getId();
        //redis存储关注用户的数据
        String key = "follows:" + userId;
        //已经关注了，再点取关
        if(isFollow(followUserId)) {
            int isSuccess = customerMapper.unFollow(followUserId, userId);
            if(isSuccess > 0) {
                stringRedisTemplate.opsForSet().remove(key,followUserId.toString());
            }
        } else {
            int isSuccess = customerMapper.follow(followUserId, userId);
            if(isSuccess > 0) {
                stringRedisTemplate.opsForSet().add(key,followUserId.toString());
            }
        }
        return Result.ok("关注/取关成功");
    }

    private Boolean isFollow(Long followUserId){
        Long userId = UserHolder.getUser().getId();
        return customerMapper.isFollow(followUserId,userId) > 0;
    }


}
