package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class InventoryItemDeleted implements Event{

	private boolean success;

	public InventoryItemDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
