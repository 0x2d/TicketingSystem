package ticketingsystem;

public class Test {

	public static void main(String[] args) throws InterruptedException {
        int routenum = 3;
		int coachnum = 1;
		int seatnum = 1;
		int stationnum = 5;
		int threadnum = 1;
		final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);

		Ticket t1 = tds.buyTicket("ted1", 1, 1, 4);
	    Ticket t2 = tds.buyTicket("ted2", 1, 4, 5);
		Ticket t3 = tds.buyTicket("ted3", 1, 1, 5);
		if (t1 != null) {
			System.out.println("t1 success");
		}
		if (t2 != null) {
			System.out.println("t2 success");
		}
		if (t3 != null) {
			System.out.println("t3 success");
		}
	}
}
