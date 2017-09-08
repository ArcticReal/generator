package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class PicklistRoleAdded implements Event{

	private boolean success;

	public PicklistRoleAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
