package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class QuoteTypeFound implements Event{

	private List<QuoteType> quoteTypes;

	public QuoteTypeFound(List<QuoteType> quoteTypes) {
		this.setQuoteTypes(quoteTypes);
	}

	public List<QuoteType> getQuoteTypes()	{
		return quoteTypes;
	}

	public void setQuoteTypes(List<QuoteType> quoteTypes)	{
		this.quoteTypes = quoteTypes;
	}
}
