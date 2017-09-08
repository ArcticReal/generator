package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemAssocTypeFound implements Event{

	private List<OrderItemAssocType> orderItemAssocTypes;

	public OrderItemAssocTypeFound(List<OrderItemAssocType> orderItemAssocTypes) {
		this.setOrderItemAssocTypes(orderItemAssocTypes);
	}

	public List<OrderItemAssocType> getOrderItemAssocTypes()	{
		return orderItemAssocTypes;
	}

	public void setOrderItemAssocTypes(List<OrderItemAssocType> orderItemAssocTypes)	{
		this.orderItemAssocTypes = orderItemAssocTypes;
	}
}
