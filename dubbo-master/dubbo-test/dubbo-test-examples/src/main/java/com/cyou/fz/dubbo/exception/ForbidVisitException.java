package com.cyou.fz.dubbo.exception;

/**
 * 无权访问
 * @author Administrator
 *
 */
public class ForbidVisitException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public ForbidVisitException(String msg){
		super(msg);
	}
	
	public ForbidVisitException(String msg, Throwable e){
		super(msg, e);
	}

}
