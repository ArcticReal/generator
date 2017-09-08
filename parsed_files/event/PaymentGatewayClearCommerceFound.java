package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGatewayClearCommerceFound implements Event{

	private List<PaymentGatewayClearCommerce> paymentGatewayClearCommerces;

	public PaymentGatewayClearCommerceFound(List<PaymentGatewayClearCommerce> paymentGatewayClearCommerces) {
		this.setPaymentGatewayClearCommerces(paymentGatewayClearCommerces);
	}

	public List<PaymentGatewayClearCommerce> getPaymentGatewayClearCommerces()	{
		return paymentGatewayClearCommerces;
	}

	public void setPaymentGatewayClearCommerces(List<PaymentGatewayClearCommerce> paymentGatewayClearCommerces)	{
		this.paymentGatewayClearCommerces = paymentGatewayClearCommerces;
	}
}
