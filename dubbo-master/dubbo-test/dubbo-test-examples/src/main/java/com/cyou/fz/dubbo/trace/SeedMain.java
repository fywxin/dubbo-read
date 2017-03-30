package com.cyou.fz.dubbo.trace;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SeedMain {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String config = SeedMain.class.getPackage().getName().replace('.', '/') + "/dubbo-app.xml";
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        System.in.read();
	}

}
