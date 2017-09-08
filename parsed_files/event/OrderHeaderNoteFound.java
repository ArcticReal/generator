package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class OrderHeaderNoteFound implements Event{

	private List<OrderHeaderNote> orderHeaderNotes;

	public OrderHeaderNoteFound(List<OrderHeaderNote> orderHeaderNotes) {
		this.setOrderHeaderNotes(orderHeaderNotes);
	}

	public List<OrderHeaderNote> getOrderHeaderNotes()	{
		return orderHeaderNotes;
	}

	public void setOrderHeaderNotes(List<OrderHeaderNote> orderHeaderNotes)	{
		this.orderHeaderNotes = orderHeaderNotes;
	}
}
