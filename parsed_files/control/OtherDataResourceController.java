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
import com.skytala.eCommerce.command.AddOtherDataResource;
import com.skytala.eCommerce.command.DeleteOtherDataResource;
import com.skytala.eCommerce.command.UpdateOtherDataResource;
import com.skytala.eCommerce.entity.OtherDataResource;
import com.skytala.eCommerce.entity.OtherDataResourceMapper;
import com.skytala.eCommerce.event.OtherDataResourceAdded;
import com.skytala.eCommerce.event.OtherDataResourceDeleted;
import com.skytala.eCommerce.event.OtherDataResourceFound;
import com.skytala.eCommerce.event.OtherDataResourceUpdated;
import com.skytala.eCommerce.query.FindOtherDataResourcesBy;

@RestController
@RequestMapping("/api/otherDataResource")
public class OtherDataResourceController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OtherDataResource>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OtherDataResourceController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OtherDataResource
	 * @return a List with the OtherDataResources
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OtherDataResource> findOtherDataResourcesBy(@RequestParam Map<String, String> allRequestParams) {

		FindOtherDataResourcesBy query = new FindOtherDataResourcesBy(allRequestParams);

		int usedTicketId;

		synchronized (OtherDataResourceController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OtherDataResourceFound.class,
				event -> sendOtherDataResourcesFoundMessage(((OtherDataResourceFound) event).getOtherDataResources(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOtherDataResourcesFoundMessage(List<OtherDataResource> otherDataResources, int usedTicketId) {
		queryReturnVal.put(usedTicketId, otherDataResources);
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
	public boolean createOtherDataResource(HttpServletRequest request) {

		OtherDataResource otherDataResourceToBeAdded = new OtherDataResource();
		try {
			otherDataResourceToBeAdded = OtherDataResourceMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOtherDataResource(otherDataResourceToBeAdded);

	}

	/**
	 * creates a new OtherDataResource entry in the ofbiz database
	 * 
	 * @param otherDataResourceToBeAdded
	 *            the OtherDataResource thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOtherDataResource(OtherDataResource otherDataResourceToBeAdded) {

		AddOtherDataResource com = new AddOtherDataResource(otherDataResourceToBeAdded);
		int usedTicketId;

		synchronized (OtherDataResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OtherDataResourceAdded.class,
				event -> sendOtherDataResourceChangedMessage(((OtherDataResourceAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOtherDataResource(HttpServletRequest request) {

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

		OtherDataResource otherDataResourceToBeUpdated = new OtherDataResource();

		try {
			otherDataResourceToBeUpdated = OtherDataResourceMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOtherDataResource(otherDataResourceToBeUpdated);

	}

	/**
	 * Updates the OtherDataResource with the specific Id
	 * 
	 * @param otherDataResourceToBeUpdated the OtherDataResource thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOtherDataResource(OtherDataResource otherDataResourceToBeUpdated) {

		UpdateOtherDataResource com = new UpdateOtherDataResource(otherDataResourceToBeUpdated);

		int usedTicketId;

		synchronized (OtherDataResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OtherDataResourceUpdated.class,
				event -> sendOtherDataResourceChangedMessage(((OtherDataResourceUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OtherDataResource from the database
	 * 
	 * @param otherDataResourceId:
	 *            the id of the OtherDataResource thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteotherDataResourceById(@RequestParam(value = "otherDataResourceId") String otherDataResourceId) {

		DeleteOtherDataResource com = new DeleteOtherDataResource(otherDataResourceId);

		int usedTicketId;

		synchronized (OtherDataResourceController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OtherDataResourceDeleted.class,
				event -> sendOtherDataResourceChangedMessage(((OtherDataResourceDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOtherDataResourceChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/otherDataResource/\" plus one of the following: "
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
