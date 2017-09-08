package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemFound implements Event{

	private List<OrderItem> orderItems;

	public OrderItemFound(List<OrderItem> orderItems) {
		this.setOrderItems(orderItems);
	}

	public List<OrderItem> getOrderItems()	{
		return orderItems;
	}

	public void setOrderItems(List<OrderItem> orderItems)	{
		this.orderItems = orderItems;
	}
}
