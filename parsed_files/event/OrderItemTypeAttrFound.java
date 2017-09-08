package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemTypeAttrFound implements Event{

	private List<OrderItemTypeAttr> orderItemTypeAttrs;

	public OrderItemTypeAttrFound(List<OrderItemTypeAttr> orderItemTypeAttrs) {
		this.setOrderItemTypeAttrs(orderItemTypeAttrs);
	}

	public List<OrderItemTypeAttr> getOrderItemTypeAttrs()	{
		return orderItemTypeAttrs;
	}

	public void setOrderItemTypeAttrs(List<OrderItemTypeAttr> orderItemTypeAttrs)	{
		this.orderItemTypeAttrs = orderItemTypeAttrs;
	}
}
