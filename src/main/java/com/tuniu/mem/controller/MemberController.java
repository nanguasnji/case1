package com.tuniu.mem.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tuniu.mem.bean.User;
import com.tuniu.mem.service.UserService;

@RestController
public class MemberController {
	
	@Autowired
	private UserService userService;

    @GetMapping("/insert")
    public String insert() {
    		User user = new User();
    		user.setId(1);
    		user.setUserName("Dick");
    		user.setEmail("zfmdick@tuniu.com");
    		user.setMobile("18602510504");
    		userService.insert(user);
        return "ok";
    }
}
