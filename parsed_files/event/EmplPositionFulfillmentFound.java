package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class EmplPositionFulfillmentFound implements Event{

	private List<EmplPositionFulfillment> emplPositionFulfillments;

	public EmplPositionFulfillmentFound(List<EmplPositionFulfillment> emplPositionFulfillments) {
		this.setEmplPositionFulfillments(emplPositionFulfillments);
	}

	public List<EmplPositionFulfillment> getEmplPositionFulfillments()	{
		return emplPositionFulfillments;
	}

	public void setEmplPositionFulfillments(List<EmplPositionFulfillment> emplPositionFulfillments)	{
		this.emplPositionFulfillments = emplPositionFulfillments;
	}
}
