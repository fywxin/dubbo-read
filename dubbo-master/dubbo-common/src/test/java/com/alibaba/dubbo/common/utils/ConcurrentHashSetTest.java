package com.alibaba.dubbo.common.utils;

import org.junit.Test;

public class ConcurrentHashSetTest {

	@Test
	public void testSort(){
		ConcurrentHashSet set = new ConcurrentHashSet();
		
		set.add(1);
		set.add(2);
		set.add(3);
		set.add(4);
		set.add(6);
		set.add(5);
		
		
		for(Object obj : set.toArray()){
			System.out.println(obj);
		}
		
	}
}
