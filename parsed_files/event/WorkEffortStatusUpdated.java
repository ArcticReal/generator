package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class WorkEffortStatusUpdated implements Event{

	private boolean success;

	public WorkEffortStatusUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
