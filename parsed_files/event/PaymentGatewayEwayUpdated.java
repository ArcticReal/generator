package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class PaymentGatewayEwayUpdated implements Event{

	private boolean success;

	public PaymentGatewayEwayUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
