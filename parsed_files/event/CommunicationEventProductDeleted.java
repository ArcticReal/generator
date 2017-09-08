package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class CommunicationEventProductDeleted implements Event{

	private boolean success;

	public CommunicationEventProductDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
