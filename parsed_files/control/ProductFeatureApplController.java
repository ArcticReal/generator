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
import com.skytala.eCommerce.command.AddProductFeatureAppl;
import com.skytala.eCommerce.command.DeleteProductFeatureAppl;
import com.skytala.eCommerce.command.UpdateProductFeatureAppl;
import com.skytala.eCommerce.entity.ProductFeatureAppl;
import com.skytala.eCommerce.entity.ProductFeatureApplMapper;
import com.skytala.eCommerce.event.ProductFeatureApplAdded;
import com.skytala.eCommerce.event.ProductFeatureApplDeleted;
import com.skytala.eCommerce.event.ProductFeatureApplFound;
import com.skytala.eCommerce.event.ProductFeatureApplUpdated;
import com.skytala.eCommerce.query.FindProductFeatureApplsBy;

@RestController
@RequestMapping("/api/productFeatureAppl")
public class ProductFeatureApplController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFeatureAppl>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFeatureApplController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFeatureAppl
	 * @return a List with the ProductFeatureAppls
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFeatureAppl> findProductFeatureApplsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFeatureApplsBy query = new FindProductFeatureApplsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFeatureApplController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureApplFound.class,
				event -> sendProductFeatureApplsFoundMessage(((ProductFeatureApplFound) event).getProductFeatureAppls(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFeatureApplsFoundMessage(List<ProductFeatureAppl> productFeatureAppls, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFeatureAppls);
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
	public boolean createProductFeatureAppl(HttpServletRequest request) {

		ProductFeatureAppl productFeatureApplToBeAdded = new ProductFeatureAppl();
		try {
			productFeatureApplToBeAdded = ProductFeatureApplMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFeatureAppl(productFeatureApplToBeAdded);

	}

	/**
	 * creates a new ProductFeatureAppl entry in the ofbiz database
	 * 
	 * @param productFeatureApplToBeAdded
	 *            the ProductFeatureAppl thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFeatureAppl(ProductFeatureAppl productFeatureApplToBeAdded) {

		AddProductFeatureAppl com = new AddProductFeatureAppl(productFeatureApplToBeAdded);
		int usedTicketId;

		synchronized (ProductFeatureApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureApplAdded.class,
				event -> sendProductFeatureApplChangedMessage(((ProductFeatureApplAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFeatureAppl(HttpServletRequest request) {

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

		ProductFeatureAppl productFeatureApplToBeUpdated = new ProductFeatureAppl();

		try {
			productFeatureApplToBeUpdated = ProductFeatureApplMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFeatureAppl(productFeatureApplToBeUpdated);

	}

	/**
	 * Updates the ProductFeatureAppl with the specific Id
	 * 
	 * @param productFeatureApplToBeUpdated the ProductFeatureAppl thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFeatureAppl(ProductFeatureAppl productFeatureApplToBeUpdated) {

		UpdateProductFeatureAppl com = new UpdateProductFeatureAppl(productFeatureApplToBeUpdated);

		int usedTicketId;

		synchronized (ProductFeatureApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureApplUpdated.class,
				event -> sendProductFeatureApplChangedMessage(((ProductFeatureApplUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFeatureAppl from the database
	 * 
	 * @param productFeatureApplId:
	 *            the id of the ProductFeatureAppl thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFeatureApplById(@RequestParam(value = "productFeatureApplId") String productFeatureApplId) {

		DeleteProductFeatureAppl com = new DeleteProductFeatureAppl(productFeatureApplId);

		int usedTicketId;

		synchronized (ProductFeatureApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureApplDeleted.class,
				event -> sendProductFeatureApplChangedMessage(((ProductFeatureApplDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFeatureApplChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFeatureAppl/\" plus one of the following: "
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
