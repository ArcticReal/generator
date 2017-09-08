package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortGoodStandardFound implements Event{

	private List<WorkEffortGoodStandard> workEffortGoodStandards;

	public WorkEffortGoodStandardFound(List<WorkEffortGoodStandard> workEffortGoodStandards) {
		this.setWorkEffortGoodStandards(workEffortGoodStandards);
	}

	public List<WorkEffortGoodStandard> getWorkEffortGoodStandards()	{
		return workEffortGoodStandards;
	}

	public void setWorkEffortGoodStandards(List<WorkEffortGoodStandard> workEffortGoodStandards)	{
		this.workEffortGoodStandards = workEffortGoodStandards;
	}
}
