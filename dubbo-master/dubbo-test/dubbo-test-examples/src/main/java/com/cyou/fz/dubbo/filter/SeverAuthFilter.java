package com.cyou.fz.dubbo.filter;

import java.util.List;
import java.util.Map;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.cyou.fz.dubbo.exception.ForbidVisitException;

@Activate(group=Constants.PROVIDER, order=1)
public class SeverAuthFilter implements Filter {

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		Map<String, List<String>> authMap = com.cyou.fz.api.Constants.AUTH_MAP;
		String userId = invocation.getAttachment(com.cyou.fz.api.Constants.USER_ID_KEY);
		
		List<String> auths = authMap.get(userId);
		
		String method = invocation.getMethodName();
		System.out.println("userId = "+userId+" , method = "+method);
		
		if(auths != null && auths.indexOf(method) != -1){
			return invoker.invoke(invocation);
		}
		
		RpcResult result = new RpcResult();
		result.setException(new ForbidVisitException("你无权访问"));
		
		//throw new RpcException(new ForbidVisitException("你无权访问"));
		return result;
	}
}
