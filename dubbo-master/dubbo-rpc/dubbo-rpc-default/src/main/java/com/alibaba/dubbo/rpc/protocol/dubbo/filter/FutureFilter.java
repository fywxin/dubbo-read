/*
 * Copyright 1999-2011 Alibaba Group.
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo.filter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Future;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.StaticContext;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.support.RpcUtils;

/**
 * EventFilter
 * @author chao.liuc
 * @author william.liangf
 */
@Activate(group = Constants.CONSUMER)
public class FutureFilter implements Filter {

    protected static final Logger logger = LoggerFactory.getLogger(FutureFilter.class);

    public Result invoke(final Invoker<?> invoker, final Invocation invocation) throws RpcException {
    	///是否异步调用
    	final boolean isAsync = RpcUtils.isAsync(invoker.getUrl(), invocation);
        
    	fireInvokeCallback(invoker, invocation);
        //需要在调用前配置好是否有返回值，已供invoker判断是否需要返回future.
        Result result = invoker.invoke(invocation);
        if (isAsync) {
            asyncCallback(invoker, invocation);
        } else {
            syncCallback(invoker, invocation, result);
        }
        return result;
    }

    private void syncCallback(final Invoker<?> invoker, final Invocation invocation, final Result result) {
        if (result.hasException()) {
            fireThrowCallback(invoker, invocation, result.getException());
        } else {
            fireReturnCallback(invoker, invocation, result.getValue());
        }
    }
    
    private void asyncCallback(final Invoker<?> invoker, final Invocation invocation) {
        Future<?> f = RpcContext.getContext().getFuture();
        if (f instanceof FutureAdapter) {
            ResponseFuture future = ((FutureAdapter<?>)f).getFuture();
            future.setCallback(new ResponseCallback() {
                public void done(Object rpcResult) {
                    if (rpcResult == null){
                        logger.error(new IllegalStateException("invalid result value : null, expected "+Result.class.getName()));
                        return;
                    }
                    ///must be rpcResult
                    if (! (rpcResult instanceof Result)){
                        logger.error(new IllegalStateException("invalid result type :" + rpcResult.getClass() + ", expected "+Result.class.getName()));
                        return;
                    }
                    Result result = (Result) rpcResult;
                    if (result.hasException()) {
                        fireThrowCallback(invoker, invocation, result.getException());
                    } else {
                        fireReturnCallback(invoker, invocation, result.getValue());
                    }
                }
                public void caught(Throwable exception) {
                    fireThrowCallback(invoker, invocation, exception);
                }
            });
        }
    }

    private void fireInvokeCallback(final Invoker<?> invoker, final Invocation invocation) {
    	///缓存执行方法  在 @ReferenceConfig 设置初始值
        final Method onInvokeMethod = (Method)StaticContext.getSystemContext().get(StaticContext.getKey(invoker.getUrl(), invocation.getMethodName(), Constants.ON_INVOKE_METHOD_KEY));
        final Object onInvokeInst = StaticContext.getSystemContext().get(StaticContext.getKey(invoker.getUrl(), invocation.getMethodName(), Constants.ON_INVOKE_INSTANCE_KEY));
        
        if (onInvokeMethod == null  &&  onInvokeInst == null ){
            return ;
        }
        if (onInvokeMethod == null  ||  onInvokeInst == null ){
            throw new IllegalStateException("service:" + invoker.getUrl().getServiceKey() +" has a onreturn callback config , but no such "+(onInvokeMethod == null ? "method" : "instance")+" found. url:"+invoker.getUrl());
        }
        if (onInvokeMethod != null && ! onInvokeMethod.isAccessible()) {
            onInvokeMethod.setAccessible(true);
        }
        
        Object[] params = invocation.getArguments();
        try {
            onInvokeMethod.invoke(onInvokeInst, params);
        } catch (InvocationTargetException e) {
            fireThrowCallback(invoker, invocation, e.getTargetException());
        } catch (Throwable e) {
            fireThrowCallback(invoker, invocation, e);
        }
    }
    
