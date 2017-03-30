package com.seed.nodeb;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class NodebTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		String config = NodebTest.class.getPackage().getName().replace('.', '/') + "/dubbo-nodeb.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        System.in.read();

	}

}
