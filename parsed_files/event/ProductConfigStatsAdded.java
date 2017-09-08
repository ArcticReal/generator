package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ProductConfigStatsAdded implements Event{

	private boolean success;

	public ProductConfigStatsAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
