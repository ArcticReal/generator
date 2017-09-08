package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class RequirementRoleDeleted implements Event{

	private boolean success;

	public RequirementRoleDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
