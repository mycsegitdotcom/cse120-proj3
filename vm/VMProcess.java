package nachos.vm;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

/**
 * A <tt>UserProcess</tt> that supports demand-paging.
 */
public class VMProcess extends UserProcess {

	/* public void initpg(int vpn, int kernpg) {
		int ppn = kernpg;

		if(pageTable[vpn].dirty == true){
			VMKernel.swapLoad(pageTable[vpn].ppn, ppn);
		} else if (pageTable[vpn].dirty == false && vpn < numCoffp){
			coffMap[vpn].section.loadPage(coffMap[vpn].sectionPage, ppn);
		} else {
			byte of_zero = 0;
			VMKernel.mlock.acquire();
			int paddr = pageSize * ppn;
			byte[] mem = Machine.processor().getMemory();
			Arrays.fill(mem, paddr, (paddr + pageSize - 1), of_zero);
			VMKernel.mlock.release();
		}
		pageTable[vpn].used = true;
		pageTable[vpn].valid = true;
		pageTable[vpn].ppn = ppn;
	} */

	/**
	 * Allocate a new process.
	 */
	public VMProcess() {
		super();
	}

	/**
	 * Save the state of this process in preparation for a context switch.
	 * Called by <tt>UThread.saveState()</tt>.
	 */
	public void saveState() {
		super.saveState();
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
	public void restoreState() {
		super.restoreState();
	}

	/**
	 * Initializes page tables for this process so that the executable can be
	 * demand-paged.
	 * 
	 * @return <tt>true</tt> if successful.
	 */
	protected boolean loadSections() {
		return super.loadSections();
	}

	/**
	 * Release any resources allocated by <tt>loadSections()</tt>.
	 */
	protected void unloadSections() {
		super.unloadSections();
	}

	/**
	 * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>
	 * . The <i>cause</i> argument identifies which exception occurred; see the
	 * <tt>Processor.exceptionZZZ</tt> constants.
	 * 
	 * @param cause the user exception that occurred.
	 */
	public void handleException(int cause) {
		Processor processor = Machine.processor();

		switch (cause) {
		case Processor.exceptionTLBMiss:
			int address = Machine.processor().readRegister(Processor.regBadVAddr);
			int vpn = Processor.pageFromAddress(address);
			TranslationEntry tlbEntry = pageTable[vpn];
			if(!tlbEntry.valid){
				int pgn = VMKernel.getPage(this, vpn);
				initpg(pgn, vpn);
			}
			handleTLBMiss(vpn);
			break;
		default:
			super.handleException(cause);
			break;
		}
	}

	public void handleTLBMiss(int vaddr) {
		int allow2enter = CONSTANT_NOT_ALLOWED;
		for ( int i = 0; i < Machine.processor().getTLBSize(); i++ ) {
			TranslationEntry tlbEntry = Machine.processor().readTLBEntry(i);
			if(!(tlbEntry.valid))
				allow2enter = i;
		}
		if(allow2enter == CONSTANT_NOT_ALLOWED)
			allow2enter = rng.nextInt(Machine.processor.getTLBSize());
		Machine.processor().writeTLBEntry(allow2enter, pageTable[vaddr]);
	}

	public int writeVirtualMemory(byte[] data, int len, int offset, int vaddr) {
		int byteNumber = 0;
		VMKernel.mlock.acquire();
		int vpn = Processor.pageFromAddress(vaddr + byteNumber);
		int ppn = pageTable[vpn].ppn;
		VMKernel.ipt[ppn].used = true;
		VMKernel.ipt[ppn].pin = true;
		pageTable[vpn].dirty = true;
		byteNum = super.writeVirtualMemory(data, len, offset, vaddr);
		VMKernel.ipt[ppn].pin = false;
		VMKernel.mlock.release();

		return byteNumber;
	}

	public int readVirtualMemory(byte[] data, int len, int offset, int vaddr) {
		int byteNumber = 0;
		VMKernel.mlock.acquire();
		int vpn = Processor.pageFromAddress(vaddr + byteNumber);
		int ppn = pageTable[vpn].ppn;
		VMKernel.ipt[ppn].used = true;
		VMKernel.ipt[ppn].pin = true;
		byteNum = super.readVirtualMemory(data, len, offset, vaddr);
		VMKernel.ipt[ppn].pin = false;
		VMKernel.mlock.release();

		return byteNumber;
	}

	public 

	private static final int pageSize = Processor.pageSize;

	private static final char dbgProcess = 'a';

	private static final char dbgVM = 'v';

	public static final int CONSTANT_NOT_ALLOWED = -420;
	public static Random rng = new Random();
}
