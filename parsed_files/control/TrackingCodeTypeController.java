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
import com.skytala.eCommerce.command.AddTrackingCodeType;
import com.skytala.eCommerce.command.DeleteTrackingCodeType;
import com.skytala.eCommerce.command.UpdateTrackingCodeType;
import com.skytala.eCommerce.entity.TrackingCodeType;
import com.skytala.eCommerce.entity.TrackingCodeTypeMapper;
import com.skytala.eCommerce.event.TrackingCodeTypeAdded;
import com.skytala.eCommerce.event.TrackingCodeTypeDeleted;
import com.skytala.eCommerce.event.TrackingCodeTypeFound;
import com.skytala.eCommerce.event.TrackingCodeTypeUpdated;
import com.skytala.eCommerce.query.FindTrackingCodeTypesBy;

@RestController
@RequestMapping("/api/trackingCodeType")
public class TrackingCodeTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TrackingCodeType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TrackingCodeTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TrackingCodeType
	 * @return a List with the TrackingCodeTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TrackingCodeType> findTrackingCodeTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindTrackingCodeTypesBy query = new FindTrackingCodeTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (TrackingCodeTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeTypeFound.class,
				event -> sendTrackingCodeTypesFoundMessage(((TrackingCodeTypeFound) event).getTrackingCodeTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTrackingCodeTypesFoundMessage(List<TrackingCodeType> trackingCodeTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, trackingCodeTypes);
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
	public boolean createTrackingCodeType(HttpServletRequest request) {

		TrackingCodeType trackingCodeTypeToBeAdded = new TrackingCodeType();
		try {
			trackingCodeTypeToBeAdded = TrackingCodeTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTrackingCodeType(trackingCodeTypeToBeAdded);

	}

	/**
	 * creates a new TrackingCodeType entry in the ofbiz database
	 * 
	 * @param trackingCodeTypeToBeAdded
	 *            the TrackingCodeType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTrackingCodeType(TrackingCodeType trackingCodeTypeToBeAdded) {

		AddTrackingCodeType com = new AddTrackingCodeType(trackingCodeTypeToBeAdded);
		int usedTicketId;

		synchronized (TrackingCodeTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeTypeAdded.class,
				event -> sendTrackingCodeTypeChangedMessage(((TrackingCodeTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTrackingCodeType(HttpServletRequest request) {

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

		TrackingCodeType trackingCodeTypeToBeUpdated = new TrackingCodeType();

		try {
			trackingCodeTypeToBeUpdated = TrackingCodeTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTrackingCodeType(trackingCodeTypeToBeUpdated);

	}

	/**
	 * Updates the TrackingCodeType with the specific Id
	 * 
	 * @param trackingCodeTypeToBeUpdated the TrackingCodeType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTrackingCodeType(TrackingCodeType trackingCodeTypeToBeUpdated) {

		UpdateTrackingCodeType com = new UpdateTrackingCodeType(trackingCodeTypeToBeUpdated);

		int usedTicketId;

		synchronized (TrackingCodeTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeTypeUpdated.class,
				event -> sendTrackingCodeTypeChangedMessage(((TrackingCodeTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TrackingCodeType from the database
	 * 
	 * @param trackingCodeTypeId:
	 *            the id of the TrackingCodeType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetrackingCodeTypeById(@RequestParam(value = "trackingCodeTypeId") String trackingCodeTypeId) {

		DeleteTrackingCodeType com = new DeleteTrackingCodeType(trackingCodeTypeId);

		int usedTicketId;

		synchronized (TrackingCodeTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeTypeDeleted.class,
				event -> sendTrackingCodeTypeChangedMessage(((TrackingCodeTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTrackingCodeTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/trackingCodeType/\" plus one of the following: "
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
