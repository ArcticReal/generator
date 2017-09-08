package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortContentFound implements Event{

	private List<WorkEffortContent> workEffortContents;

	public WorkEffortContentFound(List<WorkEffortContent> workEffortContents) {
		this.setWorkEffortContents(workEffortContents);
	}

	public List<WorkEffortContent> getWorkEffortContents()	{
		return workEffortContents;
	}

	public void setWorkEffortContents(List<WorkEffortContent> workEffortContents)	{
		this.workEffortContents = workEffortContents;
	}
}
