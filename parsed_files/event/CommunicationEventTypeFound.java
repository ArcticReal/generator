package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CommunicationEventTypeFound implements Event{

	private List<CommunicationEventType> communicationEventTypes;

	public CommunicationEventTypeFound(List<CommunicationEventType> communicationEventTypes) {
		this.setCommunicationEventTypes(communicationEventTypes);
	}

	public List<CommunicationEventType> getCommunicationEventTypes()	{
		return communicationEventTypes;
	}

	public void setCommunicationEventTypes(List<CommunicationEventType> communicationEventTypes)	{
		this.communicationEventTypes = communicationEventTypes;
	}
}
