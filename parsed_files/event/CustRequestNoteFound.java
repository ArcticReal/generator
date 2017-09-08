package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CustRequestNoteFound implements Event{

	private List<CustRequestNote> custRequestNotes;

	public CustRequestNoteFound(List<CustRequestNote> custRequestNotes) {
		this.setCustRequestNotes(custRequestNotes);
	}

	public List<CustRequestNote> getCustRequestNotes()	{
		return custRequestNotes;
	}

	public void setCustRequestNotes(List<CustRequestNote> custRequestNotes)	{
		this.custRequestNotes = custRequestNotes;
	}
}
