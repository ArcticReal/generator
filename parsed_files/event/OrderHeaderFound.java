package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderHeaderFound implements Event{

	private List<OrderHeader> orderHeaders;

	public OrderHeaderFound(List<OrderHeader> orderHeaders) {
		this.setOrderHeaders(orderHeaders);
	}

	public List<OrderHeader> getOrderHeaders()	{
		return orderHeaders;
	}

	public void setOrderHeaders(List<OrderHeader> orderHeaders)	{
		this.orderHeaders = orderHeaders;
	}
}
