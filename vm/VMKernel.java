package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * A kernel that can support multiple demand-paging user processes.
 */
public class VMKernel extends UserKernel {

	public void initialize(String[] args) {
		super.initialize(args);

		swapFile = this.fileSystem.open(swpnm, true);
		swap_p_count = 0;
		freeswp = new PriorityQueue<Integer>();
		pgnum = Machine.processor().getNumPhysPages();
		ipt = new invert_page_info[pgnum];
		initIPT(pgnum);
		mlock = new Lock();
	}

	private void initIPT(int pgnum) {
		for(int i = 0; i < pgnum; i++){
			ipt[i] = new invert_page_info(new TranslationEntry(), false, false, false, null);
		}
	}

	/**
	 * Allocate a new VM kernel.
	 */
	public VMKernel() {
		super();
	}

	/**
	 * Test this kernel.
	 */
	public void selfTest() {
		super.selfTest();
	}

	/**
	 * Start running user programs.
	 */
	public void run() {
		super.run();
	}

	/**
	 * Terminate this kernel. Never returns.
	 */
	public void terminate() {
		super.terminate();
	}

	public static int getPage(VMProcess p, int vpn) {
		int supposed_vacant_page = 1;
		mlock.acquire();
		supposed_vacant_page = clock();
		if(ipt[supposed_vacant_page].occupied)
			p_evict(supposed_vacant_page);
	    p_assign(supposed_vacant_page, p, vpn);
	    mlock.release();
	    return supposed_vacant_page;
	}

	public static void p_assign(int ppn, VMProcess p, int vpn) {
		invert_page_info newipt = invert_page_info[ppn];
		newipt.occupied = true;
		newipt.used = true;
		newipt.process = process;
		newipt.tEntry.ppn = ppn;
		newipt.tEntry.vpn = vpn;
		newipt.tEntry.valid = true;
	}

	public static void swapLoad(int ppn, int spn) {
		int physAddr = pageSize * ppn;
		int swapAddr = spn * pageSize;
		byte[] mem = Machine.processor().getMemory();
		swapFile.read(swapAddr, mem, physAddr, pageSize);
		freeswp.add(spn);
	}

	private static int clock() {
		int total = 0
		boolean pin = true;
		curr = curr % pgnum;

		while(true){
			invert_page_info icurr = ipt[curr];

			if(!icurr.occupied){
				curr++;
				return (curr - 1);
			}

			if(icurr.pin){
				icurr.used = false;
			}
			else if(icurr.used){
				icurr.used = true;
				curr++;
				return curr - 1;
			}
			else{
				pin = false;
				icurr.used = false;
				curr++;
				total++;
			}

			curr = curr % pgnum;
		}
	}

	public static int addSwap(int ppn) {
		int spn;
		if (freeswp.size() == 0)
			spn = swap_p_count++;
		else 
			spn = freeswp.poll();
		
		int paddr = pageSize * ppn;
		int swapaddr = pageSize * spn;
		byte[] mem = Machine.processor().getMemory();
		swapFile.write(swapaddr, mem, paddr, pageSize);

		return spn;
	}

	private static void evictPage(int ppn) {
		int vpn = ipt[ppn].tEntry.vpn;
		VMProcess p = ipt[ppn].process;
		process.savePage(vpn);
	}

	// dummy variables to make javac smarter
	private static VMProcess dummy1 = null;

	private static final char dbgVM = 'v';

	public static invert_page_info[] ipt;
	private static OpenFile swapFile;
	private static int curr = 0;
	private static int pgnum;
	private static Lock mlock;
	private static PriorityQueue<Integer> freeswp;
	private static int swap_p_count;
}
