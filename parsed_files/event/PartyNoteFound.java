package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyNoteFound implements Event{

	private List<PartyNote> partyNotes;

	public PartyNoteFound(List<PartyNote> partyNotes) {
		this.setPartyNotes(partyNotes);
	}

	public List<PartyNote> getPartyNotes()	{
		return partyNotes;
	}

	public void setPartyNotes(List<PartyNote> partyNotes)	{
		this.partyNotes = partyNotes;
	}
}
