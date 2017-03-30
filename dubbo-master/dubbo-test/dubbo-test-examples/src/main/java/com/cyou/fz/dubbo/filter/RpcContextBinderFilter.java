package com.cyou.fz.dubbo.filter;

import java.util.Map;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;

@Activate(group=Constants.CONSUMER, order=130001)
public class RpcContextBinderFilter implements Filter {

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation)
			throws RpcException {
		Map<String, String> attachments = RpcContext.getContext().getAttachments();
		System.out.println("RpcContextBinderFilter.attachments = "+attachments);
		
		if(attachments != null && attachments.size() > 0){
			invocation.getAttachments().putAll(attachments);
		}
		
		return invoker.invoke(invocation);
	}

}
