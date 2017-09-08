package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class CustRequestTypeUpdated implements Event{

	private boolean success;

	public CustRequestTypeUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
