package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ProductFacilityLocationAdded implements Event{

	private boolean success;

	public ProductFacilityLocationAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
