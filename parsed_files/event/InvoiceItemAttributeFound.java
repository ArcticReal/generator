package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceItemAttributeFound implements Event{

	private List<InvoiceItemAttribute> invoiceItemAttributes;

	public InvoiceItemAttributeFound(List<InvoiceItemAttribute> invoiceItemAttributes) {
		this.setInvoiceItemAttributes(invoiceItemAttributes);
	}

	public List<InvoiceItemAttribute> getInvoiceItemAttributes()	{
		return invoiceItemAttributes;
	}

	public void setInvoiceItemAttributes(List<InvoiceItemAttribute> invoiceItemAttributes)	{
		this.invoiceItemAttributes = invoiceItemAttributes;
	}
}
