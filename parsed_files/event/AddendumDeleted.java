package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class AddendumDeleted implements Event{

	private boolean success;

	public AddendumDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
