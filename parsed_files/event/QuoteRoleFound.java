package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class QuoteRoleFound implements Event{

	private List<QuoteRole> quoteRoles;

	public QuoteRoleFound(List<QuoteRole> quoteRoles) {
		this.setQuoteRoles(quoteRoles);
	}

	public List<QuoteRole> getQuoteRoles()	{
		return quoteRoles;
	}

	public void setQuoteRoles(List<QuoteRole> quoteRoles)	{
		this.quoteRoles = quoteRoles;
	}
}
