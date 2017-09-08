package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGatewayPayPalFound implements Event{

	private List<PaymentGatewayPayPal> paymentGatewayPayPals;

	public PaymentGatewayPayPalFound(List<PaymentGatewayPayPal> paymentGatewayPayPals) {
		this.setPaymentGatewayPayPals(paymentGatewayPayPals);
	}

	public List<PaymentGatewayPayPal> getPaymentGatewayPayPals()	{
		return paymentGatewayPayPals;
	}

	public void setPaymentGatewayPayPals(List<PaymentGatewayPayPal> paymentGatewayPayPals)	{
		this.paymentGatewayPayPals = paymentGatewayPayPals;
	}
}
