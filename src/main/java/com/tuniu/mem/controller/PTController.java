package com.tuniu.mem.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tuniu.mem.service.PerformanceService;

@RestController
public class PTController {
	
	@Autowired
	private PerformanceService performanceService;

    @GetMapping("/single")
    public String single(HttpServletRequest request) {
    		performanceService.setBySingle(request.getParameter("key"), request.getParameter("value"));
        return "ok";
    }
    
    @GetMapping("/pool")
    public String pool(HttpServletRequest request) {
    		performanceService.setByPool(request.getParameter("key"), request.getParameter("value"));
        return "ok";
    }
}
