package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DeductionFound implements Event{

	private List<Deduction> deductions;

	public DeductionFound(List<Deduction> deductions) {
		this.setDeductions(deductions);
	}

	public List<Deduction> getDeductions()	{
		return deductions;
	}

	public void setDeductions(List<Deduction> deductions)	{
		this.deductions = deductions;
	}
}
