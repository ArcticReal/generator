package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class EmploymentAdded implements Event{

	private boolean success;

	public EmploymentAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
