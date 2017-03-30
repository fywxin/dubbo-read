package com.alibaba.dubbo.remoting.zookeeper.support;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.zookeeper.ChildListener;
import com.alibaba.dubbo.remoting.zookeeper.StateListener;
import com.alibaba.dubbo.remoting.zookeeper.ZookeeperClient;

public abstract class AbstractZookeeperClient<TargetChildListener> implements ZookeeperClient {

	protected static final Logger logger = LoggerFactory.getLogger(AbstractZookeeperClient.class);

	private final URL url;

	private final Set<StateListener> stateListeners = new CopyOnWriteArraySet<StateListener>();

	private final ConcurrentMap<String, ConcurrentMap<ChildListener, TargetChildListener>> childListeners = new ConcurrentHashMap<String, ConcurrentMap<ChildListener, TargetChildListener>>();

	private volatile boolean closed = false;

	public AbstractZookeeperClient(URL url) {
		this.url = url;
	}

	@Override
	public URL getUrl() {
		return url;
	}

	@Override
	public void create(String path, boolean ephemeral) {
		int i = path.lastIndexOf('/');
		if (i > 0) {
			create(path.substring(0, i), false);
		}
		if (ephemeral) {
			createEphemeral(path);
		} else {
			createPersistent(path);
		}
	}
	
	protected abstract void createPersistent(String path);

	protected abstract void createEphemeral(String path);
	
	/***
	 * createTargetChildListener(path, listener)
	 * addTargetChildListener(String path, TargetChildListener listener)
	 */
	@Override
	public List<String> addChildListener(String path, final ChildListener listener) {
		ConcurrentMap<ChildListener, TargetChildListener> listeners = childListeners.get(path);
		if (listeners == null) {
			childListeners.putIfAbsent(path, new ConcurrentHashMap<ChildListener, TargetChildListener>());
			listeners = childListeners.get(path);
		}
		TargetChildListener targetListener = listeners.get(listener);
		if (targetListener == null) {
			///包装建立ChildListener 与 IZkChildListener 一对一关联
			listeners.putIfAbsent(listener, createTargetChildListener(path, listener));
			targetListener = listeners.get(listener);
		}
		///在zkClient 中 注册监听原生IZkChildListener 事件
		return addTargetChildListener(path, targetListener);
	}
	
	protected abstract TargetChildListener createTargetChildListener(String path, ChildListener listener);
	
	protected abstract List<String> addTargetChildListener(String path, TargetChildListener listener);

	@Override
	public void removeChildListener(String path, ChildListener listener) {
		ConcurrentMap<ChildListener, TargetChildListener> listeners = childListeners.get(path);
		if (listeners != null) {
			TargetChildListener targetListener = listeners.remove(listener);
			if (targetListener != null) {
				removeTargetChildListener(path, targetListener);
			}
		}
	}
	
	protected abstract void removeTargetChildListener(String path, TargetChildListener listener);

	@Override
	public void addStateListener(StateListener listener) {
		stateListeners.add(listener);
	}

	@Override
	public void removeStateListener(StateListener listener) {
		stateListeners.remove(listener);
	}

	public Set<StateListener> getSessionListeners() {
		return stateListeners;
	}

	protected void stateChanged(int state) {
		for (StateListener sessionListener : getSessionListeners()) {
			sessionListener.stateChanged(state);
		}
	}

	@Override
	public void close() {
		if (closed) {
			return;
		}
		closed = true;
		try {
			doClose();
		} catch (Throwable t) {
			logger.warn(t.getMessage(), t);
		}
	}

	protected abstract void doClose();

	

	

}
