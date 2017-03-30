package com.cluster.test.impl;

import com.cluster.test.api.AuthKeeper;

public class ServerAuthKeeper implements AuthKeeper {

	@Override
	public boolean refresh(Object datas) {
		System.out.println(datas);
		
		return true;
	}

}
