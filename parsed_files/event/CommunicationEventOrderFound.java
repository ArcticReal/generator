package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CommunicationEventOrderFound implements Event{

	private List<CommunicationEventOrder> communicationEventOrders;

	public CommunicationEventOrderFound(List<CommunicationEventOrder> communicationEventOrders) {
		this.setCommunicationEventOrders(communicationEventOrders);
	}

	public List<CommunicationEventOrder> getCommunicationEventOrders()	{
		return communicationEventOrders;
	}

	public void setCommunicationEventOrders(List<CommunicationEventOrder> communicationEventOrders)	{
		this.communicationEventOrders = communicationEventOrders;
	}
}
