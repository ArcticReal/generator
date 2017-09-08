package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderStatusFound implements Event{

	private List<OrderStatus> orderStatuss;

	public OrderStatusFound(List<OrderStatus> orderStatuss) {
		this.setOrderStatuss(orderStatuss);
	}

	public List<OrderStatus> getOrderStatuss()	{
		return orderStatuss;
	}

	public void setOrderStatuss(List<OrderStatus> orderStatuss)	{
		this.orderStatuss = orderStatuss;
	}
}
