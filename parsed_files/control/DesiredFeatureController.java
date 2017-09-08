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
import com.skytala.eCommerce.command.AddDesiredFeature;
import com.skytala.eCommerce.command.DeleteDesiredFeature;
import com.skytala.eCommerce.command.UpdateDesiredFeature;
import com.skytala.eCommerce.entity.DesiredFeature;
import com.skytala.eCommerce.entity.DesiredFeatureMapper;
import com.skytala.eCommerce.event.DesiredFeatureAdded;
import com.skytala.eCommerce.event.DesiredFeatureDeleted;
import com.skytala.eCommerce.event.DesiredFeatureFound;
import com.skytala.eCommerce.event.DesiredFeatureUpdated;
import com.skytala.eCommerce.query.FindDesiredFeaturesBy;

@RestController
@RequestMapping("/api/desiredFeature")
public class DesiredFeatureController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<DesiredFeature>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public DesiredFeatureController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a DesiredFeature
	 * @return a List with the DesiredFeatures
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<DesiredFeature> findDesiredFeaturesBy(@RequestParam Map<String, String> allRequestParams) {

		FindDesiredFeaturesBy query = new FindDesiredFeaturesBy(allRequestParams);

		int usedTicketId;

		synchronized (DesiredFeatureController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DesiredFeatureFound.class,
				event -> sendDesiredFeaturesFoundMessage(((DesiredFeatureFound) event).getDesiredFeatures(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendDesiredFeaturesFoundMessage(List<DesiredFeature> desiredFeatures, int usedTicketId) {
		queryReturnVal.put(usedTicketId, desiredFeatures);
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
	public boolean createDesiredFeature(HttpServletRequest request) {

		DesiredFeature desiredFeatureToBeAdded = new DesiredFeature();
		try {
			desiredFeatureToBeAdded = DesiredFeatureMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createDesiredFeature(desiredFeatureToBeAdded);

	}

	/**
	 * creates a new DesiredFeature entry in the ofbiz database
	 * 
	 * @param desiredFeatureToBeAdded
	 *            the DesiredFeature thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createDesiredFeature(DesiredFeature desiredFeatureToBeAdded) {

		AddDesiredFeature com = new AddDesiredFeature(desiredFeatureToBeAdded);
		int usedTicketId;

		synchronized (DesiredFeatureController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DesiredFeatureAdded.class,
				event -> sendDesiredFeatureChangedMessage(((DesiredFeatureAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateDesiredFeature(HttpServletRequest request) {

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

		DesiredFeature desiredFeatureToBeUpdated = new DesiredFeature();

		try {
			desiredFeatureToBeUpdated = DesiredFeatureMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateDesiredFeature(desiredFeatureToBeUpdated);

	}

	/**
	 * Updates the DesiredFeature with the specific Id
	 * 
	 * @param desiredFeatureToBeUpdated the DesiredFeature thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateDesiredFeature(DesiredFeature desiredFeatureToBeUpdated) {

		UpdateDesiredFeature com = new UpdateDesiredFeature(desiredFeatureToBeUpdated);

		int usedTicketId;

		synchronized (DesiredFeatureController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DesiredFeatureUpdated.class,
				event -> sendDesiredFeatureChangedMessage(((DesiredFeatureUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a DesiredFeature from the database
	 * 
	 * @param desiredFeatureId:
	 *            the id of the DesiredFeature thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletedesiredFeatureById(@RequestParam(value = "desiredFeatureId") String desiredFeatureId) {

		DeleteDesiredFeature com = new DeleteDesiredFeature(desiredFeatureId);

		int usedTicketId;

		synchronized (DesiredFeatureController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(DesiredFeatureDeleted.class,
				event -> sendDesiredFeatureChangedMessage(((DesiredFeatureDeleted) event).isSuccess(), usedTicketId));

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

	public void sendDesiredFeatureChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/desiredFeature/\" plus one of the following: "
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
