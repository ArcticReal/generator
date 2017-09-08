package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortInventoryProducedFound implements Event{

	private List<WorkEffortInventoryProduced> workEffortInventoryProduceds;

	public WorkEffortInventoryProducedFound(List<WorkEffortInventoryProduced> workEffortInventoryProduceds) {
		this.setWorkEffortInventoryProduceds(workEffortInventoryProduceds);
	}

	public List<WorkEffortInventoryProduced> getWorkEffortInventoryProduceds()	{
		return workEffortInventoryProduceds;
	}

	public void setWorkEffortInventoryProduceds(List<WorkEffortInventoryProduced> workEffortInventoryProduceds)	{
		this.workEffortInventoryProduceds = workEffortInventoryProduceds;
	}
}
