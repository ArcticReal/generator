package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ProductOrderItemAdded implements Event{

	private boolean success;

	public ProductOrderItemAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
