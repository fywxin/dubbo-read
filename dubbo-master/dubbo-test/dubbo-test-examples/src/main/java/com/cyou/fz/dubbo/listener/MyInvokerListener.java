package com.cyou.fz.dubbo.listener;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.listener.InvokerListenerAdapter;
import com.alibaba.dubbo.common.Constants;

@Activate(group={Constants.PROVIDER,Constants.CONSUMER})
public class MyInvokerListener extends InvokerListenerAdapter {

	public void referred(Invoker<?> invoker) throws RpcException {
		System.out.println("完成refer" + invoker.getInterface());
		
    }

    public void destroyed(Invoker<?> invoker) {
    	System.out.println("销毁refer" + invoker.getInterface());
    }
}
