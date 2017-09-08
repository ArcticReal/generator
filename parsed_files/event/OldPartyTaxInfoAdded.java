package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class OldPartyTaxInfoAdded implements Event{

	private boolean success;

	public OldPartyTaxInfoAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
