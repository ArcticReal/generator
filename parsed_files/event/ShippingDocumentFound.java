package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ShippingDocumentFound implements Event{

	private List<ShippingDocument> shippingDocuments;

	public ShippingDocumentFound(List<ShippingDocument> shippingDocuments) {
		this.setShippingDocuments(shippingDocuments);
	}

	public List<ShippingDocument> getShippingDocuments()	{
		return shippingDocuments;
	}

	public void setShippingDocuments(List<ShippingDocument> shippingDocuments)	{
		this.shippingDocuments = shippingDocuments;
	}
}
