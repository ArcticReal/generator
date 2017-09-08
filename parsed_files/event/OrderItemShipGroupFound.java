package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemShipGroupFound implements Event{

	private List<OrderItemShipGroup> orderItemShipGroups;

	public OrderItemShipGroupFound(List<OrderItemShipGroup> orderItemShipGroups) {
		this.setOrderItemShipGroups(orderItemShipGroups);
	}

	public List<OrderItemShipGroup> getOrderItemShipGroups()	{
		return orderItemShipGroups;
	}

	public void setOrderItemShipGroups(List<OrderItemShipGroup> orderItemShipGroups)	{
		this.orderItemShipGroups = orderItemShipGroups;
	}
}
