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
import com.cyou.fz.dubbo.trace.domain.Node_1;

@Activate(group = { Constants.PROVIDER, Constants.CONSUMER })
public class SeedFilter_1 implements Filter {

	private static final String SEED = "seed";
	
	private static final ThreadLocal<Node_1> LOCAL = new ThreadLocal<Node_1>() {};

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		if (invoker.getUrl().hasParameter(Constants.MONITOR_KEY)) {
			//父节点传入的种子 | 服务被调用时得到的种子
			String seed = invocation.getAttachment(SEED);
			String appName = invoker.getUrl().getParameter("application");
			
			Node_1 node = null;
			if(seed == null){//是根点或者为 服务调用其他服务方法点
				//根节点
				if((node = LOCAL.get()) == null){
					if(RpcContext.getContext().isProviderSide()){
						RpcResult result = new RpcResult();
						result.setException(new ForbidVisitException("没有访问种子，禁止访问"));
						return result;
					}
					//创建一个种子
					node = new Node_1();
					node.setSeed(getKey());
					node.setAppName(appName);
					node.setIntf(invocation.getInvoker().getInterface().toString());
					node.addMethod(invocation.getMethodName());
					node.setParentAppName(null);
					LOCAL.set(node);
					System.out.println("生成根Seed : "+seed+" | appliaction : "+appName+"->"+ invocation.getInvoker().getInterface()+"."+invocation.getMethodName());
				//服务调用其他服务方法点 如 
				}else{
					node.addMethod(invocation.getMethodName());
					//System.out.println("从ThreadLocal获取Seed : "++" | appliaction : "+appName+"->"+ invocation.getInvoker().getInterface()+"."+invocation.getMethodName());
				}
			//得到种子后，调用外部服务方法
			}else{
				if((node = LOCAL.get()) == null){
					String[] seeds = seed.split(">");
					node = new Node_1();
					node.setAppName(appName);
					node.setIntf(invocation.getInvoker().getInterface().toString());
					node.addMethod(invocation.getMethodName());
					node.setParentAppName(seeds[0]);
					node.setSeed(seeds[1]);
					LOCAL.set(node);
				}else{
					node.addMethod(invocation.getMethodName());
					//System.out.println("从ThreadLocal获取Seed : "+seed+" | appliaction : "+appName+"->"+ invocation.getInvoker().getInterface()+"."+invocation.getMethodName());
				}
			}
			
			System.out.println(node);
			invocation.getAttachments().put(SEED, appName+">"+LOCAL.get().getSeed());
		}
		return invoker.invoke(invocation);
	}

	private static String getKey(){
		return java.util.UUID.randomUUID().toString();
	}
}
