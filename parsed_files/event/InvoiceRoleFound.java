package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceRoleFound implements Event{

	private List<InvoiceRole> invoiceRoles;

	public InvoiceRoleFound(List<InvoiceRole> invoiceRoles) {
		this.setInvoiceRoles(invoiceRoles);
	}

	public List<InvoiceRole> getInvoiceRoles()	{
		return invoiceRoles;
	}

	public void setInvoiceRoles(List<InvoiceRole> invoiceRoles)	{
		this.invoiceRoles = invoiceRoles;
	}
}
