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
import com.skytala.eCommerce.command.AddFixedAsset;
import com.skytala.eCommerce.command.DeleteFixedAsset;
import com.skytala.eCommerce.command.UpdateFixedAsset;
import com.skytala.eCommerce.entity.FixedAsset;
import com.skytala.eCommerce.entity.FixedAssetMapper;
import com.skytala.eCommerce.event.FixedAssetAdded;
import com.skytala.eCommerce.event.FixedAssetDeleted;
import com.skytala.eCommerce.event.FixedAssetFound;
import com.skytala.eCommerce.event.FixedAssetUpdated;
import com.skytala.eCommerce.query.FindFixedAssetsBy;

@RestController
@RequestMapping("/api/fixedAsset")
public class FixedAssetController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<FixedAsset>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public FixedAssetController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a FixedAsset
	 * @return a List with the FixedAssets
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<FixedAsset> findFixedAssetsBy(@RequestParam Map<String, String> allRequestParams) {

		FindFixedAssetsBy query = new FindFixedAssetsBy(allRequestParams);

		int usedTicketId;

		synchronized (FixedAssetController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetFound.class,
				event -> sendFixedAssetsFoundMessage(((FixedAssetFound) event).getFixedAssets(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendFixedAssetsFoundMessage(List<FixedAsset> fixedAssets, int usedTicketId) {
		queryReturnVal.put(usedTicketId, fixedAssets);
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
	public boolean createFixedAsset(HttpServletRequest request) {

		FixedAsset fixedAssetToBeAdded = new FixedAsset();
		try {
			fixedAssetToBeAdded = FixedAssetMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createFixedAsset(fixedAssetToBeAdded);

	}

	/**
	 * creates a new FixedAsset entry in the ofbiz database
	 * 
	 * @param fixedAssetToBeAdded
	 *            the FixedAsset thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createFixedAsset(FixedAsset fixedAssetToBeAdded) {

		AddFixedAsset com = new AddFixedAsset(fixedAssetToBeAdded);
		int usedTicketId;

		synchronized (FixedAssetController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetAdded.class,
				event -> sendFixedAssetChangedMessage(((FixedAssetAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateFixedAsset(HttpServletRequest request) {

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

		FixedAsset fixedAssetToBeUpdated = new FixedAsset();

		try {
			fixedAssetToBeUpdated = FixedAssetMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateFixedAsset(fixedAssetToBeUpdated);

	}

	/**
	 * Updates the FixedAsset with the specific Id
	 * 
	 * @param fixedAssetToBeUpdated the FixedAsset thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateFixedAsset(FixedAsset fixedAssetToBeUpdated) {

		UpdateFixedAsset com = new UpdateFixedAsset(fixedAssetToBeUpdated);

		int usedTicketId;

		synchronized (FixedAssetController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetUpdated.class,
				event -> sendFixedAssetChangedMessage(((FixedAssetUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a FixedAsset from the database
	 * 
	 * @param fixedAssetId:
	 *            the id of the FixedAsset thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deletefixedAssetById(@RequestParam(value = "fixedAssetId") String fixedAssetId) {

		DeleteFixedAsset com = new DeleteFixedAsset(fixedAssetId);

		int usedTicketId;

		synchronized (FixedAssetController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(FixedAssetDeleted.class,
				event -> sendFixedAssetChangedMessage(((FixedAssetDeleted) event).isSuccess(), usedTicketId));

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

	public void sendFixedAssetChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/fixedAsset/\" plus one of the following: "
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
