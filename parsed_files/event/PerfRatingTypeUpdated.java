package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class PerfRatingTypeUpdated implements Event{

	private boolean success;

	public PerfRatingTypeUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
