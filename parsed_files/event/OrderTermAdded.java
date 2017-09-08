package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class OrderTermAdded implements Event{

	private boolean success;

	public OrderTermAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
