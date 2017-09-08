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
import com.skytala.eCommerce.command.AddFacilityContent;
import com.skytala.eCommerce.command.DeleteFacilityContent;
import com.skytala.eCommerce.command.UpdateFacilityContent;
import com.skytala.eCommerce.entity.FacilityContent;
import com.skytala.eCommerce.entity.FacilityContentMapper;
import com.skytala.eCommerce.event.FacilityContentAdded;
import com.skytala.eCommerce.event.FacilityContentDeleted;
import com.skytala.eCommerce.event.FacilityContentFound;
import com.skytala.eCommerce.event.FacilityContentUpdated;
import com.skytala.eCommerce.query.FindFacilityContentsBy;

@RestController
@RequestMapping("/api/facilityContent")
public class FacilityContentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FacilityContent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FacilityContentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FacilityContent
	 * @return a List with the FacilityContents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FacilityContent> findFacilityContentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFacilityContentsBy query = new FindFacilityContentsBy(allRequestParams);

		int usedTicketId;

		synchronized (FacilityContentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityContentFound.class,
				event -> sendFacilityContentsFoundMessage(((FacilityContentFound) event).getFacilityContents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFacilityContentsFoundMessage(List<FacilityContent> facilityContents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, facilityContents);
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
	public boolean createFacilityContent(HttpServletRequest request) {

		FacilityContent facilityContentToBeAdded = new FacilityContent();
		try {
			facilityContentToBeAdded = FacilityContentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFacilityContent(facilityContentToBeAdded);

	}

	/**
	 * creates a new FacilityContent entry in the ofbiz database
	 * 
	 * @param facilityContentToBeAdded
	 *            the FacilityContent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFacilityContent(FacilityContent facilityContentToBeAdded) {

		AddFacilityContent com = new AddFacilityContent(facilityContentToBeAdded);
		int usedTicketId;

		synchronized (FacilityContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityContentAdded.class,
				event -> sendFacilityContentChangedMessage(((FacilityContentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFacilityContent(HttpServletRequest request) {

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

		FacilityContent facilityContentToBeUpdated = new FacilityContent();

		try {
			facilityContentToBeUpdated = FacilityContentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFacilityContent(facilityContentToBeUpdated);

	}

	/**
	 * Updates the FacilityContent with the specific Id
	 * 
	 * @param facilityContentToBeUpdated the FacilityContent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFacilityContent(FacilityContent facilityContentToBeUpdated) {

		UpdateFacilityContent com = new UpdateFacilityContent(facilityContentToBeUpdated);

		int usedTicketId;

		synchronized (FacilityContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityContentUpdated.class,
				event -> sendFacilityContentChangedMessage(((FacilityContentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FacilityContent from the database
	 * 
	 * @param facilityContentId:
	 *            the id of the FacilityContent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefacilityContentById(@RequestParam(value = "facilityContentId") String facilityContentId) {

		DeleteFacilityContent com = new DeleteFacilityContent(facilityContentId);

		int usedTicketId;

		synchronized (FacilityContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FacilityContentDeleted.class,
				event -> sendFacilityContentChangedMessage(((FacilityContentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFacilityContentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/facilityContent/\" plus one of the following: "
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
