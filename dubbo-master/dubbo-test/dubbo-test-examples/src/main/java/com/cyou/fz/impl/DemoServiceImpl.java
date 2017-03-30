package com.cyou.fz.impl;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyou.fz.api.DemoService;

/**
 * 接口实现类
 * @author Administrator
 *
 */
public class DemoServiceImpl implements DemoService {
	
	private AtomicLong ID = new AtomicLong(0L);
	
	private AtomicInteger timeOutCallTimes = new AtomicInteger();
	
	/**
	 * 回声测试，
	 * 该方法不会被调用
	 */
	@Override
	public String $echo(String word) {
		return "我不是回声！";
	}

	/**
	 * 超时处理
	 */
	@Override
	public void doTimeOut(int time) {
		if(time > 0){
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {}
		}
		timeOutCallTimes.incrementAndGet();
	}
	
	public int getTimeOutCallTimes(){
		return timeOutCallTimes.get();
	}

	/**
	 * 除0异常
	 */
	@Override
	public void doThrowException() {
		long i=1l;
		i = ID.get()/(i-1);
	}

	@Override
	public Long getNextIdWithCache() {
		return ID.incrementAndGet();
	}

	@Override
	public Long getNextIdNoCache() {
		return ID.incrementAndGet();
	}

	@Override
	public String getOutLaw() {
		return "访问getOutLaw 成功";
	}

	@Override
	public String sayHello(String name) {
		// TODO Auto-generated method stub
		return "你好，"+name;
	}

}
