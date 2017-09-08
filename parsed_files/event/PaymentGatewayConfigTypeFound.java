package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGatewayConfigTypeFound implements Event{

	private List<PaymentGatewayConfigType> paymentGatewayConfigTypes;

	public PaymentGatewayConfigTypeFound(List<PaymentGatewayConfigType> paymentGatewayConfigTypes) {
		this.setPaymentGatewayConfigTypes(paymentGatewayConfigTypes);
	}

	public List<PaymentGatewayConfigType> getPaymentGatewayConfigTypes()	{
		return paymentGatewayConfigTypes;
	}

	public void setPaymentGatewayConfigTypes(List<PaymentGatewayConfigType> paymentGatewayConfigTypes)	{
		this.paymentGatewayConfigTypes = paymentGatewayConfigTypes;
	}
}
