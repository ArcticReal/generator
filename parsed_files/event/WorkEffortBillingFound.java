package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkEffortBillingFound implements Event{

	private List<WorkEffortBilling> workEffortBillings;

	public WorkEffortBillingFound(List<WorkEffortBilling> workEffortBillings) {
		this.setWorkEffortBillings(workEffortBillings);
	}

	public List<WorkEffortBilling> getWorkEffortBillings()	{
		return workEffortBillings;
	}

	public void setWorkEffortBillings(List<WorkEffortBilling> workEffortBillings)	{
		this.workEffortBillings = workEffortBillings;
	}
}
