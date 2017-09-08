package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class PartyContactMechPurposeDeleted implements Event{

	private boolean success;

	public PartyContactMechPurposeDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
