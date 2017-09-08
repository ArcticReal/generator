package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderAdjustmentAttributeFound implements Event{

	private List<OrderAdjustmentAttribute> orderAdjustmentAttributes;

	public OrderAdjustmentAttributeFound(List<OrderAdjustmentAttribute> orderAdjustmentAttributes) {
		this.setOrderAdjustmentAttributes(orderAdjustmentAttributes);
	}

	public List<OrderAdjustmentAttribute> getOrderAdjustmentAttributes()	{
		return orderAdjustmentAttributes;
	}

	public void setOrderAdjustmentAttributes(List<OrderAdjustmentAttribute> orderAdjustmentAttributes)	{
		this.orderAdjustmentAttributes = orderAdjustmentAttributes;
	}
}
