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
import com.cyou.fz.dubbo.trace.SeedCollector;
import com.cyou.fz.dubbo.trace.api.SeedService;
import com.cyou.fz.dubbo.trace.domain.Node;
import com.cyou.fz.dubbo.trace.domain.Span;

@Activate(group = { Constants.PROVIDER, Constants.CONSUMER })
public class SeedFilter implements Filter {

	private static final String SEED = "seed";
	
	private static final ThreadLocal<Span> LOCAL = new ThreadLocal<Span>() {};
	
	private SeedCollector seedCollector;

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		if (invoker.getUrl().hasParameter(Constants.MONITOR_KEY) && !invocation.getInvoker().getInterface().isAssignableFrom(SeedService.class)) {
			Node node = new Node(invocation.getInvoker().getInterface().toString(), invocation.getMethodName(), RpcContext.getContext().isConsumerSide());
			node.setId(java.util.UUID.randomUUID().toString());
			node.setTimestamp(new java.util.Date().getTime());
			
			//父节点传入的种子 | 服务被调用时得到的种子
			String seed = invocation.getAttachment(SEED);
			String appName = invoker.getUrl().getParameter("application");
			
			Span span = null;
			if(seed == null){//是根点或者为 服务调用其他服务方法点
				//根节点
				if((span = LOCAL.get()) == null){
					if(RpcContext.getContext().isProviderSide()){
						RpcResult result = new RpcResult();
						result.setException(new ForbidVisitException("没有访问种子，禁止访问"));
						return result;
					}
					//创建一个种子
					span = new Span(getKey(), appName, null);
					LOCAL.set(span);
					
					this.pushSpan(span);
				}else{
					node.setPid(span.getPreNode() == null ? null : span.getPreNode().getPid());
				}
			//得到种子后，调用外部服务方法
			}else{
				if((span = LOCAL.get()) == null){
					String[] seeds = seed.split(">");
					span = new Span(seeds[2], appName, seeds[0]);
					LOCAL.set(span);
					node.setPid(seeds[1]);
					
					this.pushSpan(span);
				}else{
					node.setPid(span.getPreNode() == null ? null : span.getPreNode().getPid());
				}
			}
			node.setSpanId(span.getId());
			span.setPreNode(node);
			invocation.getAttachments().put(SEED, span.getId()+">"+node.getId()+">"+LOCAL.get().getSeed());
			Result rs = invoker.invoke(invocation);
			Long duration = new java.util.Date().getTime()- node.getTimestamp();
			node.setDuration(duration.intValue());
			this.pushNode(node);
			return rs;
		}else{
			return invoker.invoke(invocation);
		}
	}
	
	private void pushSpan(Span span){
		System.out.println(span);
		this.seedCollector.collectSpan(span);
	}
	
	private void pushNode(Node node){
		System.out.println(node);
		this.seedCollector.collectNode(node);
	}

	private static String getKey(){
		return java.util.UUID.randomUUID().toString();
	}

	public void setSeedCollector(SeedCollector seedCollector) {
		this.seedCollector = seedCollector;
	}
}
