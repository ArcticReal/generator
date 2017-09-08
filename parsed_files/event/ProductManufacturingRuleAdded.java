package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ProductManufacturingRuleAdded implements Event{

	private boolean success;

	public ProductManufacturingRuleAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
