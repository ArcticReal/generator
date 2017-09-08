package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class VendorAdded implements Event{

	private boolean success;

	public VendorAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
