package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class JobRequisitionUpdated implements Event{

	private boolean success;

	public JobRequisitionUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
