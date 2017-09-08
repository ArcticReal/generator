package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class GlAccountCategoryTypeDeleted implements Event{

	private boolean success;

	public GlAccountCategoryTypeDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
