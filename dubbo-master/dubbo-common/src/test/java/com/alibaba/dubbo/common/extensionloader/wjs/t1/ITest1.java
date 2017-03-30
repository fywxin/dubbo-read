package com.alibaba.dubbo.common.extensionloader.wjs.t1;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;

@SPI("test1Impl2")
public interface ITest1 {

	@Adaptive("key")
	String test1(URL url, String s);
	
	void echo1();
}
