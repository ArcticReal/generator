package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class EmplLeaveDeleted implements Event{

	private boolean success;

	public EmplLeaveDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
