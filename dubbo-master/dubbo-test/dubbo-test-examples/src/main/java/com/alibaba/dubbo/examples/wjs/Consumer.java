package com.alibaba.dubbo.examples.wjs;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.examples.wjs.api.ITest1;

public class Consumer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		 String config = Consumer.class.getPackage().getName().replace('.', '/') + "/consumer.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        ITest1 t = (ITest1)context.getBean("test1ServiceRef");
        System.out.println(t.get(1L));
        System.in.read();
	}

}
