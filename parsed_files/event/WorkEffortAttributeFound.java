package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortAttributeFound implements Event{

	private List<WorkEffortAttribute> workEffortAttributes;

	public WorkEffortAttributeFound(List<WorkEffortAttribute> workEffortAttributes) {
		this.setWorkEffortAttributes(workEffortAttributes);
	}

	public List<WorkEffortAttribute> getWorkEffortAttributes()	{
		return workEffortAttributes;
	}

	public void setWorkEffortAttributes(List<WorkEffortAttribute> workEffortAttributes)	{
		this.workEffortAttributes = workEffortAttributes;
	}
}
