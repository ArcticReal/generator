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
import com.skytala.eCommerce.command.AddTrackingCodeOrderReturn;
import com.skytala.eCommerce.command.DeleteTrackingCodeOrderReturn;
import com.skytala.eCommerce.command.UpdateTrackingCodeOrderReturn;
import com.skytala.eCommerce.entity.TrackingCodeOrderReturn;
import com.skytala.eCommerce.entity.TrackingCodeOrderReturnMapper;
import com.skytala.eCommerce.event.TrackingCodeOrderReturnAdded;
import com.skytala.eCommerce.event.TrackingCodeOrderReturnDeleted;
import com.skytala.eCommerce.event.TrackingCodeOrderReturnFound;
import com.skytala.eCommerce.event.TrackingCodeOrderReturnUpdated;
import com.skytala.eCommerce.query.FindTrackingCodeOrderReturnsBy;

@RestController
@RequestMapping("/api/trackingCodeOrderReturn")
public class TrackingCodeOrderReturnController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TrackingCodeOrderReturn>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TrackingCodeOrderReturnController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TrackingCodeOrderReturn
	 * @return a List with the TrackingCodeOrderReturns
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TrackingCodeOrderReturn> findTrackingCodeOrderReturnsBy(@RequestParam Map<String, String> allRequestParams) {

		FindTrackingCodeOrderReturnsBy query = new FindTrackingCodeOrderReturnsBy(allRequestParams);

		int usedTicketId;

		synchronized (TrackingCodeOrderReturnController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeOrderReturnFound.class,
				event -> sendTrackingCodeOrderReturnsFoundMessage(((TrackingCodeOrderReturnFound) event).getTrackingCodeOrderReturns(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTrackingCodeOrderReturnsFoundMessage(List<TrackingCodeOrderReturn> trackingCodeOrderReturns, int usedTicketId) {
		queryReturnVal.put(usedTicketId, trackingCodeOrderReturns);
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
	public boolean createTrackingCodeOrderReturn(HttpServletRequest request) {

		TrackingCodeOrderReturn trackingCodeOrderReturnToBeAdded = new TrackingCodeOrderReturn();
		try {
			trackingCodeOrderReturnToBeAdded = TrackingCodeOrderReturnMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTrackingCodeOrderReturn(trackingCodeOrderReturnToBeAdded);

	}

	/**
	 * creates a new TrackingCodeOrderReturn entry in the ofbiz database
	 * 
	 * @param trackingCodeOrderReturnToBeAdded
	 *            the TrackingCodeOrderReturn thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTrackingCodeOrderReturn(TrackingCodeOrderReturn trackingCodeOrderReturnToBeAdded) {

		AddTrackingCodeOrderReturn com = new AddTrackingCodeOrderReturn(trackingCodeOrderReturnToBeAdded);
		int usedTicketId;

		synchronized (TrackingCodeOrderReturnController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeOrderReturnAdded.class,
				event -> sendTrackingCodeOrderReturnChangedMessage(((TrackingCodeOrderReturnAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTrackingCodeOrderReturn(HttpServletRequest request) {

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

		TrackingCodeOrderReturn trackingCodeOrderReturnToBeUpdated = new TrackingCodeOrderReturn();

		try {
			trackingCodeOrderReturnToBeUpdated = TrackingCodeOrderReturnMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTrackingCodeOrderReturn(trackingCodeOrderReturnToBeUpdated);

	}

	/**
	 * Updates the TrackingCodeOrderReturn with the specific Id
	 * 
	 * @param trackingCodeOrderReturnToBeUpdated the TrackingCodeOrderReturn thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTrackingCodeOrderReturn(TrackingCodeOrderReturn trackingCodeOrderReturnToBeUpdated) {

		UpdateTrackingCodeOrderReturn com = new UpdateTrackingCodeOrderReturn(trackingCodeOrderReturnToBeUpdated);

		int usedTicketId;

		synchronized (TrackingCodeOrderReturnController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeOrderReturnUpdated.class,
				event -> sendTrackingCodeOrderReturnChangedMessage(((TrackingCodeOrderReturnUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TrackingCodeOrderReturn from the database
	 * 
	 * @param trackingCodeOrderReturnId:
	 *            the id of the TrackingCodeOrderReturn thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetrackingCodeOrderReturnById(@RequestParam(value = "trackingCodeOrderReturnId") String trackingCodeOrderReturnId) {

		DeleteTrackingCodeOrderReturn com = new DeleteTrackingCodeOrderReturn(trackingCodeOrderReturnId);

		int usedTicketId;

		synchronized (TrackingCodeOrderReturnController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeOrderReturnDeleted.class,
				event -> sendTrackingCodeOrderReturnChangedMessage(((TrackingCodeOrderReturnDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTrackingCodeOrderReturnChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/trackingCodeOrderReturn/\" plus one of the following: "
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
