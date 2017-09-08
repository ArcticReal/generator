package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGatewaySecurePayFound implements Event{

	private List<PaymentGatewaySecurePay> paymentGatewaySecurePays;

	public PaymentGatewaySecurePayFound(List<PaymentGatewaySecurePay> paymentGatewaySecurePays) {
		this.setPaymentGatewaySecurePays(paymentGatewaySecurePays);
	}

	public List<PaymentGatewaySecurePay> getPaymentGatewaySecurePays()	{
		return paymentGatewaySecurePays;
	}

	public void setPaymentGatewaySecurePays(List<PaymentGatewaySecurePay> paymentGatewaySecurePays)	{
		this.paymentGatewaySecurePays = paymentGatewaySecurePays;
	}
}
