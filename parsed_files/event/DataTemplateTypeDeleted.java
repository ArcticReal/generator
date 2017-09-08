package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class DataTemplateTypeDeleted implements Event{

	private boolean success;

	public DataTemplateTypeDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
