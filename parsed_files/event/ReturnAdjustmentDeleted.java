package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ReturnAdjustmentDeleted implements Event{

	private boolean success;

	public ReturnAdjustmentDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
