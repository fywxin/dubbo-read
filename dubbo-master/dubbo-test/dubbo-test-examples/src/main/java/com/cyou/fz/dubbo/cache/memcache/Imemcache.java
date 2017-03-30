package com.cyou.fz.dubbo.cache.memcache;

public interface Imemcache {

	public Object get(Object key);
	
	public void set(Object key, Object value);
	
	public void delete(Object key);
	
}
