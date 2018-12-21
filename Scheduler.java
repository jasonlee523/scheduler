package scheduler;
import java.io.*;
import java.util.*;

public class Scheduler {
	static int numP;	
	static ArrayList<Process> processes = new ArrayList<Process>();
	static String algoToUse = null;
	static class Comparator_ implements Comparator<Process>{
		public int compare(Process x, Process y){
			int val = x.A - y.A;
			if (val == 0)
				return x.processNum - y.processNum;
			else
				return val;
		}
	}
	static Comparator_ c = new Comparator_();
	
	public static void main(String[] args) throws IOException {
		boolean verboseFlag =  false;
		String fileName = null, verbose = "";
		if(args.length == 2){
			algoToUse = args[0];
			fileName = args[1];
		} else if(args.length == 3) {
			algoToUse = args[0];
			verbose = args[1];
			fileName = args[2];
		} else {
			System.out.println("Incorrect number of arguments.");
			return;
		}		
		if(verbose.equals("--verbose"))
			verboseFlag = true;
		
		FileReader fr = new FileReader(fileName);
		Scanner in = new Scanner(fr);
		int n = in.nextInt();
		for(int i = 0; i < n; i++) {
			Process temp = new Process(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
			temp.remaining = temp.C;
			processes.add(temp);
			temp = null;
		}
		in.close();

		Collections.sort(processes, c);		
		for(int i = 0; i < processes.size(); i++)
			processes.get(i).processNum = i;

		switch(algoToUse) {
		case "FCFS":
			FCFS fcfs = new FCFS(n);
			fcfs.runFCFS(processes, verboseFlag);
			break;
		case "HPRN":
			HPRN hprn = new HPRN(n);
			hprn.runHPRN(processes, verboseFlag);
			break;
		case "RR":
			RR rr = new RR(n);
			rr.runRR(processes, verboseFlag);
			break;
		case "SJF":
			SJF sjf = new SJF(n);
			sjf.runSJF(processes, verboseFlag);
			break;
		default:
			System.out.println("Incorrect arguments.");
			break;
		}	
	}
	
	static String[] status = {"unstarted", "ready", "running", "blocked", "terminated"};
	static int[] randomNums = null;
	static int r = 0;
	
	public static int randomOS(int u) {
		int num = 0;
		if(randomNums == null){
			try {
				randomNums = new int[9999999];
				FileReader fr = new FileReader("scheduler_lab/random-numbers.txt");
		    	Scanner sc = new Scanner(fr);
		    	int i = 0;
		    	while(sc.hasNextInt())
		    		randomNums[i++] = sc.nextInt();
		    	sc.close();
			}
		    catch(IOException e) {
		        System.err.println("Error reading file 'random-numbers.txt'");   
		    }
		}
		if(randomNums != null) {
	    	int x = randomNums[r++];
	    	num = 1+(x%u);
		}
		else
			System.err.println("RandomOS Error");
		return num;
	}

	public static void print(ArrayList<Process> processes, int num, int t, double blocked) {		
		double finalTA = 0;
		double finalIO = 0;
		double finalWait = 0;	
		for(int i=0; i<num; i++) {
			System.out.println("Process " + i+ ":");
			System.out.println("\t(A,B,C,M) = (" + processes.get(i).A + "," + processes.get(i).B + "," + processes.get(i).C + "," + processes.get(i).M + ")");
			System.out.println("\tFinishing time: " + processes.get(i).finishTime);
			finalTA += processes.get(i).finishTime - processes.get(i).A;
			System.out.println("\tTurnaround time: " + (processes.get(i).finishTime - processes.get(i).A));			
			finalIO += processes.get(i).ioTime;
			System.out.println("\tI/O time: " + processes.get(i).ioTime);		
			finalWait +=processes.get(i).waiting;
			System.out.println("\tWaiting time: " + processes.get(i).waiting + "\n");
		}	
		double finalRun = finalTA - finalIO - finalWait;
		System.out.println("Summary Data: ");
		System.out.println("\tFinishing time: " + t);
		System.out.println("\tCPU Utilization: " + String.format("%.6f", (finalRun/t)));
		System.out.println("\tI/O Utilization: " +  String.format("%.6f", ((float)blocked/t)) ); 
		System.out.printf("\tThroughput: %.6f processes per hundred cycles\n", (((double) num * 100)/t));
		System.out.printf("\tAverage turnaround time: %.6f\n", (finalTA/num));
		System.out.printf("\tAverage waiting time: %.6f\n", (finalWait/num));
	}
	
	public static void printVerbose(ArrayList<Process> processes, int num, int t, boolean rr) {
		System.out.printf("Before cycle %4d:     ", t);
		for(int i = 0; i < num; i++) {
			int x;
			Process p = processes.get(i);
			String status = p.status;
			switch(status) {
				case "ready":
					if(rr)
						x = 0;
					else
						x = p.cpuBurst;
					break;
				case "running":
					if(rr)
						x = p.q;
					else
						x = p.cpuBurst;
					break;
				case "blocked":
					x = p.ioBurst;
					break;
				default:
					x = 0;
					break;
			}
			System.out.printf("%14s", status + " " + x + "");
		}
		System.out.println();
	}
	
	public class PQ {
	    int num;
	    Node first;
	    Node last;
	    class Node {
	        Process p;
	        Node next;
	    }
	    public PQ() {
	        num = 0;
	        first = last = null;
	    }
	    void add() {
	    	Node current = first;
	    	while(current != last) {
	    		Process c = current.p;
	    		c.waiting++;
	    		current = current.next;
	    	}
	   		last.p.waiting++;    		
	    }
	    public int length() {
	    	return num;
	    }
	    public Process getProcess() {
	        synchronized(this) {
	            Process p = null;
	            if(num > 0) {
	                p = first.p;
	                first = first.next;
	                num--;
	                if(num == 0)
	                    last = null;
	            } 
	            return p;
	        }
	    }
	    public void putProcess(Process p) {
	    	Node newNode = new Node();
	        newNode.p = p;
	        newNode.next = null;
	        if (num == 0)
	            first = last = newNode;
	        else {
	            last.next = newNode;
	            last = newNode;
	        }
	        num++;
	    }
	}
}

