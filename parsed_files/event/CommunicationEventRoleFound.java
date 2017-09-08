package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CommunicationEventRoleFound implements Event{

	private List<CommunicationEventRole> communicationEventRoles;

	public CommunicationEventRoleFound(List<CommunicationEventRole> communicationEventRoles) {
		this.setCommunicationEventRoles(communicationEventRoles);
	}

	public List<CommunicationEventRole> getCommunicationEventRoles()	{
		return communicationEventRoles;
	}

	public void setCommunicationEventRoles(List<CommunicationEventRole> communicationEventRoles)	{
		this.communicationEventRoles = communicationEventRoles;
	}
}
