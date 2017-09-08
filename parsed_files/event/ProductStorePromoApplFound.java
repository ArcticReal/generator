package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStorePromoApplFound implements Event{

	private List<ProductStorePromoAppl> productStorePromoAppls;

	public ProductStorePromoApplFound(List<ProductStorePromoAppl> productStorePromoAppls) {
		this.setProductStorePromoAppls(productStorePromoAppls);
	}

	public List<ProductStorePromoAppl> getProductStorePromoAppls()	{
		return productStorePromoAppls;
	}

	public void setProductStorePromoAppls(List<ProductStorePromoAppl> productStorePromoAppls)	{
		this.productStorePromoAppls = productStorePromoAppls;
	}
}
