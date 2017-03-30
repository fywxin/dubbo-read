package com.alibaba.dubbo.common.extensionloader.wjs.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.extensionloader.wjs.t4.IActivateTest;

public class ActivateTest {

	@Test
	public void test(){
		Map<String, String> params = new HashMap<String, String>();
		params.put("key", "activateTestImpl1");
		URL url = new URL("test", "127.0.0.1", 9998,"/", params);
		List<IActivateTest> list = ExtensionLoader.getExtensionLoader(IActivateTest.class).getActivateExtension(url, "key");
		for(IActivateTest test : list){
			test.noUrl();
		}
		
	}
}
