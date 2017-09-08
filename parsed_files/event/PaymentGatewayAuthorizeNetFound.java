package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGatewayAuthorizeNetFound implements Event{

	private List<PaymentGatewayAuthorizeNet> paymentGatewayAuthorizeNets;

	public PaymentGatewayAuthorizeNetFound(List<PaymentGatewayAuthorizeNet> paymentGatewayAuthorizeNets) {
		this.setPaymentGatewayAuthorizeNets(paymentGatewayAuthorizeNets);
	}

	public List<PaymentGatewayAuthorizeNet> getPaymentGatewayAuthorizeNets()	{
		return paymentGatewayAuthorizeNets;
	}

	public void setPaymentGatewayAuthorizeNets(List<PaymentGatewayAuthorizeNet> paymentGatewayAuthorizeNets)	{
		this.paymentGatewayAuthorizeNets = paymentGatewayAuthorizeNets;
	}
}
