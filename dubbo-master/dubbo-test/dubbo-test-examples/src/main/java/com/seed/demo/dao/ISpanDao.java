package com.seed.demo.dao;

import java.util.List;

import com.cyou.fz.common.mybatis.MyBatisRepository;
import com.seed.demo.po.SpanPo;

@MyBatisRepository
public interface ISpanDao {

	void save(SpanPo spanPo);
	
	List<SpanPo> getBySeed(String seed);
	
	List<SpanPo> getByPid(String pid);
	
	List<SpanPo> getAll();
}
