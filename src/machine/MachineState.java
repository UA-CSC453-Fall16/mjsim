package machine;

import instructions.ATmegaProgram;
import instructions.Function;
import instructions.Instr;
import instructions.InstrLabel;
import instructions.RuntimeError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import machine.functions.Func;
import machine.functions.FuncDisplaySlate;
import machine.functions.FuncMalloc;
import machine.functions.FuncSetPix;

import org.apache.log4j.Logger;

public class MachineState {
	final private static Logger logger = Logger.getLogger(MachineState.class);
	final private String ledGrid[][] = new String[8][8];
	final private String displaySlate[][] = new String[8][8];
	final private Map<Integer, Integer> stack;
	final private Map<Integer, Integer> heap;
	final private Map<Integer, Integer> registers;
	final private ArrayList<Instr> programSpace;
	final private Map<String, Integer> functionMapping;
	final private Map<String, Integer> labelMapping;
	final private Map<String, Integer> labelJumps;
	final private Map<String, Func> predefinedFunctions = new HashMap<String, Func>();
	final private String name;
	private int labelJmps = 10;// default value.
	private boolean batch = true;
	private int pc = 0;// set to the beginning of the programSpace.
	private boolean finished = false;
	private int stackPointer = 0x3e3d; // this value is taken from the SPL and
										// SPH from the main.s file.
	private int returnAddress = -1;
	final private SREG statusReg;

	public MachineState(String name) {
		// Using a TreeMap instead of a HashMap because after a large number of
		// addresses have been accessed
		// the speed of the HashMap will greatly decrease. The number of
		// registers however is small enough
		// that a HashMap will give a better speed.
		stack = new TreeMap<Integer, Integer>();
		heap = new TreeMap<Integer, Integer>();
		registers = new HashMap<Integer, Integer>();
		labelJumps = new HashMap<String, Integer>();// This will be used to
													// determine how many jumps
													// a function makes before
													// we exit.
		programSpace = new ArrayList<Instr>();
		functionMapping = new HashMap<String, Integer>();// This records the
															// function name and
															// maps it to a
															// integer where the
															// function starts
															// in the
															// programSpace.
		labelMapping = new HashMap<String, Integer>();// This records the
														// mapping from a label
														// (not a function) to a
														// pc value.
		statusReg = new SREG();
		this.name = name;
		// set up the return address for main
		stack.put(stackPointer, 0xFF);
		stack.put(stackPointer - 1, 0xFF);
		stackPointer -= 2;

		// init the 2 slate datastructures.
		for (int x = 0; x < 8; x++) {
			for (int y = 0; y < 8; y++) {
				displaySlate[x][y] = "NONE";
				ledGrid[x][y] = "NONE";
			}
		}

		// add all of the predefined functions
		predefinedFunctions.put("_Z6DrawPxhhh", new FuncSetPix(this));
		predefinedFunctions.put("_Z12DisplaySlatev", new FuncDisplaySlate(this));
		predefinedFunctions.put("malloc", new FuncMalloc(this));
	}

	public MachineState(String name, boolean batch) {
		this(name);
		this.batch = batch;
	}

	public MachineState(String name, int jmps) {
		this(name);
		this.batch = true;
		this.labelJmps = jmps;
	}

	/**
	 * Executes the current instruction at PC.
	 * 
	 * @throws RuntimeError
	 */
	public void executeInstruction() throws RuntimeError {
		logger.trace("Execute Instruction: Pc value is (" + pc + ")");

		final Instr currentInstr = programSpace.get(pc);
		// check to see if we are in batch mode, and if so check to see how many
		// times we have hit the label.
		if (batch && (currentInstr instanceof InstrLabel)) {
			logger.debug("We are in batch mode and our current instr is a Label.");
			Integer currentJmps = labelJumps.get(currentInstr.toString());
			if (currentJmps != null) {
				logger.debug("Max jumps: " + labelJmps);
				if (currentJmps > labelJmps) {
					throw new RuntimeError(
							"Hit max number of jumps for label: "
									+ currentInstr.toString());
				}
				currentJmps++;
				labelJumps.put(currentInstr.toString(), currentJmps);
			} else {
				labelJumps.put(currentInstr.toString(), new Integer(0));
			}

		}
		currentInstr.executeWrapper();
		// after this instruction the pc value should be updated.
		if ((pc >= programSpace.size()) || (pc == 0xFFFF))// all Fs should be
															// the return
															// address for the
															// main function.
		{
			finished = true;
		}
	}

