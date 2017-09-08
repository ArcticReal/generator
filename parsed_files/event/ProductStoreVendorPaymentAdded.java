package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class ProductStoreVendorPaymentAdded implements Event{

	private boolean success;

	public ProductStoreVendorPaymentAdded(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
