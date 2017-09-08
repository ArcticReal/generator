package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EmploymentAppFound implements Event{

	private List<EmploymentApp> employmentApps;

	public EmploymentAppFound(List<EmploymentApp> employmentApps) {
		this.setEmploymentApps(employmentApps);
	}

	public List<EmploymentApp> getEmploymentApps()	{
		return employmentApps;
	}

	public void setEmploymentApps(List<EmploymentApp> employmentApps)	{
		this.employmentApps = employmentApps;
	}
}
