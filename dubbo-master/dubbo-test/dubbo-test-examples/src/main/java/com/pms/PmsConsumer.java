package com.pms;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.cyou.fz.search.api.SearchService;
import com.cyou.fz.services.comment.CommentService;
import com.cyou.fz.services.comment.model.Comment;
import com.cyou.fz.services.comment.model.CommentPageResponse;
import com.cyou.fz.services.pms.IDepartmentService;
import com.solr.SolrConsumer;

public class PmsConsumer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String config = PmsConsumer.class.getPackage().getName().replace('.', '/') + "/applicationContext.xml";
        System.out.println(config);
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        
        IDepartmentService departmentService = (IDepartmentService)context.getBean("departmentService");

        System.out.println(JSON.toJSONString(departmentService.getDepartments()));
	}

}
