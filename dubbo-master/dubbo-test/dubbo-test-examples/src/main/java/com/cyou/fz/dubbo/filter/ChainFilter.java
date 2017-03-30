package com.cyou.fz.dubbo.filter;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;

//seedId : 项目编号(2) + 子编号(1)+ 控制位(2) + 时间(13)
@Activate(group = { Constants.PROVIDER, Constants.CONSUMER })
public class ChainFilter implements Filter {

	//seedId : traceId
	private static final ConcurrentMap<String, String> seeds = new ConcurrentHashMap<String, String>();
	private static final ThreadLocal<String> HOLDER = new ThreadLocal<String>();
	
	private static final String KEY = "SEED";
	
	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		
		if (invoker.getUrl().hasParameter(Constants.MONITOR_KEY)) {
			System.out.println(seeds);
			
			boolean serverSide = RpcContext.getContext().isProviderSide();
			String traceId = null;
			String seedId = null;
			
			if(serverSide){
				String chainId = invocation.getAttachment(KEY);
				if(chainId == null)
					throw new RpcException();
				seedId = chainId.substring(0, 18);
				traceId = chainId.substring(18);
				seeds.put(seedId, traceId+".0");
				HOLDER.set(seedId);
			}else{
				seedId = HOLDER.get();
				if(seedId == null){
					seedId = getSeedId();
					traceId = "0.1";
					seeds.put(seedId, "0.1");
				}else{
					traceId = seeds.get(seedId);
					int index = traceId.lastIndexOf(".");
					traceId = traceId.substring(0, index+1) + String.valueOf((Integer.parseInt(traceId.substring(index+1))+1));
				}
			}
			
			System.out.println(seedId+traceId);
			invocation.getAttachments().put(KEY, seedId+traceId);
		}else{
			return invoker.invoke(invocation);
		}
		
		return invoker.invoke(invocation);
	}

	private String getSeedId(){
		return "AB1"+"01"+new Date().getTime();
	}
	
//	public static void main(String[] args) {
//		String chainId= getSeedId()+"0.1";
//		System.out.println(chainId);
//		System.out.println(chainId.substring(0, 18));
//		String traceId = chainId.substring(18);
//		System.out.println(traceId);
//		int index = traceId.lastIndexOf(".");
//		System.out.println(index);
//		System.out.println(traceId.substring(0, index+1));
//		System.out.println(Integer.parseInt(traceId.substring(index+1))+1);
//		traceId = traceId.substring(0, index+1) + String.valueOf((Integer.parseInt(traceId.substring(index+1))+1));
//		System.out.println(traceId);
//	}
}
