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
import com.skytala.eCommerce.command.AddAcctgTrans;
import com.skytala.eCommerce.command.DeleteAcctgTrans;
import com.skytala.eCommerce.command.UpdateAcctgTrans;
import com.skytala.eCommerce.entity.AcctgTrans;
import com.skytala.eCommerce.entity.AcctgTransMapper;
import com.skytala.eCommerce.event.AcctgTransAdded;
import com.skytala.eCommerce.event.AcctgTransDeleted;
import com.skytala.eCommerce.event.AcctgTransFound;
import com.skytala.eCommerce.event.AcctgTransUpdated;
import com.skytala.eCommerce.query.FindAcctgTranssBy;

@RestController
@RequestMapping("/api/acctgTrans")
public class AcctgTransController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<AcctgTrans>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public AcctgTransController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a AcctgTrans
	 * @return a List with the AcctgTranss
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<AcctgTrans> findAcctgTranssBy(@RequestParam Map<String, String> allRequestParams) {

		FindAcctgTranssBy query = new FindAcctgTranssBy(allRequestParams);

		int usedTicketId;

		synchronized (AcctgTransController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransFound.class,
				event -> sendAcctgTranssFoundMessage(((AcctgTransFound) event).getAcctgTranss(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendAcctgTranssFoundMessage(List<AcctgTrans> acctgTranss, int usedTicketId) {
		queryReturnVal.put(usedTicketId, acctgTranss);
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
	public boolean createAcctgTrans(HttpServletRequest request) {

		AcctgTrans acctgTransToBeAdded = new AcctgTrans();
		try {
			acctgTransToBeAdded = AcctgTransMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createAcctgTrans(acctgTransToBeAdded);

	}

	/**
	 * creates a new AcctgTrans entry in the ofbiz database
	 * 
	 * @param acctgTransToBeAdded
	 *            the AcctgTrans thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createAcctgTrans(AcctgTrans acctgTransToBeAdded) {

		AddAcctgTrans com = new AddAcctgTrans(acctgTransToBeAdded);
		int usedTicketId;

		synchronized (AcctgTransController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransAdded.class,
				event -> sendAcctgTransChangedMessage(((AcctgTransAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateAcctgTrans(HttpServletRequest request) {

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

		AcctgTrans acctgTransToBeUpdated = new AcctgTrans();

		try {
			acctgTransToBeUpdated = AcctgTransMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateAcctgTrans(acctgTransToBeUpdated);

	}

	/**
	 * Updates the AcctgTrans with the specific Id
	 * 
	 * @param acctgTransToBeUpdated the AcctgTrans thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateAcctgTrans(AcctgTrans acctgTransToBeUpdated) {

		UpdateAcctgTrans com = new UpdateAcctgTrans(acctgTransToBeUpdated);

		int usedTicketId;

		synchronized (AcctgTransController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransUpdated.class,
				event -> sendAcctgTransChangedMessage(((AcctgTransUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a AcctgTrans from the database
	 * 
	 * @param acctgTransId:
	 *            the id of the AcctgTrans thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteacctgTransById(@RequestParam(value = "acctgTransId") String acctgTransId) {

		DeleteAcctgTrans com = new DeleteAcctgTrans(acctgTransId);

		int usedTicketId;

		synchronized (AcctgTransController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(AcctgTransDeleted.class,
				event -> sendAcctgTransChangedMessage(((AcctgTransDeleted) event).isSuccess(), usedTicketId));

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

	public void sendAcctgTransChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/acctgTrans/\" plus one of the following: "
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
