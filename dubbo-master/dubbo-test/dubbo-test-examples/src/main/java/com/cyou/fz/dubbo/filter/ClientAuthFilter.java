package com.cyou.fz.dubbo.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

@Activate(group=Constants.CONSUMER, order=-100000)
public class ClientAuthFilter implements Filter {

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation)
			throws RpcException {
		invocation.getAttachments().put(com.cyou.fz.api.Constants.USER_ID_KEY, com.cyou.fz.api.Constants.ADMIN_ID);
		
		return invoker.invoke(invocation);
	}
}
