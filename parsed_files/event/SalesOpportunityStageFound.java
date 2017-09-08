package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SalesOpportunityStageFound implements Event{

	private List<SalesOpportunityStage> salesOpportunityStages;

	public SalesOpportunityStageFound(List<SalesOpportunityStage> salesOpportunityStages) {
		this.setSalesOpportunityStages(salesOpportunityStages);
	}

	public List<SalesOpportunityStage> getSalesOpportunityStages()	{
		return salesOpportunityStages;
	}

	public void setSalesOpportunityStages(List<SalesOpportunityStage> salesOpportunityStages)	{
		this.salesOpportunityStages = salesOpportunityStages;
	}
}
