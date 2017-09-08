package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGatewayOrbitalFound implements Event{

	private List<PaymentGatewayOrbital> paymentGatewayOrbitals;

	public PaymentGatewayOrbitalFound(List<PaymentGatewayOrbital> paymentGatewayOrbitals) {
		this.setPaymentGatewayOrbitals(paymentGatewayOrbitals);
	}

	public List<PaymentGatewayOrbital> getPaymentGatewayOrbitals()	{
		return paymentGatewayOrbitals;
	}

	public void setPaymentGatewayOrbitals(List<PaymentGatewayOrbital> paymentGatewayOrbitals)	{
		this.paymentGatewayOrbitals = paymentGatewayOrbitals;
	}
}
