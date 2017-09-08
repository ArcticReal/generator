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
import com.skytala.eCommerce.command.AddProdConfItemContentType;
import com.skytala.eCommerce.command.DeleteProdConfItemContentType;
import com.skytala.eCommerce.command.UpdateProdConfItemContentType;
import com.skytala.eCommerce.entity.ProdConfItemContentType;
import com.skytala.eCommerce.entity.ProdConfItemContentTypeMapper;
import com.skytala.eCommerce.event.ProdConfItemContentTypeAdded;
import com.skytala.eCommerce.event.ProdConfItemContentTypeDeleted;
import com.skytala.eCommerce.event.ProdConfItemContentTypeFound;
import com.skytala.eCommerce.event.ProdConfItemContentTypeUpdated;
import com.skytala.eCommerce.query.FindProdConfItemContentTypesBy;

@RestController
@RequestMapping("/api/prodConfItemContentType")
public class ProdConfItemContentTypeController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProdConfItemContentType>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProdConfItemContentTypeController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProdConfItemContentType
	 * @return a List with the ProdConfItemContentTypes
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProdConfItemContentType> findProdConfItemContentTypesBy(@RequestParam Map<String, String> allRequestParams) {

		FindProdConfItemContentTypesBy query = new FindProdConfItemContentTypesBy(allRequestParams);

		int usedTicketId;

		synchronized (ProdConfItemContentTypeController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdConfItemContentTypeFound.class,
				event -> sendProdConfItemContentTypesFoundMessage(((ProdConfItemContentTypeFound) event).getProdConfItemContentTypes(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProdConfItemContentTypesFoundMessage(List<ProdConfItemContentType> prodConfItemContentTypes, int usedTicketId) {
		queryReturnVal.put(usedTicketId, prodConfItemContentTypes);
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
	public boolean createProdConfItemContentType(HttpServletRequest request) {

		ProdConfItemContentType prodConfItemContentTypeToBeAdded = new ProdConfItemContentType();
		try {
			prodConfItemContentTypeToBeAdded = ProdConfItemContentTypeMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProdConfItemContentType(prodConfItemContentTypeToBeAdded);

	}

	/**
	 * creates a new ProdConfItemContentType entry in the ofbiz database
	 * 
	 * @param prodConfItemContentTypeToBeAdded
	 *            the ProdConfItemContentType thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProdConfItemContentType(ProdConfItemContentType prodConfItemContentTypeToBeAdded) {

		AddProdConfItemContentType com = new AddProdConfItemContentType(prodConfItemContentTypeToBeAdded);
		int usedTicketId;

		synchronized (ProdConfItemContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdConfItemContentTypeAdded.class,
				event -> sendProdConfItemContentTypeChangedMessage(((ProdConfItemContentTypeAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProdConfItemContentType(HttpServletRequest request) {

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

		ProdConfItemContentType prodConfItemContentTypeToBeUpdated = new ProdConfItemContentType();

		try {
			prodConfItemContentTypeToBeUpdated = ProdConfItemContentTypeMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProdConfItemContentType(prodConfItemContentTypeToBeUpdated);

	}

	/**
	 * Updates the ProdConfItemContentType with the specific Id
	 * 
	 * @param prodConfItemContentTypeToBeUpdated the ProdConfItemContentType thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProdConfItemContentType(ProdConfItemContentType prodConfItemContentTypeToBeUpdated) {

		UpdateProdConfItemContentType com = new UpdateProdConfItemContentType(prodConfItemContentTypeToBeUpdated);

		int usedTicketId;

		synchronized (ProdConfItemContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdConfItemContentTypeUpdated.class,
				event -> sendProdConfItemContentTypeChangedMessage(((ProdConfItemContentTypeUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProdConfItemContentType from the database
	 * 
	 * @param prodConfItemContentTypeId:
	 *            the id of the ProdConfItemContentType thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteprodConfItemContentTypeById(@RequestParam(value = "prodConfItemContentTypeId") String prodConfItemContentTypeId) {

		DeleteProdConfItemContentType com = new DeleteProdConfItemContentType(prodConfItemContentTypeId);

		int usedTicketId;

		synchronized (ProdConfItemContentTypeController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdConfItemContentTypeDeleted.class,
				event -> sendProdConfItemContentTypeChangedMessage(((ProdConfItemContentTypeDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProdConfItemContentTypeChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/prodConfItemContentType/\" plus one of the following: "
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
