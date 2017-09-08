package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentFound implements Event{

	private List<Payment> payments;

	public PaymentFound(List<Payment> payments) {
		this.setPayments(payments);
	}

	public List<Payment> getPayments()	{
		return payments;
	}

	public void setPayments(List<Payment> payments)	{
		this.payments = payments;
	}
}
