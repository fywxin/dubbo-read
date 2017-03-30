package com.seed.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.springframework.beans.factory.annotation.Autowired;

import com.cyou.fz.dubbo.trace.api.SeedService;
import com.cyou.fz.dubbo.trace.domain.Node;
import com.cyou.fz.dubbo.trace.domain.Span;
import com.seed.demo.dao.ISpanDao;
import com.seed.demo.po.SpanPo;

public class SeedServiceMySqlImpl implements SeedService {
	
	private final ArrayBlockingQueue<Span> spanQueue = new ArrayBlockingQueue<Span>(1024);
	
	private final ArrayBlockingQueue<Node> nodeQueue= new ArrayBlockingQueue<Node>(2048);
	
	private volatile boolean flag = true;
	
	@Autowired
	private ISpanDao spanDao;
	
	private Long interval = 1000L;
	
	public SeedServiceMySqlImpl(){
		new SaveThread(spanDao).start();
	}

	@Override
	public void sendSpans(List<Span> spans) {
		System.out.println("接受到spans:"+spans);
		spanQueue.addAll(spans);
	}

	@Override
	public void sendNodes(List<Node> nodes) {
		System.out.println("接受到nodes:"+nodes);
		nodeQueue.addAll(nodes);
	}

	
	class SaveThread extends Thread{
		
		private final List<Span> spanCache = new ArrayList<Span>();
		
		private final List<Node> nodeCache = new ArrayList<Node>();
		
		ISpanDao spanDao;
		
		public SaveThread(ISpanDao spanDao){
			this.spanDao = spanDao;
		}
		
		@Override
		public void run() {
			while(flag){
				if(spanQueue.size() > 0){
					spanQueue.drainTo(spanCache);
					System.out.println("spanCache.size = "+spanCache.size());
					for(Span span : spanCache){
						System.out.println("do save : "+span);
						spanDao.save(new SpanPo(span));
					}
					spanCache.clear();
				}else{
					try {
						Thread.sleep(SeedServiceMySqlImpl.this.interval);
					} catch (InterruptedException e) {}
				}
			}
		}
		
	}
}
