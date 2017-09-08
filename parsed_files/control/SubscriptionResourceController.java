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
import com.skytala.eCommerce.command.AddSubscriptionResource;
import com.skytala.eCommerce.command.DeleteSubscriptionResource;
import com.skytala.eCommerce.command.UpdateSubscriptionResource;
import com.skytala.eCommerce.entity.SubscriptionResource;
import com.skytala.eCommerce.entity.SubscriptionResourceMapper;
import com.skytala.eCommerce.event.SubscriptionResourceAdded;
import com.skytala.eCommerce.event.SubscriptionResourceDeleted;
import com.skytala.eCommerce.event.SubscriptionResourceFound;
import com.skytala.eCommerce.event.SubscriptionResourceUpdated;
import com.skytala.eCommerce.query.FindSubscriptionResourcesBy;

@RestController
@RequestMapping("/api/subscriptionResource")
public class SubscriptionResourceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SubscriptionResource>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SubscriptionResourceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SubscriptionResource
	 * @return a List with the SubscriptionResources
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SubscriptionResource> findSubscriptionResourcesBy(@RequestParam Map<String, String> allRequestParams) {

		FindSubscriptionResourcesBy query = new FindSubscriptionResourcesBy(allRequestParams);

		int usedTicketId;

		synchronized (SubscriptionResourceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionResourceFound.class,
				event -> sendSubscriptionResourcesFoundMessage(((SubscriptionResourceFound) event).getSubscriptionResources(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSubscriptionResourcesFoundMessage(List<SubscriptionResource> subscriptionResources, int usedTicketId) {
		queryReturnVal.put(usedTicketId, subscriptionResources);
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
	public boolean createSubscriptionResource(HttpServletRequest request) {

		SubscriptionResource subscriptionResourceToBeAdded = new SubscriptionResource();
		try {
			subscriptionResourceToBeAdded = SubscriptionResourceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSubscriptionResource(subscriptionResourceToBeAdded);

	}

	/**
	 * creates a new SubscriptionResource entry in the ofbiz database
	 * 
	 * @param subscriptionResourceToBeAdded
	 *            the SubscriptionResource thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSubscriptionResource(SubscriptionResource subscriptionResourceToBeAdded) {

		AddSubscriptionResource com = new AddSubscriptionResource(subscriptionResourceToBeAdded);
		int usedTicketId;

		synchronized (SubscriptionResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionResourceAdded.class,
				event -> sendSubscriptionResourceChangedMessage(((SubscriptionResourceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSubscriptionResource(HttpServletRequest request) {

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

		SubscriptionResource subscriptionResourceToBeUpdated = new SubscriptionResource();

		try {
			subscriptionResourceToBeUpdated = SubscriptionResourceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSubscriptionResource(subscriptionResourceToBeUpdated);

	}

	/**
	 * Updates the SubscriptionResource with the specific Id
	 * 
	 * @param subscriptionResourceToBeUpdated the SubscriptionResource thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSubscriptionResource(SubscriptionResource subscriptionResourceToBeUpdated) {

		UpdateSubscriptionResource com = new UpdateSubscriptionResource(subscriptionResourceToBeUpdated);

		int usedTicketId;

		synchronized (SubscriptionResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionResourceUpdated.class,
				event -> sendSubscriptionResourceChangedMessage(((SubscriptionResourceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SubscriptionResource from the database
	 * 
	 * @param subscriptionResourceId:
	 *            the id of the SubscriptionResource thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesubscriptionResourceById(@RequestParam(value = "subscriptionResourceId") String subscriptionResourceId) {

		DeleteSubscriptionResource com = new DeleteSubscriptionResource(subscriptionResourceId);

		int usedTicketId;

		synchronized (SubscriptionResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionResourceDeleted.class,
				event -> sendSubscriptionResourceChangedMessage(((SubscriptionResourceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSubscriptionResourceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/subscriptionResource/\" plus one of the following: "
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
