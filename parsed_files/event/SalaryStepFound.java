package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SalaryStepFound implements Event{

	private List<SalaryStep> salarySteps;

	public SalaryStepFound(List<SalaryStep> salarySteps) {
		this.setSalarySteps(salarySteps);
	}

	public List<SalaryStep> getSalarySteps()	{
		return salarySteps;
	}

	public void setSalarySteps(List<SalaryStep> salarySteps)	{
		this.salarySteps = salarySteps;
	}
}
