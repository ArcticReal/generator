package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class WorkEffortContentTypeUpdated implements Event{

	private boolean success;

	public WorkEffortContentTypeUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
