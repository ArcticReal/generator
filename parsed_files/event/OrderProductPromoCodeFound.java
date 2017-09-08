package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderProductPromoCodeFound implements Event{

	private List<OrderProductPromoCode> orderProductPromoCodes;

	public OrderProductPromoCodeFound(List<OrderProductPromoCode> orderProductPromoCodes) {
		this.setOrderProductPromoCodes(orderProductPromoCodes);
	}

	public List<OrderProductPromoCode> getOrderProductPromoCodes()	{
		return orderProductPromoCodes;
	}

	public void setOrderProductPromoCodes(List<OrderProductPromoCode> orderProductPromoCodes)	{
		this.orderProductPromoCodes = orderProductPromoCodes;
	}
}
