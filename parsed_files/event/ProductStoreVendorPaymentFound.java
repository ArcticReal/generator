package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class ProductStoreVendorPaymentFound implements Event{

	private List<ProductStoreVendorPayment> productStoreVendorPayments;

	public ProductStoreVendorPaymentFound(List<ProductStoreVendorPayment> productStoreVendorPayments) {
		this.setProductStoreVendorPayments(productStoreVendorPayments);
	}

	public List<ProductStoreVendorPayment> getProductStoreVendorPayments()	{
		return productStoreVendorPayments;
	}

	public void setProductStoreVendorPayments(List<ProductStoreVendorPayment> productStoreVendorPayments)	{
		this.productStoreVendorPayments = productStoreVendorPayments;
	}
}
