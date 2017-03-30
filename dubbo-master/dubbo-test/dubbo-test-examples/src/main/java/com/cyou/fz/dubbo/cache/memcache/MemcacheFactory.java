package com.cyou.fz.dubbo.cache.memcache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.cache.support.AbstractCacheFactory;
import com.alibaba.dubbo.common.URL;

public class MemcacheFactory extends AbstractCacheFactory {

	private Imemcache imemcache;
	
	public void setImemcache(Imemcache imemcache) {
		this.imemcache = imemcache;
	}
	
	@Override
	protected Cache createCache(URL url) {
		Map<Object, Object> map = new ConcurrentHashMap<Object, Object>();
		String key = url.getParameter("side")+"-"+url.getProtocol()+":"+url.getPath()+"."+url.getParameter("method")+"-"+url.getParameter("version");
		imemcache.set(key, map);
		return new Memcache(map);
	}

}
