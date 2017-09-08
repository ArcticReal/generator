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
import com.skytala.eCommerce.command.AddAccommodationSpot;
import com.skytala.eCommerce.command.DeleteAccommodationSpot;
import com.skytala.eCommerce.command.UpdateAccommodationSpot;
import com.skytala.eCommerce.entity.AccommodationSpot;
import com.skytala.eCommerce.entity.AccommodationSpotMapper;
import com.skytala.eCommerce.event.AccommodationSpotAdded;
import com.skytala.eCommerce.event.AccommodationSpotDeleted;
import com.skytala.eCommerce.event.AccommodationSpotFound;
import com.skytala.eCommerce.event.AccommodationSpotUpdated;
import com.skytala.eCommerce.query.FindAccommodationSpotsBy;

@RestController
@RequestMapping("/api/accommodationSpot")
public class AccommodationSpotController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AccommodationSpot>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AccommodationSpotController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AccommodationSpot
	 * @return a List with the AccommodationSpots
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AccommodationSpot> findAccommodationSpotsBy(@RequestParam Map<String, String> allRequestParams) {

		FindAccommodationSpotsBy query = new FindAccommodationSpotsBy(allRequestParams);

		int usedTicketId;

		synchronized (AccommodationSpotController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationSpotFound.class,
				event -> sendAccommodationSpotsFoundMessage(((AccommodationSpotFound) event).getAccommodationSpots(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAccommodationSpotsFoundMessage(List<AccommodationSpot> accommodationSpots, int usedTicketId) {
		queryReturnVal.put(usedTicketId, accommodationSpots);
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
	public boolean createAccommodationSpot(HttpServletRequest request) {

		AccommodationSpot accommodationSpotToBeAdded = new AccommodationSpot();
		try {
			accommodationSpotToBeAdded = AccommodationSpotMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAccommodationSpot(accommodationSpotToBeAdded);

	}

	/**
	 * creates a new AccommodationSpot entry in the ofbiz database
	 * 
	 * @param accommodationSpotToBeAdded
	 *            the AccommodationSpot thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAccommodationSpot(AccommodationSpot accommodationSpotToBeAdded) {

		AddAccommodationSpot com = new AddAccommodationSpot(accommodationSpotToBeAdded);
		int usedTicketId;

		synchronized (AccommodationSpotController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationSpotAdded.class,
				event -> sendAccommodationSpotChangedMessage(((AccommodationSpotAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAccommodationSpot(HttpServletRequest request) {

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

		AccommodationSpot accommodationSpotToBeUpdated = new AccommodationSpot();

		try {
			accommodationSpotToBeUpdated = AccommodationSpotMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAccommodationSpot(accommodationSpotToBeUpdated);

	}

	/**
	 * Updates the AccommodationSpot with the specific Id
	 * 
	 * @param accommodationSpotToBeUpdated the AccommodationSpot thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAccommodationSpot(AccommodationSpot accommodationSpotToBeUpdated) {

		UpdateAccommodationSpot com = new UpdateAccommodationSpot(accommodationSpotToBeUpdated);

		int usedTicketId;

		synchronized (AccommodationSpotController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationSpotUpdated.class,
				event -> sendAccommodationSpotChangedMessage(((AccommodationSpotUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AccommodationSpot from the database
	 * 
	 * @param accommodationSpotId:
	 *            the id of the AccommodationSpot thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteaccommodationSpotById(@RequestParam(value = "accommodationSpotId") String accommodationSpotId) {

		DeleteAccommodationSpot com = new DeleteAccommodationSpot(accommodationSpotId);

		int usedTicketId;

		synchronized (AccommodationSpotController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationSpotDeleted.class,
				event -> sendAccommodationSpotChangedMessage(((AccommodationSpotDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAccommodationSpotChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/accommodationSpot/\" plus one of the following: "
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
