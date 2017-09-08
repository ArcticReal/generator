package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class PartyIdentificationAdded implements Event{

	private boolean success;

	public PartyIdentificationAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
