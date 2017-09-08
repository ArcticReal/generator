package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderContentFound implements Event{

	private List<OrderContent> orderContents;

	public OrderContentFound(List<OrderContent> orderContents) {
		this.setOrderContents(orderContents);
	}

	public List<OrderContent> getOrderContents()	{
		return orderContents;
	}

	public void setOrderContents(List<OrderContent> orderContents)	{
		this.orderContents = orderContents;
	}
}
