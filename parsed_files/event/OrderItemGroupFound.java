package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemGroupFound implements Event{

	private List<OrderItemGroup> orderItemGroups;

	public OrderItemGroupFound(List<OrderItemGroup> orderItemGroups) {
		this.setOrderItemGroups(orderItemGroups);
	}

	public List<OrderItemGroup> getOrderItemGroups()	{
		return orderItemGroups;
	}

	public void setOrderItemGroups(List<OrderItemGroup> orderItemGroups)	{
		this.orderItemGroups = orderItemGroups;
	}
}
