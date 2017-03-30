package com.seed.nodef;

import com.seed.api.NodefService;

public class NodefServiceImpl implements NodefService {

	@Override
	public String nodefHandler(String name) {
		return name+"->F";
	}

}
