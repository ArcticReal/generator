package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGatewayPayflowProFound implements Event{

	private List<PaymentGatewayPayflowPro> paymentGatewayPayflowPros;

	public PaymentGatewayPayflowProFound(List<PaymentGatewayPayflowPro> paymentGatewayPayflowPros) {
		this.setPaymentGatewayPayflowPros(paymentGatewayPayflowPros);
	}

	public List<PaymentGatewayPayflowPro> getPaymentGatewayPayflowPros()	{
		return paymentGatewayPayflowPros;
	}

	public void setPaymentGatewayPayflowPros(List<PaymentGatewayPayflowPro> paymentGatewayPayflowPros)	{
		this.paymentGatewayPayflowPros = paymentGatewayPayflowPros;
	}
}
