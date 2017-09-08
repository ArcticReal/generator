package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGatewayEwayFound implements Event{

	private List<PaymentGatewayEway> paymentGatewayEways;

	public PaymentGatewayEwayFound(List<PaymentGatewayEway> paymentGatewayEways) {
		this.setPaymentGatewayEways(paymentGatewayEways);
	}

	public List<PaymentGatewayEway> getPaymentGatewayEways()	{
		return paymentGatewayEways;
	}

	public void setPaymentGatewayEways(List<PaymentGatewayEway> paymentGatewayEways)	{
		this.paymentGatewayEways = paymentGatewayEways;
	}
}
