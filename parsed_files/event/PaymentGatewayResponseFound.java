package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGatewayResponseFound implements Event{

	private List<PaymentGatewayResponse> paymentGatewayResponses;

	public PaymentGatewayResponseFound(List<PaymentGatewayResponse> paymentGatewayResponses) {
		this.setPaymentGatewayResponses(paymentGatewayResponses);
	}

	public List<PaymentGatewayResponse> getPaymentGatewayResponses()	{
		return paymentGatewayResponses;
	}

	public void setPaymentGatewayResponses(List<PaymentGatewayResponse> paymentGatewayResponses)	{
		this.paymentGatewayResponses = paymentGatewayResponses;
	}
}
