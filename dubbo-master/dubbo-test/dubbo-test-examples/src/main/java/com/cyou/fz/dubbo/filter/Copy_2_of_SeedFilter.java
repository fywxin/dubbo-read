package com.cyou.fz.dubbo.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.cyou.fz.dubbo.exception.ForbidVisitException;

@Activate(group = { Constants.PROVIDER, Constants.CONSUMER })
public class Copy_2_of_SeedFilter implements Filter {

private static final String SEED = "seed";
	
	private static final ThreadLocal<String> LOCAL = new ThreadLocal<String>() {
		@Override
		protected String initialValue() {
			return getKey();
		}
	};

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		String seed = null;
		if (invoker.getUrl().hasParameter(Constants.MONITOR_KEY)) {
			seed = this.getSeed(invocation);
			if(seed == null){
				if(RpcContext.getContext().isConsumerSide()){
					seed = LOCAL.get();
					invocation.getAttachments().put(SEED, LOCAL.get());
					RpcContext.getContext().getAttachments().put(SEED, LOCAL.get());
					System.out.println("生成种子: "+ invocation.getInvoker().getInterface()+"."+invocation.getMethodName() + "Seed : "+seed+" appliaction : "+invoker.getUrl().getParameter("application"));
				}else{
					RpcResult result = new RpcResult();
					result.setException(new ForbidVisitException("没有访问种子，禁止访问"));
					return result;
				}
			}else{
				LOCAL.set(seed);
				System.out.println("收到种子: "+ invocation.getInvoker().getInterface()+"."+invocation.getMethodName() + " Seed : "+seed+" appliaction : "+invoker.getUrl().getParameter("application")+" side : "+ (RpcContext.getContext().isConsumerSide() ? "comsumer":"provider"));
			}
		}
		return invoker.invoke(invocation);
	}

	private String getSeed(Invocation invocation) {
		String seed = invocation.getAttachment(SEED);
		if (seed == null){
			seed = RpcContext.getContext().getAttachments().get(SEED);
		}
		return seed;
	}
	
	private static String getKey(){
		return java.util.UUID.randomUUID().toString();
	}
}
