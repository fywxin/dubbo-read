/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cyou.fz.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.cyou.fz.api.Constants;
import com.cyou.fz.api.DemoService;
import com.cyou.fz.dubbo.exception.ForbidVisitException;

public class DemoConsumer {
	
	private static DemoService demoService = null;

	static {
		init();
	}

	private static void init() {
		String config = DemoConsumer.class.getPackage().getName().replace('.', '/')+ "/demo-consumer.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
		context.start();
		DemoService demoService = (DemoService) context.getBean("demoService");
		DemoConsumer.demoService = demoService;
	}

	public static void testEcho() {
		String backWord = demoService.$echo("你好，回声！");
		System.out.println("回声测试返回：" + backWord);
	}

	public static void testTimeOut(int time) {
		try {
			demoService.doTimeOut(time);
			System.out.println("TimeOut 执行了 ："+demoService.getTimeOutCallTimes()+" 次");
		} catch (RpcException e) {
			e.printStackTrace();
		}
	}

	public static void testException() {
		demoService.doThrowException();
	}

	public static void testAdaptive() {
		System.out.println("无缓存第1次取ID：" + demoService.getNextIdNoCache());
		System.out.println("无缓存第2次取ID：" + demoService.getNextIdNoCache());
		System.out.println("无缓存第3次取ID：" + demoService.getNextIdNoCache());
		System.out.println("无缓存第4次取ID：" + demoService.getNextIdNoCache());

		System.out.println("缓存第1次取ID：" + demoService.getNextIdWithCache());
		System.out.println("缓存第2次取ID：" + demoService.getNextIdWithCache());
		System.out.println("缓存第3次取ID：" + demoService.getNextIdWithCache());
		System.out.println("缓存第4次取ID：" + demoService.getNextIdWithCache());
	}

	public static void testAuthFliter() {
		try {
			RpcContext.getContext().setAttachment(Constants.USER_ID_KEY, Constants.ADMIN_ID);
			System.out.println("访问getOutLaw ：" + demoService.getOutLaw());
			//System.out.println("继续getOutLaw ：" + demoService.getOutLaw());
		} catch (ForbidVisitException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static String test(String name) {
		return demoService.sayHello(name);
	}

	public static void main(String[] args) throws Exception {
		System.out.println(DemoConsumer.test("asdf"));
	}
}
