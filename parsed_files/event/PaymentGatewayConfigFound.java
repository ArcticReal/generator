package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class PaymentGatewayConfigFound implements Event{

	private List<PaymentGatewayConfig> paymentGatewayConfigs;

	public PaymentGatewayConfigFound(List<PaymentGatewayConfig> paymentGatewayConfigs) {
		this.setPaymentGatewayConfigs(paymentGatewayConfigs);
	}

	public List<PaymentGatewayConfig> getPaymentGatewayConfigs()	{
		return paymentGatewayConfigs;
	}

	public void setPaymentGatewayConfigs(List<PaymentGatewayConfig> paymentGatewayConfigs)	{
		this.paymentGatewayConfigs = paymentGatewayConfigs;
	}
}
