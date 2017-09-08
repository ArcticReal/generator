package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderTypeFound implements Event{

	private List<OrderType> orderTypes;

	public OrderTypeFound(List<OrderType> orderTypes) {
		this.setOrderTypes(orderTypes);
	}

	public List<OrderType> getOrderTypes()	{
		return orderTypes;
	}

	public void setOrderTypes(List<OrderType> orderTypes)	{
		this.orderTypes = orderTypes;
	}
}
