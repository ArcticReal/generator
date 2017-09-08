package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CommunicationEventWorkEffFound implements Event{

	private List<CommunicationEventWorkEff> communicationEventWorkEffs;

	public CommunicationEventWorkEffFound(List<CommunicationEventWorkEff> communicationEventWorkEffs) {
		this.setCommunicationEventWorkEffs(communicationEventWorkEffs);
	}

	public List<CommunicationEventWorkEff> getCommunicationEventWorkEffs()	{
		return communicationEventWorkEffs;
	}

	public void setCommunicationEventWorkEffs(List<CommunicationEventWorkEff> communicationEventWorkEffs)	{
		this.communicationEventWorkEffs = communicationEventWorkEffs;
	}
}
