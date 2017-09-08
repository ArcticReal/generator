package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGatewaySagePayFound implements Event{

	private List<PaymentGatewaySagePay> paymentGatewaySagePays;

	public PaymentGatewaySagePayFound(List<PaymentGatewaySagePay> paymentGatewaySagePays) {
		this.setPaymentGatewaySagePays(paymentGatewaySagePays);
	}

	public List<PaymentGatewaySagePay> getPaymentGatewaySagePays()	{
		return paymentGatewaySagePays;
	}

	public void setPaymentGatewaySagePays(List<PaymentGatewaySagePay> paymentGatewaySagePays)	{
		this.paymentGatewaySagePays = paymentGatewaySagePays;
	}
}
