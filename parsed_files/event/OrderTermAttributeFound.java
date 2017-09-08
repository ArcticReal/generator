package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderTermAttributeFound implements Event{

	private List<OrderTermAttribute> orderTermAttributes;

	public OrderTermAttributeFound(List<OrderTermAttribute> orderTermAttributes) {
		this.setOrderTermAttributes(orderTermAttributes);
	}

	public List<OrderTermAttribute> getOrderTermAttributes()	{
		return orderTermAttributes;
	}

	public void setOrderTermAttributes(List<OrderTermAttribute> orderTermAttributes)	{
		this.orderTermAttributes = orderTermAttributes;
	}
}
