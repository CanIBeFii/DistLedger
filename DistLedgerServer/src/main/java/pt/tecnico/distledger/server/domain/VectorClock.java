package pt.tecnico.distledger.server.domain;

import java.util.ArrayList;
import java.util.List;

public class VectorClock {
	private List<Integer> clock;

	public VectorClock(int numOfServers) {
		this.clock = new ArrayList<>();
		for(int i = 0; i < numOfServers; i++) {
			this.clock.add(0);
		}
	}

	public VectorClock(List<Integer> clock) {
		this.clock = new ArrayList<>(clock);
	}

	public List<Integer> getClock() {
		return clock;
	}

	public void setClock(List<Integer> clock) {
		this.clock = new ArrayList<>(clock);
	}

	public void incrementClock(int index) {
		this.clock.set(index, this.clock.get(index) + 1);
	}

	public void mergeClock(List<Integer> otherClock) {
		for(int i = 0; i < this.clock.size(); i++) {
			this.clock.set(i, Math.max(this.clock.get(i), otherClock.get(i)));
		}
	}

	public void updateIndex(int index, int value) {
		this.clock.set(index, (Integer) value);
	}

	public boolean isGreater(VectorClock other) {
		for(int i = 0; i < this.clock.size(); i++) {
			if(this.clock.get(i) < other.getClock().get(i)) {
				return false;
			}
		}
		return true;
	}

	public boolean isSmallerOrEqual(VectorClock other) {
		for(int i = 0; i < this.clock.size(); i++) {
			if(this.clock.get(i) > other.getClock().get(i)) {
				return false;
			}
		}
		return true;
	}
}
