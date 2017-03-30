package com.cyou.fz.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
	
	/**用户ID key */
	public static final String USER_ID_KEY = "USER_ID_KEY";

	/**用户ID */
	public static final String USER_ID = "USER_ID";
	
	/**用户ID */
	public static final String ADMIN_ID = "ADMIN_ID";
	
	/**可操作权限方法合集 */
	public static final Map<String, List<String>> AUTH_MAP = new HashMap<String, List<String>>();
	
	static{
		List<String> AUTHS = new ArrayList<String>();
		AUTHS.add("$echo");
		AUTHS.add("getNextIdWithCache");
		AUTHS.add("getNextIdNoCache");
		AUTHS.add("doTimeOut");
		AUTHS.add("getTimeOutCallTimes");
		AUTHS.add("doThrowException");
		//AUTHS.add("getOutLaw");
		
		AUTH_MAP.put(USER_ID, AUTHS);
		
		List<String> authsAll = new ArrayList<String>();
		authsAll.add("$echo");
		authsAll.add("getNextIdWithCache");
		authsAll.add("getNextIdNoCache");
		authsAll.add("doTimeOut");
		authsAll.add("doThrowException");
		authsAll.add("getTimeOutCallTimes");
		authsAll.add("getOutLaw");
		AUTH_MAP.put(ADMIN_ID, authsAll);
	}
}
