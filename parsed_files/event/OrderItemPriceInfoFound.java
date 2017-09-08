package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderItemPriceInfoFound implements Event{

	private List<OrderItemPriceInfo> orderItemPriceInfos;

	public OrderItemPriceInfoFound(List<OrderItemPriceInfo> orderItemPriceInfos) {
		this.setOrderItemPriceInfos(orderItemPriceInfos);
	}

	public List<OrderItemPriceInfo> getOrderItemPriceInfos()	{
		return orderItemPriceInfos;
	}

	public void setOrderItemPriceInfos(List<OrderItemPriceInfo> orderItemPriceInfos)	{
		this.orderItemPriceInfos = orderItemPriceInfos;
	}
}
