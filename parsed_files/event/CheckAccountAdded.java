package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class CheckAccountAdded implements Event{

	private boolean success;

	public CheckAccountAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
