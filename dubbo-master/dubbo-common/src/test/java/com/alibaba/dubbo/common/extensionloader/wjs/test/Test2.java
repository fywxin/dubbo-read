package com.alibaba.dubbo.common.extensionloader.wjs.test;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.extensionloader.wjs.t2.ITest2;

public class Test2 {

	@Test
	public void t1(){
		Map<String, String> params = new HashMap<String, String>();
		params.put("key", "test2Impl1");
		URL url = new URL("test", "127.0.0.1", 9998,"/", params);
		
		System.out.println("key = "+ url.getParameter("key"));
		ITest2 t2 = ExtensionLoader.getExtensionLoader(ITest2.class).getAdaptiveExtension();
		//t2.echo1(); 报错，不能被调用
		System.out.println(t2.test1(url, ""));
		
		System.out.println();
		System.out.println("--------------------在被加载类对象Test2Impl1 重新设置URL值----------------------");
		System.out.println("key = "+ url.getParameter("key"));
		System.out.println("对象不变： "+t2.test1(url, ""));
		t2 = ExtensionLoader.getExtensionLoader(ITest2.class).getAdaptiveExtension();
		System.out.println();
		System.out.println("key = "+ url.getParameter("key"));
		System.out.println("对象重新加载： "+t2.test1(url, ""));
		
		System.out.println();
		System.out.println("--------------------在外部重新设置URL值----------------------");
		url = url.addParameter("key", "test2Impl2");
		System.out.println("key = "+ url.getParameter("key"));
		t2 = ExtensionLoader.getExtensionLoader(ITest2.class).getAdaptiveExtension();
		System.out.println(t2.test1(url, ""));
		//t2.echo1();
		t2 = ExtensionLoader.getExtensionLoader(ITest2.class).getExtension("test2Impl2");
		t2.echo1();
		t2 = ExtensionLoader.getExtensionLoader(ITest2.class).getExtension("test2Impl3");
		t2.echo1();
	}
	
	@Test
	public void t2(){
		Map<String, String> params = new HashMap<String, String>();
		params.put("key", "test2Impl2");
		URL url = new URL("test", "127.0.0.1", 9998,"/", params);
		
		ITest2 t22 = ExtensionLoader.getExtensionLoader(ITest2.class).getAdaptiveExtension();
		ITest2 t21 = ExtensionLoader.getExtensionLoader(ITest2.class).getAdaptiveExtension();
		System.out.println(t21 == t22);
		System.out.println(t22.test1(url, ""));
		
		System.out.println();
		ITest2 t23 = ExtensionLoader.getExtensionLoader(ITest2.class).getExtension("test2Impl2");
		ITest2 t24 = ExtensionLoader.getExtensionLoader(ITest2.class).getExtension("test2Impl2");
		System.out.println(t22 == t23);
		System.out.println(t22.equals(t23));
		System.out.println(t22.test1(url, ""));
		System.out.println(t24 == t23);
	}
}
