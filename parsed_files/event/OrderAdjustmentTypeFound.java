package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderAdjustmentTypeFound implements Event{

	private List<OrderAdjustmentType> orderAdjustmentTypes;

	public OrderAdjustmentTypeFound(List<OrderAdjustmentType> orderAdjustmentTypes) {
		this.setOrderAdjustmentTypes(orderAdjustmentTypes);
	}

	public List<OrderAdjustmentType> getOrderAdjustmentTypes()	{
		return orderAdjustmentTypes;
	}

	public void setOrderAdjustmentTypes(List<OrderAdjustmentType> orderAdjustmentTypes)	{
		this.orderAdjustmentTypes = orderAdjustmentTypes;
	}
}
