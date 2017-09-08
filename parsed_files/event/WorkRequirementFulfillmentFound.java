package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class WorkRequirementFulfillmentFound implements Event{

	private List<WorkRequirementFulfillment> workRequirementFulfillments;

	public WorkRequirementFulfillmentFound(List<WorkRequirementFulfillment> workRequirementFulfillments) {
		this.setWorkRequirementFulfillments(workRequirementFulfillments);
	}

	public List<WorkRequirementFulfillment> getWorkRequirementFulfillments()	{
		return workRequirementFulfillments;
	}

	public void setWorkRequirementFulfillments(List<WorkRequirementFulfillment> workRequirementFulfillments)	{
		this.workRequirementFulfillments = workRequirementFulfillments;
	}
}
