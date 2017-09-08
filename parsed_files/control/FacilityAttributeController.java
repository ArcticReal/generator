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
import com.skytala.eCommerce.command.AddFacilityAttribute;
import com.skytala.eCommerce.command.DeleteFacilityAttribute;
import com.skytala.eCommerce.command.UpdateFacilityAttribute;
import com.skytala.eCommerce.entity.FacilityAttribute;
import com.skytala.eCommerce.entity.FacilityAttributeMapper;
import com.skytala.eCommerce.event.FacilityAttributeAdded;
import com.skytala.eCommerce.event.FacilityAttributeDeleted;
import com.skytala.eCommerce.event.FacilityAttributeFound;
import com.skytala.eCommerce.event.FacilityAttributeUpdated;
import com.skytala.eCommerce.query.FindFacilityAttributesBy;

@RestController
@RequestMapping("/api/facilityAttribute")
public class FacilityAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FacilityAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FacilityAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FacilityAttribute
	 * @return a List with the FacilityAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FacilityAttribute> findFacilityAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindFacilityAttributesBy query = new FindFacilityAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (FacilityAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityAttributeFound.class,
				event -> sendFacilityAttributesFoundMessage(((FacilityAttributeFound) event).getFacilityAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFacilityAttributesFoundMessage(List<FacilityAttribute> facilityAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, facilityAttributes);
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
	public boolean createFacilityAttribute(HttpServletRequest request) {

		FacilityAttribute facilityAttributeToBeAdded = new FacilityAttribute();
		try {
			facilityAttributeToBeAdded = FacilityAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFacilityAttribute(facilityAttributeToBeAdded);

	}

	/**
	 * creates a new FacilityAttribute entry in the ofbiz database
	 * 
	 * @param facilityAttributeToBeAdded
	 *            the FacilityAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFacilityAttribute(FacilityAttribute facilityAttributeToBeAdded) {

		AddFacilityAttribute com = new AddFacilityAttribute(facilityAttributeToBeAdded);
		int usedTicketId;

		synchronized (FacilityAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityAttributeAdded.class,
				event -> sendFacilityAttributeChangedMessage(((FacilityAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFacilityAttribute(HttpServletRequest request) {

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

		FacilityAttribute facilityAttributeToBeUpdated = new FacilityAttribute();

		try {
			facilityAttributeToBeUpdated = FacilityAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFacilityAttribute(facilityAttributeToBeUpdated);

	}

	/**
	 * Updates the FacilityAttribute with the specific Id
	 * 
	 * @param facilityAttributeToBeUpdated the FacilityAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFacilityAttribute(FacilityAttribute facilityAttributeToBeUpdated) {

		UpdateFacilityAttribute com = new UpdateFacilityAttribute(facilityAttributeToBeUpdated);

		int usedTicketId;

		synchronized (FacilityAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityAttributeUpdated.class,
				event -> sendFacilityAttributeChangedMessage(((FacilityAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FacilityAttribute from the database
	 * 
	 * @param facilityAttributeId:
	 *            the id of the FacilityAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefacilityAttributeById(@RequestParam(value = "facilityAttributeId") String facilityAttributeId) {

		DeleteFacilityAttribute com = new DeleteFacilityAttribute(facilityAttributeId);

		int usedTicketId;

		synchronized (FacilityAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityAttributeDeleted.class,
				event -> sendFacilityAttributeChangedMessage(((FacilityAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFacilityAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/facilityAttribute/\" plus one of the following: "
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
