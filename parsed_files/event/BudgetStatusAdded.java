package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class BudgetStatusAdded implements Event{

	private boolean success;

	public BudgetStatusAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
