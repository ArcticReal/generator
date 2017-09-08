package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class TaxAuthorityRateTypeAdded implements Event{

	private boolean success;

	public TaxAuthorityRateTypeAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
