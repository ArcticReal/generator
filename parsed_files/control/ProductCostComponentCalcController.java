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
import com.skytala.eCommerce.command.AddProductCostComponentCalc;
import com.skytala.eCommerce.command.DeleteProductCostComponentCalc;
import com.skytala.eCommerce.command.UpdateProductCostComponentCalc;
import com.skytala.eCommerce.entity.ProductCostComponentCalc;
import com.skytala.eCommerce.entity.ProductCostComponentCalcMapper;
import com.skytala.eCommerce.event.ProductCostComponentCalcAdded;
import com.skytala.eCommerce.event.ProductCostComponentCalcDeleted;
import com.skytala.eCommerce.event.ProductCostComponentCalcFound;
import com.skytala.eCommerce.event.ProductCostComponentCalcUpdated;
import com.skytala.eCommerce.query.FindProductCostComponentCalcsBy;

@RestController
@RequestMapping("/api/productCostComponentCalc")
public class ProductCostComponentCalcController {

	private static int requestTicketId = 0;
	private static Map<Integer, Boolean> commandReturnVal = new HashMap<>();
	private static Map<Integer, List<ProductCostComponentCalc>> queryReturnVal = new HashMap<>();
	private static Map<String, RequestMethod> validRequests = new HashMap<>();

	public ProductCostComponentCalcController() {

		validRequests.put("find", RequestMethod.GET);
		validRequests.put("add", RequestMethod.POST);
		validRequests.put("update", RequestMethod.PUT);
		validRequests.put("removeById", RequestMethod.DELETE);

	}

	/**
	 * 
	 * @param allRequestParams
	 *            all params by which you want to find a ProductCostComponentCalc
	 * @return a List with the ProductCostComponentCalcs
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/find")
	public List<ProductCostComponentCalc> findProductCostComponentCalcsBy(@RequestParam Map<String, String> allRequestParams) {

		FindProductCostComponentCalcsBy query = new FindProductCostComponentCalcsBy(allRequestParams);

		int usedTicketId;

		synchronized (ProductCostComponentCalcController.class) {
			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCostComponentCalcFound.class,
				event -> sendProductCostComponentCalcsFoundMessage(((ProductCostComponentCalcFound) event).getProductCostComponentCalcs(), usedTicketId));

		query.execute();

		while (!queryReturnVal.containsKey(usedTicketId)) {

		}
		return queryReturnVal.remove(usedTicketId);

	}

	public void sendProductCostComponentCalcsFoundMessage(List<ProductCostComponentCalc> productCostComponentCalcs, int usedTicketId) {
		queryReturnVal.put(usedTicketId, productCostComponentCalcs);
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
	public boolean createProductCostComponentCalc(HttpServletRequest request) {

		ProductCostComponentCalc productCostComponentCalcToBeAdded = new ProductCostComponentCalc();
		try {
			productCostComponentCalcToBeAdded = ProductCostComponentCalcMapper.map(request);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return false;
		}

		return this.createProductCostComponentCalc(productCostComponentCalcToBeAdded);

	}

	/**
	 * creates a new ProductCostComponentCalc entry in the ofbiz database
	 * 
	 * @param productCostComponentCalcToBeAdded
	 *            the ProductCostComponentCalc thats to be added
	 * @return true on success; false on fail
	 */
	public boolean createProductCostComponentCalc(ProductCostComponentCalc productCostComponentCalcToBeAdded) {

		AddProductCostComponentCalc com = new AddProductCostComponentCalc(productCostComponentCalcToBeAdded);
		int usedTicketId;

		synchronized (ProductCostComponentCalcController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCostComponentCalcAdded.class,
				event -> sendProductCostComponentCalcChangedMessage(((ProductCostComponentCalcAdded) event).isSuccess(), usedTicketId));

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
	public boolean updateProductCostComponentCalc(HttpServletRequest request) {

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

		ProductCostComponentCalc productCostComponentCalcToBeUpdated = new ProductCostComponentCalc();

		try {
			productCostComponentCalcToBeUpdated = ProductCostComponentCalcMapper.mapstrstr(dataMap);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return updateProductCostComponentCalc(productCostComponentCalcToBeUpdated);

	}

	/**
	 * Updates the ProductCostComponentCalc with the specific Id
	 * 
	 * @param productCostComponentCalcToBeUpdated the ProductCostComponentCalc thats to be updated
	 * @return true on success, false on fail
	 */
	public boolean updateProductCostComponentCalc(ProductCostComponentCalc productCostComponentCalcToBeUpdated) {

		UpdateProductCostComponentCalc com = new UpdateProductCostComponentCalc(productCostComponentCalcToBeUpdated);

		int usedTicketId;

		synchronized (ProductCostComponentCalcController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCostComponentCalcUpdated.class,
				event -> sendProductCostComponentCalcChangedMessage(((ProductCostComponentCalcUpdated) event).isSuccess(), usedTicketId));

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
	 * removes a ProductCostComponentCalc from the database
	 * 
	 * @param productCostComponentCalcId:
	 *            the id of the ProductCostComponentCalc thats to be removed
	 * 
	 * @return true on success; false on fail
	 * 
	 */
	@RequestMapping(method = RequestMethod.DELETE, value = "/removeById")
	public boolean deleteproductCostComponentCalcById(@RequestParam(value = "productCostComponentCalcId") String productCostComponentCalcId) {

		DeleteProductCostComponentCalc com = new DeleteProductCostComponentCalc(productCostComponentCalcId);

		int usedTicketId;

		synchronized (ProductCostComponentCalcController.class) {

			usedTicketId = requestTicketId;
			requestTicketId++;
		}
		Broker.instance().subscribe(ProductCostComponentCalcDeleted.class,
				event -> sendProductCostComponentCalcChangedMessage(((ProductCostComponentCalcDeleted) event).isSuccess(), usedTicketId));

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

	public void sendProductCostComponentCalcChangedMessage(boolean success, int usedTicketId) {
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

		String returnVal = "Error 404: Page not found! Valid pages are: \"eCommerce/api/productCostComponentCalc/\" plus one of the following: "
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
