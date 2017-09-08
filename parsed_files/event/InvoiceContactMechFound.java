package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceContactMechFound implements Event{

	private List<InvoiceContactMech> invoiceContactMechs;

	public InvoiceContactMechFound(List<InvoiceContactMech> invoiceContactMechs) {
		this.setInvoiceContactMechs(invoiceContactMechs);
	}

	public List<InvoiceContactMech> getInvoiceContactMechs()	{
		return invoiceContactMechs;
	}

	public void setInvoiceContactMechs(List<InvoiceContactMech> invoiceContactMechs)	{
		this.invoiceContactMechs = invoiceContactMechs;
	}
}
