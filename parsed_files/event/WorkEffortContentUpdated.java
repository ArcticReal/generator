package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class WorkEffortContentUpdated implements Event{

	private boolean success;

	public WorkEffortContentUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
