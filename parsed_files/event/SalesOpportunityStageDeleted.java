package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class SalesOpportunityStageDeleted implements Event{

	private boolean success;

	public SalesOpportunityStageDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
