package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class CreditCardTypeGlAccountDeleted implements Event{

	private boolean success;

	public CreditCardTypeGlAccountDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
