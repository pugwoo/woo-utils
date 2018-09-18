package com.pugwoo.wooutils.redis;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Student {

	private Long id;
	private String name;
	private Date birth;
	private List<BigDecimal> score;

	public void setId(Long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setBirth(Date birth) {
		this.birth = birth;
	}

	public void setScore(List<BigDecimal> score) {
		this.score = score;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Date getBirth() {
		return birth;
	}

	public List<BigDecimal> getScore() {
		return score;
	}

}
