package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class SettlementTermFound implements Event{

	private List<SettlementTerm> settlementTerms;

	public SettlementTermFound(List<SettlementTerm> settlementTerms) {
		this.setSettlementTerms(settlementTerms);
	}

	public List<SettlementTerm> getSettlementTerms()	{
		return settlementTerms;
	}

	public void setSettlementTerms(List<SettlementTerm> settlementTerms)	{
		this.settlementTerms = settlementTerms;
	}
}
