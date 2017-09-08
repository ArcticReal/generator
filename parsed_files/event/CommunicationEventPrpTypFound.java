package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CommunicationEventPrpTypFound implements Event{

	private List<CommunicationEventPrpTyp> communicationEventPrpTyps;

	public CommunicationEventPrpTypFound(List<CommunicationEventPrpTyp> communicationEventPrpTyps) {
		this.setCommunicationEventPrpTyps(communicationEventPrpTyps);
	}

	public List<CommunicationEventPrpTyp> getCommunicationEventPrpTyps()	{
		return communicationEventPrpTyps;
	}

	public void setCommunicationEventPrpTyps(List<CommunicationEventPrpTyp> communicationEventPrpTyps)	{
		this.communicationEventPrpTyps = communicationEventPrpTyps;
	}
}
