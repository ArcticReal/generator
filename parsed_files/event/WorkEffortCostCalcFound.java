package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortCostCalcFound implements Event{

	private List<WorkEffortCostCalc> workEffortCostCalcs;

	public WorkEffortCostCalcFound(List<WorkEffortCostCalc> workEffortCostCalcs) {
		this.setWorkEffortCostCalcs(workEffortCostCalcs);
	}

	public List<WorkEffortCostCalc> getWorkEffortCostCalcs()	{
		return workEffortCostCalcs;
	}

	public void setWorkEffortCostCalcs(List<WorkEffortCostCalc> workEffortCostCalcs)	{
		this.workEffortCostCalcs = workEffortCostCalcs;
	}
}
