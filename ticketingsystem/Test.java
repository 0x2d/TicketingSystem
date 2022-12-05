package ticketingsystem;

public class Test {

	public static void main(String[] args) throws InterruptedException {
		int[] threadnums = {1};
		int testnum = 1;
        int routenum = 3;
		int coachnum = 1;
		int seatnum = 1;
		int stationnum = 5;
		for (int threadnum : threadnums) {
			final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
			Thread[] threads = new Thread[threadnum];
			for (int i = 0; i < threadnum; i++) {
				threads[i] = new Thread(new Runnable() {
					public void run() {
						for (int i = 0; i < testnum; i++) {
							Ticket t = tds.buyTicket("a"+i, 1, 1, 3);
							if (t != null) {
								System.out.println("buy success");
							}
							boolean b = tds.refundTicket(t);
							if (b) {
								System.out.println("refund success");
							}
						}
						// TODO
					}
				});
				threads[i].start();
				// TODO
			}
		}
	}
}
