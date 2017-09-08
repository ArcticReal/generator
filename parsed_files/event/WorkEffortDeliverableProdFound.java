package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortDeliverableProdFound implements Event{

	private List<WorkEffortDeliverableProd> workEffortDeliverableProds;

	public WorkEffortDeliverableProdFound(List<WorkEffortDeliverableProd> workEffortDeliverableProds) {
		this.setWorkEffortDeliverableProds(workEffortDeliverableProds);
	}

	public List<WorkEffortDeliverableProd> getWorkEffortDeliverableProds()	{
		return workEffortDeliverableProds;
	}

	public void setWorkEffortDeliverableProds(List<WorkEffortDeliverableProd> workEffortDeliverableProds)	{
		this.workEffortDeliverableProds = workEffortDeliverableProds;
	}
}
