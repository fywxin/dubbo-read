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
package com.alibaba.dubbo.rpc.cluster;

import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.support.FailoverCluster;

/**
 * Cluster. (SPI, Singleton, ThreadSafe)
 * 集群容错
 * Failover Cluster 失败自动切换，当出现失败，重试其它服务器。(缺省)
	通常用于读操作，但重试会带来更长延迟。
	可通过retries="2"来设置重试次数(不含第一次)。
	Failfast Cluster 快速失败，只发起一次调用，失败立即报错。
	通常用于非幂等性的写操作，比如新增记录。
	Failsafe Cluster 失败安全，出现异常时，直接忽略。
	通常用于写入审计日志等操作。
	Failback Cluster 失败自动恢复，后台记录失败请求，定时重发。
	通常用于消息通知操作。
	Forking Cluster 并行调用多个服务器，只要一个成功即返回。
	通常用于实时性要求较高的读操作，但需要浪费更多服务资源。
	可通过forks="2"来设置最大并行数。
	Broadcast Cluster 广播调用所有提供者，逐个调用，任意一台报错则报错。(2.1.0开始支持)
	通常用于通知所有提供者更新缓存或日志等本地资源信息。
 * 
 * <a href="http://en.wikipedia.org/wiki/Computer_cluster">Cluster</a>
 * <a href="http://en.wikipedia.org/wiki/Fault-tolerant_system">Fault-Tolerant</a>
 * 
 * @author william.liangf
 */
@SPI(FailoverCluster.NAME)
public interface Cluster {

    /**
     * Merge the directory invokers to a virtual invoker.
     * 
     * @param <T>
     * @param directory
     * @return cluster invoker
     * @throws RpcException
     */
    @Adaptive
    <T> Invoker<T> join(Directory<T> directory) throws RpcException;

}