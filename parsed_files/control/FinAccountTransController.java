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
import com.skytala.eCommerce.command.AddFinAccountTrans;
import com.skytala.eCommerce.command.DeleteFinAccountTrans;
import com.skytala.eCommerce.command.UpdateFinAccountTrans;
import com.skytala.eCommerce.entity.FinAccountTrans;
import com.skytala.eCommerce.entity.FinAccountTransMapper;
import com.skytala.eCommerce.event.FinAccountTransAdded;
import com.skytala.eCommerce.event.FinAccountTransDeleted;
import com.skytala.eCommerce.event.FinAccountTransFound;
import com.skytala.eCommerce.event.FinAccountTransUpdated;
import com.skytala.eCommerce.query.FindFinAccountTranssBy;

@RestController
@RequestMapping("/api/finAccountTrans")
public class FinAccountTransController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FinAccountTrans>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FinAccountTransController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FinAccountTrans
	 * @return a List with the FinAccountTranss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FinAccountTrans> findFinAccountTranssBy(@RequestParam Map<String, String> allRequestParams) {

		FindFinAccountTranssBy query = new FindFinAccountTranssBy(allRequestParams);

		int usedTicketId;

		synchronized (FinAccountTransController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTransFound.class,
				event -> sendFinAccountTranssFoundMessage(((FinAccountTransFound) event).getFinAccountTranss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFinAccountTranssFoundMessage(List<FinAccountTrans> finAccountTranss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, finAccountTranss);
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
	public boolean createFinAccountTrans(HttpServletRequest request) {

		FinAccountTrans finAccountTransToBeAdded = new FinAccountTrans();
		try {
			finAccountTransToBeAdded = FinAccountTransMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFinAccountTrans(finAccountTransToBeAdded);

	}

	/**
	 * creates a new FinAccountTrans entry in the ofbiz database
	 * 
	 * @param finAccountTransToBeAdded
	 *            the FinAccountTrans thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFinAccountTrans(FinAccountTrans finAccountTransToBeAdded) {

		AddFinAccountTrans com = new AddFinAccountTrans(finAccountTransToBeAdded);
		int usedTicketId;

		synchronized (FinAccountTransController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTransAdded.class,
				event -> sendFinAccountTransChangedMessage(((FinAccountTransAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFinAccountTrans(HttpServletRequest request) {

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

		FinAccountTrans finAccountTransToBeUpdated = new FinAccountTrans();

		try {
			finAccountTransToBeUpdated = FinAccountTransMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFinAccountTrans(finAccountTransToBeUpdated);

	}

	/**
	 * Updates the FinAccountTrans with the specific Id
	 * 
	 * @param finAccountTransToBeUpdated the FinAccountTrans thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFinAccountTrans(FinAccountTrans finAccountTransToBeUpdated) {

		UpdateFinAccountTrans com = new UpdateFinAccountTrans(finAccountTransToBeUpdated);

		int usedTicketId;

		synchronized (FinAccountTransController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTransUpdated.class,
				event -> sendFinAccountTransChangedMessage(((FinAccountTransUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FinAccountTrans from the database
	 * 
	 * @param finAccountTransId:
	 *            the id of the FinAccountTrans thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefinAccountTransById(@RequestParam(value = "finAccountTransId") String finAccountTransId) {

		DeleteFinAccountTrans com = new DeleteFinAccountTrans(finAccountTransId);

		int usedTicketId;

		synchronized (FinAccountTransController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FinAccountTransDeleted.class,
				event -> sendFinAccountTransChangedMessage(((FinAccountTransDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFinAccountTransChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/finAccountTrans/\" plus one of the following: "
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
