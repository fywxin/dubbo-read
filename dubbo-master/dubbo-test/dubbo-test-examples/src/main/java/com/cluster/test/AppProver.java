package com.cluster.test;

import java.io.IOException;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.cyou.fz.search.api.SearchService;
import com.cyou.fz.services.comment.model.CommentPageResponse;
import com.solr.SolrConsumer;

public class AppProver {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String config = AppProver.class.getPackage().getName().replace('.', '/') + "/applicationContext.xml";
        System.out.println(config);
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        System.in.read();

	}

}
