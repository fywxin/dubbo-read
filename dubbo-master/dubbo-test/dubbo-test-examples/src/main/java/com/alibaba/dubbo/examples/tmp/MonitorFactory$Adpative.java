package com.alibaba.dubbo.examples.tmp;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.monitor.Monitor;
import com.alibaba.dubbo.monitor.MonitorFactory;

public class MonitorFactory$Adpative implements MonitorFactory {
	
	public Monitor getMonitor(URL arg0) {
		if (arg0 == null)
			throw new IllegalArgumentException("url == null");
		URL url = arg0;
		
		String extName = (url.getProtocol() == null ? "dubbo" : url.getProtocol());
		if (extName == null)
			throw new IllegalStateException("Fail to get extension(MonitorFactory) name from url("+ url.toString() + ") use keys([protocol])");
		
		MonitorFactory extension = (MonitorFactory) ExtensionLoader.getExtensionLoader(MonitorFactory.class).getExtension(extName);
		return extension.getMonitor(arg0);
	}
}
