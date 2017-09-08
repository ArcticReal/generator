package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class PaymentContentTypeUpdated implements Event{

	private boolean success;

	public PaymentContentTypeUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
