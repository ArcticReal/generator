package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class TrackingCodeVisitDeleted implements Event{

	private boolean success;

	public TrackingCodeVisitDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
