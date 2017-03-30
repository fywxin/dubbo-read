package com.alibaba.dubbo.common.extensionloader.wjs.t3;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;

@SPI
public interface ITest3 {
	String test1(URL url, String s);

	// @Adaptive 无参数URL
	void echo1();
}
