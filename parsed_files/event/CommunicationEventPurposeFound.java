package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CommunicationEventPurposeFound implements Event{

	private List<CommunicationEventPurpose> communicationEventPurposes;

	public CommunicationEventPurposeFound(List<CommunicationEventPurpose> communicationEventPurposes) {
		this.setCommunicationEventPurposes(communicationEventPurposes);
	}

	public List<CommunicationEventPurpose> getCommunicationEventPurposes()	{
		return communicationEventPurposes;
	}

	public void setCommunicationEventPurposes(List<CommunicationEventPurpose> communicationEventPurposes)	{
		this.communicationEventPurposes = communicationEventPurposes;
	}
}
