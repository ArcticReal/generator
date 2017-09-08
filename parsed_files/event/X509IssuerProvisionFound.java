package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class X509IssuerProvisionFound implements Event{

	private List<X509IssuerProvision> x509IssuerProvisions;

	public X509IssuerProvisionFound(List<X509IssuerProvision> x509IssuerProvisions) {
		this.setX509IssuerProvisions(x509IssuerProvisions);
	}

	public List<X509IssuerProvision> getX509IssuerProvisions()	{
		return x509IssuerProvisions;
	}

	public void setX509IssuerProvisions(List<X509IssuerProvision> x509IssuerProvisions)	{
		this.x509IssuerProvisions = x509IssuerProvisions;
	}
}
