package ticketingsystem;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Seat {
	//occupied中被置为1的位表示被占用，e.g. 第1站至第2站，将第1位置1；第2站至第4站，将第2位至第3位置1
	int occupied = 0;

	public boolean acquire(int test) {
		if ((occupied & test) > 0) {
			return false;
		} else {
			occupied = occupied | test;
			return true;
		}
	}

	public boolean inquire(int test) {
		if ((occupied & test) > 0) {
			return false;
		} else {
			return true;
		}
	}

	public void release(int test) {
		occupied = occupied ^ test;
	}
}

class Route {
	final ReadWriteLock lock;
	int coachNum;
	int seatNum;
	Seat[][] seats;

	public Route(int coachnum, int seatnum) {
		coachNum = coachnum;
		seatNum = seatnum;
		lock = new ReentrantReadWriteLock();
		seats = new Seat[coachNum][seatNum];
		for (int i = 0; i < coachNum; i++) {
			for (int j = 0; j < seatNum; j++) {
				seats[i][j] = new Seat();
			}
		}
	}

	public int getSeat(int departure, int arrival) {
		int test = getTest(departure, arrival);
		lock.writeLock().lock();
		try {
			for (int i = 0; i < coachNum; i++) {
				for (int j = 0; j < seatNum; j++) {
					if (seats[i][j].acquire(test))
						return i * seatNum + j;
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
		return -1;
	}

	public int countSeat(int departure, int arrival) {
		int freeSeat = 0;
		int test = getTest(departure, arrival);
		lock.readLock().lock();
		try {
			for (int i = 0; i < coachNum; i++) {
				for (int j = 0; j < seatNum; j++) {
					if (seats[i][j].inquire(test))
						freeSeat++;
				}
			}
		} finally {
			lock.readLock().unlock();
		}
		return freeSeat;
	}

	public void putSeat(int coach, int seat, int departure, int arrival) {
		int test = getTest(departure, arrival);
		lock.writeLock().lock();
		try {
			seats[coach-1][seat-1].release(test);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public int getTest(int departure, int arrival){
		int test = 0;
		int de, ar;
		if (arrival >= departure) {
			de = departure;
			ar = arrival;
		} else {
			de = arrival;
			ar = departure;
		}
		for (int i = de; i < ar; i++) {
			test = test | (1 << (i-1));
		}
		return test;
	}
}

public class TicketingDS implements TicketingSystem {
	int routeNum;
	int coachNum;
	int seatNum;
	int stationNum;
	int threadNum;
	Route[] routes;
	ConcurrentHashMap<Long, Ticket> tickets;
	AtomicInteger tidCounter;

	public TicketingDS(int routenum, int coachnum, int seatnum, int stationnum, int threadnum) {
		routeNum = routenum;
		coachNum = coachnum;
		seatNum = seatnum;
		stationNum = stationnum;
		threadNum = threadnum;
		routes = new Route[routeNum];
		for (int i = 0; i < routeNum; i++) {
			routes[i] = new Route(coachNum, seatNum);
		}
		tickets = new ConcurrentHashMap<Long, Ticket>();
		tidCounter = new AtomicInteger(0);
	}

	@Override
	public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
		int seatID = routes[route-1].getSeat(departure, arrival);
		if (seatID == -1) {
			return null;
		} else {
			Ticket newTicket = new Ticket();
			newTicket.passenger = passenger;
			newTicket.route = route;
			newTicket.departure = departure;
			newTicket.arrival = arrival;
			newTicket.coach = (seatID / seatNum) + 1;
			newTicket.seat = (seatID % seatNum) + 1;
			newTicket.tid = tidCounter.getAndIncrement();
			tickets.put(newTicket.tid, newTicket);
			return newTicket;
		}
	}

	@Override
	public int inquiry(int route, int departure, int arrival) {
		return routes[route-1].countSeat(departure, arrival);
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		if (ticket == null || !tickets.containsKey(ticket.tid)) {
			return false;
		}
		tickets.remove(ticket.tid);
		routes[ticket.route-1].putSeat(ticket.coach, ticket.seat, ticket.departure, ticket.arrival);
		return true;
	}
}
