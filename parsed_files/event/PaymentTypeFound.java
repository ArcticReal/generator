package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentTypeFound implements Event{

	private List<PaymentType> paymentTypes;

	public PaymentTypeFound(List<PaymentType> paymentTypes) {
		this.setPaymentTypes(paymentTypes);
	}

	public List<PaymentType> getPaymentTypes()	{
		return paymentTypes;
	}

	public void setPaymentTypes(List<PaymentType> paymentTypes)	{
		this.paymentTypes = paymentTypes;
	}
}
