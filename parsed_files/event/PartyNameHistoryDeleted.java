package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class PartyNameHistoryDeleted implements Event{

	private boolean success;

	public PartyNameHistoryDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
