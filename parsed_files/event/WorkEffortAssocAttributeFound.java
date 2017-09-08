package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortAssocAttributeFound implements Event{

	private List<WorkEffortAssocAttribute> workEffortAssocAttributes;

	public WorkEffortAssocAttributeFound(List<WorkEffortAssocAttribute> workEffortAssocAttributes) {
		this.setWorkEffortAssocAttributes(workEffortAssocAttributes);
	}

	public List<WorkEffortAssocAttribute> getWorkEffortAssocAttributes()	{
		return workEffortAssocAttributes;
	}

	public void setWorkEffortAssocAttributes(List<WorkEffortAssocAttribute> workEffortAssocAttributes)	{
		this.workEffortAssocAttributes = workEffortAssocAttributes;
	}
}
