package scheduler;

public class Process {
	int A, B, C, M;
	String status = "unstarted";
	int processNum;
	int remaining;
	int cpuBurst;
	int lastBurst;
	int ioBurst;
	int q;
	double r;
	int finishTime;
	int ioTime;
	int waiting = 0;	

	public Process(int a, int b, int c, int m){
		this.A = a; this.B = b; this.C = c; this.M = m;
	}

	public Process(){

	}		

	public String ProcessPrint(){
		return String.format("%s %s %s %s %s",this.A, this.B, this.C, this.M, this.r);
	}	
}
