package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class InvoiceNoteFound implements Event{

	private List<InvoiceNote> invoiceNotes;

	public InvoiceNoteFound(List<InvoiceNote> invoiceNotes) {
		this.setInvoiceNotes(invoiceNotes);
	}

	public List<InvoiceNote> getInvoiceNotes()	{
		return invoiceNotes;
	}

	public void setInvoiceNotes(List<InvoiceNote> invoiceNotes)	{
		this.invoiceNotes = invoiceNotes;
	}
}
