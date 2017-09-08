package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class SubscriptionActivityUpdated implements Event{

	private boolean success;

	public SubscriptionActivityUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
