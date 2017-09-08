package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PayrollPreferenceFound implements Event{

	private List<PayrollPreference> payrollPreferences;

	public PayrollPreferenceFound(List<PayrollPreference> payrollPreferences) {
		this.setPayrollPreferences(payrollPreferences);
	}

	public List<PayrollPreference> getPayrollPreferences()	{
		return payrollPreferences;
	}

	public void setPayrollPreferences(List<PayrollPreference> payrollPreferences)	{
		this.payrollPreferences = payrollPreferences;
	}
}
