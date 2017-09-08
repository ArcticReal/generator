package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class PerfReviewAdded implements Event{

	private boolean success;

	public PerfReviewAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
