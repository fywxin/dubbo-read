package com.alibaba.dubbo.examples.wjs.api;

import java.util.List;

import com.alibaba.dubbo.examples.wjs.common.MyBean;

public interface ITest1 {

	Long save(MyBean bean);
	
	Boolean update(MyBean bean);
	
	Boolean delete(Long[] ids);
	
	MyBean get(Long id);
	
	List<MyBean> getAll();
	
}
