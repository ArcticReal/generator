package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CommunicationEventFound implements Event{

	private List<CommunicationEvent> communicationEvents;

	public CommunicationEventFound(List<CommunicationEvent> communicationEvents) {
		this.setCommunicationEvents(communicationEvents);
	}

	public List<CommunicationEvent> getCommunicationEvents()	{
		return communicationEvents;
	}

	public void setCommunicationEvents(List<CommunicationEvent> communicationEvents)	{
		this.communicationEvents = communicationEvents;
	}
}
