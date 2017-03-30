package com.seed.nodec;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class NodecTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		String config = NodecTest.class.getPackage().getName().replace('.', '/') + "/dubbo-nodec.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        System.in.read();

	}

}
