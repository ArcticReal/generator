package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderAttributeFound implements Event{

	private List<OrderAttribute> orderAttributes;

	public OrderAttributeFound(List<OrderAttribute> orderAttributes) {
		this.setOrderAttributes(orderAttributes);
	}

	public List<OrderAttribute> getOrderAttributes()	{
		return orderAttributes;
	}

	public void setOrderAttributes(List<OrderAttribute> orderAttributes)	{
		this.orderAttributes = orderAttributes;
	}
}
