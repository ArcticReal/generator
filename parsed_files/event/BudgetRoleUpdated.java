package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class BudgetRoleUpdated implements Event{

	private boolean success;

	public BudgetRoleUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
