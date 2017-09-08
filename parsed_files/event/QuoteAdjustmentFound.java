package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class QuoteAdjustmentFound implements Event{

	private List<QuoteAdjustment> quoteAdjustments;

	public QuoteAdjustmentFound(List<QuoteAdjustment> quoteAdjustments) {
		this.setQuoteAdjustments(quoteAdjustments);
	}

	public List<QuoteAdjustment> getQuoteAdjustments()	{
		return quoteAdjustments;
	}

	public void setQuoteAdjustments(List<QuoteAdjustment> quoteAdjustments)	{
		this.quoteAdjustments = quoteAdjustments;
	}
}
