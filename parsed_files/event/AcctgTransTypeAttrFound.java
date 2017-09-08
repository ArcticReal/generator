package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AcctgTransTypeAttrFound implements Event{

	private List<AcctgTransTypeAttr> acctgTransTypeAttrs;

	public AcctgTransTypeAttrFound(List<AcctgTransTypeAttr> acctgTransTypeAttrs) {
		this.setAcctgTransTypeAttrs(acctgTransTypeAttrs);
	}

	public List<AcctgTransTypeAttr> getAcctgTransTypeAttrs()	{
		return acctgTransTypeAttrs;
	}

	public void setAcctgTransTypeAttrs(List<AcctgTransTypeAttr> acctgTransTypeAttrs)	{
		this.acctgTransTypeAttrs = acctgTransTypeAttrs;
	}
}
