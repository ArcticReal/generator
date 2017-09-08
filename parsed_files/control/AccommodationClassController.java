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
import com.skytala.eCommerce.command.AddAccommodationClass;
import com.skytala.eCommerce.command.DeleteAccommodationClass;
import com.skytala.eCommerce.command.UpdateAccommodationClass;
import com.skytala.eCommerce.entity.AccommodationClass;
import com.skytala.eCommerce.entity.AccommodationClassMapper;
import com.skytala.eCommerce.event.AccommodationClassAdded;
import com.skytala.eCommerce.event.AccommodationClassDeleted;
import com.skytala.eCommerce.event.AccommodationClassFound;
import com.skytala.eCommerce.event.AccommodationClassUpdated;
import com.skytala.eCommerce.query.FindAccommodationClasssBy;

@RestController
@RequestMapping("/api/accommodationClass")
public class AccommodationClassController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AccommodationClass>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AccommodationClassController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AccommodationClass
	 * @return a List with the AccommodationClasss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AccommodationClass> findAccommodationClasssBy(@RequestParam Map<String, String> allRequestParams) {

		FindAccommodationClasssBy query = new FindAccommodationClasssBy(allRequestParams);

		int usedTicketId;

		synchronized (AccommodationClassController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationClassFound.class,
				event -> sendAccommodationClasssFoundMessage(((AccommodationClassFound) event).getAccommodationClasss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAccommodationClasssFoundMessage(List<AccommodationClass> accommodationClasss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, accommodationClasss);
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
	public boolean createAccommodationClass(HttpServletRequest request) {

		AccommodationClass accommodationClassToBeAdded = new AccommodationClass();
		try {
			accommodationClassToBeAdded = AccommodationClassMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAccommodationClass(accommodationClassToBeAdded);

	}

	/**
	 * creates a new AccommodationClass entry in the ofbiz database
	 * 
	 * @param accommodationClassToBeAdded
	 *            the AccommodationClass thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAccommodationClass(AccommodationClass accommodationClassToBeAdded) {

		AddAccommodationClass com = new AddAccommodationClass(accommodationClassToBeAdded);
		int usedTicketId;

		synchronized (AccommodationClassController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationClassAdded.class,
				event -> sendAccommodationClassChangedMessage(((AccommodationClassAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAccommodationClass(HttpServletRequest request) {

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

		AccommodationClass accommodationClassToBeUpdated = new AccommodationClass();

		try {
			accommodationClassToBeUpdated = AccommodationClassMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAccommodationClass(accommodationClassToBeUpdated);

	}

	/**
	 * Updates the AccommodationClass with the specific Id
	 * 
	 * @param accommodationClassToBeUpdated the AccommodationClass thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAccommodationClass(AccommodationClass accommodationClassToBeUpdated) {

		UpdateAccommodationClass com = new UpdateAccommodationClass(accommodationClassToBeUpdated);

		int usedTicketId;

		synchronized (AccommodationClassController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationClassUpdated.class,
				event -> sendAccommodationClassChangedMessage(((AccommodationClassUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AccommodationClass from the database
	 * 
	 * @param accommodationClassId:
	 *            the id of the AccommodationClass thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteaccommodationClassById(@RequestParam(value = "accommodationClassId") String accommodationClassId) {

		DeleteAccommodationClass com = new DeleteAccommodationClass(accommodationClassId);

		int usedTicketId;

		synchronized (AccommodationClassController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AccommodationClassDeleted.class,
				event -> sendAccommodationClassChangedMessage(((AccommodationClassDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAccommodationClassChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/accommodationClass/\" plus one of the following: "
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
