package com.pic;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.cyou.fz.search.api.SearchService;
import com.cyou.fz.services.comment.model.CommentPageResponse;
import com.cyou.fz.services.pic.PicService;
import com.solr.SolrConsumer;

public class PicConsumer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String config = PicConsumer.class.getPackage().getName().replace('.', '/') + "/applicationContext.xml";
        System.out.println(config);
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        
        PicService picService = (PicService)context.getBean("picService");
        
       System.out.println(picService.getCDNUrlByDomain("sdfas"));

	}

}
