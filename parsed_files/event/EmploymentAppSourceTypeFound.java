package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EmploymentAppSourceTypeFound implements Event{

	private List<EmploymentAppSourceType> employmentAppSourceTypes;

	public EmploymentAppSourceTypeFound(List<EmploymentAppSourceType> employmentAppSourceTypes) {
		this.setEmploymentAppSourceTypes(employmentAppSourceTypes);
	}

	public List<EmploymentAppSourceType> getEmploymentAppSourceTypes()	{
		return employmentAppSourceTypes;
	}

	public void setEmploymentAppSourceTypes(List<EmploymentAppSourceType> employmentAppSourceTypes)	{
		this.employmentAppSourceTypes = employmentAppSourceTypes;
	}
}
