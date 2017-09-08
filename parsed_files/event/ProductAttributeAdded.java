package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ProductAttributeAdded implements Event{

	private boolean success;

	public ProductAttributeAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
