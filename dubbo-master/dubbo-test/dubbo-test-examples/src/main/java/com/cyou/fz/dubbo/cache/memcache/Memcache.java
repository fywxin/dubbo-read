package com.cyou.fz.dubbo.cache.memcache;

import java.util.Map;

import com.alibaba.dubbo.cache.Cache;

public class Memcache implements Cache {
	
	private Map<Object, Object> map;

	public Memcache(Map<Object, Object> map){
		this.map = map;
	}
	
	@Override
	public void put(Object key, Object value) {
		map.put(key, value);
	}

	@Override
	public Object get(Object key) {
		System.out.println("Memcache 缓存命中！");
		return map.get(key);
	}

}