    private void fireReturnCallback(final Invoker<?> invoker, final Invocation invocation, final Object result) {
        final Method onReturnMethod = (Method)StaticContext.getSystemContext().get(StaticContext.getKey(invoker.getUrl(), invocation.getMethodName(), Constants.ON_RETURN_METHOD_KEY));
        final Object onReturnInst = StaticContext.getSystemContext().get(StaticContext.getKey(invoker.getUrl(), invocation.getMethodName(), Constants.ON_RETURN_INSTANCE_KEY));

        //not set onreturn callback
        if (onReturnMethod == null  &&  onReturnInst == null ){
            return ;
        }
        
        if (onReturnMethod == null  ||  onReturnInst == null ){
            throw new IllegalStateException("service:" + invoker.getUrl().getServiceKey() +" has a onreturn callback config , but no such "+(onReturnMethod == null ? "method" : "instance")+" found. url:"+invoker.getUrl());
        }
        if (onReturnMethod != null && ! onReturnMethod.isAccessible()) {
            onReturnMethod.setAccessible(true);
        }
        
        Object[] args = invocation.getArguments();
        Object[] params ;
        Class<?>[] rParaTypes = onReturnMethod.getParameterTypes() ;
        if (rParaTypes.length >1 ) {
            if (rParaTypes.length == 2 && rParaTypes[1].isAssignableFrom(Object[].class)){
                params = new Object[2];
                params[0] = result;
                params[1] = args ;
            }else {
                params = new Object[args.length + 1];
                params[0] = result;
                System.arraycopy(args, 0, params, 1, args.length);
            }
        } else {
            params = new Object[] { result };
        }
        try {
            onReturnMethod.invoke(onReturnInst, params);
        } catch (InvocationTargetException e) {
            fireThrowCallback(invoker, invocation, e.getTargetException());
        } catch (Throwable e) {
            fireThrowCallback(invoker, invocation, e);
        }
    }
    
    private void fireThrowCallback(final Invoker<?> invoker, final Invocation invocation, final Throwable exception) {
        final Method onthrowMethod = (Method)StaticContext.getSystemContext().get(StaticContext.getKey(invoker.getUrl(), invocation.getMethodName(), Constants.ON_THROW_METHOD_KEY));
        final Object onthrowInst = StaticContext.getSystemContext().get(StaticContext.getKey(invoker.getUrl(), invocation.getMethodName(), Constants.ON_THROW_INSTANCE_KEY));

        //没有设置onthrow callback.
        if (onthrowMethod == null  &&  onthrowInst == null ){
            return ;
        }
        if (onthrowMethod == null  ||  onthrowInst == null ){
            throw new IllegalStateException("service:" + invoker.getUrl().getServiceKey() +" has a onthrow callback config , but no such "+(onthrowMethod == null ? "method" : "instance")+" found. url:"+invoker.getUrl());
        }
        if (onthrowMethod != null && ! onthrowMethod.isAccessible()) {
            onthrowMethod.setAccessible(true);
        }
        Class<?>[] rParaTypes = onthrowMethod.getParameterTypes() ;
        if (rParaTypes[0].isAssignableFrom(exception.getClass())){
            try {
                Object[] args = invocation.getArguments();
                Object[] params;
                
                if (rParaTypes.length >1 ) {
                    if (rParaTypes.length == 2 && rParaTypes[1].isAssignableFrom(Object[].class)){
                        params = new Object[2];
                        params[0] = exception;
                        params[1] = args ;
                    }else {
                        params = new Object[args.length + 1];
                        params[0] = exception;
                        System.arraycopy(args, 0, params, 1, args.length);
                    }
                } else {
                    params = new Object[] { exception };
                }
                onthrowMethod.invoke(onthrowInst,params);
            } catch (Throwable e) {
                logger.error(invocation.getMethodName() +".call back method invoke error . callback method :" + onthrowMethod + ", url:"+ invoker.getUrl(), e);
            } 
        } else {
            logger.error(invocation.getMethodName() +".call back method invoke error . callback method :" + onthrowMethod + ", url:"+ invoker.getUrl(), exception);
        }
    }
}