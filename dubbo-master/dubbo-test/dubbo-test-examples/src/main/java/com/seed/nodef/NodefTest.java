package com.seed.nodef;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class NodefTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
	        String config = NodefTest.class.getPackage().getName().replace('.', '/') + "/dubbo-nodef.xml";
	        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
	        context.start();
	        System.in.read();
	}

}
