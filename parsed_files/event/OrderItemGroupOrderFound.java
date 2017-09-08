package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemGroupOrderFound implements Event{

	private List<OrderItemGroupOrder> orderItemGroupOrders;

	public OrderItemGroupOrderFound(List<OrderItemGroupOrder> orderItemGroupOrders) {
		this.setOrderItemGroupOrders(orderItemGroupOrders);
	}

	public List<OrderItemGroupOrder> getOrderItemGroupOrders()	{
		return orderItemGroupOrders;
	}

	public void setOrderItemGroupOrders(List<OrderItemGroupOrder> orderItemGroupOrders)	{
		this.orderItemGroupOrders = orderItemGroupOrders;
	}
}
