package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortPurposeTypeFound implements Event{

	private List<WorkEffortPurposeType> workEffortPurposeTypes;

	public WorkEffortPurposeTypeFound(List<WorkEffortPurposeType> workEffortPurposeTypes) {
		this.setWorkEffortPurposeTypes(workEffortPurposeTypes);
	}

	public List<WorkEffortPurposeType> getWorkEffortPurposeTypes()	{
		return workEffortPurposeTypes;
	}

	public void setWorkEffortPurposeTypes(List<WorkEffortPurposeType> workEffortPurposeTypes)	{
		this.workEffortPurposeTypes = workEffortPurposeTypes;
	}
}
