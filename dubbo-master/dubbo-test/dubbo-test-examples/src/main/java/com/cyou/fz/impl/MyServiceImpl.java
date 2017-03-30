package com.cyou.fz.impl;

import com.cyou.fz.api.MyService;

public class MyServiceImpl implements MyService {

	@Override
	public String hello(String word) {
		return "你好，世界";
	}

}
