package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderSummaryEntryFound implements Event{

	private List<OrderSummaryEntry> orderSummaryEntrys;

	public OrderSummaryEntryFound(List<OrderSummaryEntry> orderSummaryEntrys) {
		this.setOrderSummaryEntrys(orderSummaryEntrys);
	}

	public List<OrderSummaryEntry> getOrderSummaryEntrys()	{
		return orderSummaryEntrys;
	}

	public void setOrderSummaryEntrys(List<OrderSummaryEntry> orderSummaryEntrys)	{
		this.orderSummaryEntrys = orderSummaryEntrys;
	}
}
