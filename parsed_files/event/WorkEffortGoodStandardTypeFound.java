package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortGoodStandardTypeFound implements Event{

	private List<WorkEffortGoodStandardType> workEffortGoodStandardTypes;

	public WorkEffortGoodStandardTypeFound(List<WorkEffortGoodStandardType> workEffortGoodStandardTypes) {
		this.setWorkEffortGoodStandardTypes(workEffortGoodStandardTypes);
	}

	public List<WorkEffortGoodStandardType> getWorkEffortGoodStandardTypes()	{
		return workEffortGoodStandardTypes;
	}

	public void setWorkEffortGoodStandardTypes(List<WorkEffortGoodStandardType> workEffortGoodStandardTypes)	{
		this.workEffortGoodStandardTypes = workEffortGoodStandardTypes;
	}
}
