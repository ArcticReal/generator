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
import com.skytala.eCommerce.command.AddFixedAssetTypeAttr;
import com.skytala.eCommerce.command.DeleteFixedAssetTypeAttr;
import com.skytala.eCommerce.command.UpdateFixedAssetTypeAttr;
import com.skytala.eCommerce.entity.FixedAssetTypeAttr;
import com.skytala.eCommerce.entity.FixedAssetTypeAttrMapper;
import com.skytala.eCommerce.event.FixedAssetTypeAttrAdded;
import com.skytala.eCommerce.event.FixedAssetTypeAttrDeleted;
import com.skytala.eCommerce.event.FixedAssetTypeAttrFound;
import com.skytala.eCommerce.event.FixedAssetTypeAttrUpdated;
import com.skytala.eCommerce.query.FindFixedAssetTypeAttrsBy;

@RestController
@RequestMapping("/api/fixedAssetTypeAttr")
public class FixedAssetTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetTypeAttr
	 * @return a List with the FixedAssetTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetTypeAttr> findFixedAssetTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetTypeAttrsBy query = new FindFixedAssetTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetTypeAttrFound.class,
				event -> sendFixedAssetTypeAttrsFoundMessage(((FixedAssetTypeAttrFound) event).getFixedAssetTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetTypeAttrsFoundMessage(List<FixedAssetTypeAttr> fixedAssetTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetTypeAttrs);
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
	public boolean createFixedAssetTypeAttr(HttpServletRequest request) {

		FixedAssetTypeAttr fixedAssetTypeAttrToBeAdded = new FixedAssetTypeAttr();
		try {
			fixedAssetTypeAttrToBeAdded = FixedAssetTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetTypeAttr(fixedAssetTypeAttrToBeAdded);

	}

	/**
	 * creates a new FixedAssetTypeAttr entry in the ofbiz database
	 * 
	 * @param fixedAssetTypeAttrToBeAdded
	 *            the FixedAssetTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetTypeAttr(FixedAssetTypeAttr fixedAssetTypeAttrToBeAdded) {

		AddFixedAssetTypeAttr com = new AddFixedAssetTypeAttr(fixedAssetTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetTypeAttrAdded.class,
				event -> sendFixedAssetTypeAttrChangedMessage(((FixedAssetTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetTypeAttr(HttpServletRequest request) {

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

		FixedAssetTypeAttr fixedAssetTypeAttrToBeUpdated = new FixedAssetTypeAttr();

		try {
			fixedAssetTypeAttrToBeUpdated = FixedAssetTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetTypeAttr(fixedAssetTypeAttrToBeUpdated);

	}

	/**
	 * Updates the FixedAssetTypeAttr with the specific Id
	 * 
	 * @param fixedAssetTypeAttrToBeUpdated the FixedAssetTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetTypeAttr(FixedAssetTypeAttr fixedAssetTypeAttrToBeUpdated) {

		UpdateFixedAssetTypeAttr com = new UpdateFixedAssetTypeAttr(fixedAssetTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetTypeAttrUpdated.class,
				event -> sendFixedAssetTypeAttrChangedMessage(((FixedAssetTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetTypeAttr from the database
	 * 
	 * @param fixedAssetTypeAttrId:
	 *            the id of the FixedAssetTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetTypeAttrById(@RequestParam(value = "fixedAssetTypeAttrId") String fixedAssetTypeAttrId) {

		DeleteFixedAssetTypeAttr com = new DeleteFixedAssetTypeAttr(fixedAssetTypeAttrId);

		int usedTicketId;

		synchronized (FixedAssetTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetTypeAttrDeleted.class,
				event -> sendFixedAssetTypeAttrChangedMessage(((FixedAssetTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetTypeAttr/\" plus one of the following: "
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
