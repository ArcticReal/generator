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
import com.skytala.eCommerce.command.AddFixedAssetGeoPoint;
import com.skytala.eCommerce.command.DeleteFixedAssetGeoPoint;
import com.skytala.eCommerce.command.UpdateFixedAssetGeoPoint;
import com.skytala.eCommerce.entity.FixedAssetGeoPoint;
import com.skytala.eCommerce.entity.FixedAssetGeoPointMapper;
import com.skytala.eCommerce.event.FixedAssetGeoPointAdded;
import com.skytala.eCommerce.event.FixedAssetGeoPointDeleted;
import com.skytala.eCommerce.event.FixedAssetGeoPointFound;
import com.skytala.eCommerce.event.FixedAssetGeoPointUpdated;
import com.skytala.eCommerce.query.FindFixedAssetGeoPointsBy;

@RestController
@RequestMapping("/api/fixedAssetGeoPoint")
public class FixedAssetGeoPointController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetGeoPoint>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetGeoPointController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetGeoPoint
	 * @return a List with the FixedAssetGeoPoints
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetGeoPoint> findFixedAssetGeoPointsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetGeoPointsBy query = new FindFixedAssetGeoPointsBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetGeoPointController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetGeoPointFound.class,
				event -> sendFixedAssetGeoPointsFoundMessage(((FixedAssetGeoPointFound) event).getFixedAssetGeoPoints(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetGeoPointsFoundMessage(List<FixedAssetGeoPoint> fixedAssetGeoPoints, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetGeoPoints);
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
	public boolean createFixedAssetGeoPoint(HttpServletRequest request) {

		FixedAssetGeoPoint fixedAssetGeoPointToBeAdded = new FixedAssetGeoPoint();
		try {
			fixedAssetGeoPointToBeAdded = FixedAssetGeoPointMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetGeoPoint(fixedAssetGeoPointToBeAdded);

	}

	/**
	 * creates a new FixedAssetGeoPoint entry in the ofbiz database
	 * 
	 * @param fixedAssetGeoPointToBeAdded
	 *            the FixedAssetGeoPoint thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetGeoPoint(FixedAssetGeoPoint fixedAssetGeoPointToBeAdded) {

		AddFixedAssetGeoPoint com = new AddFixedAssetGeoPoint(fixedAssetGeoPointToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetGeoPointController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetGeoPointAdded.class,
				event -> sendFixedAssetGeoPointChangedMessage(((FixedAssetGeoPointAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetGeoPoint(HttpServletRequest request) {

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

		FixedAssetGeoPoint fixedAssetGeoPointToBeUpdated = new FixedAssetGeoPoint();

		try {
			fixedAssetGeoPointToBeUpdated = FixedAssetGeoPointMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetGeoPoint(fixedAssetGeoPointToBeUpdated);

	}

	/**
	 * Updates the FixedAssetGeoPoint with the specific Id
	 * 
	 * @param fixedAssetGeoPointToBeUpdated the FixedAssetGeoPoint thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetGeoPoint(FixedAssetGeoPoint fixedAssetGeoPointToBeUpdated) {

		UpdateFixedAssetGeoPoint com = new UpdateFixedAssetGeoPoint(fixedAssetGeoPointToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetGeoPointController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetGeoPointUpdated.class,
				event -> sendFixedAssetGeoPointChangedMessage(((FixedAssetGeoPointUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetGeoPoint from the database
	 * 
	 * @param fixedAssetGeoPointId:
	 *            the id of the FixedAssetGeoPoint thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetGeoPointById(@RequestParam(value = "fixedAssetGeoPointId") String fixedAssetGeoPointId) {

		DeleteFixedAssetGeoPoint com = new DeleteFixedAssetGeoPoint(fixedAssetGeoPointId);

		int usedTicketId;

		synchronized (FixedAssetGeoPointController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetGeoPointDeleted.class,
				event -> sendFixedAssetGeoPointChangedMessage(((FixedAssetGeoPointDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetGeoPointChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetGeoPoint/\" plus one of the following: "
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
