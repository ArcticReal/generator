package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AcctgTransEntryFound implements Event{

	private List<AcctgTransEntry> acctgTransEntrys;

	public AcctgTransEntryFound(List<AcctgTransEntry> acctgTransEntrys) {
		this.setAcctgTransEntrys(acctgTransEntrys);
	}

	public List<AcctgTransEntry> getAcctgTransEntrys()	{
		return acctgTransEntrys;
	}

	public void setAcctgTransEntrys(List<AcctgTransEntry> acctgTransEntrys)	{
		this.acctgTransEntrys = acctgTransEntrys;
	}
}
