package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortTypeAttrFound implements Event{

	private List<WorkEffortTypeAttr> workEffortTypeAttrs;

	public WorkEffortTypeAttrFound(List<WorkEffortTypeAttr> workEffortTypeAttrs) {
		this.setWorkEffortTypeAttrs(workEffortTypeAttrs);
	}

	public List<WorkEffortTypeAttr> getWorkEffortTypeAttrs()	{
		return workEffortTypeAttrs;
	}

	public void setWorkEffortTypeAttrs(List<WorkEffortTypeAttr> workEffortTypeAttrs)	{
		this.workEffortTypeAttrs = workEffortTypeAttrs;
	}
}
