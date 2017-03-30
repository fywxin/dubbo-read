package com.cms;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.cyou.fz.cms.api.service.NewsService;
import com.cyou.fz.search.api.SearchService;
import com.cyou.fz.services.comment.CommentService;
import com.cyou.fz.services.comment.model.Comment;
import com.cyou.fz.services.comment.model.CommentPageResponse;
import com.solr.SolrConsumer;

public class CmsConsumer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String config = CmsConsumer.class.getPackage().getName().replace('.', '/') + "/applicationContext.xml";
        System.out.println(config);
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
        
        NewsService newsService = (NewsService)context.getBean("newsServiceConsumer");
        //System.out.println(JSON.toJSONString(commentService.getCommentCountByTopicIds("003da0d4artgjjyp2bmp")));
        System.out.println(JSON.toJSONString(newsService.getNews(3566432)));
        
//        Long t = System.currentTimeMillis();
//        	comment.setContent("王金绍测试");
//            comment.setChannelId("10057");
//            comment.setAuthor("author");
//            comment.setTopicId("003da0d4artgjjyp2bmp");
//            commentService.addComment(comment);
        
        
//       t = System.currentTimeMillis();
//        for(int i=0; i<100; i++){
//        	comment.setContent("王金绍测试"+i);
//            comment.setChannelId("10057");
//            comment.setAuthor("author");
//            comment.setTopicId("003da0d4artgjjyp2bmp");
//            commentService.addComment(comment);
//        }
//        
//        System.out.println(System.currentTimeMillis()-t);
	}

}
