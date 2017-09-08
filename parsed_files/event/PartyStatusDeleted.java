package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class PartyStatusDeleted implements Event{

	private boolean success;

	public PartyStatusDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
