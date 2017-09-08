package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class OrderItemGroupUpdated implements Event{

	private boolean success;

	public OrderItemGroupUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
