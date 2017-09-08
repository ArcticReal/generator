package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SalesOpportunityCompetitorFound implements Event{

	private List<SalesOpportunityCompetitor> salesOpportunityCompetitors;

	public SalesOpportunityCompetitorFound(List<SalesOpportunityCompetitor> salesOpportunityCompetitors) {
		this.setSalesOpportunityCompetitors(salesOpportunityCompetitors);
	}

	public List<SalesOpportunityCompetitor> getSalesOpportunityCompetitors()	{
		return salesOpportunityCompetitors;
	}

	public void setSalesOpportunityCompetitors(List<SalesOpportunityCompetitor> salesOpportunityCompetitors)	{
		this.salesOpportunityCompetitors = salesOpportunityCompetitors;
	}
}
