package com.alibaba.dubbo.common.extensionloader.wjs.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.extensionloader.wjs.t3.ITest3;

public class Test3 {

	@Test
	public void t1(){
		Map<String, String> params = new HashMap<String, String>();
		params.put("key", "test3Impl2");
		URL url = new URL("test", "127.0.0.1", 9998,"/", params);
		
		ITest3 t = ExtensionLoader.getExtensionLoader(ITest3.class).getAdaptiveExtension();
		System.out.println(t.test1(url, null));
	}
	
	@Test
	public void t2(){
		Map<String, String> params = new HashMap<String, String>();
		params.put("key", "test2Impl2");
		URL url = new URL("test", "127.0.0.1", 9998,"/", params);
		
		ITest3 t = ExtensionLoader.getExtensionLoader(ITest3.class).getExtension("test3Impl3");
		System.out.println(t.test1(url, null));
	}
}
