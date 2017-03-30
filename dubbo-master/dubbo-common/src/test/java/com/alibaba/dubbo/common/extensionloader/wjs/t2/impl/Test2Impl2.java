package com.alibaba.dubbo.common.extensionloader.wjs.t2.impl;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extensionloader.wjs.t2.ITest2;

public class Test2Impl2 implements ITest2 {

	@Override
	public String test1(URL url, String s) {
		return "Test2Impl2";
	}

	@Override
	public void echo1() {
		System.out.println("echo2");

	}

}
