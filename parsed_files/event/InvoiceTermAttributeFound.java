package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceTermAttributeFound implements Event{

	private List<InvoiceTermAttribute> invoiceTermAttributes;

	public InvoiceTermAttributeFound(List<InvoiceTermAttribute> invoiceTermAttributes) {
		this.setInvoiceTermAttributes(invoiceTermAttributes);
	}

	public List<InvoiceTermAttribute> getInvoiceTermAttributes()	{
		return invoiceTermAttributes;
	}

	public void setInvoiceTermAttributes(List<InvoiceTermAttribute> invoiceTermAttributes)	{
		this.invoiceTermAttributes = invoiceTermAttributes;
	}
}
