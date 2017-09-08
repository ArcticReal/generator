package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CommunicationEventProductFound implements Event{

	private List<CommunicationEventProduct> communicationEventProducts;

	public CommunicationEventProductFound(List<CommunicationEventProduct> communicationEventProducts) {
		this.setCommunicationEventProducts(communicationEventProducts);
	}

	public List<CommunicationEventProduct> getCommunicationEventProducts()	{
		return communicationEventProducts;
	}

	public void setCommunicationEventProducts(List<CommunicationEventProduct> communicationEventProducts)	{
		this.communicationEventProducts = communicationEventProducts;
	}
}
