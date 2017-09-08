package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderHeaderWorkEffortFound implements Event{

	private List<OrderHeaderWorkEffort> orderHeaderWorkEfforts;

	public OrderHeaderWorkEffortFound(List<OrderHeaderWorkEffort> orderHeaderWorkEfforts) {
		this.setOrderHeaderWorkEfforts(orderHeaderWorkEfforts);
	}

	public List<OrderHeaderWorkEffort> getOrderHeaderWorkEfforts()	{
		return orderHeaderWorkEfforts;
	}

	public void setOrderHeaderWorkEfforts(List<OrderHeaderWorkEffort> orderHeaderWorkEfforts)	{
		this.orderHeaderWorkEfforts = orderHeaderWorkEfforts;
	}
}
