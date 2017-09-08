package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class QuoteTermFound implements Event{

	private List<QuoteTerm> quoteTerms;

	public QuoteTermFound(List<QuoteTerm> quoteTerms) {
		this.setQuoteTerms(quoteTerms);
	}

	public List<QuoteTerm> getQuoteTerms()	{
		return quoteTerms;
	}

	public void setQuoteTerms(List<QuoteTerm> quoteTerms)	{
		this.quoteTerms = quoteTerms;
	}
}
