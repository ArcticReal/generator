package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AddendumFound implements Event{

	private List<Addendum> addendums;

	public AddendumFound(List<Addendum> addendums) {
		this.setAddendums(addendums);
	}

	public List<Addendum> getAddendums()	{
		return addendums;
	}

	public void setAddendums(List<Addendum> addendums)	{
		this.addendums = addendums;
	}
}
