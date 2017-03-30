/*
 * Copyright 1999-2012 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.solr;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.dubbo.common.json.JSON;
import com.cyou.fz.search.api.IndexService;
import com.cyou.fz.search.api.SearchService;
import com.cyou.fz.search.beans.QueryParams;
import com.cyou.fz.search.beans.QueryResultEntity;
import com.cyou.fz.search.beans.Response;
import com.cyou.fz.search.beans.UpdateResultEntity;

/**
 * CacheConsumer
 * 
 * @author william.liangf
 */
public class SolrConsumer {

	public static void main(String[] args) throws Exception {
		
		String str ="{\"fq\":\"game_classid:1\",\"sort\":{\"order_time\":\"desc\"},\"fl\":null,\"df\":null,\"start\":0,\"rows\":40,\"highLights\":null,\"referer\":null}";
		
		String config = SolrConsumer.class.getPackage().getName()
				.replace('.', '/')
				+ "/applicationContext.xml";
		System.out.println(config);
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
				config);
		context.start();

		IndexService indexServic = (IndexService) context
				.getBean("indexServic");
		QueryParams param = new QueryParams();

		param.setRows(10);
		param.setStart(1);
		Map<String, String> sort = new HashMap<String, String>();
		sort.put("game_code", "asc");
		param.setSort(sort);
		Map<String, String> map = null;
		Long start ;
		for (int i = 0; i < 1000; i++) {
			start = new Date().getTime();
			map = new HashMap<String, String>();

			map.put("gift_id", String.valueOf(1275 + i));
			map.put("gift_kind", "1");
			map.put("game_code", "11083");
			map.put("gift_state", "1");
			map.put("game_face", "7");
			map.put("game_type", "10");
			map.put("game_area", "21");
			map.put("game_theme", "14");
			map.put("game_engine", "0");
			map.put("game_fightmode", "0");
			map.put("game_feature", "");
			map.put("game_language", "");
			map.put("game_platform", "");
			map.put("game_language_data", "");
			map.put("game_platform_data", "");
			map.put("game_test", "0");
			map.put("game_img",
					"http://i5.17173.itc.cn/2013/uploads/fzct01/newgame/images/2013/08/13765382604052.jpg");
			map.put("game_name", "征途2");
			map.put("total_count", "3757203");
			map.put("send_kind", "1");
			map.put("game_classid", "1");
			map.put("gift_name", "征途2 17173玄兽卡");
			map.put("stock", "0");
			map.put("fahao_count", "3757203");
			map.put("gift_kind_name", "新手卡");
			map.put("gift_pic",
					"http://i3.17173.itc.cn/2012/newgame/04/1311410634.jpg");
			map.put("gift_content",
					"<p>新手卡奖励：2件时装、两个缩小药水、2个11、2个77、2个变身精魄、50个45级玄兽升级宝石<br />【17173玄兽卡是所有征途2新手卡中的霸王，不用想了！赶紧领取吧。】&nbsp;</p>");
			map.put("use_demo",
					"<p>【17173玄兽卡可以和白金至尊卡一起使用】<span style=\"text-decoration: underline;\"><a href=\"http://hao.17173.com/gift-info-1756.html\" target=\"_blank\">点击领取白金至尊卡</a></span><br /><br />登陆游戏后， 找清源村NPC&ldquo;新手使者&rdquo;（162,119），填写新手卡卡号，验证后即可领取。&nbsp;</p>");
			map.put("start_time", "1307203200");
			map.put("end_time", "1388505599");
			map.put("login_check", "0");
			map.put("operator_id_query", "0");
			map.put("operator_id", "0");
			map.put("stock_percent", "0");
			map.put("add_time", "1307548800");
			map.put("order_time", "1336464727");

			map.put("last_mod_time", "1307548800");
			map.put("send_time", "1337789130");
			map.put("game_url", "http://zt2.17173.com");
			map.put("gift_url", "http://dev.hao.17173.com/gift-info-1375.html");

			// map.put("gift_id", "1094");
			// map.put("gift_kind", "1");
			// map.put("game_code", "11210");
			// map.put("gift_state", "0");
			// map.put("game_face", "7");
			// map.put("game_type", "10");
			// map.put("game_area", "21");
			// map.put("game_theme", "17");
			// map.put("game_engine", "0");
			// map.put("game_fightmode", "0");
			// map.put("game_feature", "");
			// map.put("game_language", "");
			// map.put("game_platform", "");
			// map.put("game_language_data", "");
			// map.put("game_platform_data", "");
			// map.put("game_test", "0");
			// map.put("game_img",
			// "http://images.17173.com/2010/newgame/03/031910475739.jpg");
			// map.put("game_name", "蓬莱");
			// map.put("total_count", "6844");
			//
			// map.put("send_kind", "5");
			// map.put("game_classid", "1");
			// map.put("gift_name", "蓬莱 媒体新手卡");
			// map.put("stock", "0");
			// map.put("fahao_count", "6844");
			// map.put("gift_kind_name", "新手卡");
			// map.put("gift_pic",
			// "http://images.17173.com/2010/newgame/03/031910475739.jpg");
			// map.put("gift_content",
			// "<p>新手卡奖励：2件时装、两个缩小药水、2个11、2个77、2个变身精魄、50个45级玄兽升级宝石<br />【17173玄兽卡是所有征途2新手卡中的霸王，不用想了！赶紧领取吧。】&nbsp;</p>");
			// map.put("use_demo",
			// "<p>【17173玄兽卡可以和白金至尊卡一起使用】<span style=\"text-decoration: underline;\"><a href=\"http://hao.17173.com/gift-info-1756.html\" target=\"_blank\">点击领取白金至尊卡</a></span><br /><br />登陆游戏后， 找清源村NPC&ldquo;新手使者&rdquo;（162,119），填写新手卡卡号，验证后即可领取。&nbsp;</p>");
			// map.put("start_time", "1337734951");
			// map.put("end_time", "1337734956");
			//
			// map.put("login_check", "1");
			//
			// map.put("operator_id_query", "0");
			// map.put("operator_id", "0");
			// map.put("stock_percent", "0");
			// map.put("add_time", "1307548800");
			// map.put("order_time", "1336464727");
			//
			// map.put("last_mod_time", "1358928458");
			// map.put("send_time", "1337789130");
			// map.put("game_url", "http://zt2.17173.com");
			// map.put("gift_url",
			// "http://dev.hao.17173.com/gift-info-1375.html");

			Response<UpdateResultEntity> resp = indexServic.add("HAO", map);
			System.out.print(i+" 耗时："+String.valueOf((new Date().getTime()-start)));
			System.out.println(" 返回："+String.valueOf((resp == null ? "NULL" : resp.isFlag())));
		}

		indexServic.commit("HAO", false, false);
	}

}
