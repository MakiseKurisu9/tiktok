package org.example.tiktok.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.BooleanUtils;
import org.example.tiktok.dto.CommentersDTO;
import org.example.tiktok.dto.PageBean;
import org.example.tiktok.dto.ScrollResult;
import org.example.tiktok.entity.Result;
import org.example.tiktok.entity.User.Follow;
import org.example.tiktok.entity.Video.Comment;
import org.example.tiktok.entity.Video.Video;
import org.example.tiktok.mapper.VideoMapper;
import org.example.tiktok.service.VideoService;
import org.example.tiktok.utils.CacheClient;
import org.example.tiktok.utils.PublicVideoServiceUtil;
import org.example.tiktok.utils.SnowflakeIdWorker;
import org.example.tiktok.utils.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
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
    CacheClient cacheClient;

    @Resource
    ObjectMapper objectMapper;

    @Resource
    SnowflakeIdWorker snowflakeIdWorker;


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
        List<Video> videos = videoMapper.getVideosByVideoIds(ids);
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



    @Override
    @Transactional
    public Result commentOrAnswerComment(Long videoId, Long parentId, String content) {
        Long userId = UserHolder.getUser().getId();
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setFromUserId(userId);
        comment.setVideoId(videoId);
        comment.setCreateTime(LocalDateTime.now());
        comment.setUpdateTime(LocalDateTime.now());
        //评论视频，一级评论
        if(parentId == null) {
            comment.setParentId(0L);
            comment.setLikesCount(0);
            comment.setChildCount(0);
            videoMapper.addComment(comment);
            comment.setRootId(comment.getId());
            videoMapper.updateRootId(comment.getId(),comment.getId());
        } else {//二级评论 回复评论的评论
            Comment parentComment = videoMapper.getCommentById(parentId);
            if(parentComment == null) {
                return Result.fail("comment dose not exist");
            }
            comment.setToUserId(parentComment.getFromUserId());
            comment.setParentId(parentComment.getId());
            comment.setRootId(parentComment.getRootId() != null ? parentComment.getRootId() : parentId);
            videoMapper.addComment(comment);

            videoMapper.addChildCount(parentComment.getId());
        }
        return Result.ok("successfully comment",comment);
    }

    @Override
    public Result getCommentByVideoId(int page, int limit, Long videoId) {
        PageHelper.startPage(page,limit);
        List<Comment> comments = videoMapper.getRootCommentsByVideoId(videoId);
        if(comments == null || comments.isEmpty()) {
            return Result.ok("there is no comment",Collections.emptyList());
        }
        //可优化 先查id 再通过id查缓存 再查不在id中的user indexService中实现过了 此处就不实现了
        //未来可能会优化:)
        for(Comment comment : comments) {
            CommentersDTO commentersDTO = videoMapper.getUserById(comment.getFromUserId());
            isCommentLiked(comment);
            comment.setCommentersDTO(commentersDTO);
        }

        PageInfo<Comment> pageInfo = new PageInfo<>(comments);

        PageBean<Comment> pageBean = new PageBean<>();
        pageBean.setTotal(pageInfo.getTotal());
        pageBean.setItems(pageInfo.getList());
        return Result.ok("successfully get comment in this video",pageBean);
    }

    @Override
    public Result likeComment(Long commentId) {
        Long userId = UserHolder.getUser().getId();
        String key = "comment:liked:" + commentId;

        Boolean member = stringRedisTemplate.opsForSet().isMember(key,userId.toString());
        if(BooleanUtils.isFalse(member)) {
            Boolean isLikeSuccess = videoMapper.likeComment(commentId);
            if(isLikeSuccess) {
                stringRedisTemplate.opsForSet().add(key,userId.toString());
                return Result.ok("like success");
            }
        } else {
            Boolean isUnLikeSuccess = videoMapper.unlikeComment(commentId);
            if(isUnLikeSuccess) {
                stringRedisTemplate.opsForSet().remove(key,userId.toString());
                return Result.ok("unlike success");
            }
        }
        return Result.fail("like/unlike not affect");
    }

    //前端判断评论的创建者是否为当前用户，是的话显示删除按钮
    @Override
    @Transactional
    public Result deleteComment(Long commentId) {
        Long userId = UserHolder.getUser().getId();
        Comment comment = videoMapper.getCommentById(commentId);
        if(comment == null ) {
            return Result.fail("comment does not exist");
        }
        if(!comment.getFromUserId().equals(userId)) {
            return Result.fail("not have authorize to delete comment");
        }
        //为根评论
        if(comment.getParentId() == 0L) {
            videoMapper.deleteCommentByRootId(comment.getRootId());
        } else {
            //不为根评论 删除底下的评论
            videoMapper.deleteComment(comment.getId());
            videoMapper.deleteCommentByParentId(comment.getParentId());
        }
        return Result.ok("delete successfully");
    }

    @Override
    public Result getSubCommentsByRootId(int page, int limit, Long rootId) {
        if(rootId != null) {
            PageHelper.startPage(page,limit);
            List<Comment> comments = videoMapper.getRootCommentsExcludeParentByVideoId(rootId);
            if(comments != null) {
                for(Comment comment : comments) {
                    CommentersDTO commentersDTO = videoMapper.getUserById(comment.getFromUserId());
                    isCommentLiked(comment);
                    comment.setCommentersDTO(commentersDTO);
                }
                PageBean<Comment> pageBean = new PageBean<>();
                PageInfo<Comment> pageInfo = new PageInfo<>(comments);

                pageBean.setItems(pageInfo.getList());
                pageBean.setTotal(pageInfo.getTotal());

                return Result.ok("successfully get sub comments",pageBean);
            }
        }
        return Result.fail("this comment has no root comment");
    }

    @Override
    public Result pushFeedVideos(Long max) {
        Long userId = UserHolder.getUser().getId();
        String key = "feed:" + userId;
        //新到旧 查3条
        Set<ZSetOperations.TypedTuple<String>> typedTuples = stringRedisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, max, 0, 4);
        if(typedTuples == null || typedTuples.isEmpty()) {
            return Result.ok("no feed videos",Collections.emptyList());
        }
        List<Long> ids = new ArrayList<>(typedTuples.size());
        long minTime = 0;
        for(ZSetOperations.TypedTuple<String> zSetOperations : typedTuples) {
            //视频id
            ids.add(Long.valueOf(zSetOperations.getValue()));
            //雪花id 最小时间
            minTime = zSetOperations.getScore().longValue();
        }
        List<Video> videosByVideoIds = videoMapper.getVideosByVideoIds(ids);
        ScrollResult result = new ScrollResult();
        result.setList(videosByVideoIds);
        result.setMinTime(minTime);
        return Result.ok("successfully get feed push videos",result);
    }

    //非空判断 前端进行
    @Override
    public Result addOrUpdateVideo(Video video) throws JsonProcessingException {
        Long userId = UserHolder.getUser().getId();
        video.setPublisherId(userId);
        video.setUpdateTime(LocalDateTime.now());
        //add
        if(video.getId() == null) {
            video.setCreateTime(LocalDateTime.now());
            videoMapper.addVideo(video);
        } else {
            videoMapper.updateVideo(video);
            //删除旧的缓存数据
            stringRedisTemplate.delete("index:video:" + video.getId());
        }
        //feed 推送给所有粉丝
        List<Follow> followers = videoMapper.getFollowers(userId);
        for(Follow follow : followers) {
            Long followerId = follow.getUserId();
            String key = "feed:" + followerId;
            stringRedisTemplate.opsForZSet().add(key,video.getId().toString(),snowflakeIdWorker.nextId());
        }
        //无论是添加还是更新 都需要更新缓存数据
        cacheClient.set("index:video:" + video.getId(),objectMapper.writeValueAsString(video),2L,TimeUnit.HOURS);
        return Result.ok("success update or add",video);
    }


    private void isCommentLiked(Comment comment) {
        Long userId = UserHolder.getUser().getId();
        String key = "comment:liked:" + comment.getId();
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key,userId.toString());
        comment.setIsLiked(BooleanUtils.isTrue(isMember));
    }

    /* 递归收集低级评论 like level2 level 3 level 4，类似reddit，抖音中实际只有两级
    private void collectAllCommentIds(Long commentId, List<Long> ids) {
    ids.add(commentId);
    List<Long> children = videoMapper.getCommentIdsByParentId(commentId);
    for (Long childId : children) {
        collectAllCommentIds(childId, ids);
    }
}*/


}
