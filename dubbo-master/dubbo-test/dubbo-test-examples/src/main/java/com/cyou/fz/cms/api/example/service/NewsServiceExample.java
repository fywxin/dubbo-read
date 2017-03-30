package com.cyou.fz.cms.api.example.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.cyou.fz.cms.api.commons.dto.CmsQueryResult;
import com.cyou.fz.cms.api.service.NewsService;
import com.cyou.fz.cms.api.service.dto.NewsDTO;
import com.solr.SolrConsumer;

/**
 * 新闻实例.
 * 
 */
public class NewsServiceExample {
	
	/**
	 * 查询新闻实例.
	 * @return 返回单个新闻信息.
	 */
	public  NewsDTO getNews(){
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:comsumer.xml");
		context.start();
		NewsService newsService = (NewsService) context.getBean("newsService");
		//需要传递的参数，新闻Id
		int newsId = 123;
		return newsService.getNews(newsId);
	}
	
	public static void main(String[] args) {
		String config = NewsServiceExample.class.getPackage().getName().replace('.', '/')+ "/comsumer.xml";
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
		context.start();
		NewsService newsService = (NewsService) context.getBean("newsService");
		
		NewsDTO dto = newsService.getNews(3450966);
		System.out.println("节诶过 == "+JSON.toJSONString(dto));
		// 设置条件，注释掉的代码是例子参考。
		Map<String, String> condition = new HashMap<String, String>();
		condition.put("newsChannel", "10009");
		condition.put("newsClass", "");
		condition.put("newsKind", "");
		condition.put("keyWord", "");
		CmsQueryResult<NewsDTO> rs = newsService.queryNews(condition,1, 10);
		System.out.println("节诶过 == "+JSON.toJSONString(rs));
	}
}
