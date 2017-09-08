package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AcctgTransTypeFound implements Event{

	private List<AcctgTransType> acctgTransTypes;

	public AcctgTransTypeFound(List<AcctgTransType> acctgTransTypes) {
		this.setAcctgTransTypes(acctgTransTypes);
	}

	public List<AcctgTransType> getAcctgTransTypes()	{
		return acctgTransTypes;
	}

	public void setAcctgTransTypes(List<AcctgTransType> acctgTransTypes)	{
		this.acctgTransTypes = acctgTransTypes;
	}
}
