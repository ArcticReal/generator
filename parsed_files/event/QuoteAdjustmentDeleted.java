package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class QuoteAdjustmentDeleted implements Event{

	private boolean success;

	public QuoteAdjustmentDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
