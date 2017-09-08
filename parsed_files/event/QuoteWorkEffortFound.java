package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class QuoteWorkEffortFound implements Event{

	private List<QuoteWorkEffort> quoteWorkEfforts;

	public QuoteWorkEffortFound(List<QuoteWorkEffort> quoteWorkEfforts) {
		this.setQuoteWorkEfforts(quoteWorkEfforts);
	}

	public List<QuoteWorkEffort> getQuoteWorkEfforts()	{
		return quoteWorkEfforts;
	}

	public void setQuoteWorkEfforts(List<QuoteWorkEffort> quoteWorkEfforts)	{
		this.quoteWorkEfforts = quoteWorkEfforts;
	}
}
