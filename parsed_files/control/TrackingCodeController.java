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
import com.skytala.eCommerce.command.AddTrackingCode;
import com.skytala.eCommerce.command.DeleteTrackingCode;
import com.skytala.eCommerce.command.UpdateTrackingCode;
import com.skytala.eCommerce.entity.TrackingCode;
import com.skytala.eCommerce.entity.TrackingCodeMapper;
import com.skytala.eCommerce.event.TrackingCodeAdded;
import com.skytala.eCommerce.event.TrackingCodeDeleted;
import com.skytala.eCommerce.event.TrackingCodeFound;
import com.skytala.eCommerce.event.TrackingCodeUpdated;
import com.skytala.eCommerce.query.FindTrackingCodesBy;

@RestController
@RequestMapping("/api/trackingCode")
public class TrackingCodeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TrackingCode>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TrackingCodeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TrackingCode
	 * @return a List with the TrackingCodes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TrackingCode> findTrackingCodesBy(@RequestParam Map<String, String> allRequestParams) {

		FindTrackingCodesBy query = new FindTrackingCodesBy(allRequestParams);

		int usedTicketId;

		synchronized (TrackingCodeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeFound.class,
				event -> sendTrackingCodesFoundMessage(((TrackingCodeFound) event).getTrackingCodes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTrackingCodesFoundMessage(List<TrackingCode> trackingCodes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, trackingCodes);
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
	public boolean createTrackingCode(HttpServletRequest request) {

		TrackingCode trackingCodeToBeAdded = new TrackingCode();
		try {
			trackingCodeToBeAdded = TrackingCodeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTrackingCode(trackingCodeToBeAdded);

	}

	/**
	 * creates a new TrackingCode entry in the ofbiz database
	 * 
	 * @param trackingCodeToBeAdded
	 *            the TrackingCode thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTrackingCode(TrackingCode trackingCodeToBeAdded) {

		AddTrackingCode com = new AddTrackingCode(trackingCodeToBeAdded);
		int usedTicketId;

		synchronized (TrackingCodeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeAdded.class,
				event -> sendTrackingCodeChangedMessage(((TrackingCodeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTrackingCode(HttpServletRequest request) {

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

		TrackingCode trackingCodeToBeUpdated = new TrackingCode();

		try {
			trackingCodeToBeUpdated = TrackingCodeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTrackingCode(trackingCodeToBeUpdated);

	}

	/**
	 * Updates the TrackingCode with the specific Id
	 * 
	 * @param trackingCodeToBeUpdated the TrackingCode thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTrackingCode(TrackingCode trackingCodeToBeUpdated) {

		UpdateTrackingCode com = new UpdateTrackingCode(trackingCodeToBeUpdated);

		int usedTicketId;

		synchronized (TrackingCodeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeUpdated.class,
				event -> sendTrackingCodeChangedMessage(((TrackingCodeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TrackingCode from the database
	 * 
	 * @param trackingCodeId:
	 *            the id of the TrackingCode thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetrackingCodeById(@RequestParam(value = "trackingCodeId") String trackingCodeId) {

		DeleteTrackingCode com = new DeleteTrackingCode(trackingCodeId);

		int usedTicketId;

		synchronized (TrackingCodeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TrackingCodeDeleted.class,
				event -> sendTrackingCodeChangedMessage(((TrackingCodeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTrackingCodeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/trackingCode/\" plus one of the following: "
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
