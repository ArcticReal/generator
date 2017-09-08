package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortAssocTypeFound implements Event{

	private List<WorkEffortAssocType> workEffortAssocTypes;

	public WorkEffortAssocTypeFound(List<WorkEffortAssocType> workEffortAssocTypes) {
		this.setWorkEffortAssocTypes(workEffortAssocTypes);
	}

	public List<WorkEffortAssocType> getWorkEffortAssocTypes()	{
		return workEffortAssocTypes;
	}

	public void setWorkEffortAssocTypes(List<WorkEffortAssocType> workEffortAssocTypes)	{
		this.workEffortAssocTypes = workEffortAssocTypes;
	}
}
