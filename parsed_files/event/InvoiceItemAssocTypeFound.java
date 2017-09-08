package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceItemAssocTypeFound implements Event{

	private List<InvoiceItemAssocType> invoiceItemAssocTypes;

	public InvoiceItemAssocTypeFound(List<InvoiceItemAssocType> invoiceItemAssocTypes) {
		this.setInvoiceItemAssocTypes(invoiceItemAssocTypes);
	}

	public List<InvoiceItemAssocType> getInvoiceItemAssocTypes()	{
		return invoiceItemAssocTypes;
	}

	public void setInvoiceItemAssocTypes(List<InvoiceItemAssocType> invoiceItemAssocTypes)	{
		this.invoiceItemAssocTypes = invoiceItemAssocTypes;
	}
}
