package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class OrderItemUpdated implements Event{

	private boolean success;

	public OrderItemUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
