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
package com.alibaba.dubbo.registry.zookeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.registry.NotifyListener;
import com.alibaba.dubbo.registry.support.FailbackRegistry;
import com.alibaba.dubbo.remoting.zookeeper.ChildListener;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperClient;
import com.alibaba.dubbo.remoting.zookeeper.StateListener;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperTransporter;
import com.alibaba.dubbo.rpc.RpcException;

/**
 * ZookeeperRegistry
 * 
 * @author william.liangf
 */
public class ZookeeperRegistry extends FailbackRegistry {

    private final static Logger logger = LoggerFactory.getLogger(ZookeeperRegistry.class);

    private final static int DEFAULT_ZOOKEEPER_PORT = 2181;
    
    private final static String DEFAULT_ROOT = "dubbo";

    private final String        root;
    
    private final Set<String> anyServices = new ConcurrentHashSet<String>();

    private final ConcurrentMap<URL, ConcurrentMap<NotifyListener, ChildListener>> zkListeners = new ConcurrentHashMap<URL, ConcurrentMap<NotifyListener, ChildListener>>();
    
    private final ZookeeperClient zkClient;
    
    public ZookeeperRegistry(URL url, ZookeeperTransporter zookeeperTransporter) {
        super(url);
        if (url.isAnyHost()) {
    		throw new IllegalStateException("registry address == null");
    	}
        String group = url.getParameter(Constants.GROUP_KEY, DEFAULT_ROOT); /// /dubbo
        if (! group.startsWith(Constants.PATH_SEPARATOR)) {
            group = Constants.PATH_SEPARATOR + group;
        }
        this.root = group;
        zkClient = zookeeperTransporter.connect(url);
        ///添加包装后的监听器  三种状态  DISCONNECTED ， CONNECTED ， RECONNECTED
        ///触发点 @ZkclientZookeeperClient client.subscribeStateChanges(new IZkStateListener() {})  最后执行recover()方法
        zkClient.addStateListener(new StateListener() {
            public void stateChanged(int state) {
            	if (state == RECONNECTED) {
	            	try {
						recover();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
            	}
            }
        });
    }

    public boolean isAvailable() {
        return zkClient.isConnected();
    }

    public void destroy() {
        super.destroy();
        try {
            zkClient.close();
        } catch (Exception e) {
            logger.warn("Failed to close zookeeper client " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    protected void doRegister(URL url) {
        try {
        	zkClient.create(toUrlPath(url), url.getParameter(Constants.DYNAMIC_KEY, true));
        } catch (Throwable e) {
            throw new RpcException("Failed to register " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    protected void doUnregister(URL url) {
        try {
            zkClient.delete(toUrlPath(url));
        } catch (Throwable e) {
            throw new RpcException("Failed to unregister " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    /***
     * NotifyListener - ChildListener - IZkChildListener
     * 事件冒泡路径：
     * 		zkClient订阅 @IZkChildListener ->调用 @ChildListener -> @ZookeeperRegistry.notify() -> @AbstractRegistry.notify() -> @NotifyListener : @RegistryDirectory | @RegistryProtocol.OverrideListener
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
    protected void doSubscribe(final URL url, final NotifyListener listener) {
        try {
            if (!Constants.ANY_VALUE.equals(url.getServiceInterface())) {
            	List<URL> urls = new ArrayList<URL>();
                for (String path : toCategoriesPath(url)) {
                	/// ConcurrentMap<URL, ConcurrentMap<NotifyListener, ChildListener>> zkListeners
                    ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(url);
                    if (listeners == null) {
                        zkListeners.putIfAbsent(url, new ConcurrentHashMap<NotifyListener, ChildListener>());
                        listeners = zkListeners.get(url);
                    }
                    ChildListener zkListener = listeners.get(listener);
                    if (zkListener == null) {
                    	/// 创建节点变更通知  建立listener 与 ChildListener 一对一关联
                        listeners.putIfAbsent(listener, new ChildListener() {
                            public void childChanged(String parentPath, List<String> currentChilds) {///订阅后，发生变更通知代码， 重要
                            	ZookeeperRegistry.this.notify(url, listener, toUrlsWithEmpty(url, parentPath, currentChilds));
                            }
                        });
                        zkListener = listeners.get(listener);
                    }
                    ///创建节点 /dubbo/com.cyou.fz.api.DemoService/configurators
                    zkClient.create(path, false);
                    /// 注册通知对象对应关注的目录  [dubbo%3A%2F%2F10.5.15.223%3A20880%2Fcom.cyou.fz.api.DemoService%3Fanyhost%3Dtrue%26application%3Ddemo-test%26dubbo%3D2.0.0%26generic%3Dfalse%26interface%3Dcom.cyou.fz.api.DemoService%26methods%3DgetNextIdNoCache%2CdoTimeOut%2C%24echo%2CgetTimeOutCallTimes%2CgetNextIdWithCache%2CdoThrowException%2CgetOutLaw%26pid%3D3152%26side%3Dprovider%26timestamp%3D1374196539245]
                    List<String> children = zkClient.addChildListener(path, zkListener);
                    if (children != null) {
                    	urls.addAll(toUrlsWithEmpty(url, path, children));
                    }
                }
                notify(url, listener, urls);
            } else {
            	String root = toRootPath();
                ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(url);
                if (listeners == null) {
                    zkListeners.putIfAbsent(url, new ConcurrentHashMap<NotifyListener, ChildListener>());
                    listeners = zkListeners.get(url);
                }
                ChildListener zkListener = listeners.get(listener);
                if (zkListener == null) {
                    listeners.putIfAbsent(listener, new ChildListener() {
                        public void childChanged(String parentPath, List<String> currentChilds) {
                            for (String child : currentChilds) {
								child = URL.decode(child);
                                if (! anyServices.contains(child)) {
                                    anyServices.add(child);
                                    subscribe(url.setPath(child).addParameters(Constants.INTERFACE_KEY, child, 
                                            Constants.CHECK_KEY, String.valueOf(false)), listener);
                                }
                            }
                        }
                    });
                    zkListener = listeners.get(listener);
                }
                zkClient.create(root, false);
                List<String> services = zkClient.addChildListener(root, zkListener);
                if (services != null && services.size() > 0) {
                    for (String service : services) {
						service = URL.decode(service);
						anyServices.add(service);
                        subscribe(url.setPath(service).addParameters(Constants.INTERFACE_KEY, service, 
                                Constants.CHECK_KEY, String.valueOf(false)), listener);
                    }
                }
            }
        } catch (Throwable e) {
            throw new RpcException("Failed to subscribe " + url + " to zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    protected void doUnsubscribe(URL url, NotifyListener listener) {
        ConcurrentMap<NotifyListener, ChildListener> listeners = zkListeners.get(url);
        if (listeners != null) {
            ChildListener zkListener = listeners.get(listener);
            if (zkListener != null) {
                zkClient.removeChildListener(toUrlPath(url), zkListener);
            }
        }
    }

    public List<URL> lookup(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("lookup url == null");
        }
        try {
            List<String> providers = new ArrayList<String>();
            for (String path : toCategoriesPath(url)) {
                try {
                    List<String> children = zkClient.getChildren(path);
                    if (children != null) {
                        providers.addAll(children);
                    }
                } catch (ZkNoNodeException e) {
                    // ignore
                }
            }
            return toUrlsWithoutEmpty(url, providers);
        } catch (Throwable e) {
            throw new RpcException("Failed to lookup " + url + " from zookeeper " + getUrl() + ", cause: " + e.getMessage(), e);
        }
    }
    
    private String toRootDir() {
        if (root.equals(Constants.PATH_SEPARATOR)) {
            return root;
        }
        return root + Constants.PATH_SEPARATOR;
    }
    
    private String toRootPath() {
        return root;
    }
    
    private String toServicePath(URL url) {
        String name = url.getServiceInterface();
        if (Constants.ANY_VALUE.equals(name)) {
            return toRootPath();
        }
        return toRootDir() + URL.encode(name);
    }

    private String[] toCategoriesPath(URL url) {
        String[] categroies;
        if (Constants.ANY_VALUE.equals(url.getParameter(Constants.CATEGORY_KEY))) {
            categroies = new String[] {Constants.PROVIDERS_CATEGORY, Constants.CONSUMERS_CATEGORY, 
                    Constants.ROUTERS_CATEGORY, Constants.CONFIGURATORS_CATEGORY};
        } else {
            categroies = url.getParameter(Constants.CATEGORY_KEY, new String[] {Constants.DEFAULT_CATEGORY});
        }
        String[] paths = new String[categroies.length];
        for (int i = 0; i < categroies.length; i ++) {
            paths[i] = toServicePath(url) + Constants.PATH_SEPARATOR + categroies[i];
        }
        return paths;
    }

    private String toCategoryPath(URL url) {
        return toServicePath(url) + Constants.PATH_SEPARATOR + url.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY);
    }

    private String toUrlPath(URL url) {
        return toCategoryPath(url) + Constants.PATH_SEPARATOR + URL.encode(url.toFullString());
    }
    
    private List<URL> toUrlsWithoutEmpty(URL consumer, List<String> providers) {
    	List<URL> urls = new ArrayList<URL>();
        if (providers != null && providers.size() > 0) {
            for (String provider : providers) {
            	///consumer://10.5.15.223/com.alibaba.dubbo.examples.generic.api.IUserService?application=generic-consumer&category=providers,configurators,routers&dubbo=2.0.0&interface=com.alibaba.dubbo.examples.generic.api.IUserService&methods=get,sayHello&pid=1692&side=consumer&timestamp=1369033117158
            	///dubbo://10.5.15.223:20880/com.alibaba.dubbo.examples.generic.api.IUserService?anyhost=true&application=generic-generic&dubbo=2.0.0&generic=false&interface=com.alibaba.dubbo.examples.generic.api.IUserService&methods=get,sayHello&pid=4960&side=provider&timestamp=1369033030925
                provider = URL.decode(provider);
                if (provider.contains("://")) {
                    URL url = URL.valueOf(provider);
                    if (UrlUtils.isMatch(consumer, url)) {
                        urls.add(url);
                    }
                }
            }
        }
        return urls;
    }

    private List<URL> toUrlsWithEmpty(URL consumer, String path, List<String> providers) {
        List<URL> urls = toUrlsWithoutEmpty(consumer, providers);
        if (urls == null || urls.isEmpty()) {
        	int i = path.lastIndexOf('/');
        	String category = i < 0 ? path : path.substring(i + 1);
        	URL empty = consumer.setProtocol(Constants.EMPTY_PROTOCOL).addParameter(Constants.CATEGORY_KEY, category);
            urls.add(empty);
        }
        return urls;
    }

    static String appendDefaultPort(String address) {
        if (address != null && address.length() > 0) {
            int i = address.indexOf(':');
            if (i < 0) {
                return address + ":" + DEFAULT_ZOOKEEPER_PORT;
            } else if (Integer.parseInt(address.substring(i + 1)) == 0) {
                return address.substring(0, i + 1) + DEFAULT_ZOOKEEPER_PORT;
            }
        }
        return address;
    }

}