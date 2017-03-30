package com.alibaba.dubbo.common.extensionloader.wjs.t3.impl;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.common.extensionloader.wjs.t3.ITest3;

public class Test3Impl1 implements ITest3 {

	@Override
	public String test1(URL url, String s) {
		return "Test3Impl1";
	}

	@Override
	public void echo1() {
		System.out.println("echo1");

	}

}
