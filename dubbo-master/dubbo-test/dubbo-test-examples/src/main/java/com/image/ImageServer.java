package com.image;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class ImageServer {
	
	public static void main(String[] args) {
		String config = ImageServer.class.getPackage().getName().replace('.', '/') + "/applicationContext.xml";
	    ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
	    context.start();
	    System.out.println("启动成功");
	    try {
			Thread.currentThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    
	}
}
