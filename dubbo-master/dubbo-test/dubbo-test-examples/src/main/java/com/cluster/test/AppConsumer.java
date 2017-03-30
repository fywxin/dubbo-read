package com.cluster.test;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.cluster.test.api.AuthKeeper;
import com.cyou.fz.search.api.SearchService;
import com.cyou.fz.services.comment.model.CommentPageResponse;
import com.solr.SolrConsumer;

public class AppConsumer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String config = AppConsumer.class.getPackage().getName().replace('.', '/') + "/applicationContext.xml";
        System.out.println(config);
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        
        RpcContext.getContext().setAttachment("appIds", ",app1,app2,");
        
        AuthKeeper authKeeper = (AuthKeeper)context.getBean("authKeeper");
        
        authKeeper.refresh("权限数据");
	}

}
