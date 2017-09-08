package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortNoteFound implements Event{

	private List<WorkEffortNote> workEffortNotes;

	public WorkEffortNoteFound(List<WorkEffortNote> workEffortNotes) {
		this.setWorkEffortNotes(workEffortNotes);
	}

	public List<WorkEffortNote> getWorkEffortNotes()	{
		return workEffortNotes;
	}

	public void setWorkEffortNotes(List<WorkEffortNote> workEffortNotes)	{
		this.workEffortNotes = workEffortNotes;
	}
}
