package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ContainerGeoPointUpdated implements Event{

	private boolean success;

	public ContainerGeoPointUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
