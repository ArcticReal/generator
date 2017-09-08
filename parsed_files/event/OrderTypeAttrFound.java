package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderTypeAttrFound implements Event{

	private List<OrderTypeAttr> orderTypeAttrs;

	public OrderTypeAttrFound(List<OrderTypeAttr> orderTypeAttrs) {
		this.setOrderTypeAttrs(orderTypeAttrs);
	}

	public List<OrderTypeAttr> getOrderTypeAttrs()	{
		return orderTypeAttrs;
	}

	public void setOrderTypeAttrs(List<OrderTypeAttr> orderTypeAttrs)	{
		this.orderTypeAttrs = orderTypeAttrs;
	}
}
