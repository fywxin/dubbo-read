package com.cyou.fz.dubbo.filter;

import java.util.Date;
import java.util.Random;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.cyou.fz.dubbo.filter.domain.App;
import com.cyou.fz.dubbo.filter.domain.Node;


//seedId : 项目编号(2) + 子编号(1)+ 控制位(2) + 时间(13)
@Activate(group = { Constants.PROVIDER, Constants.CONSUMER })
public class HawkeyeFilter implements Filter {
	
	private static final String KEY = "HAWKEYE";
	
	private static final ThreadLocal<App> HOLDER = new ThreadLocal<App>();

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		if (invoker.getUrl().hasParameter(Constants.MONITOR_KEY)) {
			Long startTime = new Date().getTime();
			String seed = invocation.getAttachment(KEY);
			String traceId = null;
			String rpcId = null;
			String appName = invoker.getUrl().getParameter("application");
			String ip = invoker.getUrl().getIp();
			String fromApp = null;
			App app = null;
			Node node = null;
			
			
			//没有种子,链首
			if(seed == null){
				if((app = HOLDER.get()) == null){
					traceId = this.getTraceId(appName, ip);
					rpcId = "0";
					fromApp = appName;
					app = new App(ip, appName, traceId, rpcId);
					HOLDER.set(app);
				}else{
					traceId = app.getTraceId();
					rpcId = app.getRpcId();
				}
				
			//链中
			}else{
				if((app = HOLDER.get()) == null){
					String[] seeds = seed.split("|");
					traceId = seeds[0];
					rpcId = seeds[1]+".1";
					fromApp = seeds[2];
					app = new App(ip, appName, traceId, rpcId);
					HOLDER.set(app);
				}else{
					traceId = app.getTraceId();
					rpcId = app.getRpcId();
				}
			}
			System.out.println("traceId = "+traceId);
			int control = Integer.parseInt(traceId.substring(0, 1));
			if(control > 0){
				node = new Node();
				node.setIsClient(RpcContext.getContext().isConsumerSide());
				node.setMethod(invocation.getClass()+"."+invocation.getMethodName());
				node.setStartTime(startTime);
				
				if(appName.equals(fromApp)){
					node.setRpcId(getNextRpcId(rpcId, true));
				}else{
					node.setRpcId(getNextRpcId(rpcId, false));
				}
				
				if(control == 2){
					node.setInvocation(invocation);
				}
			}
			
			//调用其他服务
			if(RpcContext.getContext().isConsumerSide()){
				invocation.getAttachments().put(KEY, traceId+"|"+traceId+"|"+appName);
			}
			
			Result rs = invoker.invoke(invocation);
			
			if(control > 0){
				node.setDuration(new Long(startTime - new Date().getTime()).intValue());
				if(control == 2){
					node.setResult(rs);
				}
				System.out.println(node);
			}
			System.out.println(app);
			return rs;
		}else{
			return invoker.invoke(invocation);
		}
	}
	
	public String getNextRpcId(String rpcId, boolean increase) {
		if(rpcId == null)
			rpcId = "0";
		if(increase) {
			int lastIndex = rpcId.lastIndexOf(".");
			if(lastIndex == -1){
				rpcId += ".1";
			}else{
				String pre = rpcId.substring(0, lastIndex);
				rpcId = pre + (Integer.parseInt(rpcId.substring(lastIndex+1, rpcId.length()-1))+1);
			}
		}else {
			rpcId += ".1";
		}
		return rpcId;
	}
	
	/**
	 * 从traceId分发中心获取跟踪ID
	 * 第一位为控制位： 0：表示不跟踪， 1：表示正常跟踪  2：调试，会取得输入与返回的内容
	 * 第二个为id序列
	 * 
	 * 控制中心同时保存 应用名与IP
	 * 
	 * 方式2，本地生成ID，然后定时同步到ID服务中心，同时从ID服务中心返回控制信息
	 * @return
	 */
	private String getTraceId(String appName, String ip){
		
		return "1"+new Random().nextInt();
	}
}
