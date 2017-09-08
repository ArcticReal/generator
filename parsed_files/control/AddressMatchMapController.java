package com.skytala.eCommerce.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Splitter;
import com.skytala.eCommerce.command.AddAddressMatchMap;
import com.skytala.eCommerce.command.DeleteAddressMatchMap;
import com.skytala.eCommerce.command.UpdateAddressMatchMap;
import com.skytala.eCommerce.entity.AddressMatchMap;
import com.skytala.eCommerce.entity.AddressMatchMapMapper;
import com.skytala.eCommerce.event.AddressMatchMapAdded;
import com.skytala.eCommerce.event.AddressMatchMapDeleted;
import com.skytala.eCommerce.event.AddressMatchMapFound;
import com.skytala.eCommerce.event.AddressMatchMapUpdated;
import com.skytala.eCommerce.query.FindAddressMatchMapsBy;

@RestController
@RequestMapping("/api/addressMatchMap")
public class AddressMatchMapController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AddressMatchMap>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AddressMatchMapController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AddressMatchMap
	 * @return a List with the AddressMatchMaps
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AddressMatchMap> findAddressMatchMapsBy(@RequestParam Map<String, String> allRequestParams) {

		FindAddressMatchMapsBy query = new FindAddressMatchMapsBy(allRequestParams);

		int usedTicketId;

		synchronized (AddressMatchMapController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AddressMatchMapFound.class,
				event -> sendAddressMatchMapsFoundMessage(((AddressMatchMapFound) event).getAddressMatchMaps(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAddressMatchMapsFoundMessage(List<AddressMatchMap> addressMatchMaps, int usedTicketId) {
		queryReturnVal.put(usedTicketId, addressMatchMaps);
	}

	/**
	 * 
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request
	 *            HttpServletRequest
	 * @return true on success; false on fail
	 */
	@RequestMapping(method = RequestMethod.POST, value = "/add", consumes = "application/x-www-form-urlencoded")
	public boolean createAddressMatchMap(HttpServletRequest request) {

		AddressMatchMap addressMatchMapToBeAdded = new AddressMatchMap();
		try {
			addressMatchMapToBeAdded = AddressMatchMapMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAddressMatchMap(addressMatchMapToBeAdded);

	}

	/**
	 * creates a new AddressMatchMap entry in the ofbiz database
	 * 
	 * @param addressMatchMapToBeAdded
	 *            the AddressMatchMap thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAddressMatchMap(AddressMatchMap addressMatchMapToBeAdded) {

		AddAddressMatchMap com = new AddAddressMatchMap(addressMatchMapToBeAdded);
		int usedTicketId;

		synchronized (AddressMatchMapController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AddressMatchMapAdded.class,
				event -> sendAddressMatchMapChangedMessage(((AddressMatchMapAdded) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);

	}

	/**
	 * this method will only be called by Springs DispatcherServlet
	 * 
	 * @param request HttpServletRequest object
	 * @return true on success, false on fail
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "/update", consumes = "application/x-www-form-urlencoded")
	public boolean updateAddressMatchMap(HttpServletRequest request) {

		BufferedReader br;
		String data = null;
		Map<String, String> dataMap = null;

		try {
			br = new BufferedReader(new InputStreamReader(request.getInputStream()));
			if (br != null) {
				data = java.net.URLDecoder.decode(br.readLine(), "UTF-8");
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

		dataMap = Splitter.on('&').trimResults().withKeyValueSeparator(Splitter.on('=').limit(2).trimResults())
				.split(data);

		AddressMatchMap addressMatchMapToBeUpdated = new AddressMatchMap();

		try {
			addressMatchMapToBeUpdated = AddressMatchMapMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAddressMatchMap(addressMatchMapToBeUpdated);

	}

	/**
	 * Updates the AddressMatchMap with the specific Id
	 * 
	 * @param addressMatchMapToBeUpdated the AddressMatchMap thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAddressMatchMap(AddressMatchMap addressMatchMapToBeUpdated) {

		UpdateAddressMatchMap com = new UpdateAddressMatchMap(addressMatchMapToBeUpdated);

		int usedTicketId;

		synchronized (AddressMatchMapController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AddressMatchMapUpdated.class,
				event -> sendAddressMatchMapChangedMessage(((AddressMatchMapUpdated) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	/**
	 * removes a AddressMatchMap from the database
	 * 
	 * @param addressMatchMapId:
	 *            the id of the AddressMatchMap thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteaddressMatchMapById(@RequestParam(value = "addressMatchMapId") String addressMatchMapId) {

		DeleteAddressMatchMap com = new DeleteAddressMatchMap(addressMatchMapId);

		int usedTicketId;

		synchronized (AddressMatchMapController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AddressMatchMapDeleted.class,
				event -> sendAddressMatchMapChangedMessage(((AddressMatchMapDeleted) event).isSuccess(), usedTicketId));

		try {
			Scheduler.instance().schedule(com).executeNext();
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}
		while (!commandReturnVal.containsKey(usedTicketId)) {
		}

		return commandReturnVal.remove(usedTicketId);
	}

	public void sendAddressMatchMapChangedMessage(boolean success, int usedTicketId) {
		commandReturnVal.put(usedTicketId, success);
	}

	@RequestMapping(value = (" * "))
	public String returnErrorPage(HttpServletRequest request) {

		String usedUri = request.getRequestURI();
		String[] splittedString = usedUri.split("/");

		String usedRequest = splittedString[splittedString.length - 1];

		if (validRequests.containsKey(usedRequest)) {
			return "Error: request method " + request.getMethod() + " not allowed for \"" + usedUri + "\"!\n"
					+ "Please use " + validRequests.get(usedRequest) + "!";

		}

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/addressMatchMap/\" plus one of the following: "
				+ "";

		Set<String> keySet = validRequests.keySet();
		Iterator<String> it = keySet.iterator();

		while (it.hasNext()) {
			returnVal += "\"" + it.next() + "\"";
			if (it.hasNext())
				returnVal += ", ";
		}

		returnVal += "!";

		return returnVal;

	}
}
