package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceTermFound implements Event{

	private List<InvoiceTerm> invoiceTerms;

	public InvoiceTermFound(List<InvoiceTerm> invoiceTerms) {
		this.setInvoiceTerms(invoiceTerms);
	}

	public List<InvoiceTerm> getInvoiceTerms()	{
		return invoiceTerms;
	}

	public void setInvoiceTerms(List<InvoiceTerm> invoiceTerms)	{
		this.invoiceTerms = invoiceTerms;
	}
}
