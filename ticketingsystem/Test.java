package ticketingsystem;

public class Test {

	public static void main(String[] args) throws InterruptedException {
		int[] threadnums = {4, 8, 16, 32, 64};
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
						// TODO
					}
				});
				// TODO
			}
		}
	}
}
