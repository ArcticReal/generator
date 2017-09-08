package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortTransBoxFound implements Event{

	private List<WorkEffortTransBox> workEffortTransBoxs;

	public WorkEffortTransBoxFound(List<WorkEffortTransBox> workEffortTransBoxs) {
		this.setWorkEffortTransBoxs(workEffortTransBoxs);
	}

	public List<WorkEffortTransBox> getWorkEffortTransBoxs()	{
		return workEffortTransBoxs;
	}

	public void setWorkEffortTransBoxs(List<WorkEffortTransBox> workEffortTransBoxs)	{
		this.workEffortTransBoxs = workEffortTransBoxs;
	}
}