	public String getDisplaySlate(int x, int y) {
		return displaySlate[x][y];
	}

	public Integer getFunction(String functionName) {
		return functionMapping.get(functionName);
	}

	public String getGridColor(int x, int y) {
		return ledGrid[x][y];
	}

	/**
	 * Returns a value from within the heap space.
	 * 
	 * @param address
	 * @return Returns the value at the address within the heap space, returns
	 *         null if that address does not exist.
	 */
	public Integer getHeap(int address) {
		final Integer tempReg = heap.get(address);
		return tempReg;
	}

	public Integer getLabel(String labelName) {
		return labelMapping.get(labelName);
	}

	/**
	 * Returns a value from anywhere within the memory space, include the heap
	 * and the stack space.
	 * 
	 * @param address
	 * @return The value located at the provided memory address, null if the
	 *         address has not been assigned to.
	 */
	public Integer getMemory(int address) {
		Integer memValue = null;
		if ((memValue = heap.get(address)) == null) {
			return memValue;
		} else if ((memValue = stack.get(address)) == null) {
			return memValue;
		}
		return memValue;
	}

	/**
	 * Returns the current value of the PC.
	 * 
	 * @return
	 */
	public int getPC() {
		return pc;
	}

	public Func getPreDefinedFunction(String functionName) {
		return predefinedFunctions.get(functionName);
	}

	/**
	 * Returns the value of the register (reg).
	 * 
	 * @param reg
	 *            The register to get the value of.
	 * @return Returns the value of the register.
	 */
	public int getRegister(int reg) {
		final Integer tempReg = registers.get(reg);
		if (tempReg == null)// check to make sure it is not null. if it is
							// return 0.
		{
			return 0;
		}
		return tempReg;
	}

	public int getReturnAddress() {
		return returnAddress;
	}

	/**
	 * Return a copy of the SREG, changing the SREG that is returned here will
	 * not change the SREG that is inside of the machine state. You must use the
	 * UpdateEvent to edit the machine state SREG
	 * 
	 * @return
	 */
	public SREG getSREG() {
		return (SREG) statusReg.clone();
	}

	public int getStack(int address) {
		final Integer tempReg = stack.get(address);
		if (tempReg == null) {
			return 0;
		}
		return tempReg;
	}

	/**
	 * Return the current top of the stack.
	 * 
	 * @return The integer representation of the memory address of the top of
	 *         the stack.
	 */
	public int getStackPointer() {
		return stackPointer;
	}

	/**
	 * If there are still instructions to be ran, meaning we have not returned
	 * from main, return true.
	 * 
	 * @return
	 */
	public boolean hasNextInstr() {
		return !finished;
	}

	public boolean isPredefinedFunction(String functionName) {
		return predefinedFunctions.containsKey(functionName);
	}

	/**
	 * This function will init the program space so that we have a mapping from
	 * a pc integer value in the program space to an instruction, it will also
	 * map a function call to a address in the program space.
	 * 
	 * @param program
	 *            The ATmega assembly program to read into the MachineState.
	 */
	public void readAtmegaProgram(ATmegaProgram program) {
		logger.debug("Entered into the readATmegaProgram to read the ATmegaProgram object into the machine program state.");
		final List<Function> functions = program.getFunctions();
		logger.debug("There are (" + functions.size()
				+ ") functions to read into the program space.");
		for (final Function func : functions) {
			// Get the current size of the programSpace arraylist and use that
			// for the mapping.
			final int tempIndex = programSpace.size();
			final List<Instr> instructions = func.getInstructions();
			for (final Instr instr : instructions) {
				logger.trace("Adding new Instruction to ATmegaProgram: <"
						+ instr.getClass().toString() + "> " + instr.toString());
				if (instr instanceof InstrLabel) {
					logger.trace("Adding label: " + instr.toString());
					labelMapping.put(((InstrLabel) instr).getLabel(),
							programSpace.size());
				}
				programSpace.add(instr);
			}
			functionMapping.put(func.getName(), tempIndex);
			logger.info("Adding Function (" + func.getName()
					+ ") to the program space at index (" + tempIndex + ")");
		}
	}

