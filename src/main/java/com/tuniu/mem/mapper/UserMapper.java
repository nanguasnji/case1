package com.tuniu.mem.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.tuniu.mem.bean.User;

@Mapper
public interface UserMapper {
	public void insert(User user);
}
