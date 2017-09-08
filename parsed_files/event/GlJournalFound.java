package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class GlJournalFound implements Event{

	private List<GlJournal> glJournals;

	public GlJournalFound(List<GlJournal> glJournals) {
		this.setGlJournals(glJournals);
	}

	public List<GlJournal> getGlJournals()	{
		return glJournals;
	}

	public void setGlJournals(List<GlJournal> glJournals)	{
		this.glJournals = glJournals;
	}
}
