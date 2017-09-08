package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CustRequestPartyFound implements Event{

	private List<CustRequestParty> custRequestPartys;

	public CustRequestPartyFound(List<CustRequestParty> custRequestPartys) {
		this.setCustRequestPartys(custRequestPartys);
	}

	public List<CustRequestParty> getCustRequestPartys()	{
		return custRequestPartys;
	}

	public void setCustRequestPartys(List<CustRequestParty> custRequestPartys)	{
		this.custRequestPartys = custRequestPartys;
	}
}
