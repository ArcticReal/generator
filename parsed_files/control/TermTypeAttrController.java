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
import com.skytala.eCommerce.command.AddTermTypeAttr;
import com.skytala.eCommerce.command.DeleteTermTypeAttr;
import com.skytala.eCommerce.command.UpdateTermTypeAttr;
import com.skytala.eCommerce.entity.TermTypeAttr;
import com.skytala.eCommerce.entity.TermTypeAttrMapper;
import com.skytala.eCommerce.event.TermTypeAttrAdded;
import com.skytala.eCommerce.event.TermTypeAttrDeleted;
import com.skytala.eCommerce.event.TermTypeAttrFound;
import com.skytala.eCommerce.event.TermTypeAttrUpdated;
import com.skytala.eCommerce.query.FindTermTypeAttrsBy;

@RestController
@RequestMapping("/api/termTypeAttr")
public class TermTypeAttrController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<TermTypeAttr>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public TermTypeAttrController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a TermTypeAttr
	 * @return a List with the TermTypeAttrs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<TermTypeAttr> findTermTypeAttrsBy(@RequestParam Map<String, String> allRequestParams) {

		FindTermTypeAttrsBy query = new FindTermTypeAttrsBy(allRequestParams);

		int usedTicketId;

		synchronized (TermTypeAttrController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TermTypeAttrFound.class,
				event -> sendTermTypeAttrsFoundMessage(((TermTypeAttrFound) event).getTermTypeAttrs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendTermTypeAttrsFoundMessage(List<TermTypeAttr> termTypeAttrs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, termTypeAttrs);
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
	public boolean createTermTypeAttr(HttpServletRequest request) {

		TermTypeAttr termTypeAttrToBeAdded = new TermTypeAttr();
		try {
			termTypeAttrToBeAdded = TermTypeAttrMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createTermTypeAttr(termTypeAttrToBeAdded);

	}

	/**
	 * creates a new TermTypeAttr entry in the ofbiz database
	 * 
	 * @param termTypeAttrToBeAdded
	 *            the TermTypeAttr thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createTermTypeAttr(TermTypeAttr termTypeAttrToBeAdded) {

		AddTermTypeAttr com = new AddTermTypeAttr(termTypeAttrToBeAdded);
		int usedTicketId;

		synchronized (TermTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TermTypeAttrAdded.class,
				event -> sendTermTypeAttrChangedMessage(((TermTypeAttrAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateTermTypeAttr(HttpServletRequest request) {

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

		TermTypeAttr termTypeAttrToBeUpdated = new TermTypeAttr();

		try {
			termTypeAttrToBeUpdated = TermTypeAttrMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateTermTypeAttr(termTypeAttrToBeUpdated);

	}

	/**
	 * Updates the TermTypeAttr with the specific Id
	 * 
	 * @param termTypeAttrToBeUpdated the TermTypeAttr thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateTermTypeAttr(TermTypeAttr termTypeAttrToBeUpdated) {

		UpdateTermTypeAttr com = new UpdateTermTypeAttr(termTypeAttrToBeUpdated);

		int usedTicketId;

		synchronized (TermTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TermTypeAttrUpdated.class,
				event -> sendTermTypeAttrChangedMessage(((TermTypeAttrUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a TermTypeAttr from the database
	 * 
	 * @param termTypeAttrId:
	 *            the id of the TermTypeAttr thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletetermTypeAttrById(@RequestParam(value = "termTypeAttrId") String termTypeAttrId) {

		DeleteTermTypeAttr com = new DeleteTermTypeAttr(termTypeAttrId);

		int usedTicketId;

		synchronized (TermTypeAttrController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(TermTypeAttrDeleted.class,
				event -> sendTermTypeAttrChangedMessage(((TermTypeAttrDeleted) event).isSuccess(), usedTicketId));

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

	public void sendTermTypeAttrChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/termTypeAttr/\" plus one of the following: "
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
