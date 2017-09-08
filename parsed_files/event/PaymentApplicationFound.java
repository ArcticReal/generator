package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentApplicationFound implements Event{

	private List<PaymentApplication> paymentApplications;

	public PaymentApplicationFound(List<PaymentApplication> paymentApplications) {
		this.setPaymentApplications(paymentApplications);
	}

	public List<PaymentApplication> getPaymentApplications()	{
		return paymentApplications;
	}

	public void setPaymentApplications(List<PaymentApplication> paymentApplications)	{
		this.paymentApplications = paymentApplications;
	}
}
