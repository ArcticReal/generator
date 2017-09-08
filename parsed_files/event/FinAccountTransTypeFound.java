package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FinAccountTransTypeFound implements Event{

	private List<FinAccountTransType> finAccountTransTypes;

	public FinAccountTransTypeFound(List<FinAccountTransType> finAccountTransTypes) {
		this.setFinAccountTransTypes(finAccountTransTypes);
	}

	public List<FinAccountTransType> getFinAccountTransTypes()	{
		return finAccountTransTypes;
	}

	public void setFinAccountTransTypes(List<FinAccountTransType> finAccountTransTypes)	{
		this.finAccountTransTypes = finAccountTransTypes;
	}
}
