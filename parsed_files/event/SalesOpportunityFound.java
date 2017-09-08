package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SalesOpportunityFound implements Event{

	private List<SalesOpportunity> salesOpportunitys;

	public SalesOpportunityFound(List<SalesOpportunity> salesOpportunitys) {
		this.setSalesOpportunitys(salesOpportunitys);
	}

	public List<SalesOpportunity> getSalesOpportunitys()	{
		return salesOpportunitys;
	}

	public void setSalesOpportunitys(List<SalesOpportunity> salesOpportunitys)	{
		this.salesOpportunitys = salesOpportunitys;
	}
}
