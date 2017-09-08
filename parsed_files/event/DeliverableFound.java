package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class DeliverableFound implements Event{

	private List<Deliverable> deliverables;

	public DeliverableFound(List<Deliverable> deliverables) {
		this.setDeliverables(deliverables);
	}

	public List<Deliverable> getDeliverables()	{
		return deliverables;
	}

	public void setDeliverables(List<Deliverable> deliverables)	{
		this.deliverables = deliverables;
	}
}
