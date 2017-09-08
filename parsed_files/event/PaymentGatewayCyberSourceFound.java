package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGatewayCyberSourceFound implements Event{

	private List<PaymentGatewayCyberSource> paymentGatewayCyberSources;

	public PaymentGatewayCyberSourceFound(List<PaymentGatewayCyberSource> paymentGatewayCyberSources) {
		this.setPaymentGatewayCyberSources(paymentGatewayCyberSources);
	}

	public List<PaymentGatewayCyberSource> getPaymentGatewayCyberSources()	{
		return paymentGatewayCyberSources;
	}

	public void setPaymentGatewayCyberSources(List<PaymentGatewayCyberSource> paymentGatewayCyberSources)	{
		this.paymentGatewayCyberSources = paymentGatewayCyberSources;
	}
}
