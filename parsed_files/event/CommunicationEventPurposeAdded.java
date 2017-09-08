package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class CommunicationEventPurposeAdded implements Event{

	private boolean success;

	public CommunicationEventPurposeAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
