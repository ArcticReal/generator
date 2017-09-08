package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ResponsibilityTypeUpdated implements Event{

	private boolean success;

	public ResponsibilityTypeUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
