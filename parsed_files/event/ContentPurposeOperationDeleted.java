package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ContentPurposeOperationDeleted implements Event{

	private boolean success;

	public ContentPurposeOperationDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
