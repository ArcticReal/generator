package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class RoleTypeAttrDeleted implements Event{

	private boolean success;

	public RoleTypeAttrDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
