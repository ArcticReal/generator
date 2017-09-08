package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceStatusFound implements Event{

	private List<InvoiceStatus> invoiceStatuss;

	public InvoiceStatusFound(List<InvoiceStatus> invoiceStatuss) {
		this.setInvoiceStatuss(invoiceStatuss);
	}

	public List<InvoiceStatus> getInvoiceStatuss()	{
		return invoiceStatuss;
	}

	public void setInvoiceStatuss(List<InvoiceStatus> invoiceStatuss)	{
		this.invoiceStatuss = invoiceStatuss;
	}
}