	public void setDisplaySlate(int x, int y, String color) {
		if(color == null)
		{
			logger.fatal("Color is null.");
		}
		displaySlate[x][y] = color;
	}

	public void setGridColor(int x, int y, String color) {
		ledGrid[x][y] = color;
	}

	public void setReturnAddress(int address) {
		this.returnAddress = address;
	}

	@Override
	public String toString() {
		return "MachineState: " + name;
	}

	private void updateSREG(SREG statusRegisterCopy) {
		if (statusRegisterCopy.isC() != statusReg.isC()) {
			statusReg.setC(statusRegisterCopy.isC());
		}
		if (statusRegisterCopy.isH() != statusReg.isH()) {
			statusReg.setH(statusRegisterCopy.isH());
		}
		if (statusRegisterCopy.isI() != statusReg.isI()) {
			statusReg.setI(statusRegisterCopy.isI());
		}
		if (statusRegisterCopy.isN() != statusReg.isN()) {
			statusReg.setN(statusRegisterCopy.isN());
		}
		if (statusRegisterCopy.isS() != statusReg.isS()) {
			statusReg.setS(statusRegisterCopy.isS());
		}
		if (statusRegisterCopy.isT() != statusReg.isT()) {
			statusReg.setT(statusRegisterCopy.isT());
		}
		if (statusRegisterCopy.isV() != statusReg.isV()) {
			statusReg.setV(statusRegisterCopy.isV());
		}
		if (statusRegisterCopy.isZ() != statusReg.isZ()) {
			statusReg.setZ(statusRegisterCopy.isZ());
		}
	}

	public void updateState(UpdateEvent event) {
		logger.info("Received UpdateEvent...processing...");

		if (event.getRd() != null) {
			final HashMap<Integer, Integer> rds = event.getRd();
			for(Integer key:rds.keySet())
			{
				logger.info("Updating register r(" + key
						+ ") with the value (" + rds.get(key) + ")");	
				registers.put(key, rds.get(key));				
			}
		}

		if (event.getMemory() != null) {
			final Pair<Integer, Integer> pair = event.getMemory();
			logger.info("Updating memory location (" + pair.getLeft()
					+ ") with the value (" + pair.getRight() + ")");
			stack.put(pair.getLeft(), pair.getRight());
		}

		if (event.getStackPointer() >= 0) {
			logger.info("Updating stack pointer to (0x"
					+ Integer.toHexString(event.getStackPointer()) + ")");
			this.stackPointer = event.getStackPointer();
		}

		if (event.getSREG() != null) {
			updateSREG(event.getSREG());
		}

		if (event.getLongMemory() != null) {
			logger.info("Adding long memory to the stack at (0x"
					+ Integer.toHexString(event.getLongMemory().getLeft())
					+ ")");
			// store the high value in the lower memory, store the low value in
			// high memory.
			final Pair<Integer, Integer> tempPair = event.getLongMemory();
			this.stack.put(tempPair.getLeft(),
					(tempPair.getRight() & 0xFF00) >> 8);// get the high 8 bits,
															// and shift them to
															// the right.
			this.stack
					.put(tempPair.getLeft() + 1, (tempPair.getRight() & 0xFF)); // get
																				// the
																				// low
																				// 8
																				// bits.
		}

		pc = event.getPc();// this should be set in every instruction.
	}
}