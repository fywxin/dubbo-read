package com.alibaba.dubbo.examples.wjs.impl;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.alibaba.dubbo.examples.wjs.api.ITest1;
import com.alibaba.dubbo.examples.wjs.common.MyBean;

public class Test1Impl implements ITest1 {
	
	private AtomicLong ID = new AtomicLong();

	@Override
	public Long save(MyBean bean) {
		return ID.getAndIncrement();
	}

	@Override
	public Boolean update(MyBean bean) {
		return true;
	}

	@Override
	public Boolean delete(Long[] ids) {
		return false;
	}

	@Override
	public MyBean get(Long id) {
		MyBean bean = new MyBean();
		bean.setId(id);
		bean.setAge(1);
		//bean.setIsLink("true");
		bean.setName("name");
		bean.setSex(true);
		return bean;
	}

	@Override
	public List<MyBean> getAll() {
		return null;
	}

}
