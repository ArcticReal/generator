package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class TaxAuthorityAssocUpdated implements Event{

	private boolean success;

	public TaxAuthorityAssocUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
