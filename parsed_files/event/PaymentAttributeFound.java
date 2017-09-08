package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentAttributeFound implements Event{

	private List<PaymentAttribute> paymentAttributes;

	public PaymentAttributeFound(List<PaymentAttribute> paymentAttributes) {
		this.setPaymentAttributes(paymentAttributes);
	}

	public List<PaymentAttribute> getPaymentAttributes()	{
		return paymentAttributes;
	}

	public void setPaymentAttributes(List<PaymentAttribute> paymentAttributes)	{
		this.paymentAttributes = paymentAttributes;
	}
}
