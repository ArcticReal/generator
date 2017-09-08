package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class FinAccountTransAttributeFound implements Event{

	private List<FinAccountTransAttribute> finAccountTransAttributes;

	public FinAccountTransAttributeFound(List<FinAccountTransAttribute> finAccountTransAttributes) {
		this.setFinAccountTransAttributes(finAccountTransAttributes);
	}

	public List<FinAccountTransAttribute> getFinAccountTransAttributes()	{
		return finAccountTransAttributes;
	}

	public void setFinAccountTransAttributes(List<FinAccountTransAttribute> finAccountTransAttributes)	{
		this.finAccountTransAttributes = finAccountTransAttributes;
	}
}
