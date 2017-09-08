package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class QuoteNoteFound implements Event{

	private List<QuoteNote> quoteNotes;

	public QuoteNoteFound(List<QuoteNote> quoteNotes) {
		this.setQuoteNotes(quoteNotes);
	}

	public List<QuoteNote> getQuoteNotes()	{
		return quoteNotes;
	}

	public void setQuoteNotes(List<QuoteNote> quoteNotes)	{
		this.quoteNotes = quoteNotes;
	}
}
