package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ProductPromoContentAdded implements Event{

	private boolean success;

	public ProductPromoContentAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
