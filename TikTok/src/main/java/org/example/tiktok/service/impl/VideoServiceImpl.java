package org.example.tiktok.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.example.tiktok.dto.PageBean;
import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.Video.Video;
import org.example.tiktok.mapper.VideoMapper;
import org.example.tiktok.service.VideoService;
import org.example.tiktok.utils.PublicVideoServiceUtil;
import org.example.tiktok.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.example.tiktok.utils.SystemConstant.History_PREFIX;

@Service
public class VideoServiceImpl implements VideoService {
    @Resource
    VideoMapper videoMapper;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Resource
    PublicVideoServiceUtil publicVideoServiceUtil;
    @Override
    public Result getVideoInFavouriteTable(Long favouriteTableId) {
        List<Video> videoList = videoMapper.getVideoInFavouriteTable(favouriteTableId);
        //判断用户是否点赞了此视频
        videoList.forEach(
                video -> publicVideoServiceUtil.isStared(video)
        );
        if(videoList.isEmpty()) {
            return Result.ok("no data in favourite", Collections.emptyList());
        }
        return Result.ok("success get videoList in favourite table",videoList);
    }

    @Override
    public Result addVideoIntoFavouriteTable(Long favouriteTableId, Long videoId) {
        Integer isVideoInFavourite = videoMapper.isVideoInFavouriteTable(favouriteTableId, videoId);
        if(isVideoInFavourite > 0) {
            return Result.fail("this video already in this favourite");
        }
        videoMapper.addVideoIntoFavouriteTable(favouriteTableId, videoId);
        return Result.ok("add a video into favourite table success");
    }

    @Override
    public Result addVideoIntoHistory(Long videoId) {
        String key = History_PREFIX+ UserHolder.getUser().getId();

        //if video exist, first remove then add
        stringRedisTemplate.opsForList().remove(key,0,videoId);
        //make sure video in first place
        stringRedisTemplate.opsForList().leftPush(key,videoId.toString());

        //keep a user can only save 100 data
        stringRedisTemplate.opsForList().trim(key,0,99);
        //save 7 days
        stringRedisTemplate.expire(key,7, TimeUnit.DAYS);
        return Result.ok("add a video into history list");
    }

    @Override
    public Result getVideoHistory() {
        String key = History_PREFIX+ UserHolder.getUser().getId();
        //get all videoId in list, like videoId:1 videoId:2
        List<String> list = stringRedisTemplate.opsForList().range(key, 0, -1);
        if(list==null || list.isEmpty()) {
            return Result.ok("no video history",Collections.emptyList());
        }
        //解析id 将StringList转为Long
        List<Long> ids = list.stream()
                .map(Long::valueOf)
                .toList();
        //get data from db equal SELECT * FROM video WHERE id IN (1, 2, 3, 4);
        List<Video> videos = videoMapper.getVideosByVideoId(ids);
        //先将数据存入map中，用于保存历史记录
        Map<Long,Video> videoMap = videos.stream()
                .collect(Collectors.toMap(Video::getId,video -> video));
        //再通过顺序遍历ids，通过map获取当前id对应的video，并过滤null数据，最后转为list并返回
        //顺序还原order preserving
        List<Video> orderedVideos = ids.stream()
                .map(videoMap::get)
                .filter(Objects::isNull)
                .toList();
        return Result.ok("success get history list",orderedVideos);
    }

    @Override
    @Transactional
    public Result starVideo(Long videoId) {
        Long userId = UserHolder.getUser().getId();
        String key = "video:liked:" + videoId;
        //判断用户是否已经点赞
        Boolean member = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        if (BooleanUtils.isFalse(member)) {
            Boolean isStarSuccess = videoMapper.starVideo(videoId) ;
            Boolean isLikeSuccess =videoMapper.videoLike(videoId,userId) ;
            if(isStarSuccess && isLikeSuccess){
                stringRedisTemplate.opsForSet().add(key, userId.toString());
                return Result.ok("star success");
            }
        } else {
            Boolean isNotStarSuccess = videoMapper.decreaseStarVideo(videoId);
            Boolean isNotLikeSuccess =videoMapper.videoNotLike(videoId,userId);
            if(isNotStarSuccess && isNotLikeSuccess){
                stringRedisTemplate.opsForSet().remove(key, userId.toString());
                return Result.ok("decrease star success");
            }
        }
        return Result.fail("star not affect");
    }

    @Transactional
    @Override
    public Result deleteVideo(Long videoId) {
        videoMapper.deleteVideoLikes(videoId);
        videoMapper.deleteVideoShares(videoId);
        videoMapper.deleteVideoTypeRelations(videoId);
        Boolean isSuccess = videoMapper.deleteVideo(videoId);
        if(BooleanUtils.isTrue(isSuccess)) {
            stringRedisTemplate.delete("index:video:"+videoId);//maybe exist cache in redis
            return Result.ok("successfully delete video");
        } else {
            return Result.fail("fail to delete video");
        }
    }

    @Override
    public Result listVideos(Integer page, Integer limit) {
        PageBean<Video> pageBean = new PageBean<>();

        Long userId = UserHolder.getUser().getId();
        PageHelper.startPage(page,limit);
        List<Video> videos = videoMapper.getVideosByUserId(userId);
        if( videos == null || videos.isEmpty() ) {
            return Result.ok("this user do not publish any video",Collections.emptyList());
        }
        PageInfo<Video> pageInfo = new PageInfo<>(videos);

        pageBean.setItems(pageInfo.getList());
        pageBean.setTotal(pageInfo.getTotal());


        return Result.ok("successfully get data",pageBean);

    }


}
