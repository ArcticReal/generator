package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class RespondingPartyFound implements Event{

	private List<RespondingParty> respondingPartys;

	public RespondingPartyFound(List<RespondingParty> respondingPartys) {
		this.setRespondingPartys(respondingPartys);
	}

	public List<RespondingParty> getRespondingPartys()	{
		return respondingPartys;
	}

	public void setRespondingPartys(List<RespondingParty> respondingPartys)	{
		this.respondingPartys = respondingPartys;
	}
}
