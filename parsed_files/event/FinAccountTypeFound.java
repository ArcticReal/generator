package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FinAccountTypeFound implements Event{

	private List<FinAccountType> finAccountTypes;

	public FinAccountTypeFound(List<FinAccountType> finAccountTypes) {
		this.setFinAccountTypes(finAccountTypes);
	}

	public List<FinAccountType> getFinAccountTypes()	{
		return finAccountTypes;
	}

	public void setFinAccountTypes(List<FinAccountType> finAccountTypes)	{
		this.finAccountTypes = finAccountTypes;
	}
}
