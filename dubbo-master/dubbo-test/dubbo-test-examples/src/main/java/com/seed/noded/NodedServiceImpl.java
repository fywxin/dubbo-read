package com.seed.noded;

import com.seed.api.NodedService;
import com.seed.api.NodefService;

public class NodedServiceImpl implements NodedService {
	
	private NodefService nodefService;

	@Override
	public String nodedHandler(String name) {
		return this.nodefService.nodefHandler(name+"->D");
	}

	public void setNodefService(NodefService nodefService) {
		this.nodefService = nodefService;
	}

	
}
