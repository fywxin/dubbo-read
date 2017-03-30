package com.alibaba.dubbo.common.extensionloader.wjs.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionFactory;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.extensionloader.wjs.t1.ITest1;

public class Test1 {

	@Test
	public void test(){
		ITest1 t = ExtensionLoader.getExtensionLoader(ITest1.class).getAdaptiveExtension();
		System.out.println(t.test1(null, ""));
		t.echo1();
	}
	
	@Test
	public void test2(){
		ITest1 t = ExtensionLoader.getExtensionLoader(ITest1.class).getDefaultExtension();
		System.out.println(t.test1(null, ""));
		t.echo1();
	}
	
	@Test
	public void test3(){
		ITest1 t = ExtensionLoader.getExtensionLoader(ITest1.class).getExtension("test1Impl3");
		System.out.println(t.test1(null, ""));
		t.echo1();
	}
	
	@Test
	public void test4(){
		ITest1 t = ExtensionLoader.getExtensionLoader(ITest1.class).getAdaptiveExtension();
		System.out.println(t.test1(null, ""));
		t.echo1();
		
		Map<String, String> params = new HashMap<String, String>();
		params.put("key", "test1Impl2");
		URL url = new URL("test", "127.0.0.1", 9998,"/", params);
		t = ExtensionLoader.getExtensionLoader(ITest1.class).getAdaptiveExtension();
		System.out.println(t.test1(url, ""));
		t.echo1();
	}
	
	@Test
	public void test5(){
		Map<String, String> params = new HashMap<String, String>();
		params.put("key", "");
		URL url = new URL("test", "127.0.0.1", 9998,"/", params);
		ITest1 t = ExtensionLoader.getExtensionLoader(ITest1.class).getAdaptiveExtension();
		System.out.println(t.test1(url, ""));
	}
	
	@Test
	public void test6(){
		ExtensionFactory factory = ExtensionLoader.getExtensionLoader(ExtensionFactory.class).getAdaptiveExtension();
		//factory.getExtension(type, name)
	}
}
