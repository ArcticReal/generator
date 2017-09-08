package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SalesOpportunityHistoryFound implements Event{

	private List<SalesOpportunityHistory> salesOpportunityHistorys;

	public SalesOpportunityHistoryFound(List<SalesOpportunityHistory> salesOpportunityHistorys) {
		this.setSalesOpportunityHistorys(salesOpportunityHistorys);
	}

	public List<SalesOpportunityHistory> getSalesOpportunityHistorys()	{
		return salesOpportunityHistorys;
	}

	public void setSalesOpportunityHistorys(List<SalesOpportunityHistory> salesOpportunityHistorys)	{
		this.salesOpportunityHistorys = salesOpportunityHistorys;
	}
}
