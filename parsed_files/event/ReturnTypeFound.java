package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ReturnTypeFound implements Event{

	private List<ReturnType> returnTypes;

	public ReturnTypeFound(List<ReturnType> returnTypes) {
		this.setReturnTypes(returnTypes);
	}

	public List<ReturnType> getReturnTypes()	{
		return returnTypes;
	}

	public void setReturnTypes(List<ReturnType> returnTypes)	{
		this.returnTypes = returnTypes;
	}
}
