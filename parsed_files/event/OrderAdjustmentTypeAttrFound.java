package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderAdjustmentTypeAttrFound implements Event{

	private List<OrderAdjustmentTypeAttr> orderAdjustmentTypeAttrs;

	public OrderAdjustmentTypeAttrFound(List<OrderAdjustmentTypeAttr> orderAdjustmentTypeAttrs) {
		this.setOrderAdjustmentTypeAttrs(orderAdjustmentTypeAttrs);
	}

	public List<OrderAdjustmentTypeAttr> getOrderAdjustmentTypeAttrs()	{
		return orderAdjustmentTypeAttrs;
	}

	public void setOrderAdjustmentTypeAttrs(List<OrderAdjustmentTypeAttr> orderAdjustmentTypeAttrs)	{
		this.orderAdjustmentTypeAttrs = orderAdjustmentTypeAttrs;
	}
}
