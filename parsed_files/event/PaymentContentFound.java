package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentContentFound implements Event{

	private List<PaymentContent> paymentContents;

	public PaymentContentFound(List<PaymentContent> paymentContents) {
		this.setPaymentContents(paymentContents);
	}

	public List<PaymentContent> getPaymentContents()	{
		return paymentContents;
	}

	public void setPaymentContents(List<PaymentContent> paymentContents)	{
		this.paymentContents = paymentContents;
	}
}
