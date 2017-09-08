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
import com.skytala.eCommerce.command.AddAccommodationMapType;
import com.skytala.eCommerce.command.DeleteAccommodationMapType;
import com.skytala.eCommerce.command.UpdateAccommodationMapType;
import com.skytala.eCommerce.entity.AccommodationMapType;
import com.skytala.eCommerce.entity.AccommodationMapTypeMapper;
import com.skytala.eCommerce.event.AccommodationMapTypeAdded;
import com.skytala.eCommerce.event.AccommodationMapTypeDeleted;
import com.skytala.eCommerce.event.AccommodationMapTypeFound;
import com.skytala.eCommerce.event.AccommodationMapTypeUpdated;
import com.skytala.eCommerce.query.FindAccommodationMapTypesBy;

@RestController
@RequestMapping("/api/accommodationMapType")
public class AccommodationMapTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AccommodationMapType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AccommodationMapTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AccommodationMapType
	 * @return a List with the AccommodationMapTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AccommodationMapType> findAccommodationMapTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindAccommodationMapTypesBy query = new FindAccommodationMapTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (AccommodationMapTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationMapTypeFound.class,
				event -> sendAccommodationMapTypesFoundMessage(((AccommodationMapTypeFound) event).getAccommodationMapTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAccommodationMapTypesFoundMessage(List<AccommodationMapType> accommodationMapTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, accommodationMapTypes);
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
	public boolean createAccommodationMapType(HttpServletRequest request) {

		AccommodationMapType accommodationMapTypeToBeAdded = new AccommodationMapType();
		try {
			accommodationMapTypeToBeAdded = AccommodationMapTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAccommodationMapType(accommodationMapTypeToBeAdded);

	}

	/**
	 * creates a new AccommodationMapType entry in the ofbiz database
	 * 
	 * @param accommodationMapTypeToBeAdded
	 *            the AccommodationMapType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAccommodationMapType(AccommodationMapType accommodationMapTypeToBeAdded) {

		AddAccommodationMapType com = new AddAccommodationMapType(accommodationMapTypeToBeAdded);
		int usedTicketId;

		synchronized (AccommodationMapTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationMapTypeAdded.class,
				event -> sendAccommodationMapTypeChangedMessage(((AccommodationMapTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAccommodationMapType(HttpServletRequest request) {

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

		AccommodationMapType accommodationMapTypeToBeUpdated = new AccommodationMapType();

		try {
			accommodationMapTypeToBeUpdated = AccommodationMapTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAccommodationMapType(accommodationMapTypeToBeUpdated);

	}

	/**
	 * Updates the AccommodationMapType with the specific Id
	 * 
	 * @param accommodationMapTypeToBeUpdated the AccommodationMapType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAccommodationMapType(AccommodationMapType accommodationMapTypeToBeUpdated) {

		UpdateAccommodationMapType com = new UpdateAccommodationMapType(accommodationMapTypeToBeUpdated);

		int usedTicketId;

		synchronized (AccommodationMapTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationMapTypeUpdated.class,
				event -> sendAccommodationMapTypeChangedMessage(((AccommodationMapTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AccommodationMapType from the database
	 * 
	 * @param accommodationMapTypeId:
	 *            the id of the AccommodationMapType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteaccommodationMapTypeById(@RequestParam(value = "accommodationMapTypeId") String accommodationMapTypeId) {

		DeleteAccommodationMapType com = new DeleteAccommodationMapType(accommodationMapTypeId);

		int usedTicketId;

		synchronized (AccommodationMapTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationMapTypeDeleted.class,
				event -> sendAccommodationMapTypeChangedMessage(((AccommodationMapTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAccommodationMapTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/accommodationMapType/\" plus one of the following: "
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
