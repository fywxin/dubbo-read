package com.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.cyou.fz.services.search.api.IndexService;
import com.cyou.fz.services.search.api.SearchService;
import com.cyou.fz.services.search.beans.QueryResultEntity;
import com.cyou.fz.services.search.beans.Response;
import com.cyou.fz.services.search.beans.UpdateResultEntity;
import com.pic.PicConsumer;

/**
 * @author 王金绍
 * @date 2013-9-29 下午4:56:48 
 */
public class MainSearchClientServer {

	public static void main(String[] args) throws IOException {
		String config = MainSearchClientServer.class.getPackage().getName().replace('.', '/') + "/applicationContext.xml";
        System.out.println(config);
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(config);
        context.start();
//        SearchService searchService = (SearchService)context.getBean("searchService");
//    	Response<QueryResultEntity> rs = searchService.search("WEBPAGE","*.*", null);
//        System.out.println(JSON.toJSONString(rs));
        IndexService indexService = (IndexService)context.getBean("indexService");
        ArrayList<Map<String, Object>> docs = new ArrayList<Map<String,Object>>();
        Map<String, Object> map1 = new HashMap<String, Object>();
        map1.put("tid", "1");
        map1.put("fid", "26");
        map1.put("posttableid", "3");
        map1.put("author", "ljp_01");
        map1.put("authorid", "26959411");
        docs.add(map1);
        System.out.println(JSON.toJSONString(docs));
        Response<UpdateResultEntity> rs = indexService.build("BBS_THREAD", docs);
        
        System.out.println(JSON.toJSONString(rs));
        System.out.println("启动成功");    
	}
}

