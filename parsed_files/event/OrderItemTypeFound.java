package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemTypeFound implements Event{

	private List<OrderItemType> orderItemTypes;

	public OrderItemTypeFound(List<OrderItemType> orderItemTypes) {
		this.setOrderItemTypes(orderItemTypes);
	}

	public List<OrderItemType> getOrderItemTypes()	{
		return orderItemTypes;
	}

	public void setOrderItemTypes(List<OrderItemType> orderItemTypes)	{
		this.orderItemTypes = orderItemTypes;
	}
}
