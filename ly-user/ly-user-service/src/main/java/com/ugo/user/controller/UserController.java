package com.ugo.user.controller;

import com.ugo.user.pojo.User;
import com.ugo.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @Author: qc
 * @Date: 2019/4/23 22:49
 */
@RestController
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 校验的目的仅仅在于看此用户名有没有注册过
     * @param data
     * @param type
     * @return
     */
    @GetMapping("check/{data}/{type}")
    public ResponseEntity<Boolean> check(@PathVariable("data") String data, @PathVariable("type") Integer type){

        Boolean bool = userService.checkData(data,type);
        if (null==bool){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok(bool);
    }

    @PostMapping("code")
    public ResponseEntity<Void> sendSms(@RequestParam("phone") String phone){
        Boolean bool = userService.sendSms(phone);
        if (null==bool || !bool){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok().build();
    }

    @PostMapping("register")
    public ResponseEntity<Void> register(@Valid User user, @RequestParam("code") String code){
        Boolean bool = userService.register(user,code);
        if (null==bool||!bool){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("query")
    public ResponseEntity<User> query(String username,String password){

        User storeUser = this.userService.queryUserByUsernameAndPassword(username,password);

        if (null==storeUser){
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }


        return ResponseEntity.ok(storeUser);
    }
}
