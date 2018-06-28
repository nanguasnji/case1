package com.tuniu.mem.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.tuniu.mem.bean.User;
import com.tuniu.mem.dao.UserDAO;

@Repository
public class UserDAOImpl implements UserDAO {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public int insert(User user) {
		String str = "insert into user(id,user_name,email,mobile) values(?,?,?,?)";
		return jdbcTemplate.update(str, user.getId(), user.getUserName(), user.getEmail(), user.getMobile());
	}

}
