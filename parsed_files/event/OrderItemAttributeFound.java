package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemAttributeFound implements Event{

	private List<OrderItemAttribute> orderItemAttributes;

	public OrderItemAttributeFound(List<OrderItemAttribute> orderItemAttributes) {
		this.setOrderItemAttributes(orderItemAttributes);
	}

	public List<OrderItemAttribute> getOrderItemAttributes()	{
		return orderItemAttributes;
	}

	public void setOrderItemAttributes(List<OrderItemAttribute> orderItemAttributes)	{
		this.orderItemAttributes = orderItemAttributes;
	}
}
