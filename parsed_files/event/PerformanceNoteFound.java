package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PerformanceNoteFound implements Event{

	private List<PerformanceNote> performanceNotes;

	public PerformanceNoteFound(List<PerformanceNote> performanceNotes) {
		this.setPerformanceNotes(performanceNotes);
	}

	public List<PerformanceNote> getPerformanceNotes()	{
		return performanceNotes;
	}

	public void setPerformanceNotes(List<PerformanceNote> performanceNotes)	{
		this.performanceNotes = performanceNotes;
	}
}
