package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceAttributeFound implements Event{

	private List<InvoiceAttribute> invoiceAttributes;

	public InvoiceAttributeFound(List<InvoiceAttribute> invoiceAttributes) {
		this.setInvoiceAttributes(invoiceAttributes);
	}

	public List<InvoiceAttribute> getInvoiceAttributes()	{
		return invoiceAttributes;
	}

	public void setInvoiceAttributes(List<InvoiceAttribute> invoiceAttributes)	{
		this.invoiceAttributes = invoiceAttributes;
	}
}
