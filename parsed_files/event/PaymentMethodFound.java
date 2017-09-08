package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentMethodFound implements Event{

	private List<PaymentMethod> paymentMethods;

	public PaymentMethodFound(List<PaymentMethod> paymentMethods) {
		this.setPaymentMethods(paymentMethods);
	}

	public List<PaymentMethod> getPaymentMethods()	{
		return paymentMethods;
	}

	public void setPaymentMethods(List<PaymentMethod> paymentMethods)	{
		this.paymentMethods = paymentMethods;
	}
}
