package com.cyou.fz.soa.filter;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;


@Activate(group={Constants.PROVIDER, Constants.CONSUMER})
public class TreeFilter implements Filter{
	
	private static final String key = "traceId";
	
	private static final ThreadLocal<String> HOLDER = new ThreadLocal<String>(){
		@Override
		protected String initialValue() {
			return null;
		}
	};

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
			String id = null;
			String traceId = null;
			String rpcId = null;
			String appName = invoker.getUrl().getParameter("application");
			
			if(RpcContext.getContext().isConsumerSide()){
				System.out.println("C: "+invoker.getUrl());
				System.out.println("inf: "+invoker.getInterface());
				System.out.println("method: "+invocation.getMethodName());
				System.out.println("attach: "+invocation.getAttachments());
				
				id = HOLDER.get();
				//根点
				if(id == null || "".equals(id.trim())){
					traceId = this.getTraceId();
					rpcId = "0.1";
					
					System.out.println(appName+" : "+0);
				}else{
					String[] ids = id.split(":");
					traceId = ids[0];
					rpcId = ids[1];
					rpcId = this.growRpcId(rpcId);
				}
				id = traceId+":"+rpcId;
				HOLDER.set(id);
				invocation.getAttachments().put(key, id);
				System.out.println(invoker.getUrl());
				System.out.println("S:"+invoker.getClass()+"."+invoker.getInterface()+"()");
			}else{
				System.out.println("S: "+invoker.getUrl());
				System.out.println("inf: "+invoker.getInterface());
				System.out.println("method: "+invocation.getMethodName());
				System.out.println("attach: "+invocation.getAttachments());
				
				id = invocation.getAttachment(key);
//				if(id == null || "".equals(id.trim())){
//					throw new RuntimeException();
//				}else{
//					String[] ids = id.split(":");
//					traceId = ids[0];
//					rpcId = ids[1];
//					System.out.println(invoker.getUrl());
//					System.out.println(invoker.getClass()+"."+invoker.getInterface()+"()");
//					System.out.println(appName+" : "+rpcId);
//					
//					rpcId = rpcId+".0";
//				}
				id = traceId+":"+rpcId;
				HOLDER.set(id);
			}
			
			return invoker.invoke(invocation);
		
	}

	
	private String growRpcId(String rpcId){
		int index = rpcId.lastIndexOf(".")+1;
		String temp = rpcId.substring(index);
		return rpcId.substring(0, index)+String.valueOf((Integer.parseInt(temp)+1));
	}
	
	private String getTraceId(){
		return "traceId";
	}
}
