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
import com.skytala.eCommerce.command.AddFixedAssetIdent;
import com.skytala.eCommerce.command.DeleteFixedAssetIdent;
import com.skytala.eCommerce.command.UpdateFixedAssetIdent;
import com.skytala.eCommerce.entity.FixedAssetIdent;
import com.skytala.eCommerce.entity.FixedAssetIdentMapper;
import com.skytala.eCommerce.event.FixedAssetIdentAdded;
import com.skytala.eCommerce.event.FixedAssetIdentDeleted;
import com.skytala.eCommerce.event.FixedAssetIdentFound;
import com.skytala.eCommerce.event.FixedAssetIdentUpdated;
import com.skytala.eCommerce.query.FindFixedAssetIdentsBy;

@RestController
@RequestMapping("/api/fixedAssetIdent")
public class FixedAssetIdentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetIdent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetIdentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetIdent
	 * @return a List with the FixedAssetIdents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetIdent> findFixedAssetIdentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetIdentsBy query = new FindFixedAssetIdentsBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetIdentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetIdentFound.class,
				event -> sendFixedAssetIdentsFoundMessage(((FixedAssetIdentFound) event).getFixedAssetIdents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetIdentsFoundMessage(List<FixedAssetIdent> fixedAssetIdents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetIdents);
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
	public boolean createFixedAssetIdent(HttpServletRequest request) {

		FixedAssetIdent fixedAssetIdentToBeAdded = new FixedAssetIdent();
		try {
			fixedAssetIdentToBeAdded = FixedAssetIdentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetIdent(fixedAssetIdentToBeAdded);

	}

	/**
	 * creates a new FixedAssetIdent entry in the ofbiz database
	 * 
	 * @param fixedAssetIdentToBeAdded
	 *            the FixedAssetIdent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetIdent(FixedAssetIdent fixedAssetIdentToBeAdded) {

		AddFixedAssetIdent com = new AddFixedAssetIdent(fixedAssetIdentToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetIdentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetIdentAdded.class,
				event -> sendFixedAssetIdentChangedMessage(((FixedAssetIdentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetIdent(HttpServletRequest request) {

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

		FixedAssetIdent fixedAssetIdentToBeUpdated = new FixedAssetIdent();

		try {
			fixedAssetIdentToBeUpdated = FixedAssetIdentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetIdent(fixedAssetIdentToBeUpdated);

	}

	/**
	 * Updates the FixedAssetIdent with the specific Id
	 * 
	 * @param fixedAssetIdentToBeUpdated the FixedAssetIdent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetIdent(FixedAssetIdent fixedAssetIdentToBeUpdated) {

		UpdateFixedAssetIdent com = new UpdateFixedAssetIdent(fixedAssetIdentToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetIdentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetIdentUpdated.class,
				event -> sendFixedAssetIdentChangedMessage(((FixedAssetIdentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetIdent from the database
	 * 
	 * @param fixedAssetIdentId:
	 *            the id of the FixedAssetIdent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetIdentById(@RequestParam(value = "fixedAssetIdentId") String fixedAssetIdentId) {

		DeleteFixedAssetIdent com = new DeleteFixedAssetIdent(fixedAssetIdentId);

		int usedTicketId;

		synchronized (FixedAssetIdentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetIdentDeleted.class,
				event -> sendFixedAssetIdentChangedMessage(((FixedAssetIdentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetIdentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetIdent/\" plus one of the following: "
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
