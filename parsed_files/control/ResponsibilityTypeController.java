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
import com.skytala.eCommerce.command.AddResponsibilityType;
import com.skytala.eCommerce.command.DeleteResponsibilityType;
import com.skytala.eCommerce.command.UpdateResponsibilityType;
import com.skytala.eCommerce.entity.ResponsibilityType;
import com.skytala.eCommerce.entity.ResponsibilityTypeMapper;
import com.skytala.eCommerce.event.ResponsibilityTypeAdded;
import com.skytala.eCommerce.event.ResponsibilityTypeDeleted;
import com.skytala.eCommerce.event.ResponsibilityTypeFound;
import com.skytala.eCommerce.event.ResponsibilityTypeUpdated;
import com.skytala.eCommerce.query.FindResponsibilityTypesBy;

@RestController
@RequestMapping("/api/responsibilityType")
public class ResponsibilityTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ResponsibilityType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ResponsibilityTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ResponsibilityType
	 * @return a List with the ResponsibilityTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ResponsibilityType> findResponsibilityTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindResponsibilityTypesBy query = new FindResponsibilityTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ResponsibilityTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ResponsibilityTypeFound.class,
				event -> sendResponsibilityTypesFoundMessage(((ResponsibilityTypeFound) event).getResponsibilityTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendResponsibilityTypesFoundMessage(List<ResponsibilityType> responsibilityTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, responsibilityTypes);
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
	public boolean createResponsibilityType(HttpServletRequest request) {

		ResponsibilityType responsibilityTypeToBeAdded = new ResponsibilityType();
		try {
			responsibilityTypeToBeAdded = ResponsibilityTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createResponsibilityType(responsibilityTypeToBeAdded);

	}

	/**
	 * creates a new ResponsibilityType entry in the ofbiz database
	 * 
	 * @param responsibilityTypeToBeAdded
	 *            the ResponsibilityType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createResponsibilityType(ResponsibilityType responsibilityTypeToBeAdded) {

		AddResponsibilityType com = new AddResponsibilityType(responsibilityTypeToBeAdded);
		int usedTicketId;

		synchronized (ResponsibilityTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ResponsibilityTypeAdded.class,
				event -> sendResponsibilityTypeChangedMessage(((ResponsibilityTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateResponsibilityType(HttpServletRequest request) {

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

		ResponsibilityType responsibilityTypeToBeUpdated = new ResponsibilityType();

		try {
			responsibilityTypeToBeUpdated = ResponsibilityTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateResponsibilityType(responsibilityTypeToBeUpdated);

	}

	/**
	 * Updates the ResponsibilityType with the specific Id
	 * 
	 * @param responsibilityTypeToBeUpdated the ResponsibilityType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateResponsibilityType(ResponsibilityType responsibilityTypeToBeUpdated) {

		UpdateResponsibilityType com = new UpdateResponsibilityType(responsibilityTypeToBeUpdated);

		int usedTicketId;

		synchronized (ResponsibilityTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ResponsibilityTypeUpdated.class,
				event -> sendResponsibilityTypeChangedMessage(((ResponsibilityTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ResponsibilityType from the database
	 * 
	 * @param responsibilityTypeId:
	 *            the id of the ResponsibilityType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteresponsibilityTypeById(@RequestParam(value = "responsibilityTypeId") String responsibilityTypeId) {

		DeleteResponsibilityType com = new DeleteResponsibilityType(responsibilityTypeId);

		int usedTicketId;

		synchronized (ResponsibilityTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ResponsibilityTypeDeleted.class,
				event -> sendResponsibilityTypeChangedMessage(((ResponsibilityTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendResponsibilityTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/responsibilityType/\" plus one of the following: "
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
