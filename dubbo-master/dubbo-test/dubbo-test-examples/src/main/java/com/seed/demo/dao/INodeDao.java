package com.seed.demo.dao;

import java.util.List;

import com.seed.demo.po.NodePo;

public interface INodeDao {

	void save(NodePo nodePo);
	
	List<NodePo> getBySpanId(String spanId);
	
	List<NodePo> getByPid(String pid);
	
	List<NodePo> getAll();
}
