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
import com.skytala.eCommerce.command.AddSubscriptionCommEvent;
import com.skytala.eCommerce.command.DeleteSubscriptionCommEvent;
import com.skytala.eCommerce.command.UpdateSubscriptionCommEvent;
import com.skytala.eCommerce.entity.SubscriptionCommEvent;
import com.skytala.eCommerce.entity.SubscriptionCommEventMapper;
import com.skytala.eCommerce.event.SubscriptionCommEventAdded;
import com.skytala.eCommerce.event.SubscriptionCommEventDeleted;
import com.skytala.eCommerce.event.SubscriptionCommEventFound;
import com.skytala.eCommerce.event.SubscriptionCommEventUpdated;
import com.skytala.eCommerce.query.FindSubscriptionCommEventsBy;

@RestController
@RequestMapping("/api/subscriptionCommEvent")
public class SubscriptionCommEventController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<SubscriptionCommEvent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public SubscriptionCommEventController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a SubscriptionCommEvent
	 * @return a List with the SubscriptionCommEvents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<SubscriptionCommEvent> findSubscriptionCommEventsBy(@RequestParam Map<String, String> allRequestParams) {

		FindSubscriptionCommEventsBy query = new FindSubscriptionCommEventsBy(allRequestParams);

		int usedTicketId;

		synchronized (SubscriptionCommEventController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionCommEventFound.class,
				event -> sendSubscriptionCommEventsFoundMessage(((SubscriptionCommEventFound) event).getSubscriptionCommEvents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendSubscriptionCommEventsFoundMessage(List<SubscriptionCommEvent> subscriptionCommEvents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, subscriptionCommEvents);
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
	public boolean createSubscriptionCommEvent(HttpServletRequest request) {

		SubscriptionCommEvent subscriptionCommEventToBeAdded = new SubscriptionCommEvent();
		try {
			subscriptionCommEventToBeAdded = SubscriptionCommEventMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createSubscriptionCommEvent(subscriptionCommEventToBeAdded);

	}

	/**
	 * creates a new SubscriptionCommEvent entry in the ofbiz database
	 * 
	 * @param subscriptionCommEventToBeAdded
	 *            the SubscriptionCommEvent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createSubscriptionCommEvent(SubscriptionCommEvent subscriptionCommEventToBeAdded) {

		AddSubscriptionCommEvent com = new AddSubscriptionCommEvent(subscriptionCommEventToBeAdded);
		int usedTicketId;

		synchronized (SubscriptionCommEventController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionCommEventAdded.class,
				event -> sendSubscriptionCommEventChangedMessage(((SubscriptionCommEventAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateSubscriptionCommEvent(HttpServletRequest request) {

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

		SubscriptionCommEvent subscriptionCommEventToBeUpdated = new SubscriptionCommEvent();

		try {
			subscriptionCommEventToBeUpdated = SubscriptionCommEventMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateSubscriptionCommEvent(subscriptionCommEventToBeUpdated);

	}

	/**
	 * Updates the SubscriptionCommEvent with the specific Id
	 * 
	 * @param subscriptionCommEventToBeUpdated the SubscriptionCommEvent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateSubscriptionCommEvent(SubscriptionCommEvent subscriptionCommEventToBeUpdated) {

		UpdateSubscriptionCommEvent com = new UpdateSubscriptionCommEvent(subscriptionCommEventToBeUpdated);

		int usedTicketId;

		synchronized (SubscriptionCommEventController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionCommEventUpdated.class,
				event -> sendSubscriptionCommEventChangedMessage(((SubscriptionCommEventUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a SubscriptionCommEvent from the database
	 * 
	 * @param subscriptionCommEventId:
	 *            the id of the SubscriptionCommEvent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletesubscriptionCommEventById(@RequestParam(value = "subscriptionCommEventId") String subscriptionCommEventId) {

		DeleteSubscriptionCommEvent com = new DeleteSubscriptionCommEvent(subscriptionCommEventId);

		int usedTicketId;

		synchronized (SubscriptionCommEventController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(SubscriptionCommEventDeleted.class,
				event -> sendSubscriptionCommEventChangedMessage(((SubscriptionCommEventDeleted) event).isSuccess(), usedTicketId));

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

	public void sendSubscriptionCommEventChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/subscriptionCommEvent/\" plus one of the following: "
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
