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
import com.skytala.eCommerce.command.AddOldPartyTaxInfo;
import com.skytala.eCommerce.command.DeleteOldPartyTaxInfo;
import com.skytala.eCommerce.command.UpdateOldPartyTaxInfo;
import com.skytala.eCommerce.entity.OldPartyTaxInfo;
import com.skytala.eCommerce.entity.OldPartyTaxInfoMapper;
import com.skytala.eCommerce.event.OldPartyTaxInfoAdded;
import com.skytala.eCommerce.event.OldPartyTaxInfoDeleted;
import com.skytala.eCommerce.event.OldPartyTaxInfoFound;
import com.skytala.eCommerce.event.OldPartyTaxInfoUpdated;
import com.skytala.eCommerce.query.FindOldPartyTaxInfosBy;

@RestController
@RequestMapping("/api/oldPartyTaxInfo")
public class OldPartyTaxInfoController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<OldPartyTaxInfo>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public OldPartyTaxInfoController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a OldPartyTaxInfo
	 * @return a List with the OldPartyTaxInfos
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<OldPartyTaxInfo> findOldPartyTaxInfosBy(@RequestParam Map<String, String> allRequestParams) {

		FindOldPartyTaxInfosBy query = new FindOldPartyTaxInfosBy(allRequestParams);

		int usedTicketId;

		synchronized (OldPartyTaxInfoController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OldPartyTaxInfoFound.class,
				event -> sendOldPartyTaxInfosFoundMessage(((OldPartyTaxInfoFound) event).getOldPartyTaxInfos(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendOldPartyTaxInfosFoundMessage(List<OldPartyTaxInfo> oldPartyTaxInfos, int usedTicketId) {
		queryReturnVal.put(usedTicketId, oldPartyTaxInfos);
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
	public boolean createOldPartyTaxInfo(HttpServletRequest request) {

		OldPartyTaxInfo oldPartyTaxInfoToBeAdded = new OldPartyTaxInfo();
		try {
			oldPartyTaxInfoToBeAdded = OldPartyTaxInfoMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createOldPartyTaxInfo(oldPartyTaxInfoToBeAdded);

	}

	/**
	 * creates a new OldPartyTaxInfo entry in the ofbiz database
	 * 
	 * @param oldPartyTaxInfoToBeAdded
	 *            the OldPartyTaxInfo thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createOldPartyTaxInfo(OldPartyTaxInfo oldPartyTaxInfoToBeAdded) {

		AddOldPartyTaxInfo com = new AddOldPartyTaxInfo(oldPartyTaxInfoToBeAdded);
		int usedTicketId;

		synchronized (OldPartyTaxInfoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OldPartyTaxInfoAdded.class,
				event -> sendOldPartyTaxInfoChangedMessage(((OldPartyTaxInfoAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateOldPartyTaxInfo(HttpServletRequest request) {

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

		OldPartyTaxInfo oldPartyTaxInfoToBeUpdated = new OldPartyTaxInfo();

		try {
			oldPartyTaxInfoToBeUpdated = OldPartyTaxInfoMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateOldPartyTaxInfo(oldPartyTaxInfoToBeUpdated);

	}

	/**
	 * Updates the OldPartyTaxInfo with the specific Id
	 * 
	 * @param oldPartyTaxInfoToBeUpdated the OldPartyTaxInfo thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateOldPartyTaxInfo(OldPartyTaxInfo oldPartyTaxInfoToBeUpdated) {

		UpdateOldPartyTaxInfo com = new UpdateOldPartyTaxInfo(oldPartyTaxInfoToBeUpdated);

		int usedTicketId;

		synchronized (OldPartyTaxInfoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OldPartyTaxInfoUpdated.class,
				event -> sendOldPartyTaxInfoChangedMessage(((OldPartyTaxInfoUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a OldPartyTaxInfo from the database
	 * 
	 * @param oldPartyTaxInfoId:
	 *            the id of the OldPartyTaxInfo thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteoldPartyTaxInfoById(@RequestParam(value = "oldPartyTaxInfoId") String oldPartyTaxInfoId) {

		DeleteOldPartyTaxInfo com = new DeleteOldPartyTaxInfo(oldPartyTaxInfoId);

		int usedTicketId;

		synchronized (OldPartyTaxInfoController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(OldPartyTaxInfoDeleted.class,
				event -> sendOldPartyTaxInfoChangedMessage(((OldPartyTaxInfoDeleted) event).isSuccess(), usedTicketId));

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

	public void sendOldPartyTaxInfoChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/oldPartyTaxInfo/\" plus one of the following: "
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
