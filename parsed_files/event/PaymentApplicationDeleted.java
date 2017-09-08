package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class PaymentApplicationDeleted implements Event{

	private boolean success;

	public PaymentApplicationDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
