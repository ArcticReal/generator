package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AcctgTransAttributeFound implements Event{

	private List<AcctgTransAttribute> acctgTransAttributes;

	public AcctgTransAttributeFound(List<AcctgTransAttribute> acctgTransAttributes) {
		this.setAcctgTransAttributes(acctgTransAttributes);
	}

	public List<AcctgTransAttribute> getAcctgTransAttributes()	{
		return acctgTransAttributes;
	}

	public void setAcctgTransAttributes(List<AcctgTransAttribute> acctgTransAttributes)	{
		this.acctgTransAttributes = acctgTransAttributes;
	}
}
