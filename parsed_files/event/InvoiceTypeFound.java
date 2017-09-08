package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceTypeFound implements Event{

	private List<InvoiceType> invoiceTypes;

	public InvoiceTypeFound(List<InvoiceType> invoiceTypes) {
		this.setInvoiceTypes(invoiceTypes);
	}

	public List<InvoiceType> getInvoiceTypes()	{
		return invoiceTypes;
	}

	public void setInvoiceTypes(List<InvoiceType> invoiceTypes)	{
		this.invoiceTypes = invoiceTypes;
	}
}
