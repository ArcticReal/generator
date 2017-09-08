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
import com.skytala.eCommerce.command.AddAccommodationMap;
import com.skytala.eCommerce.command.DeleteAccommodationMap;
import com.skytala.eCommerce.command.UpdateAccommodationMap;
import com.skytala.eCommerce.entity.AccommodationMap;
import com.skytala.eCommerce.entity.AccommodationMapMapper;
import com.skytala.eCommerce.event.AccommodationMapAdded;
import com.skytala.eCommerce.event.AccommodationMapDeleted;
import com.skytala.eCommerce.event.AccommodationMapFound;
import com.skytala.eCommerce.event.AccommodationMapUpdated;
import com.skytala.eCommerce.query.FindAccommodationMapsBy;

@RestController
@RequestMapping("/api/accommodationMap")
public class AccommodationMapController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AccommodationMap>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AccommodationMapController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AccommodationMap
	 * @return a List with the AccommodationMaps
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AccommodationMap> findAccommodationMapsBy(@RequestParam Map<String, String> allRequestParams) {

		FindAccommodationMapsBy query = new FindAccommodationMapsBy(allRequestParams);

		int usedTicketId;

		synchronized (AccommodationMapController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationMapFound.class,
				event -> sendAccommodationMapsFoundMessage(((AccommodationMapFound) event).getAccommodationMaps(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAccommodationMapsFoundMessage(List<AccommodationMap> accommodationMaps, int usedTicketId) {
		queryReturnVal.put(usedTicketId, accommodationMaps);
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
	public boolean createAccommodationMap(HttpServletRequest request) {

		AccommodationMap accommodationMapToBeAdded = new AccommodationMap();
		try {
			accommodationMapToBeAdded = AccommodationMapMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAccommodationMap(accommodationMapToBeAdded);

	}

	/**
	 * creates a new AccommodationMap entry in the ofbiz database
	 * 
	 * @param accommodationMapToBeAdded
	 *            the AccommodationMap thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAccommodationMap(AccommodationMap accommodationMapToBeAdded) {

		AddAccommodationMap com = new AddAccommodationMap(accommodationMapToBeAdded);
		int usedTicketId;

		synchronized (AccommodationMapController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationMapAdded.class,
				event -> sendAccommodationMapChangedMessage(((AccommodationMapAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAccommodationMap(HttpServletRequest request) {

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

		AccommodationMap accommodationMapToBeUpdated = new AccommodationMap();

		try {
			accommodationMapToBeUpdated = AccommodationMapMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAccommodationMap(accommodationMapToBeUpdated);

	}

	/**
	 * Updates the AccommodationMap with the specific Id
	 * 
	 * @param accommodationMapToBeUpdated the AccommodationMap thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAccommodationMap(AccommodationMap accommodationMapToBeUpdated) {

		UpdateAccommodationMap com = new UpdateAccommodationMap(accommodationMapToBeUpdated);

		int usedTicketId;

		synchronized (AccommodationMapController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationMapUpdated.class,
				event -> sendAccommodationMapChangedMessage(((AccommodationMapUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AccommodationMap from the database
	 * 
	 * @param accommodationMapId:
	 *            the id of the AccommodationMap thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteaccommodationMapById(@RequestParam(value = "accommodationMapId") String accommodationMapId) {

		DeleteAccommodationMap com = new DeleteAccommodationMap(accommodationMapId);

		int usedTicketId;

		synchronized (AccommodationMapController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationMapDeleted.class,
				event -> sendAccommodationMapChangedMessage(((AccommodationMapDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAccommodationMapChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/accommodationMap/\" plus one of the following: "
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
