package com.alibaba.dubbo.remoting.zookeeper.zkclient;

import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkStateListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.zookeeper.ChildListener;
import com.alibaba.dubbo.remoting.zookeeper.StateListener;
import com.alibaba.dubbo.remoting.zookeeper.support.AbstractZookeeperClient;

public class ZkclientZookeeperClient extends AbstractZookeeperClient<IZkChildListener> {

	private final ZkClient client;

	private volatile KeeperState state = KeeperState.SyncConnected;

	public ZkclientZookeeperClient(URL url) {
		super(url);
		client = new ZkClient(url.getBackupAddress());
		/// 转交给 new  @ZookeeperRegistry().new StateListener().recover() 方法处理
		client.subscribeStateChanges(new IZkStateListener() {
			public void handleStateChanged(KeeperState state) throws Exception {
				ZkclientZookeeperClient.this.state = state;
				if (state == KeeperState.Disconnected) {
					stateChanged(StateListener.DISCONNECTED);
				} else if (state == KeeperState.SyncConnected) {
					stateChanged(StateListener.CONNECTED);
				}
			}
			public void handleNewSession() throws Exception {
				stateChanged(StateListener.RECONNECTED);
			}
		});
	}

	public void createPersistent(String path) {
		try {
			client.createPersistent(path, true);
		} catch (ZkNodeExistsException e) {
		}
	}

	public void createEphemeral(String path) {
		try {
			client.createEphemeral(path);
		} catch (ZkNodeExistsException e) {
		}
	}

	public void delete(String path) {
		try {
			client.delete(path);
		} catch (ZkNoNodeException e) {
		}
	}

	public List<String> getChildren(String path) {
		try {
			return client.getChildren(path);
        } catch (ZkNoNodeException e) {
            return null;
        }
	}

	public boolean isConnected() {
		return state == KeeperState.SyncConnected;
	}

	public void doClose() {
		client.close();
	}

	public IZkChildListener createTargetChildListener(String path, final ChildListener listener) {
		return new IZkChildListener() {
			public void handleChildChange(String parentPath, List<String> currentChilds)
					throws Exception {
				/// /dubbo/com.cyou.fz.api.DemoService/providers
				/// dubbo%3A%2F%2F10.5.15.223%3A 20880 %2Fcom.cyou.fz.api.DemoService%3Fanyhost%3Dtrue%26application%3Ddemo-test%26dubbo%3D2.0.0%26generic%3Dfalse%26interface%3Dcom.cyou.fz.api.DemoService%26methods%3DgetNextIdNoCache%2CdoTimeOut%2C%24echo%2CgetTimeOutCallTimes%2CgetNextIdWithCache%2CdoThrowException%2CgetOutLaw%26pid%3D4612%26side%3Dprovider%26timestamp%3D1374652263492
				/// dubbo%3A%2F%2F10.5.15.223%3A 20882 %2Fcom.cyou.fz.api.DemoService%3Fanyhost%3Dtrue%26application%3Ddemo-test%26dubbo%3D2.0.0%26generic%3Dfalse%26interface%3Dcom.cyou.fz.api.DemoService%26methods%3DgetNextIdNoCache%2CdoTimeOut%2C%24echo%2CgetTimeOutCallTimes%2CgetNextIdWithCache%2CdoThrowException%2CgetOutLaw%26pid%3D5896%26side%3Dprovider%26timestamp%3D1374652204491
				listener.childChanged(parentPath, currentChilds);
			}
		};
	}

	public List<String> addTargetChildListener(String path, final IZkChildListener listener) {
		return client.subscribeChildChanges(path, listener);
	}

	public void removeTargetChildListener(String path, IZkChildListener listener) {
		client.unsubscribeChildChanges(path,  listener);
	}

}
