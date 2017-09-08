package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class FinAccountTransAttributeUpdated implements Event{

	private boolean success;

	public FinAccountTransAttributeUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
