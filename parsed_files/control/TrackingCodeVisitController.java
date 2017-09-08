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
import com.skytala.eCommerce.command.AddTrackingCodeVisit;
import com.skytala.eCommerce.command.DeleteTrackingCodeVisit;
import com.skytala.eCommerce.command.UpdateTrackingCodeVisit;
import com.skytala.eCommerce.entity.TrackingCodeVisit;
import com.skytala.eCommerce.entity.TrackingCodeVisitMapper;
import com.skytala.eCommerce.event.TrackingCodeVisitAdded;
import com.skytala.eCommerce.event.TrackingCodeVisitDeleted;
import com.skytala.eCommerce.event.TrackingCodeVisitFound;
import com.skytala.eCommerce.event.TrackingCodeVisitUpdated;
import com.skytala.eCommerce.query.FindTrackingCodeVisitsBy;

@RestController
@RequestMapping("/api/trackingCodeVisit")
public class TrackingCodeVisitController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TrackingCodeVisit>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TrackingCodeVisitController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TrackingCodeVisit
	 * @return a List with the TrackingCodeVisits
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TrackingCodeVisit> findTrackingCodeVisitsBy(@RequestParam Map<String, String> allRequestParams) {

		FindTrackingCodeVisitsBy query = new FindTrackingCodeVisitsBy(allRequestParams);

		int usedTicketId;

		synchronized (TrackingCodeVisitController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeVisitFound.class,
				event -> sendTrackingCodeVisitsFoundMessage(((TrackingCodeVisitFound) event).getTrackingCodeVisits(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTrackingCodeVisitsFoundMessage(List<TrackingCodeVisit> trackingCodeVisits, int usedTicketId) {
		queryReturnVal.put(usedTicketId, trackingCodeVisits);
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
	public boolean createTrackingCodeVisit(HttpServletRequest request) {

		TrackingCodeVisit trackingCodeVisitToBeAdded = new TrackingCodeVisit();
		try {
			trackingCodeVisitToBeAdded = TrackingCodeVisitMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTrackingCodeVisit(trackingCodeVisitToBeAdded);

	}

	/**
	 * creates a new TrackingCodeVisit entry in the ofbiz database
	 * 
	 * @param trackingCodeVisitToBeAdded
	 *            the TrackingCodeVisit thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTrackingCodeVisit(TrackingCodeVisit trackingCodeVisitToBeAdded) {

		AddTrackingCodeVisit com = new AddTrackingCodeVisit(trackingCodeVisitToBeAdded);
		int usedTicketId;

		synchronized (TrackingCodeVisitController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeVisitAdded.class,
				event -> sendTrackingCodeVisitChangedMessage(((TrackingCodeVisitAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTrackingCodeVisit(HttpServletRequest request) {

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

		TrackingCodeVisit trackingCodeVisitToBeUpdated = new TrackingCodeVisit();

		try {
			trackingCodeVisitToBeUpdated = TrackingCodeVisitMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTrackingCodeVisit(trackingCodeVisitToBeUpdated);

	}

	/**
	 * Updates the TrackingCodeVisit with the specific Id
	 * 
	 * @param trackingCodeVisitToBeUpdated the TrackingCodeVisit thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTrackingCodeVisit(TrackingCodeVisit trackingCodeVisitToBeUpdated) {

		UpdateTrackingCodeVisit com = new UpdateTrackingCodeVisit(trackingCodeVisitToBeUpdated);

		int usedTicketId;

		synchronized (TrackingCodeVisitController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeVisitUpdated.class,
				event -> sendTrackingCodeVisitChangedMessage(((TrackingCodeVisitUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TrackingCodeVisit from the database
	 * 
	 * @param trackingCodeVisitId:
	 *            the id of the TrackingCodeVisit thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetrackingCodeVisitById(@RequestParam(value = "trackingCodeVisitId") String trackingCodeVisitId) {

		DeleteTrackingCodeVisit com = new DeleteTrackingCodeVisit(trackingCodeVisitId);

		int usedTicketId;

		synchronized (TrackingCodeVisitController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeVisitDeleted.class,
				event -> sendTrackingCodeVisitChangedMessage(((TrackingCodeVisitDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTrackingCodeVisitChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/trackingCodeVisit/\" plus one of the following: "
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
