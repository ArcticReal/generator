package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AcctgTransFound implements Event{

	private List<AcctgTrans> acctgTranss;

	public AcctgTransFound(List<AcctgTrans> acctgTranss) {
		this.setAcctgTranss(acctgTranss);
	}

	public List<AcctgTrans> getAcctgTranss()	{
		return acctgTranss;
	}

	public void setAcctgTranss(List<AcctgTrans> acctgTranss)	{
		this.acctgTranss = acctgTranss;
	}
}
