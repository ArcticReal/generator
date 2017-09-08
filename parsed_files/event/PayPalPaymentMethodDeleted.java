package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class PayPalPaymentMethodDeleted implements Event{

	private boolean success;

	public PayPalPaymentMethodDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
