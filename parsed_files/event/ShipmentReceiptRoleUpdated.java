package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ShipmentReceiptRoleUpdated implements Event{

	private boolean success;

	public ShipmentReceiptRoleUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
