package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceItemTypeGlAccountFound implements Event{

	private List<InvoiceItemTypeGlAccount> invoiceItemTypeGlAccounts;

	public InvoiceItemTypeGlAccountFound(List<InvoiceItemTypeGlAccount> invoiceItemTypeGlAccounts) {
		this.setInvoiceItemTypeGlAccounts(invoiceItemTypeGlAccounts);
	}

	public List<InvoiceItemTypeGlAccount> getInvoiceItemTypeGlAccounts()	{
		return invoiceItemTypeGlAccounts;
	}

	public void setInvoiceItemTypeGlAccounts(List<InvoiceItemTypeGlAccount> invoiceItemTypeGlAccounts)	{
		this.invoiceItemTypeGlAccounts = invoiceItemTypeGlAccounts;
	}
}
