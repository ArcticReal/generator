package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AcctgTransEntryTypeFound implements Event{

	private List<AcctgTransEntryType> acctgTransEntryTypes;

	public AcctgTransEntryTypeFound(List<AcctgTransEntryType> acctgTransEntryTypes) {
		this.setAcctgTransEntryTypes(acctgTransEntryTypes);
	}

	public List<AcctgTransEntryType> getAcctgTransEntryTypes()	{
		return acctgTransEntryTypes;
	}

	public void setAcctgTransEntryTypes(List<AcctgTransEntryType> acctgTransEntryTypes)	{
		this.acctgTransEntryTypes = acctgTransEntryTypes;
	}
}
