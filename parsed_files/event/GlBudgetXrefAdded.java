package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class GlBudgetXrefAdded implements Event{

	private boolean success;

	public GlBudgetXrefAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
