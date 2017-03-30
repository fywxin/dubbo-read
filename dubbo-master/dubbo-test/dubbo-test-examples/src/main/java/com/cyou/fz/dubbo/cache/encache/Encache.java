package com.cyou.fz.dubbo.cache.encache;

import net.sf.ehcache.Element;

import com.alibaba.dubbo.cache.Cache;

public class Encache implements Cache {
	
	private net.sf.ehcache.Ehcache ehcache;
	
	public Encache(net.sf.ehcache.Ehcache ehcache){
		this.ehcache = ehcache;
	}
	
	@Override
	public void put(Object key, Object value) {
		ehcache.put(new Element(key, value));
	}

	@Override
	public Object get(Object key) {
		Element element = ehcache.get(key);
		if(element == null)
			return null;
		System.out.println("ehcache 缓存命中！");
		return element.getObjectValue();
	}
}
