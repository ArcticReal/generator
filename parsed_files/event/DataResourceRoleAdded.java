package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class DataResourceRoleAdded implements Event{

	private boolean success;

	public DataResourceRoleAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
