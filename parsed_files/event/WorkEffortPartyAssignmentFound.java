package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortPartyAssignmentFound implements Event{

	private List<WorkEffortPartyAssignment> workEffortPartyAssignments;

	public WorkEffortPartyAssignmentFound(List<WorkEffortPartyAssignment> workEffortPartyAssignments) {
		this.setWorkEffortPartyAssignments(workEffortPartyAssignments);
	}

	public List<WorkEffortPartyAssignment> getWorkEffortPartyAssignments()	{
		return workEffortPartyAssignments;
	}

	public void setWorkEffortPartyAssignments(List<WorkEffortPartyAssignment> workEffortPartyAssignments)	{
		this.workEffortPartyAssignments = workEffortPartyAssignments;
	}
}
