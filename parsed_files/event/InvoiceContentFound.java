package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceContentFound implements Event{

	private List<InvoiceContent> invoiceContents;

	public InvoiceContentFound(List<InvoiceContent> invoiceContents) {
		this.setInvoiceContents(invoiceContents);
	}

	public List<InvoiceContent> getInvoiceContents()	{
		return invoiceContents;
	}

	public void setInvoiceContents(List<InvoiceContent> invoiceContents)	{
		this.invoiceContents = invoiceContents;
	}
}
