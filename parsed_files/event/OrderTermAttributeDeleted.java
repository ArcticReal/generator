package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class OrderTermAttributeDeleted implements Event{

	private boolean success;

	public OrderTermAttributeDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
