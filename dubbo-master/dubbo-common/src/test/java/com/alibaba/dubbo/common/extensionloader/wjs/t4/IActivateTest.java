package com.alibaba.dubbo.common.extensionloader.wjs.t4;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.SPI;

@SPI
public interface IActivateTest {

	void noUrl();
	
	void hasUrl(URL url);
}
