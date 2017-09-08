package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PicklistFound implements Event{

	private List<Picklist> picklists;

	public PicklistFound(List<Picklist> picklists) {
		this.setPicklists(picklists);
	}

	public List<Picklist> getPicklists()	{
		return picklists;
	}

	public void setPicklists(List<Picklist> picklists)	{
		this.picklists = picklists;
	}
}
