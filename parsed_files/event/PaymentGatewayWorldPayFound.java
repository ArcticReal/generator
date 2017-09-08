package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGatewayWorldPayFound implements Event{

	private List<PaymentGatewayWorldPay> paymentGatewayWorldPays;

	public PaymentGatewayWorldPayFound(List<PaymentGatewayWorldPay> paymentGatewayWorldPays) {
		this.setPaymentGatewayWorldPays(paymentGatewayWorldPays);
	}

	public List<PaymentGatewayWorldPay> getPaymentGatewayWorldPays()	{
		return paymentGatewayWorldPays;
	}

	public void setPaymentGatewayWorldPays(List<PaymentGatewayWorldPay> paymentGatewayWorldPays)	{
		this.paymentGatewayWorldPays = paymentGatewayWorldPays;
	}
}
