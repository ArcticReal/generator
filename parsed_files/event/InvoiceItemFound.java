package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceItemFound implements Event{

	private List<InvoiceItem> invoiceItems;

	public InvoiceItemFound(List<InvoiceItem> invoiceItems) {
		this.setInvoiceItems(invoiceItems);
	}

	public List<InvoiceItem> getInvoiceItems()	{
		return invoiceItems;
	}

	public void setInvoiceItems(List<InvoiceItem> invoiceItems)	{
		this.invoiceItems = invoiceItems;
	}
}
