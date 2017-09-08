package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ShipmentTypeAdded implements Event{

	private boolean success;

	public ShipmentTypeAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
