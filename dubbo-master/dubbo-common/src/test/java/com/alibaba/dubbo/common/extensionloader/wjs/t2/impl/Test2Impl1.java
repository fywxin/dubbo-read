package com.alibaba.dubbo.common.extensionloader.wjs.t2.impl;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.common.extensionloader.wjs.t2.ITest2;


public class Test2Impl1 implements ITest2 {

	@Override
	public String test1(URL url, String s) {
		url = url.addParameter("key", "test2Impl2");
		System.out.println("在Test2Impl1 修改key值 = "+url.getParameter("key"));
		return "Test2Impl1";
	}

	@Override
	public void echo1() {
		System.out.println("echo1");

	}

}
