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
import com.skytala.eCommerce.command.AddProductFeatureGroupAppl;
import com.skytala.eCommerce.command.DeleteProductFeatureGroupAppl;
import com.skytala.eCommerce.command.UpdateProductFeatureGroupAppl;
import com.skytala.eCommerce.entity.ProductFeatureGroupAppl;
import com.skytala.eCommerce.entity.ProductFeatureGroupApplMapper;
import com.skytala.eCommerce.event.ProductFeatureGroupApplAdded;
import com.skytala.eCommerce.event.ProductFeatureGroupApplDeleted;
import com.skytala.eCommerce.event.ProductFeatureGroupApplFound;
import com.skytala.eCommerce.event.ProductFeatureGroupApplUpdated;
import com.skytala.eCommerce.query.FindProductFeatureGroupApplsBy;

@RestController
@RequestMapping("/api/productFeatureGroupAppl")
public class ProductFeatureGroupApplController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductFeatureGroupAppl>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductFeatureGroupApplController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductFeatureGroupAppl
	 * @return a List with the ProductFeatureGroupAppls
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductFeatureGroupAppl> findProductFeatureGroupApplsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductFeatureGroupApplsBy query = new FindProductFeatureGroupApplsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductFeatureGroupApplController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureGroupApplFound.class,
				event -> sendProductFeatureGroupApplsFoundMessage(((ProductFeatureGroupApplFound) event).getProductFeatureGroupAppls(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductFeatureGroupApplsFoundMessage(List<ProductFeatureGroupAppl> productFeatureGroupAppls, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productFeatureGroupAppls);
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
	public boolean createProductFeatureGroupAppl(HttpServletRequest request) {

		ProductFeatureGroupAppl productFeatureGroupApplToBeAdded = new ProductFeatureGroupAppl();
		try {
			productFeatureGroupApplToBeAdded = ProductFeatureGroupApplMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductFeatureGroupAppl(productFeatureGroupApplToBeAdded);

	}

	/**
	 * creates a new ProductFeatureGroupAppl entry in the ofbiz database
	 * 
	 * @param productFeatureGroupApplToBeAdded
	 *            the ProductFeatureGroupAppl thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductFeatureGroupAppl(ProductFeatureGroupAppl productFeatureGroupApplToBeAdded) {

		AddProductFeatureGroupAppl com = new AddProductFeatureGroupAppl(productFeatureGroupApplToBeAdded);
		int usedTicketId;

		synchronized (ProductFeatureGroupApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureGroupApplAdded.class,
				event -> sendProductFeatureGroupApplChangedMessage(((ProductFeatureGroupApplAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductFeatureGroupAppl(HttpServletRequest request) {

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

		ProductFeatureGroupAppl productFeatureGroupApplToBeUpdated = new ProductFeatureGroupAppl();

		try {
			productFeatureGroupApplToBeUpdated = ProductFeatureGroupApplMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductFeatureGroupAppl(productFeatureGroupApplToBeUpdated);

	}

	/**
	 * Updates the ProductFeatureGroupAppl with the specific Id
	 * 
	 * @param productFeatureGroupApplToBeUpdated the ProductFeatureGroupAppl thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductFeatureGroupAppl(ProductFeatureGroupAppl productFeatureGroupApplToBeUpdated) {

		UpdateProductFeatureGroupAppl com = new UpdateProductFeatureGroupAppl(productFeatureGroupApplToBeUpdated);

		int usedTicketId;

		synchronized (ProductFeatureGroupApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureGroupApplUpdated.class,
				event -> sendProductFeatureGroupApplChangedMessage(((ProductFeatureGroupApplUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductFeatureGroupAppl from the database
	 * 
	 * @param productFeatureGroupApplId:
	 *            the id of the ProductFeatureGroupAppl thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductFeatureGroupApplById(@RequestParam(value = "productFeatureGroupApplId") String productFeatureGroupApplId) {

		DeleteProductFeatureGroupAppl com = new DeleteProductFeatureGroupAppl(productFeatureGroupApplId);

		int usedTicketId;

		synchronized (ProductFeatureGroupApplController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductFeatureGroupApplDeleted.class,
				event -> sendProductFeatureGroupApplChangedMessage(((ProductFeatureGroupApplDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductFeatureGroupApplChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productFeatureGroupAppl/\" plus one of the following: "
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
