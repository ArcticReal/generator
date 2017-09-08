package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class MarketInterestDeleted implements Event{

	private boolean success;

	public MarketInterestDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
