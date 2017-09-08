package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PayPalPaymentMethodFound implements Event{

	private List<PayPalPaymentMethod> payPalPaymentMethods;

	public PayPalPaymentMethodFound(List<PayPalPaymentMethod> payPalPaymentMethods) {
		this.setPayPalPaymentMethods(payPalPaymentMethods);
	}

	public List<PayPalPaymentMethod> getPayPalPaymentMethods()	{
		return payPalPaymentMethods;
	}

	public void setPayPalPaymentMethods(List<PayPalPaymentMethod> payPalPaymentMethods)	{
		this.payPalPaymentMethods = payPalPaymentMethods;
	}
}
