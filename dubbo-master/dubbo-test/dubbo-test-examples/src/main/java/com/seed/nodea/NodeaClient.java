package com.seed.nodea;

import com.seed.api.NodebService;
import com.seed.api.NodecService;


public class NodeaClient {
	
	private NodebService nodebService;
	
	private NodecService nodecService;
	
	public String needBC(String name) {
		name = name+"->A";
		return this.nodecService.callDE(this.nodebService.nodebHandler(name));
		//return this.nodebService.nodebHandler(name);
	}

	public void setNodebService(NodebService nodebService) {
		this.nodebService = nodebService;
	}

	public void setNodecService(NodecService nodecService) {
		this.nodecService = nodecService;
	}

}
