package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class QuoteFound implements Event{

	private List<Quote> quotes;

	public QuoteFound(List<Quote> quotes) {
		this.setQuotes(quotes);
	}

	public List<Quote> getQuotes()	{
		return quotes;
	}

	public void setQuotes(List<Quote> quotes)	{
		this.quotes = quotes;
	}
}
