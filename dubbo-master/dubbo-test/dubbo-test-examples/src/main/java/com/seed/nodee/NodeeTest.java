package com.seed.nodee;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class NodeeTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
	        String config = NodeeTest.class.getPackage().getName().replace('.', '/') + "/dubbo-nodee.xml";
	        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
	        context.start();
	        System.in.read();
	}

}
