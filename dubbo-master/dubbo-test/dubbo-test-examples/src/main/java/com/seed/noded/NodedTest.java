package com.seed.noded;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class NodedTest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String config = NodedTest.class.getPackage().getName().replace('.', '/') + "/dubbo-noded.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        System.in.read();

	}

}
