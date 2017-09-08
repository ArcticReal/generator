package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortSearchConstraintFound implements Event{

	private List<WorkEffortSearchConstraint> workEffortSearchConstraints;

	public WorkEffortSearchConstraintFound(List<WorkEffortSearchConstraint> workEffortSearchConstraints) {
		this.setWorkEffortSearchConstraints(workEffortSearchConstraints);
	}

	public List<WorkEffortSearchConstraint> getWorkEffortSearchConstraints()	{
		return workEffortSearchConstraints;
	}

	public void setWorkEffortSearchConstraints(List<WorkEffortSearchConstraint> workEffortSearchConstraints)	{
		this.workEffortSearchConstraints = workEffortSearchConstraints;
	}
}
