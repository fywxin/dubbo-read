package com.seed.nodea;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class NodeaTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		String config = NodeaTest.class.getPackage().getName().replace('.', '/') + "/dubbo-nodea.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        
        NodeaClient nodeaClient = (NodeaClient) context.getBean("nodeaClient");
        System.out.println(nodeaClient.needBC("开始"));
        
        System.in.read();

	}

}
