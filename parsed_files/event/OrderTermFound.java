package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderTermFound implements Event{

	private List<OrderTerm> orderTerms;

	public OrderTermFound(List<OrderTerm> orderTerms) {
		this.setOrderTerms(orderTerms);
	}

	public List<OrderTerm> getOrderTerms()	{
		return orderTerms;
	}

	public void setOrderTerms(List<OrderTerm> orderTerms)	{
		this.orderTerms = orderTerms;
	}
}
