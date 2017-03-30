package com.alibaba.dubbo.examples.wjs;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Provider {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		 String config = Provider.class.getPackage().getName().replace('.', '/') + "/provider.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        
        
        System.in.read();
	}

}
