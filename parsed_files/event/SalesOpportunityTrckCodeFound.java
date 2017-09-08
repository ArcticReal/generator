package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SalesOpportunityTrckCodeFound implements Event{

	private List<SalesOpportunityTrckCode> salesOpportunityTrckCodes;

	public SalesOpportunityTrckCodeFound(List<SalesOpportunityTrckCode> salesOpportunityTrckCodes) {
		this.setSalesOpportunityTrckCodes(salesOpportunityTrckCodes);
	}

	public List<SalesOpportunityTrckCode> getSalesOpportunityTrckCodes()	{
		return salesOpportunityTrckCodes;
	}

	public void setSalesOpportunityTrckCodes(List<SalesOpportunityTrckCode> salesOpportunityTrckCodes)	{
		this.salesOpportunityTrckCodes = salesOpportunityTrckCodes;
	}
}
