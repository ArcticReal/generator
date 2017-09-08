package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FinAccountAttributeFound implements Event{

	private List<FinAccountAttribute> finAccountAttributes;

	public FinAccountAttributeFound(List<FinAccountAttribute> finAccountAttributes) {
		this.setFinAccountAttributes(finAccountAttributes);
	}

	public List<FinAccountAttribute> getFinAccountAttributes()	{
		return finAccountAttributes;
	}

	public void setFinAccountAttributes(List<FinAccountAttribute> finAccountAttributes)	{
		this.finAccountAttributes = finAccountAttributes;
	}
}
