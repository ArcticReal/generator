package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CustRequestAttributeFound implements Event{

	private List<CustRequestAttribute> custRequestAttributes;

	public CustRequestAttributeFound(List<CustRequestAttribute> custRequestAttributes) {
		this.setCustRequestAttributes(custRequestAttributes);
	}

	public List<CustRequestAttribute> getCustRequestAttributes()	{
		return custRequestAttributes;
	}

	public void setCustRequestAttributes(List<CustRequestAttribute> custRequestAttributes)	{
		this.custRequestAttributes = custRequestAttributes;
	}
}
