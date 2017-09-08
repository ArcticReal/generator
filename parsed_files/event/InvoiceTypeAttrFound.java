package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceTypeAttrFound implements Event{

	private List<InvoiceTypeAttr> invoiceTypeAttrs;

	public InvoiceTypeAttrFound(List<InvoiceTypeAttr> invoiceTypeAttrs) {
		this.setInvoiceTypeAttrs(invoiceTypeAttrs);
	}

	public List<InvoiceTypeAttr> getInvoiceTypeAttrs()	{
		return invoiceTypeAttrs;
	}

	public void setInvoiceTypeAttrs(List<InvoiceTypeAttr> invoiceTypeAttrs)	{
		this.invoiceTypeAttrs = invoiceTypeAttrs;
	}
}
