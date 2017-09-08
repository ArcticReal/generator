package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceItemTypeFound implements Event{

	private List<InvoiceItemType> invoiceItemTypes;

	public InvoiceItemTypeFound(List<InvoiceItemType> invoiceItemTypes) {
		this.setInvoiceItemTypes(invoiceItemTypes);
	}

	public List<InvoiceItemType> getInvoiceItemTypes()	{
		return invoiceItemTypes;
	}

	public void setInvoiceItemTypes(List<InvoiceItemType> invoiceItemTypes)	{
		this.invoiceItemTypes = invoiceItemTypes;
	}
}
