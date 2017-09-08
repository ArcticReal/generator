package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DeliverableTypeFound implements Event{

	private List<DeliverableType> deliverableTypes;

	public DeliverableTypeFound(List<DeliverableType> deliverableTypes) {
		this.setDeliverableTypes(deliverableTypes);
	}

	public List<DeliverableType> getDeliverableTypes()	{
		return deliverableTypes;
	}

	public void setDeliverableTypes(List<DeliverableType> deliverableTypes)	{
		this.deliverableTypes = deliverableTypes;
	}
}
