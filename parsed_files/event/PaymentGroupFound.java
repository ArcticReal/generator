package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGroupFound implements Event{

	private List<PaymentGroup> paymentGroups;

	public PaymentGroupFound(List<PaymentGroup> paymentGroups) {
		this.setPaymentGroups(paymentGroups);
	}

	public List<PaymentGroup> getPaymentGroups()	{
		return paymentGroups;
	}

	public void setPaymentGroups(List<PaymentGroup> paymentGroups)	{
		this.paymentGroups = paymentGroups;
	}
}
