package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ContentOperationUpdated implements Event{

	private boolean success;

	public ContentOperationUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
