package com.cyou.fz.dubbo.trace;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.cyou.fz.dubbo.trace.api.SeedService;
import com.cyou.fz.dubbo.trace.domain.Node;
import com.cyou.fz.dubbo.trace.domain.Span;

public class SeedCollector {
	
	private static final Logger logger = LoggerFactory.getLogger(SeedCollector.class);
	
	private final ArrayBlockingQueue<Span> spanQueue = new ArrayBlockingQueue<Span>(1024);
	
	private final ArrayBlockingQueue<Node> nodeQueue= new ArrayBlockingQueue<Node>(2048);
	
	private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3, new NamedThreadFactory("DubboSeedSendTimer", true));
	
	private final long monitorInterval = 6000L;
	
	private SeedService seedService;
	
	private final List<Span> spanCache = new ArrayList<Span>();
	
	private final List<Node> nodeCache = new ArrayList<Node>();
	
	public SeedCollector(){
		scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                // 收集统计信息
                try {
                    send();
                } catch (Throwable t) { // 防御性容错
                    logger.error("Unexpected error occur at send statistic, cause: " + t.getMessage(), t);
                }
            }
        }, monitorInterval, monitorInterval, TimeUnit.MILLISECONDS);
	}

	public void collectSpan(Span span){
		spanQueue.add(span);
	}
	
	public void collectNode(Node node){
		nodeQueue.add(node);
	}
	
	public void send(){
		spanQueue.drainTo(spanCache);
		nodeQueue.drainTo(nodeCache);
		if(spanCache.size() > 0){
			this.seedService.sendSpans(spanCache);
			spanCache.clear();
		}
		if(nodeCache.size() > 0){
			this.seedService.sendNodes(nodeCache);
			nodeCache.clear();
		}
	}

	public void setSeedService(SeedService seedService) {
		this.seedService = seedService;
	}
}
