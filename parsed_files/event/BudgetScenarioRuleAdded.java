package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class BudgetScenarioRuleAdded implements Event{

	private boolean success;

	public BudgetScenarioRuleAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
