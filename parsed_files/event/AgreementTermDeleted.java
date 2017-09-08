package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class AgreementTermDeleted implements Event{

	private boolean success;

	public AgreementTermDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
