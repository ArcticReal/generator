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
import com.skytala.eCommerce.command.AddFacilityGroupType;
import com.skytala.eCommerce.command.DeleteFacilityGroupType;
import com.skytala.eCommerce.command.UpdateFacilityGroupType;
import com.skytala.eCommerce.entity.FacilityGroupType;
import com.skytala.eCommerce.entity.FacilityGroupTypeMapper;
import com.skytala.eCommerce.event.FacilityGroupTypeAdded;
import com.skytala.eCommerce.event.FacilityGroupTypeDeleted;
import com.skytala.eCommerce.event.FacilityGroupTypeFound;
import com.skytala.eCommerce.event.FacilityGroupTypeUpdated;
import com.skytala.eCommerce.query.FindFacilityGroupTypesBy;

@RestController
@RequestMapping("/api/facilityGroupType")
public class FacilityGroupTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FacilityGroupType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FacilityGroupTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FacilityGroupType
	 * @return a List with the FacilityGroupTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FacilityGroupType> findFacilityGroupTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindFacilityGroupTypesBy query = new FindFacilityGroupTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (FacilityGroupTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupTypeFound.class,
				event -> sendFacilityGroupTypesFoundMessage(((FacilityGroupTypeFound) event).getFacilityGroupTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFacilityGroupTypesFoundMessage(List<FacilityGroupType> facilityGroupTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, facilityGroupTypes);
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
	public boolean createFacilityGroupType(HttpServletRequest request) {

		FacilityGroupType facilityGroupTypeToBeAdded = new FacilityGroupType();
		try {
			facilityGroupTypeToBeAdded = FacilityGroupTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFacilityGroupType(facilityGroupTypeToBeAdded);

	}

	/**
	 * creates a new FacilityGroupType entry in the ofbiz database
	 * 
	 * @param facilityGroupTypeToBeAdded
	 *            the FacilityGroupType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFacilityGroupType(FacilityGroupType facilityGroupTypeToBeAdded) {

		AddFacilityGroupType com = new AddFacilityGroupType(facilityGroupTypeToBeAdded);
		int usedTicketId;

		synchronized (FacilityGroupTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupTypeAdded.class,
				event -> sendFacilityGroupTypeChangedMessage(((FacilityGroupTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFacilityGroupType(HttpServletRequest request) {

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

		FacilityGroupType facilityGroupTypeToBeUpdated = new FacilityGroupType();

		try {
			facilityGroupTypeToBeUpdated = FacilityGroupTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFacilityGroupType(facilityGroupTypeToBeUpdated);

	}

	/**
	 * Updates the FacilityGroupType with the specific Id
	 * 
	 * @param facilityGroupTypeToBeUpdated the FacilityGroupType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFacilityGroupType(FacilityGroupType facilityGroupTypeToBeUpdated) {

		UpdateFacilityGroupType com = new UpdateFacilityGroupType(facilityGroupTypeToBeUpdated);

		int usedTicketId;

		synchronized (FacilityGroupTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupTypeUpdated.class,
				event -> sendFacilityGroupTypeChangedMessage(((FacilityGroupTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FacilityGroupType from the database
	 * 
	 * @param facilityGroupTypeId:
	 *            the id of the FacilityGroupType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefacilityGroupTypeById(@RequestParam(value = "facilityGroupTypeId") String facilityGroupTypeId) {

		DeleteFacilityGroupType com = new DeleteFacilityGroupType(facilityGroupTypeId);

		int usedTicketId;

		synchronized (FacilityGroupTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityGroupTypeDeleted.class,
				event -> sendFacilityGroupTypeChangedMessage(((FacilityGroupTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFacilityGroupTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/facilityGroupType/\" plus one of the following: "
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
