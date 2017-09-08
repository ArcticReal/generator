package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class GoodIdentificationAdded implements Event{

	private boolean success;

	public GoodIdentificationAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
