package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class QuoteTypeAttrFound implements Event{

	private List<QuoteTypeAttr> quoteTypeAttrs;

	public QuoteTypeAttrFound(List<QuoteTypeAttr> quoteTypeAttrs) {
		this.setQuoteTypeAttrs(quoteTypeAttrs);
	}

	public List<QuoteTypeAttr> getQuoteTypeAttrs()	{
		return quoteTypeAttrs;
	}

	public void setQuoteTypeAttrs(List<QuoteTypeAttr> quoteTypeAttrs)	{
		this.quoteTypeAttrs = quoteTypeAttrs;
	}
}
