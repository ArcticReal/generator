package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortContactMechFound implements Event{

	private List<WorkEffortContactMech> workEffortContactMechs;

	public WorkEffortContactMechFound(List<WorkEffortContactMech> workEffortContactMechs) {
		this.setWorkEffortContactMechs(workEffortContactMechs);
	}

	public List<WorkEffortContactMech> getWorkEffortContactMechs()	{
		return workEffortContactMechs;
	}

	public void setWorkEffortContactMechs(List<WorkEffortContactMech> workEffortContactMechs)	{
		this.workEffortContactMechs = workEffortContactMechs;
	}
}
