package pt.tecnico.distledger.namingserver.domain;

import pt.tecnico.distledger.namingserver.domain.ServerEntry;

import pt.tecnico.distledger.namingserver.domain.exception.ServerEntryDuplicate;
import pt.tecnico.distledger.namingserver.domain.exception.ServerEntryDoesntExist;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceEntry {
	private static boolean DEBUG_FLAG ;

	private String serviceName;

	private List<ServerEntry> servers;

	public ServiceEntry() {
	}

	public ServiceEntry(String serviceName) {
		this.serviceName = serviceName;
		servers = new ArrayList<>();
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public List<ServerEntry> getServers() {
		return servers;
	}

	public List<ServerEntry> getSpecificServers(String qualifier) {
		return servers.stream()
			.filter(server -> server.getQualifier().equals(qualifier))
			.collect(Collectors.toList());
	}

	public void addServerEntry(ServerEntry serverEntry) throws ServerEntryDuplicate {
		if (hasAddress(serverEntry.getAddress()) == true)
			throw new ServerEntryDuplicate("ServerEntry with addres: " + serverEntry.getAddress() + " already exists");
		servers.add(serverEntry);
	}

	public void removeServerEntry(String serverEntry) throws ServerEntryDoesntExist {
		if (hasAddress(serverEntry) == false)
			throw new ServerEntryDoesntExist("ServerEntry with addres: " + serverEntry + " does not exist");
		for (ServerEntry server : servers) {
			if (server.getAddress().equals(serverEntry)) {
				servers.remove(server);
				return ;
			}
		}
	}

	public boolean hasAddress(String address) {
		for (ServerEntry serverEntry: servers) {
			if (serverEntry.getAddress().equals(address))
				return true;
		}
		return false;
	}
}
