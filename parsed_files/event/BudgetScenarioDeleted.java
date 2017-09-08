package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class BudgetScenarioDeleted implements Event{

	private boolean success;

	public BudgetScenarioDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
