package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ValueLinkKeyFound implements Event{

	private List<ValueLinkKey> valueLinkKeys;

	public ValueLinkKeyFound(List<ValueLinkKey> valueLinkKeys) {
		this.setValueLinkKeys(valueLinkKeys);
	}

	public List<ValueLinkKey> getValueLinkKeys()	{
		return valueLinkKeys;
	}

	public void setValueLinkKeys(List<ValueLinkKey> valueLinkKeys)	{
		this.valueLinkKeys = valueLinkKeys;
	}
}
