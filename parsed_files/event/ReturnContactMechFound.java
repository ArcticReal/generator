package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ReturnContactMechFound implements Event{

	private List<ReturnContactMech> returnContactMechs;

	public ReturnContactMechFound(List<ReturnContactMech> returnContactMechs) {
		this.setReturnContactMechs(returnContactMechs);
	}

	public List<ReturnContactMech> getReturnContactMechs()	{
		return returnContactMechs;
	}

	public void setReturnContactMechs(List<ReturnContactMech> returnContactMechs)	{
		this.returnContactMechs = returnContactMechs;
	}
}
