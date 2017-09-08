package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortAssocFound implements Event{

	private List<WorkEffortAssoc> workEffortAssocs;

	public WorkEffortAssocFound(List<WorkEffortAssoc> workEffortAssocs) {
		this.setWorkEffortAssocs(workEffortAssocs);
	}

	public List<WorkEffortAssoc> getWorkEffortAssocs()	{
		return workEffortAssocs;
	}

	public void setWorkEffortAssocs(List<WorkEffortAssoc> workEffortAssocs)	{
		this.workEffortAssocs = workEffortAssocs;
	}
}
