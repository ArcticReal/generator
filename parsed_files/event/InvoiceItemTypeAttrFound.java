package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceItemTypeAttrFound implements Event{

	private List<InvoiceItemTypeAttr> invoiceItemTypeAttrs;

	public InvoiceItemTypeAttrFound(List<InvoiceItemTypeAttr> invoiceItemTypeAttrs) {
		this.setInvoiceItemTypeAttrs(invoiceItemTypeAttrs);
	}

	public List<InvoiceItemTypeAttr> getInvoiceItemTypeAttrs()	{
		return invoiceItemTypeAttrs;
	}

	public void setInvoiceItemTypeAttrs(List<InvoiceItemTypeAttr> invoiceItemTypeAttrs)	{
		this.invoiceItemTypeAttrs = invoiceItemTypeAttrs;
	}
}
