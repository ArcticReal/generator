package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SalesOpportunityWorkEffortFound implements Event{

	private List<SalesOpportunityWorkEffort> salesOpportunityWorkEfforts;

	public SalesOpportunityWorkEffortFound(List<SalesOpportunityWorkEffort> salesOpportunityWorkEfforts) {
		this.setSalesOpportunityWorkEfforts(salesOpportunityWorkEfforts);
	}

	public List<SalesOpportunityWorkEffort> getSalesOpportunityWorkEfforts()	{
		return salesOpportunityWorkEfforts;
	}

	public void setSalesOpportunityWorkEfforts(List<SalesOpportunityWorkEffort> salesOpportunityWorkEfforts)	{
		this.salesOpportunityWorkEfforts = salesOpportunityWorkEfforts;
	}
}
