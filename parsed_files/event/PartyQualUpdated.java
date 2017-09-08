package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class PartyQualUpdated implements Event{

	private boolean success;

	public PartyQualUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
