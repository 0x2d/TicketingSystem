package ticketingsystem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

class Seat {
	//occupied中被置为1的位表示被占用，e.g. 第1站至第2站，将第1位置1；第2站至第4站，将第2位至第3位置1
	long occupied = 0;

	public boolean acquire(int departure, int arrival) {
		long test = 0;
		for (int i = departure; i < arrival; i++) {
			test = test | (1 << (i-1));
		}
		if ((occupied & test) > 0) {
			return false;
		} else {
			occupied = occupied | test;
			return true;
		}
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
		int seatID = -1;
		lock.writeLock().lock();
		try {
			for (int i = 0; i < coachNum; i++) {
				for (int j = 0; j < seatNum; j++) {
					if (seats[i][j].acquire(departure, arrival))
						return i * coachNum + j;
				}
			}
		} finally {
			lock.writeLock().unlock();
		}
		return seatID;
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
		routeNum = routenum == 0 ? 5 : routenum;
		coachNum = coachnum == 0 ? 8 : coachnum;
		seatNum = seatnum == 0 ? 10 : seatnum;
		stationNum = stationnum == 0 ? 100 : stationnum;
		threadNum = threadnum == 0 ? 16 : threadnum;
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
		return 0;
	}

	@Override
	public boolean refundTicket(Ticket ticket) {
		return true;
	}

	@Override
	public boolean buyTicketReplay(Ticket ticket){
		return true;
	}

	@Override
	public boolean refundTicketReplay(Ticket ticket){
		return true;
	}
}
