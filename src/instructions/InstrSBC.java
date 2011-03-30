package instructions;

import org.apache.log4j.Logger;

import machine.MachineState;
import machine.SREG;

public class InstrSBC extends Instr {
	final private int rd;
	final private int rr;
	private final static int bitMask = 0xFF;
	private final static int msbMask = 0x80;
	private final static int bit7Mask = 0x40;
	private static final Logger logger = Logger.getLogger(InstrSBC.class);
	public InstrSBC(MachineState machine, int rd, int rr) throws MalformedInstruction
	{
		super(machine);
		if(rd < 0 || rd > 31)
		{
			throw new MalformedInstruction("Invalid register number rd("+rd+")");
		}

		if(rr < 0 || rr > 31)
		{
			throw new MalformedInstruction("Invalid register number rr("+rr+")");
		}
		this.rd = rd;
		this.rr = rr;
	}

	@Override
	public String toString() {
		return "SBC " +rd+", " +rr;
	}

	@Override
	public void execute() throws RuntimeError {
		logger.debug("Executing sbc...");
		SREG newStatus = machine.getSREG();
		int dst = machine.getRegister(rd);
		int src = machine.getRegister(rr);
		//int nMsb = (dst & msbMask) & (src & msbMask);
		int result;
		if(machine.getSREG().isC())
		{
			result = dst-src-1;			
		}
		else
		{
			result = dst-src;
		}
		if(Math.abs(src) > Math.abs(dst))
		{
			newStatus.setC(true);
		}
		else
		{
			newStatus.setC(false);
		}
		this.event.setRd(rd, result);
		this.event.setPC(machine.getPC()+1);
		
		//update the SREG
		//check zero.
		if(result == 0)
		{
			newStatus.setZ(true);
		}
		else
		{
			newStatus.setZ(false);
		}

		//check for the V bit.
		// Rd7 and !Rr7 and !R7 + !Rd7 and Rr7 and R7
        // Set if two’s complement overflow resulted 
        // from the operation; cleared otherwise.
        if ( ((bit7Mask & dst)>0 && (bit7Mask & src)==0 && (bit7Mask&result)==0) 
		  || ((bit7Mask & dst)==0 && (bit7Mask & src)>0 && (bit7Mask&result)>0) 
		   )
		{
			newStatus.setV(true);
			logger.trace("Setting V to true");
		}
		else
		{
			newStatus.setV(false);
			logger.trace("Setting V to false");
		}
		
		//check the msb, if it is set, set the N bit in the SREG. 
		if((result & msbMask) == msbMask)
		{
			newStatus.setN(true);
		}
		else
		{
			newStatus.setN(false);
		}
		
		newStatus.setS(newStatus.isN() ^ newStatus.isV());
		this.event.setSREG(newStatus);
		
	}
}
