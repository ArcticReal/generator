package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class QuoteItemFound implements Event{

	private List<QuoteItem> quoteItems;

	public QuoteItemFound(List<QuoteItem> quoteItems) {
		this.setQuoteItems(quoteItems);
	}

	public List<QuoteItem> getQuoteItems()	{
		return quoteItems;
	}

	public void setQuoteItems(List<QuoteItem> quoteItems)	{
		this.quoteItems = quoteItems;
	}
}
