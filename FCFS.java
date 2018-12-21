package scheduler;
import java.util.ArrayList;

public class FCFS extends Scheduler {
	int t;
	double blocked;
	int num;
	int finished;
	int running;
	PQ pq;
	ArrayList<Process> blockedProcesses;
	ArrayList<Process> readyProcesses;
	Process current = null;
	
	public FCFS(int num) {
		this.t = 0;
		this.blocked = 0;
		this.num = num;
		this.finished = 0;
		this.running = 0;
		this.pq = new PQ();
		this.blockedProcesses = new ArrayList<Process>();
	}
	
	public void runFCFS(ArrayList<Process> processes, boolean verboseFlag) {
		System.out.print("The original input was: " + num + " ");
		for(int i = 0; i < num; i++){
			System.out.print("(" + processes.get(i).A + " " + processes.get(i).B + " " + processes.get(i).C + " " + processes.get(i).M + ") ");
		}
		System.out.println();
		System.out.print("The (sorted) input is:  " + num + " ");
		for(int i = 0; i < num; i++){
			System.out.print("(" + processes.get(i).A + " " + processes.get(i).B + " " + processes.get(i).C + " " + processes.get(i).M + ") ");
		}
		System.out.println("\n");
		
		while(finished < num) {					
			if(verboseFlag)
				printVerbose(processes, num, t, false);
			if(blockedProcesses.size() > 0)
				blocked++;		
			readyProcesses = null;
			readyProcesses = new ArrayList<Process>();

			for(int i = 0; i < blockedProcesses.size(); i++) {
				if(blockedProcesses.get(i).ioBurst > 0) {
					blockedProcesses.get(i).ioBurst--;
					blockedProcesses.get(i).ioTime++;
				}
				if(blockedProcesses.get(i).ioBurst == 0) {
					blockedProcesses.get(i).status = status[1];
					readyProcesses.add(blockedProcesses.get(i));
					blockedProcesses.remove(i);
					i--;
				}
			}
			
			while(readyProcesses.size() > 0) {
				int minIndex = 0;
				int tieBreaker = 999;
				for(int i = 0; i< readyProcesses.size(); i++) {
					Process current = readyProcesses.get(i);
					if(current.processNum < tieBreaker) {
						tieBreaker = current.processNum;
						minIndex = i;
					}
				}
				Process toAdd = readyProcesses.get(minIndex);
				readyProcesses.remove(minIndex);
				pq.putProcess(toAdd);
				
			}
			
			if(current != null) {
				current.cpuBurst--;
				current.remaining--;		
				if(current.remaining == 0) {
					finished++;
					current.finishTime = t;
					current.status = status[4];
					current = null;
				}
				else if(current.cpuBurst == 0) {
					current.ioBurst = current.M * current.lastBurst;
					current.status = status[3];
					blockedProcesses.add(current);
					current = null;
				}				
			}

			for(int i = 0; i < processes.size(); i++) {
				if(processes.get(i).A == t) {
					processes.get(i).remaining = processes.get(i).C;
					processes.get(i).status = status[1];
					pq.putProcess(processes.get(i));
				}
			}

			if(current == null) {
				if(pq.length() > 0){
					current = pq.getProcess();
					current.status = status[2];									
					if(running > 0 && current.remaining >= running) {
						current.cpuBurst = running;
						current.lastBurst = current.cpuBurst;
						running = 0;
					}					
					else if(running > 0 && current.remaining < running) {
						current.cpuBurst = current.remaining;
						current.lastBurst = current.cpuBurst;
						running -= current.cpuBurst;
					}			
					else {
						current.cpuBurst = randomOS(current.B);		
						current.lastBurst = current.cpuBurst;
					}
				}
			}			
			if(pq.length() > 0)
				pq.add();
			if(finished != num)
				t++;			
		}
		System.out.println("The scheduling algorithm used was First Come First Serve\n");
		print(processes, num, t, blocked);
	}
}
