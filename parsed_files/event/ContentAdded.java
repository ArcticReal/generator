package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ContentAdded implements Event{

	private boolean success;

	public ContentAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
