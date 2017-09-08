package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderAdjustmentBillingFound implements Event{

	private List<OrderAdjustmentBilling> orderAdjustmentBillings;

	public OrderAdjustmentBillingFound(List<OrderAdjustmentBilling> orderAdjustmentBillings) {
		this.setOrderAdjustmentBillings(orderAdjustmentBillings);
	}

	public List<OrderAdjustmentBilling> getOrderAdjustmentBillings()	{
		return orderAdjustmentBillings;
	}

	public void setOrderAdjustmentBillings(List<OrderAdjustmentBilling> orderAdjustmentBillings)	{
		this.orderAdjustmentBillings = orderAdjustmentBillings;
	}
}
