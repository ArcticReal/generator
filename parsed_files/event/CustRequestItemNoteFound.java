package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CustRequestItemNoteFound implements Event{

	private List<CustRequestItemNote> custRequestItemNotes;

	public CustRequestItemNoteFound(List<CustRequestItemNote> custRequestItemNotes) {
		this.setCustRequestItemNotes(custRequestItemNotes);
	}

	public List<CustRequestItemNote> getCustRequestItemNotes()	{
		return custRequestItemNotes;
	}

	public void setCustRequestItemNotes(List<CustRequestItemNote> custRequestItemNotes)	{
		this.custRequestItemNotes = custRequestItemNotes;
	}
}
