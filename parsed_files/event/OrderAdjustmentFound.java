package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderAdjustmentFound implements Event{

	private List<OrderAdjustment> orderAdjustments;

	public OrderAdjustmentFound(List<OrderAdjustment> orderAdjustments) {
		this.setOrderAdjustments(orderAdjustments);
	}

	public List<OrderAdjustment> getOrderAdjustments()	{
		return orderAdjustments;
	}

	public void setOrderAdjustments(List<OrderAdjustment> orderAdjustments)	{
		this.orderAdjustments = orderAdjustments;
	}
}
