package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemChangeFound implements Event{

	private List<OrderItemChange> orderItemChanges;

	public OrderItemChangeFound(List<OrderItemChange> orderItemChanges) {
		this.setOrderItemChanges(orderItemChanges);
	}

	public List<OrderItemChange> getOrderItemChanges()	{
		return orderItemChanges;
	}

	public void setOrderItemChanges(List<OrderItemChange> orderItemChanges)	{
		this.orderItemChanges = orderItemChanges;
	}
}
