package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ProductStoreEmailSettingUpdated implements Event{

	private boolean success;

	public ProductStoreEmailSettingUpdated(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
