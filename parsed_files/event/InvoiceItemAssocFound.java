package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceItemAssocFound implements Event{

	private List<InvoiceItemAssoc> invoiceItemAssocs;

	public InvoiceItemAssocFound(List<InvoiceItemAssoc> invoiceItemAssocs) {
		this.setInvoiceItemAssocs(invoiceItemAssocs);
	}

	public List<InvoiceItemAssoc> getInvoiceItemAssocs()	{
		return invoiceItemAssocs;
	}

	public void setInvoiceItemAssocs(List<InvoiceItemAssoc> invoiceItemAssocs)	{
		this.invoiceItemAssocs = invoiceItemAssocs;
	}
}
