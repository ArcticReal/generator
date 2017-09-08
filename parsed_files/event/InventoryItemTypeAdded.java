package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class InventoryItemTypeAdded implements Event{

	private boolean success;

	public InventoryItemTypeAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
