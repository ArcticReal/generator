package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EmploymentFound implements Event{

	private List<Employment> employments;

	public EmploymentFound(List<Employment> employments) {
		this.setEmployments(employments);
	}

	public List<Employment> getEmployments()	{
		return employments;
	}

	public void setEmployments(List<Employment> employments)	{
		this.employments = employments;
	}
}
