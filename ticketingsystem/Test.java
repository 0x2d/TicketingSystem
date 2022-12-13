package ticketingsystem;

import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import java.io.File;
import java.io.FileNotFoundException;

class ThreadId {
    // Atomic integer containing the next thread ID to be assigned
    private static final AtomicInteger nextId = new AtomicInteger(0);

    // Thread local variable containing each thread's ID
    private static final ThreadLocal<Integer> threadId =
        new ThreadLocal<Integer>() {
            @Override protected Integer initialValue() {
                return nextId.getAndIncrement();
        }
    };

    // Returns the current thread's unique ID, assigning it if necessary
    public static int get() {
        return threadId.get();
    }
}

public class Test {
	static int threadnum = 16;
	static int testnum = 10000;
	static int totalPc;

	static int routenum = 3;
	static int coachnum = 3;
	static int seatnum = 5;
	static int stationnum = 5;

	static int refRatio = 10; 
	static int buyRatio = 30; 
	static int inqRatio = 60; 

	static TicketingDS tds;
	final static List<String> methodList = new ArrayList<String>();
	final static List<Integer> freqList = new ArrayList<Integer>();
	final static List<Ticket> currentTicket = new ArrayList<Ticket>();
	final static List<String> currentRes = new ArrayList<String>();
    final static ArrayList<List<Ticket>> soldTicket = new ArrayList<List<Ticket>>();
	volatile static boolean initLock = false;
//	final static AtomicInteger tidGen = new AtomicInteger(0);
	final static Random rand = new Random();

	public static void initialization(){
		tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
		for(int i = 0; i < threadnum; i++){
			List<Ticket> threadTickets = new ArrayList<Ticket>();
			soldTicket.add(threadTickets);
			currentTicket.add(null);
			currentRes.add("");
		}

		methodList.add("refundTicket");
		freqList.add(refRatio);
		methodList.add("buyTicket");
		freqList.add(refRatio+buyRatio);
		methodList.add("inquiry");
		freqList.add(refRatio+buyRatio+inqRatio);
		totalPc = refRatio+buyRatio+inqRatio;
	}

	public static String getPassengerName() {
		long uid = rand.nextInt(testnum);
		return "passenger" + uid; 
	}

	private static boolean readConfig(String filename) {
		try {
			Scanner scanner = new Scanner(new File(filename));

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				//System.out.println(line);
				Scanner linescanner = new Scanner(line);
				if (line.equals("")) {
					linescanner.close();
					continue;
				}
				if (line.substring(0,1).equals("#")) {
					linescanner.close();
					continue;
				}
				routenum = linescanner.nextInt();
				coachnum = linescanner.nextInt();
				seatnum = linescanner.nextInt();
				stationnum = linescanner.nextInt();

				refRatio = linescanner.nextInt();
				buyRatio = linescanner.nextInt();
				inqRatio = linescanner.nextInt();
				//System.out.println("route: " + routenum + ", coach: " + coachnum + ", seatnum: " + seatnum + ", station: " + stationnum + ", refundRatio: " + refRatio + ", buyRatio: " + buyRatio + ", inquiryRatio: " + inqRatio);
				linescanner.close();
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			System.out.println(e);
		}
		return true;
	}

	public static boolean execute(int num){
		int route, departure, arrival;
		Ticket ticket = new Ticket();;
		switch(num){
		case 0://refund
			if(soldTicket.get(ThreadId.get()).size() == 0)
				return false;
			int n = rand.nextInt(soldTicket.get(ThreadId.get()).size());
			ticket = soldTicket.get(ThreadId.get()).remove(n);
			if(ticket == null){
				return false;
			}
			currentTicket.set(ThreadId.get(), ticket);
			boolean flag = tds.refundTicket(ticket);
			currentRes.set(ThreadId.get(), "true"); 
			return flag;
		case 1://buy
			String passenger = getPassengerName();
			route = rand.nextInt(routenum) + 1;
			departure = rand.nextInt(stationnum - 1) + 1;
			arrival = departure + rand.nextInt(stationnum - departure) + 1;
			ticket = tds.buyTicket(passenger, route, departure, arrival);
			if(ticket == null){
				ticket = new Ticket();
				ticket.passenger = passenger;
				ticket.route = route;
				ticket.departure = departure;
				ticket.arrival = arrival;
				ticket.seat = 0;
				currentTicket.set(ThreadId.get(), ticket);
				currentRes.set(ThreadId.get(), "false");
				return true;
			}
			currentTicket.set(ThreadId.get(), ticket);
			currentRes.set(ThreadId.get(), "true");
			soldTicket.get(ThreadId.get()).add(ticket);
			return true;
		case 2:
			ticket.passenger = getPassengerName();
			ticket.route = rand.nextInt(routenum) + 1;
			ticket.departure = rand.nextInt(stationnum - 1) + 1;
			ticket.arrival = ticket.departure + rand.nextInt(stationnum - ticket.departure) + 1; // arrival is always greater than departure
			ticket.seat = tds.inquiry(ticket.route, ticket.departure, ticket.arrival);
			currentTicket.set(ThreadId.get(), ticket);
			currentRes.set(ThreadId.get(), "true"); 
			return true;
		default:
			System.out.println("Error in execution.");
			return false;
	  }
	}

  	public static void main(String[] args) throws InterruptedException {
		threadnum = Integer.parseInt(args[0]);
		readConfig("TrainConfig");
		initialization();
		Thread[] threads = new Thread[threadnum];
		long[] methodTime = {0, 0, 0};
		int[] methodNum = {0, 0, 0};
		final long startTime = System.nanoTime();
			
		for (int i = 0; i < threadnum; i++) {
				threads[i] = new Thread(new Runnable() {
					public void run() {
						for(int k = 0; k < testnum; k++){
							int sel = rand.nextInt(totalPc);
							int cnt = 0;
							for(int j = 0; j < methodList.size(); j++){
								if(sel >= cnt && sel < cnt + freqList.get(j)){
									long preTime = System.nanoTime();
									boolean flag = execute(j);
									long postTime = System.nanoTime();
									cnt += freqList.get(j);
									if (flag) {
										methodTime[j] += postTime - preTime;
										methodNum[j]++;
									}
								}
							}
						}
					}
				});
				threads[i].start();
		}

		for (int i = 0; i< threadnum; i++) {
			threads[i].join();
		}

		final long endTime = System.nanoTime();
		double totalTime = (double)(endTime-startTime) / 1000000.0;
		System.out.printf("ThreadNum: %d, TestNum: %d, TotalTime: %.2f ms.\n%s: %.2f ms, %s: %.2f ms, %s: %.2f ms.\nThroughout: %.2f op/ms.\n",
			threadnum, testnum, totalTime, 
			methodList.get(0), (double)(methodTime[0]) / (1000000.0 * (double)methodNum[0]), 
			methodList.get(1), (double)(methodTime[1]) / (1000000.0 * (double)methodNum[1]), 
			methodList.get(2), (double)(methodTime[2]) / (1000000.0 * (double)methodNum[2]), 
			(double)(methodNum[0]+methodNum[1]+methodNum[2]) / totalTime);
	}
}
