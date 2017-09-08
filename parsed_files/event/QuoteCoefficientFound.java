package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class QuoteCoefficientFound implements Event{

	private List<QuoteCoefficient> quoteCoefficients;

	public QuoteCoefficientFound(List<QuoteCoefficient> quoteCoefficients) {
		this.setQuoteCoefficients(quoteCoefficients);
	}

	public List<QuoteCoefficient> getQuoteCoefficients()	{
		return quoteCoefficients;
	}

	public void setQuoteCoefficients(List<QuoteCoefficient> quoteCoefficients)	{
		this.quoteCoefficients = quoteCoefficients;
	}
}
