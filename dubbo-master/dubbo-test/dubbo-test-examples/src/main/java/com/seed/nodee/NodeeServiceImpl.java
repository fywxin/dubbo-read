package com.seed.nodee;

import com.seed.api.NodeeService;

public class NodeeServiceImpl implements NodeeService {

	@Override
	public String nodeeHandler(String name) {
		return name+"->E";
	}

}
