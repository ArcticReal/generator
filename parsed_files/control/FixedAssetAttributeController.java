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
import com.skytala.eCommerce.command.AddFixedAssetAttribute;
import com.skytala.eCommerce.command.DeleteFixedAssetAttribute;
import com.skytala.eCommerce.command.UpdateFixedAssetAttribute;
import com.skytala.eCommerce.entity.FixedAssetAttribute;
import com.skytala.eCommerce.entity.FixedAssetAttributeMapper;
import com.skytala.eCommerce.event.FixedAssetAttributeAdded;
import com.skytala.eCommerce.event.FixedAssetAttributeDeleted;
import com.skytala.eCommerce.event.FixedAssetAttributeFound;
import com.skytala.eCommerce.event.FixedAssetAttributeUpdated;
import com.skytala.eCommerce.query.FindFixedAssetAttributesBy;

@RestController
@RequestMapping("/api/fixedAssetAttribute")
public class FixedAssetAttributeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAssetAttribute>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetAttributeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAssetAttribute
	 * @return a List with the FixedAssetAttributes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAssetAttribute> findFixedAssetAttributesBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetAttributesBy query = new FindFixedAssetAttributesBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetAttributeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetAttributeFound.class,
				event -> sendFixedAssetAttributesFoundMessage(((FixedAssetAttributeFound) event).getFixedAssetAttributes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetAttributesFoundMessage(List<FixedAssetAttribute> fixedAssetAttributes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssetAttributes);
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
	public boolean createFixedAssetAttribute(HttpServletRequest request) {

		FixedAssetAttribute fixedAssetAttributeToBeAdded = new FixedAssetAttribute();
		try {
			fixedAssetAttributeToBeAdded = FixedAssetAttributeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAssetAttribute(fixedAssetAttributeToBeAdded);

	}

	/**
	 * creates a new FixedAssetAttribute entry in the ofbiz database
	 * 
	 * @param fixedAssetAttributeToBeAdded
	 *            the FixedAssetAttribute thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAssetAttribute(FixedAssetAttribute fixedAssetAttributeToBeAdded) {

		AddFixedAssetAttribute com = new AddFixedAssetAttribute(fixedAssetAttributeToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetAttributeAdded.class,
				event -> sendFixedAssetAttributeChangedMessage(((FixedAssetAttributeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAssetAttribute(HttpServletRequest request) {

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

		FixedAssetAttribute fixedAssetAttributeToBeUpdated = new FixedAssetAttribute();

		try {
			fixedAssetAttributeToBeUpdated = FixedAssetAttributeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAssetAttribute(fixedAssetAttributeToBeUpdated);

	}

	/**
	 * Updates the FixedAssetAttribute with the specific Id
	 * 
	 * @param fixedAssetAttributeToBeUpdated the FixedAssetAttribute thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAssetAttribute(FixedAssetAttribute fixedAssetAttributeToBeUpdated) {

		UpdateFixedAssetAttribute com = new UpdateFixedAssetAttribute(fixedAssetAttributeToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetAttributeUpdated.class,
				event -> sendFixedAssetAttributeChangedMessage(((FixedAssetAttributeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAssetAttribute from the database
	 * 
	 * @param fixedAssetAttributeId:
	 *            the id of the FixedAssetAttribute thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetAttributeById(@RequestParam(value = "fixedAssetAttributeId") String fixedAssetAttributeId) {

		DeleteFixedAssetAttribute com = new DeleteFixedAssetAttribute(fixedAssetAttributeId);

		int usedTicketId;

		synchronized (FixedAssetAttributeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetAttributeDeleted.class,
				event -> sendFixedAssetAttributeChangedMessage(((FixedAssetAttributeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetAttributeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAssetAttribute/\" plus one of the following: "
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
