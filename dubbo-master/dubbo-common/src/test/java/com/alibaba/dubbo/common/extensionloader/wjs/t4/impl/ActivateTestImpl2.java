package com.alibaba.dubbo.common.extensionloader.wjs.t4.impl;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.extensionloader.wjs.t4.IActivateTest;
import com.alibaba.dubbo.common.extensionloader.wjs.t4.Inf2;
import com.alibaba.dubbo.common.extensionloader.wjs.t4.Inf3;

@Activate
public class ActivateTestImpl2 implements Inf2,IActivateTest,Inf3 {

	@Override
	public void noUrl() {
		System.out.println("2");
		
	}

	@Override
	public void hasUrl(URL url) {
		// TODO Auto-generated method stub
		
	}

}
