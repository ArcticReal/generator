package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ReturnHeaderTypeFound implements Event{

	private List<ReturnHeaderType> returnHeaderTypes;

	public ReturnHeaderTypeFound(List<ReturnHeaderType> returnHeaderTypes) {
		this.setReturnHeaderTypes(returnHeaderTypes);
	}

	public List<ReturnHeaderType> getReturnHeaderTypes()	{
		return returnHeaderTypes;
	}

	public void setReturnHeaderTypes(List<ReturnHeaderType> returnHeaderTypes)	{
		this.returnHeaderTypes = returnHeaderTypes;
	}
}
