package com.cyou.fz.dubbo.cache.encache;

import net.sf.ehcache.CacheManager;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.cache.support.AbstractCacheFactory;
import com.alibaba.dubbo.common.URL;

public class EncacheFactory extends AbstractCacheFactory {

	private CacheManager cacheManager;
	
	@Override
	protected Cache createCache(URL url) {
		net.sf.ehcache.Ehcache ehcache = cacheManager.addCacheIfAbsent(url.toFullString());
		return new Encache(ehcache);
	}

	public void setCacheManager(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}
}
