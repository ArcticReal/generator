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
import com.skytala.eCommerce.command.AddSubscriptionType;
import com.skytala.eCommerce.command.DeleteSubscriptionType;
import com.skytala.eCommerce.command.UpdateSubscriptionType;
import com.skytala.eCommerce.entity.SubscriptionType;
import com.skytala.eCommerce.entity.SubscriptionTypeMapper;
import com.skytala.eCommerce.event.SubscriptionTypeAdded;
import com.skytala.eCommerce.event.SubscriptionTypeDeleted;
import com.skytala.eCommerce.event.SubscriptionTypeFound;
import com.skytala.eCommerce.event.SubscriptionTypeUpdated;
import com.skytala.eCommerce.query.FindSubscriptionTypesBy;

@RestController
@RequestMapping("/api/subscriptionType")
public class SubscriptionTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SubscriptionType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SubscriptionTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SubscriptionType
	 * @return a List with the SubscriptionTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SubscriptionType> findSubscriptionTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSubscriptionTypesBy query = new FindSubscriptionTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (SubscriptionTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionTypeFound.class,
				event -> sendSubscriptionTypesFoundMessage(((SubscriptionTypeFound) event).getSubscriptionTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSubscriptionTypesFoundMessage(List<SubscriptionType> subscriptionTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, subscriptionTypes);
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
	public boolean createSubscriptionType(HttpServletRequest request) {

		SubscriptionType subscriptionTypeToBeAdded = new SubscriptionType();
		try {
			subscriptionTypeToBeAdded = SubscriptionTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSubscriptionType(subscriptionTypeToBeAdded);

	}

	/**
	 * creates a new SubscriptionType entry in the ofbiz database
	 * 
	 * @param subscriptionTypeToBeAdded
	 *            the SubscriptionType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSubscriptionType(SubscriptionType subscriptionTypeToBeAdded) {

		AddSubscriptionType com = new AddSubscriptionType(subscriptionTypeToBeAdded);
		int usedTicketId;

		synchronized (SubscriptionTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionTypeAdded.class,
				event -> sendSubscriptionTypeChangedMessage(((SubscriptionTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSubscriptionType(HttpServletRequest request) {

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

		SubscriptionType subscriptionTypeToBeUpdated = new SubscriptionType();

		try {
			subscriptionTypeToBeUpdated = SubscriptionTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSubscriptionType(subscriptionTypeToBeUpdated);

	}

	/**
	 * Updates the SubscriptionType with the specific Id
	 * 
	 * @param subscriptionTypeToBeUpdated the SubscriptionType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSubscriptionType(SubscriptionType subscriptionTypeToBeUpdated) {

		UpdateSubscriptionType com = new UpdateSubscriptionType(subscriptionTypeToBeUpdated);

		int usedTicketId;

		synchronized (SubscriptionTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionTypeUpdated.class,
				event -> sendSubscriptionTypeChangedMessage(((SubscriptionTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SubscriptionType from the database
	 * 
	 * @param subscriptionTypeId:
	 *            the id of the SubscriptionType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesubscriptionTypeById(@RequestParam(value = "subscriptionTypeId") String subscriptionTypeId) {

		DeleteSubscriptionType com = new DeleteSubscriptionType(subscriptionTypeId);

		int usedTicketId;

		synchronized (SubscriptionTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionTypeDeleted.class,
				event -> sendSubscriptionTypeChangedMessage(((SubscriptionTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSubscriptionTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/subscriptionType/\" plus one of the following: "
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
