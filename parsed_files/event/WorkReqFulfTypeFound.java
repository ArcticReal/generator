package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkReqFulfTypeFound implements Event{

	private List<WorkReqFulfType> workReqFulfTypes;

	public WorkReqFulfTypeFound(List<WorkReqFulfType> workReqFulfTypes) {
		this.setWorkReqFulfTypes(workReqFulfTypes);
	}

	public List<WorkReqFulfType> getWorkReqFulfTypes()	{
		return workReqFulfTypes;
	}

	public void setWorkReqFulfTypes(List<WorkReqFulfType> workReqFulfTypes)	{
		this.workReqFulfTypes = workReqFulfTypes;
	}
}
