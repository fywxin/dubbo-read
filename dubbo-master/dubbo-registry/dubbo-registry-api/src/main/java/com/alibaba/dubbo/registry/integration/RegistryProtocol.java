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
package com.alibaba.dubbo.registry.integration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.RegistryFactory;
import com.alibaba.dubbo.registry.RegistryService;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Protocol;
import com.alibaba.dubbo.rpc.ProxyFactory;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Cluster;
import com.alibaba.dubbo.rpc.cluster.Configurator;
import com.alibaba.dubbo.rpc.protocol.InvokerWrapper;

/**
 * RegistryProtocol
 * 
 * @author william.liangf
 * @author chao.liuc
 */
public class RegistryProtocol implements Protocol {
	private final static Logger logger = LoggerFactory.getLogger(RegistryProtocol.class);

    private Cluster cluster;
    //注入的是Protocol 代理类 Protocol$Adpative ， 具体是什么，只有在运行时被调用了才知道
    private Protocol protocol;
    
    private RegistryFactory registryFactory;
    
    private ProxyFactory proxyFactory;
    
    private static RegistryProtocol INSTANCE;
    
    private final Map<URL, NotifyListener> overrideListeners = new ConcurrentHashMap<URL, NotifyListener>();
    
    //用于解决rmi重复暴露端口冲突的问题，已经暴露过的服务不再重新暴露
    //providerurl <--> exporter
    private final Map<String, ExporterChangeableWrapper<?>> bounds = new ConcurrentHashMap<String, ExporterChangeableWrapper<?>>();
    
    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }
    
    ///protocol Protocol$Adpative
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }
    public void setRegistryFactory(RegistryFactory registryFactory) {
        this.registryFactory = registryFactory;
    }
    public void setProxyFactory(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }
    public int getDefaultPort() {
        return 9090;
    }
    public RegistryProtocol() {
        INSTANCE = this;
    }
    public static RegistryProtocol getRegistryProtocol() {
        if (INSTANCE == null) {
            ExtensionLoader.getExtensionLoader(Protocol.class).getExtension(Constants.REGISTRY_PROTOCOL); // load
        }
        return INSTANCE;
    }
    public Map<URL, NotifyListener> getOverrideListeners() {
		return overrideListeners;
	}
    
    public <T> Exporter<T> export(final Invoker<T> originInvoker) throws RpcException {
        //export invoker 调用netty暴露当前服务
        final ExporterChangeableWrapper<T> exporter = doLocalExport(originInvoker);
        /***
         * new @ZkclientZookeeperClient.IZkStateListener - new @StateListener -new @ZookeeperRegistry
         */
        final Registry registry = getRegistry(originInvoker);
        ///dubbo://10.5.15.223:20880/com.alibaba.dubbo.examples.generic.api.IUserService?anyhost=true&application=generic-generic&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.examples.generic.api.IUserService&methods=get,sayHello&pid=1608&side=provider&timestamp=1368753767683
        final URL registedProviderUrl = getRegistedProviderUrl(originInvoker);
        ///
        registry.register(registedProviderUrl);
        // 订阅override数据
        // FIXME 提供者订阅时，会影响同一JVM即暴露服务，又引用同一服务的的场景，因为subscribed以服务名为缓存的key，导致订阅信息覆盖。
        final URL overrideSubscribeUrl = getSubscribedOverrideUrl(registedProviderUrl);
        
        ///实现 NotifyListener 接口
        final OverrideListener overrideSubscribeListener = new OverrideListener(overrideSubscribeUrl);
        overrideListeners.put(overrideSubscribeUrl, overrideSubscribeListener);
        
        /***
         * NotifyListener - ChildListener - IZkChildListener
         * 事件冒泡路径：
         * 		zkClient订阅 @IZkChildListener ->调用 @ChildListener -> @ZookeeperRegistry.notify() -> @AbstractRegistry.notify() -> @NotifyListener : @RegistryProtocol.OverrideListener.notify()
         * 		
         * 具体代码：
         * 	1. @ZkclientZookeeperClient.addTargetChildListener  : client.subscribeChildChanges(path, listener);
         *  2. @ZkclientZookeeperClient.createTargetChildListener  : new IZkChildListener() {ChildListener.childChanged(parentPath, currentChilds);});
         *  3. @AbstractZookeeperClient.addChildListener(String path, ChildListener listener) 建立 IZkChildListener 与 ChildListener关联
         *  4. @this zkListener = new ChildListener() {
    	                public void childChanged(String parentPath, List<String> currentChilds) {///订阅后，发生变更通知代码， 重要
    	                	ZookeeperRegistry.this.notify(url, listener, toUrlsWithEmpty(url, parentPath, currentChilds));
    	                }
                            
                     List<String> children = zkClient.addChildListener(path, zkListener);
                      
            5. ChildListener 调用 notify(url, listener, toUrlsWithEmpty(url, parentPath, currentChilds)) 方法
         * 
         */
        registry.subscribe(overrideSubscribeUrl, overrideSubscribeListener);
        //保证每次export都返回一个新的exporter实例
        return new Exporter<T>() {
            public Invoker<T> getInvoker() {
                return exporter.getInvoker();
            }
            public void unexport() {
            	try {
            		exporter.unexport();
            	} catch (Throwable t) {
                	logger.warn(t.getMessage(), t);
                }
                try {
                	registry.unregister(registedProviderUrl);
                } catch (Throwable t) {
                	logger.warn(t.getMessage(), t);
                }
                try {
                	overrideListeners.remove(overrideSubscribeUrl);
                	registry.unsubscribe(overrideSubscribeUrl, overrideSubscribeListener);
                } catch (Throwable t) {
                	logger.warn(t.getMessage(), t);
                }
            }
        };
    }
    
    
    
    @SuppressWarnings("unchecked")
	public <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException {
    	///转化为注册地址协议
        url = url.setProtocol(url.getParameter(Constants.REGISTRY_KEY, Constants.DEFAULT_REGISTRY)).removeParameter(Constants.REGISTRY_KEY);
        //key->protocol
        Registry registry = registryFactory.getRegistry(url);
        if (RegistryService.class.equals(type)) {
        	return proxyFactory.getInvoker((T) registry, type, url);
        }

        ///服务分组，当一个接口有多个实现，可以用分组区分，必需和服务提供方一致
        // group="a,b" or group="*"
        Map<String, String> qs = StringUtils.parseQueryString(url.getParameterAndDecoded(Constants.REFER_KEY));
        String group = qs.get(Constants.GROUP_KEY);
        if (group != null && group.length() > 0 ) {
            if ( ( Constants.COMMA_SPLIT_PATTERN.split( group ) ).length > 1
                    || "*".equals( group ) ) {
                return doRefer( getMergeableCluster(), registry, type, url );
            }
        }
        return doRefer(cluster, registry, type, url);
    }
    
    private <T> Invoker<T> doRefer(Cluster cluster, Registry registry, Class<T> type, URL url) {
        RegistryDirectory<T> directory = new RegistryDirectory<T>(type, url);
        directory.setRegistry(registry);
        directory.setProtocol(protocol);///才可以继续引用暴露
        ///consumer://10.5.15.223/com.alibaba.dubbo.examples.generic.api.IUserService?application=generic-consumer&dubbo=2.0.0&interface=com.alibaba.dubbo.examples.generic.api.IUserService&methods=get&pid=3448&side=consumer&timestamp=1368675677409
        URL subscribeUrl = new URL(Constants.CONSUMER_PROTOCOL, NetUtils.getLocalHost(), 0, type.getName(), directory.getUrl().getParameters());
        if (! Constants.ANY_VALUE.equals(url.getServiceInterface())
                && url.getParameter(Constants.REGISTER_KEY, true)) {
            registry.register(subscribeUrl.addParameters(Constants.CATEGORY_KEY, Constants.CONSUMERS_CATEGORY, Constants.CHECK_KEY, String.valueOf(false)));
        }
        String catrgoryStrs = Constants.PROVIDERS_CATEGORY + "," + Constants.CONFIGURATORS_CATEGORY + "," + Constants.ROUTERS_CATEGORY;
        
        /***
         * NotifyListener - ChildListener - IZkChildListener
         * 事件冒泡路径：
         * 		zkClient订阅 @IZkChildListener ->调用 @ChildListener -> @ZookeeperRegistry.notify() -> @AbstractRegistry.notify() -> @NotifyListener : @RegistryDirectory.notify()
         * 		
         * 具体代码：
         * 	1. @ZkclientZookeeperClient.addTargetChildListener  : client.subscribeChildChanges(path, listener);
         *  2. @ZkclientZookeeperClient.createTargetChildListener  : new IZkChildListener() {ChildListener.childChanged(parentPath, currentChilds);});
         *  3. @AbstractZookeeperClient.addChildListener(String path, ChildListener listener) 建立 IZkChildListener 与 ChildListener关联
         *  4. @this zkListener = new ChildListener() {
    	                public void childChanged(String parentPath, List<String> currentChilds) {///订阅后，发生变更通知代码， 重要
    	                	ZookeeperRegistry.this.notify(url, listener, toUrlsWithEmpty(url, parentPath, currentChilds));
    	                }
                            
                     List<String> children = zkClient.addChildListener(path, zkListener);
                      
            5. ChildListener 调用 notify(url, listener, toUrlsWithEmpty(url, parentPath, currentChilds)) 方法
         * 
         */
        directory.subscribe(subscribeUrl.addParameter(Constants.CATEGORY_KEY, catrgoryStrs));  ///category=providers,configurators,routers
        return cluster.join(directory);
    }
    
    /**
     * 对修改了url的invoker重新export
     * @param originInvoker
     * @param newInvokerUrl
     */
    @SuppressWarnings("unchecked")
    private <T> void doChangeLocalExport(final Invoker<T> originInvoker, URL newInvokerUrl){
        String key = getCacheKey(originInvoker);
        final ExporterChangeableWrapper<T> exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
        if (exporter == null){
            logger.warn(new IllegalStateException("error state, exporter should not be null"));
            return ;//不存在是异常场景 直接返回 
        } else {
            final Invoker<T> invokerDelegete = new InvokerDelegete<T>(originInvoker, newInvokerUrl);
            exporter.setExporter(protocol.export(invokerDelegete));
        }
    }

    /**
     * 根据invoker的地址获取registry实例
     * @param originInvoker
     * @return
     */
    private Registry getRegistry(final Invoker<?> originInvoker){
        URL registryUrl = originInvoker.getUrl();
        ///registry://224.5.6.7:1234/com.alibaba.dubbo.registry.RegistryService?application=generic-generic&dubbo=2.0.0&export=dubbo%3A%2F%2F10.5.15.223%3A20880%2Fcom.alibaba.dubbo.examples.generic.api.IUserService%3Fanyhost%3Dtrue%26application%3Dgeneric-generic%26dubbo%3D2.0.0%26generic%3Dfalse%26interface%3Dcom.alibaba.dubbo.examples.generic.api.IUserService%26methods%3Dget%26pid%3D1032%26side%3Dprovider%26timestamp%3D1368691460335&pid=1032&registry=multicast&timestamp=1368691460311
        if (Constants.REGISTRY_PROTOCOL.equals(registryUrl.getProtocol())) {
            String protocol = registryUrl.getParameter(Constants.REGISTRY_KEY, Constants.DEFAULT_DIRECTORY);
            registryUrl = registryUrl.setProtocol(protocol).removeParameter(Constants.REGISTRY_KEY);
        }
        return registryFactory.getRegistry(registryUrl);
    }
    
    /***
     * 暴露当前服务
     * @param <T>
     * @param originInvoker
     * @return
     */
    @SuppressWarnings("unchecked")
    private <T> ExporterChangeableWrapper<T>  doLocalExport(final Invoker<T> originInvoker){
    	///dubbo://10.5.15.223:20880/com.alibaba.dubbo.examples.generic.api.IUserService?anyhost=true&application=generic-generic&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.examples.generic.api.IUserService&methods=get&pid=2392&side=provider&timestamp=1368689200101
        String key = getCacheKey(originInvoker);
        ExporterChangeableWrapper<T> exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
        if (exporter == null) {
            synchronized (bounds) {
                exporter = (ExporterChangeableWrapper<T>) bounds.get(key);
                if (exporter == null) {
                	///对Invoker 再封装，加入提供地址getProviderUrl(originInvoker)??
                    final Invoker<?> invokerDelegete = new InvokerDelegete<T>(originInvoker, getProviderUrl(originInvoker));
                    
                    ///protocol == ?
                    exporter = new ExporterChangeableWrapper<T>((Exporter<T>)protocol.export(invokerDelegete), originInvoker);
                    bounds.put(key, exporter);
                }
            }
        }
        return (ExporterChangeableWrapper<T>) exporter;
    }

    /**
     * 返回注册到注册中心的URL，对URL参数进行一次过滤
     * @param originInvoker
     * @return
     */
    private URL getRegistedProviderUrl(final Invoker<?> originInvoker){
        URL providerUrl = getProviderUrl(originInvoker);
        //注册中心看到的地址
        final URL registedProviderUrl = providerUrl.removeParameters(getFilteredKeys(providerUrl)).removeParameter(Constants.MONITOR_KEY);
        return registedProviderUrl;
    }
    
    private URL getSubscribedOverrideUrl(URL registedProviderUrl){
    	return registedProviderUrl.setProtocol(Constants.PROVIDER_PROTOCOL)
                .addParameters(Constants.CATEGORY_KEY, Constants.CONFIGURATORS_CATEGORY, 
                        Constants.CHECK_KEY, String.valueOf(false));
    }

    /**
     * 通过invoker的url 获取 providerUrl的地址
     * @param origininvoker
     * @return
     */
    private URL getProviderUrl(final Invoker<?> origininvoker){
        String export = origininvoker.getUrl().getParameterAndDecoded(Constants.EXPORT_KEY);
        if (export == null || export.length() == 0) {
            throw new IllegalArgumentException("The registry export url is null! registry: " + origininvoker.getUrl());
        }
        
        URL providerUrl = URL.valueOf(export);
        return providerUrl;
    }

    /**
     * 获取invoker在bounds中缓存的key
     * @param originInvoker
     * @return
     */
    private String getCacheKey(final Invoker<?> originInvoker){
        URL providerUrl = getProviderUrl(originInvoker);
        String key = providerUrl.removeParameters("dynamic", "enabled").toFullString();
        return key;
    }
    
    
    
    private Cluster getMergeableCluster() {
        return ExtensionLoader.getExtensionLoader(Cluster.class).getExtension("mergeable");
    }
    
    

    //过滤URL中不需要输出的参数(以点号开头的)
    private static String[] getFilteredKeys(URL url) {
        Map<String, String> params = url.getParameters();
        if (params != null && !params.isEmpty()) {
            List<String> filteredKeys = new ArrayList<String>();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry != null && entry.getKey() != null && entry.getKey().startsWith(Constants.HIDE_KEY_PREFIX)) {
                    filteredKeys.add(entry.getKey());
                }
            }
            return filteredKeys.toArray(new String[filteredKeys.size()]);
        } else {
            return new String[] {};
        }
    }
    
    public void destroy() {
        List<Exporter<?>> exporters = new ArrayList<Exporter<?>>(bounds.values());
        for(Exporter<?> exporter :exporters){
            exporter.unexport();
        }
        bounds.clear();
    }
    
    
    /*重新export 1.protocol中的exporter destory问题 
     *1.要求registryprotocol返回的exporter可以正常destroy
     *2.notify后不需要重新向注册中心注册 
     *3.export 方法传入的invoker最好能一直作为exporter的invoker.
     */
    private class OverrideListener implements NotifyListener {
    	
    	private volatile List<Configurator> configurators;
    	
    	private final URL subscribeUrl;

		public OverrideListener(URL subscribeUrl) {
			this.subscribeUrl = subscribeUrl;
		}

		/*
         *  provider 端可识别的override url只有这两种.
         *  override://0.0.0.0/serviceName?timeout=10
         *  override://0.0.0.0/?timeout=10
         */
        public void notify(List<URL> urls) {
        	List<URL> result = null;
        	for (URL url : urls) {
        		URL overrideUrl = url;
        		if (url.getParameter(Constants.CATEGORY_KEY) == null
        				&& Constants.OVERRIDE_PROTOCOL.equals(url.getProtocol())) {
        			// 兼容旧版本
        			overrideUrl = url.addParameter(Constants.CATEGORY_KEY, Constants.CONFIGURATORS_CATEGORY);
        		}
        		if (! UrlUtils.isMatch(subscribeUrl, overrideUrl)) {
        			if (result == null) {
        				result = new ArrayList<URL>(urls);
        			}
        			result.remove(url);
        			logger.warn("Subsribe category=configurator, but notifed non-configurator urls. may be registry bug. unexcepted url: " + url);
        		}
        	}
        	if (result != null) {
        		urls = result;
        	}
        	this.configurators = RegistryDirectory.toConfigurators(urls);
            List<ExporterChangeableWrapper<?>> exporters = new ArrayList<ExporterChangeableWrapper<?>>(bounds.values());
            for (ExporterChangeableWrapper<?> exporter : exporters){
                Invoker<?> invoker = exporter.getOriginInvoker();
                final Invoker<?> originInvoker ;
                if (invoker instanceof InvokerDelegete){
                    originInvoker = ((InvokerDelegete<?>)invoker).getInvoker();
                }else {
                    originInvoker = invoker;
                }
                
                URL originUrl = RegistryProtocol.this.getProviderUrl(originInvoker);
                URL newUrl = getNewInvokerUrl(originUrl, urls);
                
                if (! originUrl.equals(newUrl)){
                    RegistryProtocol.this.doChangeLocalExport(originInvoker, newUrl);
                }
            }
        }
        
        private URL getNewInvokerUrl(URL url, List<URL> urls){
        	List<Configurator> localConfigurators = this.configurators; // local reference
            // 合并override参数
            if (localConfigurators != null && localConfigurators.size() > 0) {
                for (Configurator configurator : localConfigurators) {
                    url = configurator.configure(url);
                }
            }
            return url;
        }
    }
    
    public static class InvokerDelegete<T> extends InvokerWrapper<T>{
        private final Invoker<T> invoker;
        /**
         * @param invoker 
         * @param url invoker.getUrl返回此值
         */
        public InvokerDelegete(Invoker<T> invoker, URL url){
            super(invoker, url);
            this.invoker = invoker;
        }
        public Invoker<T> getInvoker(){
            if (invoker instanceof InvokerDelegete){
                return ((InvokerDelegete<T>)invoker).getInvoker();
            } else {
                return invoker;
            }
        }
    }
    
    /**
     * exporter代理,建立返回的exporter与protocol export出的exporter的对应关系，在override时可以进行关系修改.
     * 
     * @author chao.liuc
     *
     * @param <T>
     */
    private class ExporterChangeableWrapper<T> implements Exporter<T>{
    	
        private Exporter<T> exporter;
        
        private final Invoker<T> originInvoker;

        public ExporterChangeableWrapper(Exporter<T> exporter, Invoker<T> originInvoker){
            this.exporter = exporter;
            this.originInvoker = originInvoker;
        }
        
        public Invoker<T> getOriginInvoker() {
            return originInvoker;
        }

        public Invoker<T> getInvoker() {
            return exporter.getInvoker();
        }
        
        public void setExporter(Exporter<T> exporter){
            this.exporter = exporter;
        }

        public void unexport() {
            String key = getCacheKey(this.originInvoker);
            bounds.remove(key);
            exporter.unexport();
        }
    }
}