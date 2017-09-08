package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class QuoteAttributeFound implements Event{

	private List<QuoteAttribute> quoteAttributes;

	public QuoteAttributeFound(List<QuoteAttribute> quoteAttributes) {
		this.setQuoteAttributes(quoteAttributes);
	}

	public List<QuoteAttribute> getQuoteAttributes()	{
		return quoteAttributes;
	}

	public void setQuoteAttributes(List<QuoteAttribute> quoteAttributes)	{
		this.quoteAttributes = quoteAttributes;
	}
}
