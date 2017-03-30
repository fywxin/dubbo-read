package com.cyou.fz.api;

/**
 * 测试demo
 * @author Administrator
 *
 */
public interface DemoService {

	/**
	 * 回声测试
	 * @param word
	 * @return
	 */
	public String $echo(String word);
	
	/**
	 * 缓存测试
	 * @return
	 */
	public Long getNextIdWithCache();
	
	/**
	 * 缓存测试
	 * @return
	 */
	public Long getNextIdNoCache();
	
	/**
	 * 超时测试
	 */
	public void doTimeOut(int time);
	
	/**
	 * 超时测试被调用多少次
	 * @return
	 */
	public int getTimeOutCallTimes();
	
	/**
	 * 异常测试
	 */
	public void doThrowException();
	
	/**
	 * 权限过滤器验证
	 */
	public String getOutLaw();
	
	public String sayHello(String name);
	
}
