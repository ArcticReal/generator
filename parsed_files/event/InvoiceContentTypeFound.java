package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceContentTypeFound implements Event{

	private List<InvoiceContentType> invoiceContentTypes;

	public InvoiceContentTypeFound(List<InvoiceContentType> invoiceContentTypes) {
		this.setInvoiceContentTypes(invoiceContentTypes);
	}

	public List<InvoiceContentType> getInvoiceContentTypes()	{
		return invoiceContentTypes;
	}

	public void setInvoiceContentTypes(List<InvoiceContentType> invoiceContentTypes)	{
		this.invoiceContentTypes = invoiceContentTypes;
	}
}
