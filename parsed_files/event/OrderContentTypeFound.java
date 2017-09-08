package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderContentTypeFound implements Event{

	private List<OrderContentType> orderContentTypes;

	public OrderContentTypeFound(List<OrderContentType> orderContentTypes) {
		this.setOrderContentTypes(orderContentTypes);
	}

	public List<OrderContentType> getOrderContentTypes()	{
		return orderContentTypes;
	}

	public void setOrderContentTypes(List<OrderContentType> orderContentTypes)	{
		this.orderContentTypes = orderContentTypes;
	}
}
