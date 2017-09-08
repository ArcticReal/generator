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
import com.skytala.eCommerce.command.AddSubscription;
import com.skytala.eCommerce.command.DeleteSubscription;
import com.skytala.eCommerce.command.UpdateSubscription;
import com.skytala.eCommerce.entity.Subscription;
import com.skytala.eCommerce.entity.SubscriptionMapper;
import com.skytala.eCommerce.event.SubscriptionAdded;
import com.skytala.eCommerce.event.SubscriptionDeleted;
import com.skytala.eCommerce.event.SubscriptionFound;
import com.skytala.eCommerce.event.SubscriptionUpdated;
import com.skytala.eCommerce.query.FindSubscriptionsBy;

@RestController
@RequestMapping("/api/subscription")
public class SubscriptionController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<Subscription>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SubscriptionController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a Subscription
	 * @return a List with the Subscriptions
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<Subscription> findSubscriptionsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSubscriptionsBy query = new FindSubscriptionsBy(allRequestParams);

		int usedTicketId;

		synchronized (SubscriptionController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionFound.class,
				event -> sendSubscriptionsFoundMessage(((SubscriptionFound) event).getSubscriptions(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSubscriptionsFoundMessage(List<Subscription> subscriptions, int usedTicketId) {
		queryReturnVal.put(usedTicketId, subscriptions);
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
	public boolean createSubscription(HttpServletRequest request) {

		Subscription subscriptionToBeAdded = new Subscription();
		try {
			subscriptionToBeAdded = SubscriptionMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSubscription(subscriptionToBeAdded);

	}

	/**
	 * creates a new Subscription entry in the ofbiz database
	 * 
	 * @param subscriptionToBeAdded
	 *            the Subscription thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSubscription(Subscription subscriptionToBeAdded) {

		AddSubscription com = new AddSubscription(subscriptionToBeAdded);
		int usedTicketId;

		synchronized (SubscriptionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionAdded.class,
				event -> sendSubscriptionChangedMessage(((SubscriptionAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSubscription(HttpServletRequest request) {

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

		Subscription subscriptionToBeUpdated = new Subscription();

		try {
			subscriptionToBeUpdated = SubscriptionMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSubscription(subscriptionToBeUpdated);

	}

	/**
	 * Updates the Subscription with the specific Id
	 * 
	 * @param subscriptionToBeUpdated the Subscription thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSubscription(Subscription subscriptionToBeUpdated) {

		UpdateSubscription com = new UpdateSubscription(subscriptionToBeUpdated);

		int usedTicketId;

		synchronized (SubscriptionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionUpdated.class,
				event -> sendSubscriptionChangedMessage(((SubscriptionUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a Subscription from the database
	 * 
	 * @param subscriptionId:
	 *            the id of the Subscription thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesubscriptionById(@RequestParam(value = "subscriptionId") String subscriptionId) {

		DeleteSubscription com = new DeleteSubscription(subscriptionId);

		int usedTicketId;

		synchronized (SubscriptionController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionDeleted.class,
				event -> sendSubscriptionChangedMessage(((SubscriptionDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSubscriptionChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/subscription/\" plus one of the following: "
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
