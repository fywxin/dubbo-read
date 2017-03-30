package com.cyou.fz.cms.api.example.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.cyou.fz.cms.api.commons.dto.CmsQueryResult;
import com.cyou.fz.cms.api.service.NewsService;
import com.cyou.fz.cms.api.service.VlogService;
import com.cyou.fz.cms.api.service.dto.NewsDTO;
import com.cyou.fz.cms.api.service.dto.VlogDTO;

/**
 * Vlog实例.
 * 
 */
public class VlogServiceExample {
	/**
	 * 查询Vlog实例.
	 * @return 返回Vlog列表信息.
	 * 集合 key 查询参数值 channelId：频道编号 
	 *                   tag: 对应关键字
	 *                   gamecode : 游戏编号.
	 */
	public  CmsQueryResult<VlogDTO> queryNews() {
		//配置文件的读取可以直接读取，也可以使用Spring的配置文件读取。
		//如果不用Spring，也可以直接采用dubbo中提供的非Spring方式读取。
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				"classpath:comsumer.xml");
		context.start();
		VlogService vlogService = (VlogService) context.getBean("VlogService");
		// 设置条件，注释掉的代码是例子参考。
		Map<String, String> condition = new HashMap<String, String>();
		condition.put("channelId", "10065");
//		condition.put("tag", "");
//		condition.put("gamecode", "");
		return  vlogService.queryVlog(condition, 1, 10);
	}
	
	
}
