package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentMethodTypeFound implements Event{

	private List<PaymentMethodType> paymentMethodTypes;

	public PaymentMethodTypeFound(List<PaymentMethodType> paymentMethodTypes) {
		this.setPaymentMethodTypes(paymentMethodTypes);
	}

	public List<PaymentMethodType> getPaymentMethodTypes()	{
		return paymentMethodTypes;
	}

	public void setPaymentMethodTypes(List<PaymentMethodType> paymentMethodTypes)	{
		this.paymentMethodTypes = paymentMethodTypes;
	}
}
