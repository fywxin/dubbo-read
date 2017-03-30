package com.alibaba.dubbo.examples.wjs.common;

import java.io.Serializable;

public class MyBean implements Serializable {

	private static final long serialVersionUID = -2321L;

	private Long id;
	
	private String name;
	
	private Integer age;
	
	private Boolean sex;
	
	private String isLink=null;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public Boolean getSex() {
		return sex;
	}

	public void setSex(Boolean sex) {
		this.sex = sex;
	}

	public String getIsLink() {
		return isLink;
	}

	public void setIsLink(String isLink) {
		this.isLink = isLink;
	}

	@Override
	public String toString() {
		return "MyBean [id=" + id + ", name=" + name + ", age=" + age
				+ ", sex=" + sex + ", isLink=" + isLink + "]";
	}
	
}
