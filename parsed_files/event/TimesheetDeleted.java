package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class TimesheetDeleted implements Event{

	private boolean success;

	public TimesheetDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
