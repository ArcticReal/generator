package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class FixedAssetRegistrationUpdated implements Event{

	private boolean success;

	public FixedAssetRegistrationUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
