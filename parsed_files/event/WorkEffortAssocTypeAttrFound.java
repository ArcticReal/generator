package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortAssocTypeAttrFound implements Event{

	private List<WorkEffortAssocTypeAttr> workEffortAssocTypeAttrs;

	public WorkEffortAssocTypeAttrFound(List<WorkEffortAssocTypeAttr> workEffortAssocTypeAttrs) {
		this.setWorkEffortAssocTypeAttrs(workEffortAssocTypeAttrs);
	}

	public List<WorkEffortAssocTypeAttr> getWorkEffortAssocTypeAttrs()	{
		return workEffortAssocTypeAttrs;
	}

	public void setWorkEffortAssocTypeAttrs(List<WorkEffortAssocTypeAttr> workEffortAssocTypeAttrs)	{
		this.workEffortAssocTypeAttrs = workEffortAssocTypeAttrs;
	}
}
