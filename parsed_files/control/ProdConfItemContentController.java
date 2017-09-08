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
import com.skytala.eCommerce.command.AddProdConfItemContent;
import com.skytala.eCommerce.command.DeleteProdConfItemContent;
import com.skytala.eCommerce.command.UpdateProdConfItemContent;
import com.skytala.eCommerce.entity.ProdConfItemContent;
import com.skytala.eCommerce.entity.ProdConfItemContentMapper;
import com.skytala.eCommerce.event.ProdConfItemContentAdded;
import com.skytala.eCommerce.event.ProdConfItemContentDeleted;
import com.skytala.eCommerce.event.ProdConfItemContentFound;
import com.skytala.eCommerce.event.ProdConfItemContentUpdated;
import com.skytala.eCommerce.query.FindProdConfItemContentsBy;

@RestController
@RequestMapping("/api/prodConfItemContent")
public class ProdConfItemContentController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProdConfItemContent>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProdConfItemContentController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProdConfItemContent
	 * @return a List with the ProdConfItemContents
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProdConfItemContent> findProdConfItemContentsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProdConfItemContentsBy query = new FindProdConfItemContentsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProdConfItemContentController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdConfItemContentFound.class,
				event -> sendProdConfItemContentsFoundMessage(((ProdConfItemContentFound) event).getProdConfItemContents(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProdConfItemContentsFoundMessage(List<ProdConfItemContent> prodConfItemContents, int usedTicketId) {
		queryReturnVal.put(usedTicketId, prodConfItemContents);
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
	public boolean createProdConfItemContent(HttpServletRequest request) {

		ProdConfItemContent prodConfItemContentToBeAdded = new ProdConfItemContent();
		try {
			prodConfItemContentToBeAdded = ProdConfItemContentMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProdConfItemContent(prodConfItemContentToBeAdded);

	}

	/**
	 * creates a new ProdConfItemContent entry in the ofbiz database
	 * 
	 * @param prodConfItemContentToBeAdded
	 *            the ProdConfItemContent thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProdConfItemContent(ProdConfItemContent prodConfItemContentToBeAdded) {

		AddProdConfItemContent com = new AddProdConfItemContent(prodConfItemContentToBeAdded);
		int usedTicketId;

		synchronized (ProdConfItemContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdConfItemContentAdded.class,
				event -> sendProdConfItemContentChangedMessage(((ProdConfItemContentAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProdConfItemContent(HttpServletRequest request) {

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

		ProdConfItemContent prodConfItemContentToBeUpdated = new ProdConfItemContent();

		try {
			prodConfItemContentToBeUpdated = ProdConfItemContentMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProdConfItemContent(prodConfItemContentToBeUpdated);

	}

	/**
	 * Updates the ProdConfItemContent with the specific Id
	 * 
	 * @param prodConfItemContentToBeUpdated the ProdConfItemContent thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProdConfItemContent(ProdConfItemContent prodConfItemContentToBeUpdated) {

		UpdateProdConfItemContent com = new UpdateProdConfItemContent(prodConfItemContentToBeUpdated);

		int usedTicketId;

		synchronized (ProdConfItemContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdConfItemContentUpdated.class,
				event -> sendProdConfItemContentChangedMessage(((ProdConfItemContentUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProdConfItemContent from the database
	 * 
	 * @param prodConfItemContentId:
	 *            the id of the ProdConfItemContent thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteprodConfItemContentById(@RequestParam(value = "prodConfItemContentId") String prodConfItemContentId) {

		DeleteProdConfItemContent com = new DeleteProdConfItemContent(prodConfItemContentId);

		int usedTicketId;

		synchronized (ProdConfItemContentController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProdConfItemContentDeleted.class,
				event -> sendProdConfItemContentChangedMessage(((ProdConfItemContentDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProdConfItemContentChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/prodConfItemContent/\" plus one of the following: "
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
