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
import com.skytala.eCommerce.command.AddProductStoreSurveyAppl;
import com.skytala.eCommerce.command.DeleteProductStoreSurveyAppl;
import com.skytala.eCommerce.command.UpdateProductStoreSurveyAppl;
import com.skytala.eCommerce.entity.ProductStoreSurveyAppl;
import com.skytala.eCommerce.entity.ProductStoreSurveyApplMapper;
import com.skytala.eCommerce.event.ProductStoreSurveyApplAdded;
import com.skytala.eCommerce.event.ProductStoreSurveyApplDeleted;
import com.skytala.eCommerce.event.ProductStoreSurveyApplFound;
import com.skytala.eCommerce.event.ProductStoreSurveyApplUpdated;
import com.skytala.eCommerce.query.FindProductStoreSurveyApplsBy;

@RestController
@RequestMapping("/api/productStoreSurveyAppl")
public class ProductStoreSurveyApplController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductStoreSurveyAppl>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductStoreSurveyApplController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductStoreSurveyAppl
	 * @return a List with the ProductStoreSurveyAppls
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductStoreSurveyAppl> findProductStoreSurveyApplsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductStoreSurveyApplsBy query = new FindProductStoreSurveyApplsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductStoreSurveyApplController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreSurveyApplFound.class,
				event -> sendProductStoreSurveyApplsFoundMessage(((ProductStoreSurveyApplFound) event).getProductStoreSurveyAppls(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductStoreSurveyApplsFoundMessage(List<ProductStoreSurveyAppl> productStoreSurveyAppls, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productStoreSurveyAppls);
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
	public boolean createProductStoreSurveyAppl(HttpServletRequest request) {

		ProductStoreSurveyAppl productStoreSurveyApplToBeAdded = new ProductStoreSurveyAppl();
		try {
			productStoreSurveyApplToBeAdded = ProductStoreSurveyApplMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductStoreSurveyAppl(productStoreSurveyApplToBeAdded);

	}

	/**
	 * creates a new ProductStoreSurveyAppl entry in the ofbiz database
	 * 
	 * @param productStoreSurveyApplToBeAdded
	 *            the ProductStoreSurveyAppl thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductStoreSurveyAppl(ProductStoreSurveyAppl productStoreSurveyApplToBeAdded) {

		AddProductStoreSurveyAppl com = new AddProductStoreSurveyAppl(productStoreSurveyApplToBeAdded);
		int usedTicketId;

		synchronized (ProductStoreSurveyApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreSurveyApplAdded.class,
				event -> sendProductStoreSurveyApplChangedMessage(((ProductStoreSurveyApplAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductStoreSurveyAppl(HttpServletRequest request) {

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

		ProductStoreSurveyAppl productStoreSurveyApplToBeUpdated = new ProductStoreSurveyAppl();

		try {
			productStoreSurveyApplToBeUpdated = ProductStoreSurveyApplMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductStoreSurveyAppl(productStoreSurveyApplToBeUpdated);

	}

	/**
	 * Updates the ProductStoreSurveyAppl with the specific Id
	 * 
	 * @param productStoreSurveyApplToBeUpdated the ProductStoreSurveyAppl thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductStoreSurveyAppl(ProductStoreSurveyAppl productStoreSurveyApplToBeUpdated) {

		UpdateProductStoreSurveyAppl com = new UpdateProductStoreSurveyAppl(productStoreSurveyApplToBeUpdated);

		int usedTicketId;

		synchronized (ProductStoreSurveyApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreSurveyApplUpdated.class,
				event -> sendProductStoreSurveyApplChangedMessage(((ProductStoreSurveyApplUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductStoreSurveyAppl from the database
	 * 
	 * @param productStoreSurveyApplId:
	 *            the id of the ProductStoreSurveyAppl thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductStoreSurveyApplById(@RequestParam(value = "productStoreSurveyApplId") String productStoreSurveyApplId) {

		DeleteProductStoreSurveyAppl com = new DeleteProductStoreSurveyAppl(productStoreSurveyApplId);

		int usedTicketId;

		synchronized (ProductStoreSurveyApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductStoreSurveyApplDeleted.class,
				event -> sendProductStoreSurveyApplChangedMessage(((ProductStoreSurveyApplDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductStoreSurveyApplChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productStoreSurveyAppl/\" plus one of the following: "
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
