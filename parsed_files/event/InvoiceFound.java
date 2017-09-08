package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceFound implements Event{

	private List<Invoice> invoices;

	public InvoiceFound(List<Invoice> invoices) {
		this.setInvoices(invoices);
	}

	public List<Invoice> getInvoices()	{
		return invoices;
	}

	public void setInvoices(List<Invoice> invoices)	{
		this.invoices = invoices;
	}
}
