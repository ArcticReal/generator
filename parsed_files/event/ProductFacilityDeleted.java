package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ProductFacilityDeleted implements Event{

	private boolean success;

	public ProductFacilityDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
