package com.skytala.eCommerce.event;

import com.skytala.eCommerce.control.Event;

public class InvoiceItemAttributeDeleted implements Event{

	private boolean success;

	public InvoiceItemAttributeDeleted(boolean success) {
		this.setSuccess(success);
	}

	public boolean isSuccess()	{
		return success;
	}

	public void setSuccess(boolean success)	{
		this.success = success;
	}
}
