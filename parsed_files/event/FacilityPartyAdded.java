package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class FacilityPartyAdded implements Event{

	private boolean success;

	public FacilityPartyAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
