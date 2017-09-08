package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortTypeFound implements Event{

	private List<WorkEffortType> workEffortTypes;

	public WorkEffortTypeFound(List<WorkEffortType> workEffortTypes) {
		this.setWorkEffortTypes(workEffortTypes);
	}

	public List<WorkEffortType> getWorkEffortTypes()	{
		return workEffortTypes;
	}

	public void setWorkEffortTypes(List<WorkEffortType> workEffortTypes)	{
		this.workEffortTypes = workEffortTypes;
	}
}
