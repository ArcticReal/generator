package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortContentTypeFound implements Event{

	private List<WorkEffortContentType> workEffortContentTypes;

	public WorkEffortContentTypeFound(List<WorkEffortContentType> workEffortContentTypes) {
		this.setWorkEffortContentTypes(workEffortContentTypes);
	}

	public List<WorkEffortContentType> getWorkEffortContentTypes()	{
		return workEffortContentTypes;
	}

	public void setWorkEffortContentTypes(List<WorkEffortContentType> workEffortContentTypes)	{
		this.workEffortContentTypes = workEffortContentTypes;
	}
}
