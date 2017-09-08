package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SalesOpportunityQuoteFound implements Event{

	private List<SalesOpportunityQuote> salesOpportunityQuotes;

	public SalesOpportunityQuoteFound(List<SalesOpportunityQuote> salesOpportunityQuotes) {
		this.setSalesOpportunityQuotes(salesOpportunityQuotes);
	}

	public List<SalesOpportunityQuote> getSalesOpportunityQuotes()	{
		return salesOpportunityQuotes;
	}

	public void setSalesOpportunityQuotes(List<SalesOpportunityQuote> salesOpportunityQuotes)	{
		this.salesOpportunityQuotes = salesOpportunityQuotes;
	}
}
