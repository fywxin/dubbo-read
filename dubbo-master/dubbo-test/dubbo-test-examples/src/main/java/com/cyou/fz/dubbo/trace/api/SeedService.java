package com.cyou.fz.dubbo.trace.api;

import java.util.List;

import com.cyou.fz.dubbo.trace.domain.Node;
import com.cyou.fz.dubbo.trace.domain.Span;

public interface SeedService {

	public void sendSpans(List<Span> spans);
	
	public void sendNodes(List<Node> nodes);
}
