package nachos.vm;

import nachos.machine.*;

public class invert_page_info {

    public invert_page_info(){ 
	    this.tEntry = null;
        this.used = false;
        this.pin = false;
        this.occupied = false;
        this.process = null;
    }

    public invert_page_info(TranslationEntry tEntry, boolean used, boolean pinned, boolean assigned, VMProcess process){ 
	    this.tEntry = tEntry;
        this.used = used;
        this.pin = pinned;
        this.occupied = assigned;
        this.process = process;
    }

    public TranslationEntry tEntry;
    public boolean used;
    public boolean pin;
    public boolean occupied;
    public VMProcess process;
}