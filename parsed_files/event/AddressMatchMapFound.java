package com.skytala.eCommerce.event;

import java.util.List;

import com.skytala.eCommerce.entity.Party;
import com.skytala.eCommerce.control.Event;

public class AddressMatchMapFound implements Event{

	private List<AddressMatchMap> addressMatchMaps;

	public AddressMatchMapFound(List<AddressMatchMap> addressMatchMaps) {
		this.setAddressMatchMaps(addressMatchMaps);
	}

	public List<AddressMatchMap> getAddressMatchMaps()	{
		return addressMatchMaps;
	}

	public void setAddressMatchMaps(List<AddressMatchMap> addressMatchMaps)	{
		this.addressMatchMaps = addressMatchMaps;
	}
}
