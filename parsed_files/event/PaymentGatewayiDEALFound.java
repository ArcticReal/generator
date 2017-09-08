package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGatewayiDEALFound implements Event{

	private List<PaymentGatewayiDEAL> paymentGatewayiDEALs;

	public PaymentGatewayiDEALFound(List<PaymentGatewayiDEAL> paymentGatewayiDEALs) {
		this.setPaymentGatewayiDEALs(paymentGatewayiDEALs);
	}

	public List<PaymentGatewayiDEAL> getPaymentGatewayiDEALs()	{
		return paymentGatewayiDEALs;
	}

	public void setPaymentGatewayiDEALs(List<PaymentGatewayiDEAL> paymentGatewayiDEALs)	{
		this.paymentGatewayiDEALs = paymentGatewayiDEALs;
	}
}
