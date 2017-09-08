package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortInventoryAssignFound implements Event{

	private List<WorkEffortInventoryAssign> workEffortInventoryAssigns;

	public WorkEffortInventoryAssignFound(List<WorkEffortInventoryAssign> workEffortInventoryAssigns) {
		this.setWorkEffortInventoryAssigns(workEffortInventoryAssigns);
	}

	public List<WorkEffortInventoryAssign> getWorkEffortInventoryAssigns()	{
		return workEffortInventoryAssigns;
	}

	public void setWorkEffortInventoryAssigns(List<WorkEffortInventoryAssign> workEffortInventoryAssigns)	{
		this.workEffortInventoryAssigns = workEffortInventoryAssigns;
	}
}
