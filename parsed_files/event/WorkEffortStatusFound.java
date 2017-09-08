package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortStatusFound implements Event{

	private List<WorkEffortStatus> workEffortStatuss;

	public WorkEffortStatusFound(List<WorkEffortStatus> workEffortStatuss) {
		this.setWorkEffortStatuss(workEffortStatuss);
	}

	public List<WorkEffortStatus> getWorkEffortStatuss()	{
		return workEffortStatuss;
	}

	public void setWorkEffortStatuss(List<WorkEffortStatus> workEffortStatuss)	{
		this.workEffortStatuss = workEffortStatuss;
	}
}
