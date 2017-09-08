package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortFound implements Event{

	private List<WorkEffort> workEfforts;

	public WorkEffortFound(List<WorkEffort> workEfforts) {
		this.setWorkEfforts(workEfforts);
	}

	public List<WorkEffort> getWorkEfforts()	{
		return workEfforts;
	}

	public void setWorkEfforts(List<WorkEffort> workEfforts)	{
		this.workEfforts = workEfforts;
	}
}
