package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderBlacklistTypeFound implements Event{

	private List<OrderBlacklistType> orderBlacklistTypes;

	public OrderBlacklistTypeFound(List<OrderBlacklistType> orderBlacklistTypes) {
		this.setOrderBlacklistTypes(orderBlacklistTypes);
	}

	public List<OrderBlacklistType> getOrderBlacklistTypes()	{
		return orderBlacklistTypes;
	}

	public void setOrderBlacklistTypes(List<OrderBlacklistType> orderBlacklistTypes)	{
		this.orderBlacklistTypes = orderBlacklistTypes;
	}
}
