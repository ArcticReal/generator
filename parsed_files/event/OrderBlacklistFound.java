package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderBlacklistFound implements Event{

	private List<OrderBlacklist> orderBlacklists;

	public OrderBlacklistFound(List<OrderBlacklist> orderBlacklists) {
		this.setOrderBlacklists(orderBlacklists);
	}

	public List<OrderBlacklist> getOrderBlacklists()	{
		return orderBlacklists;
	}

	public void setOrderBlacklists(List<OrderBlacklist> orderBlacklists)	{
		this.orderBlacklists = orderBlacklists;
	}
}
