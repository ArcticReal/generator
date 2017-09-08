package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PartyNameHistoryFound implements Event{

	private List<PartyNameHistory> partyNameHistorys;

	public PartyNameHistoryFound(List<PartyNameHistory> partyNameHistorys) {
		this.setPartyNameHistorys(partyNameHistorys);
	}

	public List<PartyNameHistory> getPartyNameHistorys()	{
		return partyNameHistorys;
	}

	public void setPartyNameHistorys(List<PartyNameHistory> partyNameHistorys)	{
		this.partyNameHistorys = partyNameHistorys;
	}
}
