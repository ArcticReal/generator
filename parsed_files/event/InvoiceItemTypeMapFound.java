package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceItemTypeMapFound implements Event{

	private List<InvoiceItemTypeMap> invoiceItemTypeMaps;

	public InvoiceItemTypeMapFound(List<InvoiceItemTypeMap> invoiceItemTypeMaps) {
		this.setInvoiceItemTypeMaps(invoiceItemTypeMaps);
	}

	public List<InvoiceItemTypeMap> getInvoiceItemTypeMaps()	{
		return invoiceItemTypeMaps;
	}

	public void setInvoiceItemTypeMaps(List<InvoiceItemTypeMap> invoiceItemTypeMaps)	{
		this.invoiceItemTypeMaps = invoiceItemTypeMaps;
	}
}
