package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class DeductionTypeUpdated implements Event{

	private boolean success;

	public DeductionTypeUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
