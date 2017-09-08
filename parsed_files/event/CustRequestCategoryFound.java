package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class CustRequestCategoryFound implements Event{

	private List<CustRequestCategory> custRequestCategorys;

	public CustRequestCategoryFound(List<CustRequestCategory> custRequestCategorys) {
		this.setCustRequestCategorys(custRequestCategorys);
	}

	public List<CustRequestCategory> getCustRequestCategorys()	{
		return custRequestCategorys;
	}

	public void setCustRequestCategorys(List<CustRequestCategory> custRequestCategorys)	{
		this.custRequestCategorys = custRequestCategorys;
	}
}
