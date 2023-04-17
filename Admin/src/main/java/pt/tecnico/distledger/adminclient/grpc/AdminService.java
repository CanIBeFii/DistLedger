package pt.tecnico.distledger.adminclient.grpc;

import pt.ulisboa.tecnico.distledger.contract.DistLedgerCommonDefinitions.LedgerState;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.ActivateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.DeactivateRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.GossipRequest;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminDistLedger.getLedgerStateRequest;

import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupRequest;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServer.LookupResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc;
import pt.ulisboa.tecnico.distledger.contract.namingserver.NamingServerServiceGrpc.NamingServerServiceBlockingStub;
import pt.ulisboa.tecnico.distledger.contract.admin.AdminServiceGrpc.AdminServiceBlockingStub;

import java.util.HashMap;
import java.util.List;
public class AdminService {

	private HashMap<String, ManagedChannel> channels = new HashMap<String, ManagedChannel>();
	private HashMap<String, AdminServiceBlockingStub> stubs = new HashMap<String, AdminServiceBlockingStub>();

	private ManagedChannel namingChannel;
	private NamingServerServiceBlockingStub namingStub;

	private static boolean DEBUG_FLAG;

    public AdminService() {
    }

    public AdminService(boolean DEBUG_FLAG) {
		List<String> servers;
		ManagedChannel channel;
		AdminServiceBlockingStub stub;

		AdminService.DEBUG_FLAG = DEBUG_FLAG;
        namingChannel = ManagedChannelBuilder.forTarget("localhost:5001").usePlaintext().build();
	    namingStub = NamingServerServiceGrpc.newBlockingStub(namingChannel);

		servers = lookupServer("A");
		channel = ManagedChannelBuilder.forTarget("localhost:" + servers.get(0)).usePlaintext().build();
		stub = AdminServiceGrpc.newBlockingStub(channel);
		channels.put("A", channel);
		stubs.put("A", stub);
		
		servers = lookupServer("B");
		channel = ManagedChannelBuilder.forTarget("localhost:" + servers.get(0)).usePlaintext().build();
		stub = AdminServiceGrpc.newBlockingStub(channel);
		channels.put("B", channel);
		stubs.put("B", stub);
    }

	private static void debug(String message) {
		if (DEBUG_FLAG)
			System.out.println(message);
	}

    public void shutdownChannel() {
		debug("Shuting down Channel");
		for (ManagedChannel channel : channels.values()) {
		    if (channel != null)
				channel.shutdown();
		}
		if (namingChannel != null)
			namingChannel.shutdown();
    }

    public void activateServer(String server) {
		debug("Activate request sent");
        stubs.get(server).activate(ActivateRequest.newBuilder().build());
    }

    public void deactivateServer(String server) {
		debug("Deactivate request sent");
	    stubs.get(server).deactivate(DeactivateRequest.newBuilder().build());
		debug("Deactivate response received");
	}

    public LedgerState getLedgerState(String server) {
        LedgerState response;
        response = stubs.get(server).getLedgerState(getLedgerStateRequest.newBuilder().build()).getLedgerState();
        return response;
    }

	public void gossip(String server) {
		debug("Gossip request sent");
		stubs.get(server).gossip(GossipRequest.newBuilder().build());
		debug("Gossip response received");
	}

	private List<String> lookupServer(String server) {
		debug("Looking up server " + server + " Address");
		LookupResponse response = namingStub.lookupServer(LookupRequest.newBuilder()
				.setQualifier(server)
				.setService("DistLedger")
				.build());
		debug("Lookup response received");
		return response.getServersList();
	}
}
