package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class QuoteTermAttributeFound implements Event{

	private List<QuoteTermAttribute> quoteTermAttributes;

	public QuoteTermAttributeFound(List<QuoteTermAttribute> quoteTermAttributes) {
		this.setQuoteTermAttributes(quoteTermAttributes);
	}

	public List<QuoteTermAttribute> getQuoteTermAttributes()	{
		return quoteTermAttributes;
	}

	public void setQuoteTermAttributes(List<QuoteTermAttribute> quoteTermAttributes)	{
		this.quoteTermAttributes = quoteTermAttributes;
	}
}
