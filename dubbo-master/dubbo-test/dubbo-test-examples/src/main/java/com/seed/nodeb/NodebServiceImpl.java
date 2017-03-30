package com.seed.nodeb;

import com.seed.api.NodebService;
import com.seed.api.NodedService;

public class NodebServiceImpl implements NodebService {
	
	private NodedService nodedService;

	@Override
	public String nodebHandler(String name) {
		
		return this.nodedService.nodedHandler(name+"->B");
	}

	public void setNodedService(NodedService nodedService) {
		this.nodedService = nodedService;
	}

	
}
