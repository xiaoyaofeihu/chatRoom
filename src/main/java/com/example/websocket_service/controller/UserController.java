package com.example.websocket_service.controller;


import com.example.websocket_service.config.Base64StrToImage;
import com.example.websocket_service.config.RestResponse;
import com.example.websocket_service.entity.BASE64DecodedMultipartFile;
import com.example.websocket_service.entity.form.ComplaintInfoF;
import com.example.websocket_service.entity.form.UserServiceF;
import com.example.websocket_service.entity.vo.UserVo;
import com.example.websocket_service.service.ComplaintService;
import com.example.websocket_service.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MessageService messageService;

    @Autowired
    private ComplaintService complaintService;


    /**
     * 获取用户信息
     */
    @GetMapping
    public RestResponse getUserList() {
        List<UserVo> userList = new LinkedList<>();
        Set<String> keys = redisTemplate.keys("wait_"+"*");
        for (String key : keys) {
            UserVo userVo = new UserVo();
            String userName = key.split("_")[2];
            Double score = redisTemplate.opsForZSet().score(key, userName);
            if (Objects.nonNull(score)) {
                Date date = new Date(score.longValue());
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String createTime = format.format(date);
                String roomId = key.split("_")[1];
                userVo.setRoomId(roomId);
                userVo.setUserName(userName);
                userVo.setCreatedTime(createTime);
                userList.add(userVo);
            }
        }
//        Set<ZSetOperations.TypedTuple<String>> key = redisTemplate.opsForZSet().rangeWithScores("key", 0, -1);
//        Iterator<ZSetOperations.TypedTuple<String>> iterator = key.iterator();
//        while (iterator.hasNext()) {
//            UserVo userVo = new UserVo();
//            ZSetOperations.TypedTuple<String> typeTuple = iterator.next();
//            String value = typeTuple.getValue();
//            Double score = typeTuple.getScore();
//            if (Objects.nonNull(score)) {
//                Date date = new Date(score.longValue());
//                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                String createTime = format.format(date);
//                String roomId = value.split("_")[1];
//                String userName = value.split("_")[2];
//                userVo.setRoomId(roomId);
//                userVo.setUserName(userName);
//                userVo.setCreatedTime(createTime);
//                userList.add(userVo);
//            }
//            System.out.println("获取的区间值"+value+"===="+score);
//        }
        //实现排序
//        Collections.sort(userList, new Comparator<UserVo>() {
//            @SneakyThrows
//            @Override
//            public int compare(UserVo o1, UserVo o2) {
//                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                return (int) (format.parse(o1.getCreatedTime()).getTime()-format.parse(o2.getCreatedTime()).getTime());
//            }
//        });

        return new RestResponse().put(userList);
//        return ApiResult.ok().data(userList);
    }

    @GetMapping("/recordList")
    public RestResponse getUserServiceList(@Valid @ModelAttribute UserServiceF form) {
//        RestResponse user = feignService.user();
//        System.out.println(user);
        return new RestResponse().putApp(messageService.getServiceList(form));
    }

    //投诉记录
    @PostMapping("/complaint")
    public RestResponse complaintInfo(@RequestBody ComplaintInfoF form) throws Exception {
        BASE64DecodedMultipartFile base64DecodedMultipartFile = null;
        if (Objects.nonNull(form.getPicture())) {
            base64DecodedMultipartFile = (BASE64DecodedMultipartFile) Base64StrToImage.base64MutipartFile(form.getPicture());
            if (Objects.nonNull(base64DecodedMultipartFile)) {
                form.setPicture1(base64DecodedMultipartFile);
            }
        }
        Boolean result = complaintService.insert(form);
        return new RestResponse().putApp(result);
    }
}

