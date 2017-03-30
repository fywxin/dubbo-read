package com.seed.nodec;

import com.seed.api.NodecService;
import com.seed.api.NodedService;
import com.seed.api.NodeeService;

public class NodecServiceImpl implements NodecService {
	
	private NodedService nodedService;
	
	private NodeeService nodeeService;

	@Override
	public String callDE(String name) {
		name=name+"->C";
		return this.nodeeService.nodeeHandler(this.nodedService.nodedHandler(name));
	}

	public void setNodedService(NodedService nodedService) {
		this.nodedService = nodedService;
	}

	public void setNodeeService(NodeeService nodeeService) {
		this.nodeeService = nodeeService;
	}

}
