package com.alibaba.dubbo.common.extensionloader.wjs.t1.impl;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extensionloader.wjs.t1.ITest1;

//@Adaptive
public class Test1Impl3 implements ITest1 {

	@Override
	public String test1(URL url, String s) {
		return "3";
	}

	@Override
	public void echo1() {
		System.out.println("echo3");
	}

}
