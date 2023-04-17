package pt.tecnico.distledger.namingserver.domain;

import pt.tecnico.distledger.namingserver.domain.exception.ServiceEntryAlreadyExists;
import pt.tecnico.distledger.namingserver.domain.exception.ServiceEntryDoesntExist;
import pt.tecnico.distledger.namingserver.domain.exception.ServerEntryDuplicate;
import pt.tecnico.distledger.namingserver.domain.exception.ServerEntryDoesntExist;
import pt.tecnico.distledger.namingserver.domain.exception.QualifierDoesntExist;

import java.util.stream.*;
import java.util.HashMap;
import java.util.ArrayList; 
import java.util.List;

public class NamingServerState {
	
	private HashMap<String, ServiceEntry> services = new HashMap<>();

	private static boolean DEBUG_FLAG;

	public NamingServerState() {
	}

	public NamingServerState(boolean debugFlag) {
		NamingServerState.DEBUG_FLAG = debugFlag;
	}

	public static void debug(String message) {
		if (DEBUG_FLAG)
			System.err.println(message);
	}

	public synchronized HashMap<String, ServiceEntry> getServices() {
		return new HashMap<>(services);
	}

	public synchronized void setServices(HashMap<String, ServiceEntry> services) {
		this.services = services;
	}

	public synchronized void addServiceEntry(ServiceEntry serviceEntry) throws ServiceEntryAlreadyExists {
		if (services.containsKey(serviceEntry.getServiceName()) == true)
			throw new ServiceEntryAlreadyExists("Service " + serviceEntry.getServiceName() + " already exists");
		services.put(serviceEntry.getServiceName(), serviceEntry);
	}

	public synchronized void removeServiceEntry(ServiceEntry serviceEntry) throws ServiceEntryDoesntExist {
		if (services.containsKey(serviceEntry.getServiceName()) == false)
			throw new ServiceEntryDoesntExist("Service " + serviceEntry.getServiceName() + " does not exist");
		services.remove(serviceEntry.getServiceName());
	}

	public synchronized void register(String serviceName, ServerEntry new_entry) throws ServerEntryDuplicate {
		if (services.containsKey(serviceName) == false)
			services.put(serviceName, new ServiceEntry(serviceName));
		for (ServiceEntry service : services.values()) {
			if (service.hasAddress(new_entry.getAddress()) == true)
				throw new ServerEntryDuplicate("ServerEntry with addres: " + new_entry.getAddress() + " already exists");
		}
		services.get(serviceName).addServerEntry(new_entry);
	}

	public synchronized void delete(String serviceName, String address) throws ServiceEntryDoesntExist, ServerEntryDoesntExist {
		if (services.containsKey(serviceName) == false)
			throw new ServiceEntryDoesntExist("ServiceEntry " + serviceName + " does not exist");
		services.get(serviceName).removeServerEntry(address);
	}

	public synchronized List<String> lookup(String serviceName, String qualifier) {
		debug("NamingServer lookup begin");
		if (services.containsKey(serviceName) == false)
			return new ArrayList<>();
		debug("Contains the ServiceName given");
		if (qualifier == null || qualifier.equals(""))
			return new ArrayList<>(services.get(serviceName).getServers()
				.stream()
				.map(serverEntry -> serverEntry.getAddress())
				.collect(Collectors.toList()));
		debug("Qualifier given is not null or empty");
		if (qualifier.equals("A") == false && qualifier.equals("B") == false)
			return new ArrayList<>();
		debug("Qualifier given is valid");
		return new ArrayList<>(services.get(serviceName)
			.getSpecificServers(qualifier)
			.stream()
			.map(serverEntry -> serverEntry.getAddress())
			.collect(Collectors.toList()));
	}
}
