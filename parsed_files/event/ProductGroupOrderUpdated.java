package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ProductGroupOrderUpdated implements Event{

	private boolean success;

	public ProductGroupOrderUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
