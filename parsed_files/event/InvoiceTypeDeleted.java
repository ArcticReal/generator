package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class InvoiceTypeDeleted implements Event{

	private boolean success;

	public InvoiceTypeDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
