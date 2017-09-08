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
import com.skytala.eCommerce.command.AddDeliverableType;
import com.skytala.eCommerce.command.DeleteDeliverableType;
import com.skytala.eCommerce.command.UpdateDeliverableType;
import com.skytala.eCommerce.entity.DeliverableType;
import com.skytala.eCommerce.entity.DeliverableTypeMapper;
import com.skytala.eCommerce.event.DeliverableTypeAdded;
import com.skytala.eCommerce.event.DeliverableTypeDeleted;
import com.skytala.eCommerce.event.DeliverableTypeFound;
import com.skytala.eCommerce.event.DeliverableTypeUpdated;
import com.skytala.eCommerce.query.FindDeliverableTypesBy;

@RestController
@RequestMapping("/api/deliverableType")
public class DeliverableTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<DeliverableType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DeliverableTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a DeliverableType
	 * @return a List with the DeliverableTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<DeliverableType> findDeliverableTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindDeliverableTypesBy query = new FindDeliverableTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (DeliverableTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeliverableTypeFound.class,
				event -> sendDeliverableTypesFoundMessage(((DeliverableTypeFound) event).getDeliverableTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDeliverableTypesFoundMessage(List<DeliverableType> deliverableTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, deliverableTypes);
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
	public boolean createDeliverableType(HttpServletRequest request) {

		DeliverableType deliverableTypeToBeAdded = new DeliverableType();
		try {
			deliverableTypeToBeAdded = DeliverableTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDeliverableType(deliverableTypeToBeAdded);

	}

	/**
	 * creates a new DeliverableType entry in the ofbiz database
	 * 
	 * @param deliverableTypeToBeAdded
	 *            the DeliverableType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDeliverableType(DeliverableType deliverableTypeToBeAdded) {

		AddDeliverableType com = new AddDeliverableType(deliverableTypeToBeAdded);
		int usedTicketId;

		synchronized (DeliverableTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeliverableTypeAdded.class,
				event -> sendDeliverableTypeChangedMessage(((DeliverableTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDeliverableType(HttpServletRequest request) {

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

		DeliverableType deliverableTypeToBeUpdated = new DeliverableType();

		try {
			deliverableTypeToBeUpdated = DeliverableTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDeliverableType(deliverableTypeToBeUpdated);

	}

	/**
	 * Updates the DeliverableType with the specific Id
	 * 
	 * @param deliverableTypeToBeUpdated the DeliverableType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDeliverableType(DeliverableType deliverableTypeToBeUpdated) {

		UpdateDeliverableType com = new UpdateDeliverableType(deliverableTypeToBeUpdated);

		int usedTicketId;

		synchronized (DeliverableTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeliverableTypeUpdated.class,
				event -> sendDeliverableTypeChangedMessage(((DeliverableTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a DeliverableType from the database
	 * 
	 * @param deliverableTypeId:
	 *            the id of the DeliverableType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedeliverableTypeById(@RequestParam(value = "deliverableTypeId") String deliverableTypeId) {

		DeleteDeliverableType com = new DeleteDeliverableType(deliverableTypeId);

		int usedTicketId;

		synchronized (DeliverableTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DeliverableTypeDeleted.class,
				event -> sendDeliverableTypeChangedMessage(((DeliverableTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDeliverableTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/deliverableType/\" plus one of the following: "
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
