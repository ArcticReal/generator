package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderDeliveryScheduleFound implements Event{

	private List<OrderDeliverySchedule> orderDeliverySchedules;

	public OrderDeliveryScheduleFound(List<OrderDeliverySchedule> orderDeliverySchedules) {
		this.setOrderDeliverySchedules(orderDeliverySchedules);
	}

	public List<OrderDeliverySchedule> getOrderDeliverySchedules()	{
		return orderDeliverySchedules;
	}

	public void setOrderDeliverySchedules(List<OrderDeliverySchedule> orderDeliverySchedules)	{
		this.orderDeliverySchedules = orderDeliverySchedules;
	}
}
