package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class TestFound implements Event{

	private List<Test> tests;

	public TestFound(List<Test> tests) {
		this.setTests(tests);
	}

	public List<Test> getTests()	{
		return tests;
	}

	public void setTests(List<Test> tests)	{
		this.tests = tests;
	}
}
