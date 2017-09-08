package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class AgreementGeographicalApplicDeleted implements Event{

	private boolean success;

	public AgreementGeographicalApplicDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
